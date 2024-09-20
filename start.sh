docker run -p 9092:9092 apache/kafka:3.7.0
docker exec kafka kafka-topics.sh --create --topic people --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --config cleanup.policy=delete
docker exec kafka kafka-topics.sh --list --bootstrap-server localhost:9092
sbt run