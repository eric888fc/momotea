package demo.bigwork.util;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;

public class ECPayUtil {
	
	/**
     * 產生 CheckMacValue
     * @param params API 參數 (不含 CheckMacValue)
     * @param hashKey 商店 HashKey
     * @param hashIV 商店 HashIV
     * @return 計算後的 CheckMacValue
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

        // 2. 依照參數名稱排序 (ASCII)
        List<String> keys = new ArrayList<>(filtered.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);

        // 3. 組合字串
        StringBuilder sb = new StringBuilder();
        sb.append("HashKey=").append(hashKey);
        for (String key : keys) {
            sb.append("&").append(key).append("=").append(filtered.get(key));
        }
        sb.append("&HashIV=").append(hashIV);

        // 4. URL encode (小寫)
        String encoded = urlEncode(sb.toString()).toLowerCase();

        // 5. 使用 SHA256 產生雜湊值 (也可改 MD5)
        return sha256(encoded).toUpperCase();
    }

    /**
     * 驗證 CheckMacValue
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
    }

    // URL Encode
    private static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8")
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

    // SHA256
    private static String sha256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(str.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA256 Error", e);
        }
    }

}
