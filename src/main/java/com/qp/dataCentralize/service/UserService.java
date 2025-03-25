package com.qp.dataCentralize.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.repository.DatasRepo;
import com.qp.dataCentralize.repository.FavoriteFolderRepository;
import com.qp.dataCentralize.repository.FolderRepository;

@Service
public class UserService {

	@Autowired
	FileUploader fileUploader;

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	DatasRepo datasRepo;
	
	@Autowired
	FavoriteFolderRepository favoriteFolderRepository;

	public JsonNode getAllEmployee() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<JsonNode> response = restTemplate.exchange("http://147.93.28.177:4040/admin/fetchallemployee",
				HttpMethod.POST, requestEntity, JsonNode.class);
		return response.getBody();
	}

	public JsonNode getEmployeeByEmail(String email) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<JsonNode> response = restTemplate.exchange(
				"http://147.93.28.177:4040/emp/fetchemployeebyemail?email=" + email, HttpMethod.GET, requestEntity,
				JsonNode.class);
		return response.getBody();
	}

	public ResponseEntity<Map<String, Object>> login(String email, String password) {
		JsonNode euser = null;
		Map<String, Object> map = new HashMap<String, Object>();
		JsonNode responseBody = getEmployeeByEmail(email);
		if (responseBody != null && responseBody.has("status") && responseBody.get("status").asBoolean()) {
			JsonNode user = responseBody.get("body");
			if (user != null && user.get("userEmail").asText().equals(email)
					&& user.get("userPassword").asText().equals(password)) {
				euser = user;
			}
			if (euser == null) {
				map.put("message", "Incorrect Password");
				map.put("code", HttpStatus.BAD_REQUEST.value());
				map.put("status", "fail");
				return ResponseEntity.badRequest().body(map);
			}
			map.put("message", "Login Successful");
			map.put("code", 200);
			map.put("status", "success");
			map.put("data", euser);
			map.put("token", Security.encrypt(euser.get("userEmail") + "", "123"));
			return ResponseEntity.ok(map);
		} else {
			map.put("message", "Inavild email and password");
			map.put("code", HttpStatus.BAD_REQUEST.value());
			map.put("status", "fail");
			return ResponseEntity.badRequest().body(map);
		}
	}

	public JsonNode validateToken(String token) {
		token = token.substring(7);
		String email = Security.decrypt(token, "123");
		if (email == null)
			return null;
		email = email.replaceAll("\"", "");
		return getEmployeeByEmail(email);
	}

	public String getCreatedByInfo(JsonNode response) {
		JsonNode user = response.get("body");
		String userName = user.get("userName").asText();
		String userDepartment = user.get("userDepartment").asText();
		return userName + "(" + userDepartment + ")";
	}

	public ResponseEntity<Map<String, Object>> deleteFile(int folderId, FileEntity fileEntity) {
		return fileUploader.deleteFile(folderId, fileEntity);
	}

	public ResponseEntity<Map<String, Object>> deleteFolder(int folderId) {
		FavoriteFolders fav = favoriteFolderRepository.findByEntityIdAndType(folderId, "folder");
		if (fav != null) {
			favoriteFolderRepository.delete(fav);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		FolderEntity folder = folderRepository.findById(folderId).get();
		List<FileEntity> files = new ArrayList<>(folder.getFiles());
		for (FileEntity fileEntity : files) {
			fileUploader.deleteFile(folderId, fileEntity);
		}
		
		folderRepository.delete(folder);
		map.put("message", "folder deleted successfully");
		map.put("code", 200);
		map.put("status", "success");
		return ResponseEntity.ok(map);
	}

	public ResponseEntity<Map<String, Object>> deletedata(int id) {
		Map<String, Object> map = new HashMap<String, Object>();
		datasRepo.deleteById(id);
		map.put("message", "data deleted successfully");
		map.put("code", 200);
		map.put("status", "success");
		return ResponseEntity.ok(map);
	}
}
