# Con-Kafka-API

**Con-Kafka-API** is a simple http application that reads messages from a Kafka topic and processes them. The application is built using [Kafka](https://kafka.apache.org/) and [Scala](https://www.scala-lang.org/), utilizing Kafka consumer APIs.

## Table of Contents
- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running app](#running-app)
- [Running Tests](#running-tests)

## Getting Started

To get started with **Con-Kafka-API**, you need to have a running Kafka cluster. The application will connect to Kafka, consume messages from a topic, and process them.

## Prerequisites

- [Docker](https://docs.docker.com/get-started/get-docker/)

### Optional
- [Docker](https://www.docker.com/) for running Kafka locally.
- [Kafka CLI](https://kafka.apache.org/documentation/#quickstart) to manually interact with Kafka.

## Installation

```bash
sbt clean assembly
```

## Running-App

 ```bash
   sbt run
 ```
