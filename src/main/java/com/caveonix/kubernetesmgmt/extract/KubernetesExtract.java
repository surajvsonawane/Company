package com.caveonix.kubernetesmgmt.extract;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.auth.ApiKeyAuth;
import io.kubernetes.client.models.V1NamespaceList;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
import com.caveonix.kubernetesmgmt.config.KubernetesExtractConstants;
import com.caveonix.kubernetesmgmt.utils.GetActiveCC;
import com.caveonix.kubernetesmgmt.utils.SessionInfo;

public class KubernetesExtract implements Callable {

  private final static Logger logger = LoggerFactory.getLogger(KubernetesExtract.class);
  private String sessionId;
  private static Properties properties = null;
  private static String PERSISTENT_FILE = null;
  private static final String AWSWORKLOAD = "AWSAssetMgmt/";
  private SessionInfo session = new SessionInfo();
  private String ccRequestURL;
  private int agentId;
  private String orgInfo = "AS4gT-RTPS74JG";

  public static void main(String[] args) throws IOException, ApiException {
    KubernetesExtract ke = new KubernetesExtract();
    ke.extractAll();
  }

  public KubernetesExtract() {

  }

  public KubernetesExtract(String orgInfo) throws IOException {
    properties = KubernetesClientConfig.config();
    this.agentId = KubernetesClientConfig.getAgentId();

    String[] orgDetail = orgInfo.split("\\-");
    String activeURL = null;
    try {
      activeURL = GetActiveCC.getActiveCC(properties.getProperty(KubernetesExtractConstants.CC_URL))
          + KubernetesExtractConstants.CC_API_GETSESSION;
    } catch (JSONException | IOException e) {
      logger.info("Not able to get the CC URL : " + e.getMessage());
    }

    sessionId =
        session.getSessionDetails(activeURL, agentId, "", orgDetail[0], orgDetail[1], orgDetail[2]);
  }

  @Override
  public String call() throws IOException {
    try {
      extractAll();
    } catch (Exception e) {
      logger.error("Error in initializing EC2Instances : " + e.getMessage());
    }

    return this.orgInfo;
  }

  public void extractAll() throws IOException, ApiException {

    try {
      /*
       * ApiClient client = Config.defaultClient();
       * //Config.fromUserPassword("https://10.1.17.3:6443", "admin", "Caveon1xAdmin",false); //
       * client.setBasePath("https://10.1.17.3:6443"); client.setUsername("admin");
       * client.setPassword("Caveon1xAdmin"); client.setVerifyingSsl(false);
       */

      String credentials = new String(Base64.getEncoder().encode("admin:Caveon1xAdmin".getBytes(Charset.forName("ISO-8859-1"))));
      ApiClient client = Configuration.getDefaultApiClient();
      client.setBasePath("http://10.1.17.7:8001");
      client.setVerifyingSsl(false);
      ApiKeyAuth fakeBearerToken = (ApiKeyAuth) client.getAuthentication("BearerToken");
      fakeBearerToken.setApiKey(credentials);
      fakeBearerToken.setApiKeyPrefix("Basic");


      // ApiClient client =
      //Config.fromUserPassword("https://caveonix-ad1fd3d1.hcp.eastus.azmk8s.io:443",
      // "sen4man@gmail.com", "Caveon1xCustomer!",false); //defaultClient();
      Configuration.setDefaultApiClient(client);

      CoreV1Api api = new CoreV1Api();
      try {
      V1NamespaceList nameSpace =
          api.listNamespace(null, null, null, null, null, null, null, null, null);
      V1PodList list =
          api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
      for (V1Pod item : list.getItems()) {
        System.out.println(item.getMetadata().toString());
        System.out.println(item.getSpec().toString());
        System.out.println("----------------------");
      }

      } catch (Exception e) {
        e.printStackTrace();
      }
      V1NodeList nodelist = api.listNode(null, null, null, null, null, null, null, null, null);

      System.out.println("***********************************");

      for (V1Node node : nodelist.getItems()) {
        System.out.println(node.getMetadata().toString());
        System.out.println(node.getSpec().toString());
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
