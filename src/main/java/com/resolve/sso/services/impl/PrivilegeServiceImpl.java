package com.resolve.sso.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.GetPrivilegesRequest;
import org.elasticsearch.client.security.GetPrivilegesResponse;
import org.elasticsearch.client.security.PutPrivilegesRequest;
import org.elasticsearch.client.security.PutPrivilegesResponse;
import org.elasticsearch.client.security.RefreshPolicy;
import org.elasticsearch.client.security.user.privileges.ApplicationPrivilege;

public class PrivilegeServiceImpl {
	private static final Logger logger = LogManager.getLogger(PrivilegeServiceImpl.class);
	private static final HttpHost host = new HttpHost("localhost",9200,"http");
	protected RestHighLevelClient client;
	List<String> appPrivileges = Arrays.asList("read","write","send");
	List<String> appResources = Arrays.asList("dashboard","event center","report");
	
	
	public static void main(String... args) {
		PrivilegeServiceImpl p = new PrivilegeServiceImpl();
		p.createPrivileges(null);
		
		Set<ApplicationPrivilege> ps = p.getPrivileges("Meridian",null);
		if(ps!=null)
		for(ApplicationPrivilege ap : ps)
			System.out.println(ap.getName());
	}
	
	public PrivilegeServiceImpl() {
		client = new RestHighLevelClient(RestClient.builder(host) );
	}
	
	public Set<ApplicationPrivilege> getPrivileges(String appName, String privilegeName){
		try {
			GetPrivilegesRequest request = null;
			if(StringUtils.isNotBlank(appName) && StringUtils.isNotBlank(privilegeName))
				request = new GetPrivilegesRequest(appName,privilegeName);
			else if (StringUtils.isNotBlank(appName) )
				request = new GetPrivilegesRequest(appName);
			else
				request =  GetPrivilegesRequest.getAllPrivileges();
			// get all privileges of a specific application
			//GetPrivilegesRequest request = GetPrivilegesRequest.getApplicationPrivileges("testapp");
			
			// get all privileges of a ll applications
			//GetPrivilegesRequest request = GetPrivilegesRequest.getAllPrivileges();
			
			GetPrivilegesResponse response = client.security().getPrivileges(request, RequestOptions.DEFAULT);
			Set<ApplicationPrivilege> result = response.getPrivileges();
			return result;
						
		}catch(Exception e) {
			System.out.println("get privileges exception: "+e.getMessage());
			return null;
		}
	}
	
	//application name must start with lower case letters
	public void createPrivileges(List<ApplicationPrivilege> aps) {
		try {
			final List<ApplicationPrivilege> privileges = new ArrayList<>();
			privileges.add(ApplicationPrivilege.builder().application("meridian").privilege("all")
					.actions("action:login","action:write","action:read","action:delete")
					.metadata(Collections.singletonMap("description", "sample")).build());
			privileges.add(ApplicationPrivilege.builder().application("fireStorm").privilege("write")
					.actions("action:write_update").build());
			privileges.add(ApplicationPrivilege.builder().application("fireStorm").privilege("read")
					.actions("action:read").build());
			final PutPrivilegesRequest putPrivilegesRequest = new PutPrivilegesRequest(privileges,
					RefreshPolicy.IMMEDIATE);
			final PutPrivilegesResponse putPrivilegesResponse = client.security().putPrivileges(putPrivilegesRequest,
					RequestOptions.DEFAULT);

			final boolean status = putPrivilegesResponse.wasCreated("meridian", "all");
			final boolean status2 = putPrivilegesResponse.wasCreated("fireStorm", "write");
			final boolean status3 = putPrivilegesResponse.wasCreated("fireStorm", "read");
			System.out.println("application privileges create: "+status);
		} catch (Exception e) {
			System.out.println("create application privileges exception: "+e.getMessage());
		}
	}

}
