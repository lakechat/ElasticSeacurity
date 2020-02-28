package com.resolve.sso.utils;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class JsonUtils {
	
	private static final Logger logger = LogManager.getLogger(JsonUtils.class);
	
	// get Object from json string
	public static <T> T getObjectFromJson(String jsonString, Class<T> classType) {
        //return mapper.convertValue(jsonString, classType);
        T obj = null;
        try {
            //logger.debug("jsonString: " + jsonString + ", classType: " + classType);

            obj = new ObjectMapper().configure(MapperFeature.USE_GETTERS_AS_SETTERS, false)
                                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                                    .registerModule(new JodaModule()).readValue(jsonString, classType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return obj;
    }
	
	
	// get string from a json object
	public static String getJson(Object o) {
        return getJson(o, false);
    }

    public static String getJson(Object o, boolean prettyPrint) {
        return getJson(o, false, false);
    }

    public static String getJson(Object o, boolean prettyPrint, boolean includeNonNull) {

        ObjectMapper om = new ObjectMapper().registerModule(new JodaModule())
                                            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        if (includeNonNull) {
            om.setSerializationInclusion(Include.NON_NULL);
        }

        ObjectWriter ow = om.writer();//.withDefaultPrettyPrinter();

        if (prettyPrint)
            ow = ow.withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
