# Pravega CDI [![Java CI with Maven](https://github.com/derekm/pravega-cdi/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/derekm/pravega-cdi/actions?query=workflow%3A%22Java+CI+with+Maven%22) [![codecov](https://codecov.io/gh/derekm/pravega-cdi/branch/master/graph/badge.svg)](https://codecov.io/gh/derekm/pravega-cdi) [![Maintainability](https://api.codeclimate.com/v1/badges/e4d1c8dfd6e3d717d429/maintainability)](https://codeclimate.com/github/derekm/pravega-cdi/maintainability) [![Test Coverage](https://api.codeclimate.com/v1/badges/e4d1c8dfd6e3d717d429/test_coverage)](https://codeclimate.com/github/derekm/pravega-cdi/test_coverage) [![Coverage Status](https://coveralls.io/repos/github/derekm/pravega-cdi/badge.svg?branch=master)](https://coveralls.io/github/derekm/pravega-cdi?branch=master)

CDI-enabled client libraries for Pravega

```java
@Inject
@PravegaConfig(scope = "streams",
               stream = "test",
               serializer = ByteArraySerializer.class)
EventStreamWriter<byte[]> eventStreamWriter;
```
