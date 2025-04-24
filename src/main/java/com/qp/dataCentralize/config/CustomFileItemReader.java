package com.qp.dataCentralize.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;

import com.qp.dataCentralize.entity.Datas;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomFileItemReader implements ItemReader<Datas> {

	private String inputFile;
	private String fileType; // "csv" or "excel" or "unknown"
	private FlatFileItemReader<Datas> flatFileItemReader;
	private ExcelItemReader excelItemReader;
	private boolean initialized = false;
	private LineMapper<Datas> lineMapper;

	public CustomFileItemReader(String inputFile) {
		this.inputFile = inputFile;
		this.fileType = determineFileType(inputFile);
		
		if (this.fileType.equals("csv")) {
			this.flatFileItemReader = new FlatFileItemReader<>();
			this.flatFileItemReader.setResource(new FileSystemResource(inputFile));
			this.flatFileItemReader.setName("csvReader");
			this.flatFileItemReader.setLinesToSkip(1);
			initializeFlatFileItemReader();
		} else if (this.fileType.equals("excel")) {
			this.excelItemReader = new ExcelItemReader(inputFile);
		} else {
			throw new IllegalArgumentException("Unsupported file format: " + inputFile);
		}
	}

	private String determineFileType(String inputFile) {
		if (inputFile != null && inputFile.toLowerCase().endsWith(".csv")) {
			return "csv";
		} else if (inputFile != null && inputFile.toLowerCase().endsWith(".xlsx")) {
			return "excel";
		} else {
			return "unknown";
		}
	}

	@Override
	public Datas read() {
		try {
			if (fileType.equals("csv")) {
				if (!initialized) {

					initialized = true;
				}
				return flatFileItemReader.read();
			} else if (fileType.equals("excel")) {
				return excelItemReader.read();
			} else {
				new RuntimeException("Unsupported file format for " + inputFile);
				return null;
			}
		} catch (Exception e) {
			new RuntimeException(e.getMessage());
		}
		return null;
	}

	public void initializeFlatFileItemReader() throws ItemStreamException {
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			String headerLine = br.readLine();
			if (headerLine != null) {
				String[] headers = headerLine.split(",");

				Map<String, String> headerMappings = new HashMap<>();
				headerMappings.put("name", "name");
				headerMappings.put("email", "email");
				headerMappings.put("phone number", "phoneNumber");				
				headerMappings.put("category", "category");
				headerMappings.put("designation", "designation");
				headerMappings.put("address", "address");
				headerMappings.put("company name", "companyName");
				headerMappings.put("industry type", "industryType");
				headerMappings.put("entry date", "entryDate");
				headerMappings.put("entered by", "enteredBy");

				log.info("Headers in CSV: " + Arrays.toString(headers));
				List<String> validHeaders = new ArrayList<>();
				for (String header : headers) {
					String trimmedHeader = header.trim().toLowerCase();
					System.out.println(trimmedHeader);
					if (headerMappings.containsKey(trimmedHeader)) {
						validHeaders.add(headerMappings.get(trimmedHeader));
					}
				}
				System.out.println("validate "+validHeaders);
				String[] mappedHeaders = validHeaders.toArray(new String[0]);
				log.info("Mapped Headers: " + Arrays.toString(mappedHeaders));

				DefaultLineMapper<Datas> lineMapper = new DefaultLineMapper<>();
				DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
				lineTokenizer.setDelimiter(",");
				lineTokenizer.setStrict(false);
				lineTokenizer.setNames(mappedHeaders);

				BeanWrapperFieldSetMapper<Datas> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
				fieldSetMapper.setTargetType(Datas.class);
				fieldSetMapper.setStrict(false);

				lineMapper.setLineTokenizer(lineTokenizer);
				lineMapper.setFieldSetMapper(fieldSetMapper);
				this.lineMapper = lineMapper;
				flatFileItemReader.setLineMapper(lineMapper);

			} else {
				throw new IllegalStateException("CSV file is empty or has no header row.");
			}
		} catch (IOException e) {
			throw new ItemStreamException("Error reading CSV headers", e);
		}
		try {
			flatFileItemReader.open(new ExecutionContext());
		} catch (Exception e) {
			throw new ItemStreamException("Error opening FlatFileItemReader", e);
		}
	}
}
