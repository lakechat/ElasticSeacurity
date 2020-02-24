package ElasticSecurity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.elasticsearch.client.security.RefreshPolicy;
import org.elasticsearch.client.security.user.privileges.ApplicationPrivilege;
import org.elasticsearch.client.security.user.privileges.ApplicationResourcePrivileges;

public class Privileges {
	private static final Logger logger = LogManager.getLogger(Privileges.class);
	private static final HttpHost host = new HttpHost("localhost",9200,"http");
	protected RestHighLevelClient client;
	List<String> appPrivileges = Arrays.asList("read","write","send");
	List<String> appResources = Arrays.asList("dashboard","event center","report");
	
	
	public static void main(String... args) {
		Privileges p = new Privileges();
		p.createPrivileges(null);
		Set<ApplicationPrivilege> ps = p.getPrivileges(null,null);
		if(ps!=null)
		for(ApplicationPrivilege ap : ps)
			System.out.println(ap.getName());
	}
	
	public Privileges() {
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
	
	public void createPrivileges(List<ApplicationPrivilege> aps) {
		try {
			final List<ApplicationPrivilege> privileges = new ArrayList<>();
			privileges.add(ApplicationPrivilege.builder()
			    .application("Meridian")
			    .privilege("all")
			    .actions("action:login","action:read","action:write")
			    .metadata(Collections.singletonMap("description", "sample"))
			    .build());
			privileges.add(ApplicationPrivilege.builder()
			    .application("FireStorm")
			    .privilege("write")
			    .actions("action:write")
			    .build());
			privileges.add(ApplicationPrivilege.builder()
				    .application("FireStorm")
				    .privilege("read")
				    .actions("action:read")
				    .build());
			final PutPrivilegesRequest putPrivilegesRequest = new PutPrivilegesRequest(privileges, RefreshPolicy.IMMEDIATE);			
		}catch(Exception e) {
			
		}
	}

}
