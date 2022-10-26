FROM eclipse-temurin:19-jdk AS builder

WORKDIR /usr/src/app

COPY ./ ./
RUN chmod u+x mvnw
RUN ./mvnw verify --fail-never
RUN ./mvnw install

#-----

FROM eclipse-temurin:19-jre

WORKDIR /usr/src/app/

COPY --from=builder /usr/src/app .

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "target/whataservice-0.0.1-SNAPSHOT.jar" ]
