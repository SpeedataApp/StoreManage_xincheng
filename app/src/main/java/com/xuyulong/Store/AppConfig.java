package com.xuyulong.Store;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.rscja.deviceapi.Barcode2D;
import com.scandecode.ScanDecode;
import com.scandecode.inf.ScanInterface;
import com.wenyankeji.crash.CrashHandler;
import com.xuyulong.Store.ChenZhong_Activity.ReadThread;
import com.xuyulong.util.LogUtil;

import net.tsz.afinal.FinalDb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

//import cn.pda.serialport.SerialPort;
//import com.android.hdhe.uhf.reader.UhfReader;

public class AppConfig extends Application {
    /**
     * 记忆仓库选择
     */
    public FinalDb db;

    /**
     * 入库，出库，记忆数据
     */
    public ArrayList<HashMap<String, Object>> rukuLabels = null;
    public ArrayList<HashMap<String, Object>> chukuLabels = null;

    /**
     * 蓝牙部分
     */
    public BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
    public InputStream inputStream = null; // 输入流，用来接收蓝牙数据
    public String showMsg; // 显示用数据缓存
    public boolean timeOut = false;
    public BluetoothSocket bluetoothSocket = null; // 蓝牙通信socket
    public boolean getWeight = false;
    public ReadThread readThread;

    // 山寨机专用属性
    public boolean initFlag;
    public boolean carInitFlag;
    //	public SerialPort mSerialPort;
    public InputStream is;
    public OutputStream os;
    /* serialport parameter */
    public int port = 0;
    public int baudrate = 9600;
    public int flags = 0;
//	public UhfReader reader; // 超高频读写器

    // 成为机专用属性
    public Barcode2D barcode2DInstance = null;
    public boolean ruku_tiebiao_Flag;
    // 思必拓机专用属性
    public ScanInterface bcr = null;

    // 0:山寨机
    // public int handPhone = 0;
    // // 1:成为
    // public int handPhone = 1;
    // 2：思必拓
    public int handPhone = 2;
    // 3：肯麦思
    // public int handPhone = 3;

    public String UserName = "";
    public String appVersion = "1.0";
    public String appUrl = "";
    public String appName = "";
    public BigDecimal UserId;
    // 企业id
    public BigDecimal enterpriseId;
    // 企业平台 -----4:产生 6：处置
    public int Platform = 4;
    // 企业name
    public String enterpriseName = "";

    public boolean isDownload = false;
    // public String serviceIp = "http://112.74.76.72:5858";
    public String serviceIp = "http://www.hnweifei.com";
    // public String serviceIp = "http://192.168.1.167:8088";
//	 public String serviceIp = "http://www.hnweifei.com:7080";
    // public String serviceIp = "http://120.25.249.68:5858";
    // public String serviceIp = "http://120.25.249.68:6868";
    // public String serviceIp = "http://192.168.1.115:8888";
    public String serviceIpAfter = "/AndroidService/android/";

    public boolean installCancle = false;
    // 称重界面------------false:只支持地磅 true：支持手动输入支持地磅
    public boolean handInput = true;

    public String mobileVersion;// 手持机型号

    private static AppConfig mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        init();

    }

    public void init() {
        mInstance = this;
        db = FinalDb.create(this, "chanfei.db");

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        LogUtil.getInstance(this).start();

        mobileVersion = android.os.Build.MODEL;
        Log.w("info", "手持机版本是:    " + mobileVersion);

        // 如果是山寨机
        if (AppConfig.getInstance().handPhone == 0) {
            try {
//				mSerialPort = new SerialPort(port, baudrate, flags);
//				mSerialPort.scaner_poweron();
//				is = mSerialPort.getInputStream();
//				os = mSerialPort.getOutputStream();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /** clear useless data **/
                byte[] temp = new byte[128];
                is.read(temp);
                initFlag = true;
            } catch (SecurityException e1) {
                e1.printStackTrace();
                initFlag = false;
            } catch (IOException e1) {
                e1.printStackTrace();
                initFlag = false;
            }

            // 获取读写器实例，若返回Null,则串口初始化失败
//			reader = UhfReader.getInstance();
//			if (reader == null) {
//				carInitFlag = false;
//			} else {
//				carInitFlag = true;
//			}
        }
        // 如果是思必拓
        else if (AppConfig.getInstance().handPhone == 2) {
            String mobileVersion = android.os.Build.MODEL;
            Log.w("info", "手持机版本是:    " + mobileVersion);
            try {
                bcr = new ScanDecode(this);
                bcr.initService("true");
                if (bcr == null) {
                    AppConfig.getInstance().initFlag = false;
                } else {
                    AppConfig.getInstance().initFlag = true;
                }
            } catch (Exception e) {
                AppConfig.getInstance().initFlag = false;
            }
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 如果是山寨机
        if (AppConfig.getInstance().handPhone == 0) {
//			if (mSerialPort != null) {
//				mSerialPort.scaner_poweroff();
//				try {
//					if (is != null) {
//						is.close();
//					}
//					if (os != null) {
//						os.close();
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				mSerialPort.close(port);
//			}

            try {
//				if (reader != null) {
//					reader.close();
//				}
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 如果是思必拓手持机
        else if (AppConfig.getInstance().handPhone == 2) {
            if (AppConfig.getInstance().initFlag && bcr != null) {
                bcr.onDestroy();
                bcr = null;
                AppConfig.getInstance().initFlag = false;
            }
        }
    }

    public static AppConfig getInstance() {
        return mInstance;
    }

}
