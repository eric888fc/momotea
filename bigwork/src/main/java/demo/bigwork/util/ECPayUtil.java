package demo.bigwork.util;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

public class ECPayUtil {
<<<<<<< HEAD
    
    /**
     * 產生 CheckMacValue
=======
	
	/**
     * 產生 CheckMacValue
     * @param params API 參數 (不含 CheckMacValue)
     * @param hashKey 商店 HashKey
     * @param hashIV 商店 HashIV
     * @return 計算後的 CheckMacValue
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
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

<<<<<<< HEAD
        // 2. 依照參數名稱排序 (使用 TreeMap 確保正確排序)
        Map<String, String> sortedMap = new TreeMap<>(filtered);
=======
        // 2. 依照參數名稱排序 (ASCII)
        List<String> keys = new ArrayList<>(filtered.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb

        // 3. 組合字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey);
<<<<<<< HEAD
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
=======
        for (String key : keys) {
            sb.append("&").append(key).append("=").append(filtered.get(key));
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
        }
        sb.append("&HashIV=").append(hashIV);

        // 4. URL encode (小寫)
        String encoded = urlEncode(sb.toString()).toLowerCase();

<<<<<<< HEAD
        // 5. 使用 MD5 產生雜湊值
        return md5(encoded).toUpperCase();
=======
        // 5. 使用 SHA256 產生雜湊值 (也可改 MD5)
        return sha256(encoded).toUpperCase();
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
    }

    /**
     * 驗證 CheckMacValue
<<<<<<< HEAD
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
=======
     * @param params API 回傳參數 (含 CheckMacValue)
     * @param hashKey 商店 HashKey
     * @param hashIV 商店 HashIV
     * @return 是否驗證成功
     */
    public static boolean checkMacValue(Map<String, String> params, String hashKey, String hashIV) {
        if (!params.containsKey("CheckMacValue")) return false;
        String checkMacValue = params.get("CheckMacValue");
        String genValue = genCheckMacValue(params, hashKey, hashIV);
        return checkMacValue.equalsIgnoreCase(genValue);
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
    }

    // URL Encode
    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8")
<<<<<<< HEAD
                    // ★ 關鍵修正：將 Java 的 + 換成綠界要求的 %20
                    .replace("+", "%20") 
                    
                    // 其他不編碼的字元
=======
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
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

<<<<<<< HEAD
    /**
     * MD5 加密方法
     */
    private static String md5(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5"); 
=======
    // SHA256
    private static String sha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
            byte[] hash = digest.digest(str.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
<<<<<<< HEAD
            throw new RuntimeException("MD5 Error", e);
        }
    }
}
=======
            throw new RuntimeException("SHA256 Error", e);
        }
    }

}
>>>>>>> 6323878df637fcec51403b0fda58001a3d440cbb
