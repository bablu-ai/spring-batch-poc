package com.example.batchmongo.domain;

import lombok.Data;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing Spring Batch job execution metadata.
 * Maps to BATCH_JOB_EXECUTION in traditional JDBC implementation.
 */
@Data
@Document(collection = "BATCH_JOB_EXECUTION")
public class BatchJobExecution {
    @Id
    private String id;
    private Long jobExecutionId;
    private Long jobInstanceId;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BatchStatus status;
    private String exitCode;
    private String exitMessage;
    private Integer version;
    private LocalDateTime lastUpdated;
}
