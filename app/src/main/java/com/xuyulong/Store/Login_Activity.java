package com.xuyulong.Store;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wenyankeji.entity.ResultMessage;
import com.xuyulong.ui.HandInputDlg;
import com.xuyulong.ui.HandInputDlg.handInputSureListener;
import com.xuyulong.util.BaseActivity;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.Until;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

@SuppressLint("Registered")
@EActivity(R.layout.login_activity)
public class Login_Activity extends BaseActivity {
    static public final String UNANMEKEY = "UNANMEKEY";
    static public final String UPWDKEY = "UPWDKEY";
    static public final String MLOGIN = "MLOGIN";
    static public final String MNP = "MNP";
    static public final String AUTO_LOGIN = "AUTO_LOGIN";

    private ResultMessage resErr;
    private String LOGIN_URL;
    private String EXIT_URL;
    static public final String DIBANG = "DIBANG";
    static public final String DUKA = "DUKA";
    @Extra(AUTO_LOGIN)
    boolean mAutoLogin = false;
    @ViewById
    EditText mUserName;
    @ViewById
    EditText mPwd;
    @ViewById
    CheckBox mMindPwd;
    @ViewById
    CheckBox mMindLogin;
    @ViewById
    Button mLogin;
    @ViewById
    TextView mVer;
    @ViewById
    RelativeLayout mRoot;
    @ViewById
    TextView failText;
    @ViewById
    Button mForceExit;
    @ViewById
    Button setIP;

    private void Enable(Boolean enable) {
        mUserName.setEnabled(enable);
        mPwd.setEnabled(enable);
        mMindPwd.setEnabled(enable);
        mMindLogin.setEnabled(enable);
        mLogin.setEnabled(enable);
        mForceExit.setEnabled(enable);
    }

    @Click
    void setIPClicked() {
        new HandInputDlg(Login_Activity.this, new handInputSureListener() {

            @Override
            public void handInputSure(String handInputLabelText) {
                PutPreferences("serviceIp", handInputLabelText.trim());
                AppConfig.getInstance().serviceIp = getPreferencesString("serviceIp", "");
            }
        }, "配置服务器IP", "IP地址").show();
    }

    @Click
    void mLoginClicked() {

        if ("".equals(mUserName.getText().toString())
                || "".equals(mPwd.getText().toString())) {
            Toast.makeText(this, "请输入", Toast.LENGTH_LONG).show();
        } else {
            serviceIpInit();

            SetLoding("登录中...");
            Enable(false);
            loginNet(mUserName.getText().toString(), mPwd.getText().toString());
        }

    }

    /**
     * 登陆操作
     *
     * @param userName
     * @param pwd
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Background
    void loginNet(String userName, String pwd) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        for (int slot = 0; slot < telephonyManager.getPhoneCount(); slot++) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            String imei = telephonyManager.getDeviceId(slot);
            Log.i("tw", "loginNet: "+imei);
        }
        String request = Until.login_req(userName, pwd,telephonyManager.getDeviceId());

        LOGIN_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "login";

        String response = HttpUtils.httpPut(LOGIN_URL, request);

        // 服务器连接失败
        if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
            LogResultErr(response);
        }
        // 服务器连接成功
        else {
            String successFlag = Until.parseResult(response);

            // 登录成功
            if ("OK".equals(successFlag)) {

                ResultMessage res = Until.getUserInfo(response);
                // 存储userId
                AppConfig.getInstance().UserId = res.getUserId();
                // 存储userName
                AppConfig.getInstance().UserName = res.getUserName();
                // 存储企业Id
                AppConfig.getInstance().enterpriseId = res.getEnterpriseId();
                // 存储企业name
                AppConfig.getInstance().enterpriseName = res
                        .getEnterpriseName();

                PutPreferences(MNP, mMindPwd.isChecked());
                PutPreferences(MLOGIN, mMindLogin.isChecked());

                if (mMindPwd.isChecked() || mMindLogin.isChecked()) {
                    PutPreferences(UNANMEKEY, mUserName.getText().toString());
                    PutPreferences(UPWDKEY, mPwd.getText().toString());
                }

                //判断是否允许手工输入重量
                AppConfig.getInstance().handInput = res.isWeightManualInput();

                LogResultSuccess();

            }
            // 登录失败
            else {
                resErr = Until.getUserInfo(response);
                LogResultErr(successFlag);
            }
        }
    }

    /**
     * 登陆成功
     */
    @UiThread
    void LogResultSuccess() {
        Enable(true);
        SetLodingHid();
        if (getPreferencesString(DIBANG, "").equals("433地磅")) {
            PutPreferences(DIBANG, "433地磅");
        } else if (getPreferencesString(DIBANG, "蓝牙地磅").equals("蓝牙地磅")) {
            PutPreferences(DIBANG, "蓝牙地磅");
        }
        if (getPreferencesString(DUKA, "").equals("超高频读卡")) {
            PutPreferences(DUKA, "超高频读卡");
        } else if (getPreferencesString(DUKA, "NFC读卡").equals("NFC读卡")) {
            PutPreferences(DUKA, "NFC读卡");
        }
        Intent intent = MainActivity_.intent(this).get();
        startActivity(intent);
        finish();
    }

    /**
     * 登陆失败
     *
     * @param reslut
     */
    @UiThread
    void LogResultErr(String reslut) {
        Enable(true);
        SetLodingHid();
        failText.setVisibility(View.VISIBLE);
        failText.setText(reslut);

        if ("用户已经登录!".equals(reslut)) {
            failText.setText(reslut + "强制退出可重新登录");
            mLogin.setVisibility(View.GONE);
            mForceExit.setVisibility(View.VISIBLE);
        }
    }

    // //////////////////////////////////////////////////////
    @Click
    void mForceExit() {
        SetLoding("退出中");
        Enable(false);
        exitNet();
    }

    /**
     * 退出操作
     */
    @Background
    void exitNet() {

        String request = Until.exit_req(resErr.getUserId());

        EXIT_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "exitLogin";

        String response = HttpUtils.httpPut(EXIT_URL, request);

        // 服务器连接失败
        if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
            exitErr(response);
        }
        // 服务器连接成功
        else {
            String successFlag = Until.parseResult(response);

            // 退出成功
            if ("OK".equals(successFlag)) {
                exitSuccess();
            }
            // 退出失败
            else {
                exitErr(successFlag);
            }
        }
    }

    /**
     * 退出成功
     */
    @UiThread
    void exitSuccess() {
        Enable(true);
        SetLodingHid();
        failText.setVisibility(View.VISIBLE);
        failText.setText("强制退出成功，请登录！");
        mForceExit.setVisibility(View.GONE);
        mLogin.setVisibility(View.VISIBLE);
    }

    /**
     * 退出失败
     */
    @UiThread
    void exitErr(String result) {
        Enable(true);
        SetLodingHid();
        failText.setVisibility(View.VISIBLE);
        failText.setText(result);
    }

    // /////////////////////////////////////////////////////

    @CheckedChange({R.id.mMindLogin, R.id.mMindPwd})
    void checkedChangeOnHelloCheckBox(CompoundButton hello, boolean isChecked) {
        if (hello.getId() == R.id.mMindLogin) {
            if (isChecked) {
                mMindPwd.setChecked(true);
            }
        }

        if (hello.getId() == R.id.mMindPwd) {
            if (isChecked) {
            } else {
                if (mMindLogin.isChecked()) {
                    mMindLogin.setChecked(false);
                }
            }
        }

    }

    @AfterViews
    void init() {
        serviceIpInit();

        // 获取当前程序的版本号
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(
                    getPackageName(), 0);
            AppConfig.getInstance().appVersion = packInfo.versionName;
            mVer.setText(AppConfig.getInstance().appVersion);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mMindLogin.setChecked(getPreferencesBoolean(MLOGIN, false));
        mMindPwd.setChecked(getPreferencesBoolean(MNP, false));
        if (getPreferencesBoolean(MNP, false)) {
            mUserName.setText(getPreferencesString(UNANMEKEY, ""));
            mPwd.setText(getPreferencesString(UPWDKEY, ""));
        }
        if (getPreferencesBoolean(MLOGIN, false) && !mAutoLogin) {
            loginNet(getPreferencesString(UNANMEKEY, ""),
                    getPreferencesString(UPWDKEY, ""));
        }
    }

    /**
     * 读取配置文件里面的服务器ip地址
     */
    private void serviceIpInit() {
        try {

            String fileName = "服务器IP配置文件" + ".txt";

            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String path = "/sdcard/wenyankeji/chansheng/Ip/";
                File dir = new File(path);
                File serviceIptxt = new File(path + fileName);

                if (dir.exists() && serviceIptxt.exists()) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(
                                new FileReader(serviceIptxt));
                        String tempString = "";
                        tempString = reader.readLine();

                        if ("".equals(tempString) || tempString == null) {
                            Toast.makeText(Login_Activity.this,
                                    "服务器ip地址为空，请设置！", 3000).show();
                            Enable(false);
                            return;
                        } else if (!"http://"
                                .equals(tempString.substring(0, 7))) {
                            Toast.makeText(Login_Activity.this,
                                    "服务器ip地址格式不正确，请设置！", 3000).show();
                            Enable(false);
                            return;
                        }

                        PutPreferences("serviceIp", tempString.trim());
                        AppConfig.getInstance().serviceIp = getPreferencesString("serviceIp", "");
                        reader.close();

                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e1) {
                            }
                        }
                    }
                }

                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (!serviceIptxt.exists()) {
                    serviceIptxt.createNewFile();
                }

                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(AppConfig.getInstance().serviceIp.getBytes());
                fos.close();
            }
        } catch (Exception e) {
            System.out.println("Exception-->" + e);
        }
    }

}
