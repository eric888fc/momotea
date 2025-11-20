package demo.bigwork.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(VerificationCodeService.class);

    // (教授提醒) 這是一個簡易的記憶體快取，用 Map 儲存
    // Key: email, Value: code
    // ❗ 伺服器重啟會導致所有驗證碼遺失，這在測試階段是可接受的
    // (未來) 專業的作法是使用 Redis 或 Caffeine (有過期時間)
    private final Map<String, String> codeCache = new ConcurrentHashMap<>();

    /**
     * 儲存驗證碼 (5 分鐘)
     * (我們暫時不實作 5 分鐘過期，先讓功能運作)
     */
    public void saveCode(String email, String code) {
        logger.debug("儲存驗證碼: {} -> {}", email, code);
        codeCache.put(email, code);
    }

    /**
     * 驗證驗證碼是否正確
     */
    public boolean validateCode(String email, String code) {
        String savedCode = codeCache.get(email);
        logger.debug("驗證碼: Email={}, 傳入={}, 儲存={}", email, code, savedCode);
        return savedCode != null && savedCode.equals(code);
    }

    /**
     * (重要) 驗證成功後，必須移除
     */
    public void removeCode(String email) {
        logger.debug("移除驗證碼: {}", email);
        codeCache.remove(email);
    }
}