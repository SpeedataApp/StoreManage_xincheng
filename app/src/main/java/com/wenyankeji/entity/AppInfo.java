package com.wenyankeji.entity;

public class AppInfo {
	private String appName = "";
	private String appUrl = "";
	private String appVersion = "";
	private int id;
	private String releasedTime = "";
	private int platForm;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppUrl() {
		return appUrl;
	}

	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getReleasedTime() {
		return releasedTime;
	}

	public void setReleasedTime(String releasedTime) {
		this.releasedTime = releasedTime;
	}

	public int getPlatForm() {
		return platForm;
	}

	public void setPlatForm(int platForm) {
		this.platForm = platForm;
	}

}
