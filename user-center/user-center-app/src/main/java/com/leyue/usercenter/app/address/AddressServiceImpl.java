package com.leyue.usercenter.app.address;

import com.leyue.usercenter.api.AddressService;
import com.leyue.usercenter.app.address.excutor.CreateAddressCmdExe;
import com.leyue.usercenter.domain.address.Address;
import com.leyue.usercenter.domain.address.gateway.AddressGateway;
import com.leyue.usercenter.dto.AddressDTO;
import com.leyue.usercenter.dto.command.CreateAddressCmd;
import com.leyue.usercenter.infrastructure.address.assembler.AddressAssembler;
import com.leyue.usercenter.infrastructure.config.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressGateway addressGateway;
    private final CreateAddressCmdExe createAddressCmdExe;

    @Override
    public AddressDTO createAddress(CreateAddressCmd cmd) {
        return createAddressCmdExe.execute(cmd);
    }

    @Override
    public List<AddressDTO> listAddresses() {
        String userId = SecurityUtils.getCurrentUserId();
        List<Address> addresses = addressGateway.findByUserId(userId);
        return addresses.stream()
                .map(AddressAssembler.INSTANCE::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void setDefaultAddress(String addressId) {
        String userId = SecurityUtils.getCurrentUserId();
        addressGateway.setDefault(addressId, userId);
    }

    @Override
    public void deleteAddress(String addressId) {
        addressGateway.delete(addressId);
    }
} 