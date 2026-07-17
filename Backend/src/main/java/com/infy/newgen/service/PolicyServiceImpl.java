package com.infy.newgen.service;

import java.time.LocalDate;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.newgen.dto.PurchasePolicyDTO;
import com.infy.newgen.dto.RenewPolicyDTO;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.entity.Policy;
import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.repository.PolicyRepository;

@Service(value = "policyService")
@Transactional
@CacheConfig(cacheNames = "policyCache")
public class PolicyServiceImpl implements PolicyService {

    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private Environment env;

    private static final Log LOGGER = LogFactory.getLog(PolicyServiceImpl.class);

    @Override
    public String purchasePolicy(PurchasePolicyDTO dto, Integer customerId) throws NewGenException {
        Customer customer = customerRepository
                .findById(customerId).orElseThrow(() -> new NewGenException("Service.CUSTOMER_NOT_FOUND"));

        Policy policy = modelMapper.map(dto, Policy.class);
        policy.setPolicyId(generatePolicyId());
        policy.setLastPremiumPaymentDate(policy.getPolicyStartDate());
        policy.setPolicyEndDate(policy.getPolicyStartDate().plusYears(dto.getPolicyTerm()));
        policy.setPolicyStatus(PolicyStatus.ACTIVE);
        policy.setCustomer(customer);

        LOGGER.info(env.getProperty("Service.POLICY_PURCHASE_SUCCESS"));

        return policyRepository.save(policy).getPolicyId();
    }

    @Override
    public String renewPolicy(RenewPolicyDTO dto) throws NewGenException {
        Policy policy = policyRepository
                .findById(dto.getPolicyId()).orElseThrow(() -> new NewGenException("Service.POLICY_NOT_FOUND"));
        PolicyStatus status = calculatePolicyStatus(policy);
        if (status == PolicyStatus.ACTIVE) {
            policy.setLastPremiumPaymentDate(LocalDate.now());
            LOGGER.info(env.getProperty("Service.POLICY_RENEW_SUCCESS_WITHOUT_PENALTY"));
            return policyRepository.save(policy).getPolicyId();
        }

        policy.setLastPremiumPaymentDate(LocalDate.now());

        policy.setPolicyStatus(PolicyStatus.ACTIVE);

        LOGGER.info(env.getProperty("Service.POLICY_RENEW_SUCCESS_WITH_PENALTY"));

        return policyRepository.save(policy).getPolicyId();
    }

    @Override
    public PolicyStatus calculatePolicyStatus(Policy policy) {
        if (policy.getLastPremiumPaymentDate().isBefore(LocalDate.now().minusDays(30))) {
            policy.setPolicyStatus(PolicyStatus.LAPSED);
            return PolicyStatus.LAPSED;
        }
        policy.setPolicyStatus(PolicyStatus.ACTIVE);
        return PolicyStatus.ACTIVE;
    }

    private String generatePolicyId() {
        return Optional.ofNullable(policyRepository.findTopByOrderByPolicyIdDesc()).map(policy -> {
            int number = Integer.parseInt(policy.getPolicyId().split("-")[1]);
            return "POL-" + (number + 1);
        }).orElse("POL-101");
    }

}