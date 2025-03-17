package com.qp.dataCentralize.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.repository.FileRepository;
import com.qp.dataCentralize.repository.FolderRepository;

@Service
public class FileUploader {

    @Autowired
    FileRepository fileRepository;
    
    @Autowired
    FolderRepository folderRepository;

    private static final int SFTP_PORT = 22;
    private static final String SFTP_USER = "dh_gmj3vr";
    private static final String SFTP_PASSWORD = "Srikrishna@0700";
    private static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
    private static final String SFTP_DIRECTORY = "/home/dh_gmj3vr/mantramatrix.in/documents/";
    private static final String BASE_URL = "https://mantramatrix.in/documents/";
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public ResponseEntity<Map<String, Object>> handleFileUpload(MultipartFile[] files, int folderId) {
    	FolderEntity folder = folderRepository.findById(folderId).get();
    	List<FileEntity> fileEntities=new ArrayList<FileEntity>();
    	Map<String, Object> response =new HashMap<String, Object>();
    	if(folder==null) {
    		response.put("message", "folder not exists");
			response.put("code", HttpStatus.BAD_REQUEST.value());
			response.put("status", "fail");
			return ResponseEntity.badRequest().body(response);
    	}
        
        CompletableFuture<Void>[] futures = new CompletableFuture[files.length];
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                byte[] fileBytes = file.getBytes();
                String originalFilename = file.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(originalFilename);

                // Submit upload task and store the future
                futures[i] = CompletableFuture.runAsync(() -> {
                    String fileUrl = uploadFileViaSFTP(fileBytes, uniqueFileName);
                    if (fileUrl != null) {
                        FileEntity fileEntity = new FileEntity();
                        fileEntity.setFileLink(fileUrl);
                        fileEntity.setFileName(file.getOriginalFilename());
                        fileEntity.setType(file.getContentType());
                        fileEntity.setFileSize(file.getSize()+"");
                       fileEntities.add(fileEntity);
                       fileEntity.setTime(Instant.now());
                        System.out.println("fileUrl : " + fileUrl);
                    }
                }, executorService);
            } catch (IOException e) {
                e.printStackTrace();
                response.put("message", "Internal Server Error");
                response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
                response.put("status", "fail");
                return ResponseEntity.internalServerError().body(response);
            }
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Internal Server Error");
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("status", "fail");
            return ResponseEntity.internalServerError().body(response);
        }
		folder.setFiles(fileEntities);
		folderRepository.save(folder);
        response.put("message", "Files uploaded successfully");
        response.put("code", HttpStatus.OK.value());
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    private String uploadFileViaSFTP(byte[] fileBytes, String fileName) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp sftpChannel = null;

        try {
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            try (InputStream fileInputStream = new ByteArrayInputStream(fileBytes)) {
                sftpChannel.put(fileInputStream, SFTP_DIRECTORY + fileName);
            }
            return BASE_URL + fileName;
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
            System.out.println("Error uploading file: " + e.getMessage());
            return null;
        } finally {
            if (sftpChannel != null) {
                try {
                    sftpChannel.disconnect();
                } catch (Exception e) {
                    System.out.println("Error disconnecting SFTP channel: " + e.getMessage());
                }
            }
            if (session != null) {
                try {
                    session.disconnect();
                } catch (Exception e) {
                    System.out.println("Error disconnecting SFTP session: " + e.getMessage());
                }
            }
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        return UUID.randomUUID().toString() + extension;
    }
}
