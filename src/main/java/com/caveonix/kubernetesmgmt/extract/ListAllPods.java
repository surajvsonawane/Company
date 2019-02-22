package com.caveonix.kubernetesmgmt.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
import com.caveonix.kubernetesmgmt.config.KubernetesExtractConstants;
import com.caveonix.kubernetesmgmt.data.request.CentralCollectorRequest;
import com.caveonix.kubernetesmgmt.utils.BuildJson;
import com.caveonix.kubernetesmgmt.utils.ContainerDetails;
import com.caveonix.kubernetesmgmt.utils.GetActiveCC;
import com.caveonix.kubernetesmgmt.utils.SessionInfo;
import com.caveonix.kubernetesmgmt.utils.VMDetails;
import com.caveonix.kubernetesmgmt.utils.VMNicDetails;
import com.squareup.okhttp.Response;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.DoneableNode;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.client.Callback;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ClientResource;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.utils.InputStreamPumper;
import java.util.concurrent.TimeUnit;


public class ListAllPods implements Callable {

  VMDetails vmDetails = new VMDetails();
  VMNicDetails vmNicDetails = new VMNicDetails();

  private final static Logger logger = LoggerFactory.getLogger(ListAllPods.class);
  private SessionInfo session = new SessionInfo();
  private String sessionId;
  private static Properties properties = null;
  private static String PERSISTENT_FILE = null;
  private static final String CONTAINERWORKLOAD = "ContainerAssetMgmt/";
  private String ccRequestURL;
  private int agentId;
  private String userName;
  private String passWord;
  private String kubeMaster;
  private int sourceId;
  private static String orgInfo = "AS4gT-RTPS74JG";


  public static void main(String[] args) throws IOException {
    ListAllPods allPods =
        new ListAllPods(orgInfo, "admin", "Caveon1xAdmin!","http://10.1.17.7:8001", 1);
        //new ListAllPods(orgInfo, "sen4man@gmail.com", "Caveon1xAdmin!",
          //  "https://caveonix-ad1fd3d1.hcp.eastus.azmk8s.io:443", 1);
    allPods.listPods();
  }

  public ListAllPods() {
  // TODO:
  }

  public ListAllPods(String orgInfo, String userName, String passWord, String kubeMaster,
      int sourceId) throws IOException {
    properties = KubernetesClientConfig.config();
    this.agentId = KubernetesClientConfig.getAgentId();
    this.userName = userName;
    this.passWord = passWord;
    this.kubeMaster = kubeMaster;
    this.sourceId = sourceId;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);

    /*
     * String[] orgDetail = orgInfo.split("\\-"); String activeURL = null; try { activeURL =
     * GetActiveCC.getActiveCC(properties.getProperty(KubernetesExtractConstants.CC_URL)) +
     * KubernetesExtractConstants.CC_API_GETSESSION; } catch (JSONException | IOException e) {
     * logger.info("Not able to get the CC URL : " + e.getMessage()); } sessionId =
     * session.getSessionDetails(activeURL, agentId, "", orgDetail[0], orgDetail[1], orgDetail[2]);
     */

  }

  public String call() throws IOException {
    try {
      listPods();
    } catch (Exception e) {
      logger.error("Error in initializing EC2Instances : " + e.getMessage());
    }
    return "";
  }


  public void listPods() {

    String finalJson = null;
    /*
     * Config config = new ConfigBuilder().build(); config.setClientCertData(CA_CERT_DATA);
     * config.setUsername("senthil"); config.setPassword("0p9o8i7u);P(O*I&U"); config.setMasterUrl(
     * "https://326AA727C15269595FFA49E42EC75016.yl4.us-east-1.eks.amazonaws.com:443");
     * config.setTrustCerts(true);
     * 
     * Config config = new ConfigBuilder() .withMasterUrl(
     * "https://326AA727C15269595FFA49E42EC75016.yl4.us-east-1.eks.amazonaws.com:443")
     * .withTrustCerts(true) .withUsername("senthil") .withPassword("0p9o8i7u);P(O*I&U")
     * .withCaCertData(CA_CERT_DATA) .build();
     */

    /*
     * Config config1 = new ConfigBuilder()
     * .withMasterUrl("https://caveonix-ad1fd3d1.hcp.eastus.azmk8s.io:443") .withTrustCerts(true)
     * .withUsername("sen4man@gmail.com") .withPassword("Caveon1xCustomer!") .build();
     */

    Config config = new ConfigBuilder().withMasterUrl(kubeMaster).withTrustCerts(true)
        .withUsername(userName).withPassword(passWord).build();

    KubernetesClient kube = new DefaultKubernetesClient(config);

    logger.info("Master URL : " + kube.getMasterUrl());
    logger.info("API Version URL : " + kube.getApiVersion());

    NamespaceList nameSpaces = kube.namespaces().list();
    for (Namespace nameSpace : nameSpaces.getItems()) {
      /*
       * System.out.println("NameSpace : " + nameSpace.getMetadata().getName());
       * System.out.println("NameSpace UUID : " + nameSpace.getMetadata().getUid());
       * System.out.println("NameSpace Spec " + nameSpace.getSpec());
       * System.out.println("NameSpace Metadata " + nameSpace.getMetadata());
       * System.out.println("NameSpace Status " + nameSpace.getStatus());
       */
      System.out.println("_______________________________________________________");
      PodList pods = kube.pods().inNamespace(nameSpace.getMetadata().getName()).list();

      // PodList pods = kube.pods().list();
      for (Pod pod : pods.getItems()) {

        ArrayList<VMNicDetails> vmNicDetails = new ArrayList<VMNicDetails>();
        VMNicDetails vn = new VMNicDetails();
        String name = pod.getMetadata().getName();
        String ip = pod.getStatus().getPodIP();
        System.out.println("POD Name " + name);

        // Node
        ClientResource<Node, DoneableNode> clientNode =
            kube.nodes().withName(pod.getSpec().getNodeName());
        Node node = clientNode.get();
        vmDetails.setHostName(name);
        vmDetails.setPhysicalHostIP(pod.getStatus().getHostIP());
        vmDetails.setPhysicalHostName(node.getMetadata().getName());
        vmDetails.setNodeMemory(node.getStatus().getCapacity().get("memory").getAmount());
        vmDetails.setNodeCPU(node.getStatus().getCapacity().get("cpu").getAmount());
        vmDetails.setSourceId(sourceId);

        /*
         * System.out.println("Node Name: " + node.getMetadata().getName());
         * System.out.println("Node Namespace : " + node.getMetadata().getNamespace());
         * System.out.println("Node Annotations : " +
         * node.getMetadata().getAnnotations().toString()); System.out.println("Node Lables : " +
         * node.getMetadata().getLabels().toString()); System.out.println("Node UUID : " +
         * node.getMetadata().getUid()); System.out.println("Node Spec : " +
         * node.getSpec().toString()); System.out.println("Metadata Props : " + node.getMetadata());
         * System.out.println("Status Props : " + node.getStatus());
         * System.out.println("Node Info Props : " + node.getStatus().getNodeInfo());
         * System.out.println("Node IP : " + node.getStatus().getAddresses());
         */

        vmDetails.setToolsRunningStatus(pod.getStatus().getPhase());
        vmDetails.setPowerStatus(pod.getStatus().getPhase());
        vmDetails.setInstanceUuid(name);
        vmDetails.setGuestFullName(name);
        vmDetails.setUuid(pod.getMetadata().getUid());
        vmDetails.setVmPathName(pod.getMetadata().getSelfLink());
        vmDetails.setClusterName(pod.getMetadata().getNamespace());
        // vmDetails.setvAppName(pod.getMetadata().getNamespace());
        vn.setVmIPAddress(pod.getStatus().getPodIP());
        vmNicDetails.add(vn);
        vmDetails.setVmNicDetails(vmNicDetails);
        // Containers
        List<Container> containers = pod.getSpec().getContainers();
        vmDetails.setContainerDetails(getContainerDetails(containers,nameSpace.getMetadata().getName(),
                                                          pod.getMetadata().getName(), kube));

        HashMap<String, String> softwareMap = new HashMap<String, String>();
        try {
          finalJson = BuildJson.generateJSON(vmDetails.getVMDetailsJSON(), softwareMap, sessionId);
          System.out.println(finalJson);
        } catch (Exception e) {
          logger.error("CAV-3044 : Exception in generating the JSON file");
        }


        // Submit to CC
        try {
          String ccactiveURL =
              GetActiveCC.getActiveCC(properties.getProperty(KubernetesExtractConstants.CC_URL))
                  + CONTAINERWORKLOAD;
          CentralCollectorRequest centralCollectorRequest =
              new CentralCollectorRequest(finalJson, PERSISTENT_FILE, ccactiveURL, sessionId);
          int retCode = centralCollectorRequest.postRequest();
          logger.info("asset retCode : " + retCode);
        } catch (Exception e) {

          logger.error("CAV-3045 : Exception in submitting to Central Collector");
        }
      }
    }
    System.out.println("_______________________________________________________");
  }

  private ArrayList<ContainerDetails> getContainerDetails(List<Container> containers, String nameSpace, String podName, KubernetesClient kube) {

    ArrayList<ContainerDetails> containerDetails = new ArrayList<ContainerDetails>();
    for (int i = 0; i < containers.size(); i++) {
      Container container = containers.get(i);
      ContainerDetails containerDetail = new ContainerDetails();
      containerDetail.setName(container.getName());
      containerDetail.setImage(container.getImage());

      if (container.getResources().getRequests() != null) {
        if (container.getResources().getRequests().get("cpu") != null)
          containerDetail
              .setRequestCPU(container.getResources().getRequests().get("cpu").getAmount());
        if (container.getResources().getRequests().get("memory") != null)
          containerDetail
              .setRequestMem(container.getResources().getRequests().get("memory").getAmount());
      }
      if (container.getResources().getLimits() != null) {
        if (container.getResources().getLimits().get("cpu") != null)
          containerDetail.setLimitCPU(container.getResources().getLimits().get("cpu").getAmount());
        if (container.getResources().getLimits().get("memory") != null)
          containerDetail
              .setLimitMem(container.getResources().getLimits().get("memory").getAmount());
      }

      // Arguments
      if (container.getArgs() != null) {
        List<String> argList = container.getArgs();
        String argStr = "";
        for (int j = 0; j < argList.size(); j++) {
          String arg = argList.get(j);
          if (j == 0) {
            argStr = arg;
          } else {
            argStr = argStr + "," + arg;
          }
        }
        containerDetail.setArgs(argStr);
      }

      // Ports and Protocol
      List<ContainerPort> portsProto = container.getPorts();
      String portProtoStr = "";
      for (int j = 0; j < portsProto.size(); j++) {
        ContainerPort containerPort = portsProto.get(j);
        String tmpPortProto = "";
        tmpPortProto = containerPort.getName() + "," + containerPort.getHostIP() + ","
            + containerPort.getHostPort() + "," + containerPort.getContainerPort() + ","
            + containerPort.getProtocol();

        if (j == 0) {
          portProtoStr = tmpPortProto;
        } else {
          portProtoStr = portProtoStr + "|" + tmpPortProto;
        }
      }
      containerDetail.setPortProto(portProtoStr);

      // Vol Mounts
      List<VolumeMount> volMountsList = container.getVolumeMounts();
      String volMounts = "";
      for (int j = 0; j < volMountsList.size(); j++) {
        VolumeMount volMount = volMountsList.get(j);
        String tmpvolMount = "";
        tmpvolMount =
            volMount.getName() + "," + volMount.getReadOnly() + "," + volMount.getMountPath();

        if (j == 0) {
          volMounts = tmpvolMount;
        } else {
          volMounts = volMounts + "|" + tmpvolMount;
        }
      }
      containerDetail.setVolumnMount(volMounts);

      containerDetails.add(containerDetail);
      
      // Logs:
      /*
       * LogWatch watch =
       * kube.pods().inNamespace(nameSpace.getMetadata().getName()).withName(pod.getMetadata().
       * getName()).tailingLines(10).watchLog(System.out); System.out.println("Log Stream :" +
       * watch.getOutput()); try { Thread.sleep(10); } catch (InterruptedException l) {
       * l.printStackTrace(); }
       */

      try {
        // Exec
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        // try (
        ExecWatch ewatch = kube.pods().inNamespace(nameSpace)
            .withName(podName).inContainer(container.getName()).readingInput(System.in)
            .writingOutput(System.out).writingError(System.err).withTTY().exec("/bin/bash -c ls -lt");

        InputStream is = ewatch.getOutput();
        String result = getStringFromInputStream(is);
        System.out.println("CMD Exec : " + result);
      } catch (Exception t) {
        logger.info(t.getMessage());
      }
      
    }
    return containerDetails;
  }

  private static class SimpleListener implements ExecListener {


    @Override
    public void onClose(int code, String reason) {
      System.out.println("The shell will now close.");
    }

    @Override
    public void onFailure(IOException arg0, Response arg1) {
      // TODO Auto-generated method stub
      System.err.println("shell barfed");
    }

    @Override
    public void onOpen(Response arg0) {
      // TODO Auto-generated method stub
      System.out.println("The shell will remain open for 10 seconds.");
    }
  }

  private static class SystemOutCallback implements Callback<byte[]> {
    @Override
    public void call(byte[] data) {
      System.out.print(new String(data));
    }
  }

  // convert InputStream to String
  private static String getStringFromInputStream(InputStream is) {

    BufferedReader br = null;
    StringBuilder sb = new StringBuilder();

    String line;
    try {

      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return sb.toString();

  }

}
