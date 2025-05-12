package com.qp.dataCentralize.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.qp.dataCentralize.entity.Department;
import com.qp.dataCentralize.helper.PlanotechMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountSecurityService {

    @Autowired
    private UserService userService;

    @Autowired
    private PlanotechMailSender mailSender;

    private final Map<String, String> twoFactorCodes = new HashMap<>();
    private final Map<String, Instant> verifiedSessions = new ConcurrentHashMap<>();
    private final Map<String, Instant> otpExpiryTimes = new ConcurrentHashMap<>();
    private static final long SESSION_EXPIRY_MINUTES = 60; // Session expires after 1 hour
    private static final long OTP_EXPIRY_MINUTES = 10; // OTP expires after 10 minutes

//    public ResponseEntity<Map<String, Object>> verifyAccountTeamAccess(JsonNode user) {
//        Map<String, Object> response = new HashMap<>();
//        String department = user.get("body").get("userDepartment").asText();
//        String email = user.get("body").get("userEmail").asText();
//
//        if (!department.equalsIgnoreCase("Finance and Accounts")&&!department.equalsIgnoreCase("Administration")) {
//            response.put("message", "Access denied. User is not from Finance and Accounts department");
//            response.put("code", HttpStatus.FORBIDDEN.value());
//            response.put("status", "fail");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//        }
//
//        Instant lastVerified = verifiedSessions.get(email);
//        if (lastVerified != null && !isSessionExpired(lastVerified)) {
//            response.put("message", "Session is still valid");
//            response.put("code", HttpStatus.OK.value());
//            response.put("status", "success");
//            response.put("sessionValid", true);
//            return ResponseEntity.ok(response);
//        }
//
//        // Generate and store 2FA code
//        String twoFactorCode = generateTwoFactorCode();
//        twoFactorCodes.put(email, twoFactorCode);
//        otpExpiryTimes.put(email, Instant.now());
//
//        // Send OTP via email
//        try {
//            mailSender.sendVerificationEmail(user, twoFactorCode);
//        } catch (Exception e) {
//            response.put("message", "Failed to send OTP email");
//            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
//            response.put("status", "fail");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//
//        response.put("message", "Two-factor authentication required. OTP has been sent to your email.");
//        response.put("code", HttpStatus.OK.value());
//        response.put("status", "success");
//        response.put("sessionValid", false);
//        return ResponseEntity.ok(response);
//    }

    public ResponseEntity<Map<String, Object>> verifyDepartmentAccess(JsonNode user, List<Department> allowedDepartments) {
        Map<String, Object> response = new HashMap<>();
        String departmentStr = user.get("body").get("userDepartment").asText();
        String email = user.get("body").get("userEmail").asText();
        Department userDept;
        try {
            userDept = Department.valueOf(departmentStr.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            response.put("message", "Invalid department");
            response.put("code", HttpStatus.FORBIDDEN.value());
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // Allow ADMIN access to all
        if (!allowedDepartments.contains(userDept) && userDept != Department.ADMIN) {
            response.put("message", "Access denied for your department");
            response.put("code", HttpStatus.FORBIDDEN.value());
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Instant lastVerified = verifiedSessions.get(email);
        if (lastVerified != null && !isSessionExpired(lastVerified)) {
            response.put("message", "Session is still valid");
            response.put("code", HttpStatus.OK.value());
            response.put("status", "success");
            response.put("sessionValid", true);
            return ResponseEntity.ok(response);
        }

        // Generate and send 2FA code
        String code = generateTwoFactorCode();
        twoFactorCodes.put(email, code);
        otpExpiryTimes.put(email, Instant.now());

        try {
            mailSender.sendVerificationEmail(user, code);
        } catch (Exception e) {
            response.put("message", "Failed to send OTP email");
            response.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
        response.put("message", "Two-factor authentication required. OTP sent.");
        response.put("code", HttpStatus.OK.value());
        response.put("status", "success");
        response.put("sessionValid", false);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> verifyTwoFactorCode(String email, String code) {
        System.out.println(email+"***");
        Map<String, Object> response = new HashMap<>();
        String storedCode = twoFactorCodes.get(email);
        Instant otpExpiry = otpExpiryTimes.get(email);

        if (storedCode == null || !storedCode.equals(code)) {
            response.put("message", "Invalid two-factor authentication code");
            response.put("code", HttpStatus.UNAUTHORIZED.value());
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (otpExpiry != null && isOtpExpired(otpExpiry)) {
            response.put("message", "OTP has expired. Please request a new one.");
            response.put("code", HttpStatus.UNAUTHORIZED.value());
            response.put("status", "fail");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Clear the used code and create a new session
        twoFactorCodes.remove(email);
        otpExpiryTimes.remove(email);
        verifiedSessions.put(email, Instant.now());

        response.put("message", "Two-factor authentication successful");
        response.put("code", HttpStatus.OK.value());
        response.put("status", "success");
        response.put("sessionValid", true);
        return ResponseEntity.ok(response);
    }

    private String generateTwoFactorCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private boolean isSessionExpired(Instant lastVerified) {
        return Instant.now().isAfter(lastVerified.plusSeconds(SESSION_EXPIRY_MINUTES * 60));
    }

    private boolean isOtpExpired(Instant otpExpiry) {
        return Instant.now().isAfter(otpExpiry.plusSeconds(OTP_EXPIRY_MINUTES * 60));
    }

    public void invalidateSession(String email) {
        verifiedSessions.remove(email);
    }
}