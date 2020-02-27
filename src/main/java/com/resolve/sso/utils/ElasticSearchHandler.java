package com.resolve.sso.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;

import org.apache.http.RequestLine;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.AuthenticateResponse;

public class ElasticSearchHandler {
	
	private static final Logger logger = LogManager.getLogger(ElasticSearchHandler.class);
	protected RestClient lowLevelClient;
	private static RestHighLevelClient highLevelCient;
	
	private static String esHost = "localhost";
	private static int esPort = 9200;
	private static int numberOfWorkers = 10; 
	
	static {
		try {
			String esHttpHostPort = System.getenv("ELASTICSEARCH_HOSTNAME_HTTP_PORT");
			logger.info("Configure ES, get host:port as {} : ", esHttpHostPort);

			String[] hostPortParts = esHttpHostPort.trim().split(":");
			esHost = hostPortParts[0];
			esPort = hostPortParts.length < 2 ? 9200 : Integer.parseInt(hostPortParts[1]);
			logger.info(String.format("Adding ELASTICSEARCH HTTP [HOST:PORT]: [%s:%d]", esHost, esPort));
			
			String tmp = System.getenv("ELASTICSEARCH_NUMBER_OF_WORKERS");
			numberOfWorkers = Integer.parseInt(StringUtils.isBlank(tmp)?"10":tmp);
			logger.info(String.format("ELASTICSEARCH client number of workers: {}", numberOfWorkers));
		} catch (Exception e) {
			logger.error("getting ES host:port exception: " + e.getMessage());
		}
	}
	
//	public ElasticSearchHandler() {
//		
//		lowLevelClient = RestClient.builder(new HttpHost("localhost",9200,"http")).build();
//	}
	
//	public ElasticSearchHandler() {
//		clientHigh = new RestHighLevelClient(
//				RestClient.builder(new HttpHost("localhost",9200,"http")) );
//	}
	
	public static RestHighLevelClient getHighLevelClient() {
		if(highLevelCient == null) {
			highLevelCient = new RestHighLevelClient(getBuilder());
		}
		return highLevelCient;
		
	}
	
	public void test() {
		try {
		Request request = new Request("GET", "/");
		Response response = lowLevelClient.performRequest(request);
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
			AuthenticateResponse response = highLevelCient.security().authenticate(RequestOptions.DEFAULT);
			
		}
		catch(Exception e) {
			logger.error("Exception: "+e.getMessage());
			
		}
	}
	
	private static RestClientBuilder getBuilder() {
		RestClientBuilder builder = RestClient
				.builder(new HttpHost(esHost, esPort, "http"))
				.setHttpClientConfigCallback(new HttpClientConfigCallback() {
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder
								.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(numberOfWorkers).build());
					}
				});
		return builder;

	}
	
//	public void setConnectTimeOutConfig() {
//        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
//            @Override
//            public Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
//                requestConfigBuilder.setConnectTimeout(connectTimeOut);
//                requestConfigBuilder.setSocketTimeout(socketTimeOut);
//                requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeOut);
//                return requestConfigBuilder;
//            }
//        });
//    }
	
	/**
     * httpclient connection
     */
//    public void setMutiConnectConfig() {
//        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//            @Override
//            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
//                httpClientBuilder.setMaxConnTotal(maxConnectNum);
//                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
//                return httpClientBuilder;
//            }
//        });
//    }

	
	

}
