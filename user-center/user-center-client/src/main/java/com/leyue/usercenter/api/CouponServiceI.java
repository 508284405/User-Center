package com.leyue.usercenter.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.dto.command.coupon.CreateCouponTemplateCmd;
import com.leyue.usercenter.dto.command.coupon.IssueCouponCmd;
import com.leyue.usercenter.dto.command.coupon.ReserveCouponCmd;
import com.leyue.usercenter.dto.command.coupon.UpdateCouponTemplateCmd;
import com.leyue.usercenter.dto.command.coupon.UseCouponCmd;
import com.leyue.usercenter.dto.data.coupon.CouponDTO;
import com.leyue.usercenter.dto.data.coupon.CouponTemplateDTO;
import com.leyue.usercenter.dto.query.coupon.CouponPageQry;
import com.leyue.usercenter.dto.query.coupon.CouponTemplatePageQry;
import com.leyue.usercenter.dto.query.coupon.CouponTemplateQry;

public interface CouponServiceI {
    
    // =============== 优惠券模板管理 ===============
    
    SingleResponse<CouponTemplateDTO> createTemplate(CreateCouponTemplateCmd cmd);
    
    SingleResponse<CouponTemplateDTO> updateTemplate(UpdateCouponTemplateCmd cmd);
    
    SingleResponse<CouponTemplateDTO> getTemplate(CouponTemplateQry qry);
    
    PageResponse<CouponTemplateDTO> getTemplatePage(CouponTemplatePageQry qry);
    
    MultiResponse<CouponTemplateDTO> getAvailableTemplates();
    
    Response activateTemplate(Long templateId);
    
    Response deactivateTemplate(Long templateId);
    
    Response deleteTemplate(Long templateId);
    
    // =============== 优惠券发放管理 ===============
    
    SingleResponse<CouponDTO> issueCoupon(IssueCouponCmd cmd);
    
    MultiResponse<CouponDTO> batchIssueCoupon(IssueCouponCmd cmd, Integer count);
    
    PageResponse<CouponDTO> getUserCoupons(CouponPageQry qry);
    
    MultiResponse<CouponDTO> getAvailableCoupons(String userId);
    
    MultiResponse<CouponDTO> getUsableCoupons(String userId, String orderAmount);
    
    // =============== 优惠券使用管理 ===============
    
    Response reserveCoupon(ReserveCouponCmd cmd);
    
    Response confirmCoupon(String couponCode, String orderId);
    
    Response cancelReservation(String couponCode, String orderId);
    
    Response useCoupon(UseCouponCmd cmd);
    
    Response expireCoupon(String couponCode);
    
    // =============== 优惠券状态查询 ===============
    
    SingleResponse<CouponDTO> getCoupon(String couponCode);
    
    SingleResponse<String> getCouponStatus(String couponCode);
    
    Response validateCoupon(String couponCode, String userId);
}