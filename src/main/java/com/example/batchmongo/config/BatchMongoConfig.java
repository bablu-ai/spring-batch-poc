package com.example.batchmongo.config;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.conversions.Bson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MongoJobExplorerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MongoJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Spring Batch configuration using MongoDB for ALL metadata storage.
 */
@Slf4j
@Configuration
@EnableBatchProcessing
@EnableMongoRepositories(basePackages = "com.example.batchmongo.repository")
public class BatchMongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Bean
    public MongoClient mongoClient() {
        MongoClient mongoClient =  MongoClients.create(mongoUri);
        testMongoConnection(mongoClient);
        return mongoClient;
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, database);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory, MongoConverter converter) {
        if (converter instanceof MappingMongoConverter mappingMongoConverter) {
            mappingMongoConverter.setMapKeyDotReplacement("_");
        }
        return new MongoTemplate(mongoDatabaseFactory, converter);
    }

    public void testMongoConnection(MongoClient mongoClient) {
        try {
            MongoDatabase database = mongoClient.getDatabase("admin");
            Bson command = new BsonDocument("ping", new BsonInt64(1));
            database.runCommand(command);
            log.info("Pinged your deployment. You successfully connected to MongoDB!");
        } catch (MongoException me) {
            log.error("An error occurred while trying to connect to MongoDB: {}", me.getMessage());
            mongoClient.close();
        }
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override protected Object doGetTransaction() { return new Object(); }
            @Override protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) {}
            @Override protected void doCommit(DefaultTransactionStatus status) {}
            @Override protected void doRollback(DefaultTransactionStatus status) {}
        };
    }

    @Bean
    public JobRepository jobRepository(MongoTemplate mongoTemplate, PlatformTransactionManager transactionManager) throws Exception {
        initializeSequences(mongoTemplate);
        MongoJobRepositoryFactoryBean jobRepositoryFactoryBean = new MongoJobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setMongoOperations(mongoTemplate);
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.afterPropertiesSet();
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public JobExplorer jobExplorer(MongoTemplate mongoTemplate, PlatformTransactionManager transactionManager) throws Exception {
        MongoJobExplorerFactoryBean mongoJobExplorerFactoryBean = new MongoJobExplorerFactoryBean();
        mongoJobExplorerFactoryBean.setMongoOperations(mongoTemplate);
        mongoJobExplorerFactoryBean.setTransactionManager(transactionManager);
        mongoJobExplorerFactoryBean.afterPropertiesSet();
        return mongoJobExplorerFactoryBean.getObject();
    }

    /**
     * Initialize sequences with the correct field name 'count' and type Long (int64).
     * Spring Batch 5.2 MongoSequenceIncrementer uses 'count' field (not 'sequence').
     * Uses BsonDocument to guarantee int64 storage.
     */
    private void initializeSequences(MongoTemplate mongoTemplate) {
        String collectionName = "BATCH_SEQUENCES";
        // Spring Batch 5.2 uses 'count' as the sequence field name
        String sequenceField = "count";

        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }

        String[] sequences = {
                "BATCH_JOB_INSTANCE_SEQ",
                "BATCH_JOB_EXECUTION_SEQ",
                "BATCH_STEP_EXECUTION_SEQ"
        };

        // Get the native MongoDB collection with BsonDocument codec to preserve types
        MongoCollection<BsonDocument> collection = mongoTemplate
                .getCollection(collectionName)
                .withDocumentClass(BsonDocument.class);

        for (String seq : sequences) {
            BsonDocument existing = collection.find(Filters.eq("_id", seq)).first();

            if (existing == null) {
                // Insert using BsonDocument to guarantee int64 type
                BsonDocument doc = new BsonDocument()
                        .append("_id", new BsonString(seq))
                        .append(sequenceField, new BsonInt64(0));
                collection.insertOne(doc);
                log.info("Initialized sequence: {} with BsonInt64(0)", seq);
            } else {
                // Update existing to ensure int64 type
                long currentVal = 0L;
                if (existing.containsKey(sequenceField)) {
                    currentVal = existing.get(sequenceField).asNumber().longValue();
                }

                collection.updateOne(
                        Filters.eq("_id", seq),
                        Updates.set(sequenceField, new BsonInt64(currentVal))
                );
                log.info("Ensured sequence {} is stored as int64 with value {}", seq, currentVal);
            }
        }
    }
}
