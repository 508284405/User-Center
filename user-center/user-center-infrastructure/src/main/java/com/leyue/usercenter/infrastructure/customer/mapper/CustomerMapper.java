package com.leyue.usercenter.infrastructure.customer.mapper;

import com.leyue.usercenter.infrastructure.customer.CustomerDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper {

  CustomerDO getById(String customerId);
}
