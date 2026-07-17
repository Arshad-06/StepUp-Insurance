package com.infy.newgen.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.newgen.dto.AgentLoginDTO;
import com.infy.newgen.dto.AgentRegistrationDTO;
import com.infy.newgen.dto.CustomerResponseDTO;
import com.infy.newgen.dto.DashboardDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.enums.Role;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.repository.PolicyRepository;

@Service(value = "agentService")
@Transactional
@CacheConfig(cacheNames = "agentCache")
public class AgentServiceImpl implements AgentService {
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerService customerService;

    @Override
    public Integer registerAgent(AgentRegistrationDTO dto) throws NewGenException {

        Optional<Agent> optionalAgent = agentRepository.findByEmail(dto.getEmail());
        if (optionalAgent.isPresent())
            throw new NewGenException("Service.AGENT_ALREADY_EXISTS");
        Agent agent = modelMapper.map(dto, Agent.class);
        agent.setPassword(passwordEncoder.encode(dto.getPassword()));
        agent.setEmail(dto.getEmail().trim().toLowerCase());
        return agentRepository.save(agent).getAgentId();
    }

    @Override
    public Integer loginAgent(AgentLoginDTO dto) throws NewGenException {

        Optional<Agent> optionalAgent = agentRepository.findByEmail(dto.getEmail());
        Agent agent = optionalAgent.orElseThrow(() -> new NewGenException("Service.AGENT_NOT_FOUND"));
        if (!passwordEncoder.matches(dto.getPassword(), agent.getPassword())) {
            throw new NewGenException("Service.INVALID_PASSWORD");
        }
        if (!dto.getRole().equals(Role.AGENT)) {
            throw new NewGenException("Service.INVALID_ROLE");
        }
        return agent.getAgentId();
    }

    @Override
    public DashboardDTO getDashboard(Integer agentId) throws NewGenException {
        List<Customer> customers = customerRepository.findByAgentAgentId(agentId);
        if (customers.isEmpty())
            throw new NewGenException("Service.NO_CUSTOMERS_FOUND");
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalCustomers(customers.size());
        Long currYearPolicyCount = policyRepository.countPoliciesInCurrentYear(agentId);

        dto.setTotalPoliciesCurrentYear(currYearPolicyCount);

        dto.setAnnualProfit(policyRepository.sumCurrentYearPremium(agentId) * 0.05);

        dto.setCustomers(
                customers.stream()
                        .map(c1 -> {
                            CustomerResponseDTO c2 = modelMapper.map(c1, CustomerResponseDTO.class);
                            c2.setPolicies(customerService.getPolicies(c1.getCustomerId()));
                            return c2;
                        })
                        .toList());
        return dto;

    }
}