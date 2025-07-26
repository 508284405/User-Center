package com.leyue.usercenter.domain.address.gateway;

import com.leyue.usercenter.domain.address.Address;
import java.util.List;

public interface AddressGateway {
    Address findById(String addressId);
    List<Address> findByUserId(String userId);
    void save(Address address);
    void delete(String addressId);
    void setDefault(String addressId, String userId);
    void unsetDefault(String userId);
} 