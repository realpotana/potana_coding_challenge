#!/bin/bash

# Instructions Capture Service - Test Commands

BASE_URL="http://localhost:8080"

echo "=== Instructions Capture Service Test Commands ==="
echo

echo "1. Upload CSV file:"
echo "curl -X POST -F 'file=@samples/sample-trades.csv' $BASE_URL/api/trades/upload"
echo

echo "2. Upload JSON file:"
echo "curl -X POST -F 'file=@samples/sample-trades.json' $BASE_URL/api/trades/upload"
echo

echo "3. Process single trade:"
echo "curl -X POST -H 'Content-Type: application/json' -d '{
  \"account_number\": \"1234567890\",
  \"security_id\": \"ABC123\",
  \"trade_type\": \"Buy\",
  \"amount\": 100000,
  \"timestamp\": \"2025-08-04T21:15:33\"
}' $BASE_URL/api/trades/single"
echo

echo "4. Get statistics:"
echo "curl -X GET $BASE_URL/api/trades/stats"
echo

echo "5. Get canonical trade (replace TRADE_ID):"
echo "curl -X GET $BASE_URL/api/trades/canonical/TRADE_ID"
echo

echo "6. Send message to Kafka inbound topic (if Kafka is running):"
echo "# Using Docker:"
echo "echo '{\"account_number\":\"1234567890\",\"security_id\":\"ABC123\",\"trade_type\":\"Buy\",\"amount\":100000,\"timestamp\":\"2025-08-04T21:15:33\"}' | docker exec -i kafka kafka-console-producer --topic instructions.inbound --bootstrap-server localhost:9092"
echo "# Or if Kafka installed locally:"
echo "echo '{\"account_number\":\"1234567890\",\"security_id\":\"ABC123\",\"trade_type\":\"Buy\",\"amount\":100000,\"timestamp\":\"2025-08-04T21:15:33\"}' | \$KAFKA_HOME/bin/kafka-console-producer.sh --topic instructions.inbound --bootstrap-server localhost:9092"
echo

echo "7. Monitor Kafka outbound topic:"
echo "# Using Docker:"
echo "docker exec -it kafka kafka-console-consumer --topic instructions.outbound --bootstrap-server localhost:9092 --from-beginning"
echo "# Or if Kafka installed locally:"
echo "\$KAFKA_HOME/bin/kafka-console-consumer.sh --topic instructions.outbound --bootstrap-server localhost:9092 --from-beginning"
echo

echo "8. Health check:"
echo "curl -X GET $BASE_URL/actuator/health"
echo