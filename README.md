# Finance

## Description

A full-stack stock market web application built with Java, Spring Boot and TypeScript, with SQLite for persistent data storage.

The application allows users to look up stock quotes and simulate stock trades using real-time stock prices and virtual funds. Users can also manage their portfolio and view their transaction history through a web interface.
## Features

- 🔐 **Authentication:** Register, log in and securely manage user sessions.
- 📈 **Stock quotes:** Look up stock prices.
- 🛒 **Stock Trading:** Buy and sell shares using virtual funds.
- 💰 **Portfolio:** View your stock holdings and available cash balance.
- 📊 **Transaction history:** View previous stock purchases and sales.
- 💵 **Virtual funds:** Deposit and withdraw funds.

## Built With

**Backend:** Java, Spring Boot, Spring Data JPA  
**Frontend:** HTML, CSS, TypeScript  
**Database:** SQLite  
**ORM:** Hibernate  
**Build Tool:** Gradle  
**Containerisation:** Docker

[![My Skills](https://skillicons.dev/icons?i=java,spring,hibernate,sqlite,gradle,html,css,ts,docker)](https://skillicons.dev)

## Requirements

- Java 17 (JDK)
- Docker 
- Docker Compose

*MySQL Server is provided through Docker Compose, so it does not need to be installed separately.*

*Gradle does not need to be installed separately, as the project includes the Gradle Wrapper.*

## Setup

1. Clone this repository:

```bash
  git clone https://github.com/lewisbensted/finance.git
  cd finance
```

2. Build the project:

```bash
./gradlew build
```

3. Create environment files:

```bash
cp .env.dev.example .env.dev (for dev and test)
cp .env.dev.example .env.prod
```
Update .env.dev and .env.prod with your local credentials and configuration.


## Running the Application

### Development & Testing

Start the MySQL server using Docker Compose:
```bash
docker compose -p finance-dev --env-file .env.dev -f compose.dev.yaml up -d
```

Connect directly to the database:
```bash
docker exec -it finance-dev-mysql-1 mysql -u YOUR_MYSQL_USER -p
```

Start the application using the default development profile:
```bash
./gradlew dev
```

The application will be available at: [http://localhost:8080](http://localhost:8080)

Run tests:
```bash
./gradlew test
```

To stop the database server:
```bash
docker compose -p finance-dev --env-file .env.dev -f compose.dev.yaml down   
```
To stop the database server and delete stored data:
```bash
docker compose -p finance-dev --env-file .env.dev -f compose.dev.yaml down -v 
```
⚠️ Destructive - permanently deletes database volumes and stored data.

### Production

Start the MySQL server, as well as build and run the application:
```bash
docker compose -p finance-prod --env-file .env.prod -f compose.prod.yaml up -d
```
To stop the running containers:
```bash
docker compose -p finance-prod --env-file .env.prod -f compose.prod.yaml down
```
To stop the running containers and delete stored data:
```bash
docker compose -p finance-prod --env-file .env.prod -f compose.prod.yaml down -v 
```
⚠️ Destructive - permanently deletes database volumes and stored data.






