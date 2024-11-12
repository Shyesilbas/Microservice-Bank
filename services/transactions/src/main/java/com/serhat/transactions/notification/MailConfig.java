package com.serhat.transactions.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {


    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost("localhost");
        mailSender.setPort(1025);

        mailSender.setUsername("");
        mailSender.setPassword("");

        mailSender.getJavaMailProperties().put("mail.smtp.auth", "false");
        mailSender.getJavaMailProperties().put("mail.smtp.starttls.enable", "false");
        mailSender.getJavaMailProperties().put("mail.smtp.starttls.required", "false");

        return mailSender;

    }

}
