package com.qp.dataCentralize.helper;

import com.jcraft.jsch.*;
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.repository.FavoriteFolderRepository;
import com.qp.dataCentralize.repository.FileRepository;
import com.qp.dataCentralize.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FileUploader {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    FavoriteFolderRepository favoriteFolderRepository;

    //    local
//    public static final int SFTP_PORT = 22;
//    public static final String SFTP_USER = "dh_nw536f";
//    public static final String SFTP_PASSWORD = "Srikrishna@0700";
//    public static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
//    public static final String SFTP_DIRECTORY = "/home/dh_nw536f/aws.quantumparadigm.in/documents/";
//    public static final String BASE_URL = "https://aws.quantumparadigm.in/documents/";

    //    global
    public static final int SFTP_PORT = 22;
    public static final String SFTP_USER = "dh_gmj3vr";
    public static final String SFTP_PASSWORD = "Srikrishna@0700";
    public static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
    public static final String SFTP_DIRECTORY = "/home/dh_gmj3vr/mantramatrix.in/documents/";
    public static final String BASE_URL = "https://mantramatrix.in/documents/";

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public List<FileEntity> handleFileUpload(MultipartFile[] files) {
        List<FileEntity> filelist = new ArrayList<FileEntity>();
        CompletableFuture<Void>[] futures = new CompletableFuture[files.length];
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            try {
                byte[] fileBytes = file.getBytes();
                String originalFilename = file.getOriginalFilename();
                ;
                assert originalFilename != null;
                String uniqueFileName = generateUniqueFileName(originalFilename);

                futures[i] = CompletableFuture.runAsync(() -> {
                    String fileUrl = uploadFileViaSFTP(fileBytes, uniqueFileName);
                    if (fileUrl != null) {
                        FileEntity fileEntity = new FileEntity();
                        fileEntity.setFileLink(fileUrl);
                        fileEntity.setFileName(file.getOriginalFilename());
                        fileEntity.setType(file.getContentType());
                        fileEntity.setFileSize(file.getSize() + "");
                        fileEntity.setTime(Instant.now());
                        filelist.add(fileEntity);
                    }
                }, executorService);
            } catch (IOException e) {
                throw new RuntimeException("File Not supported");
            }
        }
        try {
            CompletableFuture.allOf(futures).get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return filelist;
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
            return null;
        } finally {
            if (sftpChannel != null) {
                try {
                    sftpChannel.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            if (session != null) {
                try {
                    session.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6)+"_"+originalFilename ;
    }

    public ResponseEntity<Map<String, Object>> deleteFile(int folderId, FileEntity fileEntity) {
        Map<String, Object> response = new HashMap<>();
        String fileName = fileEntity.getFileLink().replace(BASE_URL, "");
        String remoteFilePath = SFTP_DIRECTORY + fileName;
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

            sftpChannel.rm(remoteFilePath);
            deleteFileFromFolder(folderId, fileEntity.getId());
            
            response.put("code", 200);
            response.put("status", "success");
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);

        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            String errorMsg = "Error deleting file: " + e.getMessage();
            response.put("code", 400);
            response.put("status", "error");
            response.put("message", errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    @Transactional
    public void deleteFileFromFolder(int folderId, int fileId) {
        System.out.println(5);
        FavoriteFolders fav = favoriteFolderRepository.findByEntityIdAndType(fileId, "file");
        if (fav != null) {
            favoriteFolderRepository.delete(fav);
        }
        FolderEntity folder = folderRepository.findById(folderId).get();
        List<FileEntity> files = folder.getFiles();
        files.removeIf(file -> file.getId() == fileId);
        folderRepository.save(folder);
        fileRepository.deleteById(fileId);
    }

    public ResponseEntity<Map<String, Object>> deleteUrl(String fileurl) {
        Map<String, Object> response = new HashMap<>();
        String fileName = fileurl.replace(BASE_URL, "");
        String remoteFilePath = SFTP_DIRECTORY + fileName;
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

            sftpChannel.rm(remoteFilePath);

            response.put("code", 200);
            response.put("status", "success");
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);

        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            String errorMsg = "Error deleting file: " + e.getMessage();
            response.put("code", 400);
            response.put("status", "error");
            response.put("message", errorMsg);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private static final List<String> VALID_EXTENSIONS = Arrays.asList(
            ".pdf", ".xls", ".xlsx", ".doc", ".docx", ".png", ".jpg", ".jpeg", ".svg", ".cdr",
            ".fbx", ".ai", ".psd", ".eps", ".tiff", ".gif", ".mp4", ".mov", ".mp3", ".wav",
            ".zip", ".rar", ".7z", ".proj", ".pr", ".ppt", ".pptx"
    );

    public void cleanFilesByDate(LocalDate targetDate) {
        Instant startOfDay = targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<FileEntity> files = fileRepository.findAllByTimeBetween(startOfDay, endOfDay);

        for (FileEntity file : files) {
            String cleanedName = clean(file.getFileName());
            String cleanedLink = clean(file.getFileLink());

            if (cleanedName != null && cleanedLink != null) {
                file.setFileName(cleanedName);
                file.setFileLink(cleanedLink);
                fileRepository.save(file);
            }
//            } else {
//                // Optionally: remove junk files
//                fileRepository.delete(file);
//            }
        }
    }

    private String clean(String input) {
        if (input == null) return null;

        for (String ext : VALID_EXTENSIONS) {
            int index = input.toLowerCase().indexOf(ext);
            if (index != -1) {
                return input.substring(0, index + ext.length());
            }
        }
        return null;
    }

    public void renameInvalidFilesByDate(String targetDate) {
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

            Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(SFTP_DIRECTORY);

            for (ChannelSftp.LsEntry entry : files) {
                String fileName = entry.getFilename();
                SftpATTRS attrs = entry.getAttrs();

                // Filter directories and system files
                if (attrs.isDir() || fileName.startsWith(".")) continue;

                // Convert file modified time to LocalDate
                Instant fileTime = Instant.ofEpochSecond(attrs.getMTime());
                LocalDate fileDate = fileTime.atZone(ZoneId.systemDefault()).toLocalDate();

                if (!fileDate.toString().equals(targetDate)) continue;

                // Check if filename already ends correctly
                Optional<String> validExtension = VALID_EXTENSIONS.stream()
                        .filter(ext -> fileName.toLowerCase().contains(ext))
                        .findFirst();

                if (validExtension.isPresent()) {
                    String ext = validExtension.get();
                    int extIndex = fileName.toLowerCase().indexOf(ext);
                    int correctEndIndex = extIndex + ext.length();

                    // If the file has extra text after extension
                    if (correctEndIndex < fileName.length()) {
                        String cleanedFileName = fileName.substring(0, correctEndIndex);
                        String oldPath = SFTP_DIRECTORY + fileName;
                        String newPath = SFTP_DIRECTORY + cleanedFileName;

                        System.out.println("Renaming: " + fileName + " ➤ " + cleanedFileName);
                        sftpChannel.rename(oldPath, newPath);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }


}
