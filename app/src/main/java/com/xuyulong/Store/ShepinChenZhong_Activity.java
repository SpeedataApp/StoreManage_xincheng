package com.xuyulong.Store;

import android.CRC.CRC;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.serialport.SerialPort;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.wenyankeji.entity.Cangku;
import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.popwindow.SelectPicPopupWindow;
import com.xuyulong.ui.TongdaoHandInputDlg;
import com.xuyulong.ui.TongdaoHandInputDlg.tongdaoSureListener;
import com.xuyulong.util.BaseActivity;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;
import com.xuyulong.util.Utils;

import net.tsz.afinal.FinalDb;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import speedatagroup.brxu.com.myapplication.utils.DataConversionUtils;
import speedatagroup.brxu.com.myapplication.utils.DeviceControl;
import speedatagroup.brxu.com.myapplication.utils.MyLogger;

@EActivity(R.layout.sp_chenzhong_activity)
public class ShepinChenZhong_Activity extends SpeekBaseActivity {
    private FinalDb db;

    private Toast toast;
    public final String UPLOAD = "UPLOAD_PARM";
    public final String MODE = "MODE";
    public final String REQ = "REQ";
    SelectPicPopupWindow menuWindow = null;
    HashMap<String, Object> mKuInfo = null;
    @Extra(MODE)
    int mMode = 0;
    @Extra(REQ)
    NetRequest mReq;

    private final String SERIAL_PORT_433 = "/dev/ttyMT1";
    private int BRD_433 = 9600;
    private final String PATH_433 = "sys/class/misc/mtgpio/pin";
    private DeviceControl mDeviceControl_433;
    private SerialPort mSerialPort_433;
    private int fd_433;
    private MyLogger logger = MyLogger.jLog();
    private Timer read433timer;// task read serial
    private final int SendTypeRece = 0;
    // 0-rece 1-set 2-search
    private int receType = 0;

    @ViewById
    TextView title_paired_devices;
    @ViewById
    TextView title_unBonded_devices;
    @ViewById
    ListView pairedListView;
    @ViewById
    ListView unBondedListView;
    @ViewById
    RelativeLayout mRoot;
    @ViewById
    Button mBtSure;
    @ViewById
    ImageButton mTitleBack;
    @ViewById
    TextView mCangKu;
    @ViewById
    LinearLayout weightly;
    @ViewById
    LinearLayout cankuly;
    @ViewById
    LinearLayout lvLinearLayout;
    @ViewById
    EditText mWeightEdit;
    @ViewById
    ImageButton getWeight;
    @ViewById
    TextView tv;
    @ViewById
    TextView mTitelTxt;
    @ViewById
    ProgressBar progressBar;
    @ViewById
    ImageButton mBtLocal;
    // 手动输入标志位
    private boolean hpChangeable = false;

    KeyListener storeEditWeightListener;

    @Click
    void mTitleBackClicked() {
        finish();
    }

    // @Click
    // void mWeightEditClicked() {
    // hpChangeable = true;
    // }

    /**
     * 获取仓库仓位信息
     */
    @Background
    void getCK() {
        System.out.println("getCK");

        String request = Until.getCK_req(AppConfig.getInstance().enterpriseId);

        System.out.println("request:" + request);

        String GET_CK_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "warehouse";
        String response = HttpUtils.httpPut(GET_CK_URL, request);

        System.out.println("response:" + response);

        // 服务器连接失败
        if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
            getCKErr(response);
        }
        // 服务器连接成功
        else {
            String successFlag = Until.parseResult(response);

            // 登录成功
            if ("OK".equals(successFlag)) {

                mKuInfo = Until.getWarehouseInfo(response);

                getCKSuccess(mKuInfo);
            }
            // 登录失败
            else {
                getCKErr(successFlag);
            }
        }
    }

    @UiThread
    void getCKSuccess(HashMap<String, Object> mKuInfo) {

        menuWindow = new SelectPicPopupWindow(this,
                (String[]) mKuInfo.get("repositoryName"),
                (String[][]) mKuInfo.get("spaceName"), mCangKu,
                (int[]) mKuInfo.get("repositoryId"),
                (int[][]) mKuInfo.get("spaceId"));
        menuWindow.showAtLocation(mRoot, Gravity.BOTTOM
                | Gravity.CENTER_HORIZONTAL, 0, 0);

        SetLodingHid();
        Enable(true);
    }

    @UiThread
    void getCKErr(String result) {
        SetLodingHid();
        Enable(true);

        toast = Toast.makeText(ShepinChenZhong_Activity.this, result,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Click
    void mBtLocalClicked() {
        SetLoding("仓库信息获取中...");
        getCK();
        mBtSure.setEnabled(false);
    }

    private String mStrupload = "";

    @Click
    void mBtSureClicked() {

        if (mMode == 2) {
            if ("".equals(mWeightEdit.getText().toString())) {
                Toast.makeText(this, "请先称重", Toast.LENGTH_SHORT).show();
                return;
            }

            // 总重量
            mReq.getReceiveMessage()
                    .getRukuCs()
                    .setWeight(new BigDecimal(mWeightEdit.getText().toString()));

            if ("".equals(mCangKu.getText().toString())) {
                Toast.makeText(this, "请先选择仓库", Toast.LENGTH_SHORT).show();
                return;
            }

            Cangku cangku = (Cangku) db.findById(
                    AppConfig.getInstance().UserId.toString(), Cangku.class);

            if (cangku != null) {
                int repositoryId = cangku.getRepositoryId();
                int spaceId = cangku.getSpaceId();

                String repositoryName = cangku.getRepositoryName();
                String spaceName = cangku.getSpaceName();

                mReq.getReceiveMessage().getRukuCs()
                        .setLocationId(new BigDecimal(spaceId));
                mReq.getReceiveMessage().getRukuCs()
                        .setWarehouseId(new BigDecimal(repositoryId));
                mReq.getReceiveMessage().getRukuCs().setLocationName(spaceName);
                mReq.getReceiveMessage().getRukuCs()
                        .setWarehouseName(repositoryName);

                String number = "";
                number = mReq.getReceiveMessage().getCsRukuDetails().size()
                        + "";

                mStrupload = JSON.toJSONString(mReq);
                System.out.println("入库请求如下");
                System.out.println(mStrupload);

                new MySureDlg(this, "入库提交确认", number, "", mWeightEdit.getText()
                        .toString(), mCangKu.getText().toString(),
                        new MySureDlg.OnCustomDialogListener() {

                            @Override
                            public void Sure() {
                                if (!mStrupload.equals("")) {
                                    SetLoding("入库中...");
                                    Enable(false);
                                    mTitleBack.setEnabled(false);
                                    rukuNet(mStrupload);
                                } else {
                                    Toast.makeText(
                                            ShepinChenZhong_Activity.this,
                                            "系统错误", Toast.LENGTH_SHORT).show();
                                }
                            }

                        }).show();
            }

        }
    }

    // /////////////////////////////////////

    /**
     * 入库
     *
     * @param json
     */
    @Background
    void rukuNet(String json) {
        String RUKU_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "inventoryIn";
        String response = HttpUtils.httpPut(RUKU_URL, json);

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
        toast = Toast.makeText(ShepinChenZhong_Activity.this, "入库成功!",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // 接受一个入库单号
        SetLodingHid();
        Enable(true);
        mTitleBack.setEnabled(true);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @UiThread
    void rukuErr(String result) {
        toast = Toast.makeText(ShepinChenZhong_Activity.this, result,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        SetLodingHid();
        Enable(true);
        mTitleBack.setEnabled(true);
    }

    // ///////////////////////////////////////

    /**
     * 获取标签信息
     *
     * @param json
     */
    @Background
    void tiebiaoNet(String json) {

        System.out.println("贴标的request" + json);

        String TIEBIAO_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "scanLabelUpdate";
        String response = HttpUtils.httpPut(TIEBIAO_URL, json);

        // 服务器连接失败
        if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
            tiebiaoErr(response);
        }
        // 服务器连接成功
        else {
            String successFlag = Until.parseResult(response);
            // 查询成功
            if ("OK".equals(successFlag)) {
                tiebiaoSuccess();
            }
            // 查询失败
            else {
                tiebiaoErr(successFlag);
            }
        }
    }

    @UiThread
    void tiebiaoSuccess() {
        toast = Toast.makeText(ShepinChenZhong_Activity.this, "贴标成功!",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        SetLodingHid();
        Enable(true);
        mTitleBack.setEnabled(true);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @UiThread
    void tiebiaoErr(String result) {
        toast = Toast.makeText(ShepinChenZhong_Activity.this, result,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        SetLodingHid();
        Enable(true);
        mTitleBack.setEnabled(true);
    }

    @Click
    void getWeightClicked() {
        // false:只支持地磅 true：只支持手动输入
//		System.out.println("handInput:" + AppConfig.getInstance().handInput);
//		if(!AppConfig.getInstance().handInput){
//			new TongdaoHandInputDlg(
//					ShepinChenZhong_Activity.this,
//					getPreferencesString(BaseActivity.TONGDAO, ""),
//					new tongdaoSureListener() {
//						@Override
//						public void tongdaoSure(String tongdao) {
//							mWeightEdit.setText("");
//							PutPreferences(BaseActivity.TONGDAO, tongdao);
//						}
//					}
//			).show();
//		}
        hpChangeable = false;
        new TongdaoHandInputDlg(ShepinChenZhong_Activity.this,
                getPreferencesString(BaseActivity.TONGDAO, ""),
                new tongdaoSureListener() {
                    @Override
                    public void tongdaoSure(String tongdao) {
                        mWeightEdit.setText("");
                        PutPreferences(BaseActivity.TONGDAO, tongdao);
                    }
                }).show();
    }

    @UiThread(delay = 1000)
    void ToSpeek(int str) {
        Speek(str);
    }

    // 关闭程序掉用处理部分
    @Override
    public void onDestroy() {
        if (read433timer != null) {
            read433timer.cancel();
        }
        if (mSerialPort_433 != null) {
            mSerialPort_433.CloseSerial(fd_433);
        }
        if (mDeviceControl_433 != null) {
            mDeviceControl_433.MTGpioOff();
        }

        super.onDestroy();

        System.out.println("onDestroy:开始执行");
    }

    // 设置按钮是否可用
    void Enable(boolean enabled) {
        mBtLocal.setEnabled(enabled);
        mBtSure.setEnabled(enabled);
        getWeight.setEnabled(enabled);
        // mWeightEdit.setEnabled(enabled);
    }

    @UiThread
    void setWeight(String weight) {
        if (!hpChangeable) {
            mWeightEdit.setText(parseWeight(weight)); // 显示数据
        }
    }

    @AfterViews
    void Init() {
        lvLinearLayout.setVisibility(View.GONE);
        db = AppConfig.getInstance().db;
        Cangku cangku = db.findById(AppConfig.getInstance().UserId.toString(),
                Cangku.class);
        if (cangku != null) {
            if (cangku.isCangkuDefault()) {
                mCangKu.setText(cangku.getRepositoryName() + "\n"
                        + cangku.getSpaceName());
            }
        }
        mBtSure.setText("入库");
        mTitelTxt.setText("入库");

        // 保存默认的KeyListener以便恢复
        storeEditWeightListener = mWeightEdit.getKeyListener();

        if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
            ToSpeek(R.raw.cangkudibang);
        }

        init433();
        // false:只支持地磅 true：支持手动输入支持地磅
        if (!AppConfig.getInstance().handInput) {
            // 设置KeyListener为null, 变为不可输入状态
           // mWeightEdit.setKeyListener(null);
          // mWeightEdit.setClickable(false);
        } else {
            Utils.setPricePoint1(mWeightEdit);
        }
    }

    private final int READ_433_GRAP = 500;

    private void init433() {
        mSerialPort_433 = new SerialPort();
        try {
            mSerialPort_433.OpenSerial(SERIAL_PORT_433, BRD_433);
            fd_433 = mSerialPort_433.getFd();
            logger.d("--onCreate--open-serial=" + fd_433);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "无串口权限,强制退出！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
            // System.exit(0);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Toast.makeText(this, "未找到串口,强制退出！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
            // System.exit(0);
        }
        try {
            mDeviceControl_433 = new DeviceControl(PATH_433, this);
        } catch (IOException e) {
            e.printStackTrace();
            mDeviceControl_433 = null;
            return;
        }
        mDeviceControl_433.MTGpioOn();
        if (read433timer == null) {
            read433timer = new Timer();
        }

        read433timer.schedule(new Read433Task(), 50, READ_433_GRAP);
    }

    private class Read433Task extends TimerTask {
        int ng = 0;
        int ok = 0;
        int oldOk = 0;

        @Override
        public void run() {
            try {
                logger.d("--433--read--task--start---");
                sendReceDataCmd(
                        getPreferencesString(BaseActivity.TONGDAO, "1"),
                        SendTypeRece);
                byte[] temp1 = mSerialPort_433.ReadSerial(fd_433, 80);
                if (temp1 != null) {
                    ++ok;
                    if (ok > 100) {
                        ok = 0;
                    }
                    // logger.d("--433--read--ok---");
                    logger.d("--433--read--ok---" + temp1.length);
                    Message msg2 = new Message();
                    msg2.what = 1;
                    msg2.obj = temp1;
                    handler.sendMessage(msg2);
                } else {
                    ++ng;
                    if (ng == 4) {
                        oldOk = ok;
                    }
                    if (ng == 5 && oldOk == ok) {
                        ok = 0;
                        ng = 0;
                        Message msg3 = new Message();
                        msg3.what = 2;
                        handler.sendMessage(msg3);
                    }
                    logger.d("--433--read--nodata---");
                }
                logger.d("--433--read--task--end---");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private Handler handler = new Handler() {
        // 4~9 10~29
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                byte[] data = (byte[]) msg.obj;
                List<byte[]> listbyte = DataConversionUtils.getListData(
                        (byte) 0x21, (byte) 34, data, logger);
                if (listbyte.size() > 0) {
                    logger.d("handler getdata.size=" + listbyte.size());
                    for (int i = 0; i < listbyte.size(); i++) {
                        byte[] item = listbyte.get(i);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id",
                                DataConversionUtils.byteArrayToInt(new byte[]{
                                        item[4], item[5], item[6], item[7],
                                        item[8]})
                                        + "");
                        map.put("channle_id",
                                DataConversionUtils
                                        .byteArrayToInt(new byte[]{item[9]})
                                        + "");
                        map.put("time", DataConversionUtils
                                .getCurrentTime("HH:mm:ss:SSS"));
                        byte[] rece_data = new byte[20];
                        for (int j = 0; j < 20; j++) {
                            rece_data[j] = item[j + 10];
                        }
                        setWeight(DataConversionUtils
                                .byteArrayToAscii(rece_data) + "");
                    }
                    logger.d("handler---message--");
                } else {
                    logger.d("解析数据list.size小于0");
                }
            } else if (msg.what == 2) {
                if (!hpChangeable) {
                    //自动获取地磅数据，如果是手动输入，则不需要清空
                    if (!AppConfig.getInstance().handInput)
                        mWeightEdit.setText("");
                }
            }
        }
    };

    private void sendReceDataCmd(String transmissionId, int type) {
        // aa 0a 55 00 01 00 00 00 00 00 00 01 00 bb
        // aa0a55 0001 0000 0000 0000 60bb
        int id = 1;
        // String string = edvTransmission.getText().toString();
        if (!"".equals(transmissionId)) {
            try {
                id = Integer.parseInt(transmissionId);
            } catch (NumberFormatException e) {
                Toast.makeText(ShepinChenZhong_Activity.this, "ID超出范围！",
                        Toast.LENGTH_SHORT).show();
                return;
            }

        } else {
            id = 01;
        }
        if (id >= 268435454) {
            Toast.makeText(ShepinChenZhong_Activity.this, "ID超出范围！",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] cmd = new byte[13];
        cmd[0] = (byte) 0xaa;
        cmd[1] = 0x0a;
        cmd[2] = 0x55;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) 0x01;
        cmd[5] = 0x00;
        cmd[6] = 0x00;
        cmd[7] = 0x00;
        cmd[8] = 0x00;
        cmd[9] = 0x00;
        cmd[10] = (byte) id;
        cmd[11] = (byte) CRC.crc(cmd, 1);// 校验
        cmd[12] = (byte) 0xbb;
        mSerialPort_433.WriteSerialByte(fd_433, cmd);
        receType = type;
        // mSerialPort_433.WriteSerialByte(fd_433,cmd_search);
    }

    // 解析重量
    private String parseWeight(String weightOld) {
        String weight = "";
        if (weightOld.contains("=")) {

            int index = weightOld.lastIndexOf("=");
            weight = weightOld.substring(index + 1, index + 8);

            if ("0000000".equals(weight) || "00.0000".equals(weight)
                    || "0.00000".equals(weight)) {
                weight = "0";
            } else {
                // 获得每一位
                char[] c = weight.toCharArray();

                String[] wei = new String[c.length];
                int j = 0;
                for (int i = c.length - 1; i >= 0; i--) {
                    wei[j] = String.valueOf(c[i]);
                    j++;
                }
                String msg = "";
                // 长度肯定是6
                for (int m = 0; m < wei.length; m++) {
                    msg += wei[m];
                }
                // 转换成数字类型
                try {
                    Float f = Float.parseFloat(msg);
                    weight = String.valueOf(f);
                } catch (Exception e) {
                    return weight;
                }
            }
            return weight;
        } else {
            return weight;
        }
    }

}
