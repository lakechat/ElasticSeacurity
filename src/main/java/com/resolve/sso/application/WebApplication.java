package com.resolve.sso.application;

import java.util.Arrays;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages="com.resolve.sso")
public class WebApplication {
	public static void main(String... args) {
		SpringApplication.run(WebApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("Let's inspect the beans provided by Spring Boot");
			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
//			for (String beanName : beanNames) {
//				System.out.println(beanName);
//			}

		};

	}
	
//	@Bean
//	public EmbeddedServletContainerFactory servletContainer() {
//	  TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
//	      @Override
//	      protected void postProcessContext(Context context) {
//	        SecurityConstraint securityConstraint = new SecurityConstraint();
//	        securityConstraint.setUserConstraint("CONFIDENTIAL");
//	        SecurityCollection collection = new SecurityCollection();
//	        collection.addPattern("/*");
//	        securityConstraint.addCollection(collection);
//	        context.addConstraint(securityConstraint);
//	      }
//	    };
//	   
//	  tomcat.addAdditionalTomcatConnectors(redirectConnector());
//	  return tomcat;
//	}
//	 
//	private Connector redirectConnector() {
//	  Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
//	  connector.setScheme("http");
//	  connector.setPort(8080);
//	  connector.setSecure(false);
//	  connector.setRedirectPort(8443);
//	   
//	  return connector;
//	}

}
