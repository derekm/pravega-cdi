package io.pravega.cdi;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.grpc.StatusRuntimeException;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.impl.ByteArraySerializer;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.local.LocalPravegaEmulator;
import io.pravega.test.common.TestUtils;

@ExtendWith(WeldJunit5Extension.class)
public class EventStreamProducersTest {

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(EventStreamProducers.class);

    static LocalPravegaEmulator localPravega;

    @BeforeAll
    public static void launchPravegaCluster() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        localPravega = LocalPravegaEmulator.builder()
                .controllerPort(9090)
                .segmentStorePort(TestUtils.getAvailableListenPort())
                .zkPort(TestUtils.getAvailableListenPort())
                .enableRestServer(false)
                .enableAuth(false)
                .enableTls(false)
                .build();
        Method startMethod = localPravega.getClass().getDeclaredMethod("start");
        startMethod.setAccessible(true);
        startMethod.invoke(localPravega);
    }

    @AfterAll
    public static void shutdown() throws Exception {
        localPravega.close();
    }

    /*
     * TEST WRITING AND READING USING BYTE ARRAYS
     */

    @Inject
    @PravegaConfig(scope = "streams",
                   stream = "test",
                   serializer = ByteArraySerializer.class)
    Instance<EventStreamWriter<byte[]>> eventStreamWriterInstance;

    @Inject
    @PravegaConfig(scope = "streams",
                   stream = "test",
                   serializer = ByteArraySerializer.class)
    Instance<EventStreamReader<byte[]>> eventStreamReaderInstance;

    @Test
    public void writeToAndReadFromPravegaUsingByteArray() throws InterruptedException, ExecutionException {
        final String expectHelloWorld = "Hello, world!";
        EventStreamWriter<byte[]> writer = eventStreamWriterInstance.get();
        writer.writeEvent(expectHelloWorld.getBytes()).get();
        EventStreamReader<byte[]> reader = eventStreamReaderInstance.get();
        byte[] bytes = reader.readNextEvent(1000).getEvent();
        assertEquals(expectHelloWorld, new String(bytes), "Message is not '" + expectHelloWorld + "'");
        eventStreamWriterInstance.destroy(writer);
        eventStreamReaderInstance.destroy(reader);
    }

    /*
     * TEST WRITING AND READING USING STRINGS
     */

    @Inject
    @PravegaConfig(scope = "streams",
                   stream = "test-string",
                   serializer = UTF8StringSerializer.class)
    Instance<EventStreamWriter<String>> eventStreamWriterStringInstance;

    @Inject
    @PravegaConfig(scope = "streams",
                   stream = "test-string",
                   serializer = UTF8StringSerializer.class)
    Instance<EventStreamReader<String>> eventStreamReaderStringInstance;

    @Test
    public void writeToAndReadFromPravegaUsingString() throws InterruptedException, ExecutionException {
        final String expectHelloWorld = "Hello, world!";
        EventStreamWriter<String> writer = eventStreamWriterStringInstance.get();
        writer.writeEvent(expectHelloWorld).get();
        EventStreamReader<String> reader = eventStreamReaderStringInstance.get();
        String string = reader.readNextEvent(1000).getEvent();
        assertEquals(expectHelloWorld, string, "Message is not '" + expectHelloWorld + "'");
        eventStreamWriterStringInstance.destroy(writer);
        eventStreamReaderStringInstance.destroy(reader);
    }

    /*
     * TEST ERROR WHEN SCOPE AND STREAM ARE NOT CREATED
     */

    @Inject
    @PravegaConfig(scope = "not-created",
                   createScope = false,
                   stream = "not-created",
                   createStream = false,
                   serializer = ByteArraySerializer.class)
    Instance<EventStreamWriter<byte[]>> noScopeNoStreamWriterInstance;

    @Test
    public void nonExistentScopeWithoutScopeAndStreamCreationFails() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> noScopeNoStreamWriterInstance.get());
        assertTrue("Exception is not a gRPC Status exception", ex.getCause() instanceof StatusRuntimeException);
        assertTrue("Exception message doesn't start with 'NOT_FOUND'", ex.getCause().getMessage().startsWith("NOT_FOUND"));
    }

    /*
     * TEST ERROR WHEN SCOPE IS NOT CREATED BUT STREAM IS CREATED
     */

    @Inject
    @PravegaConfig(scope = "not-created",
                   createScope = false,
                   stream = "not-created",
                   createStream = true,
                   serializer = ByteArraySerializer.class)
    Instance<EventStreamWriter<byte[]>> noScopeWithStreamWriterInstance;

    @Test
    public void nonExistentScopeWithoutScopeButWithStreamCreationFailsDifferently() {
        RuntimeException ex = assertThrows(IllegalArgumentException.class, () -> noScopeWithStreamWriterInstance.get());
        assertTrue("Exception message doesn't start with 'Scope does not exist'", ex.getMessage().startsWith("Scope does not exist"));
    }

    @Test
    public void canUseAnnotationLiteralsToFetchClients() throws InterruptedException, ExecutionException {
        @SuppressWarnings("rawtypes")
        Instance<EventStreamWriter> writerInst = CDI.current().select(EventStreamWriter.class, PravegaConfigQualifier.builder()
                .scope("streams")
                .stream("test-select")
                .serializer(UTF8StringSerializer.class)
                .build());

        final String expectHelloWorld = "Hello, world!";
        @SuppressWarnings("unchecked")
        EventStreamWriter<String> writer = writerInst.get();
        writer.writeEvent(expectHelloWorld).get();

        @SuppressWarnings("rawtypes")
        Instance<EventStreamReader> readerInst = CDI.current().select(EventStreamReader.class, PravegaConfigQualifier.builder()
                .scope("streams")
                .stream("test-select")
                .serializer(UTF8StringSerializer.class)
                .build());
        @SuppressWarnings("unchecked")
        EventStreamReader<String> reader = readerInst.get();

        String string = reader.readNextEvent(1000).getEvent();
        assertEquals(expectHelloWorld, string, "Message is not '" + expectHelloWorld + "'");
    }

}
