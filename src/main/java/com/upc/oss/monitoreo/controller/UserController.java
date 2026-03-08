package com.upc.oss.monitoreo.controller;

import com.upc.oss.monitoreo.dto.UserDto;
import com.upc.oss.monitoreo.dto.request.CreateUserRequest;
import com.upc.oss.monitoreo.dto.response.DataResponse;
import com.upc.oss.monitoreo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<DataResponse<UserDto>> createUser(@RequestBody CreateUserRequest request) {
        UserDto userDto = userService.createUser(request);

        DataResponse<UserDto> response = DataResponse.<UserDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("User created successfully")
                .data(userDto)
                .timestamp(LocalDateTime.now())
                .build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userDto.idUser())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }
}
