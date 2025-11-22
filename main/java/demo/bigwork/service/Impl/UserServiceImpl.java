package demo.bigwork.service.Impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException; // 登入失敗例外
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import demo.bigwork.dao.PasswordResetTokenDAO;
import demo.bigwork.dao.UserDAO;
import demo.bigwork.dao.WalletDAO;
import demo.bigwork.exception.EmailAlreadyExistsException;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.PasswordResetTokenPO;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.po.WalletPO;
import demo.bigwork.model.vo.LoginRequestVO;
import demo.bigwork.model.vo.ProfileRequestVO;
import demo.bigwork.model.vo.RegisterRequestVO;
import demo.bigwork.service.AuthHelperService;
import demo.bigwork.service.EmailService;
import demo.bigwork.service.UserService;
import demo.bigwork.service.VerificationCodeService;

@Service
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationCodeService codeService;
    private final PasswordResetTokenDAO tokenDAO;
    private final WalletDAO walletDAO; 
    private final AuthHelperService authHelperService;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    // (關鍵新增！)注入application.properties 中的「前端網址」
    @Value("${frontend.reset-password-url}")
    private String frontendResetUrl;

    // (關鍵) 修改建構子，注入新的 Service
    @Autowired
    public UserServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, 
                           EmailService emailService, VerificationCodeService codeService,
                           PasswordResetTokenDAO tokenDAO, WalletDAO walletDAO,
                           AuthHelperService authHelperService) { // <-- 新增
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.codeService = codeService;
        this.tokenDAO = tokenDAO;
        this.walletDAO = walletDAO;
        this.authHelperService = authHelperService; // <-- 新增
    }

    // (重構) 我們將 register 邏輯抽出來共用

    @Transactional
    @Override
    public UserPO registerBuyer(RegisterRequestVO requestVO) throws EmailAlreadyExistsException, Exception {
        return register(requestVO, UserRole.BUYER); // 呼叫共用方法，指定角色
    }

    @Transactional
    @Override
    public UserPO registerSeller(RegisterRequestVO requestVO) throws EmailAlreadyExistsException, Exception {
        return register(requestVO, UserRole.SELLER); // 呼叫共用方法，指定角色
    }

    // (新方法) 這是 Controller 要呼叫的「發送驗證碼」方法
    @Override
    public void sendVerificationCode(String email) throws Exception {
        // 1. 檢查 Email 是否已存在
        if (userDAO.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("此 Email 已經被註冊: " + email);
        }

        // 2. 生成 6 位數驗證碼
        String code = String.format("%06d", new Random().nextInt(999999));

        // 3. (關鍵) 儲存驗證碼到快取
        codeService.saveCode(email, code);

        // 4. (關鍵) 發送信件
        String subject = "您的 [交易平台] 註冊驗證碼";
        String body = "您的驗證碼是：" + code + "\n\n(此為測試，未來請加上 5 分鐘內有效)";
        emailService.sendSimpleMessage(email, subject, body);
    }

    // (修改) 修改 `register` 共用方法
    private UserPO register(RegisterRequestVO requestVO, UserRole role) throws Exception {

        // 1. (新) 驗證驗證碼
        if (!codeService.validateCode(requestVO.getEmail(), requestVO.getCode())) {
            throw new Exception("驗證碼錯誤或已過期");
        }

        // 2. 檢查 Email (Service 裡再檢查一次)
        if (userDAO.existsByEmail(requestVO.getEmail())) {
            throw new EmailAlreadyExistsException("此 Email 已經被註冊: " + requestVO.getEmail());
        }

        // 3. 密碼雜湊
        String hashedPassword = passwordEncoder.encode(requestVO.getPassword());

        // 4. 從 VO 轉換到 PO
        UserPO newUser = new UserPO();
        newUser.setName(requestVO.getName());
        newUser.setEmail(requestVO.getEmail());
        newUser.setPassword(hashedPassword);
        newUser.setPhone(requestVO.getPhone());
        newUser.setDefaultAddress(requestVO.getAddress());
        newUser.setRole(role);

        // 5. 儲存到資料庫
        UserPO savedUser = userDAO.save(newUser);

        // 6. (新) 註冊成功，移除驗證碼
        codeService.removeCode(requestVO.getEmail());

        // 5. (新) 建立「模擬錢包」
        logger.info("正在為新使用者 {} 建立錢包...", savedUser.getEmail());
        
        BigDecimal initialBalance;
        if (role == UserRole.BUYER) {
            // (模擬) 送買家 10,000 元模擬金
            initialBalance = new BigDecimal("10000.00"); 
        } else {
            // (賣家) 餘額為 0
            initialBalance = BigDecimal.ZERO; 
        }

        // 6. (新) 建立 WalletPO 並關聯 UserPO
        WalletPO newWallet = new WalletPO(savedUser, initialBalance);
        
        // 7. (新) 儲存錢包
        walletDAO.save(newWallet);
        
        logger.info("使用者 {} 錢包建立成功，初始餘額: {}", savedUser.getEmail(), initialBalance);
        
        // 8. (不變) 回傳「使用者」
        return savedUser;
    }

    @Override
    public UserPO login(LoginRequestVO requestVO) throws Exception {
        // 1. 透過 Email 尋找使用者
        UserPO user = userDAO.findByEmail(requestVO.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email錯誤"));

        // 2. (關鍵) 驗證密碼
        // 使用 passwordEncoder.matches 來比較「明文密碼」(request) 和「雜湊密碼」(db)
        if (!passwordEncoder.matches(requestVO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("密碼錯誤");
        }

        // 3. 密碼正確，登入成功
        return user;
    }

    @Override
    public UserPO findUserByEmail(String email) {
        return userDAO.findByEmail(email).orElse(null);
    }

    @Override
    @Transactional
    public void createPasswordResetToken(String email) {
        UserPO user = userDAO.findByEmail(email).orElse(null);

        if (user == null) {
            logger.warn("密碼重設請求：找不到 Email {}，已靜默處理。",email);
            return; 
        }

        // 1. (關鍵新增！) 刪除 這個使用者 「所有」 舊的 Token
        // ( 這正是你要的 tokenDAO.deleteByUser(user) )
        tokenDAO.deleteByUser(user);

        // 2. (關鍵新增！) 計算「 1 小時後」 的過期時間
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1); // 從現在開始，加 1 小時
        Timestamp expiryDate = new Timestamp(cal.getTime().getTime());

        // 3. (產生 Token) 產生一個 UUID
        String tokenString = UUID.randomUUID().toString();
        
        // 4. (關鍵修正！) 使用 你在 `PasswordResetTokenPO.java` 
        //  定義的「完整建構子」
        PasswordResetTokenPO resetToken = new PasswordResetTokenPO(tokenString, user, expiryDate);
        
        // 5. (儲存) 將新的 Token 存入資料庫
        tokenDAO.save(resetToken);

        // 6. (發送郵件) 組合 完整的 URL
        String resetLink = frontendResetUrl + "?token=" + tokenString;
        
        String subject = "您的密碼重設請求";
        String text = "您收到此郵件是因為您（或某人）請求重設您的帳戶密碼。\n\n"
                  + "請點擊（或複製貼上）以下連結以完成重設：\n\n"
                  + resetLink + "\n\n"
                  + "如果您沒有請求重設，請忽略此郵件。 此連結將在1小時後過期。";

        emailService.sendSimpleMessage(user.getEmail(), subject, text);
        
        logger.info("已為使用者 {} 產生密碼重設連結。",user.getEmail());
        logger.info(resetLink);
    }

    @Override
    public void resetPassword(String token, String newPassword) throws Exception {
        // 1. 驗證 Token
        PasswordResetTokenPO resetToken = tokenDAO.findByToken(token).orElseThrow(() -> new Exception("無效的 Token"));

        // 2. 驗證是否過期
        if (resetToken.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
            tokenDAO.delete(resetToken); // (順便清理)
            throw new Exception("Token 已過期");
        }

        // 3. 取得使用者
        UserPO user = resetToken.getUser();

        // 4. (關鍵) 雜湊新密碼並更新
        String newHashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(newHashedPassword);

        // 5. (JPA 更新) 儲存使用者
        userDAO.save(user);

        // 6. (關鍵) 刪除已使用過的 Token
        tokenDAO.delete(resetToken);

        logger.info("使用者 {} 已成功重設密碼", user.getEmail());
    }
    
    @Override
    @Transactional(readOnly = true) // 查詢
    public UserPO getMyProfile() throws AccessDeniedException {
        // (關鍵) 
        // 我們 100% 重用 AuthHelperService 
        // 取得「當前登入者」(不限角色)
        logger.info("正在查詢個人資料...");
        return authHelperService.getCurrentAuthenticatedUser();
    }

    @Override
    @Transactional // 寫入
    public UserPO updateMyProfile(ProfileRequestVO requestVO) throws AccessDeniedException {
        // 1. (安全) 取得「當前登入者」
        UserPO userToUpdate = authHelperService.getCurrentAuthenticatedUser();
        
        // 2. (業務) 從 VO 更新 PO 的資料
        // (注意：我們「不」更新 email, password, role)
        userToUpdate.setName(requestVO.getName());
        userToUpdate.setPhone(requestVO.getPhone());
        userToUpdate.setDefaultAddress(requestVO.getAddress());
        
        // 3. (儲存) 
        // 呼叫 save() 執行 UPDATE SQL
        logger.info("使用者 {} 正在更新個人資料", userToUpdate.getEmail());
        return userDAO.save(userToUpdate);
    }
}