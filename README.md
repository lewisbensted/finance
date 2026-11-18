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

[![My Skills](https://skillicons.dev/icons?i=java,spring,hibernate,sqlite,gradle,html,css,ts,)](https://skillicons.dev)

## Requirements

- Java 17 (JDK)  

*Gradle does not need to be installed separately, as the project includes the Gradle Wrapper.*

## Setup

1. Clone this repository:

```bash
  git clone https://github.com/lewisbensted/finance.git
  cd finance
```

2. Build the project.:

```bash
  ./gradlew build
```

## Running the Application

### Development

Start the application using the default development profile:

```bash
./gradlew bootRun
```

The application will be available at:
[http://localhost:8080](http://localhost:8080)

### Production

Build the application:

```bash
./gradlew clean build
```

The executable JAR is generated in `build/libs/`.  
Run the JAR in the production environment using the production profile:

```bash
java -Dspring.profiles.active=prod -jar build/libs/<jar-file>.jar
```



