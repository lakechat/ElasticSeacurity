package com.resolve.sso.services;

import org.springframework.stereotype.Service;

import com.resolve.sso.entities.user.UserAuthenticationRequest;
import com.resolve.sso.entities.user.UserAuthenticationResult;

@Service
public interface UserService {

	public UserAuthenticationResult authenticateUser(UserAuthenticationRequest authRequest);
}
