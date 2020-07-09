package io.pravega.cdi;

import javax.enterprise.util.AnnotationLiteral;

import io.pravega.client.stream.Serializer;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@SuppressWarnings("serial")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class PravegaConfigQualifier extends AnnotationLiteral<PravegaConfig> implements PravegaConfig {
    @Builder.Default
    String controllerURI = "tcp://localhost:9090";

    String scope;

    @Builder.Default
    boolean createScope = true;

    String stream;

    @Builder.Default
    boolean createStream = true;

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
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public int maxBackoffMillis() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public int retryAttempts() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public int backoffMultiple() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public boolean enableConnectionPooling() {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public long transactionTimeoutTime() {
        // TODO Auto-generated method stub
        return 0;
    }

}
