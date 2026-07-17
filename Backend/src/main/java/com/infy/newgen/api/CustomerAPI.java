package com.infy.newgen.api;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.newgen.dto.AuthResponseDTO;
import com.infy.newgen.dto.CustomerLoginDTO;
import com.infy.newgen.dto.CustomerRegistrationDTO;
import com.infy.newgen.dto.PolicyResponseDTO;
import com.infy.newgen.exception.NewGenException;
import com.infy.newgen.repository.CustomerRepository;
import com.infy.newgen.service.CustomerService;
import com.infy.newgen.utility.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "http://localhost:4200")
public class CustomerAPI {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Environment env;

    private static final String CUSTOMER = "CUSTOMER";

    @Operation(summary = "Registers The Given Customer In The Database")
    @PostMapping("/register")
    @ApiResponse(responseCode = "201", description = "Customer Added Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<AuthResponseDTO> registerCustomer(@RequestBody @Valid CustomerRegistrationDTO registerDTO)
            throws NewGenException {
        Integer customerId = customerService.registerCustomer(registerDTO);
        String token = jwtUtil.generateToken(registerDTO.getEmail(), customerId, CUSTOMER);
        @Nullable
        String message = env.getProperty("API.CUSTOMER_REGISTER_SUCCESS") + customerId;
        AuthResponseDTO responseBody = new AuthResponseDTO(token, customerId, CUSTOMER, message, registerDTO.getName());
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    @Operation(summary = "Facilitates Customer Login")
    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "Customer Logged In Successfully")
    @ApiResponse(responseCode = "400", description = "Error In Data Validation")
    public ResponseEntity<AuthResponseDTO> loginCustomer(@RequestBody @Valid CustomerLoginDTO loginDTO)
            throws NewGenException {
        Integer customerId = customerService.loginCustomer(loginDTO);
        String token = jwtUtil.generateToken(loginDTO.getEmail(), customerId, CUSTOMER);
        @Nullable
        String message = env.getProperty("API.CUSTOMER_LOGIN_SUCCESS") + customerId;
        AuthResponseDTO responseBody = new AuthResponseDTO(token, customerId, CUSTOMER, message,
                customerRepository.findById(customerId).get().getName());
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Fetches Customer Policies")
    @GetMapping("/policies")
    @ApiResponse(responseCode = "200", description = "Policies Fetched Successfully")
    @ApiResponse(responseCode = "400", description = "Invalid Customer ID")
    public ResponseEntity<List<PolicyResponseDTO>> getPolicies(@RequestHeader("Authorization") String authHeader)
            throws NewGenException {
        String token = authHeader.substring(7);
        Integer customerId = jwtUtil.extractAllClaims(token).get("id", Integer.class);
        List<PolicyResponseDTO> policies = customerService.getPolicies(customerId);

        return new ResponseEntity<>(policies, HttpStatus.OK);
    }
}
