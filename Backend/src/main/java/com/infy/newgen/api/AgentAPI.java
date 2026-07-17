package com.infy.newgen.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.newgen.dto.AgentLoginDTO;
import com.infy.newgen.dto.AgentRegistrationDTO;
import com.infy.newgen.dto.AuthResponseDTO;
import com.infy.newgen.dto.DashboardDTO;
import com.infy.newgen.dto.PaymentReminderDTO;
import com.infy.newgen.entity.Policy;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.AgentRepository;
import com.infy.newgen.repository.PolicyRepository;
import com.infy.newgen.service.AgentService;
import com.infy.newgen.service.EmailService;
import com.infy.newgen.utility.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/agent")
@CrossOrigin(origins = "http://localhost:4200")
@Validated
public class AgentAPI {
    @Autowired
    private AgentService agentService;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private Environment environment;
    private static final String AGENT = "AGENT";

    @Operation(summary = "Registers The Given Agent In The Database")
    @PostMapping("/register")
    @ApiResponse(responseCode = "201", description = "Agent Added Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<AuthResponseDTO> registerAgent(
            @Valid @RequestBody AgentRegistrationDTO agentRegistrationDTO) {
        Integer agentId = agentService.registerAgent(agentRegistrationDTO);
        String token = jwtUtil.generateToken(agentRegistrationDTO.getEmail(), agentId, AGENT);
        String message = environment.getProperty("API.AGENT_REGISTER_SUCCESS") + agentId;
        AuthResponseDTO responseBody = new AuthResponseDTO(token, agentId, AGENT, message,
                agentRegistrationDTO.getName());
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    @Operation(summary = "Facilitates Agent Login")
    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "Agent Logged In Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<AuthResponseDTO> loginAgent(@Valid @RequestBody AgentLoginDTO agentLoginDTO) {
        Integer agentId = agentService.loginAgent(agentLoginDTO);
        String token = jwtUtil.generateToken(agentLoginDTO.getEmail(), agentId, AGENT);
        String message = environment.getProperty("API.AGENT_LOGIN_SUCCESS") + agentId;
        AuthResponseDTO responseBody = new AuthResponseDTO(token, agentId, AGENT, message,
                agentRepository.findById(agentId).get().getName());
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Fetches Agent Dashboard")
    @GetMapping("/dashboard")
    @ApiResponse(responseCode = "200", description = "Dashboard Fetched Successfully")
    @ApiResponse(responseCode = "400", description = "Invalid Agent ID")
    public ResponseEntity<DashboardDTO> getDashboard(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Integer agentId = jwtUtil.extractAllClaims(token).get("id", Integer.class);
        DashboardDTO dashboard = agentService.getDashboard(agentId);
        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }

    @Operation(summary = "Checks if an agent exists by agent ID (Public Endpoint For UI Validation)")
    @GetMapping("/check/{agentId}")
    @ApiResponse(responseCode = "200", description = "Agent Exists")
    @ApiResponse(responseCode = "400", description = "Invalid Agent ID")
    public ResponseEntity<Boolean> checkAgentExists(@PathVariable Integer agentId) {
        Boolean exists = agentRepository.existsById(agentId);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @Operation(summary = "Sends Payment Reminder To Given Policy's Customer")
    @GetMapping("/remind/{policyId}")
    @ApiResponse(responseCode = "200", description = "Reminder Sent Successfully")
    @ApiResponse(responseCode = "400", description = "Invalid Policy ID")
    public ResponseEntity<String> sendPaymentReminder(@PathVariable String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NewGenException("Service.POLICY_NOT_FOUND"));
        PaymentReminderDTO p = new PaymentReminderDTO();
        p.setCustomerEmail(policy.getCustomer().getEmail());
        p.setCustomerName(policy.getCustomer().getName());
        p.setPolicyType(policy.getPolicyType());
        p.setPolicyId(policy.getPolicyId());
        emailService.sendPaymentReminder(p);
        return new ResponseEntity<>("Reminder Sent Successfully!", HttpStatus.OK);
    }
}
