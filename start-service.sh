#!/bin/bash

echo "=== Instructions Capture Service Startup ==="
echo

# Check if Docker is available
if command -v docker &> /dev/null; then
    echo "✅ Docker is available"
    
    # Check if Kafka container is running
    if docker ps | grep -q kafka; then
        echo "✅ Kafka container is already running"
    else
        echo "🚀 Starting Kafka with Docker Compose..."
        docker-compose up -d
        echo "⏳ Waiting for Kafka to be ready..."
        sleep 10
    fi
    
    # Optional: Show Kafka UI URL
    if docker ps | grep -q kafka-ui; then
        echo "🌐 Kafka UI available at: http://localhost:8090"
    fi
else
    echo "⚠️  Docker not found. You can still run the service, but Kafka features won't work."
    echo "   Only file upload endpoints will be functional."
fi

echo
echo "🚀 Starting Instructions Capture Service..."
echo "📊 Swagger UI will be available at: http://localhost:8080/swagger-ui.html"
echo "📈 Service endpoints at: http://localhost:8080/api/trades/"
echo

# Build and run the Spring Boot application
mvn clean install -q
mvn spring-boot:run