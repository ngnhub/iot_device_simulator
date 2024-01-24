docker build -t device-simulator .
docker run -p 9090:7070 device-simulator 

# IoT Device Simulator

## Stack
- Java 21
- Spring Boot
- Project Reactor
- RSocket
- Mosquitto

## Overview
The IoT Device Simulator is a Java application built with Spring Boot, Project Reactor, RSocket, and Mosquitto. It allows you to simulate IoT devices by receiving data over MQTT channels and sending data through MQTT and RSocket.

## Getting Started
The simplest way to start work with simulator is in case **mosquitto is running locally on port 1883 with the anonymous access**: <p>
1. Pull the Docker image `docker run -p 7070:7070 --name device-simulator ngnhub/device-simulator`<p>
2. Run the Container `docker run -p 7070:7070 --name device-simulator ngnhub/device-simulator`<p>
3. Once the container is up and running, it will start generating and sending random values to the MQTT broker. To verify that the application is generating values, 
you can subscribe to one of the emulated topics using the following command:
`mosquitto_sub -h localhost -p 1883 -t voltage`.
4. Additionally, the app provides RSocket communication. To start consuming values, you can also subscribe to it via the RSocket URL:
`subscribe/voltage` or `subscribe/value/voltage`, on `port 7070`.

## Configuration
The IoT Device Simulator allows you to configure and simulate various IoT devices. Define your devices in a JSON configuration file for simulation. More details on device configuration will be provided in the documentation.