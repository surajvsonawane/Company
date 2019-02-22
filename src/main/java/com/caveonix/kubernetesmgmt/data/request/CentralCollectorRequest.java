package com.caveonix.kubernetesmgmt.data.request;
/****************************************************************************
 * Caveonix Inc CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017 Caveonix All Rights Reserved. Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 ****************************************************************************
 *
 * Description: CentralCollectorRequest
 * 
 * @version: 1.0
 *
 ****************************************************************************/

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
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
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
/**
 * CLASS-NAME:CentralCollectorRequest.java This class is used for request to Central Collector This
 * class consists following methods: 1.Integer postRequest(): this method Sending Request to
 * centralCollector and return responseCode 2.private String getPersistentFilePath():used for
 * getting persistent file path 3.private void writePersistentFile(): used for write persistent file
 * IMPLEMENTS:Callable EXTENDS:n/a
 * IMPORTS:java.io,java.util,java.rmi,java.net,org.slf4j,com.vmware.vim25
 */
public class CentralCollectorRequest extends  KubernetesClientConfig {

  private final Logger logger = LoggerFactory.getLogger(CentralCollectorRequest.class);
  private final String data;
  private final String persistentFile;
  private final String centralCollectorURL;
  private final String sessionId;
  private static Properties properties = null;
  
  /**
   * This is Parameterised constructor.
   * 
   * @param data : This is String parameter which gives data.
   * @param persistentFile :This is String parameter which gives persistentFile
   * @param centralCollectorURL :This is String parameter which gives centralCollectorURL
   * @param sessionId :This is String parameter which gives sessionId
   */
  public CentralCollectorRequest(String data, String persistentFile, String centralCollectorURL,
      String sessionId) {
    logger.info("Central collector request .. ." + centralCollectorURL);
    this.data = data;
    this.persistentFile = persistentFile;
    this.centralCollectorURL = centralCollectorURL;
    this.sessionId = sessionId;
  }

  /**
   * This is postRequestMethod which sends Request to centralCollector and returns responseCode.
   * 
   * @return responseCode
   * @throws Exception
   */
  public Integer postRequest() throws Exception {
    logger.debug("Sending Request ..." + centralCollectorURL);
    
    Scanner in = null;
    Integer responseCode = null;
    CloseableHttpResponse response = null;
    CloseableHttpClient httpClient = null;
    
    try {
      properties =  KubernetesClientConfig.config();
      if (properties.getProperty("server.ssl.enabled").equals("true")) {
        SSLContextBuilder builder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf = null;
        try {
          builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
          sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
          logger.error("SSL Connection Exception : " + e1.getMessage());
        }

        httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
      } else {
        // Creates CloseableHttpClient instance with default configuration.
        httpClient = HttpClients.createDefault();
      }
      
      // At some point try fluent API from httpCLient
      HttpPost post = new HttpPost(centralCollectorURL);
      post.addHeader("content-type", "application/x-www-form-urlencoded");// Added Header
      List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
      // Need to take these from result of the first request is used for splitting a String into its
      // substrings based on the given regular expression.
      String[] sessionDetails = sessionId.split("\\|");
      String sessionId = sessionDetails[1];
      logger.info("Session ID " + sessionId);
      logger.info(" ID " + sessionDetails[0]);
      logger.debug("Payload : " + data);
      urlParameters.add(new BasicNameValuePair("authId", sessionId));// sessionID is added
      urlParameters.add(new BasicNameValuePair("appId", sessionDetails[0]));// id is added
      urlParameters.add(new BasicNameValuePair("json", data));

      /**
       * HTTP entity is the information transferred as the payload of a request or response. An
       * entity consists of meta information in the form of entity-header fields and content in the
       * form of an entity-body.
       */
      HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);// pass the request in entity
      post.setEntity(postParams);

      response = httpClient.execute(post);// executing processs
      HttpEntity entity = response.getEntity();
      in = new Scanner(entity.getContent());
      while (in.hasNext()) {
        logger.info(in.next());
      }
      EntityUtils.consume(entity); // release all resources held by the httpEntity,
      responseCode = response.getStatusLine().getStatusCode();// getting status code
    } catch (IOException e) {
      // When not able to pose request write to persistent file
      String filePath = getPersistentFilePath();
      writePersistentFile(filePath, data);
      // throw new CaveoCustomExceptions(" Not able to send request to CentralCollector",
      // "CAV-4017");
      logger.error("CAV-5017:  Not able to send request to CentralCollector");
    }
    return responseCode;
  }

  /**
   * This method used to get persistent File path append method appends the specified string to this
   * character sequence.
   * 
   * @return persistentFilePath
   */
  private String getPersistentFilePath() {
    StringBuilder persistentFilePath = new StringBuilder();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH");
    Date date = new Date();
    persistentFilePath.append(this.persistentFile).append("/").append(dateFormat.format(date))
        .append("/").append(UUID.randomUUID()).append(".json");
    logger.info("Writing to persistent path");
    return persistentFilePath.toString();
  }

  /**
   * This method used for write Persistent file flush() writes the content of the buffer to the
   * destination and makes the buffer empty for further data to store close() closes the stream
   * permanently.
   * 
   * @param path : This is String parameter which gives path.
   * @param payLoad :This is String parameter which gives payLoad
   * @throws CaveoCustomExceptions 
   * @throws IOException
   */

  private void writePersistentFile(String path, String payLoad) {
    File localFile = new File(path); // path of the file
    localFile.getParentFile().mkdirs(); // get parent File
    try (Writer writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localFile), "utf-8"))) {
      writer.write(payLoad);
      // flush() writes the content of the buffer to the destination and makes the buffer empty for
      // further data to store
      writer.flush();
      // close() closes the stream permanently
      writer.close();
    } catch (IOException e) {
      logger
          .error("CAV-5018: Exception in writing payload to the persistent file" + e.getMessage());
      // throw new CaveoCustomExceptions("Exception in writing payload to the persistent file ",
      // "CAV-4018");
    }
  }


//Create a trust manager that does not validate certificate chains
static TrustManager[] trustAllCerts = new TrustManager[] { 
   new X509TrustManager() {     
       public java.security.cert.X509Certificate[]  getAcceptedIssuers() { 
           return new X509Certificate[0];
       } 
       public void checkClientTrusted( 
           java.security.cert.X509Certificate[] certs, String authType) {
           } 
       public void checkServerTrusted( 
           java.security.cert.X509Certificate[] certs, String authType) {
       }
   } 
}; 


}
