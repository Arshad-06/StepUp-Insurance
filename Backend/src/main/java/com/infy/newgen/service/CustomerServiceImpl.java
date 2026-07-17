package com.infy.newgen.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service(value = "customerService")
@Transactional
@CacheConfig(cacheNames = "customerCache")
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Integer registerCustomer(CustomerRegistrationDTO registerDTO) throws NewGenException {
        Optional<Agent> optionalAgent = agentRepository.findById(registerDTO.getAgentId());

        if (optionalAgent.isEmpty()) {
            throw new NewGenException("Service.AGENT_NOT_FOUND");
        }

        Optional<Customer> optionalCustomer = customerRepository.findByEmail(registerDTO.getEmail());

        if (optionalCustomer.isPresent()) {
            throw new NewGenException("Service.CUSTOMER_ALREADY_EXISTS");
        }
        Customer customer = modelMapper.map(registerDTO, Customer.class);
        customer.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        customer.setRole(Role.CUSTOMER);
        customer.setAgent(optionalAgent.get());
        customer.setEmail(registerDTO.getEmail().trim().toLowerCase());
        return customerRepository.save(customer).getCustomerId();
    }

    @Override
    public Integer loginCustomer(CustomerLoginDTO dto) throws NewGenException {
        Optional<Customer> optionalCustomer = customerRepository.findByEmail(dto.getEmail());

        Customer customer = optionalCustomer.orElseThrow(() -> new NewGenException("Service.CUSTOMER_NOT_FOUND"));

        if (!passwordEncoder.matches(dto.getPassword(), customer.getPassword())) {
            throw new NewGenException("Service.INVALID_PASSWORD");
        }
        if (!dto.getRole().equals(Role.CUSTOMER)) {
            throw new NewGenException("Service.INVALID_ROLE");
        }

        return customer.getCustomerId();
    }

    @Override
    public List<PolicyResponseDTO> getPolicies(Integer customerId) throws NewGenException {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);

        if (optionalCustomer.isEmpty()) {
            throw new NewGenException("Service.CUSTOMER_NOT_FOUND");
        }

        List<Policy> policies = policyRepository.findByCustomerCustomerId(customerId);

        policies.stream().forEach(p -> policyService.calculatePolicyStatus(p));

        return policies.stream().map(p -> modelMapper.map(p, PolicyResponseDTO.class)).toList();
    }
}