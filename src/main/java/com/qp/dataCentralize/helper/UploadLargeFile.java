package com.qp.dataCentralize.helper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.qp.dataCentralize.entity.FileEntity;

@Service
public class UploadLargeFile {
//global
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USER = "dh_gmj3vr";
    private static final String SFTP_PASSWORD = "Srikrishna@0700";
    private static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
    private static final String SFTP_DIRECTORY = "/home/dh_gmj3vr/mantramatrix.in/documents/";
    private static final String BASE_URL = "https://mantramatrix.in/documents/";


    //    local
//    private final int SFTP_PORT = 22;
//    private final String SFTP_USER = "dh_nw536f";
//    private final String SFTP_PASSWORD = "Srikrishna@0700";
//    private final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
//    private final String SFTP_DIRECTORY = "/home/dh_nw536f/aws.quantumparadigm.in/documents/";
//    private final String BASE_URL = "https://aws.quantumparadigm.in/documents/";

    // Increased buffer size (1MB)
    private static final int BUFFER_SIZE = 10* 1024 * 1024;

    // Thread pool for parallel uploads
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    public List<FileEntity> handleFileUpload(MultipartFile[] files) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<FileEntity> filelist=new ArrayList<FileEntity>();
        for (MultipartFile file : files) {
            futures.add(CompletableFuture.runAsync(() -> {
            	try (InputStream inputStream = file.getInputStream()) {
                    String uniqueName = generateUniqueFileName(file.getOriginalFilename());
                    String fileUrl = uploadFileViaSFTP(inputStream, uniqueName);

                    if (fileUrl != null) {

						FileEntity fileEntity = new FileEntity();
						fileEntity.setFileLink(fileUrl);
						fileEntity.setFileName(file.getOriginalFilename());
						fileEntity.setType(file.getContentType());
						fileEntity.setFileSize(file.getSize() + "");
						fileEntity.setTime(Instant.now());
						filelist.add(fileEntity);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
                }
            }, executorService));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            
        } catch (Exception e) {
        	new RuntimeException(e.getMessage());
        }
        return filelist;
    }

    private String uploadFileViaSFTP(InputStream inputStream, String fileName) {
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

            // Use a larger buffer size for faster transfers
            byte[] buffer = new byte[BUFFER_SIZE];
            try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                OutputStream os = sftpChannel.put(SFTP_DIRECTORY + fileName, ChannelSftp.OVERWRITE);
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
                return BASE_URL + fileName;
            }
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (sftpChannel != null) sftpChannel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        String ext = originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        return UUID.randomUUID() + ext;
    }
}

//package com.qp.dataCentralize.service;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.BlockingDeque;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.TimeUnit;
//
//import javax.annotation.PreDestroy;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.jcraft.jsch.ChannelSftp;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.JSchException;
//import com.jcraft.jsch.Session;
//import com.jcraft.jsch.SftpProgressMonitor;
//import com.qp.dataCentralize.entity.FileEntity;
//import com.qp.dataCentralize.entity.FolderEntity;
//import com.qp.dataCentralize.repository.FileRepository;
//import com.qp.dataCentralize.repository.FolderRepository;
//
//public class UploadLargeFile {
//
//	@Autowired
//    private FileRepository fileRepository;
//    
//    @Autowired
//    private FolderRepository folderRepository;
//
//    private static final int SFTP_PORT = 22;
//    private static final String SFTP_USER = "dh_gmj3vr";
//    private static final String SFTP_PASSWORD = "Srikrishna@0700";
//    private static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
//    private static final String SFTP_DIRECTORY = "/home/dh_gmj3vr/mantramatrix.in/documents/";
//    private static final String BASE_URL = "https://mantramatrix.in/documents/";
//
//    // Chunk size (10MB)
//    private static final int CHUNK_SIZE = 10 * 1024 * 1024;
//
//    // Thread pools
//    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(8);
//    private final ExecutorService chunkExecutor = Executors.newFixedThreadPool(4);
//
//    // Connection pool
//    private final BlockingDeque<ChannelSftp> sftpPool = new LinkedBlockingDeque<>(10);
//
//    public ResponseEntity<Map<String, Object>> handleLargeFileUpload(MultipartFile[] files, int folderId) {
//        FolderEntity folder = folderRepository.findById(folderId)
//            .orElseThrow(() -> new RuntimeException("Folder not found"));
//        
//        Map<String, Object> response = new HashMap<>();
//        List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();
//
//        try {
//            for (MultipartFile file : files) {
//                String originalName = file.getOriginalFilename();
//                String uniqueName = generateUniqueFileName(originalName);
//                String targetPath = SFTP_DIRECTORY + uniqueName;
//                
//                uploadFutures.add(processFileUpload(file, targetPath, uniqueName));
//            }
//
//            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0]))
//                .get(2, TimeUnit.HOURS); // Timeout after 2 hours
//
//            response.put("status", "success");
//            response.put("message", "Files uploaded successfully");
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("status", "error");
//            response.put("message", "Upload failed: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//    private CompletableFuture<Void> processFileUpload(MultipartFile file, String targetPath, String uniqueName) {
//        return CompletableFuture.runAsync(() -> {
//            try (InputStream inputStream = file.getInputStream()) {
//                ChannelSftp sftpChannel = getSftpChannel();
//                sftpChannel.put(inputStream, targetPath, new MonitoringProgress(inputStream.available()));
//
//                returnChannel(sftpChannel);
//                saveFileMetadata(file, uniqueName);
//                
//            } catch (Exception e) {
//                throw new RuntimeException("File upload failed: " + e.getMessage(), e);
//            }
//        }, uploadExecutor);
//    }
//
//    private void saveFileMetadata(MultipartFile file, String uniqueName) {
//        FileEntity entity = new FileEntity();
//        entity.setFileLink(BASE_URL + uniqueName);
//        entity.setFileName(file.getOriginalFilename());
//        entity.setType(file.getContentType());
////        entity.setFileSize(file.getSize());
//        fileRepository.save(entity);
//    }
//
//    private ChannelSftp getSftpChannel() throws JSchException {
//        ChannelSftp channel = sftpPool.poll();
//        if (channel != null && channel.isConnected()) {
//            return channel;
//        }
//        return createNewChannel();
//    }
//
//    private synchronized ChannelSftp createNewChannel() throws JSchException {
//        JSch jsch = new JSch();
//        Session session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
//        session.setPassword(SFTP_PASSWORD);
//        session.setConfig("StrictHostKeyChecking", "no");
//        session.connect();
//        
//        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
//        channel.connect();
//        return channel;
//    }
//
//    private void returnChannel(ChannelSftp channel) {
//        if (channel != null && channel.isConnected()) {
//            sftpPool.offer(channel);
//        }
//    }
//
//    private static class MonitoringProgress implements SftpProgressMonitor {
//        private final long totalSize;
//        private long uploadedSize = 0;
//
//        public MonitoringProgress(long totalSize) {
//            this.totalSize = totalSize;
//        }
//
//        @Override
//        public void init(int op, String src, String dest, long max) {
//            System.out.println("Starting upload: " + dest);
//        }
//
//        @Override
//        public boolean count(long count) {
//            uploadedSize += count;
//            double progress = (uploadedSize * 100.0) / totalSize;
//            System.out.printf("Progress: %.2f%%%n", progress);
//            return true;
//        }
//
//        @Override
//        public void end() {
//            System.out.println("Upload completed.");
//        }
//    }
//
//    private String generateUniqueFileName(String originalFilename) {
//        String ext = originalFilename.contains(".") 
//            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
//            : "";
//        return UUID.randomUUID() + ext;
//    }
//
//    @PreDestroy
//    public void cleanup() {
//        uploadExecutor.shutdown();
//        chunkExecutor.shutdown();
//        sftpPool.forEach(channel -> {
//            try {
//                channel.disconnect();
//                channel.getSession().disconnect();
//            } catch (JSchException e) {
//                // Log disconnect error
//            }
//        });
//    }
//}
//
//