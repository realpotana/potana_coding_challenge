#!/bin/bash

echo "=== Testing Instructions Capture Service (No Kafka Required) ==="
echo

# Check if the service is running
if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Service is running"
else
    echo "❌ Service is not running. Please start it with: mvn spring-boot:run"
    exit 1
fi

echo
echo "🧪 Running tests..."
echo

# Test 1: Upload CSV file
echo "1. Testing CSV file upload..."
response=$(curl -s -X POST -F 'file=@samples/sample-trades.csv' http://localhost:8080/api/trades/upload)
if echo "$response" | grep -q "processedCount"; then
    echo "   ✅ CSV upload successful"
    echo "   📊 Response: $response"
else
    echo "   ❌ CSV upload failed"
    echo "   📊 Response: $response"
fi

echo

# Test 2: Upload JSON file
echo "2. Testing JSON file upload..."
response=$(curl -s -X POST -F 'file=@samples/sample-trades.json' http://localhost:8080/api/trades/upload)
if echo "$response" | grep -q "processedCount"; then
    echo "   ✅ JSON upload successful"
    echo "   📊 Response: $response"
else
    echo "   ❌ JSON upload failed"
    echo "   📊 Response: $response"
fi

echo

# Test 3: Single trade processing
echo "3. Testing single trade processing..."
response=$(curl -s -X POST -H 'Content-Type: application/json' -d '{
  "account_number": "1234567890",
  "security_id": "ABC123",
  "trade_type": "Buy",
  "amount": 100000,
  "timestamp": "2025-08-04T21:15:33"
}' http://localhost:8080/api/trades/single)

if echo "$response" | grep -q "tradeId"; then
    echo "   ✅ Single trade processing successful"
    echo "   📊 Response: $response"
    
    # Extract trade ID for next test
    trade_id=$(echo "$response" | grep -o '"tradeId":"[^"]*"' | cut -d'"' -f4)
    
    if [ ! -z "$trade_id" ]; then
        echo
        echo "4. Testing canonical trade retrieval..."
        canonical_response=$(curl -s http://localhost:8080/api/trades/canonical/$trade_id)
        if echo "$canonical_response" | grep -q "accountNumber"; then
            echo "   ✅ Canonical trade retrieval successful"
            echo "   📊 Response: $canonical_response"
        else
            echo "   ❌ Canonical trade retrieval failed"
        fi
    fi
else
    echo "   ❌ Single trade processing failed"
    echo "   📊 Response: $response"
fi

echo

# Test 4: Statistics
echo "5. Testing statistics endpoint..."
stats_response=$(curl -s http://localhost:8080/api/trades/stats)
if echo "$stats_response" | grep -q "storedTradeCount"; then
    echo "   ✅ Statistics endpoint working"
    echo "   📊 Response: $stats_response"
else
    echo "   ❌ Statistics endpoint failed"
fi

echo
echo "🎉 Testing complete!"
echo "📖 Check Swagger UI at: http://localhost:8080/swagger-ui.html"