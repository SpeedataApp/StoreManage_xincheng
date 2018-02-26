package com.xuyulong.Store;

import java.util.ArrayList;
import java.util.HashMap;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.wenyankeji.thread.ScanThreadTest;
import com.xuyulong.adapter.ChaXunListViewAdapter;
import com.xuyulong.ui.HVListView;
import com.xuyulong.ui.HandInputDlg;
import com.xuyulong.ui.HandInputDlg.handInputSureListener;
import com.xuyulong.ui.MySpinnerButton;
import com.xuyulong.ui.MySpinnerButton.GetQRListener;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;
import com.zbar.lib.CaptureActivity;

@EActivity(R.layout.chaxun_activity)
public class ChaXun_Activity extends SpeekBaseActivity {
	private final int SCAN_REQUEST = 1;
	private Toast toast;
	@ViewById
	TextView mQrCode;
	@ViewById
	TextView wasteName;
	@ViewById
	TextView zhuangzhi;
	@ViewById
	TextView harmFeature;
	@ViewById
	TextView shifter;
	@ViewById
	TextView dangerCase;
	@ViewById
	TextView weight;
	@ViewById
	TextView weightName;
	@ViewById
	LinearLayout head;

	@ViewById
	HVListView mList;
	@ViewById
	RelativeLayout mRoot;
	@ViewById
	TextView mTitelTxt;
	@ViewById
	MySpinnerButton mySpinnerButton;
	@ViewById
	ImageButton mTitleBack;
	@ViewById
	ImageButton handInput;

	ChaXunListViewAdapter mAdapter = null;
	private Handler mHandler;
//	private ScanThreadTest thread = null;

	@UiThread(delay = 1000)
	void ToSpeek(int str) {
		Speek(str);
	}

	void Enable(boolean enabled) {
		mList.setEnabled(enabled);
		mySpinnerButton.setEnabled(enabled);
		mTitleBack.setEnabled(enabled);
	}

	@OnActivityResult(SCAN_REQUEST)
	void onResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String labelCode = data.getStringExtra("labelCode");
			qrCodeNet(labelCode);
			SetLoding("标签获取中...");
			Enable(false);
		}
	}

	@AfterViews
	void init() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MySpinnerButton.SCAN_MODE_SCAN_HEAD) {
					SetLoding("标签获取中...");
					Enable(false);
					qrCodeNet(msg.getData().getString("labelCode"));
				}
			}
		};

		mTitelTxt.setText("查询");
		mAdapter = new ChaXunListViewAdapter(
				new ArrayList<HashMap<String, Object>>(), this, mList);
		// 设置列头
		mList.mListHead = head;

		// 设置数据
		mList.setAdapter(mAdapter);

		if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
			ToSpeek(R.raw.saomiaobiaoqian);
		}

		mySpinnerButton.creat();
		// 回调接口
		mySpinnerButton.setGetQRListener(new GetQRListener() {

			@Override
			public void getQRHappend(String content) {
				if (MySpinnerButton.SCAN_WAY_CAMERA.equals(content)) {

					Intent intent = new Intent(ChaXun_Activity.this,
							CaptureActivity.class);
					startActivityForResult(intent, SCAN_REQUEST);
					return;
				}
				// 如果是山寨机
				if (AppConfig.getInstance().handPhone == 0) {
					if (MySpinnerButton.SCAN_WAY_SCAN_HEAD.equals(content)) {
						if (AppConfig.getInstance().initFlag == false) {
							toast = Toast.makeText(ChaXun_Activity.this,
									content, 3000);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						} else {
							scanShanZhai();
						}
					}
				}
				// 如果是成为
				else if (AppConfig.getInstance().handPhone == 1) {
					if (MySpinnerButton.INIT_FAIL.equals(content)) {
						toast = Toast.makeText(ChaXun_Activity.this, content,
								3000);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					} else {
						Speek(R.raw.beep);
						SetLoding("标签获取中...");
						Enable(false);
						qrCodeNet(content);
					}
				}
				// 如果是思必拓
				else if (AppConfig.getInstance().handPhone == 2) {
					if (MySpinnerButton.INIT_FAIL.equals(content)) {
						toast = Toast.makeText(ChaXun_Activity.this, content,
								3000);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					} else {
						Speek(R.raw.beep);
						SetLoding("标签获取中...");
						Enable(false);
						qrCodeNet(content);
					}
				}

				// 如果是肯麦思
				else if (AppConfig.getInstance().handPhone == 3) {
					Speek(R.raw.beep);
					SetLoding("标签获取中...");
					Enable(false);
					qrCodeNet(content);
				}

			}
		});
	}

	private void scanShanZhai() {
//		if (thread != null) {
//			thread.interrupt();
//		}
//		thread = new ScanThreadTest(mHandler);
//		thread.start();
//
//		if (AppConfig.getInstance().mSerialPort.scaner_trig_stat() == true) {
//			AppConfig.getInstance().mSerialPort.scaner_trigoff();
//		} else {
//			AppConfig.getInstance().mSerialPort.scaner_trigon();
//		}
	}

	@Click
	void handInputClicked() {
		new HandInputDlg(ChaXun_Activity.this, new handInputSureListener() {

			@Override
			public void handInputSure(String handInputLabelText) {
				SetLoding("标签获取中...");
				Enable(false);
				qrCodeNet(handInputLabelText);
			}
		}).show();
	}

	@Click
	void mTitleBackClicked() {
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mySpinnerButton.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mySpinnerButton.pause();
		// 如果是山寨机
//		if (AppConfig.getInstance().handPhone == 0) {
//			if (thread != null) {
//				thread.interrupt();
//			}
//			if (AppConfig.getInstance().mSerialPort.scaner_trig_stat() == true) {
//				AppConfig.getInstance().mSerialPort.scaner_trigoff();
//			}
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mySpinnerButton.destory();

		speechSynthesizer.stopSpeaking();
		speechSynthesizer.destroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// 如果是山寨机
		if (AppConfig.getInstance().handPhone == 0) {
			if (keyCode == 134) {
				if (event.getRepeatCount() == 0) {
					scanShanZhai();
					return true;
				}
			}
		}
		// 如果是成为手持机
		else if (AppConfig.getInstance().handPhone == 1) {

			if (keyCode == 136 || keyCode == 139) {
				if (event.getRepeatCount() == 0) {
					mySpinnerButton.keyDown(keyCode);
					return true;
				}
			}
		}

		// 如果是思必拓手持机
		else if (AppConfig.getInstance().handPhone == 2) {
			if (keyCode == 134 || keyCode == 135) {
				if (event.getRepeatCount() == 0) {
					mySpinnerButton.keyDown(keyCode);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 获取标签信息
	 * 
	 * @param qrcode
	 */
	@Background
	void qrCodeNet(String qrcode) {

		String request = Until.getLabelEvent_req(qrcode,
				AppConfig.getInstance().UserId);

		String GET_LABEL_EVENT_URL = AppConfig.getInstance().serviceIp
				+ AppConfig.getInstance().serviceIpAfter + "labelEventRecorder";
		String response = HttpUtils.httpPut(GET_LABEL_EVENT_URL, request);

		// 服务器连接失败
		if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
			getLabelInfoErr(response);
		}
		// 服务器连接成功
		else {
			String successFlag = Until.parseResult(response);
			// 查询成功
			if ("OK".equals(successFlag)) {

				HashMap<String, Object> obj = Until.queryLabelEvent(response);
				getLabelInfoSuccess(obj, qrcode);
			}
			// 查询失败
			else {
				getLabelInfoErr(successFlag);
			}
		}
	}

	@UiThread
	void getLabelInfoSuccess(HashMap<String, Object> obj, String qrcode) {
		// speechSynthesizer.startSpeaking(obj.get("wasteName").toString(),
		// this);
		mQrCode.setText(qrcode);

		obj.put("qrCode", qrcode);
		wasteName.setText(obj.get("wasteName").toString());
		zhuangzhi.setText(obj.get("gongxu").toString());
		harmFeature.setText(obj.get("harmFeature").toString());
		shifter.setText(obj.get("shifter").toString());
		dangerCase.setText(obj.get("dangerCase").toString());

		if (obj.containsKey("weight")) {
			weight.setText(obj.get("weight").toString());
		} else {
			weight.setText("0");
		}

		if (obj.containsKey("labelEvents")) {
			mAdapter.SetData((ArrayList<HashMap<String, Object>>) obj
					.get("labelEvents"));
		}

		SetLodingHid();
		Enable(true);
	}

	@UiThread
	void getLabelInfoErr(String result) {
		toast = Toast.makeText(ChaXun_Activity.this, result, 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		SetLodingHid();
		Enable(true);
	}
}
