package io.pravega.cdi;

import java.net.URI;
import java.util.UUID;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.EventWriterConfig.EventWriterConfigBuilder;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.Stream;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.ByteArraySerializer;
import lombok.Cleanup;

public class EventStreamProducers {

    @SuppressWarnings("unchecked")
    @Produces
    @PravegaConfig(scope = "", /* parameter is non-binding, this dummy value is ignored */
                   stream = "", /* parameter is non-binding, this dummy value is ignored */
                   serializer = ByteArraySerializer.class /* parameter is non-binding, this dummy value is ignored */)
    public <T> EventStreamWriter<T> getNewEventStreamWriter(InjectionPoint ip) throws InstantiationException, IllegalAccessException {

        PravegaConfig pravegaConfig = null;
        if (ip.getAnnotated() != null) {
            pravegaConfig = ip.getAnnotated().getAnnotation(PravegaConfig.class);
        } else {
            pravegaConfig = (PravegaConfig) ip.getQualifiers().parallelStream().filter(q -> q instanceof PravegaConfig).findAny().get();
        }
        URI controllerURI = URI.create(pravegaConfig.controllerURI());

        final StreamConfiguration streamConfig = createStreamConfiguration(pravegaConfig);

        createScopeAndStream(pravegaConfig, streamConfig, controllerURI);

        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        // controllerURI, credentials, trustStore, validateHostName, maxConnectionsPerSegmentStore,
        // deriveTlsEnabledFromControllerURI, enableTlsToController, enableTlsToSegmentStore, metricListener

        EventWriterConfigBuilder writerConfigBuilder = EventWriterConfig.builder();
        if (pravegaConfig != null) {
//            writerConfigBuilder.initialBackoffMillis(pravegaConfig.initialBackoffMillis());
            writerConfigBuilder.initalBackoffMillis(pravegaConfig.initialBackoffMillis());
            writerConfigBuilder.maxBackoffMillis(pravegaConfig.maxBackoffMillis());
            writerConfigBuilder.retryAttempts(pravegaConfig.retryAttempts());
            writerConfigBuilder.backoffMultiple(pravegaConfig.backoffMultiple());
            writerConfigBuilder.enableConnectionPooling(pravegaConfig.enableConnectionPooling());
            writerConfigBuilder.transactionTimeoutTime(pravegaConfig.transactionTimeoutTime());
            writerConfigBuilder.automaticallyNoteTime(pravegaConfig.automaticallyNoteTime());            
        }
        EventWriterConfig writerConfig = writerConfigBuilder.build();

        return (EventStreamWriter<T>) EventStreamClientFactory.withScope(pravegaConfig.scope(), clientConfig)
                .createEventWriter(pravegaConfig.stream(), pravegaConfig.serializer().newInstance(), writerConfig);
    }

    public void closeEventStreamWriter(
            @Disposes
            @PravegaConfig(scope = "", /* parameter is non-binding, this dummy value is ignored */
                           stream = "", /* parameter is non-binding, this dummy value is ignored */
                           serializer = ByteArraySerializer.class /* parameter is non-binding, this dummy value is ignored */)
            EventStreamWriter<?> eventStreamWriter
    ) {
        eventStreamWriter.close();
    }

    @SuppressWarnings("unchecked")
    @Produces
    @PravegaConfig(scope = "", /* parameter is non-binding, this dummy value is ignored */
                   stream = "", /* parameter is non-binding, this dummy value is ignored */
                   serializer = ByteArraySerializer.class /* parameter is non-binding, this dummy value is ignored */)
    public <T> EventStreamReader<T> getNewEventStreamReader(InjectionPoint ip) throws InstantiationException, IllegalAccessException {

        PravegaConfig pravegaConfig = null;
        if (ip.getAnnotated() != null) {
            pravegaConfig = ip.getAnnotated().getAnnotation(PravegaConfig.class);
        } else {
            pravegaConfig = (PravegaConfig) ip.getQualifiers().parallelStream().filter(q -> q instanceof PravegaConfig).findAny().get();
        }
        URI controllerURI = URI.create(pravegaConfig.controllerURI());

        final StreamConfiguration streamConfig = createStreamConfiguration(pravegaConfig);

        createScopeAndStream(pravegaConfig, streamConfig, controllerURI);

        ClientConfig clientConfig = ClientConfig.builder().controllerURI(controllerURI).build();
        // controllerURI, credentials, trustStore, validateHostName, maxConnectionsPerSegmentStore,
        // deriveTlsEnabledFromControllerURI, enableTlsToController, enableTlsToSegmentStore, metricListener

        String readerGroup = UUID.randomUUID().toString().replace("-", "");
        ReaderGroupConfig readerGroupConfig = ReaderGroupConfig.builder()
                .stream(Stream.of(pravegaConfig.scope(), pravegaConfig.stream()))
                .disableAutomaticCheckpoints()
                .build();
        @Cleanup
        ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(pravegaConfig.scope(), clientConfig);
        readerGroupManager.createReaderGroup(readerGroup, readerGroupConfig);

        return (EventStreamReader<T>) EventStreamClientFactory.withScope(pravegaConfig.scope(), clientConfig)
                .createReader("fakeId", readerGroup, pravegaConfig.serializer().newInstance(), ReaderConfig.builder().initialAllocationDelay(0).build());
    }

    public void closeEventStreamReader(
            @Disposes
            @PravegaConfig(scope = "", /* parameter is non-binding, this dummy value is ignored */
                           stream = "", /* parameter is non-binding, this dummy value is ignored */
                           serializer = ByteArraySerializer.class /* parameter is non-binding, this dummy value is ignored */)
            EventStreamReader<?> eventStreamReader
    ) {
        eventStreamReader.close();
    }

    private StreamConfiguration createStreamConfiguration(PravegaConfig pravegaConfig) {
        // TODO FIXME support StreamConfiguration policies
        return StreamConfiguration.builder()
                .scalingPolicy(ScalingPolicy.fixed(1))
                .build();
    }

    private void createScopeAndStream(PravegaConfig pravegaConfig, StreamConfiguration streamConfig, URI controllerURI) {
        if (pravegaConfig.createScope() || pravegaConfig.createStream()) {
            try (final StreamManager streamManager = StreamManager.create(controllerURI)) {
                createScope(pravegaConfig, streamManager);
                createStream(pravegaConfig, streamManager, streamConfig);
            }
        }
    }

    private void createScope(PravegaConfig pravegaConfig, StreamManager streamManager) {
        if (pravegaConfig.createScope()) {
            streamManager.createScope(pravegaConfig.scope());
        }
    }

    private void createStream(PravegaConfig pravegaConfig, StreamManager streamManager, StreamConfiguration streamConfig) {
        if (pravegaConfig.createStream()) {
            streamManager.createStream(pravegaConfig.scope(), pravegaConfig.stream(), streamConfig);
        }
    }

}
