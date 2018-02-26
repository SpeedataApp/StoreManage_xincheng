package com.xuyulong.Store;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.tsz.afinal.FinalDb;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.xuyulong.adapter.ChenZhongListViewAdapter;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.SpeekBaseActivity;
import com.xuyulong.util.Until;

@EActivity(R.layout.chenzhong_activity)
public class ChenZhong_Activity extends SpeekBaseActivity {
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

    private BluetoothDevice bluetoothDevice = null; // 蓝牙设备
    private boolean isRegistedFlag = false;
    private boolean showFlag = false;
    private boolean isSameFlag = false;
    private BluetoothAdapter bluetoothAdapter;
    public static final String TIMEOUT_MSG = "连接已断开，正在重新连接";
    public static final String NOMAL_MSG = "正在连接设备,请稍等";

    private ChenZhongListViewAdapter mPairedDevicesArrayAdapter = null;
    private ChenZhongListViewAdapter mUnBondedDevicesArrayAdapter = null;
    private List<String> unBondedDevices_name = new ArrayList<String>();
    private List<String> unBondedDevices_address = new ArrayList<String>();
    private List<String> pairedDevices_name = new ArrayList<String>();
    private List<String> pairedDevices_address = new ArrayList<String>();

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


    @Click
    void mTitleBackClicked() {
        finish();
    }

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

        toast = Toast.makeText(ChenZhong_Activity.this, result,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Click
    void mBtLocalClicked() {// 点击获取仓库信息
        SetLoding("仓库信息获取中...");
        getCK();
        mBtSure.setEnabled(false);
    }

    private String mStrupload = "";

    @Click
    void mBtSureClicked() {// 入库按钮

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
                                    Toast.makeText(ChenZhong_Activity.this,
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
        toast = Toast.makeText(ChenZhong_Activity.this, "入库成功!",
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
        toast = Toast.makeText(ChenZhong_Activity.this, result,
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
        toast = Toast.makeText(ChenZhong_Activity.this, "贴标成功!",
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
        toast = Toast.makeText(ChenZhong_Activity.this, result,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        SetLodingHid();
        Enable(true);
        mTitleBack.setEnabled(true);
    }

    @Click
    void getWeightClicked() {// 射频称重按钮

        // 如果蓝牙服务还没打开
        if (bluetoothAdapter.isEnabled() == false) {
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            return;
        }

        if (!isRegistedFlag) {
            // 注册Receiver来获取蓝牙设备相关的结果
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);// 远程设备发现动作。
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);// 远程设备的键态的变化动作。
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);// 蓝牙扫描本地适配器模改变动作。
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 状态改变动作
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 查找结束
            registerReceiver(mReceiver, filter);// 注册接收
            isRegistedFlag = true;
        }

        closeBluetoothSocket();

        doDiscovery();
    }

    @UiThread(delay = 1000)
    void ToSpeek(int str) {
        Speek(str);
    }

    @AfterViews
    void Init() {
        // Until.setPricePoint(mWeightEdit);
        // 这里要做一个输入时候只保留两位小数
        // if (mMode == 0) {
        // mBtSure.setText("贴标");
        // mTitelTxt.setText("贴标");
        // cankuly.setVisibility(View.GONE);
        // weightly.setVisibility(View.VISIBLE);
        // if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
        // ToSpeek(R.raw.lianjiedibang);
        // }
        // } else if (mMode == 1) {
        // mBtSure.setText("入库");
        // mTitelTxt.setText("入库");
        // cankuly.setVisibility(View.VISIBLE);
        // weightly.setVisibility(View.GONE);
        // lvLinearLayout.setVisibility(View.GONE);
        // if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
        // ToSpeek(R.raw.xuanzecangku);
        // }
        // } else {
        // mBtSure.setText("入库");
        // mTitelTxt.setText("入库");
        // if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
        // ToSpeek(R.raw.cangkudibang);
        // }
        // }

        // 为了测试
        // mWeightEdit.setText("100");

        db = AppConfig.getInstance().db;
        Cangku cangku = db.findById(AppConfig.getInstance().UserId.toString(),
                Cangku.class);
        if (cangku != null) {
            if (cangku.isCangkuDefault()) {
                mCangKu.setText(cangku.getRepositoryName() + "\n"
                        + cangku.getSpaceName());
            }
        }

        bluetoothAdapter = AppConfig.getInstance().bluetoothAdapter;
        AppConfig.getInstance().getWeight = true;

        mBtSure.setText("入库");
        mTitelTxt.setText("入库");
        if (getPreferencesBoolean(Setting_Activity.YUYING, true)) {
            ToSpeek(R.raw.cangkudibang);
        }

        // 如果打开本地蓝牙设备不成功，提示信息，结束程序
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }

        // 直接调用函数enable()去打开蓝牙设备 （不会出现对话框要你确认啥的）
        if (bluetoothAdapter.isEnabled() == false) {
            bluetoothAdapter.enable();
            wasteTime();
        } else {
            setBondedDevices();
        }

        String mac = getPreferencesString("btAddress", "");
        compareConnect(mac, NOMAL_MSG);

        // 设置未配队设备列表
        mUnBondedDevicesArrayAdapter = new ChenZhongListViewAdapter(
                unBondedDevices_name, ChenZhong_Activity.this);

        unBondedListView.setAdapter(mUnBondedDevicesArrayAdapter);
        unBondedListView.setOnItemClickListener(mDeviceClickListener);

        // 设置已配队设备列表
        mPairedDevicesArrayAdapter = new ChenZhongListViewAdapter(
                pairedDevices_name, ChenZhong_Activity.this);

        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
    }

    @Background
    void wasteTime() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setBondedDevices();
    }

    @UiThread
    void setBondedDevices() {
        // 获得已配对的远程蓝牙设备的集合
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> it = devices.iterator(); it
                    .hasNext(); ) {
                BluetoothDevice device = (BluetoothDevice) it.next();
                // 打印出远程蓝牙设备的物理地址
                System.out.println(device.getAddress());

                if (device.getName() == null) {
                    pairedDevices_name.add(device.getAddress());
                } else {
                    pairedDevices_name.add(device.getName());
                }
                pairedDevices_address.add(device.getAddress());
            }
            mPairedDevicesArrayAdapter.notifyDataSetChanged();
        } else {
            System.out.println("还没有已配对的远程蓝牙设备！");
            pairedDevices_name.add("没有可连接设备,请先配对！");
            pairedDevices_address.add("");
            mPairedDevicesArrayAdapter.notifyDataSetChanged();
        }
    }

    // 关闭程序掉用处理部分
    @Override
    public void onDestroy() {
        super.onDestroy();

        AppConfig.getInstance().getWeight = false;
        // 关闭服务查找
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                // 准备连接设备，关闭服务查找
                bluetoothAdapter.cancelDiscovery();
            }
        }

        if (isRegistedFlag) {
            // 注销action接收器
            this.unregisterReceiver(mReceiver);
            isRegistedFlag = false;
        }

        // 关闭蓝牙连接
        // closeBluetoothSocket();

        System.out.println("onDestroy:开始执行");
    }

    /**
     * 开始服务和设备查找
     */
    private void doDiscovery() {

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            return;
        } else {
            // 清除数据
            unBondedDevices_name.clear();
            unBondedDevices_address.clear();
            pairedDevices_name.clear();
            pairedDevices_address.clear();

            // 显示其它设备（未配对设备）列表
            title_unBonded_devices.setVisibility(View.VISIBLE);
            // 显示其它设备（已配对设备）列表
            title_paired_devices.setVisibility(View.VISIBLE);
        }

        tv.setText("正在搜索,可连接已搜索到的设备");
        progressBar.setVisibility(View.VISIBLE);
        Enable(false);
        getWeight.setEnabled(true);

        bluetoothAdapter.startDiscovery();
    }

    // 设置按钮是否可用
    void Enable(boolean enabled) {
        mBtLocal.setEnabled(enabled);
        mBtSure.setEnabled(enabled);
        getWeight.setEnabled(enabled);
        // mWeightEdit.setEnabled(enabled);
    }

    // 查找到设备和搜索完成action监听器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 查找到设备action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 得到蓝牙设备
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                // 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    if (!unBondedDevices_address.contains(deviceAddress)) {

                        if (deviceName == null || "".equals(deviceName)) {
                            unBondedDevices_name.add(deviceAddress);
                        } else {
                            unBondedDevices_name.add(deviceName);
                        }
                        unBondedDevices_address.add(deviceAddress);
                    }

                } else {
                    // 添加到已配对设备列表
                    if (!pairedDevices_address.contains(deviceAddress)) {

                        if (deviceName == null || "".equals(deviceName)) {
                            pairedDevices_name.add(deviceAddress);
                        } else {
                            pairedDevices_name.add(deviceName);
                        }
                        pairedDevices_address.add(deviceAddress);
                    }
                }

                mUnBondedDevicesArrayAdapter.notifyDataSetChanged();
                mPairedDevicesArrayAdapter.notifyDataSetChanged();

                // 搜索完成action
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {

                // 未配对的情况
                if (unBondedDevices_name.size() == 0
                        || unBondedDevices_name.isEmpty()) {
                    unBondedDevices_name.add("没有找到新设备");
                    unBondedDevices_address.add("");
                }

                // 已配对的情况
                if (pairedDevices_name.size() == 0
                        || pairedDevices_name.isEmpty()) {
                    pairedDevices_name.add("没有可连接设备,请先配对！");
                    pairedDevices_address.add("");
                }

                mUnBondedDevicesArrayAdapter.notifyDataSetChanged();
                mPairedDevicesArrayAdapter.notifyDataSetChanged();

                tv.setText("请连接设备");
                progressBar.setVisibility(View.GONE);
                Enable(true);
            }
        }
    };

    // 选择设备响应函数
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @SuppressLint("NewApi")
        public void onItemClick(AdapterView<?> av, View v, int position,
                                long arg3) {

            if (bluetoothAdapter.isDiscovering()) {
                // 准备连接设备，关闭服务查找
                bluetoothAdapter.cancelDiscovery();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 得到mac地址
            TextView bluetoothDevice = (TextView) v
                    .findViewById(R.id.bluetoothDevice);
            String deviceName = bluetoothDevice.getText().toString();
            System.out.println("************按下item的name*******");
            System.out.println(deviceName);

            int devicePosition = 0;
            String mac = "";

            // 点击了未配对设备
            if (unBondedDevices_name.contains(deviceName)) {
                devicePosition = unBondedDevices_name.indexOf(deviceName);
                mac = unBondedDevices_address.get(devicePosition);
            }
            // 点击了已配对设备
            else if (pairedDevices_name.contains(deviceName)) {
                devicePosition = pairedDevices_name.indexOf(deviceName);
                mac = pairedDevices_address.get(devicePosition);
            }

            System.out.println("************address*******");
            System.out.println(mac);

            compareConnect(mac, NOMAL_MSG);
        }
    };

    /**
     * 判断连接状态 决定连接方式
     *
     * @param mac
     */
    @UiThread
    @SuppressLint("NewApi")
    void compareConnect(String mac, String msg) {
        if (!"".equals(mac)) {
            mWeightEdit.setText("");
            SetLoding("正在连接设备读取重量...");
            Enable(false);
            pairedListView.setEnabled(false);
            unBondedListView.setEnabled(false);
            mTitleBack.setEnabled(false);

            // 如果这个设备正在连接着，则直接读取数据,不再重新连接
            if (AppConfig.getInstance().bluetoothSocket != null
                    && AppConfig.getInstance().bluetoothSocket.isConnected()
                    && mac.equals(AppConfig.getInstance().bluetoothSocket
                    .getRemoteDevice().getAddress())) {

                System.out.println("这个设备正在连接着，直接读取数据,不再重新连接");
                tv.setText("已连接的设备，正在读取数据,请稍等");
                isSameFlag = true;
                getWeightFromInputstream();
            }
            // 其他情况都是先关闭连接，输入流，重写连接获取输入流
            else {
                isSameFlag = false;
                AppConfig.getInstance().showMsg = "";
                System.out.println("其他情况");
                tv.setText(msg);
                closeSocket(mac);
            }
        }
    }

    @UiThread
    void closeSocket(String mac) {
        closeBluetoothSocket();
        connect(mac);
    }

    /**
     * 连接蓝牙设备
     *
     * @param address
     */
    @Background
    void connect(String mac) {

        System.out.println("开始连接");

        // 得到蓝牙设备句柄
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);

        // 如果未配对，连接建立之前的先配对
        try {
            if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                Method creMethod = BluetoothDevice.class
                        .getMethod("createBond");

                creMethod.invoke(bluetoothDevice);
            }
        } catch (Exception e) {
            closePs("配对失败");
            e.printStackTrace();
            return;
        }

        // 用服务号得到socket
        try {
            String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
            AppConfig.getInstance().bluetoothSocket = bluetoothDevice
                    .createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {

            closePs("连接失败！createRfcommSocketToServiceRecor：" + e);
            return;
        }

        // 连接socket
        try {
            AppConfig.getInstance().bluetoothSocket.connect();
            System.out.println("连接" + bluetoothDevice.getName() + "成功！");
        } catch (IOException e) {
            System.out.println("连接失败！connect：" + e);
            closePs("连接失败，请重试!");
            closeBluetoothSocket();
            return;
        }

        showFlag = true;
        PutPreferences("btAddress", mac);
        // 成功的话打开接收线程
        getWeightFromInputstream();
    }

    @UiThread
    void closePs(String res) {
        tv.setText(res);
        SetLodingHid();
        Enable(true);
        pairedListView.setEnabled(true);
        unBondedListView.setEnabled(true);
        mTitleBack.setEnabled(true);
    }

    @Background
    void getWeightFromInputstream() {

        // 相同设备
        if (isSameFlag) {
            if (AppConfig.getInstance().timeOut) {
                timeOutShow();
            } else {
                setWeight();
            }
        }
        // 不同设备
        else {
            // 成功的话打开接收线程
            try {
                AppConfig.getInstance().inputStream = AppConfig.getInstance().bluetoothSocket
                        .getInputStream(); // 得到蓝牙数据输入流
            } catch (IOException e) {
                System.out.println("bluetoothSocket.getInputStream() 接收数据失败！"
                        + e);
                closePs("接收数据失败，请重试");
                return;
            }
            AppConfig.getInstance().readThread = new ReadThread();
            AppConfig.getInstance().readThread.start();
        }
    }

    // 接收数据线程
    class ReadThread extends Thread {

        public void run() {
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            long exitTime;

            // 接收线程
            while (!isInterrupted()) {
                try {
                    // 如果没有接收到数据 则等待
                    while (!isInterrupted()
                            && AppConfig.getInstance().inputStream.available() == 0) {
                        exitTime = System.currentTimeMillis();
                        // 超过10秒认为超时
                        while (!isInterrupted()
                                && AppConfig.getInstance().inputStream
                                .available() == 0) {
                            if ((System.currentTimeMillis() - exitTime) > 10000) {
                                AppConfig.getInstance().timeOut = true;
                            }
                        }
                    }

                    AppConfig.getInstance().timeOut = false;
                    while (!isInterrupted()) {
                        num = AppConfig.getInstance().inputStream.read(buffer); // 读入数据
                        n = 0;

                        for (i = 0; i < num; i++) {
                            if ((buffer[i] == 0x0d) && (buffer[i + 1] == 0x0a)) {
                                buffer_new[n] = 0x0a;
                                i++;
                            } else {
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new, 0, n);
                        AppConfig.getInstance().showMsg += s; // 写入接收缓存

                        if (AppConfig.getInstance().showMsg.length() > 200) {
                            System.out
                                    .println("长度超长 AppConfig.getInstance().showMsg.length() > 200");
                            AppConfig.getInstance().showMsg = AppConfig
                                    .getInstance().showMsg.substring(150,
                                            AppConfig.getInstance().showMsg.length());
                        }

                        if (AppConfig.getInstance().inputStream.available() == 0)
                            break; // 短时间没有数据才跳出进行显示
                    }

                    if (AppConfig.getInstance().getWeight && !isInterrupted()
                            && !AppConfig.getInstance().showMsg.equals("")) {
                        // 发送显示消息，进行显示刷新
                        handler.sendMessage(handler.obtainMessage());
                    }

                } catch (IOException e) {
                }
            }
        }
    }

    ;

    // 消息处理队列
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 显示数据
            if (showFlag && AppConfig.getInstance().showMsg.length() > 20) {
                showFlag = false;
                setWeight();
            }
        }
    };

    @UiThread
    void timeOutShow() {
        closePs("连接已经断开!正在重新连接");
        closeBluetoothSocket();
        compareConnect(getPreferencesString("btAddress", ""), TIMEOUT_MSG);
    }

    @UiThread
    void setWeight() {
        closePs("接收数据成功，点设备可获取最新数据");
        mWeightEdit.setText(parseWeight()); // 显示数据
    }

    // 解析重量
    private String parseWeight() {

        System.out.println("解析重量开始：showMsg：" + AppConfig.getInstance().showMsg);
        String[] weights = AppConfig.getInstance().showMsg.split("=");
        String weight = weights[weights.length - 2];
        System.out.println("解析重量开始：Weight：" + weight);

        if ("0000000".equals(weight) || "00.0000".equals(weight)) {
            weight = "0";
            System.out.println("解析重量开始：重量为0");
        } else {
            // 获得每一位
            char[] c = weight.toCharArray();

            String[] wei = new String[c.length];
            int j = 0;
            for (int i = c.length - 1; i >= 0; i--) {
                wei[j] = String.valueOf(c[i]);
                System.out.println("第" + j + "位:" + wei[j]);
                j++;
            }

            String msg = "";
            // 长度肯定是6
            for (int m = 0; m < wei.length; m++) {
                msg += wei[m];
            }
            // 转换成数字类型
            Float f = Float.parseFloat(msg);
            weight = String.valueOf(f);
        }

        System.out.println("weight:" + weight);
        return weight;
    }

    /**
     * 关闭连接socket
     */
    @SuppressLint("NewApi")
    public static void closeBluetoothSocket() {

        if (AppConfig.getInstance().readThread != null) {
            AppConfig.getInstance().readThread.interrupt();
            AppConfig.getInstance().readThread = null;
        }

        if (AppConfig.getInstance().inputStream != null) {
            try {
                AppConfig.getInstance().inputStream.close();
                AppConfig.getInstance().inputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (AppConfig.getInstance().bluetoothSocket != null) {
            try {
                System.out.println("bluetoothSocket关闭开始...");
                AppConfig.getInstance().bluetoothSocket.close();
                AppConfig.getInstance().bluetoothSocket = null;
                System.out.println("bluetoothSocket关闭成功...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
