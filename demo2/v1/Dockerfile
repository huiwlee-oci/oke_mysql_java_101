FROM eclipse-temurin:11-jdk
#RUN useradd -M -r -U swd
#USER swd
#COPY --chown=swd:swd target/basic-0.0.1-SNAPSHOT.jar /app/app.jar
COPY target/basic-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]
