package com.caveonix.kubernetesmgmt;

import java.io.IOException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
import com.caveonix.kubernetesmgmt.extract.KubeMasterComplianceProcessor;
import com.caveonix.kubernetesmgmt.extract.KubernetesExtractProcessor;
import com.caveonix.kubernetesmgmt.utils.KubeMasterCompliance;

@RestController
@RequestMapping("/caveokubeassetmgmt/api/V1")
public class CaveoKubernetesMgmtController {
  
  private static final Logger logger = LoggerFactory.getLogger(CaveoKubernetesMgmtController.class);
  private KubernetesExtractProcessor kubeExtractProcessor = new KubernetesExtractProcessor();
  private KubeMasterComplianceProcessor kubeMasterComplianceProcessor = new KubeMasterComplianceProcessor();
  private Properties prop;

  @PostConstruct
  public void init() throws IOException, JSONException {
    logger.info("Starting the application ...");
    prop = KubernetesClientConfig.config();
    kubeExtractProcessor.initialize();
    kubeMasterComplianceProcessor.initialize();
  
  }
  @GetMapping("/status")
  public ResponseEntity<String> status() {
    logger.info("Status: Alive ");
    return new ResponseEntity<String>("Status Alive", HttpStatus.ACCEPTED);
  }
  
  @PostMapping("/startContainerExtract/")
  public ResponseEntity<String> startContainerExtract(HttpServletRequest request,
      HttpServletResponse response) throws JSONException {
    String payLoad =  request.getParameter("data");
    String orgInfo =  request.getParameter("orgInfo");
    String orgId   =  request.getParameter("orgId");
    String source  =  request.getParameter("source");
    String assetId = "";
 
    JSONArray kubeJSONArray = new JSONArray(payLoad);
   // for (int i = 0; i < kubeJSONArray.length(); i++) {
      try {
        JSONObject kubeDetails = kubeJSONArray.getJSONObject(0);
        String[] scanDetails =
            {orgInfo, kubeDetails.getString("user_name"), kubeDetails.getString("password"), orgId, assetId, 
              kubeDetails.getString("connection_name"), Integer.toString(kubeDetails.getInt("id")) };
        String keyStr = orgInfo + "-" + kubeDetails.getString("connection_name");
        
        if (KubernetesExtractProcessor.checkScanAlreadyRunning(keyStr)) {
          logger.error("Scan is already running for this organization : " + keyStr);
          return new ResponseEntity<String>(
              "AWS Asset scan is already running for this organization :" + keyStr,
              HttpStatus.ALREADY_REPORTED);
        }
        kubeExtractProcessor.addOrgScan(scanDetails);
      } catch (Exception e) {
        logger.error("Error in parsing payload from central collector" + e.getMessage());
      }

   // }
    
    return new ResponseEntity<String>("Accepted asset scan :", HttpStatus.OK);
  }

  @PostMapping("/startKubeCompliance/")
  public ResponseEntity<String> startKubeCompliance(HttpServletRequest request,
      HttpServletResponse response) throws JSONException {
    String payLoad =  request.getParameter("data");
    String orgInfo =  request.getParameter("orgInfo");
    String orgId   =  request.getParameter("orgId");
    String source  =  request.getParameter("source");
    String assetId = request.getParameter("assetId");
    
    JSONArray kubeJSONArray = new JSONArray(payLoad);
    for (int i = 0; i < kubeJSONArray.length(); i++) {
      try {
        JSONObject kubeDetails = kubeJSONArray.getJSONObject(0);
        String[] scanDetails =
            {orgInfo, kubeDetails.getString("user_name"), kubeDetails.getString("password"), orgId, assetId, 
              kubeDetails.getString("connection_name"), Integer.toString(kubeDetails.getInt("id")) };
        kubeMasterComplianceProcessor.addOrgScan(scanDetails);
      } catch (Exception e) {
        logger.error("Error in parsing payload from central collector" + e.getMessage());
      }

    }
    
    return new ResponseEntity<String>("Accepted asset scan :", HttpStatus.OK);
  }
}
