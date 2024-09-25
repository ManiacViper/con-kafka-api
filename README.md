# Con-Kafka-API

It's a simple http application that reads messages from a Kafka topic and processes them. 
The application is built using [Kafka](https://kafka.apache.org/) and [Scala](https://www.scala-lang.org/) and [Http4s](https://http4s.org/), utilizing Java Kafka APIs.

## Table of Contents
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Assembly](#installation)
- [Running app](#running-app)
- [Running Tests](#running-tests)

## Getting Started

To get started with **Con-Kafka-API**, you need to have a running Kafka cluster. 
The application will connect to Kafka, consume messages from a topic, and process them.

## Prerequisites

- [Docker](https://docs.docker.com/get-started/get-docker/)

### Optional
- [Docker](https://www.docker.com/) for running Kafka locally.
- [Kafka CLI](https://kafka.apache.org/documentation/#quickstart) to manually interact with Kafka.

## Assembly

```bash
sbt clean assembly
```

## Running-App

Below should run the app. This should run the kafka server via docker container and then create a topic inside the container also, 
lastly it will run the app and load messages into the topic and start the http4s server.
 ```bash
   ./start.sh
 ```
## Running-Test

 ```bash
   sbt test
 ```
