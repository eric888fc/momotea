package demo.bigwork.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * (關鍵) @Configuration
 * 告訴 Spring Boot：這是一個「設定類別」
 *
 * (關鍵) WebMvcConfigurer
 * 讓我們可以「客製化」Spring MVC 的預設行為
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceConfig.class);

    // (關鍵) 
    // 我們「再一次」從 application.properties 讀取這個路徑
    @Value("${file.storage.location}")
    private String storageLocationString;

    /**
     * (關鍵) 覆寫 (Override) 這個方法
     * * 它的職責就是「新增」URL 規則
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // (關鍵) 
        // 規則 1：
        // 網路路徑："/uploads/**"
        // 任何 /uploads/... 開頭的請求
        String webPath = "/uploads/**"; 

        // 規則 2：
        // 實體路徑："file:./uploads/"
        // (注意)
        // 1. "file:" 字首是「必須的」，它告訴 Spring 
        //    這是一個「外部」的實體資料夾路徑
        // 2. 結尾的 "/" 也是「必須的」
        Path storagePath = Paths.get(storageLocationString).toAbsolutePath();
        String physicalPath = "file:" + storagePath.toString() + "/";
        
        registry.addResourceHandler(webPath)  // (設定網路路徑)
                .addResourceLocations(physicalPath); // (對應到實體路徑)
        
        logger.info("--- 靜態資源設定 ---");
        logger.info("網路路徑 (Web Path):   {}", webPath);
        logger.info("實體路徑 (Physical Path): {}", physicalPath);
        logger.info("--------------------");
    }
}