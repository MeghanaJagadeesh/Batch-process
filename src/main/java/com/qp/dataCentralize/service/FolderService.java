package com.qp.dataCentralize.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.repository.FolderRepository;

@Service
public class FolderService {

	@Autowired
	FolderRepository folderRepository;

	public ResponseEntity<Map<String, Object>> saveFolder(String folderName) {
		Map<String, Object> response = new HashMap<String, Object>();
		FolderEntity folder = folderRepository.findByFolderName(folderName);
		if (folder != null) {
			response.put("message", "folder already exists with this email");
			response.put("code", HttpStatus.BAD_REQUEST.value());
			response.put("status", "fail");
			return ResponseEntity.badRequest().body(response);
		}
		FolderEntity folderEntity = new FolderEntity();
		folderEntity.setFolderName(folderName);
		folderRepository.save(folderEntity);
		response.put("message", "folder created");
		response.put("code", HttpStatus.OK.value());
		response.put("status", "success");
		return ResponseEntity.ok().body(response);
	}

	public ResponseEntity<List<Object[]>> getAllFolders() {
		List<Object[]> obj = folderRepository.findFolderNamesAndIds();
		return ResponseEntity.ok(obj);
	}

	public ResponseEntity<FolderEntity> getAllFile() {
		return ResponseEntity.ok(folderRepository.findById(1).get());
	}

}
