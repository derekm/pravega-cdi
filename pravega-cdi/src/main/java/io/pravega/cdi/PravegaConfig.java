package io.pravega.cdi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

//import io.pravega.client.ClientConfig;
//import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.Serializer;

@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD, PARAMETER })
public @interface PravegaConfig {

    // ClientConfig attributes in constructor order
//    @Nonbinding String controllerURI() default ClientConfig.Defaults.CONTROLLER_URI;
    @Nonbinding String controllerURI() default "tcp://localhost:9090";

    // StreamConfiguration attributes

    // StreamManager & ClientFactory attributes
    @Nonbinding String scope();
    @Nonbinding boolean createScope() default true;
    @Nonbinding String stream();
    @Nonbinding boolean createStream() default true;
    @Nonbinding Class<? extends Serializer<?>> serializer();

    // EventWriterConfig attributes in constructor order
//    @Nonbinding int initialBackoffMillis() default EventWriterConfig.Defaults.INITIAL_BACKOFF_MILLIS;
//    @Nonbinding int maxBackoffMillis() default EventWriterConfig.Defaults.MAX_BACKOFF_MILLIS;
//    @Nonbinding int retryAttempts() default EventWriterConfig.Defaults.RETRY_ATTEMPTS;
//    @Nonbinding int backoffMultiple() default EventWriterConfig.Defaults.BACKOFF_MULTIPLE;
//    @Nonbinding boolean enableConnectionPooling() default EventWriterConfig.Defaults.ENABLE_CONNECTION_POOLING;
//    @Nonbinding long transactionTimeoutTime() default EventWriterConfig.Defaults.TRANSACTION_TIMEOUT_TIME;
//    @Nonbinding boolean automaticallyNoteTime() default EventWriterConfig.Defaults.AUTOMATICALLY_NOTE_TIME;

    // EventWriterConfig attributes in constructor order
    @Nonbinding int initialBackoffMillis() default 1;
    @Nonbinding int maxBackoffMillis() default 20000;
    @Nonbinding int retryAttempts() default 10;
    @Nonbinding int backoffMultiple() default 10;
    @Nonbinding boolean enableConnectionPooling() default false;
    @Nonbinding long transactionTimeoutTime() default 90 * 1000 - 1;
    @Nonbinding boolean automaticallyNoteTime() default false;

}
