#!/bin/bash

# Continuous event streaming script
# Sends events to Kafka producer in real-time at a configurable rate
# Press Ctrl+C to stop

PRODUCER_URL="http://localhost:8080/orders"
INTERVAL=${1:-2}  # Default to 2 seconds between events

echo "======================================"
echo "Starting continuous event stream"
echo "Sending 1 event every $INTERVAL seconds"
echo "Press Ctrl+C to stop"
echo "======================================"
echo ""

COUNT=0

while true; do
  COUNT=$((COUNT + 1))
  CUSTOMER_ID="cust-$(printf "%05d" $RANDOM)"
  AMOUNT=$((RANDOM % 200 + 10))
  STATUS=("CREATED" "UPDATED" "CANCELLED" "SHIPPED" "DELIVERED")
  RANDOM_STATUS=${STATUS[$RANDOM % ${#STATUS[@]}]}

  TIMESTAMP=$(date +"%H:%M:%S")

  echo "[$COUNT] [$TIMESTAMP] 📦 Sending: $CUSTOMER_ID - \$$AMOUNT - $RANDOM_STATUS"

  curl -s -X POST $PRODUCER_URL \
    -H "Content-Type: application/json" \
    -d "{\"customerId\": \"$CUSTOMER_ID\", \"status\": \"$RANDOM_STATUS\", \"amount\": $AMOUNT}" \
    > /dev/null

  sleep $INTERVAL
done
