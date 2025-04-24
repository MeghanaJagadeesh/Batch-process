package com.qp.dataCentralize.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.Datas;
import com.qp.dataCentralize.service.JobService;
import com.qp.dataCentralize.service.UserService;

@RestController
@RequestMapping("/planotech-inhouse")
public class JobController {

	@Autowired
	JobService jobService;

	@Autowired
	UserService userService;
	
	@PostMapping("/add/customers")
	public ResponseEntity<Map<String, Object>> addCustomers(@RequestBody Datas data, @RequestHeader("Authorization") String token){
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return jobService.addCustomers(data,user);
	}

	@PostMapping("/importCustomers")
	public ResponseEntity<Map<String, Object>> importCsvToDBJob(@RequestParam("file") MultipartFile file,
			@RequestParam String category, @RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return jobService.uploadFile(file, category, user);
	}

	@GetMapping("/getAll/data")
	public Object fetchAll(@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "50") int pageSize, @RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return jobService.fetchAll(pageNo, pageSize);
	}

	@GetMapping("/get/data/{category}")
	public Object fetchByCategory(@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "50") int pageSize, @PathVariable String category,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return jobService.fetchByCategory(pageNo, pageSize, category);
	}

	@GetMapping("/search")
	public Object search(@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "50") int pageSize,@RequestParam String searchText,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return jobService.searchText(searchText, pageNo, pageSize);

	}
}
