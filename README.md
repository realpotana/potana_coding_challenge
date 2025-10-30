# Instructions Capture Service

A Spring Boot microservice that processes trade instructions from multiple sources (file upload and Kafka), transforms them into a canonical format, and publishes the results to Kafka.

## Features

- **Multiple Input Sources**: File upload (CSV/JSON) and Kafka message consumption
- **Data Transformation**: Normalizes and masks sensitive fields
- **Platform Integration**: Converts to platform-specific JSON format
- **Kafka Publishing**: Asynchronous publishing to outbound topic
- **In-Memory Storage**: Temporary storage for auditing and retry logic
- **Security**: Input validation, sanitization, and sensitive data masking
- **Performance**: Stream-based processing and asynchronous operations

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Apache Kafka (running on localhost:9092)

### Setup Kafka Topics

```bash
# Create required topics
kafka-topics.sh --create --topic instructions.inbound --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics.sh --create --topic instructions.outbound --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### Run the Application

```bash
# Build and run
mvn clean install
mvn spring-boot:run

# Or run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

## API Documentation

Once running, access the Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Key Endpoints

- `POST /api/trades/upload` - Upload CSV or JSON file
- `POST /api/trades/single` - Process single trade via JSON
- `GET /api/trades/canonical/{tradeId}` - Retrieve stored canonical trade
- `GET /api/trades/stats` - Get processing statistics

## Data Transformation

### Input Format (Canonical)
```json
{
  "account_number": "1234567890",
  "security_id": "ABC123",
  "trade_type": "Buy",
  "amount": 100000,
  "timestamp": "2025-08-04T21:15:33"
}
```

### Output Format (Platform-Specific)
```json
{
  "platform_id": "ACCT123",
  "trade": {
    "account": "****7890",
    "security": "ABC123",
    "type": "B",
    "amount": 100000,
    "timestamp": "2025-08-04T21:15:33Z"
  }
}
```

## Configuration

Key configuration properties in `application.yml`:

```yaml
app:
  kafka:
    topics:
      inbound: instructions.inbound
      outbound: instructions.outbound
  platform:
    id: ACCT123
```

## Testing

### Send Test Message to Kafka

```bash
# Send test message to inbound topic
kafka-console-producer.sh --topic instructions.inbound --bootstrap-server localhost:9092

# Paste this JSON:
{"account_number":"1234567890","security_id":"abc123","trade_type":"Buy","amount":100000,"timestamp":"2025-08-04T21:15:33"}
```

### Monitor Outbound Topic

```bash
kafka-console-consumer.sh --topic instructions.outbound --bootstrap-server localhost:9092 --from-beginning
```

## Sample Files

See the `samples/` directory for example CSV and JSON files.

## Docker

```bash
# Build image
docker build -t instructions-capture-service .

# Run container
docker run -p 8080:8080 -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 instructions-capture-service
```

## Security Features

- Input validation and sanitization
- Account number masking (shows only last 4 digits)
- Secure deserialization
- No sensitive data in logs
- Request size limits

## Performance Features

- Stream-based file processing
- Asynchronous Kafka publishing
- Concurrent in-memory storage
- Efficient CSV/JSON parsing