package com.resolve.sso.services.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.DeleteRoleRequest;
import org.elasticsearch.client.security.DeleteRoleResponse;
import org.elasticsearch.client.security.GetRoleMappingsRequest;
import org.elasticsearch.client.security.GetRoleMappingsResponse;
import org.elasticsearch.client.security.GetRolesRequest;
import org.elasticsearch.client.security.GetRolesResponse;
import org.elasticsearch.client.security.PutRoleMappingRequest;
import org.elasticsearch.client.security.PutRoleMappingResponse;
import org.elasticsearch.client.security.PutRoleRequest;
import org.elasticsearch.client.security.PutRoleResponse;
import org.elasticsearch.client.security.RefreshPolicy;
import org.elasticsearch.client.security.support.expressiondsl.RoleMapperExpression;
import org.elasticsearch.client.security.support.expressiondsl.expressions.AnyRoleMapperExpression;
import org.elasticsearch.client.security.support.expressiondsl.fields.FieldRoleMapperExpression;
import org.elasticsearch.client.security.user.privileges.ApplicationResourcePrivileges;
import org.elasticsearch.client.security.user.privileges.Role;
import org.springframework.stereotype.Component;

import com.resolve.sso.services.RoleService;

@Component
public class RoleServiceImpl implements RoleService {
	
	private static final HttpHost host = new HttpHost("localhost", 9200, "http");
	protected RestHighLevelClient client;
	
	public static void main(String... args) {
		RoleServiceImpl r = new RoleServiceImpl();
		r.testCreateRole();
		
				
	}
	
	public RoleServiceImpl() {
		client = new RestHighLevelClient(RestClient.builder(host) );
	}
	
	@Override
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
	
	public void getRoleMapping() {
		try {
		final GetRoleMappingsRequest request = new GetRoleMappingsRequest("clicks_admin");
		final GetRoleMappingsResponse response = client.security().getRoleMappings(request, RequestOptions.DEFAULT);
		System.out.println(response.toString());
		}catch(Exception e) {
			
		}
	}
	
	private  void testRoleMapping() {
		getRoleMapping();
		
	}
	
	private void testCreateRole() {
		//r.deleteRolesAsync("clicks_admin");
		
				List<String> appPrivileges = Arrays.asList("read","write","send");
				List<String> appResources = Arrays.asList("dashboard","event center","report");
				
				System.out.println(createRole("testPutRole3","fireStorm", appPrivileges, appResources));
				List<Role> roles = getRoles();
				for(Role role : roles)
				System.out.println(role);
	}
	
	private boolean putRoleMapping() {
		try {
			final RoleMapperExpression rules = AnyRoleMapperExpression.builder()
					.addExpression(FieldRoleMapperExpression.ofUsername("user3"))
					.addExpression(FieldRoleMapperExpression.ofGroups("cn=admins,dc=example,dc=com")).build();
			final PutRoleMappingRequest request = new PutRoleMappingRequest("mapping_example", true,
					Collections.singletonList("beats_system"), Collections.emptyList(), rules, null, RefreshPolicy.NONE);
			final PutRoleMappingResponse response = client.security().putRoleMapping(request, RequestOptions.DEFAULT);
			return response.isCreated();
			
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	private void testPutRoleMapping() {
		putRoleMapping();
	}


}
