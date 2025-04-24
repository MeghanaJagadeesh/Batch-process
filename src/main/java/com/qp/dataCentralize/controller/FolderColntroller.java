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
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.helper.FileUploader;
import com.qp.dataCentralize.service.FolderService;
import com.qp.dataCentralize.service.UserService;

@RestController
@RequestMapping("/planotech-inhouse")
public class FolderColntroller {

	@Autowired
	FileUploader fileUploader;

	@Autowired
	FolderService folderService;

	@Autowired
	UserService userService;

	@PostMapping("/create/folder/{folderName}")
	public ResponseEntity<Map<String, Object>> createFolder(@PathVariable String folderName,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.saveFolder(folderName, user);
	}

	@PostMapping("/uploadFile")
	public ResponseEntity<Map<String, Object>> createFolder(MultipartFile[] files, @RequestParam int folderId,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.handleFile(files, folderId, user);
	}
	
	@PostMapping("/upload/largeFile")
	public ResponseEntity<Map<String, Object>> uploadLargeFile(MultipartFile[] files, @RequestParam int folderId,
			@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.handleLargeFile(files, folderId, user);
	}

	@GetMapping("/getFolders/{department}")
	public Object getAllFolders(@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "10") int pageSize, @RequestHeader("Authorization") String token, @PathVariable String department) {
		Map<String, Object> map = new HashMap<String, Object>();
		System.out.println("page ****"+pageNo+" "+pageSize);
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.getAllFolders(department,pageNo, pageSize);
	}

	@GetMapping("/getFiles")
	public Object getAllFiles(@RequestParam int folderId, @RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.getAllFile(folderId);
	}
	
	@PostMapping("/add/favorite")
	public ResponseEntity<Map<String, Object>> addFavorites(@RequestHeader("Authorization") String token, @RequestBody FavoriteFolders favoriteFolders) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.addFavorites(favoriteFolders);
	}
	
	@GetMapping("/getAll/favorites")
	public ResponseEntity<Map<String, Object>> getAllFavorites(@RequestHeader("Authorization") String token) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.getAllFavorites();
	}
	
	@GetMapping("/unstar/favorites")
	public ResponseEntity<Map<String, Object>> unStarFavorites(@RequestHeader("Authorization") String token, @RequestParam int id,@RequestParam String type) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.unStarFavorites(id,type);
	}

	@GetMapping("/get/accounts/dashboard")
	public ResponseEntity<Map<String, Object>> getAccountsDashboard(
			@RequestHeader("Authorization") String token,
			@RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "10") int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode user = userService.validateToken(token);
		if (user == null) {
			map.put("message", "Invalid Token or User Not found");
			map.put("code", 400);
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
		return folderService.getAccountsDashboard(user, pageNo, pageSize);
	}
}
