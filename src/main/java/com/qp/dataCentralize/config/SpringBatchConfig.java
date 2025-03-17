package com.qp.dataCentralize.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
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
	public FlatFileItemReader<Datas> reader(@Value("#{jobParameters['input.file']}") String inputFile) {
		FlatFileItemReader<Datas> itemReader = new FlatFileItemReader<>();
		itemReader.setResource(new FileSystemResource(inputFile));
		itemReader.setName("csvReader");
		itemReader.setLinesToSkip(1); // Keep it 1 if the file has headers
		itemReader.setLineMapper(dynamicLineMapper(inputFile)); // Pass input file for dynamic mapping
		return itemReader;
	}

//	private LineMapper<Datas> dynamicLineMapper(String inputFile) {
//	    DefaultLineMapper<Datas> lineMapper = new DefaultLineMapper<>();
//	    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
//
//	    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
//	        String headerLine = br.readLine();
//	        if (headerLine != null) {
//	            String[] headers = headerLine.split(","); // Assuming CSV is comma-separated
//	            lineTokenizer.setNames(headers);
//	        }
//	    } catch (IOException e) {
//	        throw new RuntimeException("Error reading CSV headers", e);
//	    }
//
//	    BeanWrapperFieldSetMapper<Datas> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
//	    fieldSetMapper.setTargetType(Datas.class);
//
//	    lineMapper.setLineTokenizer(lineTokenizer);
//	    lineMapper.setFieldSetMapper(fieldSetMapper);
//	    return lineMapper;
//	}

	private LineMapper<Datas> dynamicLineMapper(String inputFile) {
		DefaultLineMapper<Datas> lineMapper = new DefaultLineMapper<>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);

		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			String headerLine = br.readLine();
			if (headerLine != null) {
				String[] headers = headerLine.split(",");

				Map<String, String> headerMappings = new HashMap<>();
				headerMappings.put("company name", "companyName");
				headerMappings.put("name", "name");
				headerMappings.put("designation", "designation");
				headerMappings.put("phone number", "phoneNumber"); 
				headerMappings.put("industry type", "industryType");

				String[] mappedHeaders = Arrays.stream(headers)
						.map(header -> headerMappings.getOrDefault(header.trim().toLowerCase(), header.trim()))
						.toArray(String[]::new);

				lineTokenizer.setNames(mappedHeaders);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading CSV headers", e);
		}

		BeanWrapperFieldSetMapper<Datas> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Datas.class);

		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
	}

	private LineMapper<Datas> lineMapper() {
		DefaultLineMapper<Datas> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("companyName", "name", "designation", "address", "email", "phoneNumber", "industryType");

		BeanWrapperFieldSetMapper<Datas> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(Datas.class);

		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
	}

	@Bean
	@StepScope
	public ItemProcessor<Datas, Datas> processor(@Value("#{jobParameters['categoryType']}") String categoryType,
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
		return stepBuilderFactory.get("csv-step").<Datas, Datas>chunk(50).reader(reader(null))
				.processor(processor(null, null)).writer(writer()).taskExecutor(taskExecutor()).build();
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
