# Phynance Spring Boot Backend

This is the Java Spring Boot backend for the Phynance project.

## Requirements

- Java 17 or higher
- Maven

## Setup

1. Install dependencies:
   ```sh
   mvn clean install
   ```
2. Run the application:
   ```sh
   mvn spring-boot:run
   ```
3. The API will be available at `http://localhost:8080`.

## H2 Database Console

- Access at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- User: `sa`
- Password: (leave blank)
