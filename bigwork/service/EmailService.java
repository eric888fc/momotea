package demo.bigwork.service;

public interface EmailService {
    /**
     * 發送一封簡單的純文字郵件
     * @param to 收件者 Email
     * @param subject 主旨
     * @param text 內容
     */
    void sendSimpleMessage(String to, String subject, String text);
}