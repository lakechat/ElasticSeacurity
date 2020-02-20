package ElasticSecurity;

import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.AuthenticateResponse;

public class ElasticSearchHandler {
	
	private static final Logger logger = LogManager.getLogger(ElasticSearchHandler.class);
	protected RestClient clientLow;
	protected RestHighLevelClient clientHigh;
	
	public ElasticSearchHandler() {
		
		clientLow = RestClient.builder(new HttpHost("localhost",9200,"http")).build();
	}
	
//	public ElasticSearchHandler() {
//		clientHigh = new RestHighLevelClient(
//				RestClient.builder(new HttpHost("localhost",9200,"http")) );
//	}
	
	public void test() {
		try {
		Request request = new Request("GET", "/");
		Response response = clientLow.performRequest(request);
		RequestLine requestLine = response.getRequestLine();
		String responseBody = EntityUtils.toString(response.getEntity());
		System.out.println(responseBody);
		
	
		}
		catch(Exception e) {
			logger.error("Exception: "+e.getMessage());
		}
	}
	
	public void securityTest() {
		try {
			AuthenticateResponse response = clientHigh.security().authenticate(RequestOptions.DEFAULT);
			
		}
		catch(Exception e) {
			logger.error("Exception: "+e.getMessage());
			
		}
	}
	
	
	
	

}
