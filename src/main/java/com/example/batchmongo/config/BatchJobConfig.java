package com.example.batchmongo.config;

import com.example.batchmongo.domain.Person;
import com.example.batchmongo.processor.PersonItemProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration for CSV processing.
 * Reads from input CSV, transforms records, and writes to output CSV.
 */
@Slf4j
@Configuration
public class BatchJobConfig {

    @Value("${batch.input.file}")
    private String inputFile;

    @Value("${batch.output.file}")
    private String outputFile;

    @Value("${batch.chunk.size}")
    private int chunkSize;

    /**
     * CSV file reader configuration.
     * Reads Person records from input CSV file.
     */
    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new FileSystemResource(inputFile))
                .linesToSkip(1) // Skip header row
                .delimited()
                .delimiter(",")
                .names("name", "email", "age")
                .targetType(Person.class)
                .build();
    }

    /**
     * CSV file writer configuration.
     * Writes transformed Person records to output CSV file.
     */
    @Bean
    public FlatFileItemWriter<Person> writer() {
        // Field extractor to convert Person object to CSV fields
        BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"name", "email", "age", "processedAt"});
        fieldExtractor.afterPropertiesSet();

        // Line aggregator to format CSV output
        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<Person>()
                .name("personItemWriter")
                .resource(new FileSystemResource(outputFile))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("name,email,age,processedAt"))
                .build();
    }

    /**
     * Processing step configuration.
     * Reads from CSV, processes records, and writes to output CSV.
     */
    @Bean
    public Step csvProcessingStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   FlatFileItemReader<Person> reader,
                                   PersonItemProcessor processor,
                                   FlatFileItemWriter<Person> writer) {
        return new StepBuilder("csvProcessingStep", jobRepository)
                .<Person, Person>chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    /**
     * Main job configuration.
     * Defines the CSV processing job with a single step.
     */
    @Bean
    public Job csvProcessingJob(JobRepository jobRepository, Step csvProcessingStep) {
        return new JobBuilder("csvProcessingJob", jobRepository)
                .start(csvProcessingStep)
                .build();
    }
}
