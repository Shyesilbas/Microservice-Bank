package com.serhat.transactions.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailService {
   private final JavaMailSender mailSender;
   private final TemplateEngine templateEngine;

   public void sendEmail(String to, String subject, String template, Context context) {
      try {
         String content = templateEngine.process(template, context);


         MimeMessage message = mailSender.createMimeMessage();
         MimeMessageHelper helper = new MimeMessageHelper(message, true);
         helper.setTo(to);
         helper.setSubject(subject);
         helper.setText(content, true);

         mailSender.send(message);
      } catch (Exception e) {
         e.printStackTrace();

      }
   }
}


