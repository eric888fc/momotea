package demo.bigwork.exception;

/**
 * 自定義業務例外：當註冊的 Email 已經存在時拋出
 */
public class EmailAlreadyExistsException extends Exception {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}