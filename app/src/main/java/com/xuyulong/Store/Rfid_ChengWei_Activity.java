package com.xuyulong.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.utility.StringUtility;
import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.entity.ResultMessage;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SoundUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;

@EActivity(R.layout.rfid_activity)
public class Rfid_ChengWei_Activity extends SpeekBaseActivity implements
		OnItemClickListener {
	public static final String GET_CAR_INFO_URL = AppConfig.getInstance().serviceIp
			+ AppConfig.getInstance().serviceIpAfter + "scanCarCard";

	public static final String CHUKU_URL = AppConfig.getInstance().serviceIp
			+ AppConfig.getInstance().serviceIpAfter + "inventoryOut";

	private Toast toast;
	public RFIDWithUHF mReader = null; // 超高频读写器
	public final String REQ = "REQ";
	SoundUtils mPlayer;
	@Extra(REQ)
	NetRequest mReq;

	private boolean initFlag = false;
	private boolean threadStop = true;
	private Handler handler;

	@Click
	void mTitleBackClicked() {
		if (mReader != null) {
			mReader.free();
			mReader = null;
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
	private ArrayList<HashMap<String, String>> tagList;
	private SimpleAdapter adapter;
	private LinkedList<ResultMessage> carList = new LinkedList<ResultMessage>();
	private ResultMessage carInfoOne = null;

	@Click
	void mBtSureClicked() {

		if ("".equals(mText.getText().toString())) {
			toast = Toast.makeText(Rfid_ChengWei_Activity.this, "请先扫卡！", 3000);
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
						SetLoding("出库中...");
						Enable(false);
						chukuNet(mStrupload);
					}
				}).show();

	}

	// /////////////////////////////////////
	/**
	 * 出库
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
		toast = Toast.makeText(Rfid_ChengWei_Activity.this, "出库成功!", 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		SetLodingHid();
		Enable(true);
		setResult(Activity.RESULT_OK);
		finish();
	}

	@UiThread
	void rukuErr(String result) {
		toast = Toast.makeText(Rfid_ChengWei_Activity.this, result, 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();

		SetLodingHid();
		Enable(true);
	}

	// ///////////////////////////////////////
	/*
	 * 扫车卡
	 */
	@Click
	void mScanCarCardClicked() {

		if (initFlag) {
			readTag();
		} else {
			status.setVisibility(View.VISIBLE);
			status.setText("初始化端口失败,确保为手持机后请重试！");
		}
	}

	private void readTag() {
		if (threadStop) {
			mTextTip.setText("停止扫描");
			mTextTip.setTextColor(android.graphics.Color.RED);
			new TagThread(handler).start();
		} else {
			// 停止识别
			stopInventory();
		}
	}

	class TagThread extends Thread {

		String strUII = "";
		Handler handler = null;
		HashMap<String, String> map;

		public TagThread(Handler handler) {
			this.handler = handler;
		}

		public void run() {
			// 设置标志位
			threadStop = false;
			while (!threadStop && StringUtility.isEmpty(strUII)) {
				strUII = mReader.inventorySingleTag();
			}

			// 设置标志位
			threadStop = true;

			if (!StringUtility.isEmpty(strUII)) {
				String strEPC = mReader.convertUiiToEPC(strUII);
				Message msg = handler.obtainMessage();
				msg.obj = strEPC;
				handler.sendMessage(msg);
				System.out.println("卡号------------------>" + strEPC);
			}
		}
	}

	/**
	 * 停止识别
	 */
	private void stopInventory() {
		if (!threadStop) {
			threadStop = true;
			if (mReader.stopInventory()) {
				mTextTip.setText("开始扫描");
				mTextTip.setTextColor(android.graphics.Color.BLACK);
			} else {
				Toast.makeText(Rfid_ChengWei_Activity.this, "关闭失败",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////
	@AfterViews
	void Init() {
		mTitelTxt.setText("出库");
		rfidData.setOnItemClickListener(this);
		tagList = new ArrayList<HashMap<String, String>>();

		adapter = new SimpleAdapter(Rfid_ChengWei_Activity.this, tagList,
				R.layout.item_of_listview_rfid, new String[] { "ID", "EPC",
						"COUNT" }, new int[] { R.id.textView_id,
						R.id.textView_epc, R.id.textView_count });

		mPlayer = new SoundUtils(this);
		// 初始化声音池
		if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
			ToSpeek(R.raw.saomiaochaka);
		}

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				System.out.println("Handler卡号------------------>"
						+ (String) msg.obj);
				mTextTip.setText("开始扫描");
				mTextTip.setTextColor(android.graphics.Color.BLACK);

				SetLoding("获取车卡信息中...");
				Enable(false);
				carNet((String) msg.obj);
			}
		};

		try {
			mReader = RFIDWithUHF.getInstance();
		} catch (Exception ex) {
			status.setVisibility(View.VISIBLE);
			status.setText("初始化端口失败,请确保设备为手持机");
			mScanCarCard.setEnabled(false);
			return;
		}
		if (mReader != null) {
			new InitTask().execute();
		}

	}

	/**
	 * 添加EPC到列表中
	 * 
	 * @param epc
	 */
	private void addEPCToList(String epc, ResultMessage res) {
		if (!StringUtility.isEmpty(epc)) {
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
		if (StringUtility.isEmpty(strEPC)) {
			return existFlag;
		}
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

	// /////////////////////////////////////////////////////////////////

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

		if (threadStop) {
		} else {
			// 停止识别
			stopInventory();
			mTextTip.setText("开始扫描");
			mTextTip.setTextColor(android.graphics.Color.BLACK);
		}

		carInfoOne = carList.get(position);
		show();
		System.out.println("item的position:------>" + position);
	}

	// //////////////////////////////////////////////////////////////////////////////////

	private void show() {
		mText.setText("车牌号：" + carInfoOne.getCarNo() + "\n司机名："
				+ carInfoOne.getDriverName());
	}

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

		toast = Toast.makeText(Rfid_ChengWei_Activity.this, result, 3000);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		SetLodingHid();
		Enable(true);
	}

	// ////////////////////////////////////////////////////////////////////

	/**
	 * 设备上电异步类
	 * 
	 */
	public class InitTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			return mReader.init();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			initFlag = result;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		// 停止识别
		stopInventory();
	}

	@Override
	protected void onDestroy() {

		if (mReader != null) {
			mReader.free();
			mReader = null;
		}
		super.onDestroy();
	}
}