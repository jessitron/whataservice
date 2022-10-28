FROM eclipse-temurin:19-jdk AS builder

WORKDIR /usr/src/app

# get the dependencies only
COPY mvnw ./
COPY .mvn/ .mvn/
RUN chmod u+x mvnw

COPY pom.xml ./
RUN ./mvnw dependency:go-offline

# now bring in the source and build it
COPY ./src ./src
# i wish I could use -o, but somehow not all the deps have been downloaded. Still, it's fewer
RUN ./mvnw package

#-----

FROM eclipse-temurin:19-jre

WORKDIR /usr/src/app/

ARG version=1.16.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v$version/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
ENV JAVA_TOOL_OPTIONS=-javaagent:/app/opentelemetry-javaagent.jar

COPY --from=builder /usr/src/app .

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "target/whataservice-0.0.1-SNAPSHOT.jar" ]
