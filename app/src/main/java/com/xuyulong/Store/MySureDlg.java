package com.xuyulong.Store;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MySureDlg extends Dialog {

	private TextView mTotal;
	private TextView mDriverInfo;
	private TextView mWeight;
	private TextView mCangku;
	private TextView mTitelTxt;
	private LinearLayout lyDriverInfo;
	private LinearLayout lyWeight;
	private LinearLayout lyCangKu;
	private String mTotalString;
	private String mDriverInfoString;
	private String mWeightString;
	private String mCangkuString;
	private String mTitelTxtString;
	private OnCustomDialogListener mL = null;

	public interface OnCustomDialogListener {
		public void Sure();
	}

	public MySureDlg(Context context, String s0, String s1, String s2,
			String s3, String s4, OnCustomDialogListener li) {
		super(context);
		mTitelTxtString = s0;
		mTotalString = s1;
		mDriverInfoString = s2;
		mWeightString = s3;
		mL = li;
		mCangkuString = s4;
	}

	private void initview() {
		mTotal = (TextView) findViewById(R.id.mTotal);
		mDriverInfo = (TextView) findViewById(R.id.mDriverInfo);
		mWeight = (TextView) findViewById(R.id.mWeight);
		mCangku = (TextView) findViewById(R.id.mCangku);
		mTitelTxt = (TextView) findViewById(R.id.mTitelTxt);
		lyDriverInfo = (LinearLayout) findViewById(R.id.lyDriverInfo);
		lyWeight = (LinearLayout) findViewById(R.id.lyWeight);
		lyCangKu = (LinearLayout) findViewById(R.id.lyCangKu);
		if (mDriverInfoString.equals(""))
			lyDriverInfo.setVisibility(View.GONE);
		if (mWeightString.equals(""))
			lyWeight.setVisibility(View.GONE);
		if (mCangkuString.equals(""))
			lyCangKu.setVisibility(View.GONE);
		mTotal.setText(mTotalString);
		mDriverInfo.setText(mDriverInfoString);
		mWeight.setText(mWeightString);
		mTitelTxt.setText(mTitelTxtString);
		mCangku.setText(mCangkuString);
		findViewById(R.id.btSure).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mL != null)
							mL.Sure();
						MySureDlg.this.dismiss();
					}
				});

		findViewById(R.id.btCancle).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MySureDlg.this.dismiss();
					}
				});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sure_pop);
		initview();

	}

}
