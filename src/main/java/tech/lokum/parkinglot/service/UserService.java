package tech.lokum.parkinglot.service;

import org.springframework.stereotype.Service;
import tech.lokum.parkinglot.dto.RegisterRequest;
import tech.lokum.parkinglot.dto.RegisterResponse;

@Service
public interface UserService {
    public RegisterResponse registerUser(RegisterRequest request);
}
