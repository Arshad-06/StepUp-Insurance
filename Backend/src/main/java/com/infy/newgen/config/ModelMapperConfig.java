package com.infy.newgen.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.infy.newgen.dto.CustomerRegistrationDTO;
import com.infy.newgen.entity.Customer;

@Configuration
public class ModelMapperConfig {
    @Bean
    ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(CustomerRegistrationDTO.class, Customer.class)
                .addMappings(mapper -> mapper.skip(Customer::setCustomerId));
        return modelMapper;
    }
}
