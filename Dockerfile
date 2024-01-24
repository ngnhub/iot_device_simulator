FROM openjdk:21-jdk-slim

WORKDIR /app

COPY ./build/libs/IoT_device_simulator-*.jar /app/IoT_device_simulator.jar

VOLUME /app/sensors/

COPY src/main/resources/default_sensors.json /app/sensors/sensors.json

CMD ["java", "-Dspring.profiles.active=docker", "-jar", "IoT_device_simulator.jar"]
