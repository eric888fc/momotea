package demo.bigwork.service.Impl;

import demo.bigwork.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // (關鍵) 匯入
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    /**
     * (關鍵 - 修正 1)
     * 1. 我們使用「欄位注入 (Field Injection)」
     * Spring 會自動將 "file.storage.location" 的值注入這個 String
     */
    @Value("${file.storage.location}")
    private String storageLocationString;

    /**
     * 2. 這是我們真正要操作的「路徑」物件
     */
    private Path rootLocation;

    /**
     * (關鍵 - 修正 2)
     * 3. 我們不再需要「建構子」來注入
     * Spring 會使用預設的空建構子
     */
    public FileStorageServiceImpl() {
        // 空的建構子
    }

    /**
     * (關鍵 - 修正 3)
     * 4. 在 @PostConstruct 中，我們做「兩件事」：
     * a. 將 "storageLocationString" (String) 轉換為 "rootLocation" (Path)
     * b. 建立資料夾
     */
    @Override
    @PostConstruct
    public void init() {
        try {
            // (新) 將 String 轉換為 Path
            this.rootLocation = Paths.get(storageLocationString);
            
            // (舊) 建立資料夾
            Files.createDirectories(rootLocation);
            logger.info("檔案儲存資料夾已建立於: {}", rootLocation.toAbsolutePath());
        } catch (IOException e) {
            logger.error("無法初始化檔案儲存資料夾", e);
            throw new RuntimeException("無法初始化檔案儲存資料夾", e);
        }
    }

    /**
     * 儲存檔案的核心邏輯
     * (這個方法 100% 保持不變)
     */
    @Override
    public String store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            if (file.isEmpty()) {
                throw new IOException("檔案為空: " + originalFilename);
            }
            if (originalFilename.contains("..")) {
                throw new SecurityException("檔名包含無效路徑: " + originalFilename);
            }

            String extension = StringUtils.getFilenameExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

            // (重要) 
            // 這裡的 "this.rootLocation" 
            // 現在已經被 init() 方法安全地初始化了
            Path destinationFile = this.rootLocation.resolve(uniqueFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, 
                        StandardCopyOption.REPLACE_EXISTING);
            }
            
            logger.info("檔案儲存成功: {}", uniqueFilename);
            
            // (重要) 
            // 我們回傳的路徑必須以 "/" 開頭
            // 這樣才能匹配我們下一步的「靜態資源」設定
            return "/uploads/" + uniqueFilename;

        } catch (IOException e) {
            logger.error("儲存檔案失敗: {}", originalFilename, e);
            throw new RuntimeException("儲存檔案失敗: " + originalFilename, e);
        }
    }
}