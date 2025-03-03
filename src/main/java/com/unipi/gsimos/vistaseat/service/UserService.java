package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;

public interface UserService {

    NewUserDto createUser(NewUserDto newUserDto);
}
