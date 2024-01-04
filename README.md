# Spring Batch 
- Java Techie : https://www.youtube.com/watch?v=hr2XTbKSdAQ&list=PLVz2XdJiJQxyC2LMLgDjFGJBX9TJAM4A2
 ![image](https://user-images.githubusercontent.com/69948118/216734685-b74227f7-fb4e-4b46-abfc-aae8856da8ca.png)
![image](https://user-images.githubusercontent.com/69948118/216734753-37b5b6e4-b983-435d-8dc8-c88b2e566de4.png)
![image](https://user-images.githubusercontent.com/69948118/216734917-0179680c-1197-490f-9ebc-2cc63e6b9dd8.png)
![image](https://user-images.githubusercontent.com/69948118/216743479-f7a038fe-b109-4c78-a879-aa6247d85243.png)
![image](https://user-images.githubusercontent.com/69948118/216746186-f784c5c7-f4ec-4536-9f2c-e57000e58277.png)
![image](https://github.com/jdbirla/JD_master_Spring_Batch/assets/69948118/0e753538-0667-448a-a710-2abde74c62e0)
![image](https://github.com/jdbirla/JD_master_Spring_Batch/assets/69948118/acbd2de7-9e9b-4923-a883-42f2ac048de4)






## Code
- SpringBatchConfig.java
```java
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

```
