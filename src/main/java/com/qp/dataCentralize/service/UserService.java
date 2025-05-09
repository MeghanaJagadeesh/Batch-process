package com.qp.dataCentralize.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.FavoriteFolders;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.FolderEntity;
import com.qp.dataCentralize.entity.LeadsData;
import com.qp.dataCentralize.helper.FileUploader;
import com.qp.dataCentralize.helper.Security;
import com.qp.dataCentralize.repository.DatasRepo;
import com.qp.dataCentralize.repository.FavoriteFolderRepository;
import com.qp.dataCentralize.repository.FolderRepository;
import com.qp.dataCentralize.repository.LeadsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    FileUploader fileUploader;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    DatasRepo datasRepo;

    @Autowired
    FavoriteFolderRepository favoriteFolderRepository;

    @Autowired
    LeadsRepository leadsRepository;

    public JsonNode getAllEmployee() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange("http://147.93.28.177:4040/admin/fetchallemployee",
                HttpMethod.POST, requestEntity, JsonNode.class);
        return response.getBody();
    }

    public JsonNode getEmployeeByEmail(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                "http://147.93.28.177:4040/emp/fetchemployeebyemail?email=" + email, HttpMethod.GET, requestEntity,
                JsonNode.class);
        return response.getBody();
    }

    public ResponseEntity<Map<String, Object>> login(String email, String password) {
        JsonNode euser = null;
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode responseBody = getEmployeeByEmail(email);
        if (responseBody != null && responseBody.has("status") && responseBody.get("status").asBoolean()) {
            JsonNode user = responseBody.get("body");
            if (user != null && user.get("userEmail").asText().equals(email)
                    && user.get("userPassword").asText().equals(password)) {
                euser = user;
            }
            if (euser == null) {
                map.put("message", "Incorrect Password");
                map.put("code", HttpStatus.BAD_REQUEST.value());
                map.put("status", "fail");
                return ResponseEntity.badRequest().body(map);
            }
            map.put("message", "Login Successful");
            map.put("code", 200);
            map.put("status", "success");
            map.put("data", euser);
            map.put("token", Security.encrypt(euser.get("userEmail") + "", "123"));
            return ResponseEntity.ok(map);
        } else {
            map.put("message", "Inavild email and password");
            map.put("code", HttpStatus.BAD_REQUEST.value());
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
    }

    public JsonNode validateToken(String token) {
        token = token.substring(7);
        String email = Security.decrypt(token, "123");
        if (email == null)
            return null;
        email = email.replaceAll("\"", "");
        return getEmployeeByEmail(email);
    }

    public String getCreatedByInfo(JsonNode response) {
        JsonNode user = response.get("body");
        String userName = user.get("userName").asText();
        String userDepartment = user.get("userDepartment").asText();
        return userName + "(" + userDepartment + ")";
    }

    public ResponseEntity<Map<String, Object>> deleteFile(int folderId, FileEntity fileEntity) {
        return fileUploader.deleteFile(folderId, fileEntity);
    }

    public ResponseEntity<Map<String, Object>> deleteFolder(int folderId) {
        FavoriteFolders fav = favoriteFolderRepository.findByEntityIdAndType(folderId, "folder");
        if (fav != null) {
            favoriteFolderRepository.delete(fav);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        FolderEntity folder = folderRepository.findById(folderId).get();
        List<FileEntity> files = new ArrayList<>(folder.getFiles());
        for (FileEntity fileEntity : files) {
            fileUploader.deleteFile(folderId, fileEntity);
        }
        folderRepository.delete(folder);
        map.put("message", "folder deleted successfully");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<Map<String, Object>> deletedata(int id) {
        return null;
    }

    public ResponseEntity<?> addLeads(JsonNode user, LeadsData leads) {
        Map<String, Object> map = new HashMap<>();
        leads.setEnteredBy(getCreatedByInfo(user));
        leads.setEntryDate(Instant.now() + "");
        leadsRepository.save(leads);
        map.put("message", "leads saved successfully");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    public Page<LeadsData> getAllLeads(JsonNode user, int pagesize, int pageno) {
        Pageable pageable = PageRequest.of(pageno, pagesize);
        return leadsRepository.findAllByOrderByEntryDateDesc(pageable);
    }

    public ResponseEntity<?> modifyStatus(JsonNode user, int id, String status) {
        LeadsData leads = leadsRepository.findById(id).get();
        leads.setStatus(status);
        leadsRepository.save(leads);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "modified successfully");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<?> modifyNote(JsonNode user, int id, String note) {
        LeadsData leads = leadsRepository.findById(id).get();
        leads.setNote(note);
        leadsRepository.save(leads);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "modified successfully");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }

    public Page<LeadsData> fetchByStatus(JsonNode user, String status, int pageno, int pagesize) {
        Pageable pageable = PageRequest.of(pageno, pagesize);
        return leadsRepository.findAllByStatusOrderByEntryDateDesc(status, pageable);
    }

    public Page<LeadsData> search(String searchText, int pageno, int pagesize) {
        Specification<LeadsData> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("person_name")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("phone_number")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("company_name")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("address")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("status")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("note")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("entryDate")),
                    "%" + searchText.toLowerCase() + "%"));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("enteredBy")),
                    "%" + searchText.toLowerCase() + "%"));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageno, pagesize);
        return leadsRepository.findAll(spec, pageable);
    }

    public ResponseEntity<Map<String, Object>> deleteLead(int id) {
        leadsRepository.deleteById(id);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "deleted successfully");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }
}
