package com.caveonix.kubernetesmgmt.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
import com.caveonix.kubernetesmgmt.config.KubernetesExtractConstants;

public class GetActiveCC {
  private static final Logger logger = LoggerFactory.getLogger(GetActiveCC.class);
  private static Properties properties = new Properties();

  public static String getActiveCC(String ccStr) throws IOException, JSONException {
      String retCC = null;
      boolean flag = false;
      HttpsURLConnection connectionhttps;
      HttpURLConnection connection ;
      int code = 0;
      properties = KubernetesClientConfig.config();
      String[] ccList = ccStr.split(",");
      for (int i=0 ; i < ccList.length; i++) {

          try {
              String ccURL = ccList[i]+"status/";
              URL siteURL = new URL(ccURL);
              if (properties.getProperty("server.ssl.enabled").equals("true"))  {
                  connectionhttps = (HttpsURLConnection) siteURL
                          .openConnection();
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
                  flag = true;
              }
              if (flag)
                  return ccList[i];
          } catch (Exception e) {
              flag = false;
          }
      }

      if (!flag) {
          String newCentralCollectorURL = KubernetesClientConfig.getCCListDetails(properties.getProperty(KubernetesExtractConstants.CCLEADER));
          properties.setProperty(KubernetesExtractConstants.CC_URL, newCentralCollectorURL);
          getActiveCC(properties.getProperty(KubernetesExtractConstants.CC_URL));
      }

      return retCC;
  }

  public static String getCCList (String ccURL) throws IOException {

      String retString = null;
      HttpsURLConnection connectionhttps;
      HttpURLConnection connection ;
      properties = KubernetesClientConfig.config();
      try {
          URL siteURL = new URL(ccURL+"getCCList/");
          if (properties.getProperty("server.ssl.enabled").equals("true"))  {
            connectionhttps = (HttpsURLConnection) siteURL
                    .openConnection();
            connectionhttps.setRequestMethod("GET");
            connectionhttps.connect();
            if (connectionhttps.getResponseCode() == 200 || connectionhttps.getResponseCode() == 202) {
              BufferedReader br = new BufferedReader(new InputStreamReader(connectionhttps.getInputStream()));
              StringBuffer sb = new StringBuffer();
              String output = "";
              while ((output = br.readLine()) != null)  {
                sb.append(output);
              }
                retString =  sb.toString();
            }
        }
        else {
            connection = (HttpURLConnection) siteURL
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200 || connection.getResponseCode() == 202) {
              BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              StringBuffer sb = new StringBuffer();
              String output = "";
              while ((output = br.readLine()) != null)  {
                sb.append(output);
              }
                retString =  sb.toString();
            }
        }
          
      } catch (IOException e) {
          logger.error("CAV-3080 : Unable to execute request : " + e.getMessage());
      } 
      return retString;
  }

  
  // Create a trust manager that does not validate certificate chains
  static TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
  }};

}
