package com.leyue.usercenter.infrastructure.customer;

import com.leyue.usercenter.domain.customer.Customer;
import com.leyue.usercenter.domain.customer.gateway.CustomerGateway;

import com.leyue.usercenter.infrastructure.customer.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerGatewayImpl implements CustomerGateway {
    private final CustomerMapper customerMapper;

    public Customer getByById(String customerId){
      CustomerDO customerDO = customerMapper.getById(customerId);
      //Convert to Customer
      return null;
    }
}
