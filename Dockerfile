FROM openjdk:8-jdk-alpine

ADD . /src

RUN apk update && apk add bash \
		&& mkdir -p /src /app \
		&& cd /src \
		&& /bin/bash gradlew --no-daemon build \
		&& cp build/libs/mts-task-app.jar /app \
		&& rm -rf /src


ENTRYPOINT ["java", "-jar", "/app/mts-task-app.jar"]