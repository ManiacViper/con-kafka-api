#!/bin/bash
# Stop and remove the existing container if it exists
if [ $(docker ps -aq -f name=kafka-people) ]; then
    docker stop kafka-people
    docker rm kafka-people
fi
if [[ -f kafka.log ]]; then
    rm kafka.log
fi
docker run --name kafka-people -p 9092:9092 apache/kafka:3.7.0 > kafka.log 2>&1 &
sleep 5
docker exec -it kafka-people /opt/kafka/bin/kafka-topics.sh --create --topic people --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --config cleanup.policy=delete
docker exec -it kafka-people /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
sleep 5
sbt run
