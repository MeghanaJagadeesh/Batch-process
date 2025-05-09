package com.qp.dataCentralize.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.service.AccountSecurityService;
import com.qp.dataCentralize.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/planotech-inhouse/accounts")
public class AccountSecurityController {

    @Autowired
    private AccountSecurityService accountSecurityService;

    @Autowired
    private UserService userService;

    @GetMapping("/verify-access")
    public ResponseEntity<Map<String, Object>> verifyAccountAccess(@RequestHeader("Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        JsonNode user = userService.validateToken(token);

        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }
        return accountSecurityService.verifyAccountTeamAccess(user);
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<Map<String, Object>> verifyTwoFactorCode(
            @RequestHeader("Authorization") String token,
            @RequestParam String code) {
        Map<String, Object> map = new HashMap<>();
        JsonNode user = userService.validateToken(token);

        if (user == null) {
            map.put("message", "Invalid Token or User Not found");
            map.put("code", 400);
            map.put("status", "fail");
            return ResponseEntity.badRequest().body(map);
        }

        String email = user.get("body").get("userEmail").asText();
        return accountSecurityService.verifyTwoFactorCode(email, code);
    }

    @PostMapping("/logout")
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