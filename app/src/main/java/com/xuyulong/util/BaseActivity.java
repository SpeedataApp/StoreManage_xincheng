package com.xuyulong.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xuyulong.Store.R;

public class BaseActivity extends Activity {

	private final String SHARE_NAME = "WENYAN";
	private View mLoading;
	private TextView mTextShow;
	private boolean mFirst = false;
	public static final String TONGDAO = "";

	private void init() {
		LayoutInflater tmp = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLoading = tmp.inflate(R.layout.loading, null);
		mTextShow = (TextView) mLoading.findViewById(R.id.tx);
	}

	public void SetLoding(String text) {
		if (!mFirst) {
			RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			params3.addRule(RelativeLayout.CENTER_IN_PARENT);
			addContentView(mLoading, params3);
			mLoading.setVisibility(View.GONE);
			mFirst = true;
		}
		mTextShow.setText(text);
		mLoading.setVisibility(View.VISIBLE);
	}

	public void SetLodingHid() {
		mLoading.setVisibility(View.GONE);

	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		init();
		// 每创建一个活动，就加入到活动管理器中
		ActivityCollector.addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 每销毁一个活动，就从活动管理器中移除
		ActivityCollector.removeActivity(this);
	}

	public String getPreferencesString(String key, String defValue) {
		SharedPreferences settings = getSharedPreferences(SHARE_NAME,
				Activity.MODE_PRIVATE);
		return settings.getString(key, defValue);
	}

	public int getPreferencesint(String key, int defValue) {
		SharedPreferences settings = getSharedPreferences(SHARE_NAME,
				Activity.MODE_PRIVATE);
		return settings.getInt(key, defValue);
	}

	public Boolean getPreferencesBoolean(String key, Boolean defValue) {
		SharedPreferences settings = getSharedPreferences(SHARE_NAME,
				Activity.MODE_PRIVATE);
		return settings.getBoolean(key, defValue);
	}

	public void PutPreferences(String key, String value) {
		SharedPreferences settings = getSharedPreferences(SHARE_NAME,
				Activity.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void PutPreferences(String key, int value) {
		SharedPreferences settings = getSharedPreferences(SHARE_NAME,
				Activity.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void PutPreferences(String key, boolean value) {
		SharedPreferences settings = getSharedPreferences(SHARE_NAME,
				Activity.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}
