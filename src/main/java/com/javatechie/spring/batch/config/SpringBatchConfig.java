package com.javatechie.spring.batch.config;

import com.javatechie.spring.batch.entity.Customer;
import com.javatechie.spring.batch.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private final CustomerRepository customerRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;

    @Bean
    public FlatFileItemReader<Customer> reader() {
        var itemReader = new FlatFileItemReader<Customer>();
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setResource(new ClassPathResource("customers.csv"));
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    //Helper method to convert a raw line of text from csv file into a domain object i.e., Customer in this case
    private LineMapper<Customer> lineMapper() {

        var lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        var fieldSetMapper = new BeanWrapperFieldSetMapper<Customer>();
        fieldSetMapper.setTargetType(Customer.class);
        fieldSetMapper.setConversionService(new DefaultConversionService());

        var lineMapper = new DefaultLineMapper<Customer>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    RepositoryItemWriter<Customer> writer() {
        var writer = new RepositoryItemWriter<Customer>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    Step step1() {
        var stepBuilder = new StepBuilder("csv-step", jobRepository).<Customer, Customer>chunk(10, transactionManager);
        return stepBuilder.reader(reader()).processor(processor()).writer(writer()).taskExecutor(taskExecutor).build();
    }

    @Bean
    Job runJob() {
        var jobBuilder = new JobBuilder("importCustomers", jobRepository);
        var flow = new FlowBuilder<Flow>("importCustomersFlow").start(step1()).build();
        return jobBuilder.start(flow).end().build();
    }
}