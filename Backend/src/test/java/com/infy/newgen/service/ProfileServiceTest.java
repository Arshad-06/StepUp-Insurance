package com.infy.newgen.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.infy.newgen.dto.ProfileResponseDTO;
import com.infy.newgen.dto.UpdateProfileDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.utility.JwtUtil;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AgentRepository agentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ProfileServiceImpl profileService;

    // Testing on getProfileByEmail Method

    @Test
    void getProfileByEmailCustomerValid() throws NewGenException {
        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        when(customerRepository.findByEmail(c.getEmail())).thenReturn(Optional.of(c));
        when(jwtUtil.generateToken(c.getEmail(), c.getCustomerId(), "CUSTOMER")).thenReturn("newtoken");
        ProfileResponseDTO actual = profileService.getProfileByEmail("kiran@gmail.com", "CUSTOMER");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals(1, actual.getAgentId());
        Assertions.assertEquals("newtoken", actual.getToken());
    }

    @Test
    void getProfileByEmailAgentValid() throws NewGenException {
        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        when(agentRepository.findByEmail(a.getEmail())).thenReturn(Optional.of(a));
        when(jwtUtil.generateToken(a.getEmail(), a.getAgentId(), "AGENT")).thenReturn("newtoken");
        ProfileResponseDTO actual = profileService.getProfileByEmail("kiran@gmail.com", "AGENT");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("AGENT", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
        Assertions.assertEquals("newtoken", actual.getToken());
    }

    @Test
    void getProfileByEmailCustomerInvalid() throws NewGenException {

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.empty());

        NewGenException e = assertThrows(NewGenException.class,
                () -> profileService.getProfileByEmail("kiran@gmail.com", "CUSTOMER"));

        assertEquals("Service.CUSTOMER_NOT_FOUND", e.getMessage());

    }

    @Test
    void getProfileByEmailAgentInvalid() throws NewGenException {

        when(agentRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.empty());

        NewGenException e = assertThrows(NewGenException.class,
                () -> profileService.getProfileByEmail("kiran@gmail.com", "AGENT"));

        assertEquals("Service.AGENT_NOT_FOUND", e.getMessage());

    }

    @Test
    void getProfileByEmailRoleInvalid() throws NewGenException {
        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        ProfileResponseDTO actual = profileService.getProfileByEmail("kiran@gmail.com", "INVALID");

        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getCustomerId());
        Assertions.assertNull(actual.getContact());
        Assertions.assertNull(actual.getName());
        Assertions.assertNull(actual.getRole());
        Assertions.assertNull(actual.getEmail());
        Assertions.assertNull(actual.getAgentId());
    }

    // Testing on updateProfile Method for Customer

    @Test
    void updateProfileCustomerValid() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Kiranmayee");
        dto.setEmail("kiranmayee@gmail.com");
        dto.setContact("9485382939");
        dto.setPassword("Password@123");

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(c));

        Mockito.when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        when(customerRepository.save(any(Customer.class))).thenReturn(c);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "CUSTOMER", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("9485382939", actual.getContact());
        Assertions.assertEquals("Kiranmayee", actual.getName());
        Assertions.assertEquals("kiranmayee@gmail.com", actual.getEmail());
        Assertions.assertEquals("encodedPassword", c.getPassword());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileCustomerValidBothEmailId() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Kiranmayee");
        dto.setEmail("kiranmayee@gmail.com");
        dto.setContact("9485382939");
        dto.setPassword("Password@123");

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(c));
        ProfileResponseDTO actual = profileService.updateProfile(2, "kiran@gmail.com", "CUSTOMER", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileCustomerContactOnly() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setContact("9485382939");

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(c));

        when(customerRepository.save(any(Customer.class))).thenReturn(c);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "CUSTOMER", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("9485382939", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileCustomerInvalid1() throws NewGenException {

        UpdateProfileDTO profile = new UpdateProfileDTO();

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.empty());

        NewGenException e = assertThrows(NewGenException.class,
                () -> profileService.updateProfile(null, "kiran@gmail.com", "CUSTOMER", profile));

        assertEquals("Service.CUSTOMER_NOT_FOUND", e.getMessage());

    }

    @Test
    void updateProfileCustomerInvalid2() throws NewGenException {

        UpdateProfileDTO profile = new UpdateProfileDTO();

        when(customerRepository.findById(1)).thenReturn(Optional.empty());

        NewGenException e = assertThrows(NewGenException.class,
                () -> profileService.updateProfile(1, null, "CUSTOMER", profile));

        assertEquals("Service.CUSTOMER_NOT_FOUND", e.getMessage());

    }

    @Test
    void updateProfileCustomerNull() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        UpdateProfileDTO dto = new UpdateProfileDTO();

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(c));

        when(customerRepository.save(any(Customer.class))).thenReturn(c);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "CUSTOMER", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileCustomerEmpty() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setContact("");
        dto.setEmail("");
        dto.setName("");
        dto.setPassword("");

        when(customerRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(c));

        when(customerRepository.save(any(Customer.class))).thenReturn(c);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "CUSTOMER", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    // Testing on updateProfile Method for Agent

    @Test
    void updateProfileAgentValid() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Kiranmayee");
        dto.setEmail("kiranmayee@gmail.com");
        dto.setContact("9485382939");
        dto.setPassword("Password@123");

        when(agentRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(a));

        Mockito.when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        when(agentRepository.save(any(Agent.class))).thenReturn(a);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "AGENT", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals("9485382939", actual.getContact());
        Assertions.assertEquals("Kiranmayee", actual.getName());
        Assertions.assertEquals("kiranmayee@gmail.com", actual.getEmail());
        Assertions.assertEquals("encodedPassword", a.getPassword());
        Assertions.assertEquals("AGENT", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileRoleInvalid1() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Kiranmayee");
        dto.setEmail("kiranmayee@gmail.com");
        dto.setContact("9485382939");
        dto.setPassword("Password@123");

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "INVALID", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getContact());
        Assertions.assertNull(actual.getName());
        Assertions.assertNull(actual.getEmail());
        Assertions.assertNull(a.getPassword());
        Assertions.assertNull(actual.getRole());
        Assertions.assertNull(actual.getAgentId());
    }

    @Test
    void updateProfileRoleInvalid2() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Kiranmayee");
        dto.setEmail("kiranmayee@gmail.com");
        dto.setContact("9485382939");
        dto.setPassword("Password@123");

        ProfileResponseDTO actual = profileService.updateProfile(1, null, "INVALID", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getContact());
        Assertions.assertNull(actual.getName());
        Assertions.assertNull(actual.getEmail());
        Assertions.assertNull(a.getPassword());
        Assertions.assertNull(actual.getRole());
        Assertions.assertNull(actual.getAgentId());
    }

    @Test
    void updateProfileAgentEmailOnly() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("kiranmayee@gmail.com");

        when(agentRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(a));

        when(agentRepository.save(any(Agent.class))).thenReturn(a);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "AGENT", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiranmayee@gmail.com", actual.getEmail());
        Assertions.assertEquals("AGENT", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileAgentNull() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();

        when(agentRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(a));

        when(agentRepository.save(any(Agent.class))).thenReturn(a);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "AGENT", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("AGENT", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileAgentEmpty() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setContact("");
        dto.setEmail("");
        dto.setName("");
        dto.setPassword("");
        when(agentRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.of(a));

        when(agentRepository.save(any(Agent.class))).thenReturn(a);

        ProfileResponseDTO actual = profileService.updateProfile(null, "kiran@gmail.com", "AGENT", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiran@gmail.com", actual.getEmail());
        Assertions.assertEquals("AGENT", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileAgentById() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);
        a.setEmail("kiran@gmail.com");
        a.setName("Kiran");
        a.setContact("8476352626");

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setEmail("kiranmayee@gmail.com");

        when(agentRepository.findById(1)).thenReturn(Optional.of(a));
        when(agentRepository.findByEmail("kiranmayee@gmail.com")).thenReturn(Optional.of(a));
        when(agentRepository.save(any(Agent.class))).thenReturn(a);

        ProfileResponseDTO actual = profileService.updateProfile(1, null, "AGENT", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals("8476352626", actual.getContact());
        Assertions.assertEquals("Kiran", actual.getName());
        Assertions.assertEquals("kiranmayee@gmail.com", actual.getEmail());
        Assertions.assertEquals("AGENT", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileCustomerById() throws NewGenException {

        Agent a = new Agent();
        a.setAgentId(1);

        Customer c = new Customer();
        c.setCustomerId(2);
        c.setEmail("kiran@gmail.com");
        c.setName("Kiran");
        c.setContact("8476352626");
        c.setAgent(a);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setName("Kiranmayee");
        dto.setEmail("kiranmayee@gmail.com");
        dto.setContact("9485382939");
        dto.setPassword("Password@123");

        when(customerRepository.findById(2)).thenReturn(Optional.of(c));
        when(customerRepository.findByEmail("kiranmayee@gmail.com")).thenReturn(Optional.of(c));
        Mockito.when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        when(customerRepository.save(any(Customer.class))).thenReturn(c);

        ProfileResponseDTO actual = profileService.updateProfile(2, null, "CUSTOMER", dto);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.getCustomerId());
        Assertions.assertEquals("9485382939", actual.getContact());
        Assertions.assertEquals("Kiranmayee", actual.getName());
        Assertions.assertEquals("kiranmayee@gmail.com", actual.getEmail());
        Assertions.assertEquals("encodedPassword", c.getPassword());
        Assertions.assertEquals("CUSTOMER", actual.getRole());
        Assertions.assertEquals(1, actual.getAgentId());
    }

    @Test
    void updateProfileAgentInvalid1() throws NewGenException {

        UpdateProfileDTO profile = new UpdateProfileDTO();

        when(agentRepository.findByEmail("kiran@gmail.com")).thenReturn(Optional.empty());

        NewGenException e = assertThrows(NewGenException.class,
                () -> profileService.updateProfile(null, "kiran@gmail.com", "AGENT", profile));

        assertEquals("Service.AGENT_NOT_FOUND", e.getMessage());

    }

    @Test
    void updateProfileAgentInvalid2() throws NewGenException {

        UpdateProfileDTO profile = new UpdateProfileDTO();

        when(agentRepository.findById(1)).thenReturn(Optional.empty());

        NewGenException e = assertThrows(NewGenException.class,
                () -> profileService.updateProfile(1, null, "AGENT", profile));

        assertEquals("Service.AGENT_NOT_FOUND", e.getMessage());

    }
}