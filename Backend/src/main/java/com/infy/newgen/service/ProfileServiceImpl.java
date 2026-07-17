package com.infy.newgen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.newgen.dto.ProfileResponseDTO;
import com.infy.newgen.dto.UpdateProfileDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.utility.JwtUtil;

@Service(value = "profileService")
@Transactional
public class ProfileServiceImpl implements ProfileService{

   @Autowired
   private CustomerRepository customerRepository;
   @Autowired
   private AgentRepository agentRepository;
   @Autowired
   private PasswordEncoder passwordEncoder;
   @Autowired
   private JwtUtil jwtUtil;
   private static final String CUSTOMER = "CUSTOMER";
   private static final String AGENT = "AGENT";
   private static final String CUSTOMER_NOT_FOUND = "Service.CUSTOMER_NOT_FOUND";
   private static final String AGENT_NOT_FOUND = "Service.AGENT_NOT_FOUND";
   
   @Override
   public ProfileResponseDTO getProfileByEmail(String email, String role) {
       ProfileResponseDTO response = new ProfileResponseDTO();
       if (role.contains(CUSTOMER)) {
           Customer customer = customerRepository.findByEmail(email)
                   .orElseThrow(() -> new NewGenException(CUSTOMER_NOT_FOUND));
           response.setName(customer.getName());
           response.setEmail(customer.getEmail());
           response.setContact(customer.getContact());
           response.setRole(CUSTOMER);
           response.setCustomerId(customer.getCustomerId());
           response.setAgentId(customer.getAgent().getAgentId());
           String newToken = jwtUtil.generateToken(customer.getEmail(), customer.getCustomerId(), role);
           response.setToken(newToken);
       }
   
       else if (role.contains(AGENT)) {

           Agent agent = agentRepository.findByEmail(email)
                   .orElseThrow(() -> new NewGenException(AGENT_NOT_FOUND));
           response.setName(agent.getName());
           response.setEmail(agent.getEmail());
           response.setContact(agent.getContact());
           response.setRole(AGENT);
           response.setAgentId(agent.getAgentId());
           String newToken = jwtUtil.generateToken(agent.getEmail(), agent.getAgentId(), role);
           response.setToken(newToken);
       }
       return response;
   }

   @Override
   public ProfileResponseDTO updateProfile(Integer id, String email, String role, UpdateProfileDTO dto) {
   if(email == null) {
           if (role.contains(CUSTOMER)) {
               Customer customer = customerRepository.findById(id)
                       .orElseThrow(() -> new NewGenException(CUSTOMER_NOT_FOUND));
               updateCustomerFields(customer, dto);
               customerRepository.save(customer);
               email = customer.getEmail();
           }
           else if (role.contains(AGENT)) {
               Agent agent = agentRepository.findById(id)
                       .orElseThrow(() -> new NewGenException(AGENT_NOT_FOUND));
               updateAgentFields(agent, dto);
               agentRepository.save(agent);
               email = agent.getEmail();

           }
   }
   else if(id == null) {
           if (role.contains(CUSTOMER)) {
               Customer customer = customerRepository.findByEmail(email)
                       .orElseThrow(() -> new NewGenException(CUSTOMER_NOT_FOUND));
               updateCustomerFields(customer, dto);
               customerRepository.save(customer);
           }
           else if (role.contains(AGENT)) {
               Agent agent = agentRepository.findByEmail(email)
                       .orElseThrow(() -> new NewGenException(AGENT_NOT_FOUND));
               updateAgentFields(agent, dto);
               agentRepository.save(agent);
           }
   }
       return getProfileByEmail(email, role);

   }

   private void updateCustomerFields(Customer customer, UpdateProfileDTO dto) {

       if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
           customer.setName(dto.getName());
       }

       if (dto.getContact() != null && !dto.getContact().trim().isEmpty()) {
           customer.setContact(dto.getContact());
       }

       if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
           customer.setEmail(dto.getEmail());
       }

       if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
           customer.setPassword(passwordEncoder.encode(dto.getPassword()));
       }
   }

   private void updateAgentFields(Agent agent, UpdateProfileDTO dto) {
       if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
           agent.setName(dto.getName());
       }

       if (dto.getContact() != null && !dto.getContact().trim().isEmpty()) {
           agent.setContact(dto.getContact());
       }

       if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
           agent.setEmail(dto.getEmail());
       }

       if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
           agent.setPassword(passwordEncoder.encode(dto.getPassword()));
       }
   }
}