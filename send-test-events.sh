#!/bin/bash

# Test script to send sample events to the Kafka producer
# Usage: ./send-test-events.sh [count]
# Example: ./send-test-events.sh 10  (sends 10 events)

PRODUCER_URL="http://localhost:8080/orders"
COUNT=${1:-5}  # Default to 5 events if not specified

echo "======================================"
echo "Sending $COUNT test events to Kafka"
echo "======================================"
echo ""

for i in $(seq 1 $COUNT); do
  CUSTOMER_ID="cust-$(printf "%03d" $i)"
  AMOUNT=$((RANDOM % 100 + 10))
  STATUS=("CREATED" "UPDATED" "SHIPPED" "DELIVERED")
  RANDOM_STATUS=${STATUS[$RANDOM % ${#STATUS[@]}]}

  echo "[$i/$COUNT] Sending order for $CUSTOMER_ID - \$$AMOUNT - $RANDOM_STATUS"

  curl -s -X POST $PRODUCER_URL \
    -H "Content-Type: application/json" \
    -d "{\"customerId\": \"$CUSTOMER_ID\", \"status\": \"$RANDOM_STATUS\", \"amount\": $AMOUNT}" \
    | jq -r '.'

  echo ""
  sleep 0.5  # Small delay between requests
done

echo ""
echo "======================================"
echo "✅ Sent $COUNT events successfully!"
echo "======================================"
echo ""
echo "Check the consumer logs to see them being processed."
echo "Or visit Kafka UI at http://localhost:8090 to view messages."
