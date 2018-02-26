package com.xuyulong.Store;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.xuyulong.util.BaseActivity;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.Until;

@EActivity(R.layout.geren_activity)
public class Geren_Activity extends BaseActivity {

	private Toast toast;
	private static final int ZHUXIAO = 0;
	private static final int TUICHU = 1;

	@ViewById
	TextView tv_userName;
	@ViewById
	TextView mLoginOut;
	@ViewById
	TextView mExit;
	@ViewById
	ImageButton mTitleBack;

	@Click
	void mTitleBackClicked() {
		finish();
	}

	@Click
	void mLoginOutClicked() {

		SetLoding("注销中");
		Enable(false);
		exitNet(ZHUXIAO);
	}

	@Click
	void mExitClicked() {

		SetLoding("退出中");
		Enable(false);
		exitNet(TUICHU);
	}

	private void Enable(boolean b) {
		mExit.setEnabled(b);
		mLoginOut.setEnabled(b);
		mTitleBack.setEnabled(b);
	}

	/**
	 * 退出操作
	 */
	@Background
	void exitNet(int mode) {
		/**
		 * 关闭蓝牙连接
		 */
		ChenZhong_Activity.closeBluetoothSocket();
		if (AppConfig.getInstance().bluetoothAdapter.isEnabled()) {
			AppConfig.getInstance().bluetoothAdapter.disable();
		}
		/////////////////////////////////////////////////////////////

		String request = Until.exit_req(AppConfig.getInstance().UserId);
		
		String EXIT_URL = 
				AppConfig.getInstance().serviceIp 
				+ AppConfig.getInstance().serviceIpAfter + 
				"exitLogin";
		String response = HttpUtils.httpPut(EXIT_URL, request);

		// 服务器连接失败
		if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
			exitErr(mode, response);
		}
		// 服务器连接成功
		else {
			String successFlag = Until.parseResult(response);

			// 退出成功
			if ("OK".equals(successFlag)) {
				exitSuccess(mode);
			}
			// 退出失败
			else {
				exitErr(mode, successFlag);
			}
		}
	}

	/**
	 * 退出成功
	 */
	@UiThread
	void exitSuccess(int mode) {
		Enable(true);
		SetLodingHid();

		if (mode == ZHUXIAO) {
			setResult(-3);
			finish();
		} else if (mode == TUICHU) {
			setResult(-2);
			finish();
		}
	}

	/**
	 * 退出失败
	 * 
	 * @param reslut
	 */
	@UiThread
	void exitErr(int mode, String result) {

		toast = Toast.makeText(Geren_Activity.this, result, 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		Enable(true);
		SetLodingHid();

		if (mode == ZHUXIAO) {
			setResult(-3);
			finish();
		} else if (mode == TUICHU) {
			setResult(-2);
			finish();
		}
	}

	@AfterViews
	void init() {
		tv_userName.setText(AppConfig.getInstance().UserName);
	}
}
