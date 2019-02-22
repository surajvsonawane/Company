package com.caveonix.kubernetesmgmt.utils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;

public class SessionInfo {

  private final Logger logger = LoggerFactory.getLogger(SessionInfo.class);
  private static Properties properties = null;

  /**
   * this method used to getting Session Details
   * 
   * @param requestURL This is String parameter which gives request URL.
   * @param agentId This is Integer parameter which gives agent Id.
   * @param key This is String parameter.
   * @param orgId This is String parameter which gives org Id.
   * @param locationId This is String parameter which gives location Id.
   * @param serviceProviderId This is String parameter which gives Service provider Id.
   * @return sessionId
   * @throws CaveoCustomExceptions
   * @exception IOException
   */

  public String getSessionDetails(String requestURL, int agentId, String key, String orgId,
      String locationId, String serviceProviderId) {
    // Creates CloseableHttpClient instance with default configuration.
    CloseableHttpClient httpclient = null;

    if (properties.getProperty("server.ssl.enabled").equals("true")) {
      SSLContextBuilder builder = new SSLContextBuilder();
      SSLConnectionSocketFactory sslsf = null;
      try {
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        sslsf = new SSLConnectionSocketFactory(builder.build());
      } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
        logger.error("SSL Connection Exception : " + e1.getMessage());
      }

      httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
    } else {
      httpclient = HttpClients.createDefault();
    }

    String sessionId = null;
    CloseableHttpResponse response = null;
    try {
      HttpPost post = new HttpPost(requestURL); // is used to transfer data from client to server
      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      // Need to take these from result of the first request
      urlParameters.add(new BasicNameValuePair("key", key));// Key is added in urlParameters
      urlParameters.add(new BasicNameValuePair("organizationId", orgId));// organizationID is added
      urlParameters.add(new BasicNameValuePair("locationId", locationId));// LocationID is added
      // service_provider is added
      urlParameters.add(new BasicNameValuePair("serviceProviderId", serviceProviderId));
      // agentID is added
      urlParameters.add(new BasicNameValuePair("agentId", Integer.toString(agentId)));
      urlParameters.add(new BasicNameValuePair("application", "Container"));
      /**
       * HTTP entity is the information transferred as the payload of a request or response. An
       * entity consists of meta information in the form of entity-header fields and content in the
       * form of an entity-body.
       */
      HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);// pass the request in entity
      post.setEntity(postParams);
      response = httpclient.execute(post); // Execute the method
      HttpEntity entity = response.getEntity();
      sessionId = EntityUtils.toString(entity, "UTF-8");
    } catch (IOException e) {
      logger
          .error("CAV-3009 : Unable to get session [check Central Collctor URL] " + e.getMessage());
      // throw new CaveoCustomExceptions("Unable to get session [check Central Collctor URL]", e,
      // "CAV-3009");
    } finally {
      try {
        if (response != null)
          response.close();
      } catch (IOException e) {
        // throw new CaveoCustomExceptions("Unable to close response", e,
        // "CAV-3010");
        logger.error("CAV-3010 : Unable to close response" + e.getMessage());
      }
    }
    return sessionId;
  }

  /**
   * This method is used for getting AgentID
   * 
   * @param requestURL This is String parameter which gives request URL.
   * @exception UnknownHostException
   * @return rcId
   * @throws NullPointerException
   * @throws MalformedObjectNameException
   * @throws CaveoCustomExceptions
   * @throws JSONException
   * @throws IOException
   */
  public int getAgentId(String requestURL)
      throws MalformedObjectNameException, NullPointerException, JSONException, IOException {
    InetAddress ip = null;
    CloseableHttpClient httpclient = null;

    try {
      properties = KubernetesClientConfig.config();
      ip = InetAddress.getLocalHost();
      logger.info("Central Collector URL: " + requestURL);
      logger.info("Current IP address : " + ip.getHostAddress());// getting HostAddress
      logger.info("Hostname : " + ip.getHostName());// getting HostName
    } catch (UnknownHostException e) {
      // throw new CaveoCustomExceptions("Unknown host exception", e, "CAV-3011");
      logger.error("CAV-3011 : Unknown host exception" + e.getMessage());
    }

    if (properties.getProperty("server.ssl.enabled").equals("true")) {
      SSLContextBuilder builder = new SSLContextBuilder();
      SSLConnectionSocketFactory sslsf = null;
      try {
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        sslsf = new SSLConnectionSocketFactory(builder.build());
      } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
        logger.error("SSL Connection Exception : " + e1.getMessage());
      }

      httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
    } else {
      // Creates CloseableHttpClient instance with default configuration.
      httpclient = HttpClients.createDefault();
    }

    String rcId = null;
    CloseableHttpResponse response = null;
    try {
      HttpPost post = new HttpPost(requestURL);
      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      // Need to take these from result of the first request
      urlParameters.add(new BasicNameValuePair("rcUrl", getIpAddressAndPort()));// rcUrl is added
      urlParameters.add(new BasicNameValuePair("hostName", ip.getHostName()));// hostName is added
      urlParameters.add(new BasicNameValuePair("agentName", "Container"));// agentName is added
      HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);// pass the request in entity
      post.setEntity(postParams);
      response = httpclient.execute(post);// execute the process
      HttpEntity entity = response.getEntity();
      rcId = EntityUtils.toString(entity, "UTF-8");
    } catch (IOException e) {
      logger
          .error("CAV-3012 : Unable to get session [check Central Collctor URL]" + e.getMessage());
    } finally {
      try {
        if (response != null)
          response.close();
      } catch (IOException e) {
        logger.error("CAV-3013 : Unable to close response " + e.getMessage());
      }
    }
    return Integer.parseInt(rcId);
  }

  /**
   * This method used to getVMwareDetails
   * 
   * @param requestURL This is String parameter which gives request URL.
   * @param sessionId This is String parameter which provides session id.
   * @throws CaveoCustomExceptions
   * @exception IOException
   */

  public JSONArray getVMwareDetails(String requestURL, String sessionId) {
    // Creates CloseableHttpClient instance with default
    CloseableHttpClient httpclient = HttpClients.createDefault();
    String rcMap = null;
    CloseableHttpResponse response = null;
    try {
      HttpPost post = new HttpPost(requestURL);
      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      String[] sessionDetails = sessionId.split("\\|");
      logger.info("Session ID " + sessionId);
      logger.info(" ID " + sessionDetails[0]);
      urlParameters.add(new BasicNameValuePair("authId", sessionDetails[1]));// added a sessionId
      urlParameters.add(new BasicNameValuePair("appId", sessionDetails[0]));// id is added
      urlParameters.add(new BasicNameValuePair("type", "CaveoAWS")); // VCD Type
      HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);// pass the request in entity
      post.setEntity(postParams);
      response = httpclient.execute(post);// Execute the process
      HttpEntity entity = response.getEntity();
      rcMap = EntityUtils.toString(entity, "UTF-8");
    } catch (IOException e) {
      logger.error("CAV-3014 :Unable to get session [check Central Collctor URL]" + e.getMessage());
    } finally {
      try {
        if (response != null)
          response.close();
      } catch (IOException e) {
        logger.error("CAV-3015 : Unable to close response " + e.getMessage());
      }
    }
    // A Java serialization/deserialization library to convert Java Objects into JSON and back
    JSONArray vmWareArr = new JSONArray();
    try {
      vmWareArr = new JSONArray(rcMap);
    } catch (JSONException e) {
      // throw new CaveoCustomExceptions("Exception in coverting to JSON array", e, "CAV-3016");
      logger.error("CAV-3016 : Exception in coverting to JSON array " + e.getMessage());
    }
    return vmWareArr;
  }

  public String getStringFromRequest(String requestURL, String sessionId, String orgId,
      String assetId) {
    logger.info("Called getStringFromRequest " + requestURL);
    String[] sessionDetails = sessionId.split("\\|");
    // Creates CloseableHttpClient instance with default configurations
    CloseableHttpClient httpclient = HttpClients.createDefault();
    String retString = null;
    CloseableHttpResponse response = null;
    try {
      HttpPost post = new HttpPost(requestURL); // Request that URL by using Htttp Post method.
      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      // Need to take these from result of the first request
      urlParameters.add(new BasicNameValuePair("authId", sessionDetails[1]));
      urlParameters.add(new BasicNameValuePair("appId", sessionDetails[0]));
      if (orgId != null)
        urlParameters.add(new BasicNameValuePair("orgId", orgId));
      if (assetId != null)
        urlParameters.add(new BasicNameValuePair("assetId", assetId));
      /**
       * An entity composed of a list of url-encoded pairs that can be sent or received with an HTTP
       * message.
       */
      HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
      post.setEntity(postParams);
      response = httpclient.execute(post); // Execute the method.
      HttpEntity entity = response.getEntity();
      // Get the entity content as a String using the provided charcter set.
      retString = EntityUtils.toString(entity, "UTF-8");

    } catch (IOException e) {
      // throw new CaveoCustomExceptions("Unable to get session [check Central Collctor URL]", e,
      // "CAV-3017");
      logger
          .error("CAV-3017 : Unable to get session [check Central Collctor URL] " + e.getMessage());
    } finally {
      try {
        if (response != null)
          response.close();
      } catch (IOException e) {
        // throw new CaveoCustomExceptions("Unable to close response", e, "CAV-3018");
        logger.error("CAV-3018 : Unable to close response " + e.getMessage());
      }
    }
    return retString;
  }

  private static String getIpAddressAndPort()
      throws MalformedObjectNameException, NullPointerException, IOException, JSONException {
    properties = KubernetesClientConfig.config();
    String protocol = null;
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
        Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
    String host = InetAddress.getLocalHost().getHostAddress();
    String port;

    if (objectNames.iterator().hasNext()
        && objectNames.iterator().next().getKeyProperty("port") != null)
      port = objectNames.iterator().next().getKeyProperty("port");
    else
      port = System.getProperty("server.port");

    if (port == null)
      port = properties.getProperty("server.port");

    if (properties.getProperty("server.ssl.enabled").equals("true"))
      protocol = "https";
    else
      protocol = "http";

    String ipadd = protocol + "://" + host + ":" + port;

    return ipadd;
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
