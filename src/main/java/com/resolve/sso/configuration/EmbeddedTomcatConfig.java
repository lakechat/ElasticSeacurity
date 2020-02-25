package com.resolve.sso.configuration;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedTomcatConfig {

	@Value("${http.port}")
	private int httpPort;
	
	@Bean
	public WebServerFactoryCustomizer customizeTomcatConnector() {
		return new WebServerFactoryCustomizer() {
			@Override
			public void customize(WebServerFactory container) {
                if (container instanceof TomcatServletWebServerFactory) {
                	TomcatServletWebServerFactory containerFactory =(TomcatServletWebServerFactory) container;
                    Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
                    connector.setPort(httpPort);
                    containerFactory.addAdditionalTomcatConnectors(connector);

                }
			}
		};
	}
	
}
