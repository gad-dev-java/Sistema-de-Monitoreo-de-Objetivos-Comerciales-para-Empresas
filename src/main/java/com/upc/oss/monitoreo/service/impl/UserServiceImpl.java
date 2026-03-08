package com.upc.oss.monitoreo.service.impl;

import com.upc.oss.monitoreo.dto.UserDto;
import com.upc.oss.monitoreo.dto.request.CreateUserRequest;
import com.upc.oss.monitoreo.entities.Company;
import com.upc.oss.monitoreo.entities.User;
import com.upc.oss.monitoreo.exception.CompanyNotFoundException;
import com.upc.oss.monitoreo.repository.CompanyRepository;
import com.upc.oss.monitoreo.repository.UserRepository;
import com.upc.oss.monitoreo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Override
    public UserDto createUser(CreateUserRequest request) {
        Company companyFound = companyRepository.findByNameIgnoreCase(request.companyName())
                .orElseThrow(() -> new CompanyNotFoundException("Company not found with name " + request.companyName()));

        User userToSave = User.builder()
                .name(request.email())
                .email(request.email())
                .password(request.password())
                .role("USER")
                .company(companyFound)
                .build();

        User userSaved = userRepository.save(userToSave);

        return UserDto.builder()
                .idUser(userSaved.getIdUser())
                .name(userSaved.getName())
                .email(userSaved.getEmail())
                .role(userSaved.getRole())
                .companyName(userSaved.getCompany().getName())
                .companyStatus(userSaved.getCompany().getStatus())
                .build();
    }
}
