package com.resolve.sso.controllers;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.resolve.sso.entities.user.UserAuthenticationRequest;
import com.resolve.sso.entities.user.UserAuthenticationResult;
import com.resolve.sso.services.AuthenticationService;
import com.resolve.sso.services.UserService;
import com.resolve.sso.utils.JWTUtil;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	private static Logger logger = LogManager.getLogger(AuthenticationController.class);
	
	
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	
	public UserAuthenticationResult authenticateUser(
			@RequestBody(required=true) UserAuthenticationRequest authRequest) {
		    
		UserAuthenticationResult authResult = userService.authenticateUser(authRequest);
		
		return authResult;
		
	}
	
	@RequestMapping(value="/login/jwt", method = RequestMethod.POST)
	public Object authenticateUserJWT(HttpServletRequest httpRequest,HttpServletResponse httpResponse,
			@RequestBody(required=true) UserAuthenticationRequest authRequest) {
		    
		UserAuthenticationResult authResult = userService.authenticateUserJWT(authRequest);
		
		if(StringUtils.isBlank(authResult.getError())) {
			try {
				StringBuffer url = new StringBuffer("http://localhost:8081/auth/login/test?");
				url.append("jwt=").append(authResult.getAccess_token());
				url.append("&key=").append(authResult.getKey());
				httpResponse.sendRedirect(url.toString());
				
				}catch(Exception e) {
					
				}
			return null;
		}else {
			return authResult;
		}
	}
	
}
