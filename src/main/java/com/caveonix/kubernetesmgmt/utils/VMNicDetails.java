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

public class VMNicDetails {

  private String vmMac;
  private String vmNic;
  private String vmIPAddress;

  public String getVmMac() {
    return vmMac;
  }

  /**
   * this method set VM MAC
   * 
   * @param vmMac
   */
  public void setVmMac(String vmMac) {
    this.vmMac = vmMac;
  }

  public String getVmNic()// used for getting VmNic
  {
    return vmNic;
  }

  /**
   * this method set VmNic
   * 
   * @param vmNic
   */
  public void setVmNic(String vmNic) {
    this.vmNic = vmNic;
  }

  public String getVmIPAddress()// used for getting VmIPAddress
  {
    return vmIPAddress;
  }

  /**
   * this method set VmIP address
   * 
   * @param vmIPAddress
   */
  public void setVmIPAddress(String vmIPAddress) {
    this.vmIPAddress = vmIPAddress;
  }

}
