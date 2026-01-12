package com.example.batchmongo.domain;

import lombok.Data;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing Spring Batch step execution metadata.
 * Maps to BATCH_STEP_EXECUTION in traditional JDBC implementation.
 */
@Data
@Document(collection = "BATCH_STEP_EXECUTION")
public class BatchStepExecution {
    @Id
    private String id;
    private Long stepExecutionId;
    private Long jobExecutionId;
    private String stepName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BatchStatus status;
    private Long commitCount;
    private Long readCount;
    private Long filterCount;
    private Long writeCount;
    private Long readSkipCount;
    private Long writeSkipCount;
    private Long processSkipCount;
    private Long rollbackCount;
    private String exitCode;
    private String exitMessage;
    private Integer version;
    private LocalDateTime lastUpdated;
}
