package demo.bigwork.service.Impl;

import demo.bigwork.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender emailSender;

    // (關鍵) 從 application.properties 讀取你的「寄件者」Email 帳號
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    public EmailServiceImpl(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage(); 
            message.setFrom(fromEmail); // 設定寄件者
            message.setTo(to); 
            message.setSubject(subject); 
            message.setText(text);
            
            logger.info("正在發送郵件至: {}", to);
            emailSender.send(message);
            logger.info("郵件發送成功。");
            
        } catch (Exception e) {
            logger.error("發送郵件失敗: {}", e.getMessage());
            // (教授提醒) 這裡不應向上拋出例外，避免註冊流程因「寄信失敗」而中斷
            // 但在「發送驗證碼」功能中，我們需要讓 Controller 知道失敗了
            throw new RuntimeException("Email 發送失敗", e);
        }
    }
}