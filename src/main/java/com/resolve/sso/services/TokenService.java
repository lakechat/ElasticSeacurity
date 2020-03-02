package com.resolve.sso.services;

import org.elasticsearch.client.security.CreateTokenResponse;

import com.resolve.sso.entities.user.UserAuthenticationRequest;

public interface TokenService {
	
	public CreateTokenResponse createAccessToken(UserAuthenticationRequest authRequest);
	public CreateTokenResponse createPasswordGrantToken(String userName,String password);
	public CreateTokenResponse refreshToken(String token);

}
