package com.caveonix.kubernetesmgmt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caveonix.kubernetesmgmt.config.KubernetesClientConfig;
import com.caveonix.kubernetesmgmt.config.KubernetesExtractConstants;
import com.caveonix.kubernetesmgmt.data.request.CentralCollectorRequest;
import com.google.gson.JsonObject;
import com.caveonix.kubernetesmgmt.data.request.CentralCollectorMultiPartRequest;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.util.Map.Entry;

public class KubeMasterCompliance implements Callable {

	private final static Logger logger = LoggerFactory.getLogger(KubeMasterCompliance.class);
	private ArrayBlockingQueue<String[]> extractQueue = new ArrayBlockingQueue<String[]>(500);
	private static SessionInfo sessionInfo = new SessionInfo();
	private static Properties properties = new Properties();
	private int NUM_OF_EXECUTORS = 3;
	private ExecutorService executorService;
	private static final String CONTAINERWORKLOAD = "ContainerAssetMgmt/";
	private static String PERSISTENT_FILE = null;
	private static String LOCAL_FILE_PATH = null;
	private String sessionId;
	private static String persistentLocation = "/tmp" + "/scannerResults";
	private static final String SCANNER_RESULTS = "containerScannerResults/";
	private String ccRequestURL;
	private int agentId;
	private String userName;
	private String passWord;
	private String kubeMaster;
	private int sourceId;
	private String assetId;
	private String authId = "";
	private String appId = "";
	private String orgId = "";
	JSONArray hostJsonArray = new JSONArray();

	public static void main(String[] args) throws Exception {
		//KubeMasterCompliance kubeCompliance = new KubeMasterCompliance();
		// kubeCompliance.getComplianceDetails("caveonix", "10.1.17.7", 22, "Caveon1xVM");
	}

	public KubeMasterCompliance(String orgInfo, String userName, String passWord, String kubeMaster,String assetId,
			int sourceId) throws IOException {
		properties = KubernetesClientConfig.config();
		this.agentId = KubernetesClientConfig.getAgentId();
		this.userName = userName;
		this.passWord = passWord;
		this.kubeMaster = kubeMaster;
		this.sourceId = sourceId;
		this.assetId = assetId;
		String[] orgDetail = orgInfo.split("\\-");
		String activeURL = null;
		try {
			activeURL = GetActiveCC.getActiveCC(properties.getProperty(KubernetesExtractConstants.CC_URL))
					+ KubernetesExtractConstants.CC_API_GETSESSION;
		} catch (JSONException | IOException e) {
			logger.info("Not able to get the CC URL : " + e.getMessage());
		}
		sessionId =	sessionInfo.getSessionDetails(activeURL, agentId, "", orgDetail[0], orgDetail[1], orgDetail[2]);
		this.orgId = orgDetail[0];
		if (sessionId != null) {
			String[] authDetails = sessionId.split("\\|");
			this.authId = authDetails[1];
			logger.info("Authorization ID " + authId);
			logger.info(" ID " + authDetails[0]);
			this.appId = authDetails[0];
		}
	}

	public void addOrgScan(String[] scanDetails) {
		extractQueue.add(scanDetails);
	}

	public String call() throws IOException {
		try {
			getComplianceDetails();
		} catch (Exception e) {
			logger.error("Error in initializing EC2Instances : " + e.getMessage());
		}
		return "";
	}


	public void getComplianceDetails() throws Exception {
		try {
			properties = KubernetesClientConfig.config();
			HashMap ruleMap = new HashMap<>();
			JSONArray assetObject = new JSONArray(assetId);
			boolean checkForvCenter=true;
			
			JSONArray rules = (JSONArray) assetObject.get(1);
			for (int i = 0; i < rules.length(); i++) {
				JSONObject obj = (JSONObject) rules.get(i);
				String rule_unique_key = (String) obj.get("rule_unique_key");
				ruleMap.put(rule_unique_key, obj);
			}
			String requestURL = null;
			String ccURL = null;
			boolean checkStatus = CCStatusRequest
					.checkCCStatus(properties.getProperty(KubernetesExtractConstants.CC_URL));
			if (checkStatus) {
				ccURL = GetActiveCC.getActiveCC(properties.getProperty(KubernetesExtractConstants.CC_URL));
				requestURL = ccURL + SCANNER_RESULTS;
			} else {
				logger.error(" centralCollector is down :");
			}
//			JSONArray assets = new JSONArray();
//			if (!assetObject.get(0).toString().equals("{}")) {
//				assets = (JSONArray) assetObject.get(0);
//			}
//			JSONObject hostObject = new JSONObject();
//			for (int k = 0; k < assets.length(); k++) {
//				hostObject = assets.getJSONObject(k);
//				if (kubeMaster.equals(hostObject.get("network_ip"))) {
//					checkForvCenter = false;
//					break;
//				}
//			}
//			if (checkForvCenter) {
//				try {
//					JSONObject finalJson = new JSONObject();
//					finalJson.put("host_name", kubeMaster);
//					finalJson.put("username", userName);
//					finalJson.put("password", passWord);
//					LOCAL_FILE_PATH = properties.getProperty("persistent.file.store.location");
//					PERSISTENT_FILE = LOCAL_FILE_PATH + "/persistent/asset/";
//					logger.info("Before sending request to central collector for asset insert");
//					CentralCollectorRequest centralCollectorRequest = new CentralCollectorRequest(finalJson.toString(),
//							PERSISTENT_FILE, ccURL + "kubeMgmt/", sessionId);
//					int retCode = centralCollectorRequest.postRequest();
//					logger.info("asset retCode : " + retCode);
//				} catch (Exception e) {
//					logger.error("CAV-3045 : Exception in submitting to Central Collector");
//				}
//			}
			HashMap<String, String> kubeSettings = new HashMap<String, String>();
			JSch jsch = new JSch();
			Session session = jsch.getSession(userName, kubeMaster, 22);
			session.setPassword(passWord);
			session.setConfig("StrictHostKeyChecking", "no");
			System.out.println("Establishing Connection...");
			session.connect();
			String host_name = "";
			host_name = session.getHost();

			HashMap<String, String> API_Server_kubeSettings = new HashMap<String, String>();
			HashMap<String, String> Controller_Manager_kubeSettings = new HashMap<String, String>();
			HashMap<String, String> etcd_kubeSettings = new HashMap<String, String>();
			HashMap<String, String> Kubelet_kubeSettings = new HashMap<String, String>();
			HashMap<String, String> Scheduler_kubeSettings = new HashMap<String, String>();
			HashMap<String, String> kubeProxy_kubeSettings = new HashMap<String, String>();
			HashMap<String, String> config_kubeSettings = new HashMap<String, String>();
			
			API_Server_kubeSettings = getkubeSetting("ps -ef |grep kube-apiserver ;", session);
			Controller_Manager_kubeSettings = getkubeSetting("ps -ef | grep kube-controller-manager ;", session);
			etcd_kubeSettings = getkubeSetting("ps -ef | grep etcd ;", session);
			Kubelet_kubeSettings = getkubeSetting("ps -ef|grep kubelet;", session);
			Scheduler_kubeSettings = getkubeSetting("ps -ef | grep kube-scheduler ;", session);
			kubeProxy_kubeSettings = getkubeSetting("ps -ef | grep kube-proxy", session);
			config_kubeSettings = getkubeSetting("ps -ef | grep kubelet | grep config", session);
			
           getConfileFilePath(API_Server_kubeSettings, Controller_Manager_kubeSettings, etcd_kubeSettings, Kubelet_kubeSettings,
			Scheduler_kubeSettings, kubeProxy_kubeSettings, config_kubeSettings, ruleMap,  session, rules,  host_name);

			session.disconnect();
			System.out.println(kubeSettings.toString());
			String result = "";
			Set<Entry<String, String>> setOfEntries = kubeSettings.entrySet();
			Iterator<Entry<String, String>> itr = setOfEntries.iterator();
			String propValue = "";

			if (API_Server_kubeSettings.containsKey("--anonymous-auth")) {
				propValue = API_Server_kubeSettings.get("--anonymous-auth");
				if (propValue.equals("false")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-1", result, propValue);

			propValue = "";
			if (API_Server_kubeSettings.containsKey("--basic-auth-file")) {
				propValue = API_Server_kubeSettings.get("--basic-auth-file");
				result = "fail";
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-2", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--insecure-allow-any-token")) {
				propValue = API_Server_kubeSettings.get("--insecure-allow-any-token");
				result = "fail";
			} else {
				result = "pass";
			}

			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-3", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--kubelet-https")) {
				propValue = API_Server_kubeSettings.get("--kubelet-https");
				if (propValue.equals("true")) {
					propValue = API_Server_kubeSettings.get("--kubelet-https");
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-4", result, propValue); //
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--insecure-bind-address")) {
				propValue = API_Server_kubeSettings.get("--insecure-bind-address");
				if (propValue == null || propValue.equals("127.0.0.1.")) {
					result = "pass";
				} else {
					result = "fail";
				}
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap,  host_name, "API-5", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--insecure-port")) {
				propValue = API_Server_kubeSettings.get("--insecure-port");
				if (propValue.equals("0")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-6", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--secure-port")) {
				propValue = API_Server_kubeSettings.get("--secure-port");
				if (propValue != null && (Integer.parseInt(propValue) >= 1 && Integer.parseInt(propValue) <= 65535)) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap,host_name, "API-7", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--profiling")) {
				propValue = API_Server_kubeSettings.get("--profiling");
				if (propValue.equals("FALSE")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-8", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--repair-malformed-updates")) {
				propValue = API_Server_kubeSettings.get("--repair-malformed-updates");
				if (propValue.equals("FALSE")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-9", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (!propValue.contains("AlwaysAdmit")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-10", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("AlwaysPullImages")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-11", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("DenyEscalatingExec")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-12", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("SecurityContextDeny")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-13", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--disable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--disable-admission-plugins");
				if (!propValue.contains("NamespaceLifecycle")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-14", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--audit-log-path")) {
				propValue = API_Server_kubeSettings.get("--audit-log-path");
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-15", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--audit-log-maxage")) {
				propValue = API_Server_kubeSettings.get("--audit-log-maxage");
				if (propValue.equals("30")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-16", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--audit-log-maxbackup")) {
				propValue = API_Server_kubeSettings.get("--audit-log-maxbackup");
				if (propValue.equals("10")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-17", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--audit-log-maxsize")) {
				propValue = API_Server_kubeSettings.get("--audit-log-maxsize");
				if (propValue.equals("100")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-18", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--authorization-mode")) {
				propValue = API_Server_kubeSettings.get("--authorization-mode");
				if (propValue.equalsIgnoreCase("AlwaysAllow")) {
					result = "fail";
				} else {
					result = "pass";
				}
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-19", result, propValue);
			}
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--token-auth-file")) {
				result = "fail";
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-20", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--kubelet-certificate-authority")) {
				propValue = API_Server_kubeSettings.get("--kubelet-certificate-authority");
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-21", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--kubelet-client-certificate")
					&& API_Server_kubeSettings.containsKey("--kubelet-client-key")) {
				propValue = API_Server_kubeSettings.get("--kubelet-client-certificate");
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-22", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--service-account-lookup")) {
				propValue = API_Server_kubeSettings.get("--service-account-lookup");
				if (propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-23", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("PodSecurityPolicy")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-24", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--service-account-key-file")) {
				propValue = API_Server_kubeSettings.get("--service-account-key-file");

				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-25", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--etcd-certfile")) {
				propValue = API_Server_kubeSettings.get("--etcd-certfile");
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-26", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("ServiceAccount")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-27", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--tls-cert-file") && kubeSettings.containsKey("--tls-private-key-file")) {
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-28", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--client-ca-file")) {
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-29", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("-etcd-cafile")) {
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-31", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--authorization-mode")) {
				propValue = API_Server_kubeSettings.get("--authorization-mode");
				if (propValue.contains("Node")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-32", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("NodeRestriction")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-33", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--enable-admission-plugins")) {
				propValue = API_Server_kubeSettings.get("--enable-admission-plugins");
				if (propValue.contains("EventRateLimit")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-36", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--request-timeout")) {
				propValue = API_Server_kubeSettings.get("--request-timeout");
				if (propValue.equals("300s")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "API-38", result, propValue);
			propValue = "";
			if (API_Server_kubeSettings.containsKey("--profiling")) {
				propValue = API_Server_kubeSettings.get("--profiling");
				if (propValue.equals("false")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "SCD-1", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--address")) {
				propValue = kubeSettings.get("--address");
				if (propValue.equals("127.0.0.1")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "SCD-2", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--terminated-pod-gc-threshold")) {
				propValue = kubeSettings.get("--terminated-pod-gc-threshold");
				if (propValue.equals("10")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-1", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--profiling")) {
				propValue = kubeSettings.get("--profiling");
				if (propValue.equals("false")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-2", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--use-service-account-credentials")) {
				propValue = kubeSettings.get("--use-service-account-credentials");
				if (propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-3", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--service-account-private-key-file")) {
				propValue = kubeSettings.get("--service-account-private-key-file");
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-4", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--root-ca-file")) {
				propValue = kubeSettings.get("--root-ca-file");
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-5", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("RotateKubeletServerCertificate")) {
				propValue = kubeSettings.get("RotateKubeletServerCertificate");
				if (propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-6", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--address")) {
				propValue = kubeSettings.get("--address");
				if (propValue.equals("127.0.0.1")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COM-7", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--cert-file") && kubeSettings.containsKey("--key-file")) {
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-1", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--client-cert-auth")) {
				propValue = kubeSettings.get("--client-cert-auth");
				if (propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-2", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--auto-tls")) {
				propValue = kubeSettings.get("--client-cert-auth");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-3", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--peer-cert-file") && kubeSettings.containsKey("--peer-key-file")) {
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-4", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--peer-client-cert-auth")) {
				propValue = kubeSettings.get("--peer-client-cert-auth");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-5", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--peer-auto-tls")) {
				propValue = kubeSettings.get("--peer-auto-tls");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-6", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--trusted-ca-file") && kubeSettings.containsKey("--client-ca-file")) {
				propValue = kubeSettings.get("--trusted-ca-file and --client-ca-file");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "ETC-7", result, propValue);
			// ==================================================================================
			propValue = "";
			if (kubeSettings.containsKey("--allow-privileged")) {
				propValue = kubeSettings.get("--allow-privileged");
				if (propValue.equals("false")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-1", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--anonymous-auth")) {
				propValue = kubeSettings.get("--anonymous-auth");
				if (propValue.equals("false")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-2", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--authorization-mode")) {
				propValue = kubeSettings.get("--authorization-mode");
				if (!propValue.equals("AlwaysAllow")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-3", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--read-only-port")) {
				propValue = kubeSettings.get("--read-only-port");
				if (!propValue.equals("AlwaysAllow")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-5", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--streaming-connection-idle-timeout")) {
				propValue = kubeSettings.get("--streaming-connection-idle-timeout");
				if (!propValue.equals("0")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-6", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--protect-kernel-defaults")) {
				propValue = kubeSettings.get("--protect-kernel-defaults");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-7", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--make-iptables-util-chains")) {
				propValue = kubeSettings.get("--make-iptables-util-chains");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-8", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--hostname-override")) {
				result = "fail";
			} else {
				result = "pass";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-9", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--event-qps")) {
				propValue = kubeSettings.get("--event-qps");
				if (!propValue.equals("0")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-10", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--tls-cert-file") && kubeSettings.containsKey("--tls-private-key-file")) {
				result = "pass";
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-11", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--cadvisor-port")) {
				propValue = kubeSettings.get("--cadvisor-port");
				if (!propValue.equals("0")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-12", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("--rotate-certificates")) {
				propValue = kubeSettings.get("--rotate-certificates");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-13", result, propValue);
			propValue = "";
			if (kubeSettings.containsKey("RotateKubeletServerCertificate")) {
				propValue = kubeSettings.get("RotateKubeletServerCertificate");
				if (!propValue.equals("true")) {
					result = "pass";
				} else {
					result = "fail";
				}
			} else {
				result = "fail";
			}
			hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "KUB-14", result, propValue);
						

			String gzFileName = jsonToXmlFile(persistentLocation, sourceId, host_name, host_name,
					hostJsonArray.toString());
			CentralCollectorMultiPartRequest request = new CentralCollectorMultiPartRequest(gzFileName,
					persistentLocation, requestURL, authId, appId, "kubernetesInfra", orgId, "scan");
			request.call();
		} catch (JSchException e) {
			logger.error(e.getMessage());
		} catch (JSONException e) {
			logger.error(e.getMessage());
		}
	}

	public HashMap<String, String> getkubeSetting(String command, Session session) throws Exception {
		HashMap<String, String> kubeSettings = new HashMap<String, String>();
		try {
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (true) {
				while (reader.ready()) {
					String line = reader.readLine();
					if (!line.contains("grep")) {
						String[] kubeOpt = line.split("\\s");
						for (int j = 0; j < kubeOpt.length; j++) {
							String[] keyVal = kubeOpt[j].split("=");
							if (keyVal.length > 1) {
								kubeSettings.put(keyVal[0], keyVal[1]);
								System.out.println(keyVal[0] + "<==>" + keyVal[1]);
							}
						}
					}
				}
				if (channel.isClosed()) {
					// logger.info("Exit Code :" + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					logger.error(ee.getMessage());
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return kubeSettings;
	}

	public void getConfileFilePath(HashMap<String, String> API_Server_kubeSettings,
			HashMap<String, String> Controller_Manager_kubeSettings, HashMap<String, String> etcd_kubeSettings,
			HashMap<String, String> Kubelet_kubeSettings, HashMap<String, String> Scheduler_kubeSettings,
			HashMap<String, String> kubeProxy_kubeSettings, HashMap<String, String> config_kubeSettings,
			HashMap ruleMap, Session session, JSONArray rules, String host_name) throws JSONException {
		try {
			String secondResult = "";
			String result = "";
			secondResult = runForwordCommand(session, kubeProxy_kubeSettings, host_name, "stat -c %a <File Name>",
					"--kubeconfig");
			if (secondResult != null && secondResult.equals("644")) {
				result = "pass";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-23", result, secondResult);
			} else {
				result = "fail";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-23", result, secondResult);
			}

			secondResult = runForwordCommand(session, kubeProxy_kubeSettings, host_name, "stat -c %U:%G <File Name>",
					"--kubeconfig");
			if (secondResult != null && secondResult.equals("root:root")) {
				result = "pass";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-24", result, secondResult);
			} else {
				result = "fail";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-24", result, secondResult);
			}

			secondResult = runForwordCommand(session, Kubelet_kubeSettings, host_name, "stat -c %a <File Name>",
					"--client-ca-file");
			if (secondResult != null && secondResult.equals("644")) {
				result = "pass";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-25", result, secondResult);
			} else {
				result = "fail";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-25", result, secondResult);
			}

			secondResult = runForwordCommand(session, Kubelet_kubeSettings, host_name, "stat -c %a <File Name>",
					"--client-ca-file");
			if (secondResult != null && secondResult.equals("root:root")) {
				result = "pass";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-26", result, secondResult);
			} else {
				result = "fail";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-26", result, secondResult);
			}

			secondResult = runForwordCommand(session, config_kubeSettings, host_name, "stat -c %U:%G <File Name>",
					"--config");
			if (secondResult != null && secondResult.equals("root:root")) {
				result = "pass";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-27", result, secondResult);
			} else {
				result = "fail";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-27", result, secondResult);
			}

			secondResult = runForwordCommand(session, config_kubeSettings, host_name, "stat -c %a <File Name>",
					"--config");
			if (secondResult != null && secondResult.equals("644")) {
				result = "pass";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-28", result, secondResult);
			} else {
				result = "fail";
				hostJsonArray = prepareJsonObject(hostJsonArray, ruleMap, host_name, "COF-28", result, secondResult);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public String runForwordCommand(Session session, HashMap<String, String> kubeSettings, String host_name,
			String command, String key) {
		String value = "";
		try {
			if (kubeSettings.containsKey(key)) {
				command = command.replace("<File Name>", kubeSettings.get(key));
				Channel channel = channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(command);
				channel.setInputStream(null);
				((ChannelExec) channel).setErrStream(System.err);
				InputStream in = channel.getInputStream();
				channel.connect();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				value = reader.readLine();
				channel.disconnect();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return value;
	}
	
	private JSONArray prepareJsonObject(JSONArray hostJsonArray, HashMap ruleMap,
			String host_name, String rule_key, String result, String CurrentVal) throws JSONException {
		if (ruleMap.get(rule_key) != null) {
			JSONObject ntp = new JSONObject();
			ntp.put("rule_key", rule_key);
			ntp.put("rule_result", result);
			ntp.put("asset_name", host_name);
			ntp.put("CurrentVal", CurrentVal);
			ntp.put("host_name", host_name);
			hostJsonArray.put(ntp);
		}else {
			logger.error("Compliance Rule "+rule_key+" not found in host compliance ");
		}
		return hostJsonArray;
	}

	private String jsonToXmlFile(String persistentLocation, int repoId, String physicalHostName, String vmName,
			String jsonArray) {
		String tempVmName = "";
		if (vmName.length() > 0) {
			tempVmName = "_vmName" + vmName;
		}
		String fileName = persistentLocation + "/containerId" + repoId + "_" + physicalHostName + tempVmName
				+ "_kubernetes_CompliankceResult.json";
		String gzFileName = persistentLocation + "/containerId" + repoId + "_" + physicalHostName + tempVmName
				+ "_kubernetes_CompliankceResult.json.gz";
		try {
			File dir = new File(persistentLocation);
			if (!dir.exists()) {
				dir.mkdir();
				logger.info("Local Folder created : " + persistentLocation);
			}
		} catch (Exception e) {
			logger.error("CAV-4040 : Exception in creating persistent folder" + e.getMessage());
		}

		try {

			try (FileWriter fileWriter = new FileWriter(fileName)) {
				fileWriter.write(jsonArray);
			} catch (IOException e) {
				logger.error("CAV-4041 : Exception in creating file" + e.getMessage());
			}
			compressGzipFile(fileName, gzFileName);
		} catch (CaveoCustomExceptions | IOException e) {
			logger.error("CAV-4042 : Exception in creating zip file and json conversion" + e.getMessage());
		}
		return gzFileName;
	}

	private static void compressGzipFile(String file, String gzipFile) throws CaveoCustomExceptions, IOException {
		try {
			System.out.println("File name :" + file);
			System.out.println("GZFile name :" + gzipFile);
			FileInputStream fis = new FileInputStream(file);
			FileOutputStream fos = new FileOutputStream(gzipFile);
			GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) != -1) {
				gzipOS.write(buffer, 0, len);
			}
			gzipOS.close();
			fos.close();
			fis.close();
			logger.info("gzipped the scanner result : " + gzipFile);
		} catch (IOException e) {
			logger.error("Exception in GZIP " + e.getMessage());
		} finally {
			Files.deleteIfExists(Paths.get(file));
		}
	}

}
