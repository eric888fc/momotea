package demo.bigwork.service;

import java.util.List;

import demo.bigwork.model.enums.UserRole;
import demo.bigwork.model.vo.AdminUserSummaryVO;
import demo.bigwork.model.vo.OrderResponseVO;

/**
 * 管理員的「會員管理」服務
 */
public interface AdminMemberService {

    /** 取得指定角色（BUYER / SELLER）的會員列表 */
    List<AdminUserSummaryVO> listUsersByRole(UserRole role);

    /** 取得某位買家的所有訂單（以 buyer 身份） */
    List<OrderResponseVO> listOrdersByBuyer(Long buyerId);

    /** 取得某位賣家的所有訂單（以 seller 身份） */
    List<OrderResponseVO> listOrdersBySeller(Long sellerId);
}