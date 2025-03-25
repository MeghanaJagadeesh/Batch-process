//package com.qp.dataCentralize.config;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.data.RepositoryItemWriter;
//import org.springframework.batch.item.file.FlatFileItemReader;
//import org.springframework.batch.item.file.LineMapper;
//import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
//import org.springframework.batch.item.file.mapping.DefaultLineMapper;
//import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import com.qp.dataCentralize.entity.Datas;
//import com.qp.dataCentralize.repository.DatasRepo;
//
//import lombok.AllArgsConstructor;
//
//@Configuration
//@EnableBatchProcessing
//@AllArgsConstructor
//public class SpringBatchConfig {
//
//	private JobBuilderFactory jobBuilderFactory;
//
//	private StepBuilderFactory stepBuilderFactory;
//
//	private DatasRepo customerRepository;
//
//	@Bean
//	public WebMvcConfigurer configurer() {
//		return new WebMvcConfigurer() {
//			@Override
//			public void addCorsMappings(CorsRegistry reg) {
//				reg.addMapping("/**").allowedOrigins("*").allowedMethods("*");
//			}
//		};
//	}
//
//	@Bean
//	@StepScope
//	public ItemReader<Datas> reader(@Value("#{jobParameters['input.file']}") String inputFile) {
//		System.err.println(inputFile);
//		if (inputFile.endsWith(".csv")) {
//			System.out.println("csv");
//			FlatFileItemReader<Datas> itemReader = new FlatFileItemReader<>();
//			itemReader.setResource(new FileSystemResource(inputFile));
//			itemReader.setName("csvReader");
//			itemReader.setLinesToSkip(1); // Keep it 1 if the file has headers
//			itemReader.setLineMapper(dynamicLineMapper(inputFile)); // Pass input file for dynamic mapping
//			return itemReader;
//		} else if (inputFile.endsWith(".xlsx")) {
//			System.err.println("excel");
//			return excelReader(inputFile);
//		} else {
//			throw new IllegalArgumentException("Unsupported file format: " + inputFile);
//		}
//	}
//
//	private ItemReader<Datas> excelReader(String inputFile) {
//		return new ExcelItemReader(inputFile);
//	}
//
//	private LineMapper<Datas> dynamicLineMapper(String inputFile) {
//		DefaultLineMapper<Datas> lineMapper = new DefaultLineMapper<>();
//		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
//		lineTokenizer.setDelimiter(",");
//		lineTokenizer.setStrict(false);
//
//		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
//			String headerLine = br.readLine();
//			if (headerLine != null) {
//				String[] headers = headerLine.split(",");
//
//				Map<String, String> headerMappings = new HashMap<>();
//				headerMappings.put("name", "name");
//				headerMappings.put("email", "email");
//				headerMappings.put("phone number", "phoneNumber");
//				headerMappings.put("category", "category");
//				headerMappings.put("designation", "designation");
//				headerMappings.put("address", "address");
//				headerMappings.put("company name", "companyName");
//				headerMappings.put("industry type", "industryType");
//				headerMappings.put("entry date", "entryDate");
//				headerMappings.put("entered by", "enteredBy");
//
//				System.out.println("Headers in CSV: " + Arrays.toString(headers));
//				List<String> validHeaders = new ArrayList<>();
//				for (String header : headers) {
//					String trimmedHeader = header.trim().toLowerCase();
//					if (headerMappings.containsKey(trimmedHeader)) {
//						validHeaders.add(headerMappings.get(trimmedHeader)); // Use mapped field name
//					}
//				}
//				String[] mappedHeaders = validHeaders.toArray(new String[0]);
//				lineTokenizer.setNames(mappedHeaders);
//				System.out.println("Mapped Headers: " + Arrays.toString(mappedHeaders));
//			}
//		} catch (IOException e) {
//			throw new RuntimeException("Error reading CSV headers", e);
//		}
//		BeanWrapperFieldSetMapper<Datas> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//		fieldSetMapper.setTargetType(Datas.class);
//		fieldSetMapper.setStrict(false); // Allow missing columns
//
//		lineMapper.setLineTokenizer(lineTokenizer);
//		lineMapper.setFieldSetMapper(fieldSetMapper);
//		return lineMapper;
//	}
//
//	@Bean
//	@StepScope
//	public ItemProcessor<Datas, Datas> processor(@Value("#{jobParameters['categoryType']}") String categoryType,
//			@Value("#{jobParameters['enteredBy']}") String enteredBy) {
//		DataProcessor processor = new DataProcessor();
//		processor.setCategory(categoryType, enteredBy);
//		return processor;
//	}
//
//	@Bean
//	public RepositoryItemWriter<Datas> writer() {
//		RepositoryItemWriter<Datas> writer = new RepositoryItemWriter<>();
//		writer.setRepository(customerRepository);
//		writer.setMethodName("save");
//		return writer;
//	}
//
//	@Bean
//	public Step step1() {
//		return stepBuilderFactory.get("csv-step").<Datas, Datas>chunk(50).reader(reader(null))
//				.processor(processor(null, null)).writer(writer()).taskExecutor(taskExecutor()).build();
//	}
//
//	@Bean
//	public Job runJob() {
//		return jobBuilderFactory.get("importCustomers").flow(step1()).end().build();
//
//	}
//
//	@Bean
//	public TaskExecutor taskExecutor() {
//		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
//		asyncTaskExecutor.setConcurrencyLimit(20);
//		return asyncTaskExecutor;
//	}
//
//}


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
