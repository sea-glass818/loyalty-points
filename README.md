# Loyalty Points System

Spring Boot backend for a retail loyalty program. Customers can make purchases and earn points, check their current balance and tier, redeem points for rewards, receive purchase refunds, and rely on the system to exclude points after their 12-month expiry.

## Tech stack

- Java 17
- Spring Boot 4.0.5
- Gradle
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Lombok
- Log4j2
- Spring Boot Actuator

## Run locally

Prerequisites:

- JDK 17
- Gradle 8.14 or newer

Start the app:

```sh
gradle bootRun
```

Run tests:

```sh
gradle test
```

The API runs at:

```text
http://localhost:8080
```

Health check:

```sh
curl http://localhost:8080/actuator/health
```

H2 console:

```text
http://localhost:8080/h2-console/
```

Use these values:

```text
JDBC URL: jdbc:h2:mem:loyaltydb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
User: sa
Password:
```

## API

### Earn points

Creates the customer if they do not already exist, stores the purchase, and creates a points ledger entry. Points are awarded at 1 point per whole dollar spent and expire after 12 months.

```sh
curl -X POST http://localhost:8080/api/v1/purchases \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "alice",
    "purchaseId": "order-123",
    "amount": "125.75"
  }'
```

Example response:

```json
{
  "customerId": "alice",
  "purchaseId": "order-123",
  "amount": 125.75,
  "pointsEarned": 125,
  "purchasedAt": "2026-05-14T22:00:00Z",
  "expiresAt": "2027-05-14T22:00:00Z"
}
```

You may also provide `purchasedAt`:

```json
{
  "customerId": "alice",
  "purchaseId": "order-124",
  "amount": "50.00",
  "purchasedAt": "2026-05-14T12:00:00Z"
}
```

### Check balance

```sh
curl http://localhost:8080/api/v1/customers/alice/balance
```

Example response:

```json
{
  "customerId": "alice",
  "availablePoints": 175,
  "tier": "SILVER"
}
```

Expired ledger entries are excluded from the available balance. Tier is based on non-refunded spend in the rolling 12 months:

- Silver: below $1,000
- Gold: $1,000 to $4,999.99
- Platinum: $5,000 or more

### List rewards

```sh
curl http://localhost:8080/api/v1/rewards
```

Example response:

```json
[
  {
    "rewardId": "free-coffee",
    "name": "Free Coffee",
    "pointCost": 50
  },
  {
    "rewardId": "five-dollar-coupon",
    "name": "$5 Coupon",
    "pointCost": 500
  }
]
```

### Redeem points

Redeems points for a reward if the customer has enough unexpired points. Redemption consumes the eligible ledger rows closest to expiry first.

```sh
curl -X POST http://localhost:8080/api/v1/redemptions \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "alice",
    "rewardId": "free-coffee"
  }'
```

Example response:

```json
{
  "customerId": "alice",
  "rewardId": "free-coffee",
  "rewardName": "Free Coffee",
  "pointsSpent": 50,
  "remainingBalance": 75,
  "redeemedAt": "2026-05-14T22:05:00Z",
  "allocations": [
    {
      "ledgerEntryId": 1,
      "points": 50,
      "expiresAt": "2027-05-14T22:00:00Z"
    }
  ]
}
```

### Refund a purchase

Refunds a purchase and claws back the points earned from it. Unused points from the purchase are removed. If points from that purchase were already redeemed, the refund creates point debt and the balance can become negative.

```sh
curl -X POST http://localhost:8080/api/v1/refunds \
  -H 'Content-Type: application/json' \
  -d '{
    "purchaseId": "order-123"
  }'
```

Example response:

```json
{
  "refundId": 1,
  "customerId": "alice",
  "purchaseId": "order-123",
  "earnedPoints": 125,
  "removedAvailablePoints": 75,
  "debtPoints": 50,
  "remainingBalance": -50,
  "refundedAt": "2026-05-14T22:10:00Z"
}
```

## Data model

### Customer

Represents a loyalty member. The `externalId` is the caller-provided customer identifier, such as `alice`, and is unique.

### Purchase

Represents a customer purchase. `purchaseId` is unique to prevent duplicate point awards if the same order is submitted more than once. Refunded purchases are marked with `refunded` and `refundedAt`, and excluded from tier spend.

### PointsLedger

Represents points earned from a purchase and refund adjustments. Earn rows store original points, remaining points, earned timestamp, and expiry timestamp. Balance queries only include rows where `expiresAt` is in the future. Redemption locks eligible rows and decrements `remainingPoints` in expiry order. Refund debt is recorded as a negative ledger entry.

### Refund

Represents a purchase refund audit record. It stores the original earned points, the available points removed from the customer balance, any debt created because points had already been redeemed, and the refund timestamp.

The explicit H2 schema is defined in [schema.sql](/Users/gchoi/dev/git/loyalty-points/src/main/resources/schema.sql). Spring initializes the database from this file and Hibernate validates that the JPA mappings match it. Indexes support customer lookup, rolling 12-month spend, FIFO redemption scans, purchase-ledger lookup, and refund history lookup.

## Simplifying assumptions

- Customers are created lazily on their first purchase. A separate customer-management API is intentionally omitted to keep the exercise focused on loyalty behavior.
- H2 is used as the in-memory database to keep local setup and reviewer demo steps simple. For a production deployment, I would switch to a persistent database such as PostgreSQL and manage schema changes with migrations.

## Application config

Primary configuration lives in [application.yml](/Users/gchoi/dev/git/loyalty-points/src/main/resources/application.yml).

Important settings:

- H2 in-memory datasource: `jdbc:h2:mem:loyaltydb`
- H2 console: `/h2-console`
- Actuator health endpoint: `/actuator/health`
- Point expiry: `loyalty.points-expire-after-months: 12`
- Schema initialization: `src/main/resources/schema.sql`
- Logging: console output plus rolling files under `logs/loyalty-points.log`
- Tracing: Micrometer Tracing with the Brave bridge generates `traceId` and `spanId` for sampled requests and includes them in console and file logs

## Error handling

`GlobalExceptionHandler` returns consistent `ErrorDetails` responses for:

- Duplicate purchase ids: `409 Conflict`
- Unknown customers or rewards: `404 Not Found`
- Insufficient points: `409 Conflict`
- Unknown purchases: `404 Not Found`
- Already refunded purchases: `409 Conflict`
- Validation errors: `400 Bad Request`
- Unexpected errors: `500 Internal Server Error`

## Notes and next steps

This implementation covers the required core flows:

- Make a purchase and earn points
- Check current balance
- Redeem points for a reward
- Exclude points after 12 months
- Return tier status with balance
- Refund purchases and claw back points
- Redeem points closest to expiry first

With more time, I would add:

- Integration tests with MockMvc or WebTestClient
- Database migrations with Flyway or Liquibase
- A persistent database profile for local development

## AI usage disclosure

I used ChatGPT/Codex to scaffold and iterate on the Spring Boot application, including the Gradle build, JPA model, service/controller code, exception handling, and README.
