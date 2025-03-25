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

import javax.transaction.Transactional;

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
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.repository.FavoriteFolderRepository;
import com.qp.dataCentralize.repository.FileRepository;
import com.qp.dataCentralize.repository.FolderRepository;

@Service
public class FileUploader {

	@Autowired
	FileRepository fileRepository;

	@Autowired
	FolderRepository folderRepository;

	@Autowired
	FavoriteFolderRepository favoriteFolderRepository;

	private static final int SFTP_PORT = 22;
	private static final String SFTP_USER = "dh_gmj3vr";
	private static final String SFTP_PASSWORD = "Srikrishna@0700";
	private static final String SFTP_HOST = "pdx1-shared-a2-03.dreamhost.com";
	private static final String SFTP_DIRECTORY = "/home/dh_gmj3vr/mantramatrix.in/documents/";
	private static final String BASE_URL = "https://mantramatrix.in/documents/";
	private final ExecutorService executorService = Executors.newFixedThreadPool(10);

	public List<FileEntity> handleFileUpload(MultipartFile[] files) {
		List<FileEntity> filelist = new ArrayList<FileEntity>();
		CompletableFuture<Void>[] futures = new CompletableFuture[files.length];
		for (int i = 0; i < files.length; i++) {
			MultipartFile file = files[i];
			try {
				byte[] fileBytes = file.getBytes();
				String originalFilename = file.getOriginalFilename();
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
				new RuntimeException("File Not supported");
			}
		}
		try {
			CompletableFuture.allOf(futures).get();
		} catch (Exception e) {
			new RuntimeException(e.getMessage());
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
			return null;
		} finally {
			if (sftpChannel != null) {
				try {
					sftpChannel.disconnect();
				} catch (Exception e) {
					return null;
				}
			}
			if (session != null) {
				try {
					session.disconnect();
				} catch (Exception e) {
					return null;
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
}
