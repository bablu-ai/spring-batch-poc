package com.example.batchmongo.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing Spring Batch job instance metadata.
 * Maps to BATCH_JOB_INSTANCE in traditional JDBC implementation.
 */
@Data
@Document(collection = "BATCH_JOB_INSTANCE")
public class BatchJobInstance {
    @Id
    private String id;
    private Long jobInstanceId;
    private String jobName;
    private String jobKey;
    private Integer version;
}
