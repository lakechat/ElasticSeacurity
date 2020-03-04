package com.resolve.sso.services.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.security.CreateTokenResponse;
import org.elasticsearch.client.security.GetUsersRequest;
import org.elasticsearch.client.security.GetUsersResponse;
import org.elasticsearch.client.security.PutUserRequest;
import org.elasticsearch.client.security.PutUserResponse;
import org.elasticsearch.client.security.RefreshPolicy;
import org.elasticsearch.client.security.user.User;
import org.elasticsearch.client.security.user.privileges.ApplicationResourcePrivileges;
import org.elasticsearch.client.security.user.privileges.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.resolve.sso.entities.user.UserAuthenticationRequest;
import com.resolve.sso.entities.user.UserAuthenticationResult;
import com.resolve.sso.services.AuthenticationService;
import com.resolve.sso.services.RoleService;
import com.resolve.sso.services.TokenService;
import com.resolve.sso.services.UserService;
import com.resolve.sso.utils.ElasticSearchHandler;
import com.resolve.sso.utils.JsonUtils;
import com.resolve.sso.utils.UserConstants;

@Component
public class UserServiceImpl implements UserService{

	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
	
	@Autowired
	private AuthenticationService authService;
	
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	RoleService roleService;
	
	public static void main(String[] args) {
		UserServiceImpl usi = new UserServiceImpl();
//		Set<User> users = usi.getUser("user3");
//		for (User user : users)
//			System.out.println(user.getRoles());
		
		usi.createUser("user3", "password");
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
			GetUsersResponse response = ElasticSearchHandler.getHighLevelClient().security()
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
					//loadUserPrivileges(authRequest.getUserName(),token);
					
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
				User[] userNames = users.toArray(new User[0]);
				// there is only one user 
				String[] roleNames = userNames[0].getRoles().toArray(new String[0]);
				
				if(logger.isDebugEnabled()) {
					logger.debug("[loadUserPrivileges] user{} has roles: {}", userNames[0].getUsername(), roleNames);
				}
				//2. get role privileges
				List<Role> roles = roleService.getRoles(roleNames);
				for(Role role : roles) {
					Set<ApplicationResourcePrivileges> applicationPrivileges = role.getApplicationPrivileges();
					if (applicationPrivileges != null && !applicationPrivileges.isEmpty()) {
						for (ApplicationResourcePrivileges ap : applicationPrivileges) {
							String application = ap.getApplication();
							Set<String> privileges = ap.getPrivileges();
							if (logger.isDebugEnabled()) {
								logger.debug("[loadUserPrivileges] role {} has application - {}, privileges - {} ",
											role.getName(), application,privileges);
							}
							//3. save to Redis
							// key format: Prefix,userName:AppName
							// value [set of privileges]
							
						}
					}else {
						if (logger.isDebugEnabled()) {
							logger.debug("[loadUserPrivileges] role {} does not have application privileges ",
										role.getName());
						}
					}
				}
				
			}else {
				logger.error("[loadUserPrivileges], user {} does not own any roles.",userName);
			}
			
			
		}else {
			logger.error("[loadUserPrivileges], user {} is does not exist in ES.",userName);
		}
		
		
	}
	
	public boolean createUser(String userName, String password) {
		try {
			char[] pass = password.toCharArray();
			User user = new User(userName, Arrays.asList("testPutRole2","testPutRole3","superuser")) ;
			PutUserRequest request = PutUserRequest.withPassword(user, pass, true, RefreshPolicy.NONE);
			PutUserResponse response = ElasticSearchHandler.getHighLevelClient().security()
										.putUser(request, RequestOptions.DEFAULT);


			boolean result = response.isCreated();
			return result;
		}catch(Exception e) {
			logger.error("[createUser] create user exception: "+e.getMessage());
			return false;
		}
	}
	
	
}
