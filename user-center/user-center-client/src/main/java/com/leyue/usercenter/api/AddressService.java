package com.leyue.usercenter.api;

import com.leyue.usercenter.dto.AddressDTO;
import com.leyue.usercenter.dto.command.CreateAddressCmd;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(CreateAddressCmd cmd);

    List<AddressDTO> listAddresses();

    void setDefaultAddress(String addressId);

    void deleteAddress(String addressId);
} 