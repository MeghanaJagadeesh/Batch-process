package com.qp.dataCentralize.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.dao.DatasDao;
import com.qp.dataCentralize.entity.Datas;
import com.qp.dataCentralize.repository.DatasRepo;

@Service
public class JobService {

	@Autowired
	DatasDao dao;

	@Autowired
	UserService userService;

	@Autowired
	DatasRepo datasRepo;

	private final JobLauncher jobLauncher;
	private final Job importCustomersJob;

	@Autowired
	public JobService(JobLauncher jobLauncher, @Qualifier("runJob") Job importCustomersJob) {
		this.jobLauncher = jobLauncher;
		this.importCustomersJob = importCustomersJob;
	}

//	public ResponseEntity<Map<String, Object>> uploadFile(MultipartFile file, String category, JsonNode user) {
//		
//		Map<String, Object> map = new HashMap<>();
//	    try {
//	    	String extension=null;
//	    	String filename=file.getOriginalFilename();
//	    	if(filename.endsWith(".csv")) {
//	    		extension=".csv";
//	    	}else if(filename.endsWith(".xlsx")) {
//	    		extension=".xlsx";
//	    	}else {
//	    		extension="";
//	    	}
//	        Path tempPath = Files.createTempFile("temp", extension);
//	        try (InputStream inputStream = file.getInputStream()) {
//	            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
//	        }
//        JobParameters params = new JobParametersBuilder().addString("input.file", tempPath.toString())
//					.addString("categoryType", category).addString("enteredBy", userService.getCreatedByInfo(user)).toJobParameters();
//			
//			jobLauncher.run(importCustomersJob, params);
//			map.put("message", "File uploaded and processed successfully");
//			map.put("code", HttpStatus.OK.value());
//			map.put("status", "success");
//			return ResponseEntity.status(HttpStatus.OK).body(map);
//		} catch (Exception e) {
//			e.printStackTrace();
//			map.put("message", "Something went wrong");
//			map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
//			map.put("status", "fail");
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
//		}
//	}

	public ResponseEntity<Map<String, Object>> uploadFile(MultipartFile file, String category, JsonNode user) {

		Map<String, Object> map = new HashMap<>();
		Path tempPath = null;
		try {
			tempPath = Files.createTempFile("temp", file.getOriginalFilename()); // Keep original file name
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
			}
			JobParameters params = new JobParametersBuilder().addString("input.file", tempPath.toString())
					.addString("categoryType", category).addString("enteredBy", userService.getCreatedByInfo(user))
					.toJobParameters();

			jobLauncher.run(importCustomersJob, params);
			map.put("message", "File uploaded and processed successfully");
			map.put("code", HttpStatus.OK.value());
			map.put("status", "success");
			return ResponseEntity.status(HttpStatus.OK).body(map);

		} catch (IOException e) {
			map.put("message", "Error reading the uploaded file.");
			map.put("code", HttpStatus.BAD_REQUEST.value());
			map.put("status", "fail");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		} catch (JobExecutionException e) {
			map.put("message", "Something went wrong during file processing.");
			map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("status", "fail");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		} finally {
			if (tempPath != null) {
				try {
					Files.deleteIfExists(tempPath);
				} catch (IOException e) {
				}
			}
		}
	}

	public Page<Datas> fetchAll(int pageNo, int pageSize) {
		return dao.getPaginatedData(pageNo, pageSize);
	}

	public Page<Datas> fetchByCategory(int pageNo, int pageSize, String category) {
		return dao.getPaginatedData(pageNo, pageSize, category);
	}

	public Page<Datas> searchText(String searchText, int pageNo, int pageSize) {
		Specification<Datas> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();

			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("category")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("designation")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("industryType")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("entryDate")),
					"%" + searchText.toLowerCase() + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("enteredBy")),
					"%" + searchText.toLowerCase() + "%"));

			return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
		};
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return datasRepo.findAll(spec, pageable);
	}

	public ResponseEntity<Map<String, Object>> addCustomers(Datas data, JsonNode user) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("UTC"));
		Instant now = Instant.now();
		data.setEntryDate(formatter.format(now));
		data.setEnteredBy(userService.getCreatedByInfo(user));
		datasRepo.save(data);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("message", "Data added successfully");
		map.put("code", HttpStatus.OK.value());
		map.put("status", "success");
		return ResponseEntity.status(HttpStatus.OK).body(map);
	}

}
