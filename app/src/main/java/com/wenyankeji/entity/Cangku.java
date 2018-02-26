package com.wenyankeji.entity;

import java.io.Serializable;

import net.tsz.afinal.annotation.sqlite.Table;
import net.tsz.afinal.annotation.sqlite.Id;

@Table(name = "tb_user_cangku")
public class Cangku implements Serializable{
	@Id (column = "userId")//自定义主键名称
	private String UserId;
	private int spaceId;
	private int repositoryId;
	private String repositoryName;
	private String spaceName;
	private boolean cangkuDefault;
	
	public int getSpaceId() {
		return spaceId;
	}
	public void setSpaceId(int spaceId) {
		this.spaceId = spaceId;
	}
	public int getRepositoryId() {
		return repositoryId;
	}
	public void setRepositoryId(int repositoryId) {
		this.repositoryId = repositoryId;
	}
	public String getRepositoryName() {
		return repositoryName;
	}
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}
	public String getSpaceName() {
		return spaceName;
	}
	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}
	public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public boolean isCangkuDefault() {
		return cangkuDefault;
	}
	public void setCangkuDefault(boolean cangkuDefault) {
		this.cangkuDefault = cangkuDefault;
	}
	

}
