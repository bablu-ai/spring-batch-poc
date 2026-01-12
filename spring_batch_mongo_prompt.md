# Spring Batch with MongoDB - Claude Code Generation Prompt

## Objective
Generate a complete, production-ready Spring Batch application that:
- Stores ALL metadata (job instance, job execution, step execution, chunk data) in MongoDB instead of an RDBMS
- Reads CSV input, processes records, writes to output CSV
- Includes configuration, domain models, repositories, and a runnable demo
- Uses Spring Data MongoDB for persistence
- Includes placeholder variables for MongoDB connection details (URI, username, password, database name)

## Deliverables Required

### 1. **build.gradle**
- Spring Boot 3.x with Spring Batch latest stable
- Spring Data MongoDB
- Embedded MongoDB for testing (testcontainers or de.flapdoodle.embed.mongo)
- Lombok, Jackson for CSV
- Standard Gradle plugins (Spring Boot, Java, Gradle Wrapper)

### 2. **Application Configuration (application.properties / application.yml)**
- Placeholder MongoDB URI: `${MONGO_URI:mongodb://localhost:27017}`
- Placeholder username/password: `${MONGO_USER:admin}`, `${MONGO_PASSWORD:password}`
- Placeholder database: `${MONGO_DB:springbatch}`
- Spring Batch datasource configured to use MongoDB (not H2/MySQL/Postgres)
- Batch table prefix settings (if applicable)

### 3. **MongoDB Configuration Class (BatchMongoConfig.java)**
- Custom `JobRepository` backed by MongoDB
- Custom `PlatformTransactionManager` for MongoDB
- `JobLauncher` bean
- `MapJobRegistry`
- MongoOperations / MongoTemplate bean setup
- Enable MongoDB repositories

### 4. **Spring Batch Job Definition (BatchJobConfig.java)**
- Job: `csvProcessingJob`
- Step 1: Read from CSV (input.csv placeholder)
- ItemProcessor: simple transformation (e.g., uppercase name field or add timestamp)
- Write to CSV (output.csv placeholder)
- Chunk size: 10 items
- Error handling / skip policy (optional)

### 5. **Domain Models**
- `Person.java` (sample data model: name, email, age)
- `JobMetadata.java` (if custom metadata tracking needed beyond Spring Batch defaults)

### 6. **CSV Reader/Writer Configuration**
- `FlatFileItemReader` for input.csv
- `FlatFileItemWriter` for output.csv
- Proper delimiter, enclosure, field mapping

### 7. **Item Processor (PersonItemProcessor.java)**
- Simple processor that demonstrates transformation
- Example: Convert name to uppercase, add processing timestamp

### 8. **Main Application Class (SpringBatchMongoApp.java)**
- Entry point with `main()` method
- Programmatic job launch with sample parameters
- Graceful shutdown

### 9. **input.csv (Sample Data)**
```
name,email,age
John Doe,john@example.com,30
Jane Smith,jane@example.com,28
Bob Johnson,bob@example.com,35
```

### 10. **README.md**
- Prerequisites: Java 11+, MongoDB running locally or accessible
- How to set MongoDB URI, user, password
- How to run: `./gradlew bootRun` with env vars or properties file
- Expected output: output.csv with transformed records
- Metadata location: MongoDB collections (e.g., `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`)

## Implementation Notes

- **MongoDB as JobRepository**: Use Spring Batch's `JobRepository` interface with a custom MongoDB implementation OR use `SimpleJobRepository` with MongoTemplate for job tracking.
- **Transaction Management**: MongoDB's transaction support is limited; ensure idempotent operations or handle partial failures gracefully.
- **Metadata Tables**: Map Spring Batch's standard metadata schemas (job_instance, job_execution, step_execution, etc.) to MongoDB collections.
- **Connection Pooling**: Configure appropriate connection pool size via MongoClient settings.
- **Placeholders Format**: Use `${VAR_NAME:default_value}` for all sensitive/environment-specific values.

## Constraints & Assumptions
- Java 11 or higher
- Spring Boot 3.x
- Latest Spring Batch stable release
- MongoDB 4.4+ (for transaction support if needed)
- No external message queue (in-memory job launch)
- Single-threaded execution (no multi-threaded chunk processing initially)

## Code Style
- Use Lombok annotations (@Data, @RequiredArgsConstructor, etc.)
- Follow Spring conventions (camelCase for property names, @Bean for configuration)
- Comprehensive inline comments for non-obvious logic
- Exception handling with meaningful error messages
- Resource management (try-with-resources for file I/O)

## Validation Checklist
- [ ] Code compiles without errors
- [ ] pom.xml has all required dependencies
- [ ] MongoDB configuration is externalized (environment variables or properties file)
- [ ] Job runs end-to-end without exceptions
- [ ] input.csv is read correctly
- [ ] Records are processed (transformation applied)
- [ ] output.csv is written with expected data
- [ ] Metadata is persisted in MongoDB (query collections directly to verify)
- [ ] README provides clear setup & run instructions

---

**Ready to generate: YES**

When you generate, produce all files (build.gradle, gradle.properties, Java classes, config files, README, sample CSVs) in a single, organized output. Include console output examples showing successful job completion.