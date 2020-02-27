package com.resolve.sso.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.resolve.sso.entities.user.UserAuthenticationRequest;
import com.resolve.sso.entities.user.UserAuthenticationResult;
import com.resolve.sso.services.AuthenticationService;
import com.resolve.sso.services.UserService;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	private static Logger logger = LogManager.getLogger(AuthenticationController.class);
	
	@Autowired
	AuthenticationService authService;
	
	@Autowired
	UserService userService;
	
	@RequestMapping(value="/login", method = RequestMethod.POST)
	
	public UserAuthenticationResult authenticateUser(
			@RequestBody(required=true) UserAuthenticationRequest authRequest) {
		
		
		return null;
		
	}

}
