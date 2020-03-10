package com.resolve.sso.entities.user;

import org.elasticsearch.client.security.CreateTokenResponse;

public class UserAuthenticationResult {
	
	private String access_token;
	private String token_type;
	private long expires_in;
	private String realm;
	private String error;
	private String error_description;
	private String refreshToken;
	private String key;
	
	public UserAuthenticationResult() {
		
	}
	
	public UserAuthenticationResult(CreateTokenResponse tokenResponse) {
		this.access_token = tokenResponse.getAccessToken();
		this.token_type = tokenResponse.getType();
		this.expires_in = tokenResponse.getExpiresIn().getSeconds();
		this.refreshToken = tokenResponse.getRefreshToken();
	}
	
	public UserAuthenticationResult(String description) {
		// create a result in case of error occurs
		this.error = "true";
		this.error_description = description;
	}
	
	
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getToken_type() {
		return token_type;
	}
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}
	public long getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(long expires_in) {
		this.expires_in = expires_in;
	}
	public String getRealm() {
		return realm;
	}
	public void setRealm(String realm) {
		this.realm = realm;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getError_description() {
		return error_description;
	}
	public void setError_description(String error_description) {
		this.error_description = error_description;
	}
	
	

}
