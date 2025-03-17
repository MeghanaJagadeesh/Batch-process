package com.qp.dataCentralize.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qp.dataCentralize.entity.Datas;
import com.qp.dataCentralize.service.JobService;

@RestController
@RequestMapping("/planotech-inhouse")
public class JobController {

	@Autowired
	JobService jobService;

	@PostMapping("/importCustomers")
	public ResponseEntity<Map<String, Object>> importCsvToDBJob(@RequestParam("file") MultipartFile file,
			@RequestParam String category, @RequestParam String enteredBy) {
		return jobService.uploadFile(file, category, enteredBy);
	}

	@GetMapping("/getAll/data")
	public Page<Datas> fetchAll(@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "50") int pageSize) {
		return jobService.fetchAll(pageNo, pageSize);
	}

	@GetMapping("/get/data/{category}")
	public Page<Datas> fetchByCategory(@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "50") int pageSize, @PathVariable String category) {
		return jobService.fetchByCategory(pageNo, pageSize, category);
	}
}
