package com.wenyankeji.entity;

import java.math.BigDecimal;

public class ResultMessage {

	private BigDecimal userId;
	private String userName = "";
	private boolean success;
	private String message = "";
	private BigDecimal enterpriseId;
	private String enterpriseName = "";
	private String carNo = "";
	private String driverName = "";
	private String rfid = "";
	//是否手动输入重量，默认自动录入
	private boolean weightManualInput = false;

	public String getCarNo() {
		return this.carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEnterpriseName() {
		return enterpriseName;
	}

	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}

	public BigDecimal getUserId() {
		return userId;
	}

	public void setUserId(BigDecimal userId) {
		this.userId = userId;
	}

	public BigDecimal getEnterpriseId() {
		return enterpriseId;
	}

	public void setEnterpriseId(BigDecimal enterpriseId) {
		this.enterpriseId = enterpriseId;
	}

	public String getRfid() {
		return rfid;
	}

	public void setRfid(String rfid) {
		this.rfid = rfid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public boolean isWeightManualInput() {
		return weightManualInput;
	}

	public void setWeightManualInput(boolean weightManualInput) {
		this.weightManualInput = weightManualInput;
	}
}
