package com.xuyulong.Store;

import java.util.ArrayList;
import java.util.HashMap;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.Barcode2D;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.wenyankeji.entity.NetRequest;
//import com.wenyankeji.thread.ScanThreadTest;
import com.xuyulong.adapter.ListViewRuKuAdapter;
import com.xuyulong.ui.HandInputDlg;
import com.xuyulong.ui.HandInputDlg.handInputSureListener;
import com.xuyulong.ui.MySpinnerButton;
import com.xuyulong.ui.MySpinnerButton.GetQRListener;
import com.xuyulong.ui.QQListView;
import com.xuyulong.ui.QQListView.DelButtonClickListener;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;
import com.zbar.lib.CaptureActivity;

/**
 * 废弃 功能已在贴标实现了
 * @author admin
 *
 */
@EActivity(R.layout.ruku_activity)
public class RuKu_Activity extends SpeekBaseActivity {

	private final int SCAN_REQUEST = 1;
	private final int CHENZHONG_REQUEST = 2;
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
	QQListView mList;
	@ViewById
	Button mEdit;
	@ViewById
	RelativeLayout mRoot;
	@ViewById
	Button mRuKu;
	@ViewById
	TextView mTitelTxt;
	@ViewById
	MySpinnerButton mySpinnerButton;
	@ViewById
	ImageButton handInput;
	
	ListViewRuKuAdapter mAdapter = null;
	private Handler mHandler;
//	private ScanThreadTest thread = null;
	
	@UiThread(delay = 1000)
	void ToSpeek(int str) {
		Speek(str);
	}

	void Enable(boolean enabled) {
		mEdit.setEnabled(enabled);
		mRuKu.setEnabled(enabled);
		mList.setEnabled(enabled);
	}

	@OnActivityResult(CHENZHONG_REQUEST)
	void onCHENZHONGResult(int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			finish();
		}
	}

	@OnActivityResult(SCAN_REQUEST)
	void onResult(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String labelCode = data.getStringExtra("labelCode");
			SetLoding("标签获取中...");
			Enable(false);
			qrCodeNet(labelCode);
		}
	}
	
	@ItemClick
	public void mListItemClicked(HashMap<String, Object> obj) {
		mQrCode.setText(obj.get("qrCode").toString());
		wasteName.setText(obj.get("wasteName").toString());
		zhuangzhi.setText(obj.get("gongxu").toString());
		harmFeature.setText(obj.get("harmFeature").toString());
		shifter.setText(obj.get("shifter").toString());
		dangerCase.setText(obj.get("dangerCase").toString());

		if (obj.containsKey("weight")) {
			weight.setText(obj.get("weight").toString());
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

		mTitelTxt.setText("入库");
		mAdapter = new ListViewRuKuAdapter(
				new ArrayList<HashMap<String, Object>>(), this);
		mList.setAdapter(mAdapter);
		mList.setDelButtonClickListener(new DelButtonClickListener() {
			@Override
			public void clickHappend(int position) {
				mAdapter.DeleteHash(position);
				clearDetail();
			}
		});

		if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
			ToSpeek(R.raw.saomiaobiaoqian);
		}

		mySpinnerButton.creat();
		// 回调接口
		mySpinnerButton.setGetQRListener(new GetQRListener() {

			@Override
			public void getQRHappend(String content) {
				if (MySpinnerButton.SCAN_WAY_CAMERA.equals(content)) {

					Intent intent = new Intent(RuKu_Activity.this,
							CaptureActivity.class);
					startActivityForResult(intent, SCAN_REQUEST);
					return;
				} 
				
				// 如果是山寨机
				if(AppConfig.getInstance().handPhone == 0){
					if (MySpinnerButton.SCAN_WAY_SCAN_HEAD.equals(content)) {
						if (AppConfig.getInstance().initFlag == false) {
							toast = Toast.makeText(RuKu_Activity.this, content,
									3000);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						} else {
							scanShanZhai();
						}
					}
				}
				// 如果是成为
				else if(AppConfig.getInstance().handPhone == 1){
					if (MySpinnerButton.INIT_FAIL.equals(content)) {
						toast = Toast
								.makeText(RuKu_Activity.this, content, 3000);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					} else {
						SetLoding("标签获取中...");
						Enable(false);
						qrCodeNet(content);
					}
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
	void handInputClicked(){
		new HandInputDlg(RuKu_Activity.this, new handInputSureListener(){

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

	@Click
	void mRuKuClicked() {

		if (mAdapter.getCount() == 0) {
			toast = Toast.makeText(RuKu_Activity.this, "您还没有扫描标签！", 3000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			return;
		}

		new MySureDlg(this, "确认数量", mAdapter.getCount() + "", "", "", "",
				new MySureDlg.OnCustomDialogListener() {

					@Override
					public void Sure() {
						// mode:1 只有选择仓库
						int mode = 1;
						NetRequest netRequest = mAdapter.GetJsonResultRuku();

//						startActivityForResult(
//								ChenZhong_Activity_.intent(RuKu_Activity.this)
//										.mJson("").mMode(mode).mReq(netRequest)
//										.get(), CHENZHONG_REQUEST);
					}

				}).show();

	}

	@ViewById
	Button mCancle;

	@Click
	void mCancleClicked() {
		mAdapter.Cancle();
		mAdapter.ShowCheck(false);
		mEdit.setText("编辑");
		mCancle.setVisibility(View.GONE);
	}

	@Click
	void mEditClicked() {
		if (!mAdapter.GetCheck()) {
			mAdapter.ShowCheck(true);
			mEdit.setText("删除");
			mCancle.setVisibility(View.VISIBLE);
		} else {
			mAdapter.ShowCheck(false);
			mEdit.setText("编辑");
			int count = mAdapter.DeleteSelect();
			if (count > 0) {
				clearDetail();
			}
			mCancle.setVisibility(View.GONE);
		}
	}

	/**
	 * 删除item时清空详细信息
	 */
	protected void clearDetail() {
		mQrCode.setText("");
		wasteName.setText("");
		zhuangzhi.setText("");
		harmFeature.setText("");
		shifter.setText("");
		dangerCase.setText("");
		weight.setText("");
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
					if(AppConfig.getInstance().ruku_tiebiao_Flag &&
							AppConfig.getInstance().barcode2DInstance == null){
						Toast toast = Toast.makeText(RuKu_Activity.this, "正在初始化扫描头", 3000);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
			
						try {
							AppConfig.getInstance().barcode2DInstance = Barcode2D.getInstance();
							AppConfig.getInstance().initFlag = true;
						} catch (ConfigurationException e) {
							AppConfig.getInstance().initFlag = false;
						}
						mySpinnerButton.resume();
					}
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

		String request = Until.getLabelInfo_req(qrcode,
				AppConfig.getInstance().UserId, 2);

		String GET_LABEL_INFO_URL = AppConfig.getInstance().serviceIp
				+ AppConfig.getInstance().serviceIpAfter + "scanLabel";
		String response = HttpUtils.httpPut(GET_LABEL_INFO_URL, request);

		// 服务器连接失败
		if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
			getLabelInfoErr(response, qrcode);
		}
		// 服务器连接成功
		else {
			String successFlag = Until.parseResult(response);
			// 查询成功
			if ("OK".equals(successFlag)) {
				HashMap<String, Object> obj = Until.getLabelCode(response);
				getLabelInfoSuccess(obj, qrcode);
			}
			// 查询失败
			else {
				getLabelInfoErr(successFlag, qrcode);
			}
		}
	}

	@UiThread
	void getLabelInfoSuccess(HashMap<String, Object> obj, String qrcode) {
		mQrCode.setText(qrcode);

		// 判断是否已经在列表中
		if (mAdapter.getData() != null && mAdapter.getData().size() != 0) {
			ArrayList<HashMap<String, Object>> dataAll = mAdapter.getData();
			for (int i = 0; i < dataAll.size(); i++) {
				HashMap<String, Object> dataOne = dataAll.get(i);
				if (qrcode.equals(dataOne.get("qrCode"))) {

					mAdapter.replaceHash(dataOne, i);

					toast = Toast.makeText(RuKu_Activity.this,
							"该标签已扫过！列表中该数据已经更新！", 3000);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					SetLodingHid();
					Enable(true);

					return;
				}

				// 判断是否为不同类型的危废
				if (!(obj.get("wasteCode").equals(
						dataAll.get(0).get("wasteCode")) && obj
						.get("wasteName").equals(
								dataAll.get(0).get("wasteName")))) {
					toast = Toast.makeText(RuKu_Activity.this, "只能扫描同类型危废！",
							3000);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					SetLodingHid();
					Enable(true);

					return;
				}
			}
		}

		if (obj != null) {
			obj.put("qrCode", qrcode);
			mAdapter.AddHash(obj);
			wasteName.setText(obj.get("wasteName").toString());
			zhuangzhi.setText(obj.get("gongxu").toString());
			harmFeature.setText(obj.get("harmFeature").toString());
			shifter.setText(obj.get("shifter").toString());
			dangerCase.setText(obj.get("dangerCase").toString());

			if (obj.containsKey("weight")) {
				weight.setText(obj.get("weight").toString());
			}
		}
		SetLodingHid();
		Enable(true);
	}

	@UiThread
	void getLabelInfoErr(String result, String qrcode) {
		SetLodingHid();
		Enable(true);
		
		if("标签未粘贴,不能入库!".equals(result)){
			toast = Toast.makeText(RuKu_Activity.this, "标签未粘贴,转向贴标界面!", 3000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			
			AppConfig.getInstance().ruku_tiebiao_Flag = true;
			startActivity(
					TieBiao_Activity_.intent(RuKu_Activity.this).get());
		}else{
			toast = Toast.makeText(RuKu_Activity.this, result, 3000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
}
