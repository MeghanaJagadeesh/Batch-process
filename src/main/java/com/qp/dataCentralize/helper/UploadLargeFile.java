package com.qp.dataCentralize.helper;

import com.jcraft.jsch.*;
import com.qp.dataCentralize.entity.FileEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

@Service
public class UploadLargeFile {

    @Value("${server.sftp_port}")
    int SFTP_PORT;

    @Value("${server.sftp_user}")
    String SFTP_USER;

    @Value("${server.sftp_password}")
    String SFTP_PASSWORD;

    @Value("${server.sftp_host}")
    String SFTP_HOST;

    @Value("${server.sftp_directory}")
    String SFTP_DIRECTORY;

    @Value("${server.baseurl}")
    String BASE_URL;

    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    public List<FileEntity> handleFileUpload(MultipartFile[] files) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<FileEntity> filelist = new ArrayList<FileEntity>();
        for (MultipartFile file : files) {
            futures.add(CompletableFuture.runAsync(() -> {
                try (InputStream inputStream = file.getInputStream()) {
                    String uniqueName = generateUniqueFileName(file.getOriginalFilename());
                    String fileUrl = uploadFileViaSFTP(inputStream, uniqueName, file.getSize());

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

    private String uploadFileViaSFTP(InputStream inputStream, String fileName,long totalSize) {
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

            byte[] buffer = new byte[BUFFER_SIZE];
            try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                OutputStream os = sftpChannel.put(SFTP_DIRECTORY + fileName, ChannelSftp.OVERWRITE);
                int bytesRead;
                long totalRead = 0;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);

                    int progress = (int) ((totalRead * 100) / totalSize);
                    System.out.println("Uploading " + fileName + ": " + progress + "%");
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
        return originalFilename+UUID.randomUUID() ;
    }
}
