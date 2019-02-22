package com.caveonix.kubernetesmgmt.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;


public class CCStatusRequest {
	  private static final Logger logger = LoggerFactory.getLogger(GetActiveCC.class);
	  private static Properties properties = new Properties();

	  public static boolean checkCCStatus(String ccURL) {
		try {
			properties = KubernetesClientConfig.config();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    HttpsURLConnection connectionhttps;
	    HttpURLConnection connection ;
	    int code = 0;
	    
	    try {
	      String ccRequestURL = ccURL +"status/";
	      URL siteURL = new URL(ccRequestURL);
	      if (properties.getProperty("server.ssl.enabled").equals("true"))  {
	       connectionhttps = (HttpsURLConnection) siteURL.openConnection();
	       connectionhttps.setRequestMethod("GET");
	       connectionhttps.connect();
	       code = connectionhttps.getResponseCode();
	      }
	      else {
	         connection = (HttpURLConnection) siteURL
	        .openConnection();
	      connection.setRequestMethod("GET");
	      connection.connect();
	      code = connection.getResponseCode();
	      }

	      
	      if (code == 200 || code == 202) {
	        return true;
	      }

	  } catch (Exception e) {
	    logger.error("Error in getting CC status : " + e.getMessage());
	    
	  }
	    return false;
	  }
}
