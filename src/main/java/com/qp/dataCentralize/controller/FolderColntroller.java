package com.qp.dataCentralize.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.service.FileUploader;
import com.qp.dataCentralize.service.FolderService;

@RestController
@RequestMapping("/planotech-inhouse")
public class FolderColntroller {

	@Autowired
	FileUploader fileUploader;

	@Autowired
	FolderService folderService;

	@PostMapping("/create/folder/{folderName}")
	public ResponseEntity<Map<String, Object>> createFolder(@PathVariable String folderName) {
		return folderService.saveFolder(folderName);
	}

	@PostMapping("/uploadFile")
	public ResponseEntity<Map<String, Object>> createFolder(MultipartFile[] files, @RequestParam int folderId) {
		return fileUploader.handleFileUpload(files, folderId);
	}
	
	@GetMapping("/getFolders")
	public ResponseEntity<List<Object[]>> getAllFolders() {
		return folderService.getAllFolders();
	}
	
	@GetMapping("/getFiles")
	public ResponseEntity<FolderEntity> getAllFiles(@RequestParam int folderId) {
		return folderService.getAllFile();
	}
}
