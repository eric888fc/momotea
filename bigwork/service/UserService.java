package demo.bigwork.service;

import org.springframework.security.access.AccessDeniedException;

import demo.bigwork.exception.EmailAlreadyExistsException;
import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.po.UserPO;
import demo.bigwork.model.vo.LoginRequestVO;
import demo.bigwork.model.vo.ProfileRequestVO;
import demo.bigwork.model.vo.RegisterRequestVO;

public interface UserService {

    /**
     * 業務邏輯：註冊一個新買家
     */
    UserPO registerBuyer(RegisterRequestVO requestVO) throws EmailAlreadyExistsException, Exception;

    /**
     * 業務邏輯：註冊一個新賣家
     */
    UserPO registerSeller(RegisterRequestVO requestVO) throws EmailAlreadyExistsException, Exception;
    
    /**
     * 業務邏輯：使用者登入
     * @return 登入成功的使用者物件 (若失敗則拋出例外)
     */
    UserPO login(LoginRequestVO requestVO) throws Exception;
    
    UserPO findUserByEmail(String email);

	void sendVerificationCode(String email) throws Exception;
	
	/**
     * 業務邏輯：建立密碼重設 Token
     * (給 /api/auth/forgot-password 呼叫)
     */
    void createPasswordResetToken(String email) throws Exception;
    
    /**
     * 業務邏輯：使用 Token 重設密碼
     * (給 /api/auth/reset-password 呼叫)
     */
    void resetPassword(String token, String newPassword) throws Exception;
    
    UserPO getMyProfile() throws AccessDeniedException;
    
    UserPO updateMyProfile(ProfileRequestVO requestVO) throws AccessDeniedException;

	}