package com.example.batchmongo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main Spring Boot application for Spring Batch with MongoDB.
 * Executes CSV processing batch job with MongoDB-based metadata storage.
 */
@Slf4j
@SpringBootApplication
public class SpringBatchMongoApp {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(SpringBatchMongoApp.class, args)));
    }

    /**
     * CommandLineRunner to execute the batch job on application startup.
     * Launches the CSV processing job with a timestamp parameter to ensure uniqueness.
     */
    @Bean
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job csvProcessingJob) {
        return args -> {
            log.info("Starting CSV processing batch job...");

            try {
                // Create job parameters with timestamp to ensure each run is unique
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("timestamp", System.currentTimeMillis())
                        .toJobParameters();

                // Launch the job
                var execution = jobLauncher.run(csvProcessingJob, jobParameters);

                log.info("Job Execution Status: {}", execution.getStatus());
                log.info("Job Instance ID: {}", execution.getJobInstance().getId());
                log.info("Job Execution ID: {}", execution.getId());
                log.info("Exit Status: {}", execution.getExitStatus());

                if (execution.getAllFailureExceptions().isEmpty()) {
                    log.info("CSV processing completed successfully!");
                    log.info("Output file created: output.csv");
                } else {
                    log.error("Job completed with errors:");
                    execution.getAllFailureExceptions().forEach(e ->
                            log.error("Error: {}", e.getMessage(), e));
                }

            } catch (Exception e) {
                log.error("Failed to execute batch job", e);
                throw e;
            }
        };
    }
}
