package com.xuyulong.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.alibaba.fastjson.JSON;
import com.wenyankeji.broadcastreceiver.ScreenStateReceiver;
import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.entity.ResultMessage;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SoundUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;

@EActivity(R.layout.rfid_activity)
public class Rfid_Activity extends SpeekBaseActivity implements
		OnItemClickListener {

	public static final String GET_CAR_INFO_URL = AppConfig.getInstance().serviceIp
			+ AppConfig.getInstance().serviceIpAfter + "scanCarCard";

	public static final String CHUKU_URL = AppConfig.getInstance().serviceIp
			+ AppConfig.getInstance().serviceIpAfter + "inventoryOut";

	private Toast toast;
	private boolean startFlag = false;
	private boolean initFlag = false;
	private Handler handler;
	private LinkedList<ResultMessage> carList = new LinkedList<ResultMessage>();
	private ResultMessage carInfoOne = null;
	private ArrayList<HashMap<String, String>> tagList;
	private SimpleAdapter adapter;
//	private UhfReader reader; // 超高频读写器
	private ScreenStateReceiver screenReceiver = null;
	private List<byte[]> epcList = null;
	private InventoryThread thread = null;
	public final String REQ = "REQ";
	SoundUtils mPlayer;
	@Extra(REQ)
	NetRequest mReq;

	@Click
	void mTitleBackClicked() {
		finish();
	}

	@UiThread(delay = 1000)
	void ToSpeek(int str) {
		Speek(str);
	}

	@ViewById
	TextView mText;
	@ViewById
	TextView mTextTip;
	@ViewById
	ImageButton mScanCarCard;
	@ViewById
	ListView rfidData;
	@ViewById
	TextView status;
	@ViewById
	RelativeLayout mRoot;
	@ViewById
	Button mBtSure;
	@ViewById
	ImageButton mTitleBack;
	@ViewById
	TextView mTitelTxt;

	private String mStrupload = "";

	@Click
	void mBtSureClicked() {

		if ("".equals(mText.getText().toString())) {
			toast = Toast.makeText(Rfid_Activity.this, "请先扫卡！", 3000);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}

		mReq.getReceiveMessage().getChukuCs().setCarNo(carInfoOne.getCarNo());
		mReq.getReceiveMessage().getChukuCs().setRfid(carInfoOne.getRfid());

		mStrupload = JSON.toJSONString(mReq);
		String number = "";
		number = mReq.getReceiveMessage().getChukuCsDetail().size() + "";

		new MySureDlg(this, "出库提交确认", number, mText.getText().toString(), "",
				"", new MySureDlg.OnCustomDialogListener() {
					@Override
					public void Sure() {
						chukuNet(mStrupload);
					}
				}).show();

	}

	// /////////////////////////////////////
	/**
	 * 入库
	 * 
	 * @param json
	 */
	@Background
	void chukuNet(String json) {

		String response = HttpUtils.httpPut(CHUKU_URL, json);

		// 服务器连接失败
		if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
			rukuErr(response);
		}
		// 服务器连接成功
		else {
			String successFlag = Until.parseResult(response);
			// 查询成功
			if ("OK".equals(successFlag)) {
				rukuSuccess();
			}
			// 查询失败
			else {
				rukuErr(successFlag);
			}
		}
	}

	@UiThread
	void rukuSuccess() {
		toast = Toast.makeText(Rfid_Activity.this, "出库成功!", 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		SetLodingHid();
		Enable(true);
		setResult(Activity.RESULT_OK);
		finish();
	}

	@UiThread
	void rukuErr(String result) {
		toast = Toast.makeText(Rfid_Activity.this, result, 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		SetLodingHid();
		Enable(true);
	}

	// ///////////////////////////////////////

	@Click
	void mScanCarCardClicked() {
		if (initFlag) {
			if(thread != null){
				startFlag = false;
				thread.interrupt();
				thread = null;
				mTextTip.setText("开始扫描");
				mTextTip.setTextColor(android.graphics.Color.BLACK);
			}else{
				startFlag = true;
				epcList = null;
				mTextTip.setText("停止扫描");
				mTextTip.setTextColor(android.graphics.Color.RED);
				thread = new InventoryThread(handler);
				thread.start();
			}
		}
	}

	@AfterViews
	void Init() {

		mTitelTxt.setText("出库");
		rfidData.setOnItemClickListener(this);
		mPlayer = new SoundUtils(this);
		// 初始化声音池
		if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
			ToSpeek(R.raw.saomiaochaka);
		}

		tagList = new ArrayList<HashMap<String, String>>();

		adapter = new SimpleAdapter(Rfid_Activity.this, tagList,
				R.layout.item_of_listview_rfid, new String[] { "ID", "EPC",
						"COUNT" }, new int[] { R.id.textView_id,
						R.id.textView_epc, R.id.textView_count });

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				System.out.println("Handler卡号------------------>"
						+ (String) msg.obj);
				mTextTip.setText("开始扫描");
				mTextTip.setTextColor(android.graphics.Color.BLACK);
				if(thread != null){
					thread.interrupt();
					thread = null;
				}
				SetLoding("获取车卡信息中...");
				Enable(false);
				carNet((String) msg.obj);
			}
		};

		// 获取读写器实例，若返回Null,则串口初始化失败
//		reader = AppConfig.getInstance().reader;
		initFlag = AppConfig.getInstance().carInitFlag;
		if (initFlag == false) {
			status.setVisibility(0);
			status.setText("初始化端口失败,请确保设备为手持机");
			mScanCarCard.setEnabled(false);
			return;
		}
		
		// 获取用户设置功率,并设置
		SharedPreferences shared = getSharedPreferences("power", 0);
		int value = shared.getInt("value", 26);
		System.out.println("用户设置功率:" + value);
		Log.e("", "value" + value);
//		reader.setOutputPower(value);

		// 添加广播，默认屏灭时休眠，屏亮时唤醒
		screenReceiver = new ScreenStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(screenReceiver, filter);

	}

	// 设置按钮是否可用
	void Enable(boolean enabled) {
		mScanCarCard.setEnabled(enabled);
		rfidData.setEnabled(enabled);
		mBtSure.setEnabled(enabled);
		mTitleBack.setEnabled(enabled);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
		
		mTextTip.setText("开始扫描");
		mTextTip.setTextColor(android.graphics.Color.BLACK);

		carInfoOne = carList.get(position);
		show();
		System.out.println("item的position:------>" + position);
	}

	// //////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取车辆信息
	 * 
	 * @param carCode
	 */
	@Background
	void carNet(String rfid) {

		String request = Until.getCar_req(rfid);
		String response = HttpUtils.httpPut(GET_CAR_INFO_URL, request);

		// 服务器连接失败
		if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
			carErr(response);
		}
		// 服务器连接成功
		else {
			String successFlag = Until.parseResult(response);
			// 查询成功
			if ("OK".equals(successFlag)) {
				carInfoOne = Until.geCarInfo(response);
				carInfoOne.setRfid(rfid);
				carSuccess(carInfoOne);
			}
			// 查询失败
			else {
				carErr(successFlag);
			}
		}
	}

	@UiThread
	void carSuccess(ResultMessage res) {
		addEPCToList(res.getRfid(), res);

		show();
		SetLodingHid();
		Enable(true);
	}

	@UiThread
	void carErr(String result) {
		mText.setText("");

		toast = Toast.makeText(Rfid_Activity.this, result, 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		SetLodingHid();
		Enable(true);
	}

	/**
	 * 添加EPC到列表中
	 * 
	 * @param epc
	 */
	private void addEPCToList(String epc, ResultMessage res) {

		HashMap<String, String> map = new HashMap<String, String>();
		map.put("EPC", epc);

		int index = checkIsExist(epc);
		// 如果列表中没有该数据 则加入列表
		if (index == -1) {
			carList.add(res);
			tagList.add(map);
			map.put("ID", tagList.size() + "");
		}
		rfidData.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 判断EPC是否在列表中
	 * 
	 * @param strEPC
	 *            索引
	 * @return
	 */
	public int checkIsExist(String strEPC) {
		int existFlag = -1;
		String tempStr = "";
		for (int i = 0; i < tagList.size(); i++) {
			HashMap<String, String> temp = new HashMap<String, String>();
			temp = tagList.get(i);
			tempStr = temp.get("EPC");
			if (strEPC.equals(tempStr)) {
				existFlag = i;
				break;
			}
		}
		return existFlag;
	}

	private void show() {
		mText.setText("车牌号：" + carInfoOne.getCarNo() + "\n司机名字："
				+ carInfoOne.getDriverName());
	}

	// ////////////////////////////////////////////////////////////////////

	/**
	 * 盘存线程
	 * 
	 */
	class InventoryThread extends Thread {
		Handler mHandler = null;

		public InventoryThread(Handler handler) {
			this.mHandler = handler;
		}

		@Override
		public void run() {
			while (!interrupted()) {
				while (startFlag && 
						(epcList == null || 
						(epcList != null && epcList.isEmpty())
						)) {
//					epcList = reader.inventoryRealTime(); // 实时盘存
					
					if (epcList != null && !epcList.isEmpty() && epcList.size() > 0) {
						startFlag = false;
						// 播放提示音
						mPlayer.play(R.raw.msg);
						byte[] epc = epcList.get(0);
//						String epcStr = Tools.Bytes2HexString(epc, epc.length);
						Message msg = mHandler.obtainMessage();
//						msg.obj = epcStr;
						mHandler.sendMessage(msg);
						// 设置为空
						epcList = null;
//						System.out.println("卡号------------------>" + epcStr);
					}
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		try {
			if (screenReceiver != null) {
				unregisterReceiver(screenReceiver);
			}
			if (thread != null) {
				thread.interrupt();
			}
		} catch (Exception e) {
			System.out.println("Rfid_Activity_onDestroy_Exception--->" + e);
		}
		super.onDestroy();
	}

}
