package com.caveonix.kubernetesmgmt.utils;

/****************************************************************************
 * Caveonix Inc CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017 Caveonix All Rights Reserved. Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 ****************************************************************************
 *
 * Description: VMNicDetails
 * 
 * @version: 1.0
 *
 ****************************************************************************/
/**
 * CLASS-NAME: VMNicDetails.java This class is used for getting Virtual Machine NIC Details this
 * class get all the Virtual Machine Details Info:vmMac,vmNic,vmIPAddress 
 * IMPLEMENTS:n/a EXTENDS:n/a
 * imports:n/a
 */

public class ContainerDetails {

  private String name;
  private String image;
  private String limitCPU;
  private String limitMem;
  private String requestCPU;
  private String requestMem;
  private String volumnMount;
  private String portProto;
  private String args; 
 
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * @return the image
   */
  public String getImage() {
    return image;
  }
  /**
   * @param image the image to set
   */
  public void setImage(String image) {
    this.image = image;
  }
  /**
   * @return the limitCPU
   */
  public String getLimitCPU() {
    return limitCPU;
  }
  /**
   * @param limitCPU the limitCPU to set
   */
  public void setLimitCPU(String limitCPU) {
    this.limitCPU = limitCPU;
  }
  /**
   * @return the limitMem
   */
  public String getLimitMem() {
    return limitMem;
  }
  /**
   * @param limitMem the limitMem to set
   */
  public void setLimitMem(String limitMem) {
    this.limitMem = limitMem;
  }
  /**
   * @return the requestCPU
   */
  public String getRequestCPU() {
    return requestCPU;
  }
  /**
   * @param requestCPU the requestCPU to set
   */
  public void setRequestCPU(String requestCPU) {
    this.requestCPU = requestCPU;
  }
  /**
   * @return the requestMem
   */
  public String getRequestMem() {
    return requestMem;
  }
  /**
   * @param requestMem the requestMem to set
   */
  public void setRequestMem(String requestMem) {
    this.requestMem = requestMem;
  }
  /**
   * @return the volumnMount
   */
  public String getVolumnMount() {
    return volumnMount;
  }
  /**
   * @param volumnMount the volumnMount to set
   */
  public void setVolumnMount(String volumnMount) {
    this.volumnMount = volumnMount;
  }
  /**
   * @return the portProto
   */
  public String getPortProto() {
    return portProto;
  }
  /**
   * @param portProto the portProto to set
   */
  public void setPortProto(String portProto) {
    this.portProto = portProto;
  }
  /**
   * @return the args
   */
  public String getArgs() {
    return args;
  }
  /**
   * @param args the args to set
   */
  public void setArgs(String args) {
    this.args = args;
  }


}
