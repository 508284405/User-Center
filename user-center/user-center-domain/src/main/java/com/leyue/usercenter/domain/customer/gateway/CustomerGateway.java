package com.leyue.usercenter.domain.customer.gateway;

import com.leyue.usercenter.domain.customer.Customer;

public interface CustomerGateway {
    Customer getByById(String customerId);
}
