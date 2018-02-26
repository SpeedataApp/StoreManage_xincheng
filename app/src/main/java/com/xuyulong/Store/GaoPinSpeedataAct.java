package com.xuyulong.Store;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.PowerManager;
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

import com.alibaba.fastjson.JSON;
import com.rscja.utility.StringUtility;
import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.entity.ResultMessage;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SoundUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * ----------Dragon be here!----------/
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃神兽保佑
 * 　　　　┃　　　┃代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━
 *
 * @author :孙天伟 in  2018/1/16   13:43.
 *         联系方式:QQ:420401567
 *         功能描述:
 */
@EActivity(R.layout.gaopin_layout)
public class GaoPinSpeedataAct extends SpeekBaseActivity implements AdapterView.OnItemClickListener {
    public static final String GET_CAR_INFO_URL = AppConfig.getInstance().serviceIp
            + AppConfig.getInstance().serviceIpAfter + "scanCarCard";

    public static final String CHUKU_URL = AppConfig.getInstance().serviceIp
            + AppConfig.getInstance().serviceIpAfter + "inventoryOut";

    private Toast toast;
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
    TextView mText;// 显示车卡号
    @ViewById
    ImageButton mScanCarCard;// 扫描图像按钮
    @ViewById
    ListView rfidData;// 显示车卡号等信息列表
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
    private PowerManager pM = null;
    private PowerManager.WakeLock wk = null;

    @SuppressLint("WrongConstant")
    @Click
    void mBtSureClicked() {

        if ("".equals(mText.getText().toString())) {
            toast = Toast.makeText(GaoPinSpeedataAct.this, "请先扫卡！", 3000);
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

    @SuppressLint("WrongConstant")
    @UiThread
    void rukuSuccess() {
        toast = Toast.makeText(GaoPinSpeedataAct.this, "出库成功!", 3000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        SetLodingHid();
        Enable(true);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @SuppressLint("WrongConstant")
    @UiThread
    void rukuErr(String result) {
        toast = Toast.makeText(GaoPinSpeedataAct.this, result, 3000);
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
        } else {
            inSearch = true;
        }

    }

    private NfcAdapter mNfcAdapter;
    private PendingIntent pi;
    private IntentFilter tagDetected;

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, pi,
                    null, null);
    }

    @AfterViews
    void Init() {
        mTitelTxt.setText("出库");
        newWakeLock();
        tagList = new ArrayList<>();
        //初始化NfcAdapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // 初始化PendingIntent，当有NFC设备连接上的时候，就交给当前Activity处理
        pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter == null) {
            showToast("NFC不可用！");
            finish();
        } else if (!mNfcAdapter.isEnabled()) {
            showToast("请手动打开NFC！");
            startActivity(new Intent("android.settings.NFC_SETTINGS"));
        }


        adapter = new SimpleAdapter(GaoPinSpeedataAct.this, tagList,
                R.layout.item_of_listview_rfid, new String[]{"ID", "EPC",
                "COUNT"}, new int[]{R.id.textView_id,
                R.id.textView_epc, R.id.textView_count});
        rfidData.setOnItemClickListener(this);
        mPlayer = new SoundUtils(this);
        // 初始化声音池
        if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
            ToSpeek(R.raw.saomiaochaka);
        }
//        mNfcAdapter.enableForegroundDispatch(this, pi, null, null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 当前app正在前端界面运行，这个时候有intent发送过来，那么系统就会调用onNewIntent回调方法，将intent传送过来
        // 我们只需要在这里检验这个intent是否是NFC相关的intent，如果是，就调用处理方法
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            processIntent(intent);
        }
    }


    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    public void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String CardId = ByteArrayToHexString(tagFromIntent.getId());//nfc 卡片id
        SetLoding("获取车卡信息中...");
        Toast.makeText(GaoPinSpeedataAct.this, CardId, Toast.LENGTH_LONG).show();
        Enable(false);
        carNet(CardId);
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
        toast = Toast.makeText(GaoPinSpeedataAct.this, result, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        SetLodingHid();
        Enable(true);
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


    @Override
    protected void onDestroy() {
        if (wk != null) {
            wk.release();
        }
        super.onDestroy();
    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

}
