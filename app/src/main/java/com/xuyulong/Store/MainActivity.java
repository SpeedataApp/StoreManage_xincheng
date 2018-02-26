package com.xuyulong.Store;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.wenyankeji.appupdate.NotificationUpdateActivity;
import com.wenyankeji.entity.AppInfo;
import com.xuyulong.adapter.GradViewAdapter;
import com.xuyulong.ui.SlidingMenu;
import com.xuyulong.util.ActivityCollector;
import com.xuyulong.util.BaseActivity;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.Until;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;

@EActivity(R.layout.main_activity)
public class MainActivity extends BaseActivity {

    final int REQUEST_GEREN = 1;

    @ViewById
    SlidingMenu mMenu;

    @ViewById
    GridView mGridview;

    //跳转个人设置界面
    @Click
    void mGeRenClicked() {
        Intent intent = Geren_Activity_.intent(this).get();
        startActivityForResult(intent, REQUEST_GEREN);
    }

    //跳转到设置界面
    @Click
    void mSettingClicked() {
        Intent intent = Setting_Activity_.intent(this).get();
        startActivity(intent);
    }

    @Click
    void mBtMenuClicked() {
        mMenu.toggle();
    }

    @AfterViews
    void init() {
        // 获取app版本信息
        getAppVersion();
        ArrayList<HashMap<String, Object>> tmp = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < 3; i++)
            tmp.add(new HashMap<String, Object>());
        // tmp.get(0).put("ItemText", "贴标");
        // tmp.get(0).put("ItemImage", R.drawable.main_tiebiebiao);
        tmp.get(0).put("ItemText", "入库");
        tmp.get(0).put("ItemImage", R.drawable.main_ruku);
        tmp.get(1).put("ItemText", "出库");
        tmp.get(1).put("ItemImage", R.drawable.main_chuku);
        tmp.get(2).put("ItemText", "查询");
        tmp.get(2).put("ItemImage", R.drawable.main_chaxun);
        mGridview.setAdapter(new GradViewAdapter(tmp, this));
        mGridview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:
                        // 入库操作在TieBiaoActivity.java中已经实现,RuKuActivity.java已经废弃
                        startActivity(TieBiao_Activity_.intent(MainActivity.this)
                                .get());
                        break;
                    case 1:
                        startActivity(GaoPinSpeedataAct_.intent(MainActivity.this)
                                .get());
                        break;
//				case 1:
//					startActivity(ChenZhong_Activity_.intent(MainActivity.this)
//							.get());
//					break;
                    case 2:
                        startActivity(ChaXun_Activity_.intent(MainActivity.this)
                                .get());
                        break;
                    default:
                        break;
                }
            }
        });

    }

    /**
     * 获取app信息
     */
    @Background
    void getAppVersion() {
        String request = Until.getApp_req();

        String GET_APP_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "appVersion";
        String response = HttpUtils.httpPut(GET_APP_URL, request);

        // 服务器连接失败
        if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
            getAppErr(response);
        }
        // 服务器连接成功
        else {
            String successFlag = Until.parseResult(response);
            // 获取成功
            if ("OK".equals(successFlag)) {
                // 解析版本
                AppInfo appInfo = Until.getAppInfo(response);
                // 如果有新版本
                if (AppConfig.getInstance().Platform == appInfo.getPlatForm()
                        && !appInfo.getAppVersion().contains("v")) {
                    Double newVersionDouble = Double.parseDouble(appInfo
                            .getAppVersion());
                    Double oldVersionDouble = Double.parseDouble(AppConfig
                            .getInstance().appVersion);
                    // 有最新版本
                    if (newVersionDouble > oldVersionDouble) {
                        getAppSuccess(appInfo);
                    }
                }
            }
            // 获取失败
            else {
                getAppErr(successFlag);
            }
        }
    }

    @UiThread
    void getAppSuccess(AppInfo appInfo) {
        showCustomMessage(appInfo);
    }

    @SuppressLint("WrongConstant")
    @UiThread
    void getAppErr(String result) {
        Toast.makeText(MainActivity.this, result, 5000).show();
    }

    boolean mBQout = false;

    public void onBackPressed() {
        if (mBQout) {
            SetLoding("退出中");
            Enable(false);
            exitNet();
            return;
        }
        mBQout = true;
        Toast.makeText(this, "请再按一次退出!", Toast.LENGTH_SHORT).show();
    }

    @OnActivityResult(REQUEST_GEREN)
    void onResult(int resultCode, Intent data) {
        if (resultCode == -3 || resultCode == -2) {
            if (resultCode == -3) {
                startActivity(Login_Activity_.intent(this).mAutoLogin(true)
                        .get());
            }
            finish();
        }
    }

    private void Enable(boolean b) {
        mMenu.setEnabled(b);
        mGridview.setEnabled(b);
    }

    /**
     * 退出操作
     */
    @Background
    void exitNet() {

        /**
         * 关闭蓝牙连接
         */
        ChenZhong_Activity.closeBluetoothSocket();
        if (AppConfig.getInstance().bluetoothAdapter.isEnabled()) {
            AppConfig.getInstance().bluetoothAdapter.disable();
        }
        // ///////////////////////////////////////////////////////////

        String request = Until.exit_req(AppConfig.getInstance().UserId);

        String EXIT_URL = AppConfig.getInstance().serviceIp
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
        finish();
    }

    /**
     * 退出失败
     */
    @UiThread
    void exitErr(String result) {
        @SuppressLint("WrongConstant") Toast toast = Toast.makeText(MainActivity.this, result, 3000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        Enable(true);
        SetLodingHid();
        finish();
    }

    /**
     * it will show the OK/CANCEL dialog like iphone, make sure no keyboard is
     * visible
     */
    private void showCustomMessage(final AppInfo appInfo) {

        AppConfig.getInstance().appName = appInfo.getAppName();
        AppConfig.getInstance().appUrl = appInfo.getAppUrl();

        final Dialog lDialog = new Dialog(MainActivity.this,
                android.R.style.Theme_Translucent_NoTitleBar);
        lDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        lDialog.setContentView(R.layout.r_okcanceldialogview);

        TextView version = (TextView) lDialog.findViewById(R.id.version);
        TextView time = (TextView) lDialog.findViewById(R.id.time);
        version.setText(appInfo.getAppVersion());
        time.setText(appInfo.getReleasedTime().substring(0, 10));

        ((Button) lDialog.findViewById(R.id.cancel))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        lDialog.dismiss();
                    }
                });

        ((Button) lDialog.findViewById(R.id.ok))
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        lDialog.dismiss();

                        // 下载新版的app
                        Intent it = new Intent(MainActivity.this,
                                NotificationUpdateActivity.class);
                        startActivity(it);
                        AppConfig.getInstance().isDownload = true;
                    }
                });
        lDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果安装取消时候 直接退出app
        if (AppConfig.getInstance().installCancle) {
            ActivityCollector.finishAll();
        }
    }

}
