package com.qp.dataCentralize.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.service.UserService;

@RestController
@RequestMapping("/planotech-inhouse")
public class UserController {

	@Autowired
	UserService userService;

	private String adminName="Admin";
	
	private String adminEmail="admin0700@gmail.com";

	private String adminPassword="SriAdmin@0700";

	@PostMapping("user/login")
	public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String password) {
		return userService.login(email, password);
	}

	@DeleteMapping("admin/delete/file")
	public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam int folderId,
			@RequestBody FileEntity fileEntity, @RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		JsonNode response = user.get("body");
		String username = response.get("userName").asText();
		String userEmail = response.get("userEmail").asText();
		String password = response.get("userPassword").asText();
		if (username.equals(adminName) && userEmail.equals(adminEmail) && password.equals(adminPassword)) {
			return userService.deleteFile(folderId, fileEntity);
		} else {
			map.put("message", "Employess Restricted, Admin use only");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
	}

	@DeleteMapping("admin/delete/folder/{folderId}")
	public ResponseEntity<Map<String, Object>> deleteFolder(@PathVariable int folderId,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		JsonNode response = user.get("body");
		String username = response.get("userName").asText();
		String userEmail = response.get("userEmail").asText();
		String password = response.get("userPassword").asText();
		if (username.equals(adminName) && userEmail.equals(adminEmail) && password.equals(adminPassword)) {
			return userService.deleteFolder(folderId);
		} else {
			map.put("message", "Employess Restricted, Admin use only");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
	}

	@DeleteMapping("admin/delete/data/{id}")
	public ResponseEntity<Map<String, Object>> deletedata(@PathVariable int id,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		JsonNode response = user.get("body");
		String username = response.get("userName").asText();
		String userEmail = response.get("userEmail").asText();
		String password = response.get("userPassword").asText();
		if (username.equals(adminName) && userEmail.equals(adminEmail) && password.equals(adminPassword)) {
			return userService.deletedata(id);
		} else {
			map.put("message", "Employess Restricted, Admin use only");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
	}

	@GetMapping("/get/profileInfo")
	public Object getProfileInfo(@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		String email = user.get("body").get("userEmail").asText();
		user = userService.getEmployeeByEmail(email);
		return ResponseEntity.ok().body(user);
	}
}
