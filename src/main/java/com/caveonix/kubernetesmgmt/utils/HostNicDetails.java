package com.caveonix.kubernetesmgmt.utils;
/****************************************************************************
 * Caveonix Inc CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017 Caveonix All Rights Reserved. Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 ****************************************************************************
 *
 * Description: HostNicDetails
 * 
 * @version: 1.0
 *
 ****************************************************************************/


/**
 * CLASS-NAME: HostNicDetails.java 
 * This class is used for getting HostNic Details this class get all
 * the Host Details info:physicalMac,physicalNic,physicalKey,physicalHostIP,portGroupKey
 * portDynamicType,portSwitchUUID, portGroup 
 * IMPLEMENTS:n/a EXTENDS:n/a
 */
public class HostNicDetails {
  private String physicalMac;
  private String physicalNic;
  private String physicalKey;
  private String physicalHostIP;
  private String portDynamicType;
  private String portGroupKey;
  private String portSwitchUUID;
  private String portGroup;
  private String port;

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public String getPhysicalMac() // used for getting physicalMac of host
  {
    return physicalMac;
  }

  /**
   * this method used for set PhysicalMac
   * 
   * @param physicalMac
   */
  public void setPhysicalMac(String physicalMac) {
    this.physicalMac = physicalMac;
  }

  public String getPhysicalNic()// used for getting PhysicalNic of Host
  {
    return physicalNic;
  }

  /**
   * this method set PhysicalNic
   * 
   * @param physicalNic
   */
  public void setPhysicalNic(String physicalNic) {
    this.physicalNic = physicalNic;
  }

  public String getPhysicalKey()// used for getting PhysicalKey of host
  {
    return physicalKey;
  }

  /**
   * this method used for setting PhysicalKey of Host
   * 
   * @param physicalKey
   */
  public void setPhysicalKey(String physicalKey) {
    this.physicalKey = physicalKey;
  }

  public String getPhysicalHostIP() // used for getting Physical Host IP
  {
    return physicalHostIP;
  }

  /**
   * this method used for set PhysicalHostIP
   * 
   * @param physicalHostIP
   */
  public void setPhysicalHostIP(String physicalHostIP) {
    this.physicalHostIP = physicalHostIP;
  }

  public String getPortDynamicType() // used for getting Port Dynamic Type
  {
    return portDynamicType;
  }

  /**
   * this method used for setting Port Dynamic Type
   * 
   * @param portDynamicType
   */
  public void setPortDynamicType(String portDynamicType) {
    this.portDynamicType = portDynamicType;
  }

  public String getPortGroupKey()// used for getting Port Group Key
  {
    return portGroupKey;
  }

  /**
   * this method used for setting Port Group Key
   * 
   * @param portGroupKey
   */
  public void setPortGroupKey(String portGroupKey) {
    this.portGroupKey = portGroupKey;
  }

  public String getPortSwitchUUID() // used for getting PortSwitchUUID
  {
    return portSwitchUUID;
  }

  /**
   * this method used for set PortSwitchUUID
   * 
   * @param portSwitchUUID
   */
  public void setPortSwitchUUID(String portSwitchUUID) {
    this.portSwitchUUID = portSwitchUUID;
  }

  public String getPortGroup() // for getting PortGroup
  {
    return portGroup;
  }

  /**
   * this method used for Setting PortGroup
   * 
   * @param portGroup
   */
  public void setPortGroup(String portGroup) {
    this.portGroup = portGroup;
  }

}
