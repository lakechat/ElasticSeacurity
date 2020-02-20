package ElasticSecurity;

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
import org.elasticsearch.client.security.user.privileges.Role;

public class Roles {
	
	private static final HttpHost host = new HttpHost("localhost", 9200, "http");
	protected RestHighLevelClient client;
	
	public static void main(String... args) {
		Roles r = new Roles();
		List<Role> roles = r.getRoles("clicks_admin");
//		for(Role role : roles)
//			System.out.println(role);
		
		r.deleteRolesAsync("clicks_admin");
				
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


}
