package com.caveonix.kubernetesmgmt.extract;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Node;
import io.kubernetes.client.models.V1NodeList;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KubernetesExtractProcessor implements Runnable {

  private final static Logger logger = LoggerFactory.getLogger(KubernetesExtractProcessor.class);
  private ArrayBlockingQueue<String[]> extractQueue = new ArrayBlockingQueue<String[]>(500);
  private int NUM_OF_EXECUTORS = 3;
  private ExecutorService executorService;
  private Properties prop = null;
  private static HashMap<String, String> extractDetails = new HashMap<String, String>();
  
  public void initialize() {
    executorService = Executors.newFixedThreadPool(NUM_OF_EXECUTORS);
    Thread t = new Thread(this);
    t.start();
  }
  
  public void addOrgScan(String[] scanDetails) {
    extractQueue.add(scanDetails);
  }

  @Override
  public void run() {
    while (true) {
      try { 
        String scanDetails[] = extractQueue.take(); // Retrieves and removes head of the queue.
        logger.info("Taking message from the queue");
        startExtract(scanDetails);
      } catch (InterruptedException ie) {
        logger.info("Worker thread " + Thread.currentThread().getName() + " Interrupted...");
        break;
      } catch (Exception e) {
        logger.error("Unknown error occured...", e);
      }
    }
    logger.info(this.getClass().getSimpleName() + "Terminated...");
  }
  
  private void startExtract(String[] scanDetails) throws JSONException, IOException {
    System.out.println("Start Extract ....");
    String vpcId  = null;
    String  startTime  = null;
    String endTime  = null;
    
    
    String orgInfo = scanDetails[0];
    String userName = scanDetails[1];
    String passWord = scanDetails[2];
    String orgId = scanDetails[3];
    String assetId = scanDetails[4];
    String kubeMaster  = scanDetails[5];
    String sourceId  = scanDetails[6];
    
    if (scanDetails.length > 7 ) {
       vpcId  = scanDetails[7];
       startTime  = scanDetails[8];
       endTime  = scanDetails[9];
    }
    
    String keyStr = orgInfo +"-"+kubeMaster;
    
    String executorName = "";
    extractDetails.put(keyStr, executorName);
    Future<String> future = null;
    try {
      
        //future = executorService.submit(new EC2Instances(userName, passWord,orgInfo, region, sourceId));
      future = executorService.submit(new ListAllPods(orgInfo, userName, passWord, kubeMaster, Integer.parseInt(sourceId)));
      
      extractDetails.remove(future.get());
      
      if (extractDetails.containsKey(keyStr))
        extractDetails.remove(keyStr);
      
    } catch (InterruptedException | ExecutionException  e) {
      logger.error("CAV-5019:Exception in extracting " + e.getMessage());
    }
  }
  
  public static boolean checkScanAlreadyRunning(String orgInfo) {
    if(extractDetails != null && extractDetails.containsKey(orgInfo)) 
      return true;
    else
      return false;
  }
  
  public static  void removeFromList(String orgInfo) {
    if(extractDetails != null && extractDetails.containsKey(orgInfo)) 
      extractDetails.remove(orgInfo);
  }
  
  
  
  public static void main(String[] args) throws IOException, ApiException{
      KubernetesExtractProcessor ke = new KubernetesExtractProcessor() ;
        ke.getKubeDetails();
    }
    
    public  void getKubeDetails() throws  IOException, ApiException {
      
     ApiClient client = Config.fromUserPassword("https://10.1.17.3", "admin", "Caveon1xAdmin"); //defaultClient();
     // ApiClient client = Config.fromUserPassword("https://caveonix-ad1fd3d1.hcp.eastus.azmk8s.io:443",
         // "sen4man@gmail.com", "Caveon1xCustomer!",false); //defaultClient();
      Configuration.setDefaultApiClient(client);

      CoreV1Api api = new CoreV1Api();
      V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null);
      for (V1Pod item : list.getItems()) {
          System.out.println(item.getMetadata().toString());
          System.out.println(item.getSpec().toString());
          item.getSpec().getDnsConfig().getOptions().listIterator(0);
          System.out.println("----------------------");
      }
      
      V1NodeList nodelist = api.listNode(null, null, null, null, null, null, null, null, null);
      
      System.out.println("***********************************");
      
      for (V1Node node : nodelist.getItems()) {
        System.out.println(node.getMetadata().toString());
        System.out.println(node.getSpec().toString());
      }
    }
    
}
