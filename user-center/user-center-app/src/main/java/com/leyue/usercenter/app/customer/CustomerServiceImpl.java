package com.leyue.usercenter.app.customer;

import com.alibaba.cola.catchlog.CatchAndLog;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.leyue.usercenter.api.CustomerServiceI;
import com.leyue.usercenter.app.customer.executor.CustomerAddCmdExe;
import com.leyue.usercenter.app.customer.executor.query.CustomerListByNameQryExe;
import com.leyue.usercenter.dto.CustomerAddCmd;
import com.leyue.usercenter.dto.CustomerListByNameQry;
import com.leyue.usercenter.dto.data.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@CatchAndLog
public class CustomerServiceImpl implements CustomerServiceI {

    private final CustomerAddCmdExe customerAddCmdExe;

    private final CustomerListByNameQryExe customerListByNameQryExe;

    public Response addCustomer(CustomerAddCmd customerAddCmd) {
        return customerAddCmdExe.execute(customerAddCmd);
    }

    @Override
    public MultiResponse<CustomerDTO> listByName(CustomerListByNameQry customerListByNameQry) {
        return customerListByNameQryExe.execute(customerListByNameQry);
    }

}