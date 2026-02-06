# Vibe Coding Project

This is a Spring Boot 2.7.x (Java 8) project with Maven multi-module build.

## How to Build and Test

### Build the project
```bash
mvn clean install
```

### Run tests
```bash
mvn test
```

Expected result: All tests pass successfully.

## How to Start the Application

```bash
mvn -pl backend spring-boot:run
```

Expected result: The application starts successfully on port 8080.

## How to Verify Health Endpoints

### Check Spring Boot Actuator health endpoint
```bash
curl http://localhost:8080/actuator/health
```

Expected result: `{"status":"UP"}`

### Check custom health endpoint
```bash
curl http://localhost:8080/health
```

Expected result: `{"status":"UP"}`