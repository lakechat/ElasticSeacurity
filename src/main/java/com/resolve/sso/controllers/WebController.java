package com.resolve.sso.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class WebController {
	private static Logger logger = LogManager.getLogger(WebController.class);
	
		@RequestMapping("/")
		public String index() {
			int n=5;
			logger.debug("test {},---{}",n,(n+5));
			logger.debug("test ");
			return "Greetings from Spring Boot";
		}
	

}
