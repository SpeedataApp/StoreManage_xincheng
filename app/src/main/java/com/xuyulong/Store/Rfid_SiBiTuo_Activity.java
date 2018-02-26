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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.rscja.utility.StringUtility;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.Tag_Data;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.entity.ResultMessage;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SoundUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;

@EActivity(R.layout.rfid_activity)
public class Rfid_SiBiTuo_Activity extends SpeekBaseActivity implements
        OnItemClickListener {

    public static final String GET_CAR_INFO_URL = AppConfig.getInstance().serviceIp
            + AppConfig.getInstance().serviceIpAfter + "scanCarCard";

    public static final String CHUKU_URL = AppConfig.getInstance().serviceIp
            + AppConfig.getInstance().serviceIpAfter + "inventoryOut";

    private Toast toast;
    // public RFIDWithUHF mReader = null; // 超高频读写器
    public final String REQ = "REQ";
    SoundUtils mPlayer;
    @Extra(REQ)
    NetRequest mReq;

    private Handler handler;

    @Click
    void mTitleBackClicked() {
        finish();
    }

    @UiThread(delay = 1000)
    void ToSpeek(int str) {
        Speek(str);
    }

    @ViewById
    TextView mText;// 显示车卡号
    @ViewById
    TextView mTextTip;// "开始扫描"提示
    @ViewById
    ImageButton mScanCarCard;// 扫描图像按钮
    @ViewById
    ListView rfidData;// 显示车卡号等信息列表
    @ViewById
    TextView status;// 状态文本,
    @ViewById
    RelativeLayout mRoot;// 该界面整个布局
    @ViewById
    Button mBtSure;// 底部"出库"按钮
    @ViewById
    ImageButton mTitleBack;// 返回上一页按钮
    @ViewById
    TextView mTitelTxt;// 页面标题文本

    private String mStrupload = "";
    private ArrayList<HashMap<String, String>> tagList;
    private SimpleAdapter adapter;
    private LinkedList<ResultMessage> carList = new LinkedList<ResultMessage>();
    private ResultMessage carInfoOne = null;

    private boolean inSearch = false;
    private IUHFService iuhfService;
    private PowerManager pM = null;
    private WakeLock wk = null;

    @Click
    void mBtSureClicked() {

        if ("".equals(mText.getText().toString())) {
            toast = Toast.makeText(Rfid_SiBiTuo_Activity.this, "请先扫卡！", Toast.LENGTH_SHORT);
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
        toast = Toast.makeText(Rfid_SiBiTuo_Activity.this, "出库成功!", 3000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        SetLodingHid();
        Enable(true);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @UiThread
    void rukuErr(String result) {
        toast = Toast.makeText(Rfid_SiBiTuo_Activity.this, result, 3000);
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
        if (inSearch) {
            inSearch = false;
            iuhfService.inventory_stop();
        } else {
            inSearch = true;
            iuhfService.inventory_start();
        }

    }

    // //////////////////////////////////////////////////////////////////
    @AfterViews
    void Init() {

        try {
            iuhfService = UHFManager.getUHFService(Rfid_SiBiTuo_Activity.this);
            String factory = SharedXmlUtil.getInstance(getApplicationContext())
                    .read("modle", "");
            if (factory.equals("3992")) {
                Toast.makeText(Rfid_SiBiTuo_Activity.this, "UHF模块不识别或无模块",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

//			if (iuhfService == null) {
//                Toast.makeText(Rfid_SiBiTuo_Activity.this, "模块不识别",
//                        Toast.LENGTH_SHORT).show();
//            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Rfid_SiBiTuo_Activity.this, "UHF模块不识别或无模块",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        newWakeLock();

        mTitelTxt.setText("出库");
        rfidData.setOnItemClickListener(this);
        tagList = new ArrayList<HashMap<String, String>>();

        adapter = new SimpleAdapter(Rfid_SiBiTuo_Activity.this, tagList,
                R.layout.item_of_listview_rfid, new String[]{"ID", "EPC",
                "COUNT"}, new int[]{R.id.textView_id,
                R.id.textView_epc, R.id.textView_count});

        mPlayer = new SoundUtils(this);
        // 初始化声音池
        if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
            ToSpeek(R.raw.saomiaochaka);
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    ArrayList<Tag_Data> ks = (ArrayList<Tag_Data>) msg.obj;
                    String[] tmp = new String[ks.size()];
                    for (int i = 0; i < ks.size(); i++) {
                        byte[] nq = ks.get(i).epc;
                        if (nq != null) {
                            tmp[i] = new String();
                            for (int j = 0; j < nq.length; j++) {
                                tmp[i] += String.format("%02x", nq[j]);
                            }
                        }
                    }
                    String[] x = tmp[0].trim().split(" ");
                    String labelCode = "";
                    for (String one : x) {
                        labelCode += one;
                    }
                    // 停止识别
                    iuhfService.inventory_stop();
                    System.out.println("Handler卡号------------------>"
                            + labelCode);

                    SetLoding("获取车卡信息中...");
                    Enable(false);
                    carNet(labelCode);
                }

            }
            // 停止识别
            // stopInventory();
            // System.out.println("Handler卡号------------------>"
            // + (String) msg.obj);
            //
            // SetLoding("获取车卡信息中...");
            // Enable(false);
            // carNet((String) msg.obj);
        };
        iuhfService.reg_handler(handler);

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
     * @param strEPC 索引
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

        // 停止识别
        stopInventory();

        carInfoOne = carList.get(position);
        show();
        System.out.println("item的position:------>" + position);
    }

    // //////////////////////////////////////////////////////////////////////////////////

    private void stopInventory() {
        mTextTip.setText("开始扫描");
        mTextTip.setTextColor(android.graphics.Color.BLACK);
    }

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

        toast = Toast.makeText(Rfid_SiBiTuo_Activity.this, result, 3000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        SetLodingHid();
        Enable(true);
    }

    // ////////////////////////////////////////////////////////////////////

    @Override
    protected void onResume() {
        super.onResume();
        if (openDev())
            return;
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止识别
        if (inSearch) {
            iuhfService.inventory_stop();
            inSearch = false;
        }
        iuhfService.CloseDev();
    }

    private void newWakeLock() {
        pM = (PowerManager) getSystemService(POWER_SERVICE);
        if (pM != null) {
            wk = pM.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "lock3992");
            if (wk != null) {
                wk.acquire();
            }
        }
    }

    private boolean openDev() {
        if (iuhfService.OpenDev() != 0) {
            status.setText("初始化端口失败！");
            new AlertDialog.Builder(this)
                    .setTitle("警告！")
                    .setMessage("端口初始化失败！")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    // TODO Auto-generated method stub
                                    finish();
                                }
                            }).show();
            return true;
        }
        return false;

    }

    @Override
    protected void onDestroy() {
        if (wk != null) {
            wk.release();
        }
        super.onDestroy();
    }
}