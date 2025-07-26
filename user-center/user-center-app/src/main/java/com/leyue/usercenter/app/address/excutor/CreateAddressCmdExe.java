package com.leyue.usercenter.app.address.excutor;

import com.leyue.usercenter.domain.address.Address;
import com.leyue.usercenter.domain.address.gateway.AddressGateway;
import com.leyue.usercenter.dto.AddressDTO;
import com.leyue.usercenter.dto.command.CreateAddressCmd;
import com.leyue.usercenter.infrastructure.address.assembler.AddressAssembler;
import com.leyue.usercenter.infrastructure.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateAddressCmdExe {
    private final AddressGateway addressGateway;

    public AddressDTO execute(CreateAddressCmd cmd) {
        Address address = new Address();
        address.setAddressId(UUID.randomUUID().toString());
        address.setUserId(SecurityUtils.getCurrentUserId());
        address.setReceiverName(cmd.getReceiverName());
        address.setReceiverPhone(cmd.getReceiverPhone());
        address.setProvince(cmd.getProvince());
        address.setCity(cmd.getCity());
        address.setDistrict(cmd.getDistrict());
        address.setDetailAddress(cmd.getDetailAddress());
        address.setIsDefault(cmd.getIsDefault());

        addressGateway.save(address);

        if (cmd.getIsDefault()) {
            addressGateway.setDefault(address.getAddressId(), address.getUserId());
        }

        return AddressAssembler.INSTANCE.toDTO(address);
    }
} 