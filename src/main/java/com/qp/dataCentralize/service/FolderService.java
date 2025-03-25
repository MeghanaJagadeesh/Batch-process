package com.qp.dataCentralize.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderDTO;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.repository.FavoriteFolderRepository;
import com.qp.dataCentralize.repository.FileRepository;
import com.qp.dataCentralize.repository.FolderRepository;

@Service
public class FolderService {

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	FileRepository fileRepository;

	@Autowired
	FileUploader fileUploader;

	@Autowired
	UserService userService;

	@Autowired
	UploadLargeFile uploadLargeFile;

	@Autowired
	FavoriteFolderRepository favoriteFolderRepository;

	public ResponseEntity<Map<String, Object>> saveFolder(String folderName, JsonNode user) {

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
		folderEntity.setTime(Instant.now());
		folderEntity.setCreatedBy(userService.getCreatedByInfo(user));
		folderRepository.save(folderEntity);
		response.put("message", "folder created");
		response.put("code", HttpStatus.OK.value());
		response.put("status", "success");
		return ResponseEntity.ok().body(response);
	}

	public ResponseEntity<Page<FolderEntity>> getAllFolders(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("time").descending());
		Page<FolderEntity> obj = folderRepository.findFolderNamesAndIds(pageable);
		return ResponseEntity.ok(obj);
	}

	public ResponseEntity<FolderEntity> getAllFile(int folderId) {
		return ResponseEntity.ok(folderRepository.findFolderWithFilesSortedByTime(folderId));
	}

	public ResponseEntity<Map<String, Object>> handleLargeFile(MultipartFile[] files, int folderId, JsonNode user) {
		Map<String, Object> map = new HashMap<String, Object>();
		FolderEntity folder = folderRepository.findById(folderId)
				.orElseThrow(() -> new RuntimeException("Folder not found"));
		List<FileEntity> fileList = uploadLargeFile.handleFileUpload(files);
		String createdBy = userService.getCreatedByInfo(user);
		for (FileEntity file : fileList) {
			file.setCreatedBy(createdBy);
			saveFileToFolder(folder, file);
		}
		map.put("message", "file uploaded successfully");
		map.put("code", HttpStatus.OK);
		map.put("status", "success");
		map.put("data", fileList);
		return ResponseEntity.ok(map);
	}

	public ResponseEntity<Map<String, Object>> handleFile(MultipartFile[] files, int folderId, JsonNode user) {
		Map<String, Object> map = new HashMap<String, Object>();
		FolderEntity folder = folderRepository.findById(folderId)
				.orElseThrow(() -> new RuntimeException("Folder not found"));
		List<FileEntity> fileList = fileUploader.handleFileUpload(files);
		System.err.println(fileList);
		String createdBy = userService.getCreatedByInfo(user);
		for (FileEntity file : fileList) {
			file.setCreatedBy(createdBy);
			saveFileToFolder(folder, file);
		}
		map.put("message", "file uploaded successfully");
		map.put("code", HttpStatus.OK);
		map.put("status", "success");
		map.put("data", fileList);
		return ResponseEntity.ok(map);
	}

	private void saveFileToFolder(FolderEntity folder, FileEntity fileEntity) {
		System.out.println("save file "+fileEntity);
		List<FileEntity> files = folder.getFiles();
		if (files == null || files.isEmpty()) {
			files = new ArrayList<>();
			files.add(fileEntity);
		} else {
			files.add(fileEntity);
		}
		folder.setTime(Instant.now());
		folder.setFiles(files);
		System.out.println("saved");
		folderRepository.save(folder);
		System.out.println("done");
	}

	public ResponseEntity<Map<String, Object>> addFavorites(FavoriteFolders favoriteFolders) {
		Map<String, Object> map = new HashMap<String, Object>();
		favoriteFolderRepository.save(favoriteFolders);
		map.put("message", "favorites added successfully");
		map.put("code", 200);
		map.put("status", "success");
		return ResponseEntity.ok(map);
	}

//	public ResponseEntity<Map<String, Object>> getAllFavorites() {
//		Map<String, Object> map = new HashMap<String, Object>();
//		List<FavoriteFolders> favorites = favoriteFolderRepository.findAll();
//		List<Object> fav = new ArrayList<Object>();
//		for (FavoriteFolders f : favorites) {
//			if (f.getType().equals(("folder"))) {
//				FolderDTO folder = folderRepository.findFolderById(f.getEntityId());
//				fav.add(folder);
//			} else if (f.getType().equals("file")) {
//				FileEntity file = fileRepository.findById(f.getEntityId()).get();
//				fav.add(file);
//			}
//		}
//		map.put("data", fav);
//		return ResponseEntity.ok(map);
//	}
	
	public ResponseEntity<Map<String, Object>> getAllFavorites() {
	    Map<String, Object> response = new HashMap<>();

	    List<FavoriteFolders> favorites = favoriteFolderRepository.findAll();
	    List<Integer> folderIds = new ArrayList<>();
	    List<Integer> fileIds = new ArrayList<>();

	    // Separate folder and file entity IDs
	    for (FavoriteFolders fav : favorites) {
	        if ("folder".equals(fav.getType())) {
	            folderIds.add(fav.getEntityId());
	        } else if ("file".equals(fav.getType())) {
	            fileIds.add(fav.getEntityId());
	        }
	    }

	    // Fetch all folders in a single query
	    List<FolderDTO> folders = folderIds.isEmpty() ? Collections.emptyList() : folderRepository.findFoldersByIds(folderIds);

	    // Fetch all files in a single query
	    List<FileEntity> files = fileIds.isEmpty() ? Collections.emptyList() : fileRepository.findAllById(fileIds);

	    // Combine results
	    List<Object> favoritesList = new ArrayList<>();
	    favoritesList.addAll(folders);
	    favoritesList.addAll(files);

	    response.put("data", favoritesList);
	    return ResponseEntity.ok(response);
	}


}
