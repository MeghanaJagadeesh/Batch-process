package com.qp.dataCentralize.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.FileEntity;
import com.qp.dataCentralize.entity.LeadsData;
import com.qp.dataCentralize.service.AccountSecurityService;
import com.qp.dataCentralize.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/planotech-inhouse")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AccountSecurityService accountSecurityService;

   @PostMapping("user/login")
    public ResponseEntity<Map<String, Object>> login(@RequestParam String email, @RequestParam String password) {
        return userService.login(email, password);
    }

    @DeleteMapping("admin/delete/file")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam int folderId,
                                                          @RequestBody FileEntity fileEntity, @RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        JsonNode response = user.get("body");
        String dept = response.get("userDepartment").asText();
        if (dept.equals("Admin")) {
            return userService.deleteFile(folderId, fileEntity);
        } else {
            map.put("message", "Employess Restricted, Admin use only");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
    }

    @DeleteMapping("admin/delete/folder/{folderId}")
    public ResponseEntity<Map<String, Object>> deleteFolder(@PathVariable int folderId,
                                                            @RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<String, Object>();

        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        System.out.println(user);
        JsonNode response = user.get("body");
        String dept = response.get("userDepartment").asText();
        if (dept.equals("Admin")) {

            return userService.deleteFolder(folderId);
        } else {
            map.put("message", "Employess Restricted, Admin use only");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
    }

    @DeleteMapping("admin/delete/data/{id}")
    public ResponseEntity<Map<String, Object>> deletedata(@PathVariable int id,
                                                          @RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        System.out.println(user);
        JsonNode response = user.get("body");
        String dept = response.get("userDepartment").asText();
        if (dept.equals("Admin")) {
            return userService.deletedata(id);
        } else {
            map.put("message", "Employess Restricted, Admin use only");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
    }

    @GetMapping("/get/profileInfo")
    public Object getProfileInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        String email = user.get("body").get("userEmail").asText();
        user = userService.getEmployeeByEmail(email);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("/add/leads")
    public ResponseEntity<?> addLeads(@RequestHeader("Authorization") String token, @RequestBody LeadsData leads) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return userService.addLeads(user, leads);
    }

    @GetMapping("/get/leads")
    public Object getLeads(@RequestHeader("Authorization") String token, @RequestParam(defaultValue = "20") int pageSize, @RequestParam(defaultValue = "0") int pageNo) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return userService.getAllLeads(user, pageSize, pageNo);
    }

    @PostMapping("/modify/lead/status/{id}")
    public ResponseEntity<?> modifyStatus(@PathVariable int id, @RequestHeader("Authorization") String token,
                                          @RequestParam(name = "status") String status) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return userService.modifyStatus(user, id, status);
    }

    @PostMapping("/modify/lead/note/{id}")
    public ResponseEntity<?> modifyNote(@PathVariable int id, @RequestHeader("Authorization") String token,
                                        @RequestParam(name = "note") String note) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return userService.modifyNote(user, id, note);
    }

    @GetMapping("/fetchby/status/{status}")
    public Object fetchBystatus(@PathVariable String status, @RequestHeader("Authorization") String token, @RequestParam(defaultValue = "20") int pagesize, @RequestParam(defaultValue = "0") int pageno) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return userService.fetchByStatus(user, status, pageno, pagesize);
    }

    @GetMapping("/search/leads")
    public Object searchLeads(@RequestParam String searchText, @RequestHeader("Authorization") String token, @RequestParam(defaultValue = "20") int pagesize, @RequestParam(defaultValue = "0") int pageno) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return userService.search(searchText, pageno, pagesize);
    }

    @DeleteMapping("admin/delete/leads")
    public ResponseEntity<Map<String, Object>> deleteLead(@RequestParam int id, @RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonNode user = userService.validateToken(token);
        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        System.out.println(user);
        JsonNode response = user.get("body");
        String dept = response.get("userDepartment").asText();
        if (dept.equals("Admin")) {
            return userService.deleteLead(id);
        } else {
            map.put("message", "Employess Restricted, Admin use only");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        JsonNode user = userService.validateToken(token);

        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }

        String email = user.get("body").get("userEmail").asText();
        accountSecurityService.invalidateSession(email);

        map.put("message", "Successfully logged out");
        map.put("code", 200);
        map.put("status", "success");
        return ResponseEntity.ok(map);
    }
}
