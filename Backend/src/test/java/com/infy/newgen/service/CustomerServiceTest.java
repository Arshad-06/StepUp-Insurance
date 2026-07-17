package com.infy.newgen.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.infy.newgen.dto.CustomerLoginDTO;
import com.infy.newgen.dto.CustomerRegistrationDTO;
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
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Spy
    private PolicyService policyService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TypeMap<CustomerRegistrationDTO, Customer> typeMap;

    @Mock
    private PasswordEncoder passwordEncoder;

    CustomerRegistrationDTO dto = new CustomerRegistrationDTO();
    CustomerLoginDTO loginDTO = new CustomerLoginDTO();
    PolicyResponseDTO policyDTO = new PolicyResponseDTO();

    // Testing RegisterCustomer Method

    @Test
    void registerCustomerValid() throws NewGenException {
        dto.setAgentId(2);
        dto.setName("Kiran");
        dto.setEmail("kiran@gmail.com");
        dto.setPassword("KiraN@123");
        dto.setContact("8923462163");
        dto.setRole(Role.CUSTOMER);

        Agent a = new Agent();
        a.setAgentId(dto.getAgentId());

        Customer c = new Customer();
        c.setCustomerId(11);

        Mockito.when(agentRepository.findById(dto.getAgentId())).thenReturn(Optional.of(a));

        Mockito.when(customerRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        Mockito.when(modelMapper.map(dto, Customer.class)).thenReturn(c);

        Mockito.when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        Mockito.when(customerRepository.save(c)).thenReturn(c);

        Integer customerId = customerService.registerCustomer(dto);

        Assertions.assertEquals(11, customerId);
    }

    @Test
    void registerCustomerInValidAgentId() throws NewGenException {
        dto.setAgentId(11);

        Agent a = new Agent();
        a.setAgentId(dto.getAgentId());

        Mockito.when(agentRepository.findById(dto.getAgentId())).thenReturn(Optional.empty());

        NewGenException e = Assertions.assertThrows(NewGenException.class, () -> customerService.registerCustomer(dto));

        Assertions.assertEquals("Service.AGENT_NOT_FOUND", e.getMessage());
    }

    @Test
    void registerCustomerInValidCustomerExists() throws NewGenException {
        dto.setEmail("kiran@gmail.com");
        Customer c = new Customer();

        Agent a = new Agent();

        Mockito.when(agentRepository.findById(dto.getAgentId())).thenReturn(Optional.of(a));

        Mockito.when(customerRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(c));

        NewGenException e = Assertions.assertThrows(NewGenException.class, () -> customerService.registerCustomer(dto));

        Assertions.assertEquals("Service.CUSTOMER_ALREADY_EXISTS", e.getMessage());
    }

    // Testing loginCustomer Method

    @Test
    void loginCustomerValidTest() throws NewGenException {
        loginDTO.setEmail("kiran@gmail.com");
        loginDTO.setPassword("KiraN@123");
        loginDTO.setRole(Role.CUSTOMER);

        Customer c = new Customer();
        c.setEmail("kiran@gmail.com");
        c.setCustomerId(11);
        c.setPassword("KiraN@123");
        c.setRole(Role.CUSTOMER);

        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(c));

        Mockito.when(passwordEncoder.matches(loginDTO.getPassword(), c.getPassword())).thenReturn(true);

        Integer customerId = customerService.loginCustomer(loginDTO);

        Assertions.assertEquals(11, customerId);
    }

    @Test
    void loginCustomerInValidCustomer() throws NewGenException {
        loginDTO.setEmail("kiran@gmail.com");

        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.empty());

        NewGenException e = Assertions.assertThrows(NewGenException.class,
                () -> customerService.loginCustomer(loginDTO));

        Assertions.assertEquals("Service.CUSTOMER_NOT_FOUND", e.getMessage());
    }

    @Test
    void loginCustomerInValidPassword() throws NewGenException {
        loginDTO.setEmail("kiran@gmail.com");
        Customer c = new Customer();

        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(c));

        Mockito.when(passwordEncoder.matches(loginDTO.getPassword(), c.getPassword())).thenReturn(false);

        NewGenException e = Assertions.assertThrows(NewGenException.class,
                () -> customerService.loginCustomer(loginDTO));

        Assertions.assertEquals("Service.INVALID_PASSWORD", e.getMessage());
    }

    @Test
    void loginCustomerInValidRole() throws NewGenException {
        loginDTO.setEmail("kiran@gmail.com");
        loginDTO.setPassword("KiraN@123");
        loginDTO.setRole(Role.AGENT);
        Customer c = new Customer();
        c.setPassword("encodedPassword");

        Mockito.when(customerRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(c));

        Mockito.when(passwordEncoder.matches(loginDTO.getPassword(), c.getPassword())).thenReturn(true);

        NewGenException e = Assertions.assertThrows(NewGenException.class,
                () -> customerService.loginCustomer(loginDTO));

        Assertions.assertEquals("Service.INVALID_ROLE", e.getMessage());
    }

    // Testing getPolicies Method

    @Test
    void getPoliciesValidTest() throws NewGenException {

        Customer c = new Customer();
        c.setCustomerId(1);

        Policy p = new Policy();

        List<Policy> policies = List.of(p);

        Mockito.when(customerRepository.findById(c.getCustomerId())).thenReturn(Optional.of(c));

        Mockito.when(policyRepository.findByCustomerCustomerId(c.getCustomerId())).thenReturn(policies);

        Mockito.when(modelMapper.map(p, PolicyResponseDTO.class)).thenReturn(policyDTO);

        List<PolicyResponseDTO> list = customerService.getPolicies(1);

        Assertions.assertEquals(1, list.size());

    }

    @Test
    void getPoliciesInValidTest() throws NewGenException {
        Customer c = new Customer();
        c.setCustomerId(1);
        Mockito.when(customerRepository.findById(1)).thenReturn(Optional.empty());

        NewGenException e = Assertions.assertThrows(NewGenException.class, () -> {
            customerService.getPolicies(1);
        });

        Assertions.assertEquals("Service.CUSTOMER_NOT_FOUND", e.getMessage());
    }

}