FROM openjdk:8

ADD . /app
WORKDIR /app

ENV JAVA_OPTS -Xmx1024m
ENV GRADLE_OPTS -Xmx1024m

RUN ./gradlew assemble

CMD ./gradlew check

