package com.xuyulong.Store;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xuyulong.util.SpeekBaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.setting_activity)
public class Setting_Activity extends SpeekBaseActivity {
    static public final String YUYING = "YUYING";
    static public final String URL = "URL";
    static public final String DIBANG = "DIBANG";
    static public final String DUKA = "DUKA";
    @ViewById
    TextView tv_userName;
    @ViewById
    ImageView imgSetting;
    @ViewById
    CheckBox dibang_433;
    @ViewById
    CheckBox dibang_ble;
    @ViewById
    CheckBox duka_uhf;
    @ViewById
    CheckBox duka_nfc;

    @Click
    void mTitleBackClicked() {
        finish();
    }

    @ViewById
    TextView mUrl;
    @ViewById
    CheckBox mYuYing;

    @CheckedChange({R.id.mYuYing})
    void checkedChangeOnHelloCheckBox(CompoundButton hello, boolean isChecked) {
        PutPreferences(YUYING, isChecked);
        if (isChecked) {
            Speek(R.raw.kaiqiyuyin);
        }
    }


    @AfterViews
    void init() {
        imgSetting.setImageResource(R.drawable.btn_setting_w);
        mYuYing.setChecked(getPreferencesBoolean(YUYING, true));
        mUrl.setText(AppConfig.getInstance().serviceIp);
        tv_userName.setVisibility(View.GONE);
        dibang_433.setOnCheckedChangeListener(cb);
        dibang_ble.setOnCheckedChangeListener(cb);
        duka_nfc.setOnCheckedChangeListener(duKacb);
        duka_uhf.setOnCheckedChangeListener(duKacb);
//        dibang_ble.setChecked(true);
//        duka_nfc.setChecked(true);
        if (getPreferencesString(DIBANG, "").equals("433地磅")) {
            dibang_433.setChecked(true);
            PutPreferences(DIBANG, "433地磅");
        } else if (getPreferencesString(DIBANG, "蓝牙地磅").equals("蓝牙地磅")) {
            PutPreferences(DIBANG, "蓝牙地磅");
            dibang_ble.setChecked(true);
        }
        if (getPreferencesString(DUKA, "").equals("超高频读卡")) {
            duka_uhf.setChecked(true);
            PutPreferences(DUKA, "超高频读卡");
        } else if (getPreferencesString(DUKA, "NFC读卡").equals("NFC读卡")) {
            duka_nfc.setChecked(true);
            PutPreferences(DUKA, "NFC读卡");
        }
    }

    private CompoundButton.OnCheckedChangeListener cb = new CompoundButton.OnCheckedChangeListener() { //实例化一个cb
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView.getText().toString().equals("433地磅")) {
                    dibang_ble.setChecked(false);
                    PutPreferences(DIBANG, "433地磅");
                } else {
                    dibang_433.setChecked(false);
                    PutPreferences(DIBANG, "蓝牙地磅");
                }
                Toast toast = Toast.makeText(Setting_Activity.this, "您选中了" + buttonView.getText().toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener duKacb = new CompoundButton.OnCheckedChangeListener() { //实例化一个cb
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView.getText().toString().equals("超高频读卡")) {
                    duka_nfc.setChecked(false);
                    PutPreferences(DUKA, "超高频读卡");
                } else {
                    duka_uhf.setChecked(false);
                    PutPreferences(DUKA, "NFC读卡");
                }
                Toast toast = Toast.makeText(Setting_Activity.this, "您选中了" + buttonView.getText().toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    };
}
