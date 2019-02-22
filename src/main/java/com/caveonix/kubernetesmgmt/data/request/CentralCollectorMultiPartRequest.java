package com.caveonix.kubernetesmgmt.data.request;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

public class CentralCollectorMultiPartRequest implements Callable{
  
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  //private final String data;
  private final String persistentFile;
  private final String centralCollectorURL;
  private final String authId;
  private final String appId;
  private final String orgId; 
  private final String payloadType;
  private String scanType = null;
  private String jsonFileName = null;
  private static Properties properties = null;
  
  public CentralCollectorMultiPartRequest(String fileName, String persistentFile, String centralCollectorURL, String authId, String appId, String scanType, String orgId, String payloadType) {
    logger.info("Central collector request with Scanner detail.. . : " + centralCollectorURL);
    this.jsonFileName = fileName;
    this.persistentFile = persistentFile;
    this.centralCollectorURL = centralCollectorURL;
    this.authId = authId;
    this.appId = appId;
    this.scanType = scanType;
    this.orgId = orgId;
    this.payloadType = payloadType;
    //this.data = null;
    
    // We need to store data for persistent file
  }

  @Override
  public Integer call() throws Exception {
    logger.debug("Sending Request ..." + centralCollectorURL);
    
    Scanner in = null;
    Integer responseCode = null;
    CloseableHttpResponse response = null;
    CloseableHttpClient httpClient = null;
    
    try  {
      // Need to take these from result of the first request
      logger.info(" JSON FileName " + jsonFileName);
      // At some point try fluent API from httpCLient
      properties =  KubernetesClientConfig.config();
      if (properties.getProperty("server.ssl.enabled").equals("true")) {
        SSLContextBuilder builder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf = null;
        try {
          builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
          sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (Exception e1) {
          logger.error("SSL Connection Exception : " + e1.getMessage());
        }

        httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
      } else {
        httpClient = HttpClients.createDefault();
      }
      
      HttpPost post = new HttpPost(centralCollectorURL);
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
      StringBody bauthId = new StringBody(authId, ContentType.TEXT_PLAIN);
      builder.addPart("authId", bauthId);
      StringBody bappId = new StringBody(appId, ContentType.TEXT_PLAIN);
      builder.addPart("appId", bappId);
      StringBody scanT = new StringBody(scanType, ContentType.TEXT_PLAIN);
      builder.addPart("scanType", scanT);
      StringBody borgId = new StringBody(orgId, ContentType.TEXT_PLAIN);
      builder.addPart("orgId", borgId);
      StringBody btype = new StringBody(payloadType, ContentType.TEXT_PLAIN);
      builder.addPart("type", btype);
      FileBody fb = new FileBody(new File(jsonFileName), ContentType.DEFAULT_BINARY);
      builder.addPart("json", fb);

      post.setEntity(builder.build());
      response = httpClient.execute(post); // Execute the method.
      HttpEntity entity = response.getEntity();
      in = new Scanner(entity.getContent());
      while (in.hasNext()) {
        logger.info(in.next());
      }
      EntityUtils.consume(entity); // Release all resources held by the httpEntity,
      responseCode = response.getStatusLine().getStatusCode(); // Getting status code
      logger.info("NetFlows Result Return Code : " + responseCode);
      
      String nonGZ = jsonFileName.substring(0, jsonFileName.length() - 3);
      String tmpFile = nonGZ+".tmp";
      logger.info("File to remove : " + nonGZ + ":" + jsonFileName + tmpFile);
      
      //File file = new File(jsonFileName);
      //file.delete();
      File file1 = new File(nonGZ);
      file1.delete();
      File file2 = new File(tmpFile);
      file2.delete();
    } catch (IOException e) {
      // When not able to post request write to persistent file
      logger.error("Not able to send request : " + e.getMessage());
      String filePath = getPersistentFilePath();
      logger.error("Writing to persistent store instead : " + filePath);
      writePersistentFile(filePath, jsonFileName);
    }
    
    logger.info("CentralCollectorMultiPartRequest responseCode : " + responseCode);
    return responseCode;

  }
  private String getPersistentFilePath() {
    StringBuilder persistentFilePath = new StringBuilder();
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd/HH");
    Date date = new Date();
    persistentFilePath.append(this.persistentFile).append("/").append(dateFormat.format(date))
        .append("/").append(UUID.randomUUID()).append(".json");
    logger.info("Writing to persistent path");
    return persistentFilePath.toString();
  }

   private void writePersistentFile(String path, String payLoad) {
    File localFile = new File(path);
    localFile.getParentFile().mkdirs();
    try (Writer writer =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(localFile), "utf-8"))) {
      writer.write(payLoad);
      writer.flush();
      writer.close();
    } catch (IOException e) {
      logger.error("CAV-4030 : Error in writing persistent file " + e.getMessage());
    }
  }

}
