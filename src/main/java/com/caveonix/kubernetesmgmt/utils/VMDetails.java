package com.caveonix.kubernetesmgmt.utils;
/****************************************************************************
 * Caveonix Inc CONFIDENTIAL INFORMATION
 *
 * Copyright (c) 2017 Caveonix All Rights Reserved. Unauthorized reproduction, transmission, or
 * distribution of this software is a violation of applicable laws.
 *
 ****************************************************************************
 *
 * Description: VMDetails
 * 
 * @version: 1.0
 *
 ****************************************************************************/

import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * CLASS-NAME: VMDetails.java This class is used for getting Virtual Machine Details this class get
 * all the VM Details info:guestFullName, guestId, guestFamily,instanceUuid,memorySizeMB,numCpu,
 * numEthernetCards,
 * numVirtualDisks,uuid,hostName,vmPathName,vmName,vmFolderName,physicalHostName,physicalHostIP,hostNicDetails,vmNicDetails
 * IMPLEMENTS:n/a EXTENDS:n/a
 */
public class VMDetails {

  private String physicalHostName;// Physical Host name
  private Long physicalHostMem;// Physical host cores
  private Integer physicalHostCores;// Physical host cores
  private String physicalHostIP;// Network details for physical host
  private ArrayList<HostNicDetails> hostNicDetails;// Physical host datafolder
  private String phDatacenter;// Network details for VM
  private String vmFolderName;// Guest Folder name
  private String vmName;// VM name
  private String guestFullName;// VM full name
  private String guestId;// Guest ID
  private String guestFamily;// Guest family to check if its windows or linux
  private String instanceUuid;// VM uuid
  private Integer memorySizeMB;// VM memory in MB
  private Integer numCpu;// Number of cores for vm
  private Integer numEthernetCards;// Number of Ethernet
  private Integer numVirtualDisks; // Number of Virtual disks
  private String uuid;// Host UUID
  private String vmPathName;// VM path
  private String hostName;// VM host name
  private String macAddress;// VM MAC Address
  private String operatingSystem; // Physical host memory in Bytes
  private ArrayList<VMNicDetails> vmNicDetails;
  private String toolsRunningStatus;
  private Integer vCenterId;
  private Integer sourceId;
  private String[] portGroups;
  private Integer remoteCollectorId;
  private String resourcePoolName;
  private String clusterName;
  private String powerStatus; 
  private String vAppName;
  private String scanFlag;
  private ArrayList<ContainerDetails> containerDetails;
  private String nodeMemory;
  private String nodeCPU;
  
  
  /**
   * Gets the value of the remoteCollectorId property.
   * 
   * @return remoteCollectorId possible object is {@link Integer}
   */

  public Integer getRemoteCollectorId() {
    return remoteCollectorId;
  }

  /**
   * Sets the value of the remoteCollectorId property.
   * 
   * @param remoteCollectorId allowed object is {@link Integer}
   */
  public void setRemoteCollectorId(Integer remoteCollectorId) {
    this.remoteCollectorId = remoteCollectorId;
  }

  /**
   * Gets the value of the vCenterId property.
   * 
   * @return vCenterId possible object is {@link Integer}
   */

  public Integer getVCenterId() {
    return vCenterId;
  }

  /**
   * Sets the value of the vCenterId property.
   * 
   * @param vCenterId allowed object is {@link Integer}
   */
  public void setVCenterId(Integer VCenterId) {
    vCenterId = VCenterId;
  }

  /**
   * Gets the value of the macAddress property.
   * 
   * @return macAddress possible object is {@link String}
   */
  public String getMacAddress() {
    return macAddress;
  }

  /**
   * Sets the value of the macAddress property.
   * 
   * @param macAddress allowed object is {@link String}
   */
  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  /**
   * Sets the value of the physicalHostCores property.
   * 
   * @param physicalHostCores allowed object is {@link Integer}
   */

  public void setPhysicalHostCores(Integer physicalHostCores) {
    this.physicalHostCores = physicalHostCores;
  }

  /**
   * Gets the value of the guestFullName property.
   * 
   * @return guestFullName possible object is {@link String}
   */

  public String getGuestFullName() {
    return guestFullName;
  }

  /**
   * Sets the value of the guestFullName property.
   * 
   * @param guestFullName allowed object is {@link String}
   */

  public void setGuestFullName(String guestFullName) {
    this.guestFullName = guestFullName;
  }

  /**
   * Gets the value of the guestId property.
   * 
   * @return guestId possible object is {@link String}
   */

  public String getGuestId() {
    return guestId;
  }

  /**
   * Sets the value of the guestId property.
   * 
   * @param guestId allowed object is {@link String}
   */
  public void setGuestId(String guestId) {
    this.guestId = guestId;
  }

  /**
   * Gets the value of the instanceUuid property.
   * 
   * @return instanceUuid possible object is {@link String}
   */

  public String getInstanceUuid()// getting Instance Uuid
  {
    return instanceUuid;
  }

  /**
   * Sets the value of the instanceUuid property.
   * 
   * @param instanceUuid allowed object is {@link String}
   */

  public void setInstanceUuid(String instanceUuid) {
    this.instanceUuid = instanceUuid;
  }

  /**
   * Gets the value of the memorySizeMB property.
   * 
   * @return memorySizeMB possible object is {@link Integer}
   */

  public Integer getMemorySizeMB() {
    return memorySizeMB;
  }

  /**
   * Sets the value of the memorySizeMB property.
   * 
   * @param memorySizeMB allowed object is {@link Integer}
   */

  public void setMemorySizeMB(Integer memorySizeMB) {
    this.memorySizeMB = memorySizeMB;
  }

  /**
   * Gets the value of the numCpu property.
   * 
   * @return numCpu possible object is {@link Integer}
   */

  public Integer getNumCpu() {
    return numCpu;
  }

  /**
   * Sets the value of the numCpu property.
   * 
   * @param numCpu allowed object is {@link Integer}
   */
  public void setNumCpu(Integer numCpu) {
    this.numCpu = numCpu;
  }

  /**
   * Gets the value of the numEthernetCards property.
   * 
   * @return numEthernetCards possible object is {@link Integer}
   */

  public Integer getNumEthernetCards() {
    return numEthernetCards;
  }

  /**
   * Sets the value of the numEthernetCards property.
   * 
   * @param numEthernetCards allowed object is {@link Integer}
   */

  public void setNumEthernetCards(Integer numEthernetCards) {
    this.numEthernetCards = numEthernetCards;
  }

  /**
   * Gets the value of the numVirtualDisks property.
   * 
   * @return numVirtualDisks possible object is {@link Integer}
   */

  public Integer getNumVirtualDisks() // getting number of Virtual Disks
  {
    return numVirtualDisks;
  }

  /**
   * Sets the value of the numVirtualDisks property.
   * 
   * @param numVirtualDisks allowed object is {@link Integer}
   */

  public void setNumVirtualDisks(Integer numVirtualDisks) {
    this.numVirtualDisks = numVirtualDisks;
  }

  /**
   * Gets the value of the uuid property.
   * 
   * @return uuid possible object is {@link String}
   */

  public String getUuid() // getting Uuid
  {
    return uuid;
  }

  /**
   * Sets the value of the uuid property.
   * 
   * @param uuid allowed object is {@link String}
   */

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Gets the value of the vmPathName property.
   * 
   * @return vmPathName possible object is {@link String}
   */

  public String getVmPathName() // getting VmPathName
  {
    return vmPathName;
  }

  /**
   * Sets the value of the portGroups property.
   * 
   * @param portGroups allowed object is {@link String[] }
   */

  public void setVmPathName(String vmPathName) {
    this.vmPathName = vmPathName;
  }

  /**
   * Gets the value of the hostName property.
   * 
   * @return hostName possible object is {@link String}
   */

  public String getHostName()// getting Host name
  {
    return hostName;
  }

  /**
   * Sets the value of the hostName property.
   * 
   * @param hostName allowed object is {@link String}
   */
  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  /**
   * Gets the value of the vmName property.
   * 
   * @return vmName possible object is {@link String}
   */

  public String getVmName() // getting VMName
  {
    return vmName;
  }

  /**
   * Sets the value of the vmName property.
   * 
   * @param vmName allowed object is {@link String}
   */

  public void setVmName(String vmName) {
    this.vmName = vmName;
  }

  /**
   * Gets the value of the physicalHostName property.
   * 
   * @return physicalHostName possible object is {@link String}
   */

  public String getPhysicalHostName()// getting Physical Host name
  {
    return physicalHostName;
  }

  /**
   * Sets the value of the physicalHostName property.
   * 
   * @param physicalHostName allowed object is {@link String}
   */

  public void setPhysicalHostName(String physicalHostName) {
    this.physicalHostName = physicalHostName;
  }

  /**
   * Gets the value of the vmFolderName property.
   * 
   * @return vmFolderName possible object is {@link String}
   */
  public String getVmFolderName() // getting vm folder name
  {
    return vmFolderName;
  }

  /**
   * Sets the value of the vmFolderName property.
   * 
   * @param vmFolderName allowed object is {@link String}
   */
  public void setVmFolderName(String vmFolderName) {
    this.vmFolderName = vmFolderName;
  }

  /**
   * Gets the value of the physicalHostMem property.
   * 
   * @return physicalHostMem possible object is {@link Long}
   */
  public Long getPhysicalHostMem() {
    return physicalHostMem;
  }

  /**
   * Sets the value of the physicalHostMem property.
   * 
   * @param physicalHostMem allowed object is {@link Long}
   */
  public void setPhysicalHostMem(Long physicalHostMem) {
    this.physicalHostMem = physicalHostMem;
  }

  /**
   * Gets the value of the physicalHostCores property.
   * 
   * @return physicalHostCores possible object is {@link Integer}
   */
  public Integer getPhysicalHostCores() // getting physical Host Cores
  {
    return physicalHostCores;
  }

  /**
   * Gets the value of the physicalHostIP property.
   * 
   * @return physicalHostIP possible object is {@link String}
   */
  public String getPhysicalHostIP()// getting physical host IP
  {
    return physicalHostIP;
  }

  /**
   * Sets the value of the physicalHostIP property.
   * 
   * @param physicalHostIP allowed object is {@link String}
   */
  public void setPhysicalHostIP(String physicalHostIP) {
    this.physicalHostIP = physicalHostIP;
  }

  /**
   * Gets the value of the phDatacenter property.
   * 
   * @return phDatacenter possible object is {@link String}
   */
  public String getPhDatacenter() {
    return phDatacenter;
  }

  /**
   * Sets the value of the phDatacenter property.
   * 
   * @param phDatacenter allowed object is {@link String}
   */
  public void setPhDatacenter(String phDatacenter) {
    this.phDatacenter = phDatacenter;
  }

  /**
   * Gets the value of the guestFamily property.
   * 
   * @return guestFamily possible object is {@link String}
   */
  public String getGuestFamily() {
    return guestFamily;
  }

  /**
   * Sets the value of the guestFamily property.
   * 
   * @param guestFamily allowed object is {@link String}
   */
  public void setGuestFamily(String guestFamily) {
    this.guestFamily = guestFamily;
  }

  /**
   * Gets the value of the vmNicDetails property.
   * 
   * @return vmNicDetails possible object is {@link ArrayList<VMNicDetails>}
   */
  public ArrayList<VMNicDetails> getVmNicDetails() {
    return vmNicDetails;
  }

  /**
   * Sets the value of the vmNicDetails property.
   * 
   * @param vmNicDetails allowed object is {@link ArrayList<VMNicDetails>}
   */
  public void setVmNicDetails(ArrayList<VMNicDetails> vmNicDetails) {
    this.vmNicDetails = vmNicDetails;
  }

  /**
   * Gets the value of the hostNicDetails property.
   * 
   * @return hostNicDetails possible object is {@link ArrayList<HostNicDetails>}
   */
  public ArrayList<HostNicDetails> getHostNicDetails() {
    return hostNicDetails;
  }

  /**
   * Sets the value of the hostNicDetails property.
   * 
   * @param hostNicDetails allowed object is {@link ArrayList<HostNicDetails> }
   */
  public void setHostNicDetails(ArrayList<HostNicDetails> hostNicDetails) {
    this.hostNicDetails = hostNicDetails;
  }

  /**
   * Gets the value of the toolsRunningStatus property.
   * 
   * @return toolsRunningStatus possible object is {@link String}
   */
  public String getToolsRunningStatus() // get the toolsRunningStatus
  {
    return toolsRunningStatus;
  }

  /**
   * Sets the value of the toolsRunningStatus property.
   * 
   * @param toolsRunningStatus allowed object is {@link String}
   */
  public void setToolsRunningStatus(String toolsRunningStatus) {
    this.toolsRunningStatus = toolsRunningStatus;
  }

  /**
   * Gets the value of the VMDetailsJSON property.
   * 
   * @return VMDetailsJSON possible object is {@link String}
   */
  public String getVMDetailsJSON() {
    final GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.setPrettyPrinting();
    final Gson gson = gsonBuilder.create();
    return gson.toJson(this);
  }

  /**
   * Gets the value of the operatingSystem property.
   * 
   * @return operatingSystem possible object is {@link String}
   */
  public String getOperatingSystem() {
    return operatingSystem;
  }

  /**
   * Sets the value of the operatingSystem property.
   * 
   * @param operatingSystem allowed object is {@link String}
   */
  public void setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

  /**
   * Gets the value of the portGroups property.
   * 
   * @return portGroups possible object is {@link String[]}
   */
  public String[] getPortGroups() {
    return portGroups;
  }

  /**
   * Sets the value of the portGroups property.
   * 
   * @param portGroups allowed object is {@link String[] }
   */

  public void setPortGroups(String[] portGroups) {
    this.portGroups = portGroups;
  }

  public Integer getSourceId() {
    return sourceId;
  }

  public void setSourceId(Integer sourceId) {
    this.sourceId = sourceId;
  }

  public String getResourcePoolName() {
    return resourcePoolName;
  }

  public void setResourcePoolName(String resourcePoolName) {
    this.resourcePoolName = resourcePoolName;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getPowerStatus() {
    return powerStatus;
  }

  public void setPowerStatus(String powerStatus) {
    this.powerStatus = powerStatus;
  }

  public String getvAppName() {
    return vAppName;
  }

  public void setvAppName(String vAppName) {
    this.vAppName = vAppName;
  }

  public String getscanFlag() {
    return scanFlag;
  }

  public void setscanFlag(String scanFlag) {
    this.scanFlag = scanFlag;
  }

  /**
   * @return the containerDetails
   */
  public ArrayList<ContainerDetails> getContainerDetails() {
    return containerDetails;
  }

  /**
   * @param containerDetails the containerDetails to set
   */
  public void setContainerDetails(ArrayList<ContainerDetails> containerDetails) {
    this.containerDetails = containerDetails;
  }

  /**
   * @return the nodeMemory
   */
  public String getNodeMemory() {
    return nodeMemory;
  }

  /**
   * @param nodeMemory the nodeMemory to set
   */
  public void setNodeMemory(String nodeMemory) {
    this.nodeMemory = nodeMemory;
  }

  /**
   * @return the nodeCPU
   */
  public String getNodeCPU() {
    return nodeCPU;
  }

  /**
   * @param nodeCPU the nodeCPU to set
   */
  public void setNodeCPU(String nodeCPU) {
    this.nodeCPU = nodeCPU;
  }
}
