# AirX Backend

## Prerequisites

### Prepare Kafka

Install Kafka and a bootstrap server.

### Prepare Database and Redis

Install a database and Redis.

### Initialize Database
s
Run `src/main/resources/db/init.sql` on the database.

### Prepare application.properties

Create `application.properties` from `application.properties.template` in `src/main/resources/` and fill in the values.

## Start

Run `mvn spring-boot:run` to start the application.
