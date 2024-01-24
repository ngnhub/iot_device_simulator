FROM openjdk:21-jdk-slim

WORKDIR /app

COPY ./build/libs/IoT_device_simulator-*.jar /app/IoT_device_simulator.jar

CMD ["java", "-jar", "IoT_device_simulator.jar"]
