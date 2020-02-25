package com.resolve.sso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.concurrent.Cancellable;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.DeleteRoleRequest;
import org.elasticsearch.client.security.DeleteRoleResponse;
import org.elasticsearch.client.security.GetRolesRequest;
import org.elasticsearch.client.security.GetRolesResponse;
import org.elasticsearch.client.security.PutRoleRequest;
import org.elasticsearch.client.security.PutRoleResponse;
import org.elasticsearch.client.security.RefreshPolicy;
import org.elasticsearch.client.security.user.privileges.ApplicationResourcePrivileges;
import org.elasticsearch.client.security.user.privileges.Role;

public class Roles {
	
	private static final HttpHost host = new HttpHost("localhost", 9200, "http");
	protected RestHighLevelClient client;
	
	public static void main(String... args) {
		Roles r = new Roles();
		
		//r.deleteRolesAsync("clicks_admin");
		
		List<String> appPrivileges = Arrays.asList("read","write","send");
		List<String> appResources = Arrays.asList("dashboard","event center","report");
		
		System.out.println(r.createRole("testPutRole2","testApp", appPrivileges, appResources));
		List<Role> roles = r.getRoles();
		for(Role role : roles)
		System.out.println(role);
				
	}
	
	public Roles() {
		client = new RestHighLevelClient(RestClient.builder(host) );
	}
	
	public List<Role> getRoles(String... roleNames){
		try {
		GetRolesRequest request = new GetRolesRequest(roleNames);
		GetRolesResponse response = client.security().getRoles(request, RequestOptions.DEFAULT);
		return response.getRoles();
		}catch(Exception e) {
			System.out.println("get roles exception: "+e.getMessage());
			return null;
		}
	}
	
	public void deleteRolesAsync(String roleName) {
		try {
			DeleteRoleRequest request = new DeleteRoleRequest(roleName);
			ActionListener<DeleteRoleResponse> listener = new ActionListener<DeleteRoleResponse>() {
				@Override
				public void onResponse(DeleteRoleResponse deleteRoleResponse) {
					System.out.println("delete role success.");
					System.out.println(deleteRoleResponse.isFound());
				}
				
				@Override
				public void onFailure(Exception e) {
					System.out.println("delete role failed.");
				}
			};
			client.security().deleteRoleAsync(request, RequestOptions.DEFAULT, listener);
			//DeleteRoleResponse response = client.security().deleteRole(request, RequestOptions.DEFAULT);
		}catch(Exception e) {
			System.out.println("delete role exception: "+e.getMessage());
		}
		
	}
	
	public boolean deleteRoles(String roleName) {
		try {
			DeleteRoleRequest request = new DeleteRoleRequest(roleName);
			
			DeleteRoleResponse response = client.security().deleteRole(request, RequestOptions.DEFAULT);
			return response.isFound();
		}catch(Exception e) {
			System.out.println("delete role exception: "+e.getMessage());
			return false;
		}
		
	}
	
	public boolean createRole(String roleName, String applicationName, List<String> appPrivileges,List<String> appResources ) {
		boolean result = false;
		
		try {
		 Role role = Role.builder()
			    .name(roleName)
			    .clusterPrivileges("MONITOR","MANAGE")
			    .applicationResourcePrivileges(new ApplicationResourcePrivileges(applicationName,appPrivileges,appResources))
			    .build();
			 PutRoleRequest request = new PutRoleRequest(role, RefreshPolicy.NONE);
			 PutRoleResponse response = client.security().putRole(request, RequestOptions.DEFAULT);
			 result = response.isCreated();
		}catch(Exception e) {
			System.out.println("create role exception: "+e.getMessage());
			
		}
		return result;	
	}


}
