 package com.caveonix.kubernetesmgmt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.management.MalformedObjectNameException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.utils.GetActiveCC;
import com.caveonix.kubernetesmgmt.utils.SessionInfo;

public class KubernetesClientConfig 
{
  private final static Logger logger = LoggerFactory.getLogger(KubernetesClientConfig.class);
  private static KubernetesClientConfig config;
  private final static Properties properties = new Properties();
  private final static Properties systemProperties = System.getProperties();
  private static String centralCollectorURL ="";
  private static String newCentralCollectorURL ="";
  private static GetActiveCC getActiveCC = new GetActiveCC();
  private static String activeURL = "";
  private static int agentId;
  private static SessionInfo session = new SessionInfo();


  public static Properties config() throws IOException {
    if (config == null) {
      config = new KubernetesClientConfig();
    }
    return config.properties();
  }
    
  private Properties properties() {
    return this.properties;
  }
  
  public static int getAgentId() {
    return agentId;
  }
  
  public static void initialize()  throws IOException, JSONException {
    logger.debug("Loading properties ... ");
    
    String localCentralCollectorURL = System.getProperty("central.collector"); // getting central collector url
    String propFile = System.getProperty("PROP_FILE"); // getting property file
    
    if (propFile == null) {
      // throw new CaveoCustomExceptions("No Property file defined",
      // "CAV-3019");
      logger.error("CAV-3019 : No Property file defined");
    }
    
    try (final InputStream in = new FileInputStream(propFile)) {
      properties.load(in); // Reads a property list from the input character stream.
    }
    
    for (final Object key : properties.keySet()) {
      if (systemProperties.containsKey(key)) {
        properties.setProperty(key.toString(), systemProperties.getProperty(key.toString()));
      }
    }
    
    // check if the trust all certs are enabled for all calls
    if (properties.getProperty("trust.all.certs").equals("true")) {
      try {
        SSLContext sc = SSLContext.getInstance("SSL"); 
        sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      } catch (GeneralSecurityException e) {
      } 
    }

    try {
      File dir = new File(KubernetesExtractConstants.PERSISTENT_FILE);
      if (!dir.exists()) {
        dir.mkdir();
        logger.info("Local Folder created : " +  properties.getProperty("persistent.file.store.location"));
      }
    } catch (Exception e) {
      
      logger.error("CAV-3023 : Not able to create the persistent folder " + e.getMessage());
    }
    
    properties.setProperty(KubernetesExtractConstants.CCLEADER, localCentralCollectorURL);
    String newCentralCollectorURL = getCCListDetails(localCentralCollectorURL);
    
    if(newCentralCollectorURL != null) {
    properties.setProperty(KubernetesExtractConstants.CC_URL, newCentralCollectorURL);
    activeURL = GetActiveCC.getActiveCC(newCentralCollectorURL);
    
    if (activeURL != null) {
      try {
        agentId = session.getAgentId(activeURL + KubernetesExtractConstants.CC_API_REGISTERRC);
        System.out.println("Agent ID : " + agentId);
      } catch (MalformedObjectNameException | NullPointerException e) {
        // throw new CaveoCustomExceptions(
        // "Exception in registering agent with central collector hence exiting", e, "CAV-3019");
        logger
            .error("CAV-3019 : Exception in registering agent with central collector hence exiting "
                + e.getMessage());
      } }
    else {
      logger.error("CAV-3020 : Exception Central Collector URL not running ");
      System.exit(1);
    }
    } else {
      logger.error("CAV-3020 : Exception Central Collector URL missing ");
      System.exit(1);
      // throw new CaveoCustomExceptions("Exception Central Collector URL missing ", "CAV-3020");
    }
  }
  
  public static String getCCListDetails(String localCentralCollectorURL) throws IOException, JSONException {
    // if there are multiple Leaders in the central.collector from the -D option
    // the following logic will handle it
    String[] ccList = localCentralCollectorURL.split(",");
    for (int i = 0; i < ccList.length; i++ ) {
      String tmpCentralCollectorURL;
    if (!ccList[i].substring(ccList[i].length()-1, ccList[i].length()).equals("/")) 
        tmpCentralCollectorURL = ccList[i]+"/centralcollector/api/V1/";
    else 
      tmpCentralCollectorURL = ccList[i]+"centralcollector/api/V1/";
    
    centralCollectorURL = centralCollectorURL + tmpCentralCollectorURL +",";
    }
    centralCollectorURL = centralCollectorURL.substring(0, centralCollectorURL.length()-1);
    
    
    // Call the CC Leader URL to get the list of CC for future communication 
    String ccMap = getActiveCC.getCCList(centralCollectorURL);
    System.out.println("** CC :" + ccMap);
    
    if(ccMap != null) {
      JSONArray ccArr = new JSONArray(ccMap);
      
      for (int i = 0; i < ccArr.length(); i++ ) {
        if (ccArr.getJSONObject(i).length() > 0 ) {
        JSONObject ccObj = ccArr.getJSONObject(i);
        String tmpCentralCollectorURL = ccObj.getString("ip_address");
      if (!tmpCentralCollectorURL.substring(tmpCentralCollectorURL.length()-1, tmpCentralCollectorURL.length()).equals("/")) 
          tmpCentralCollectorURL = tmpCentralCollectorURL+"/centralcollector/api/V1/";
      else 
        tmpCentralCollectorURL = tmpCentralCollectorURL+"centralcollector/api/V1/";
      
      newCentralCollectorURL = newCentralCollectorURL + tmpCentralCollectorURL +",";
        }
      }
      newCentralCollectorURL = newCentralCollectorURL.substring(0, newCentralCollectorURL.length()-1);
     
      return newCentralCollectorURL;
    }
    else {
      logger.error("None of the central collecotr url provides are active. Can not start AWS Extractor");
      return null;
    }
  }
  
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
 