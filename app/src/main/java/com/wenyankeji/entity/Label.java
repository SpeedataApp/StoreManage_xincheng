package com.wenyankeji.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class Label implements Serializable {

	/**
	 * UID 
	 */
	private static final long serialVersionUID = 1L;
	private String qrCode = "";
	private BigDecimal userId;
	private BigDecimal enterpriseId;
	private int platform;
	private int operator;
	

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
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



}
