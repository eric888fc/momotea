package demo.bigwork.util;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

public class ECPayUtil {
    
    /**
     * 產生 CheckMacValue
     */
    public static String genCheckMacValue(Map<String, String> params, String hashKey, String hashIV) {
        // 1. 排除空值與 CheckMacValue 本身
        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length() > 0
                    && !entry.getKey().equalsIgnoreCase("CheckMacValue")) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }

        // 2. 依照參數名稱排序 (使用 TreeMap 確保正確排序)
        Map<String, String> sortedMap = new TreeMap<>(filtered);

        // 3. 組合字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey);
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        sb.append("&HashIV=").append(hashIV);

        // 4. URL encode (小寫)
        String encoded = urlEncode(sb.toString()).toLowerCase();

        // 5. 使用 MD5 產生雜湊值
        return md5(encoded).toUpperCase();
    }

    /**
     * 驗證 CheckMacValue
     */
    public static boolean checkMacValue(Map<String, String> params, String hashKey, String hashIV) {
        if (!params.containsKey("CheckMacValue")) return false;
        
        String receivedCheckMacValue = params.get("CheckMacValue");
        String calculatedCheckMacValue = genCheckMacValue(params, hashKey, hashIV);
        
        // (Debug) 印出比較，方便除錯
        if (!receivedCheckMacValue.equalsIgnoreCase(calculatedCheckMacValue)) {
            System.out.println("===== 綠界驗證失敗詳情 (最終診斷) =====");
            System.out.println("錯誤原因：空白鍵編碼或 HashKey/HashIV 差異");
            System.out.println("綠界傳來: " + receivedCheckMacValue);
            System.out.println("我方計算: " + calculatedCheckMacValue);
            System.out.println("=====================================");
        }

        return receivedCheckMacValue.equalsIgnoreCase(calculatedCheckMacValue);
    }

    // URL Encode
    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8")
                    // ★ 關鍵修正：將 Java 的 + 換成綠界要求的 %20
                    .replace("+", "%20") 
                    
                    // 其他不編碼的字元
                    .replace("%21", "!")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%2A", "*")
                    .replace("%2D", "-")
                    .replace("%2E", ".")
                    .replace("%5F", "_");
        } catch (Exception e) {
            throw new RuntimeException("URL Encode Error", e);
        }
    }

    /**
     * MD5 加密方法
     */
    private static String md5(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5"); 
            byte[] hash = digest.digest(str.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 Error", e);
        }
    }
}