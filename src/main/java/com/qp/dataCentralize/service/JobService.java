package com.qp.dataCentralize.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qp.dataCentralize.dao.DatasDao;
import com.qp.dataCentralize.entity.Datas;

@Service
public class JobService {
	
	@Autowired
	DatasDao dao;

	private final JobLauncher jobLauncher;
	private final Job importCustomersJob;

	@Autowired
	public JobService(JobLauncher jobLauncher, @Qualifier("runJob") Job importCustomersJob) {
		this.jobLauncher = jobLauncher;
		this.importCustomersJob = importCustomersJob;
	}

	public ResponseEntity<Map<String, Object>> uploadFile(MultipartFile file, String category, String enteredBy) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			Path tempPath = Files.createTempFile("temp", ".csv");
			Files.copy(file.getInputStream(), tempPath, StandardCopyOption.REPLACE_EXISTING);
			JobParameters params = new JobParametersBuilder().addString("input.file", tempPath.toString())
					.addString("categoryType", category).addString("enteredBy", enteredBy).toJobParameters();
			jobLauncher.run(importCustomersJob, params);
			map.put("message", "File uploaded and processed successfully");
			map.put("code", HttpStatus.OK.value());
			map.put("status", "success");
			return ResponseEntity.status(HttpStatus.OK).body(map);
		} catch (Exception e) {
			map.put("message", "Something went wrong");
			map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
			map.put("status", "fail");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		}
	}

	public Page<Datas> fetchAll(int pageNo, int pageSize) {
		return dao.getPaginatedData(pageNo, pageSize);
		
	}

	public Page<Datas> fetchByCategory(int pageNo, int pageSize, String category) {
		return dao.getPaginatedData(pageNo, pageSize,category);
	}

}
