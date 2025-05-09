package com.qp.dataCentralize.helper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class PlanotechMailSender {

    @Autowired
   private JavaMailSender mailSender;

    public void sendVerificationEmail(JsonNode user, String otp) {
           String userName = user.get("body").get("userName").asText();
           String email = user.get("body").get("userEmail").asText();
           MimeMessage message = mailSender.createMimeMessage();
           MimeMessageHelper helper = new MimeMessageHelper(message);
           try {
               helper.setFrom("planotechevents1@gmail.com", "Planotech Events");
               helper.setSubject("Planotech Events - OTP Verification");
               helper.setTo(email);
               String htmlBody = null;


               htmlBody = readHtmlTemplate();
               htmlBody = htmlBody.replace("{{OTP}}", otp);
               htmlBody = htmlBody.replace("{{USERNAME}}", userName);
               helper.setText(htmlBody, true);
           } catch (UnsupportedEncodingException | MessagingException e) {
               e.printStackTrace();
               throw new RuntimeException(e.getMessage());
           }
           mailSender.send(message);
    }

    private String readHtmlTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + "otp.html");
            InputStream inputStream = resource.getInputStream();
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
