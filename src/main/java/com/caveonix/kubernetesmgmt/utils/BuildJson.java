package com.caveonix.kubernetesmgmt.utils;
/****************************************************************************
 * Caveonix Inc CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017 Caveonix All Rights Reserved. Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 ****************************************************************************
 *
 * Description: BuildJson
 * 
 * @version: 1.0
 *
 ****************************************************************************/

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

/**
 * Class Name: BuildJson.java This class Generate JSON, or JavaScript Object Notation. Is a syntax
 * for storing and exchanging data. JSON is text, and we can convert any JavaScript object into
 * JSON, and send JSON to the server.
 * IMPLEMENTS: n/a EXTENDS:n/a. 
 * imported packages:java.util,org.slf4j,com.google.gson.
 * this class has one method generateJSON() for building JSON
 */
public class BuildJson {
  private static final Logger logger = LoggerFactory.getLogger(BuildJson.class);
  private static final String LINE_SEP = "\n";

  /**
   * this method Generate Common Text Data Format used for Asynchronous Browser-Server
   * Communication,it can be easily Sent To and From a Server.
   * @param hostConfig to get all host configuration details.
   * @param softwareConfig to get all Software configuration details.
   * @return String.
   * @throws CaveoCustomExceptions 
   * @exception :NA.
   */
  public static String generateJSON(String hostConfig, HashMap<String, String> softwareConfig,
      String sessionId)  {
    
    try {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
    Date date = new Date();
    String process_ts = dateFormat.format(date);
    long longTime = Instant.now().getEpochSecond(); 
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append(LINE_SEP);
    sb.append("\"assetDetails\": ");
    sb.append(hostConfig); // Convert HashMap Object to JSON string
    sb.append(LINE_SEP);
    sb.append(",");
    sb.append(LINE_SEP);
    sb.append("\"process_ts\" : \"" + longTime + "\" ");
    sb.append(",");
    sb.append(LINE_SEP);
    sb.append("\"assetType\" : \"Container\" ");
    sb.append(",");
    sb.append(LINE_SEP);
    sb.append("\"session_id\" : \"" + sessionId + "\" ");
    sb.append(",");
    sb.append(LINE_SEP);
    sb.append("\"softwareDetails\": [");
    sb.append(LINE_SEP);
    /**
     * This method perform Iteration over entries using For-Each loop.
     */
    if (softwareConfig != null) {
      for (Entry<String, String> entry : softwareConfig.entrySet()) {
        sb.append("{");
        sb.append(LINE_SEP);
        sb.append("\"software\": \"" + entry.getKey() + "\",");
        sb.append(LINE_SEP);
        sb.append("\"version\": \"" + entry.getValue() + "\"");
        sb.append(LINE_SEP);
        sb.append("},");
      }
      sb.setLength(sb.length() - 1); // Remove the "," from the last record
    }
    sb.append("]");
    sb.append(LINE_SEP);
    sb.append("}");
   // logger.debug(sb.toString()); // print the value of a variable at any given point
    return sb.toString();
    } catch (Exception e) {
      logger.error("CAV-3052 : Exception in sending request");
    }
    return null;
  }
  
}


/***********************************
 * Required Format ************************************************
 * 
 * { "assetDetails": { "Memory": "16.0 GB", "IP4": "192.168.1.3", "TotalCores": "8", "MACAddr":
 * "14:10:9F:D9:37:75", "OS": "Mac OS X", "Version": "10.12.6", "NIC Count": "3", "Hostname":
 * "skumar.home" }, "softwareDetails": [{ "software": "Windows 2000", "version": "11.0.0.1" }, {
 * "software": "Windows 2013", "version": "10.0.0.1" } ] }
 * 
 ****************************************************************************************************/
