# Spring Batch with MongoDB

A production-ready Spring Batch application that stores ALL metadata (job instance, job execution, step execution, execution context) in MongoDB instead of a traditional RDBMS.

## Overview

This application demonstrates:
- **MongoDB-backed JobRepository**: Complete replacement of JDBC-based metadata storage
- **CSV Processing**: Read from CSV, transform records, write to output CSV
- **Custom DAO Implementations**: MongoDB-specific implementations of Spring Batch DAOs
- **Spring Boot 3.x**: Latest Spring Batch with modern configuration
- **Configurable**: All MongoDB connection details via environment variables

## Prerequisites

- **Java 17+** (required for Spring Boot 3.x)
- **MongoDB 4.4+** running locally or accessible remotely
  - Can be standalone or replica set
  - For replica set: Enables full transaction support
  - For standalone: Uses ResourcelessTransactionManager (development mode)
- **Gradle** (wrapper included)

## Project Structure

```
spring-batch-poc/
├── src/main/java/com/example/batchmongo/
│   ├── SpringBatchMongoApp.java              # Main application entry point
│   ├── config/
│   │   ├── BatchMongoConfig.java             # MongoDB & Batch configuration
│   │   └── BatchJobConfig.java               # Job, step, reader, writer config
│   ├── domain/
│   │   ├── Person.java                       # CSV record domain model
│   │   ├── BatchJobInstance.java             # Job instance metadata document
│   │   ├── BatchJobExecution.java            # Job execution metadata document
│   │   └── BatchStepExecution.java           # Step execution metadata document
│   ├── processor/
│   │   └── PersonItemProcessor.java          # Item transformation processor
│   └── repository/
│       ├── MongoJobRepositoryFactoryBean.java # Custom JobRepository factory
│       ├── MongoJobInstanceDao.java          # Job instance DAO
│       ├── MongoJobExecutionDao.java         # Job execution DAO
│       ├── MongoStepExecutionDao.java        # Step execution DAO
│       ├── MongoExecutionContextDao.java     # Execution context DAO
│       ├── BatchJobInstanceRepository.java   # Spring Data MongoDB repo
│       ├── BatchJobExecutionRepository.java  # Spring Data MongoDB repo
│       └── BatchStepExecutionRepository.java # Spring Data MongoDB repo
├── src/main/resources/
│   └── application.yml                       # Application configuration
├── input.csv                                  # Sample input data
├── build.gradle                              # Build configuration
└── README.md                                 # This file
```

## Configuration

### MongoDB Connection

The application uses environment variables for MongoDB configuration. You can set these via:

1. **Environment variables**:
```bash
export MONGO_URI="mongodb://localhost:27017"
export MONGO_DB="springbatch"
export MONGO_USER="admin"
export MONGO_PASSWORD="password"
```

2. **application.yml** (default values provided):
```yaml
spring:
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27017}
      database: ${MONGO_DB:springbatch}
      username: ${MONGO_USER:admin}
      password: ${MONGO_PASSWORD:password}
```

3. **Command-line arguments**:
```bash
./gradlew bootRun --args='--spring.data.mongodb.uri=mongodb://localhost:27017'
```

### MongoDB Collections

The application creates the following collections to store Spring Batch metadata:

- `BATCH_JOB_INSTANCE` - Job instance records
- `BATCH_JOB_EXECUTION` - Job execution records
- `BATCH_STEP_EXECUTION` - Step execution records
- `BATCH_JOB_EXECUTION_CONTEXT` - Job execution context data
- `BATCH_STEP_EXECUTION_CONTEXT` - Step execution context data

## Building the Project

```bash
# Build the project
./gradlew build

# Build without tests
./gradlew build -x test

# Clean and build
./gradlew clean build
```

## Running the Application

### Option 1: Using Gradle

```bash
# Run with default configuration (localhost MongoDB)
./gradlew bootRun

# Run with custom MongoDB URI
MONGO_URI="mongodb://remote-host:27017" ./gradlew bootRun

# Run with all custom settings
MONGO_URI="mongodb+srv://user:pass@cluster.mongodb.net/" \
MONGO_DB="mybatchdb" \
./gradlew bootRun
```

### Option 2: Using JAR

```bash
# Build the JAR
./gradlew bootJar

# Run the JAR
java -jar build/libs/spring-batch-poc-0.0.1-SNAPSHOT.jar

# Run with environment variables
MONGO_URI="mongodb://localhost:27017" \
java -jar build/libs/spring-batch-poc-0.0.1-SNAPSHOT.jar
```

## How It Works

### Batch Job Flow

1. **Application Starts**: `SpringBatchMongoApp.main()` launches the Spring Boot application
2. **Job Launch**: `CommandLineRunner` triggers the `csvProcessingJob`
3. **CSV Reading**: `FlatFileItemReader` reads records from `input.csv`
4. **Processing**: `PersonItemProcessor` transforms each record (uppercase name, add timestamp)
5. **CSV Writing**: `FlatFileItemWriter` writes transformed records to `output.csv`
6. **Metadata Storage**: All job/step metadata persisted to MongoDB collections

### Sample Transformation

**Input** (input.csv):
```csv
name,email,age
John Doe,john@example.com,30
Jane Smith,jane@example.com,28
```

**Processing**:
- Convert name to uppercase
- Add processing timestamp

**Output** (output.csv):
```csv
name,email,age,processedAt
JOHN DOE,john@example.com,30,2024-01-15T10:30:45.123
JANE SMITH,jane@example.com,28,2024-01-15T10:30:45.234
```

## Expected Output

### Console Output

```
Starting CSV processing batch job...
Transforming person: John Doe -> JOHN DOE
Transforming person: Jane Smith -> JANE SMITH
...
Job Execution Status: COMPLETED
Job Instance ID: 1
Job Execution ID: 1
Exit Status: exitCode=COMPLETED;exitDescription=
CSV processing completed successfully!
Output file created: output.csv
```

### MongoDB Verification

Query MongoDB to verify metadata storage:

```javascript
// Connect to MongoDB
use springbatch

// View job instances
db.BATCH_JOB_INSTANCE.find().pretty()

// View job executions
db.BATCH_JOB_EXECUTION.find().pretty()

// View step executions
db.BATCH_STEP_EXECUTION.find().pretty()

// Example output:
{
  "_id" : ObjectId("..."),
  "jobInstanceId" : NumberLong(1),
  "jobName" : "csvProcessingJob",
  "jobKey" : "d41d8cd98f00b204e9800998ecf8427e",
  "version" : 0
}
```

## Troubleshooting

### MongoDB Connection Failed

**Error**: `MongoTimeoutException: Timed out after 30000 ms`

**Solution**:
- Verify MongoDB is running: `mongod --version` or check service status
- Check connection string format
- Ensure network access (firewall, security groups)
- For MongoDB Atlas: Whitelist your IP address

### Transaction Support Warning

**Warning**: `Transaction support is not available`

**Explanation**:
- Standalone MongoDB doesn't support multi-document transactions
- Application uses `ResourcelessTransactionManager` for development
- For production, use MongoDB replica set for full ACID transactions

**Fix**: Deploy MongoDB as a replica set or accept idempotent operation design

### FileNotFoundException: input.csv

**Error**: `FileNotFoundException: input.csv (No such file or directory)`

**Solution**:
- Ensure `input.csv` exists in the project root directory
- Or specify custom path via environment variable:
  ```bash
  INPUT_FILE=/path/to/custom/input.csv ./gradlew bootRun
  ```

### OutOfMemoryError

**Error**: `Java heap space OutOfMemoryError`

**Solution**:
- Reduce chunk size in `application.yml`: `batch.chunk.size: 5`
- Increase JVM heap: `./gradlew bootRun -Dorg.gradle.jvmargs="-Xmx2g"`

## Testing

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests com.example.batchmongo.SpringBatchMongoAppTests
```

## Customization

### Change Chunk Size

Edit `src/main/resources/application.yml`:
```yaml
batch:
  chunk:
  size: 20  # Process 20 items per chunk
```

### Modify Transformation Logic

Edit `src/main/java/com/example/batchmongo/processor/PersonItemProcessor.java`:
```java
@Override
public Person process(Person person) throws Exception {
    // Custom transformation logic here
    String transformed = person.getName().toLowerCase(); // Example: lowercase
    return new Person(transformed, person.getEmail(), person.getAge(), LocalDateTime.now());
}
```

### Add Error Handling

Edit `BatchJobConfig.java` step configuration:
```java
return new StepBuilder("csvProcessingStep", jobRepository)
    .<Person, Person>chunk(chunkSize, transactionManager)
    .reader(reader)
    .processor(processor)
    .writer(writer)
    .faultTolerant()
    .skipLimit(10)
    .skip(Exception.class)
    .build();
```

## Production Considerations

1. **MongoDB Replica Set**: Deploy MongoDB as replica set for transaction support
2. **Connection Pooling**: Configure appropriate pool size via `MongoClientSettings`
3. **Monitoring**: Enable Spring Boot Actuator for health checks and metrics
4. **Logging**: Configure structured logging (JSON format) for production
5. **Security**: Use MongoDB authentication, TLS/SSL for connections
6. **Idempotency**: Design jobs to handle restarts gracefully (e.g., unique job parameters)

## Dependencies

Key dependencies (see `build.gradle` for full list):

- **Spring Boot 3.2.1**
- **Spring Batch** (latest via Spring Boot)
- **Spring Data MongoDB**
- **MongoDB Driver 4.11.1**
- **Lombok** (code generation)
- **OpenCSV 5.9** (CSV processing)
- **Embedded MongoDB 4.11.0** (testing)

## License

This is a demonstration project. Use at your own discretion.

## Support

For issues or questions:
1. Check MongoDB connection and logs
2. Verify Java version compatibility
3. Review Spring Batch documentation: https://spring.io/projects/spring-batch
4. MongoDB documentation: https://docs.mongodb.com

---
https://github.com/PascoalBayonne/Batch-Processing-ETL/tree/feature/mongo-writer/src
**Generated with Claude Code** - Production-ready Spring Batch with MongoDB metadata storage
