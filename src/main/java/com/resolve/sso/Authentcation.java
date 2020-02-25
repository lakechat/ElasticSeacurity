package com.resolve.sso;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

public class Authentcation {
	
	private static final Logger logger = LogManager.getLogger(Authentcation.class);
	private static RestClient client;
	private static final String host = "localhost";
	private static final int port = 9200;
	
	private static final HttpHost httpHost = new HttpHost(host,port);
	
	public static void main(String[] args) {
		basicAuth("user1", "password");
	}
	
	public static boolean basicAuth(String user, String passwd) {
		boolean result = false;
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user,passwd));
		
		RestClientBuilder builder = RestClient.builder(httpHost)
				.setHttpClientConfigCallback(new HttpClientConfigCallback() {
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(
							HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
				}
				);
		
		RestClient client = builder.build();
		result = test();
		closeClient();
		return result;
	}
	
	public static boolean tokenAuth(String token) {
		boolean result = false;
		StringBuffer sb = new StringBuffer("Bearer ").append(token);
		RestClientBuilder builder = RestClient.builder(httpHost);
		Header[] defaultHeaders = new Header[] {new BasicHeader("Autherization",sb.toString())};
		builder.setDefaultHeaders(defaultHeaders);
		RestClient client = builder.build();
		
		result = test();
		closeClient();
		return result;
	}
	
	public static boolean apiKeyAuth(String keyId, String keySecret) {
		boolean result = false;
		String apiKey = new StringBuffer(keyId).append(":").append(keySecret).toString();
		String apiKeyAuth = Base64.getEncoder().encodeToString(apiKey.getBytes(StandardCharsets.UTF_8));
		String authValue = new StringBuilder("ApiKey ").append(apiKeyAuth).toString();
		RestClientBuilder builder = RestClient.builder(httpHost);
		Header[] defaultHeaders = new Header[] {new BasicHeader("Autherization",authValue)};
		builder.setDefaultHeaders(defaultHeaders);
		
		RestClient client = builder.build();
		
		result = test();
		closeClient();
		return result;
	}
	
	private static void closeClient() {
		if(client != null) {
			try {
				client.close();
			}catch(IOException e) {
				logger.error("close rest client exception: "+e.getMessage());
			}
		}
	}
	
	private static boolean test() {
		try {
			Request request = new Request("GET", "/");
			Response response = client.performRequest(request);
			RequestLine requestLine = response.getRequestLine();
			String responseBody = EntityUtils.toString(response.getEntity());
			System.out.println(responseBody);
			return true;
		
			}
			catch(Exception e) {
				System.out.println("Exception: "+e.getMessage());
				return false;
			}
	}

}
