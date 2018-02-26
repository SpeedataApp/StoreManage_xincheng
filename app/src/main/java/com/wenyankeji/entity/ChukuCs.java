package com.wenyankeji.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class ChukuCs implements Serializable {
	private Integer id;

	private String chukuNo;

	private BigDecimal rukuId;

	private BigDecimal cfId;

	private String cfName;

	private String carNo;

	private BigDecimal ysId;

	private String ysName;

	private BigDecimal qty;

	private BigDecimal weight;

	private BigDecimal czId;

	private String czName;

	private BigDecimal warehouseId;

	private String warehouseName;

	private BigDecimal userId;

	private String userName;

	private String createdAt;

	private String rfid;
	private BigDecimal wasteId;
	private String wasteName;
	private String wasteCode;
	private String wasteType;

	public String getWasteName() {
		return wasteName;
	}

	public void setWasteName(String wasteName) {
		this.wasteName = wasteName;
	}

	public String getWasteCode() {
		return wasteCode;
	}

	public void setWasteCode(String wasteCode) {
		this.wasteCode = wasteCode;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getChukuNo() {
		return chukuNo;
	}

	public void setChukuNo(String chukuNo) {
		this.chukuNo = chukuNo == null ? null : chukuNo.trim();
	}

	public BigDecimal getRukuId() {
		return rukuId;
	}

	public void setRukuId(BigDecimal rukuId) {
		this.rukuId = rukuId;
	}

	public BigDecimal getCfId() {
		return cfId;
	}

	public void setCfId(BigDecimal cfId) {
		this.cfId = cfId;
	}

	public String getCfName() {
		return cfName;
	}

	public void setCfName(String cfName) {
		this.cfName = cfName == null ? null : cfName.trim();
	}

	public String getCarNo() {
		return carNo;
	}

	public void setCarNo(String carNo) {
		this.carNo = carNo == null ? null : carNo.trim();
	}

	public BigDecimal getYsId() {
		return ysId;
	}

	public void setYsId(BigDecimal ysId) {
		this.ysId = ysId;
	}

	public String getYsName() {
		return ysName;
	}

	public void setYsName(String ysName) {
		this.ysName = ysName == null ? null : ysName.trim();
	}

	public BigDecimal getQty() {
		return qty;
	}

	public void setQty(BigDecimal qty) {
		this.qty = qty;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public BigDecimal getCzId() {
		return czId;
	}

	public void setCzId(BigDecimal czId) {
		this.czId = czId;
	}

	public String getCzName() {
		return czName;
	}

	public void setCzName(String czName) {
		this.czName = czName == null ? null : czName.trim();
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

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt == null ? null : createdAt.trim();
	}

	public String getRfid() {
		return rfid;
	}

	public void setRfid(String rfid) {
		this.rfid = rfid;
	}

	public BigDecimal getWasteId() {
		return wasteId;
	}

	public void setWasteId(BigDecimal wasteId) {
		this.wasteId = wasteId;
	}

	public String getWasteType() {
		return wasteType;
	}

	public void setWasteType(String wasteType) {
		this.wasteType = wasteType;
	}
}
