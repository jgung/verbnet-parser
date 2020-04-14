FROM maven:3.6.3-jdk-8 AS MAVEN_BUILD

MAINTAINER James Gung

COPY pom.xml /build/
COPY semparse-tf4j /build/semparse-tf4j
COPY semparse-core /build/semparse-core
COPY semparse-web /build/semparse-web
COPY semparse/ /build/semparse-web/src/main/resources/

WORKDIR /build/
RUN mvn package -DskipTests

FROM openjdk:8

WORKDIR /app
COPY --from=MAVEN_BUILD /build/semparse-web/target/semparse-web-0.1-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "-XX:+UseConcMarkSweepGC", "-Xmx3g", "semparse-web-0.1-SNAPSHOT.jar"]
