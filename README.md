# Kafka Setup: Producer & Consumer Demo

A complete Kafka demonstration with Spring Boot producer and consumer applications, featuring custom polling rates and real-time event streaming.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Environment                      │
│                                                              │
│  ┌──────────────┐      ┌──────────────┐                    │
│  │    Kafka     │◄─────┤   Kafka UI   │                    │
│  │   Broker     │      │ (localhost:  │                    │
│  │  (KRaft)     │      │    8090)     │                    │
│  └──────┬───────┘      └──────────────┘                    │
│         │                                                    │
│         │ Port 9092 (internal Docker)                       │
│         │ Port 9093 (host machine)                          │
└─────────┼──────────────────────────────────────────────────┘
          │
          │
    ┌─────┴─────────────────┐
    │                       │
┌───▼──────────┐    ┌───────▼─────┐
│   Producer   │    │   Consumer  │
│ (localhost:  │    │ (localhost: │
│    8080)     │    │    8081)    │
│              │    │             │
│ Sends events │    │ Polls every │
│ via REST API │    │   3 seconds │
└──────────────┘    └─────────────┘
```

## Components

### 1. Kafka Broker (Docker)
- **Image**: `apache/kafka:3.7.0`
- **Mode**: KRaft (no ZooKeeper needed)
- **Ports**:
  - `9092` - Internal Docker network (for Kafka UI)
  - `9093` - Host machine access (for Spring Boot apps)
- **Topics**: Auto-created on first use

### 2. Kafka UI (Docker)
- **URL**: http://localhost:8090
- **Purpose**: Web dashboard to inspect topics, messages, consumer groups, and offsets
- **Features**:
  - Browse messages in topics
  - View consumer group lag
  - Monitor partition distribution
  - Inspect message headers and payloads

### 3. Kafka Producer (Spring Boot)
- **Port**: 8080
- **Purpose**: REST API that produces `OrderEvent` messages to Kafka
- **Endpoint**: `POST /orders`
- **How it works**:
  1. Receives HTTP POST request with order data
  2. Creates an `OrderEvent` object with generated IDs and timestamp
  3. Sends event to `orders` topic using `orderId` as the partition key
  4. Returns immediately (async send)
  5. Logs success/failure via CompletableFuture callback

### 4. Kafka Consumer (Spring Boot)
- **Port**: 8081
- **Purpose**: Continuously polls the `orders` topic and processes events
- **How it works**:
  1. `@KafkaListener` annotation starts a background polling thread
  2. Polls Kafka every **3 seconds** (`poll-interval-ms=3000`)
  3. Fetches up to **10 records** per poll (`max-poll-records=10`)
  4. Processes each message by calling `consumeOrderEvent()`
  5. Commits offsets after entire batch is processed (`AckMode.BATCH`)
  6. Continues polling indefinitely

## How the Consumer Polling Works

### Polling Configuration

The consumer uses **custom polling intervals** configured in [application.properties](kafka-consumer/src/main/resources/application.properties):

```properties
# Consumer polls every 3 seconds (3000ms)
kafka.consumer.poll-interval-ms=3000

# Maximum 10 records fetched per poll
spring.kafka.consumer.max-poll-records=10
```

### Polling Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│                   Consumer Lifecycle                    │
└─────────────────────────────────────────────────────────┘

1. Application Start
   ↓
2. @KafkaListener initializes
   ↓
3. Background thread starts polling
   ↓
   ┌─────────────────────────────────┐
   │   Polling Loop (continuous)     │
   │                                 │
   │  ┌──────────────────────────┐  │
   │  │ 1. poll() Kafka          │  │
   │  │    - Wait up to 3000ms   │  │
   │  │    - Fetch max 10 records│  │
   │  └──────────┬───────────────┘  │
   │             │                   │
   │             ▼                   │
   │  ┌──────────────────────────┐  │
   │  │ 2. Process each message  │  │
   │  │    - Call listener method│  │
   │  │    - Log event details   │  │
   │  │    - Execute business    │  │
   │  │      logic               │  │
   │  └──────────┬───────────────┘  │
   │             │                   │
   │             ▼                   │
   │  ┌──────────────────────────┐  │
   │  │ 3. Commit offsets        │  │
   │  │    - Mark batch as done  │  │
   │  └──────────┬───────────────┘  │
   │             │                   │
   │             └──────┐            │
   │                    │            │
   └────────────────────┼────────────┘
                        │
                        └──► Repeat
```

### Key Consumer Concepts

**Consumer Group**: `order-consumer-group`
- All consumers with the same group ID share the workload
- Each message goes to only ONE consumer in the group
- If you run 2 instances, they'll automatically load-balance

**Offset Management**:
- `auto-offset-reset=earliest` - Start from beginning if no offset exists
- `enable-auto-commit=true` - Automatically commit offsets every 5 seconds
- `AckMode.BATCH` - Commit after processing entire batch

**Partition Assignment**:
- Kafka automatically assigns partitions to consumers
- With 1 partition → 1 consumer handles everything
- With 3 partitions → up to 3 consumers can work in parallel

## Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 17
- Maven 3.x

### Step 1: Start Kafka Infrastructure

```bash
cd kafka-setup
docker compose up -d
```

This starts:
- Kafka broker on ports 9092/9093
- Kafka UI on http://localhost:8090

Wait 30 seconds, then verify Kafka UI shows **1 online broker**.

### Step 2: Run the Producer

Open a terminal and run:

```bash
cd kafka-producer
mvn spring-boot:run
```

The producer starts on port 8080 and waits for HTTP requests.

### Step 3: Run the Consumer

Open a **new terminal** and run:

```bash
cd kafka-consumer
mvn spring-boot:run
```

The consumer starts on port 8081 and **immediately begins polling** the `orders` topic every 3 seconds.

You'll see logs like:
```
INFO  --- Started KafkaConsumerApplication in 2.3 seconds
INFO  --- Polling for messages every 3000ms...
```

### Step 4: Send Events

In a **third terminal**, send some test orders:

```bash
# Send a single order
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "cust-123", "status": "CREATED", "amount": 49.99}'

# Send multiple orders
for i in {1..5}; do
  curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d "{\"customerId\": \"cust-$i\", \"status\": \"CREATED\", \"amount\": $((RANDOM % 100 + 10))}"
  echo ""
done
```

### Step 5: Watch the Consumer Process Events

Switch to the **consumer terminal** and watch it automatically poll and process events:

```
════════════════════════════════════════════════════════════
📦 Consumed OrderEvent:
   Topic: orders
   Partition: 0
   Offset: 0
   Key: 550e8400-e29b-41d4-a716-446655440000
   ---
   Event ID: 7d8f9a2c-3b4e-4f5a-b6c1-8d9e0f1a2b3c
   Order ID: 550e8400-e29b-41d4-a716-446655440000
   Customer ID: cust-123
   Status: CREATED
   Amount: $49.99
   Timestamp: 2026-04-08T10:30:15.123Z
════════════════════════════════════════════════════════════
```

### Step 6: Monitor in Kafka UI

Open http://localhost:8090 and explore:

1. **Topics** → `orders` → View messages
2. **Consumers** → `order-consumer-group` → See lag and offsets
3. **Brokers** → Monitor health and metrics

## Continuous Event Streaming

To continuously generate events, run this bash script:

```bash
# Sends a new order every 2 seconds
while true; do
  curl -X POST http://localhost:8080/orders \
    -H "Content-Type: application/json" \
    -d "{\"customerId\": \"cust-$RANDOM\", \"status\": \"CREATED\", \"amount\": $((RANDOM % 100 + 1))}"
  echo " → Order sent at $(date +%H:%M:%S)"
  sleep 2
done
```

The consumer will poll every 3 seconds and process batches of up to 10 messages.

## Customizing the Polling Rate

Edit [kafka-consumer/src/main/resources/application.properties](kafka-consumer/src/main/resources/application.properties):

```properties
# Change polling interval (milliseconds)
kafka.consumer.poll-interval-ms=5000  # Poll every 5 seconds

# Change batch size
spring.kafka.consumer.max-poll-records=20  # Fetch up to 20 messages per poll
```

Restart the consumer to apply changes.

## Project Structure

```
kafka-setup/
├── docker-compose.yml          # Kafka + Kafka UI
├── kafka-producer/             # Spring Boot Producer
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/demo/kafka/
│       │   ├── KafkaProducerApplication.java
│       │   ├── config/KafkaProducerConfig.java
│       │   ├── controller/EventController.java
│       │   ├── producer/OrderEventProducer.java
│       │   └── model/OrderEvent.java
│       └── resources/
│           └── application.properties
└── kafka-consumer/             # Spring Boot Consumer
    ├── pom.xml
    ├── Dockerfile              # Optional Docker build
    └── src/main/
        ├── java/com/demo/kafka/
        │   ├── KafkaConsumerApplication.java
        │   ├── config/KafkaConsumerConfig.java
        │   ├── consumer/OrderEventConsumer.java
        │   └── model/OrderEvent.java
        └── resources/
            └── application.properties
```

## Troubleshooting

### Consumer not receiving messages?

1. Check consumer is connected:
   ```bash
   docker exec kafka /opt/kafka/bin/kafka-consumer-groups.sh \
     --bootstrap-server localhost:9092 --describe --group order-consumer-group
   ```

2. Verify topic exists:
   ```bash
   docker exec kafka /opt/kafka/bin/kafka-topics.sh \
     --bootstrap-server localhost:9092 --list
   ```

3. Check consumer logs for errors

### Kafka UI shows offline broker?

- This was fixed by using dual listeners (see [docker-compose.yml](docker-compose.yml))
- Restart if needed: `docker compose restart kafka-ui`

### Messages stuck or not committing?

- Check consumer group lag in Kafka UI
- Verify `enable-auto-commit=true` in [application.properties](kafka-consumer/src/main/resources/application.properties)
- Look for errors in consumer logs

## Advanced: Running Consumer in Docker

```bash
# Build and run consumer in Docker
docker compose --profile consumer up -d

# View logs
docker compose logs -f kafka-consumer
```

Note: The Docker consumer uses `kafka:9092` (internal network) instead of `localhost:9093`.

## Clean Up

```bash
# Stop all services
docker compose down

# Stop and remove volumes (deletes all Kafka data)
docker compose down -v
```

## Next Steps

- Add error handling and retry logic
- Implement dead letter queues (DLQ)
- Add consumer metrics and monitoring
- Explore different AckModes (manual, record, batch)
- Test with multiple consumer instances for load balancing
- Add integration tests with embedded Kafka

## References

- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka UI GitHub](https://github.com/provectus/kafka-ui)
