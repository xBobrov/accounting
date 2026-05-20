package com.vodokanal.accounting.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("your_email@yandex.ru");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            // 3. Добавляем файл
           // FileSystemResource file = new FileSystemResource(new File(task.attachmentPath()));
            // Первый аргумент — имя файла в письме, второй — сам файл
           // helper.addAttachment(file.getFilename(), file);

            // 4. Отправляем
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
