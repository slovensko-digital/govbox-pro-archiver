FROM bellsoft/liberica-runtime-container:jdk-all-17.0.7-glibc as build

COPY mvnw mvnw
COPY .mvn .mvn

COPY pom.xml pom.xml

COPY src src

RUN ./mvnw package

FROM bellsoft/liberica-runtime-container:jre-17.0.7-glibc as prod

COPY --from=build target/archiver-1.0.0-jar-with-dependencies.jar archiver-1.0.0.jar

CMD ["java", "-jar", "archiver-1.0.0.jar"]
