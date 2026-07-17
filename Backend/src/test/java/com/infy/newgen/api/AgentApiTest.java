package com.infy.newgen.api;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.newgen.dto.AgentLoginDTO;
import com.infy.newgen.dto.AgentRegistrationDTO;
import com.infy.newgen.dto.CustomerResponseDTO;
import com.infy.newgen.dto.DashboardDTO;
import com.infy.newgen.dto.PaymentReminderDTO;
import com.infy.newgen.dto.PolicyResponseDTO;
import com.infy.newgen.entity.Agent;
import com.infy.newgen.entity.Customer;
import com.infy.newgen.entity.Policy;
import com.infy.newgen.enums.PolicyType;
import com.infy.newgen.enums.Role;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.PolicyRepository;
import com.infy.newgen.service.AgentService;
import com.infy.newgen.service.EmailService;
import com.infy.newgen.utility.ExceptionControllerAdvice;
import com.infy.newgen.utility.JwtUtil;

import io.jsonwebtoken.Claims;

@WebMvcTest(AgentAPI.class)
@Import(ExceptionControllerAdvice.class)
@TestPropertySource(properties = {
        "API.AGENT_REGISTER_SUCCESS=Agent registered successfully with agent Id:",
        "API.AGENT_LOGIN_SUCCESS=Agent logged in successfully with agent Id:",
        "Service.AGENT_ALREADY_EXISTS=Agent already exists",
        "Service.AGENT_NOT_FOUND=Agent not found",
        "Service.INVALID_PASSWORD=Please enter a valid password",
        "Service.INVALID_ROLE=Please choose a valid role",
        "Service.NO_CUSTOMERS_FOUND=No customers found",
        "Service.NO_POLICIES_FOUND=No policies found",
        "Service.POLICY_NOT_FOUND=Policy not found"
})
class AgentApiTest {
    @Autowired
    MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @MockitoBean
    AgentService agentService;
    @MockitoBean
    EmailService emailService;
    @MockitoBean
    JwtUtil jwtUtil;
    @MockitoBean
    AgentRepository agentRepository;
    @MockitoBean
    PolicyRepository policyRepository;

    @Test
    void agentRegisterSucess() throws Exception {
        AgentRegistrationDTO dto = new AgentRegistrationDTO();
        dto.setName("John");
        dto.setEmail("john@gmail.com");
        dto.setContact("9876543210");
        dto.setPassword("Abcd@1234");
        dto.setRole(Role.AGENT);

        when(agentService.registerAgent(any())).thenReturn(1);

        when(jwtUtil.generateToken(dto.getEmail(), 1, "AGENT")).thenReturn("token123");

        mockMvc.perform(post("/agent/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("AGENT"))
                .andExpect(jsonPath("$.message").value("Agent registered successfully with agent Id:1"))
                .andExpect(jsonPath("$.name").value("John"));

    }

    @Test
    void registerAgentAlreadyExists() throws Exception {
        AgentRegistrationDTO dto = new AgentRegistrationDTO();
        dto.setName("John");
        dto.setEmail("john@gmail.com");
        dto.setContact("9876543210");
        dto.setPassword("Abcd@1234");
        dto.setRole(Role.AGENT);

        when(agentService.registerAgent(any())).thenThrow(new NewGenException("Service.AGENT_ALREADY_EXISTS"));

        mockMvc.perform(post("/agent/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("Agent already exists"));
    }

    @Test
    void loginSucess() throws Exception {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("john@gmail.com");
        dto.setPassword("Password@1");
        dto.setRole(Role.AGENT);

        Agent agent = new Agent();
        agent.setAgentId(1);
        agent.setName("John");

        when(agentService.loginAgent(any())).thenReturn(1);

        when(jwtUtil.generateToken(dto.getEmail(), 1, "AGENT")).thenReturn("token123");

        when(agentRepository.findById(1)).thenReturn(Optional.of(agent));

        mockMvc.perform(post("/agent/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.role").value("AGENT"))
                .andExpect(jsonPath("$.message").value("Agent logged in successfully with agent Id:1"))
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void loginAgentNotFound() throws Exception {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("john@gmail.com");
        dto.setPassword("Password@1");
        dto.setRole(Role.AGENT);

        when(agentService.loginAgent(any())).thenThrow(new NewGenException("Service.AGENT_NOT_FOUND"));

        mockMvc.perform(post("/agent/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("Agent not found"));
    }

    @Test
    void loginAgentInvalidPassword() throws Exception {
        AgentLoginDTO dto = new AgentLoginDTO();
        dto.setEmail("john@gmail.com");
        dto.setPassword("WrongPassword");
        dto.setRole(Role.AGENT);

        when(agentService.loginAgent(any())).thenThrow(new NewGenException("Service.INVALID_PASSWORD"));

        mockMvc.perform(post("/agent/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("Please enter a valid password"));
    }

    @Test
    void dashboardSucess() throws Exception {
        Claims claims = mock(Claims.class);
        PolicyResponseDTO policy = new PolicyResponseDTO();
        policy.setPolicyId("POL-101");
        CustomerResponseDTO customer = new CustomerResponseDTO();
        customer.setName("John");
        customer.setPolicies(List.of(policy));
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalCustomers(1);
        dto.setTotalPoliciesCurrentYear(1L);
        dto.setAnnualProfit(5000.0);
        dto.setCustomers(List.of(customer));

        when(jwtUtil.extractAllClaims("token123")).thenReturn(claims);
        when(claims.get("id", Integer.class)).thenReturn(1);

        when(agentService.getDashboard(1)).thenReturn(dto);
        mockMvc.perform(get("/agent/dashboard").header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(1))
                .andExpect(jsonPath("$.totalPoliciesCurrentYear").value(1))
                .andExpect(jsonPath("$.annualProfit").value(5000.0))
                .andExpect(jsonPath("$.customers[0].name").value("John"))
                .andExpect(jsonPath("$.customers[0].policies[0].policyId").value("POL-101"));

    }

    @Test
    void dashboardNoCustomer() throws Exception {
        Claims claims = mock(Claims.class);
        when(jwtUtil.extractAllClaims("token123")).thenReturn(claims);
        when(claims.get("id", Integer.class)).thenReturn(1);

        when(agentService.getDashboard(1)).thenThrow(new NewGenException("Service.NO_CUSTOMERS_FOUND"));

        mockMvc.perform(get("/agent/dashboard").header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("No customers found"));
    }

    @Test
    void checkAgentExistsSuccess() throws Exception {
        when(agentRepository.existsById(1)).thenReturn(true);

        mockMvc.perform(get("/agent/check/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void checkAgentExistsFailure() throws Exception {
        when(agentRepository.existsById(99)).thenReturn(false);

        mockMvc.perform(get("/agent/check/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    void sendPaymentReminderSuccess() throws Exception {
        Policy p = new Policy();
        p.setPolicyId("POL-123");
        p.setPolicyType(PolicyType.HEALTH_INSURANCE);
        Customer c = new Customer();
        c.setEmail("arshaddpvt@gmail.com");
        c.setName("Arshad");
        p.setCustomer(c);
        when(policyRepository.findById(any())).thenReturn(Optional.of(p));
        doNothing().when(emailService).sendPaymentReminder(any(PaymentReminderDTO.class));
        mockMvc.perform(get("/agent/remind/{policyId}", "POL-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reminder Sent Successfully!"));

        verify(emailService, Mockito.times(1)).sendPaymentReminder(any(PaymentReminderDTO.class));
    }

    @Test
    void sendPaymentReminderPolicyNotFound() throws Exception {
        when(policyRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/agent/remind/{policyId}", "POL123")) // Invalid ID
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(400))
                .andExpect(jsonPath("$.errorMessage").value("Policy not found"));

        verify(emailService, Mockito.never()).sendPaymentReminder(any(PaymentReminderDTO.class));
    }
}