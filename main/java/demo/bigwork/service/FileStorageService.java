package demo.bigwork.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 初始化儲存空間 (e.g., 建立資料夾)
     */
    void init();

    /**
     * 儲存檔案
     * @param file 從 Controller 傳入的檔案
     * @return 儲存後可供存取的「相對路徑」 (e.g., "/uploads/filename.jpg")
     */
    String store(MultipartFile file);
    
    // (未來)
    // Resource loadFile(String filename);
    // void deleteFile(String filename);
}