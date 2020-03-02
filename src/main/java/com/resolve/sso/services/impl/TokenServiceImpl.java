package com.resolve.sso.services.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.security.CreateTokenRequest;
import org.elasticsearch.client.security.CreateTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.resolve.sso.entities.auth.Realm;
import com.resolve.sso.entities.user.UserAuthenticationRequest;
import com.resolve.sso.services.TokenService;
import com.resolve.sso.utils.ElasticSearchHandler;

@Component
public class TokenServiceImpl implements TokenService {

	private static final Logger logger = LogManager.getLogger(TokenServiceImpl.class);
	
	@Autowired
	private ElasticSearchHandler esHandler;
	
	@Override
	public CreateTokenResponse createPasswordGrantToken(String userName, String password) {
		try {
			if(logger.isDebugEnabled()) // should not display password in production env.
				logger.debug("creating password granted token, username is {}, password is {}", userName, password);
			
			CreateTokenRequest createTokenRequest = CreateTokenRequest.passwordGrant(userName, password.toCharArray());
			CreateTokenResponse createTokenResponse = esHandler.getHighLevelClient().security()
															.createToken(createTokenRequest, RequestOptions.DEFAULT);
			return  createTokenResponse;
			
		}catch(Exception e) {
			logger.error("creating password granted token exception, username is {}, ", userName);
			logger.error("creating password granted token exception, ", e.getMessage());
		}
		
		
		return null;
	}

	@Override
	public CreateTokenResponse createAccessToken(UserAuthenticationRequest authRequest) {
		String realmName = authRequest.getRealm();
		String userName = authRequest.getUserName();
		String password = authRequest.getPassword();
		CreateTokenResponse result = null;
		Realm realm = Realm.getRealm(realmName);
		switch(realm) {
		case NATIVE:
			result = createPasswordGrantToken(userName, password);
			break;
		default:
			result = createPasswordGrantToken(userName, password);
			break;
		}
		return result;
	}
	
	@Override
	public CreateTokenResponse refreshToken(String refreshToken) {
		try {
			if(logger.isDebugEnabled()) // should not display password in production env.
				logger.debug("Refresh existing token {}", refreshToken);
			CreateTokenRequest createTokenRequest = CreateTokenRequest.refreshTokenGrant(refreshToken);
			CreateTokenResponse createTokenResponse = esHandler.getHighLevelClient().security()
														.createToken(createTokenRequest, RequestOptions.DEFAULT);
			return createTokenResponse;
		}catch(Exception e) {
			logger.error("refresh token exception, refreshToken is {}, ", refreshToken);
			logger.error("refresh token exception, ", e.getMessage());
			return null;
		}
	}

}
