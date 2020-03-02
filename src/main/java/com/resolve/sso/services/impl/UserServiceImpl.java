package com.resolve.sso.services.impl;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.security.CreateTokenResponse;
import org.elasticsearch.client.security.GetUsersRequest;
import org.elasticsearch.client.security.GetUsersResponse;
import org.elasticsearch.client.security.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.resolve.sso.entities.user.UserAuthenticationRequest;
import com.resolve.sso.entities.user.UserAuthenticationResult;
import com.resolve.sso.services.AuthenticationService;
import com.resolve.sso.services.TokenService;
import com.resolve.sso.services.UserService;
import com.resolve.sso.utils.UserConstants;
import com.resolve.sso.utils.ElasticSearchHandler;
import com.resolve.sso.utils.JsonUtils;

@Component
public class UserServiceImpl implements UserService{

	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private ElasticSearchHandler esHandler;
	
	@Autowired
	private TokenService tokenService;
	
	public static void main(String[] args) {
		UserServiceImpl usi = new UserServiceImpl();
		Set<User> users = usi.getUser("user3");
		for(User user : users)
			System.out.println(user.getRoles());
			}
	
	public static void testUser() {
		UserServiceImpl  user = new UserServiceImpl();
		Set<User> users = user.getUser(null);
		
		for(User u : users)
			System.out.println(u.getRoles());
		
	}
	
	public static void testToken() {
		UserServiceImpl  user = new UserServiceImpl();
		UserAuthenticationRequest authRequest = new UserAuthenticationRequest();
		authRequest.setUserName("elastic");
		authRequest.setPassword("password");
		authRequest.setRealm("Native");
		user.authenticateUser(authRequest);
	}
	
	public UserServiceImpl() {
		
	}
	
	private Set<User> getUser(String userName) {
		GetUsersRequest request = null;
		try {
			if(StringUtils.isBlank(userName))
				request = new GetUsersRequest();
			else
				request = new GetUsersRequest(userName);
			GetUsersResponse response = esHandler.getHighLevelClient().security()
												 .getUsers(request, RequestOptions.DEFAULT);
			return response.getUsers();

		}catch(Exception e) {
			logger.error("getUser("+userName+") exception: "+e.getMessage());
			return null;
		}
	}

	@Override
	public UserAuthenticationResult authenticateUser(UserAuthenticationRequest authRequest) {
		
		UserAuthenticationResult authResult = null;
		boolean isValidUser = authService.authenticateUser(authRequest);
		if(isValidUser) {
			CreateTokenResponse tokenResponse = tokenService.createAccessToken(authRequest);
			if(tokenResponse != null) {
				try {
					authResult = new UserAuthenticationResult(tokenResponse);
					authResult.setRealm(authRequest.getRealm());
					final String token = authResult.getAccess_token();
					// load up user roles and privileges into Redis if user is verified
					Callable<Void> callable = () -> {
						loadUserPrivileges(authRequest.getUserName(),token);
						return null;
					};
					
				}catch(Exception e) {
					logger.error("creating authResult from tokenResponse exception: "+e.getMessage());
					authResult = new UserAuthenticationResult(e.getMessage());
					authResult.setRealm(authRequest.getRealm());
				}
			}
			System.out.println(JsonUtils.getJson(tokenResponse));
		}else {
			logger.error(UserConstants.INVALID_USER_CREDENTIAL);
			authResult = new UserAuthenticationResult(UserConstants.INVALID_USER_CREDENTIAL);
			authResult.setRealm(authRequest.getRealm());
		}
		return authResult;
	}

	private void loadUserPrivileges(String userName, String accessToken) {
		//1. get roles
		Set<User> users = getUser(userName);
		if(users != null) {
			if( !users.isEmpty()) {
				String[] roles = (String[])users.toArray();
				
				//2. get privileges
				
				//3. save to Redis
			}else {
				logger.error("[loadUserPrivileges], user {} does not own any roles.",userName);
			}
			
			
		}else {
			logger.error("[loadUserPrivileges], user {} is does not exist in ES.",userName);
		}
		
		
	}
	
	
}
