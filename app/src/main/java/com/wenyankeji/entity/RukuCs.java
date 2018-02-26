package com.wenyankeji.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class RukuCs implements Serializable {
	private Integer id;

	private String rukuNo;

	private BigDecimal entId;

	private String entName;

	private BigDecimal wasteId;

	private String wasteName;

	private String wasteCode;

	private String wasteType;

	private String qty;

	private BigDecimal weight;

	private BigDecimal userId;

	private String userName;

	private BigDecimal warehouseId;

	private String warehouseName;

	private BigDecimal locationId;

	private String locationName;

	private String xingtai;

	private String dangerFeatures;

	private String carNo;

	private String createdAt;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRukuNo() {
		return rukuNo;
	}

	public void setRukuNo(String rukuNo) {
		this.rukuNo = rukuNo == null ? null : rukuNo.trim();
	}

	public BigDecimal getEntId() {
		return entId;
	}

	public void setEntId(BigDecimal entId) {
		this.entId = entId;
	}

	public String getEntName() {
		return entName;
	}

	public void setEntName(String entName) {
		this.entName = entName == null ? null : entName.trim();
	}

	public BigDecimal getWasteId() {
		return wasteId;
	}

	public void setWasteId(BigDecimal wasteId) {
		this.wasteId = wasteId;
	}

	public String getWasteName() {
		return wasteName;
	}

	public void setWasteName(String wasteName) {
		this.wasteName = wasteName == null ? null : wasteName.trim();
	}

	public String getWasteCode() {
		return wasteCode;
	}

	public void setWasteCode(String wasteCode) {
		this.wasteCode = wasteCode == null ? null : wasteCode.trim();
	}

	public String getQty() {
		return qty;
	}

	public void setQty(String qty) {
		this.qty = qty == null ? null : qty.trim();
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public BigDecimal getUserId() {
		return userId;
	}

	public void setUserId(BigDecimal userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName == null ? null : userName.trim();
	}

	public BigDecimal getWarehouseId() {
		return warehouseId;
	}

	public void setWarehouseId(BigDecimal warehouseId) {
		this.warehouseId = warehouseId;
	}

	public String getWarehouseName() {
		return warehouseName;
	}

	public void setWarehouseName(String warehouseName) {
		this.warehouseName = warehouseName == null ? null : warehouseName
				.trim();
	}

	public BigDecimal getLocationId() {
		return locationId;
	}

	public void setLocationId(BigDecimal locationId) {
		this.locationId = locationId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName == null ? null : locationName.trim();
	}

	public String getXingtai() {
		return xingtai;
	}

	public void setXingtai(String xingtai) {
		this.xingtai = xingtai == null ? null : xingtai.trim();
	}

	public String getDangerFeatures() {
		return dangerFeatures;
	}

	public void setDangerFeatures(String dangerFeatures) {
		this.dangerFeatures = dangerFeatures == null ? null : dangerFeatures
				.trim();
	}

	public String getCarNo() {
		return carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo == null ? null : carNo.trim();
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt == null ? null : createdAt.trim();
	}

	public String getWasteType() {
		return wasteType;
	}

	public void setWasteType(String wasteType) {
		this.wasteType = wasteType;
	}
}
