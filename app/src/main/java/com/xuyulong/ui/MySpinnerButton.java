package com.xuyulong.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rscja.deviceapi.Barcode2D;
import com.rscja.deviceapi.exception.ConfigurationException;
import com.rscja.utility.StringUtility;
import com.scandecode.inf.ScanInterface;
import com.xuyulong.Store.AppConfig;
import com.xuyulong.Store.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SuppressLint("AppCompatCustomView")
public class MySpinnerButton extends ImageButton {
    private Context context;
    public static final String SCAN_WAY_CAMERA = "照相机";
    public static final String SCAN_WAY_SCAN_HEAD = "扫描头";
    public static final String INIT_FAIL = "端口初始化失败，请确保设备为手持机";

    public static final int SCAN_MODE_CAMERA = 0;
    public static final int SCAN_MODE_SCAN_HEAD = 1;

    private String labelCode;

    // 成为手机
    private boolean threadStop = true;
    private ExecutorService executor = null;

    // 思必拓
    private static final int STATE_IDLE = 0;
    private static final int STATE_DECODE = 1;
    private int state = STATE_IDLE;
    // 肯麦思
    private static final int ENABLE = 1;
    private static final int DISENABLE = 0;
    private boolean startScanFlg = false;

    /**
     * 为按钮提供一个回调接口 通用
     */
    private GetQRListener getQRListener;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == SCAN_MODE_SCAN_HEAD) {

                labelCode = msg.getData().getString("labelCode");
                System.out.println("Handler------------------------>labelCode:"
                        + labelCode);
                getQRListener.getQRHappend(labelCode);
            }
        }

        ;
    };

    public MySpinnerButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        // 设置监听事件
        this.setOnClickListener(new MySpinnerButtonOnClickListener());

        System.out.println("MySpinnerButton初始化1");
    }

    public MySpinnerButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        // 设置监听事件
        this.setOnClickListener(new MySpinnerButtonOnClickListener());

        System.out.println("MySpinnerButton初始化2");
    }

    public MySpinnerButton(Context context) {
        super(context);
        this.context = context;
        // 设置监听事件
        this.setOnClickListener(new MySpinnerButtonOnClickListener());

        System.out.println("MySpinnerButton初始化3");
    }

    /**
     * MySpinnerButton的点击事件
     */
    class MySpinnerButtonOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            System.out.println("MySpinnerButton被点击了");
            final MySpinnerDropDownItems mSpinnerDropDrownItems = new MySpinnerDropDownItems(
                    context);
            if (!mSpinnerDropDrownItems.isShowing()) {
                mSpinnerDropDrownItems.showAsDropDown(MySpinnerButton.this);
            }
        }
    }

    /**
     * MySpinnerButton的下拉列表
     */
    class MySpinnerDropDownItems extends PopupWindow {

        private Context context;
        private LinearLayout mLayout; // 下拉列表的布局
        private ListView mListView; // 下拉列表控件
        private ArrayList<HashMap<String, String>> mData;

        public MySpinnerDropDownItems(Context context) {
            super(context);

            this.context = context;
            // 下拉列表的布局
            mLayout = new LinearLayout(context);
            mLayout.setOrientation(LinearLayout.VERTICAL);
            // 下拉列表控件
            mListView = new ListView(context);
            mListView.setLayoutParams(new LayoutParams(120,
                    LayoutParams.WRAP_CONTENT));
            mListView.setCacheColorHint(Color.TRANSPARENT);
            mData = new ArrayList<HashMap<String, String>>();

            // 扫描方式
            String[] scanWay = {SCAN_WAY_CAMERA, SCAN_WAY_SCAN_HEAD};

            for (int i = 0; i < scanWay.length; i++) {
                HashMap<String, String> mHashmap = new HashMap<String, String>();
                mHashmap.put("spinner_dropdown_item_textview", scanWay[i]);
                mData.add(mHashmap);
            }
            // 为listView设置适配器
            mListView.setAdapter(new MyAdapter(context, mData,
                    R.layout.spinner_dropdown_item,
                    new String[]{"spinner_dropdown_item_textview"},
                    new int[]{R.id.spinner_dropdown_item_textview}));
            // 设置listView的点击事件
            mListView
                    .setOnItemClickListener(new MySpinnerButtonListViewOnItemClickedListener());
            // 把下拉列表添加到layout中。
            mLayout.addView(mListView);

            setWidth(LayoutParams.WRAP_CONTENT);
            setHeight(LayoutParams.WRAP_CONTENT);
            setContentView(mLayout);
            setFocusable(true);

            mLayout.setFocusableInTouchMode(true);
        }

        /**
         * 我的适配器
         */
        public class MyAdapter extends BaseAdapter {

            private Context context;
            private List<? extends Map<String, ?>> mData;
            private int mResource;
            private String[] mFrom;
            private int[] mTo;
            private LayoutInflater mLayoutInflater;

            /**
             * 我的适配器的构造方法
             *
             * @param context  调用方的上下文
             * @param data     数据
             * @param resource
             * @param from
             * @param to
             */
            public MyAdapter(Context context,
                             List<? extends Map<String, ?>> data, int resource,
                             String[] from, int[] to) {

                this.context = context;
                this.mData = data;
                this.mResource = resource;
                this.mFrom = from;
                this.mTo = to;
                this.mLayoutInflater = (LayoutInflater) context
                        .getSystemService(context.LAYOUT_INFLATER_SERVICE);
            }

            /**
             * 系统在绘制ListView之前，将会先调用getCount方法来获取Item的个数
             */
            public int getCount() {

                return this.mData.size();
            }

            public Object getItem(int position) {

                return this.mData.get(position);
            }

            public long getItemId(int position) {

                return position;
            }

            /**
             * 每绘制一个 Item就会调用一次getView方法，
             * 在此方法内就可以引用事先定义好的xml来确定显示的效果并返回一个View对象作为一个Item显示出来。 也
             * 正是在这个过程中完成了适配器的主要转换功能，把数据和资源以开发者想要的效果显示出来。
             * 也正是getView的重复调用，使得ListView的使用更 为简单和灵活。
             * 这两个方法是自定ListView显示效果中最为重要的，同时只要重写好了就两个方法，ListView就能完全按开发者的要求显示。 而
             * getItem和getItemId方法将会在调用ListView的响应方法的时候被调用到。
             * 所以要保证ListView的各个方法有效的话，这两个方法也得重写。
             */
            public View getView(int position, View contentView, ViewGroup parent) {

                contentView = this.mLayoutInflater.inflate(this.mResource,
                        parent, false);

                // 设置contentView的内容和样式，这里重点是设置contentView中文字的大小
                for (int index = 0; index < this.mTo.length; index++) {
                    TextView textView = (TextView) contentView
                            .findViewById(this.mTo[index]);
                    textView.setText(this.mData.get(position)
                            .get(this.mFrom[index]).toString());
                }

                return contentView;
            }
        }

        /**
         * listView的点击事件
         */
        class MySpinnerButtonListViewOnItemClickedListener implements
                AdapterView.OnItemClickListener {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                TextView mTextView = (TextView) view
                        .findViewById(R.id.spinner_dropdown_item_textview);
                String content = mTextView.getText().toString();
                System.out.println("点击了" + content);
                MySpinnerDropDownItems.this.dismiss();

                // 选择扫描方式：照相机
                if (content.equals(SCAN_WAY_CAMERA)) {
                    getQRListener.getQRHappend(content);
                }
                // 选择扫描方式：扫描枪
                else if (content.equals(SCAN_WAY_SCAN_HEAD)) {
                    // 如果是山寨机
                    if (AppConfig.getInstance().handPhone == 0) {
                        getQRListener.getQRHappend(content);
                    }
                    // 如果是成为手持机
                    else if (AppConfig.getInstance().handPhone == 1) {

                        // 初始化成功，说明有蓝牙模块
                        if (AppConfig.getInstance().initFlag) {
                            scanChenWei();
                        }
                        // 初始化失败，说明没有蓝牙模块
                        else {
                            getQRListener.getQRHappend(INIT_FAIL);
                        }
                    }
                    // 如果是思必拓手持机
                    else if (AppConfig.getInstance().handPhone == 2) {

                        // 初始化成功，说明有蓝牙模块
                        if (AppConfig.getInstance().initFlag) {
                            scanSibituo();
                        }
                        // 初始化失败，说明没有蓝牙模块
                        else {
                            getQRListener.getQRHappend(INIT_FAIL);
                        }
                    }
                    // 如果是肯麦思手持机
                    else if (AppConfig.getInstance().handPhone == 3) {
                        scanKenMaiSi();
                    }
                }
            }
        }
    }


    // /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 肯麦思手持机 扫描条码的线程（二维码&条形码）
     *
     * @author Jiayu
     */
    private void scanKenMaiSi() {
        if (!startScanFlg) {
            startScanFlg = true;
            Intent startIntent = new Intent(
                    "android.intent.action.SCANNER_BUTTON_DOWN", null);
            context.sendOrderedBroadcast(startIntent, null);
        } else {
            startScanFlg = false;
            Intent endIntent = new Intent(
                    "android.intent.action.SCANNER_BUTTON_UP", null);
            context.sendOrderedBroadcast(endIntent, null);
        }

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i("whw", "getAction=" + intent.getAction().toString());
            if (intent.getAction().equals(
                    "com.android.server.scannerservice.broadcast")) {
                String barcode = "";
                barcode = intent.getExtras().getString("scannerdata");
                System.out.println("二维码是：" + barcode);
                getQRListener.getQRHappend(barcode);
                startScanFlg = false;
            }
        }
    };
    // /////////////////////////////////////////////////////////////////////////////////////

    /**
     * 成为手持机 扫描条码的线程（二维码&条形码）
     *
     * @author Jiayu
     */
    private void scanChenWei() {
        if (threadStop) {
            if (!executor.isShutdown()) {
                executor.execute(new GetBarcode(mHandler));
            }
        } else {
            threadStop = true;
        }
    }

    /**
     * 成为手持机 扫描二维码
     */
    public class GetBarcode implements Runnable {
        String labelCode = "";
        Message msg = null;
        Handler handler = null;

        public GetBarcode(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            // 设置标志位
            threadStop = false;
            // 如果线程是停止状态则开始扫描
            while (!threadStop && StringUtility.isEmpty(labelCode)) {
                labelCode = AppConfig.getInstance().barcode2DInstance.scan();
                System.out.println("二维码是：" + labelCode);
            }

            System.out.println("二维码是：" + labelCode);
            // 设置标志位
            threadStop = true;
            if (!StringUtility.isEmpty(labelCode)) {
                msg = handler
                        .obtainMessage(MySpinnerButton.SCAN_MODE_SCAN_HEAD);
                Bundle bundle = new Bundle();
                bundle.putString("labelCode", labelCode);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }
    }

    /**
     * 成为手机 设备上电异步类
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            return AppConfig.getInstance().barcode2DInstance.open();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            AppConfig.getInstance().initFlag = result;
        }
    }

    // /////////////////////////////////////////////////////////////////////////////////////////

    public void setGetQRListener(GetQRListener listener) {
        getQRListener = listener;
    }

    // 回调接口
    public interface GetQRListener {
        public void getQRHappend(String content);
    }

    public void creat() {
        // 如果是成为手持机
        if (AppConfig.getInstance().handPhone == 1) {
            try {
                AppConfig.getInstance().barcode2DInstance = Barcode2D
                        .getInstance();
                AppConfig.getInstance().initFlag = true;
            } catch (ConfigurationException e) {
                AppConfig.getInstance().initFlag = false;
            }
        }
        // 如果是思必拓手持机
        else if (AppConfig.getInstance().handPhone == 2) {

            if (AppConfig.getInstance().initFlag) {
                AppConfig.getInstance().bcr.getBarCode(new ScanInterface.OnScanListener() {
                    @Override
                    public void getBarcode(String s) {
                        if (!s.isEmpty()) {
                            state = STATE_IDLE;
                            labelCode = s;
                            getQRListener.getQRHappend(labelCode);
                        }else {

                        }
                    }

                    @Override
                    public void getBarcodeByte(byte[] bytes) {

                    }
                });
//                doSetParam(22, 2);
//                doSetParam(23, 40);
//                doSetParam(BarCodeReader.ParamNum.QR_INVERSE,
//                        BarCodeReader.ParamVal.INVERSE_AUTOD);
//                doSetParam(BarCodeReader.ParamNum.DATAMATRIX_INVERSE,
//                        BarCodeReader.ParamVal.INVERSE_AUTOD);
            }
        }
        // 如果是肯麦思手持机
        else if (AppConfig.getInstance().handPhone == 3) {
            IntentFilter filter = new IntentFilter("com.android.server.scannerservice.broadcast");
            context.registerReceiver(receiver, filter);

            Intent intent = new Intent(
                    "com.android.server.scannerservice.onoff");
            intent.putExtra("scanneronoff", ENABLE);
            context.sendBroadcast(intent);
        }
    }

    public void resume() {
        // 如果是成为手持机
        if (AppConfig.getInstance().handPhone == 1) {
            if (AppConfig.getInstance().initFlag
                    && AppConfig.getInstance().barcode2DInstance != null) {
                executor = Executors.newFixedThreadPool(6);
                new InitTask().execute();
            }
        }
    }

    public void pause() {
        // 如果是成为手持机
        if (AppConfig.getInstance().handPhone == 1) {
            threadStop = true;
            if (executor != null) {
                executor.shutdownNow();
            }
        }
        // 如果是思必拓手持机
        else if (AppConfig.getInstance().handPhone == 2) {
            if (AppConfig.getInstance().initFlag) {
                AppConfig.getInstance().bcr.stopScan();
                state = STATE_IDLE;
            }
        }
    }

    public void destory() {
        // 如果是成为手持机
        if (AppConfig.getInstance().handPhone == 1) {
            if (AppConfig.getInstance().initFlag
                    && AppConfig.getInstance().barcode2DInstance != null) {
                AppConfig.getInstance().barcode2DInstance.close();
                AppConfig.getInstance().barcode2DInstance = null;
                AppConfig.getInstance().initFlag = false;
            }
        }
        // 如果是肯麦思手持机
        else if (AppConfig.getInstance().handPhone == 3) {
            Intent intent = new Intent(
                    "com.android.server.scannerservice.onoff");
            intent.putExtra("scanneronoff", DISENABLE);
            context.sendBroadcast(intent);

            if (null != receiver) {
                try {
                    context.unregisterReceiver(receiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void keyDown(int keyCode) {
        // 如果是成为手持机
        if (AppConfig.getInstance().handPhone == 1) {
            if (keyCode == 136 || keyCode == 139) {
                if (AppConfig.getInstance().initFlag) {
                    scanChenWei();
                } else {
                    getQRListener.getQRHappend(INIT_FAIL);
                }
            }
        }
        // 如果是思必拓手持机
        else if (AppConfig.getInstance().handPhone == 2) {
            if (keyCode == 135) {
                if (AppConfig.getInstance().initFlag) {
                    scanSibituo();
                } else {
                    getQRListener.getQRHappend(INIT_FAIL);
                }
            }
        }
    }

    // ----------------------------------------
    // 思必拓手持机

    public void scanSibituo() {
        if (state == STATE_IDLE) {
//            state = STATE_DECODE;
            AppConfig.getInstance().bcr.starScan();
        } else {
            AppConfig.getInstance().bcr.stopScan();
            state = STATE_IDLE;
        }
    }

//    @Override
//    public void onDecodeComplete(int symbology, int length, byte[] data,
//                                 BarCodeReader reader) {
//        if (length > 0) {
//            AppConfig.getInstance().bcr.getBarCode();
//            state = STATE_IDLE;
//
//            if (symbology == 0x99) // type 99?
//            {
//                symbology = data[0];
//                int n = data[1];
//                int s = 2;
//                int d = 0;
//                int len = 0;
//                byte d99[] = new byte[data.length];
//                for (int i = 0; i < n; ++i) {
//                    s += 2;
//                    len = data[s++];
//                    System.arraycopy(data, s, d99, d, len);
//                    s += len;
//                    d += len;
//                }
//                d99[d] = 0;
//                data = d99;
//            }
//
//            labelCode = new String(data, 0, length);
//            getQRListener.getQRHappend(labelCode);
//        } else // no-decode
//        {
//            switch (length) {
//                case BarCodeReader.DECODE_STATUS_TIMEOUT:
//                    state = STATE_IDLE;
//                    break;
//
//                case BarCodeReader.DECODE_STATUS_CANCELED:
//                    state = STATE_IDLE;
//                    break;
//
//                case BarCodeReader.DECODE_STATUS_ERROR:
//                    state = STATE_IDLE;
//                default:
//                    break;
//            }
//        }
//
//    }

//    @Override
//    public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
//        // TODO Auto-generated method stub
//
//    }

    // ----------------------------------------
    // set param
//    private int doSetParam(int num, int val) {
//        String s = "";
//        int ret = AppConfig.getInstance().bcr.setParameter(num, val);
//        if (ret != BarCodeReader.BCR_ERROR) {
//            if (num == BarCodeReader.ParamNum.PRIM_TRIG_MODE) {
//                trigMode = val;
//                if (val == BarCodeReader.ParamVal.HANDSFREE) {
//                    s = "HandsFree";
//                } else if (val == BarCodeReader.ParamVal.AUTO_AIM) {
//                    s = "AutoAim";
//                    ret = AppConfig.getInstance().bcr
//                            .startHandsFreeDecode(BarCodeReader.ParamVal.AUTO_AIM);
//                    if (ret != BarCodeReader.BCR_SUCCESS) {
//                    }
//                } else if (val == BarCodeReader.ParamVal.LEVEL) {
//                    s = "Level";
//                }
//            }
//        } else
//            s = " FAILED (" + ret + ")";
//
//        return ret;
//    }

}
