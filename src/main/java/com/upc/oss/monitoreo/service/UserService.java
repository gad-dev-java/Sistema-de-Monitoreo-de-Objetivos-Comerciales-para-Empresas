package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.UserDto;
import com.upc.oss.monitoreo.dto.request.CreateUserRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {
    UserDto createUser(CreateUserRequest request);
}
