package com.jd.batch.spring.batch.config;

import com.jd.batch.spring.batch.entity.Customer;
import com.jd.batch.spring.batch.listner.StepSkipListener;
import com.jd.batch.spring.batch.partition.ColumnRangePartitioner;
import com.jd.batch.spring.batch.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;


/**
 * Created by jd birla on 03-02-2023 at 06:25
 */
@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    //private CustomerRepository customerRepository;
    private CustomerWritter customerWritter;


    @Bean
    @StepScope
    public FlatFileItemReader<Customer> reader(@Value("#{jobParameters[fullPathFileName]}") String pathToFile){
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(new File(pathToFile)));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return  itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob","age");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return  lineMapper;
    }

    @Bean
    public CustomerProcessor processor(){
        return  new CustomerProcessor();
    }

//    @Bean
//    public RepositoryItemWriter<Customer> writer()
//    {
//        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
//        writer.setRepository(customerRepository);
//        writer.setMethodName("save");
//        return writer;
//    }

    @Bean
    public ColumnRangePartitioner partitioner()
    {
        return new ColumnRangePartitioner();
    }
    @Bean
    public PartitionHandler partitionHandler(FlatFileItemReader<Customer> itemReader)
    {
        TaskExecutorPartitionHandler executorPartitionHandler = new TaskExecutorPartitionHandler();
        executorPartitionHandler.setGridSize(4);
        executorPartitionHandler.setTaskExecutor(taskExecutor());
        executorPartitionHandler.setStep(salveStep(itemReader));
        return executorPartitionHandler;
    }
    @Bean
    public Step salveStep(FlatFileItemReader<Customer> itemReader)
    {
        return  stepBuilderFactory.get("slavestep").<Customer,Customer>chunk(250)
                .reader(itemReader)
                .processor(processor())
                //.writer(writer())
                .writer(customerWritter)
               // .taskExecutor(taskExecutor())
                .faultTolerant()
                .skipLimit(100)
                //.skip(NumberFormatException.class)
               // .noSkip(IllegalArgumentException.class)
                .listener(stepSkipListener())
                .skipPolicy(customExceptionSkipPolicy())
                .build();

    }
    @Bean
    public CustomExceptionSkipPolicy customExceptionSkipPolicy()
    {
        return new CustomExceptionSkipPolicy();
    }

    @Bean
    public StepSkipListener stepSkipListener()
    {
        return new StepSkipListener();
    }

    @Bean
    public Step masterStep(FlatFileItemReader<Customer> itemReader)
    {
        return  stepBuilderFactory.get("masterstep").partitioner(salveStep(itemReader).getName(),partitioner())
                .partitionHandler(partitionHandler(itemReader))
                .listener(stepSkipListener())
                .build();

    }
    @Bean
    public Job runJob(FlatFileItemReader<Customer> itemReader)
    {
        return jobBuilderFactory.get("importCustomers")
                .flow(masterStep(itemReader))
                .end().build();
    }
    @Bean
    public TaskExecutor taskExecutor()
    {
//        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
//        taskExecutor.setConcurrencyLimit(10);
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setQueueCapacity(4);
        return taskExecutor;
    }
}
