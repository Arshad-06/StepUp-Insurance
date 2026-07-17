package com.infy.newgen.service;

import com.infy.newgen.dto.PurchasePolicyDTO;
import com.infy.newgen.dto.RenewPolicyDTO;
import com.infy.newgen.entity.Policy;
import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.exception.NewGenException;

public interface PolicyService {

    String purchasePolicy(PurchasePolicyDTO dto, Integer customerId) throws NewGenException;

    String renewPolicy(RenewPolicyDTO dto) throws NewGenException;

    PolicyStatus calculatePolicyStatus(Policy policy);

}