package com.qp.dataCentralize.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.qp.dataCentralize.entity.Datas;
import com.qp.dataCentralize.repository.DatasRepo;

import lombok.AllArgsConstructor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private DatasRepo customerRepository;

    @Bean
    public WebMvcConfigurer configurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry reg) {
                reg.addMapping("/**").allowedOrigins("*").allowedMethods("*");
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<Datas> reader(
            @Value("#{jobParameters['input.file']}") String inputFile) {
        return new CustomFileItemReader(inputFile);
    }

    @Bean
    @StepScope
    public ItemProcessor<Datas, Datas> processor(
            @Value("#{jobParameters['categoryType']}") String categoryType,
            @Value("#{jobParameters['enteredBy']}") String enteredBy) {
        DataProcessor processor = new DataProcessor();
        processor.setCategory(categoryType, enteredBy);
        return processor;
    }

    @Bean
    public RepositoryItemWriter<Datas> writer() {
        RepositoryItemWriter<Datas> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("csv-step")
                .<Datas, Datas>chunk(50)
                .reader(reader(null)) // Pass null for multipartFile; it will be injected correctly
                .processor(processor(null, null))
                .writer(writer())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomers").flow(step1()).end().build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(20);
        return asyncTaskExecutor;
    }
}
