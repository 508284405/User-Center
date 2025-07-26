package com.leyue.usercenter.web;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.usercenter.api.AddressService;
import com.leyue.usercenter.dto.AddressDTO;
import com.leyue.usercenter.dto.command.CreateAddressCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 地址管理控制器
 * 提供地址相关的RESTful API，包括地址的创建、查询、设置默认地址和删除等功能
 *
 * @author Trae
 * @since 2024-01-20
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    
    /**
     * 创建新地址
     * 
     * @param cmd 创建地址命令，包含地址基本信息
     * @return 创建的地址信息
     */
    @PostMapping
    public SingleResponse<AddressDTO> createAddress(@Valid @RequestBody CreateAddressCmd cmd) {
        return SingleResponse.of(addressService.createAddress(cmd));
    }
    
    /**
     * 获取当前用户的所有地址
     * 
     * @return 地址列表
     */
    @GetMapping
    public SingleResponse<List<AddressDTO>> listAddresses() {
        return SingleResponse.of(addressService.listAddresses());
    }
    
    /**
     * 设置默认地址
     * 
     * @param addressId 要设置为默认的地址ID
     * @return 无返回内容
     */
    @PutMapping("/{addressId}/default")
    public SingleResponse<Void> setDefaultAddress(@PathVariable String addressId) {
        addressService.setDefaultAddress(addressId);
        return SingleResponse.of(null);
    }
    
    /**
     * 删除地址
     * 
     * @param addressId 要删除的地址ID
     * @return 无返回内容
     */
    @DeleteMapping("/{addressId}")
    public SingleResponse<Void> deleteAddress(@PathVariable String addressId) {
        addressService.deleteAddress(addressId);
        return SingleResponse.of(null);
    }
}