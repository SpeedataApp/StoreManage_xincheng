package com.wenyankeji.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class User implements Serializable {
    private String username = "";
    private String password = "";
    private String deviceid = "";
    private int platform;
    private BigDecimal UserId;

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", deviceid='" + deviceid + '\'' +
                ", platform=" + platform +
                ", UserId=" + UserId +
                '}';
    }

    public BigDecimal getUserId() {
        return UserId;
    }

    public void setUserId(BigDecimal userId) {
        UserId = userId;
    }
}
