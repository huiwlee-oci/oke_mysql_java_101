FROM mcr.microsoft.com/openjdk/jdk:11-ubuntu

RUN useradd -M -r -U swd \
    && mkdir /app \
    && chown swd:swd /app
USER swd

COPY --chown=swd:swd target/ai-demo-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENTRYPOINT ["java","-jar","app.jar"]
