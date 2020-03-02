package com.resolve.sso.services;

import org.springframework.stereotype.Service;

import com.resolve.sso.entities.user.UserAuthenticationRequest;

@Service
public interface AuthenticationService {

	public boolean authenticateUser(UserAuthenticationRequest authRequest);
}
