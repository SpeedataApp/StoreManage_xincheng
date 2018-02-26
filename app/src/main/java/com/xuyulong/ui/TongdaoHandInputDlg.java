package com.xuyulong.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.xuyulong.Store.R;

public class TongdaoHandInputDlg extends Dialog {

	private tongdaoSureListener listener = null;
	private EditText handInputLabel;
	private Context context;
	private InputMethodManager imm;
	private String tongdao;

	public interface tongdaoSureListener {
		public void tongdaoSure(String tongdao);
	}

	public TongdaoHandInputDlg(Context context, String tongdao,
			tongdaoSureListener listener) {
		super(context);
		this.context = context;
		this.listener = listener;
		this.tongdao = tongdao;
		this.imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.hand_input_sure_pop2);
		initview();
	}

	private void initview() {
		handInputLabel = (EditText) findViewById(R.id.handInputLabel);
		handInputLabel.setText(tongdao);

		findViewById(R.id.btSure).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (listener != null) {
							String tongdao = handInputLabel.getText()
									.toString();
							if (tongdao != null && !"".equals(tongdao)) {
								listener.tongdaoSure(tongdao);
								imm.hideSoftInputFromWindow(
										handInputLabel.getWindowToken(), 0);
								TongdaoHandInputDlg.this.dismiss();
							} else {
								Toast toast = Toast.makeText(context, "请输入！",
										Toast.LENGTH_SHORT);
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
						TongdaoHandInputDlg.this.dismiss();
					}
				});
	}

}
