package com.upc.oss.monitoreo.service;

import com.upc.oss.monitoreo.dto.UserDto;
import com.upc.oss.monitoreo.dto.request.CreateUserRequest;

public interface UserService {
    UserDto createUser(CreateUserRequest request);
}
