package com.infy.newgen.service;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;

import com.infy.newgen.dto.PurchasePolicyDTO;
import com.infy.newgen.dto.RenewPolicyDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.entity.Policy;
import com.infy.newgen.enums.PolicyStatus;
import com.infy.newgen.enums.PolicyType;
import com.infy.newgen.enums.Role;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.repository.PolicyRepository;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PolicyServiceImpl policyService;

    @Spy
    private ModelMapper modelMapper;

    @Spy
    private Environment env;

    @Test
    void purchasePolicyValidTest() throws NewGenException {
        PurchasePolicyDTO dto = new PurchasePolicyDTO();
        dto.setPolicyTerm(5);
        dto.setPolicyStartDate(LocalDate.now());
        dto.setNominee("Srinivas Keshav");
        dto.setPolicyType(PolicyType.LIFE_INSURANCE);
        dto.setPremiumAmount(1000);
        Policy savedPolicy = modelMapper.map(dto, Policy.class);
        savedPolicy.setPolicyId("POL-101");

        Agent agent = new Agent();
        agent.setAgentId(1);
        agent.setContact("7098654321");
        agent.setEmail("harish123@gmail.com");
        agent.setName("Harish Venkat");
        agent.setPassword("Harish@123");
        agent.setRole(Role.AGENT);

        Customer customer = new Customer();
        customer.setAgent(agent);
        customer.setContact("6098754321");
        customer.setName("Jeeva Shankar");
        customer.setPassword("Jeeva@123");
        customer.setEmail("jeeva12@gmail.com");
        customer.setRole(Role.CUSTOMER);
        customer.setCustomerId(1);

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer));
        when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);
        when(policyRepository.findTopByOrderByPolicyIdDesc()).thenReturn(savedPolicy);
        String result = policyService.purchasePolicy(dto, 1);

        assertEquals("POL-101", result);

        verify(customerRepository).findById(1);
        verify(policyRepository).save(any(Policy.class));

    }

    @Test
    void purchasePolicyInvalidTestCustomerNotFound() {
        PurchasePolicyDTO dto = new PurchasePolicyDTO();

        when(customerRepository.findById(101)).thenReturn(Optional.empty());

        NewGenException exception = assertThrows(NewGenException.class, () -> policyService.purchasePolicy(dto, 101));
        assertEquals("Service.CUSTOMER_NOT_FOUND", exception.getMessage());
        verify(policyRepository, never()).save(any());
    }

    @Test
    void purchasePolicyInvalidTestSaveFails() {
        PurchasePolicyDTO dto = new PurchasePolicyDTO();
        dto.setPolicyStartDate(LocalDate.now());
        dto.setPolicyTerm(5);

        Customer customer = new Customer();
        Policy p = new Policy();
        p.setPolicyId("POL-101");
        when(customerRepository.findById(101)).thenReturn(Optional.of(customer));
        when(policyRepository.save(any())).thenThrow(new RuntimeException());
        when(policyRepository.findTopByOrderByPolicyIdDesc()).thenReturn(p);
        assertThrows(RuntimeException.class, () -> policyService.purchasePolicy(dto, 101));
    }

    @Test
    void renewPolicyValidTest1() {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL-101");
        dto.setPolicyStatus(PolicyStatus.LAPSED);
        Policy policy = new Policy();
        policy.setPolicyId("POL-101");
        policy.setLastPremiumPaymentDate(LocalDate.now().minusDays(40));
        when(policyRepository.findById("POL-101")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        String result = policyService.renewPolicy(dto);

        assertEquals("POL-101", result);
        assertEquals(PolicyStatus.ACTIVE, policy.getPolicyStatus());
        verify(policyRepository).save(policy);
    }

    @Test
    void renewPolicyValidTest2() {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL-101");
        dto.setPolicyStatus(PolicyStatus.ACTIVE);
        Policy policy = new Policy();
        policy.setPolicyId("POL-101");
        policy.setLastPremiumPaymentDate(LocalDate.now().minusDays(10));
        when(policyRepository.findById("POL-101")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        String result = policyService.renewPolicy(dto);

        assertEquals("POL-101", result);
        assertEquals(PolicyStatus.ACTIVE, policy.getPolicyStatus());
        verify(policyRepository).save(policy);
    }

    @Test
    void renewPolicyInvalidTestPolicyNotFound() {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL-101");

        when(policyRepository.findById("POL-101")).thenReturn(Optional.empty());

        NewGenException exception = assertThrows(NewGenException.class, () -> policyService.renewPolicy(dto));
        assertEquals("Service.POLICY_NOT_FOUND", exception.getMessage());
        verify(policyRepository, never()).save(any());
    }

    @Test
    void renewPolicyInvalidTestSaveFails() {
        RenewPolicyDTO dto = new RenewPolicyDTO();
        dto.setPolicyId("POL-101");

        Policy policy = new Policy();
        policy.setLastPremiumPaymentDate(LocalDate.now().minusDays(40));
        when(policyRepository.findById("POL-101")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any())).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> policyService.renewPolicy(dto));
    }

    @Test
    void calculatePolicystatusValidActive() {
        Policy policy = new Policy();
        policy.setLastPremiumPaymentDate(LocalDate.now().minusDays(10));
        PolicyStatus status = policyService.calculatePolicyStatus(policy);
        assertEquals(PolicyStatus.ACTIVE, status);
    }

    @Test
    void calculatePolicyStatusValidLapsed() {
        Policy policy = new Policy();
        policy.setLastPremiumPaymentDate(LocalDate.now().minusDays(40));

        PolicyStatus status = policyService.calculatePolicyStatus(policy);

        assertEquals(PolicyStatus.LAPSED, status);
    }

}