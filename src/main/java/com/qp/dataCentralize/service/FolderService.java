package com.qp.dataCentralize.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderDTO;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.helper.FileUploader;
import com.qp.dataCentralize.helper.UploadLargeFile;
import com.qp.dataCentralize.repository.FavoriteFolderRepository;
import com.qp.dataCentralize.repository.FileRepository;
import com.qp.dataCentralize.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

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

    @Autowired
    AccountSecurityService accountSecurityService;

    public ResponseEntity<Map<String, Object>> saveFolder(String folderName, JsonNode user) {

        Map<String, Object> response = new HashMap<String, Object>();
        FolderEntity folder = folderRepository.findByFolderName(folderName);
        if (folder != null) {
            response.put("message", "folder already exists");
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

    public ResponseEntity<Page<FolderEntity>> getAllFolders(String department, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("time").descending());
        Page<FolderEntity> obj = folderRepository.findFolderNamesAndIds(department, pageable);
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
        System.out.println("save file " + fileEntity);
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
        FavoriteFolders exist = favoriteFolderRepository.findByEntityIdAndType(favoriteFolders.getEntityId(),
                favoriteFolders.getType());
        if (exist == null)
            favoriteFolderRepository.save(favoriteFolders);

        Map<String, Object> map = new HashMap<String, Object>();

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

        for (FavoriteFolders fav : favorites) {
            if ("folder".equals(fav.getType())) {
                folderIds.add(fav.getEntityId());
            } else if ("file".equals(fav.getType())) {
                fileIds.add(fav.getEntityId());
            }
        }

        // Fetch all folders in a single query
        List<FolderDTO> folders = folderIds.isEmpty() ? Collections.emptyList()
                : folderRepository.findFoldersByIds(folderIds);

        // Fetch all files in a single query
        List<FileEntity> files = fileIds.isEmpty() ? Collections.emptyList() : fileRepository.findAllById(fileIds);

        // Combine results
        List<Object> favoritesList = new ArrayList<>();
        favoritesList.addAll(folders);
        favoritesList.addAll(files);

        response.put("data", favoritesList);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> unStarFavorites(int entityId, String type) {
        System.out.println(type + " " + entityId);
        FavoriteFolders fav = favoriteFolderRepository.findByEntityIdAndType(entityId, type);
        System.out.println(fav);
        if (fav != null) {
            System.out.println(fav);
            favoriteFolderRepository.delete(fav);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", "Unstar");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<Map<String, Object>> getAccountsDashboard(JsonNode res, int pageNo, int pageSize) {
        // First verify if the user is from accounts team and has valid 2FA
        ResponseEntity<Map<String, Object>> securityCheck = accountSecurityService.verifyAccountTeamAccess(res);
        if (securityCheck.getStatusCode() != HttpStatus.OK) {
            return securityCheck;
        }

        Map<String, Object> map = new HashMap<>();
        try {
            // Fetch folders for Finance and Accounts department with pagination
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("time").descending());
            Page<FolderEntity> folders = folderRepository.findFolderNamesAndIds("Finance and Accounts", pageable);
            
            map.put("message", "Access granted to accounts dashboard");
            map.put("code", HttpStatus.OK.value());
            map.put("status", "success");
            map.put("data", folders.getContent());
            map.put("totalElements", folders.getTotalElements());
            map.put("totalPages", folders.getTotalPages());
            map.put("currentPage", folders.getNumber());
            map.put("pageSize", folders.getSize());
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            map.put("message", "Error fetching dashboard data: " + e.getMessage());
            map.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            map.put("status", "fail");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }
}
