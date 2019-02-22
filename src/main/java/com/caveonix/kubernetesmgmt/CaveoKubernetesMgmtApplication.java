package com.caveonix.kubernetesmgmt;

import java.io.IOException;
import java.util.Properties;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;

@SpringBootApplication
public class CaveoKubernetesMgmtApplication {

  private static final Logger logger = LoggerFactory.getLogger(CaveoKubernetesMgmtApplication.class);
  private static Properties prop = new Properties();

  
  public static void main(String[] args) throws JSONException {
    try {
      KubernetesClientConfig.initialize();
      //prop = KubernetesClientConfig.config();
    } catch (IOException e) {
      logger.error("Error on reading the property file .." + e.getMessage());
    }
    SpringApplication.run(CaveoKubernetesMgmtApplication.class, args);
  }
}
