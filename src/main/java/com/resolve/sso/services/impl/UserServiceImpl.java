package com.resolve.sso.services.impl;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.GetUsersRequest;
import org.elasticsearch.client.security.GetUsersResponse;
import org.elasticsearch.client.security.user.User;

import com.resolve.sso.utils.ElasticSearchHandler;

public class UserServiceImpl {

	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);
	
	protected RestHighLevelClient client;
	
	public static void main(String[] args) {
		UserServiceImpl  user = new UserServiceImpl();
		Set<User> users = user.getUser(null);
		
		for(User u : users)
			System.out.println(u.getRoles());
		
	}
	
	public UserServiceImpl() {
		client = ElasticSearchHandler.getHighLevelClient();
	}
	
	public Set<User> getUser(String userName) {
		GetUsersRequest request = null;
		try {
			if(StringUtils.isBlank(userName))
				request = new GetUsersRequest();
			else
				request = new GetUsersRequest(userName);
			GetUsersResponse response = client.security().getUsers(request, RequestOptions.DEFAULT);
			return response.getUsers();

		}catch(Exception e) {
			logger.error("getUser("+userName+") exception: "+e.getMessage());
			return null;
		}
	}
	
	
}
