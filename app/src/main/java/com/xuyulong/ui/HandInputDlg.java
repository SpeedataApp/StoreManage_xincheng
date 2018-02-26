package com.xuyulong.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xuyulong.Store.AppConfig;
import com.xuyulong.Store.R;

public class HandInputDlg extends Dialog {

	private handInputSureListener listener = null;
	private EditText handInputLabel;
	private Context context;
	private InputMethodManager imm;
	private TextView mTitelTxt;
	private TextView tip_tv;
	private String title = "";
	private String tip = "";
	private boolean setIpflag = false;
	private LinearLayout ipLinearLayout;

	public interface handInputSureListener {
		public void handInputSure(String handInputLabelText);
	}

	public HandInputDlg(Context context, handInputSureListener listener) {
		super(context);
		this.context = context;
		this.listener = listener;
		this.imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	public HandInputDlg(Context context, handInputSureListener listener,
			String title, String tip) {
		super(context);
		this.context = context;
		this.listener = listener;
		this.imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		this.title = title;
		this.tip = tip;
		setIpflag = true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hand_input_sure_pop);
		initview();
	}

	private void initview() {
		handInputLabel = (EditText) findViewById(R.id.handInputLabel);
		mTitelTxt = (TextView) findViewById(R.id.mTitelTxt);
		tip_tv = (TextView) findViewById(R.id.tip_tv);

		if (setIpflag) {
			ipLinearLayout = (LinearLayout) findViewById(R.id.ipLinearLayout);
			mTitelTxt.setText(title);
			tip_tv.setText(tip);
			handInputLabel.setText(AppConfig.getInstance().serviceIp);
			handInputLabel.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			ipLinearLayout.setVisibility(View.VISIBLE);
		}

		findViewById(R.id.btSure).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (listener != null) {
							String handInputLabelText = handInputLabel
									.getText().toString();
							if (handInputLabelText != null
									&& !"".equals(handInputLabelText)) {
								listener.handInputSure(handInputLabelText);
								imm.hideSoftInputFromWindow(
										handInputLabel.getWindowToken(), 0);
								HandInputDlg.this.dismiss();
							} else {
								Toast toast = Toast.makeText(context, "请输入！",
										3000);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();
							}
						}
					}
				});

		findViewById(R.id.btCancle).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						imm.hideSoftInputFromWindow(
								handInputLabel.getWindowToken(), 0);
						HandInputDlg.this.dismiss();
					}
				});
	}

}
