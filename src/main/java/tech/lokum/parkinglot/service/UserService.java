package tech.lokum.parkinglot.service;

import tech.lokum.parkinglot.dto.RegisterRequest;
import tech.lokum.parkinglot.dto.RegisterResponse;


public interface UserService {
    public RegisterResponse registerUser(RegisterRequest request);
}
