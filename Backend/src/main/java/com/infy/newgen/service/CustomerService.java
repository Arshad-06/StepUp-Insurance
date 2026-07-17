package com.infy.newgen.service;

import java.util.List;

import com.infy.newgen.dto.CustomerLoginDTO;
import com.infy.newgen.dto.CustomerRegistrationDTO;
import com.infy.newgen.dto.PolicyResponseDTO;
import com.infy.newgen.exception.NewGenException;

public interface CustomerService {
    public Integer registerCustomer(CustomerRegistrationDTO registerDTO) throws NewGenException;

    public Integer loginCustomer(CustomerLoginDTO loginDTO) throws NewGenException;

    public List<PolicyResponseDTO> getPolicies(Integer customerId) throws NewGenException;
}