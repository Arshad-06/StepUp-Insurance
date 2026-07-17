package com.infy.newgen.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.infy.newgen.dto.AgentLoginDTO;
import com.infy.newgen.dto.AgentRegistrationDTO;
import com.infy.newgen.dto.CustomerResponseDTO;
import com.infy.newgen.dto.DashboardDTO;
import com.infy.newgen.dto.PolicyResponseDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.entity.Policy;
import com.infy.newgen.enums.Role;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.repository.PolicyRepository;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {
    @InjectMocks
    private AgentServiceImpl serviceImpl;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomerService customerService;

    @Test
    void registerAgentValidTest() throws NewGenException {
        AgentRegistrationDTO dto = new AgentRegistrationDTO();
        dto.setName("John");
        dto.setEmail("john@newgen.com");
        dto.setContact("9876123456");
        dto.setPassword("Abc@1234");
        dto.setRole(Role.AGENT);

        Agent agent = new Agent();
        agent.setAgentId(101);

        when(agentRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(modelMapper.map(dto, Agent.class)).thenReturn(agent);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("passwordencoded");
        when(agentRepository.save(agent)).thenReturn(agent);

        Integer result = serviceImpl.registerAgent(dto);
        Assertions.assertEquals(101, result);
        verify(agentRepository).save(agent);

    }

    @Test
    void registerAgentInvalidTest() throws NewGenException {
        AgentRegistrationDTO dto = new AgentRegistrationDTO();
        dto.setEmail("john@gmail.com");
        when(agentRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Agent()));
        NewGenException exception = Assertions.assertThrows(NewGenException.class,
                () -> serviceImpl.registerAgent(dto));
        Assertions.assertEquals("Service.AGENT_ALREADY_EXISTS", exception.getMessage());

    }

    @Test
    void loginAgentValidTest() throws NewGenException {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("john@newgen.com");
        dto.setPassword("Abc@1234");
        dto.setRole(Role.AGENT);

        Agent agent = new Agent();
        agent.setAgentId(101);
        agent.setPassword("passwordencoded");
        agent.setRole(Role.AGENT);

        when(agentRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(agent));
        when(passwordEncoder.matches(dto.getPassword(), agent.getPassword())).thenReturn(true);
        Integer result = serviceImpl.loginAgent(dto);
        Assertions.assertEquals(101, result);

    }

    @Test
    void loginAgentAgentInvalidTest() throws NewGenException {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("abc@newgen.com");
        when(agentRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        NewGenException exception = Assertions.assertThrows(NewGenException.class, () -> serviceImpl.loginAgent(dto));
        Assertions.assertEquals("Service.AGENT_NOT_FOUND", exception.getMessage());
    }

    @Test
    void loginAgentPasswordInvalidTest() throws NewGenException {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("john@newgen.com");
        dto.setPassword("abc");
        dto.setRole(Role.AGENT);

        Agent agent = new Agent();
        agent.setPassword("encoded");
        agent.setRole(Role.AGENT);

        when(agentRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(agent));

        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        NewGenException exception = Assertions.assertThrows(NewGenException.class, () -> serviceImpl.loginAgent(dto));
        Assertions.assertEquals("Service.INVALID_PASSWORD", exception.getMessage());
    }

    @Test
    void loginAgentRoleInvalidTest() throws NewGenException {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("john@newgen.com");
        dto.setPassword("Abc@1234");
        dto.setRole(Role.CUSTOMER);

        Agent agent = new Agent();
        agent.setEmail("john@newgen.com");
        agent.setPassword("passwordencoded");
        agent.setRole(Role.AGENT);

        when(agentRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(agent));

        when(passwordEncoder.matches(dto.getPassword(), agent.getPassword())).thenReturn(true);

        NewGenException exception = Assertions.assertThrows(NewGenException.class, () -> serviceImpl.loginAgent(dto));
        Assertions.assertEquals("Service.INVALID_ROLE", exception.getMessage());

    }

    @Test
    void getDashboardValidTest() throws NewGenException {
        Integer agentId = 1;
        Agent agent = new Agent();
        agent.setAgentId(1);

        Customer customer = new Customer();
        customer.setCustomerId(101);
        customer.setName("Mike");
        customer.setEmail("Mike@gmail.com");
        customer.setContact("9876543210");
        customer.setAgent(agent);

        Policy policy = new Policy();
        policy.setPolicyId("P101");
        policy.setCustomer(customer);
        policy.setPremiumAmount(10000.0);
        policy.setPolicyStartDate(LocalDate.now().minusDays(10));
        policy.setPolicyEndDate(LocalDate.now().plusDays(10));

        PolicyResponseDTO policyDTO = new PolicyResponseDTO();
        policyDTO.setPolicyId("P101");

        CustomerResponseDTO customerRDTO = new CustomerResponseDTO();
        customerRDTO.setCustomerId(101);
        customerRDTO.setName("Mike");

        when(customerRepository.findByAgentAgentId(agentId)).thenReturn(List.of(customer));

        when(policyRepository.countPoliciesInCurrentYear(agentId)).thenReturn(1L);

        when(policyRepository.sumCurrentYearPremium(agentId)).thenReturn(10000.0);

        when(modelMapper.map(customer, CustomerResponseDTO.class)).thenReturn(customerRDTO);

        when(customerService.getPolicies(101)).thenReturn(List.of(policyDTO));

        DashboardDTO result = serviceImpl.getDashboard(agentId);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalCustomers());
        Assertions.assertEquals(1L, result.getTotalPoliciesCurrentYear());
        Assertions.assertEquals(500.0, result.getAnnualProfit());
        Assertions.assertEquals(1, result.getCustomers().size());

        verify(customerRepository).findByAgentAgentId(agentId);
        verify(policyRepository).countPoliciesInCurrentYear(agentId);
        verify(policyRepository).sumCurrentYearPremium(agentId);
        verify(customerService).getPolicies(101);

    }

    @Test
    void getDashboardCustomerInvalidTest() throws NewGenException {
        Integer agentId = 1;
        when(customerRepository.findByAgentAgentId(agentId)).thenReturn(Collections.emptyList());

        NewGenException exception = Assertions.assertThrows(NewGenException.class,
                () -> serviceImpl.getDashboard(agentId));
        Assertions.assertEquals("Service.NO_CUSTOMERS_FOUND", exception.getMessage());

        verify(customerRepository).findByAgentAgentId(agentId);
    }
}