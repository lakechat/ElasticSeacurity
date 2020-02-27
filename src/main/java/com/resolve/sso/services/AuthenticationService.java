package com.resolve.sso.services;

import com.resolve.sso.entities.user.UserAuthenticationRequest;

public interface AuthenticationService {

	public boolean authenticateUser(UserAuthenticationRequest authRequest);
}
