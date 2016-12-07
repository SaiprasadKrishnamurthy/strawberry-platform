#!/bin/bash
KAFKA_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' kafka)
ZK_IP=$(docker inspect --format '{{ .NetworkSettings.IPAddress }}' zookeeper)
docker run --rm ches/kafka kafka-topics.sh --create --topic $1 --replication-factor 1 --partitions 1 --zookeeper $ZK_IP:2181