package com.xuyulong.Store;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.xuyulong.adapter.ListViewAdapter;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;

//import com.wenyankeji.thread.ScanThreadTest;

@EActivity(R.layout.chuku_activity)
public class ChuKu_Activity extends SpeekBaseActivity {
    private final int SCAN_REQUEST = 1;
    private final int CAR_REQUEST = 2;
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
    Button mEdit;// 页面下方"编辑"按钮
    @ViewById
    Button btn_total;// 页面下方"总重量"按钮
    @ViewById
    RelativeLayout mRoot;
    @ViewById
    Button mChuku;// 页面下方"下一步"按钮
    @ViewById
    TextView mTitelTxt;
    @ViewById
    MySpinnerButton mySpinnerButton;
    @ViewById
    ImageButton handInput;
    @ViewById
    TextView count_tv;

    ListViewAdapter mAdapter = null;

    String mCarInfoUp = "";
    private Handler mHandler;
    private NetRequest netRequest;
    //	private ScanThreadTest thread = null;

    @UiThread(delay = 1000)
    void ToSpeek(int str) {
        Speek(str);
    }

    void Enable(boolean enabled) {
        mEdit.setEnabled(enabled);
        mChuku.setEnabled(enabled);
        mList.setEnabled(enabled);
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

    @OnActivityResult(CAR_REQUEST)
    void onCARResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AppConfig.getInstance().chukuLabels = null;
            finish();
        }
    }

    @ItemClick
    public void mListItemClicked(HashMap<String, Object> obj) {
        showDetails(obj);
    }

    private void showDetails(HashMap<String, Object> obj) {
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

        mTitelTxt.setText("出库");

        if (AppConfig.getInstance().chukuLabels != null
                && AppConfig.getInstance().chukuLabels.size() != 0) {
            mAdapter = new ListViewAdapter(AppConfig.getInstance().chukuLabels,
                    this, false);
        } else {
            mAdapter = new ListViewAdapter(
                    new ArrayList<HashMap<String, Object>>(), this, false);
        }
        count_tv.setText(mAdapter.getCount() + "");
        mList.setAdapter(mAdapter);
        mList.setDelButtonClickListener(new DelButtonClickListener() {
            @Override
            public void clickHappend(int position) {
                mAdapter.DeleteHash(position);
                clearDetail();
                count_tv.setText(mAdapter.getCount() + "");
            }
        });

        if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
            ToSpeek(R.raw.saomiaobiaoqian);
        }

        mySpinnerButton.creat();
        // 回调接口
        mySpinnerButton.setGetQRListener(new GetQRListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void getQRHappend(String content) {
                if (MySpinnerButton.SCAN_WAY_CAMERA.equals(content)) {

                    Intent intent = new Intent(ChuKu_Activity.this,
                            CaptureActivity.class);
                    startActivityForResult(intent, SCAN_REQUEST);
                    return;
                }

                // 如果是山寨机
                if (AppConfig.getInstance().handPhone == 0) {
                    if (MySpinnerButton.SCAN_WAY_SCAN_HEAD.equals(content)) {
                        if (AppConfig.getInstance().initFlag == false) {
                            toast = Toast.makeText(ChuKu_Activity.this,
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
                        toast = Toast.makeText(ChuKu_Activity.this, content,
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
                        toast = Toast.makeText(ChuKu_Activity.this, content,
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

    @Click
    void handInputClicked() {
        new HandInputDlg(ChuKu_Activity.this, new handInputSureListener() {

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

    @SuppressLint("WrongConstant")
    @Click
    void mChukuClicked() {
        // 点击"下一步"按钮,跳转至扫描车卡界面
        if (mAdapter.getCount() == 0) {
            toast = Toast.makeText(ChuKu_Activity.this, "您还没有扫描标签！", 3000);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        netRequest = mAdapter.GetJsonResultChuku();

        // 如果是山寨机
        if (AppConfig.getInstance().handPhone == 0) {
            startActivityForResult(Rfid_Activity_.intent(ChuKu_Activity.this)
                    .mReq(netRequest).get(), CAR_REQUEST);
        }
        // 如果是成为手持机
        else if (AppConfig.getInstance().handPhone == 1) {
            startActivityForResult(
                    Rfid_ChengWei_Activity_.intent(ChuKu_Activity.this)
                            .mReq(netRequest).get(), CAR_REQUEST);
        }
        // 如果是思必拓手持机
        else if (AppConfig.getInstance().handPhone == 2) {
//            showSingleChoiceDialog("超高频", "高频");
            if (getPreferencesString(Setting_Activity.DUKA, "").equals("超高频读卡")) {
                startActivityForResult(
                        Rfid_SiBiTuo_Activity_.intent(ChuKu_Activity.this)
                                .mReq(netRequest).get(), CAR_REQUEST);
            } else if (getPreferencesString(Setting_Activity.DUKA, "").equals("NFC读卡")) {
                startActivityForResult(
                        GaoPinSpeedataAct_.intent(ChuKu_Activity.this)
                                .mReq(netRequest).get(), CAR_REQUEST);
            }else {
                showToast("请到设置选择读卡方式");
            }
        }

        // 如果是肯麦思手持机
        else if (AppConfig.getInstance().handPhone == 3) {
            startActivityForResult(
                    Rfid_KenMaiSi_Activity_.intent(ChuKu_Activity.this)
                            .mReq(netRequest).get(), CAR_REQUEST);
        }

        // new MySureDlg(this, "确认数量", mAdapter.getCount() + "", "", "", "",
        // new MySureDlg.OnCustomDialogListener() {
        // @Override
        // public void Sure() {
        // NetRequest netRequest = mAdapter.GetJsonResultChuku();
        //
        // // 如果是山寨机
        // if (AppConfig.getInstance().handPhone == 0) {
        // startActivityForResult(
        // Rfid_Activity_.intent(ChuKu_Activity.this)
        // .mReq(netRequest).get(),
        // CAR_REQUEST);
        // }
        // // 如果是成为手持机
        // else if (AppConfig.getInstance().handPhone == 1) {
        // startActivityForResult(
        // Rfid_ChengWei_Activity_
        // .intent(ChuKu_Activity.this)
        // .mReq(netRequest).get(),
        // CAR_REQUEST);
        // }
        // }
        // }).show();
    }

    int is = 0;

    public void showSingleChoiceDialog(String s1, String s2) {

        final String[] items = {s1, s2};
        final AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(this);
        singleChoiceDialog.setTitle("选择功能");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        is = which;

                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (is == 0) {
                            startActivityForResult(
                                    Rfid_SiBiTuo_Activity_.intent(ChuKu_Activity.this)
                                            .mReq(netRequest).get(), CAR_REQUEST);
                        } else if (is == 1) {
                            startActivityForResult(
                                    GaoPinSpeedataAct_.intent(ChuKu_Activity.this)
                                            .mReq(netRequest).get(), CAR_REQUEST);
                        }
                        Toast.makeText(ChuKu_Activity.this,
                                "你选择了" + items[is],
                                Toast.LENGTH_SHORT).show();
                    }
                });
        singleChoiceDialog.show();

    }

    @ViewById
    Button mCancle;

    @Click
    void mCancleClicked() {
        mAdapter.Cancle();
        mAdapter.ShowCheck(false);
        mEdit.setText("编辑");
        mCancle.setVisibility(View.INVISIBLE);
        btn_total.setVisibility(View.INVISIBLE);
    }

    @Click
    void mEditClicked() {
        if (!mAdapter.GetCheck()) {
            mAdapter.ShowCheck(true);
            mEdit.setText("删除");
            mCancle.setVisibility(View.VISIBLE);
            btn_total.setVisibility(View.VISIBLE);

        } else {
            mAdapter.ShowCheck(false);
            mEdit.setText("编辑");
            int count = mAdapter.DeleteSelect();
            if (count > 0) {
                clearDetail();
            }
            mCancle.setVisibility(View.INVISIBLE);
            btn_total.setVisibility(View.INVISIBLE);
            count_tv.setText(mAdapter.getCount() + "");
        }

    }

    /**
     * 计算总重量
     */
    @Click
    void btn_total() {
        int total = 0;
        ArrayList<HashMap<String, Object>> dataAll = mAdapter.getData();
        for (int i = 0; i < dataAll.size(); i++) {
            total += Integer.parseInt(String.valueOf(dataAll.get(i).get("weight")));
        }
        weight.setText(total + "(总)");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 如果是成为机
        if (AppConfig.getInstance().handPhone == 1
                && AppConfig.getInstance().barcode2DInstance != null) {
            try {
                AppConfig.getInstance().barcode2DInstance.close();
                AppConfig.getInstance().barcode2DInstance = null;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                AppConfig.getInstance().barcode2DInstance = Barcode2D
                        .getInstance();
                AppConfig.getInstance().initFlag = true;
            } catch (ConfigurationException e) {
                AppConfig.getInstance().initFlag = false;
            }
        }

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

        String request = Until.getLabelInfo_req(qrcode,
                AppConfig.getInstance().UserId, 3);

        String GET_LABEL_INFO_URL = AppConfig.getInstance().serviceIp
                + AppConfig.getInstance().serviceIpAfter + "scanLabel";
        String response = HttpUtils.httpPut(GET_LABEL_INFO_URL, request);

        // 服务器连接失败
        if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
            getLabelInfoErr(response);
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
                getLabelInfoErr(successFlag);
            }
        }
    }

    @UiThread
    void getLabelInfoSuccess(HashMap<String, Object> obj, String qrcode) {
        // speechSynthesizer.startSpeaking(obj.get("wasteName").toString(),
        // this);
        mQrCode.setText(qrcode);

        // 判断是否已经在列表中
        if (mAdapter.getData() != null && mAdapter.getData().size() != 0) {
            ArrayList<HashMap<String, Object>> dataAll = mAdapter.getData();
            for (int i = 0; i < dataAll.size(); i++) {
                HashMap<String, Object> dataOne = dataAll.get(i);
                if (qrcode.equals(dataOne.get("qrCode"))) {

                    obj.put("qrCode", qrcode);
                    mAdapter.replaceHash(obj, i);

                    toast = Toast.makeText(ChuKu_Activity.this,
                            "该标签已扫过！列表中该数据已经更新！", 3000);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    SetLodingHid();
                    Enable(true);
                    showDetails(obj);

                    return;
                }

                // 判断是否为不同类型的危废
                if (!(obj.get("wasteCode").equals(
                        dataAll.get(0).get("wasteCode")) && obj
                        .get("wasteName").equals(
                                dataAll.get(0).get("wasteName")))) {
                    toast = Toast.makeText(ChuKu_Activity.this, "只能扫描同类型危废！",
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
            showDetails(obj);
        }

        SetLodingHid();
        Enable(true);
        count_tv.setText(mAdapter.getCount() + "");
    }

    @SuppressLint("WrongConstant")
    @UiThread
    void getLabelInfoErr(String result) {
        toast = Toast.makeText(ChuKu_Activity.this, result, 3000);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        SetLodingHid();
        Enable(true);
    }

}
