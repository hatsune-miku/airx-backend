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

### Prepare RSA Key Pair

Create an RSA key pair and put the private key in `src/main/resources/public.crt` and the public key in `src/main/resources/private.key`.

## Start

Run `mvn spring-boot:run` to start the application.
