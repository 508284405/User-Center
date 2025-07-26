package com.leyue.usercenter.domain.address;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Address {
    private Long id;
    private String addressId;
    private String userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Boolean isDefault;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public void setDefault() {
        this.isDefault = true;
    }
    
    public void unsetDefault() {
        this.isDefault = false;
    }
} 