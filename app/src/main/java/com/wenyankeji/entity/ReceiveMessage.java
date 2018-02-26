package com.wenyankeji.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import com.xuyulong.Store.AppConfig;

public class ReceiveMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private int enterpriseType = AppConfig.getInstance().Platform;

	private RukuCs rukuCs;

	private ChukuCs chukuCs;

	private LinkedList<HashMap<String, Object>> csRukuDetails;

	private LinkedList<HashMap<String, Object>> chukuCsDetail;

	public int getEnterpriseType() {
		return enterpriseType;
	}

	public void setEnterpriseType(int enterpriseType) {
		this.enterpriseType = enterpriseType;
	}

	public RukuCs getRukuCs() {
		return rukuCs;
	}

	public void setRukuCs(RukuCs rukuCs) {
		this.rukuCs = rukuCs;
	}

	public LinkedList<HashMap<String, Object>> getCsRukuDetails() {
		return csRukuDetails;
	}

	public void setCsRukuDetails(
			LinkedList<HashMap<String, Object>> csRukuDetails) {
		this.csRukuDetails = csRukuDetails;
	}

	public ChukuCs getChukuCs() {
		return chukuCs;
	}

	public void setChukuCs(ChukuCs chukuCs) {
		this.chukuCs = chukuCs;
	}

	public LinkedList<HashMap<String, Object>> getChukuCsDetail() {
		return chukuCsDetail;
	}

	public void setChukuCsDetail(
			LinkedList<HashMap<String, Object>> chukuCsDetail) {
		this.chukuCsDetail = chukuCsDetail;
	}

}
