package io.pravega.cdi;

import javax.enterprise.util.AnnotationLiteral;

import io.pravega.client.stream.Serializer;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class PravegaConfigQualifier extends AnnotationLiteral<PravegaConfig> implements PravegaConfig {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    String controllerURI = "tcp://localhost:9090";

    @NonNull
    String scope;

    @Builder.Default
    boolean createScope = true;

    @NonNull
    String stream;

    @Builder.Default
    boolean createStream = true;

    @NonNull
    Class<? extends Serializer<?>> serializer;

    boolean automaticallyNoteTime;

    @Override
    public String controllerURI() {
        return controllerURI;
    }
    @Override
    public String scope() {
        return scope;
    }
    @Override
    public boolean createScope() {
        return createScope;
    }
    @Override
    public String stream() {
        return stream;
    }
    @Override
    public boolean createStream() {
        return createStream;
    }
    @Override
    public Class<? extends Serializer<?>> serializer() {
        return serializer;
    }
    @Override
    public boolean automaticallyNoteTime() {
        return automaticallyNoteTime;
    }
    @Override
    public int initialBackoffMillis() {
        return 1;
    }
    @Override
    public int maxBackoffMillis() {
        return 20000;
    }
    @Override
    public int retryAttempts() {
        return 10;
    }
    @Override
    public int backoffMultiple() {
        return 10;
    }
    @Override
    public boolean enableConnectionPooling() {
        return false;
    }
    @Override
    public long transactionTimeoutTime() {
        return 90 * 1000 - 1;
    }

}
