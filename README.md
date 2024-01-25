# IoT Device Simulator

1. [Overview](#overview)
2. [Stack](#stack)
3. [Getting started](#getting-started)
4. [Sensors configuration](#sensor-description)
5. [MQTT](#mqtt)
6. [RSocket](#rsocket)
7. [License](#license)

## Overview

The IoT Device Simulator is a Java application built with Spring Boot, Project Reactor, RSocket, and Mosquitto. It
allows you to simulate IoT devices by receiving data over MQTT channels and sending data through MQTT and RSocket.
By default, it sends a predefined
sensor [data](https://github.com/ngnhub/iot_device_simulator/blob/main/src/main/resources/default_sensors.json).
(Sensor configuration is described in this [section](#configuration)).

## Stack

- Java 21
- Spring Boot
- Project Reactor
- RSocket
- Mosquitto

## Getting Started

### With a Mosquitto broker on a host-machine

The simplest way to start work with simulator is in case **mosquitto is running locally on port 1883 with the anonymous
access**: <p>

1. Pull the Docker image

```shell
docker run -p 7070:7070 --name device-simulator ngnhub/device-simulator
```

2. Run the Container

```shell
docker run -p 7070:7070 --name device-simulator ngnhub/device-simulator
```

3. Once the container is up and running, it will start generating and sending random values to the MQTT broker. To
   verify that the application is generating values,
   you can subscribe to one of the emulated topics using the following command:

```shell
mosquitto_sub -h localhost -p 1883 -t voltage
```

4. Additionally, the app provides RSocket communication. To start consuming values, you can also subscribe to it via the
   RSocket URL:
   `subscribe/voltage` or `subscribe/value/voltage`, on `port 7070`.<p>

In case the broker requires an authentication, you can pass credentials via the environment variables:

```shell
docker run -p 7070:7070 --name device-simulator --env MQTT_USERNAME=username --env MQTT_PASSWORD=password ngnhub/device-simulator
```

### Without a Mosquitto broker on a host-machine

In this case the eclipse [image](https://hub.docker.com/_/eclipse-mosquitto) can be utilized. The docker-compose
example:<p>

```yaml
version: '3.8'
services:
  mqtt:
    image: eclipse-mosquitto:latest
    container_name: mosquitto
    ports:
      - '1883:1883'
    volumes:
      - /path/to/mosquitto.conf:/mosquitto/config/mosquitto.conf

  simulator:
    image: ngnhub/device-simulator
    container_name: device-simulator
    ports:
      - '7070:7070'
```

[Mosquitto.conf](https://gist.github.com/ngnhub/3e6aeebd28226ec060a0815cf6844236).
This config file can be easily mount to the eclipse-mosquitto container.
This config file contains the setting `listener 1883 0.0.0.0.` This allows Mosquitto to communicate with any IP address,
providing the simplest way to set up a Mosquitto container and connect other containers to it. However, this is just an
example,
and such a configuration should not be used on a production broker.

### Environment variables

The simulator provides a set of properties that can be used to configure a container environment:<p>

| Variable                   | Description                                                                                                                                                                                                                                                                                                                                              | Default                |
|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------|
| RSOCKET_PORT               | Defines the RSocket port                                                                                                                                                                                                                                                                                                                                 | `7070`                 |
| MQTT_ENABLED               | If only RSocket communication is required, MQTT can be disabled.                                                                                                                                                                                                                                                                                         | `true`                 |
| MQTT_HOST                  | Mqtt host                                                                                                                                                                                                                                                                                                                                                | `host.docker.internal` |
| MQTT_PORT                  | Mqtt port                                                                                                                                                                                                                                                                                                                                                | `1883`                 |
| MQTT_USERNAME              | Broker username                                                                                                                                                                                                                                                                                                                                          |                        |
| MQTT_PASSWORD              | Broker password                                                                                                                                                                                                                                                                                                                                          |                        |
| MQTT_QOS                   | QoS. See: https://www.hivemq.com/blog/mqtt-essentials-part-6-mqtt-quality-of-service-levels/                                                                                                                                                                                                                                                             | `2`                    |
| MQTT_RECONNECTION_DELAY    | Defines the delay period in milliseconds after failover when the application attempts to reconnect to the broker                                                                                                                                                                                                                                         | `128000`               |
| MQTT_TOPIC_BASE_PATH       | In some cases, it is necessary to define a base context path for all topics, such as `/base/path/{topic}`. <br/> All the mqtt topics will be concatenated with this base path, and values will be sent to the modified topic                                                                                                                             |                        |
| MQTT_UNIQUE_TOPICS_ENABLED | If there are multiple consumers, and atomicity is required, this property can be enabled. <br/>With this property enabled, each topic will have a UUID prefix, such as `73cd502c-adf3-4803-a5b4-a7457b73cc04/{topic}`.<br/> If a **base path** is defined, it will be concatenated as follows: `{basePath}/73cd502c-adf3-4803-a5b4-a7457b73cc04/{topic}` | `false`                |
| LOG_LEVEL                  | Enabling the debug level may provide a better understanding what's happening inside the simulator                                                                                                                                                                                                                                                        | `info`                 |

The docker compose example:

```yaml
  simulator:
    image: ngnhub/device-simulator
    container_name: device-simulator
    environment:
      - MQTT_USERNAME=admin
      - MQTT_PASSWORD=admin
      - MQTT_TOPIC_BASE_PATH=/base/path/
      - LOG_LEVEL=debug
    ports:
      - '7070:7070'
```

## Sensors configuration

The IoT Device Simulator allows you to configure and simulate various IoT devices. By default, it sends a predefined
sensor [data](https://github.com/ngnhub/iot_device_simulator/blob/main/src/main/resources/default_sensors.json).
The method for describing a custom schema is outlined below. Firstly let's examine a description schema.

### Sensor description

In this section the sensor description schema is described.<p> Any sensor that should be emulated must be described as
the json object.
There are 3 types of sensor description:

1. Sensor generating random values periodically (RVP)
2. Sensor generating strict values periodically (SVP)
3. Switchable value sensor (SV)

#### Sensor generating random values periodically (RVP)

```json
  {
  "topic": "voltage",
  "type": "DOUBLE",
  "min": 0,
  "max": 5,
  "intervalInMillis": 100
}
```

This sensor will generate a random value from 0 to 5 each 100 millis.
If there are any subscriber (via MQTT or RSocket) they will receive these values each 100 millis.

* **topic** (required) - simply a topic name
* **type** (required) - the type of the generated value. Only 2 types are supported : **DOUBLE and STRING**.
  This information is mostly needed for the internal processes and for the set of validation rules.
* **min/max** - specifies the high and low bounds for generating values and is only applicable to the **DOUBLE** type
* **intervalInMillis** (required) - specifies the value generation time period.<p>

#### Sensor generating strict values periodically (SVP)

```json
  {
  "topic": "smart_light",
  "type": "STRING",
  "possibleValues": ["morning", "day", "night"],
  "intervalInMillis": 10000
}
```

This sensor will generate on of the possible values each 10_000 millis.
A specific field here is:

* **possibleValues** (required) - set of values that will be chosen randomly within the interval.

Defining min/max will not result in an error; however, these fields will have no effect. They are only applicable to RVP
sensors.

**possibleValues** has a higher priority than **min/max** values.

#### Switchable value sensor (SV)

```json
  {
  "topic": "fan",
  "type": "STRING",
  "possibleValues": ["on", "of"],
  "switcher": "fan/state"
}
```

This sensor will transmit a value received from the **switcher** to the specified **topic**.
Essentially, it consumes values from the topic defined in the **switcher** field and then forwards them to the **topic**
specified in the topic field.

A specific field here is:

* **switcher** (required if the interval is not present) - a topic-switcher.

#### Additional fields

There are some additional fields that can be used in any type:

```json
{
  "qos": 1,
  "unitOfMeasure": "V"
}
```

* **qos** (int) - overrides qos level defined on the application level
* **unitOfMeasure** (string) - a specific prefix that will be attached to the sending value like "5V".

#### Sensor description validation rules:

1. Fields **topic** and **type** are required for any type
2. The **type** field has only 2 possible values at the moment : **DOUBLE** and **STRING**
3. **min/max** fields are only applicable for the **DOUBLE** type
4. **possibleValue** items type have to be matched to the **type** field
5. Either **possibleValues** or **min/max** values have to be defined if the **switcher** field is empty

If any of the rules is violated, the application run will fail.

The rules are quite strict, and while most of them could be skipped, catching them during the application initialization
step helps avoid application's unpredictable behavior.

### Custom sensor description

The custom JSON description can be easily attached to the application using the specific volume `/app/sensors/`.
This can be accomplished as follows

```shell
docker run -p 7070:7070 -v /path/to/sensors.json:/app/sensors/sensors.json ngnhub/device-simulator
```

docker-compose:

```yaml
  simulator:
    image: ngnhub/device-simulator
    container_name: device-simulator
    ports:
      - '7070:7070'
    volumes:
      - /path/to/sensors.json:/app/sensors/sensors.json
```

The example of the sensor description json file can be
fond [here](https://github.com/ngnhub/iot_device_simulator/blob/main/src/main/resources/default_sensors.json).

## MQTT

Despite the fact that sensor descriptions require defining the type of value, data is sent to the MQTT channel in string
format,
and it's perfectly acceptable to include a unit of measurement, such as `15C`, which could mean 15 degrees Celsius.
Additionally, an error message could be sent to the topic, informing the user about what went wrong, for example,
`Error occurred during the data processing: {'3.0' is not a possible value for this topic}`
Type restrictions are primarily needed for the correct functioning of the application.

## RSocket

For the purposes of the experiment, a solution for subscribing to devices via RSocket was implemented. There are two
endpoints:

* `subscribe/{topic}`
* `subscribe/value/{topic}`

The first endpoint receives data in the form of JSON, where all fields are quite self-explanatory.

```json
{
  "topic": "gpio",
  "values": "1.2",
  "time": "2024-01-25T19:06:24.111201",
  "errored": false
}
```

The **error** field indicates that something went wrong while generating the value.
In this instance, the value field will contain an error message.
The most common scenario occurs when an SV sensor is expected to consume a DOUBLE value, but a non-parsable value is
sent.
Under such circumstances, the following error message will be received:
`Cannot convert the consumed value. Should have type: Double`.

RSocket listening 7070 port.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
