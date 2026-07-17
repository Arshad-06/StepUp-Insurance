package com.infy.newgen.api;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.newgen.dto.CustomerLoginDTO;
import com.infy.newgen.dto.CustomerRegistrationDTO;
import com.infy.newgen.dto.PolicyResponseDTO;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.enums.Role;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.service.CustomerService;
import com.infy.newgen.utility.JwtUtil;

import io.jsonwebtoken.Claims;

@WebMvcTest(CustomerAPI.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "API.CUSTOMER_REGISTER_SUCCESS= Customer registered successfully with Customer ID : ",
        "API.CUSTOMER_LOGIN_SUCCESS= Customer logged in successfully with Customer ID : "
})
class CustomerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerRepository customerRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void registerCustomerValidTest() throws Exception {

        CustomerRegistrationDTO dto = new CustomerRegistrationDTO();
        dto.setEmail("arjun@gmail.com");
        dto.setName("Arjun");
        dto.setPassword("Arjun@123");
        dto.setContact("7583747253");
        dto.setAgentId(2);
        dto.setRole(Role.CUSTOMER);

        Mockito.when(customerService.registerCustomer(ArgumentMatchers.any(CustomerRegistrationDTO.class)))
                .thenReturn(1);
        Mockito.when(jwtUtil.generateToken(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyString()))
                .thenReturn("dummy token");

        mockMvc.perform(MockMvcRequestBuilders.post("/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("CUSTOMER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("dummy token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Customer registered successfully with Customer ID : 1"));
    }

    @Test
    void registerCustomerEmailInValidTest() throws Exception {
        CustomerRegistrationDTO dto = new CustomerRegistrationDTO();
        dto.setEmail("arjun");
        dto.setName("Arjun");
        dto.setPassword("Arjun@123");
        dto.setContact("7583747253");
        dto.setAgentId(2);
        dto.setRole(Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders.post("/customer/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value("Please provide a valid customer email"));
    }

    @Test
    void loginCustomerValidTest() throws Exception {
        CustomerLoginDTO dto = new CustomerLoginDTO();
        dto.setEmail("arjun@gmail.com");
        dto.setPassword("Arjun@123");
        dto.setRole(Role.CUSTOMER);

        Customer c = new Customer();
        c.setName("Arjun");

        Mockito.when(customerService.loginCustomer(ArgumentMatchers.any(CustomerLoginDTO.class)))
                .thenReturn(1);
        Mockito.when(jwtUtil.generateToken(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyString()))
                .thenReturn("dummy token");

        Mockito.when(customerRepository.findById(1)).thenReturn(Optional.of(c));

        mockMvc.perform(MockMvcRequestBuilders.post("/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("CUSTOMER"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("dummy token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Arjun"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Customer logged in successfully with Customer ID : 1"));
    }

    @Test
    void loginCustomerInValidTest() throws Exception {
        CustomerLoginDTO dto = new CustomerLoginDTO();
        dto.setEmail("arjun");
        dto.setPassword("Arjun@123");
        dto.setRole(Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders.post("/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage")
                        .value("Please provide a valid customer email"));
    }

    @Test
    void loginCustomerInValidTestCustomerExists() throws Exception {
        CustomerLoginDTO dto = new CustomerLoginDTO();
        dto.setEmail("arjun@gmail.com");
        dto.setPassword("Arjun@123");
        dto.setRole(Role.CUSTOMER);

        Mockito.when(customerService.loginCustomer(ArgumentMatchers.any(CustomerLoginDTO.class)))
                .thenThrow(new NewGenException("Service.CUSTOMER_NOT_FOUND"));

        mockMvc.perform(MockMvcRequestBuilders.post("/customer/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Customer not found"));
    }

    @Test
    void getPoliciesValidTest() throws Exception {

        Claims claims = Mockito.mock(Claims.class);
        PolicyResponseDTO p = new PolicyResponseDTO();
        List<PolicyResponseDTO> policies = List.of(p);

        Mockito.when(jwtUtil.extractAllClaims("dummyToken"))
                .thenReturn(claims);

        Mockito.when(claims.get("id", Integer.class)).thenReturn(1);

        Mockito.when(customerService.getPolicies(1)).thenReturn(policies);

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/policies")
                .header("Authorization", "Bearer dummyToken"))
                .andExpect(status().isOk());
    }

    @Test
    void getPoliciesInValidTest() throws Exception {
        Claims claims = Mockito.mock(Claims.class);

        Mockito.when(jwtUtil.extractAllClaims("dummyToken"))
                .thenReturn(claims);

        Mockito.when(claims.get("id", Integer.class)).thenReturn(1);

        Mockito.when(customerService.getPolicies(1)).thenThrow(new NewGenException("Service.CUSTOMER_NOT_FOUND"));

        mockMvc.perform(MockMvcRequestBuilders.get("/customer/policies")
                .header("Authorization", "Bearer dummyToken"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorMessage").value("Customer not found"));
    }
}