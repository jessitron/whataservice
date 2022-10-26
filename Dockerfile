FROM eclipse-temurin:19-jre

WORKDIR /usr/src/app

COPY ./ ./
RUN chmod u+x mvnw
RUN ./mvnw install

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "target/whataservice-0.0.1-SNAPSHOT.jar" ]