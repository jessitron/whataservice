# Whataservice

It'll do something, eventually, if I can get it working

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/jessitron/whataservice)

## just run it

mvn spring-boot:run

## run with tracing

see [docs](https://opentelemetry.io/docs/instrumentation/java/getting-started/)

which say... Download [opentelemetry-javaagent.jar](https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar) from Releases of the opentelemetry-java-instrumentation repo. The JAR file contains the agent and all automatic instrumentation packages.

`mvn package`

`export JAVA_TOOL_OPTIONS=-javaagent:./opentelemetry-javaagent.jar`

`java -jar target/whataservice-0.0.1-SNAPSHOT.jar`

this will try to send to a local collector.
