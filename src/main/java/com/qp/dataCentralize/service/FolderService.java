package com.qp.dataCentralize.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.*;
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
import java.util.stream.Collectors;

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
        System.out.println("user:  "+user);
        String department=user.get("body").get("userDepartment").asText();
        String searchPattern = "(" + department + ")";
        boolean exists = folderRepository.existsByFolderNameAndDepartment(folderName, searchPattern);
        if (exists) {
            response.put("message", "Folder with this name already exists in your department");
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

    public ResponseEntity<Map<String, Object>> createSubFolder(String folderName, JsonNode user, int rootFolderId) {
        Map<String, Object> response = new HashMap<>();

        FolderEntity parentFolder = folderRepository.findById(rootFolderId)
                .orElseThrow(() -> new RuntimeException("Parent folder not found"));

//        FolderEntity folder = folderRepository.findByFolderName(folderName);
//        if (folder != null) {
//            response.put("message", "folder already exists");
//            response.put("code", HttpStatus.BAD_REQUEST.value());
//            response.put("status", "fail");
//            return ResponseEntity.badRequest().body(response);
//        }
        Optional<FolderEntity> existingFolder = folderRepository.findByFolderNameAndParent(folderName, parentFolder);
        if (existingFolder.isPresent()) {
            response.put("message", "Folder already exists under this parent");
            response.put("code", HttpStatus.BAD_REQUEST.value());
            response.put("status", "fail");
            return ResponseEntity.badRequest().body(response);
        }

        FolderEntity subFolder = new FolderEntity();
        subFolder.setFolderName(folderName);
        subFolder.setTime(Instant.now());
        subFolder.setCreatedBy(userService.getCreatedByInfo(user));
        subFolder.setParent(parentFolder); // Set the parent link

        folderRepository.save(subFolder);

        response.put("message", "Subfolder created");
        response.put("code", HttpStatus.OK.value());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

//    public ResponseEntity<Page<FolderEntity>> getAllFolders(String department, int pageNo, int pageSize) {
//        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("time").descending());
//        Page<FolderEntity> obj = folderRepository.findFolderNamesAndIds(department, pageable);
//        return ResponseEntity.ok(obj);
//    }

    public ResponseEntity<Page<FolderDTO>> getAllFolders(String department, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("time").descending());
        String searchPattern = "(" + department + ")";
        Page<FolderDTO> obj = folderRepository.findRootFoldersByDepartment(searchPattern, pageable);
        return ResponseEntity.ok(obj);
    }


//    public ResponseEntity<?> getAllFile(int folderId) {
////        return ResponseEntity.ok(folderRepository.findFolderWithFilesSortedByTime(folderId));
//        FolderEntity folder = folderRepository.findFolderWithFilesAndSubFolders(folderId);
//        System.out.println("folder: "+folder);
//        if (folder == null) {
//            return ResponseEntity.notFound().build();
//        }
//        List<FolderDTO> subFolders = folder.getSubFolder().stream()
//                .map(sub -> new FolderDTO(sub.getEntityId(), sub.getFolderName(), sub.getTime(), sub.getCreatedBy()))
//                .toList();
//
//        FolderWithFilesDTO dto = new FolderWithFilesDTO(
//                folder.getEntityId(),
//                folder.getFolderName(),
//                folder.getTime(),
//                folder.getCreatedBy(),
//                folder.getFiles(),
//                subFolders
//        );
//
//        return ResponseEntity.ok(dto);
//
//    }

public ResponseEntity<?> getAllFile(int folderId) {
    try {
        FolderEntity folder = folderRepository.findFolderWithFiles(folderId); // files fetched
        if (folder == null) {
            return ResponseEntity.notFound().build();
        }
        System.out.println("service");
        List<FolderEntity> subFolders = folderRepository.findSubFolders(folderId); // subfolders fetched separately

        List<FileEntity> sortedFiles = folder.getFiles().stream()
                .sorted(Comparator.comparing(FileEntity::getTime).reversed())
                .collect(Collectors.toList());

        List<FolderDTO> sortedSubFolders = subFolders.stream()
                .sorted(Comparator.comparing(FolderEntity::getTime).reversed())
                .map(sub -> new FolderDTO(sub.getEntityId(), sub.getFolderName(), sub.getTime(), sub.getCreatedBy()))
                .collect(Collectors.toList());

        FolderWithFilesDTO dto = new FolderWithFilesDTO(
                folder.getEntityId(),
                folder.getFolderName(),
                folder.getTime(),
                folder.getCreatedBy(),
                sortedFiles,
                sortedSubFolders
        );
        System.out.println(dto);
        return ResponseEntity.ok(dto);
    }catch (Exception e){
        e.printStackTrace();
        return  null;
    }
}


    public ResponseEntity<Map<String, Object>> handleLargeFile(MultipartFile[] files, int folderId, JsonNode user) {
        Map<String, Object> map = new HashMap<String, Object>();
        ResponseEntity<Map<String, Object>> response = isExists(files, folderId, user);
        Object res = response.getBody().get("exists");
        if (res instanceof Map<?, ?> && !((Map<?, ?>) res).isEmpty()) {
            map.put("exists", res);
            return ResponseEntity.ok(map);
        }
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

    public ResponseEntity<Map<String, Object>> isExists(MultipartFile[] files, int folderId, JsonNode user) {
        Map<String, Object> map = new HashMap<String, Object>();
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        Map<Object, Object> exists = new HashMap<>();
        for (MultipartFile file : files) {
            FileEntity existfile = fileRepository.findFileInFolderByFileName(folderId, file.getOriginalFilename()).orElse(null);

            if (existfile != null) {
                exists.put(existfile.getId(), existfile);
            }
        }
        map.put("exists", exists);
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<Map<String, Object>> handleFile(MultipartFile[] files, int folderId, JsonNode user) {
        Map<String, Object> map = new HashMap<String, Object>();
        ResponseEntity<Map<String, Object>> response = isExists(files, folderId, user);
        Object res = response.getBody().get("exists");

        if (res instanceof Map<?, ?> && !((Map<?, ?>) res).isEmpty()) {
            map.put("exists", res);
            return ResponseEntity.ok(map);
        }
        FolderEntity folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        List<FileEntity> fileList = fileUploader.handleFileUpload(files);
        String createdBy = userService.getCreatedByInfo(user);
        for (FileEntity file1 : fileList) {
            file1.setCreatedBy(createdBy);
            saveFileToFolder(folder, file1);
        }
        map.put("data", fileList);
        map.put("message", "file uploaded successfully");
        map.put("code", HttpStatus.OK);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    private void saveFileToFolder(FolderEntity folder, FileEntity fileEntity) {
        List<FileEntity> files = folder.getFiles();
        if (files == null || files.isEmpty()) {
            files = new ArrayList<>();
            files.add(fileEntity);
        } else {
            files.add(fileEntity);
        }
        folder.setTime(Instant.now());
        folder.setFiles(files);
        folderRepository.save(folder);
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
        FavoriteFolders fav = favoriteFolderRepository.findByEntityIdAndType(entityId, type);
        if (fav != null) {
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
        ResponseEntity<Map<String, Object>> securityCheck = accountSecurityService.verifyDepartmentAccess(res, List.of(Department.FINANCE_AND_ACCOUNTS));
        if (securityCheck.getStatusCode() != HttpStatus.OK) {
            return securityCheck;
        }

        Map<String, Object> map = new HashMap<>();
        try {
            // Fetch folders for Finance and Accounts department with pagination
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("time").descending());

            // Wrap department with brackets
            String department = "Finance and Account";
            String searchPattern = "(" + department + ")";
            Page<FolderDTO> folders = folderRepository.findFolderNamesAndIds(searchPattern, pageable);

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

    public ResponseEntity<Map<String, Object>> replaceFile(int folderId, JsonNode user, List<String> files, MultipartFile[] replacefiles) {
        Map<String, Object> map = new HashMap<>();
        for (String id:files) {
            FileEntity fileEntity = fileRepository.findById(Integer.parseInt(id)).get();
            userService.deleteFile(folderId, fileEntity);
        }
        return handleLargeFile(replacefiles, folderId, user);
    }


    public ResponseEntity<Map<String, Object>> rename(int folderId, String foldername) {
        Map<String, Object> map = new HashMap<>();
        FolderEntity folder = folderRepository.findById(folderId).get();
        folder.setFolderName(foldername);
        folderRepository.save(folder);
        map.put("message", "rename successful");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<Map<String, Object>> renameFile(int fileId, String filename) {
        Map<String, Object> map = new HashMap<>();
        FileEntity file = fileRepository.findById(fileId).get();
        String[] arr = file.getFileName().split("\\.");
        file.setFileName(filename + "." + arr[1]);
        fileRepository.save(file);
        map.put("message", "rename successful");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

//    public  Map<String, Object> search(String searchText) {
//        List<FileDTO> files = fileRepository.searchFiles(searchText);
//        List<FolderDTO> folders = folderRepository.searchFolders(searchText);
//        Map<String, Object> map=new HashMap<>();
//        map.put("folders",folders);
//        map.put("files",files);
//        return map;
//    }

    public Map<String, Object> search(String searchText) {
        List<FileDTO> files = fileRepository.searchFiles(searchText);
        List<FolderDTO> folders = folderRepository.searchFolders(searchText);

        // Group files by department
        Map<String, List<FileDTO>> filesGroupedByDepartment = files.stream()
                .collect(Collectors.groupingBy(file -> extractDepartment(file.getCreatedBy())));

        // Group folders by department
        Map<String, List<FolderDTO>> foldersGroupedByDepartment = folders.stream()
                .collect(Collectors.groupingBy(folder -> extractDepartment(folder.getCreatedBy())));

        // Prepare final response
        Map<String, Object> result = new HashMap<>();
        result.put("files", filesGroupedByDepartment);
        result.put("folders", foldersGroupedByDepartment);

        return result;
    }

    private String extractDepartment(String createdBy) {
        if (createdBy != null && createdBy.contains("(") && createdBy.contains(")")) {
            int start = createdBy.indexOf('(') + 1;
            int end = createdBy.indexOf(')');
            return createdBy.substring(start, end).trim();
        }
        return "Unknown";
    }


    public List<String> getParentFoldersForFile(String fileName) {
        FileEntity file = fileRepository.findByFileName(fileName);
        if (file == null) return List.of();
        FolderEntity folder = folderRepository.findFolderByFileId(file.getId()).orElse(null);
        System.out.println(folder.getFolderName());
        List<String> path = new ArrayList<>();
        while (folder != null) {
            path.add(folder.getFolderName());
            folder = folder.getParent() != null
                    ? folderRepository.findById(folder.getParent().getEntityId()).orElse(null)
                    : null;
        }

        Collections.reverse(path);
        return path;
    }
}
