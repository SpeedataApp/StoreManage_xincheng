package com.wenyankeji.appupdate;

import com.wenyankeji.appupdate.DownloadService.DownloadBinder;
import com.xuyulong.Store.AppConfig;
import com.xuyulong.Store.ChenZhong_Activity;
import com.xuyulong.Store.R;
import com.xuyulong.ui.MyProgressBar;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.Until;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotificationUpdateActivity extends Activity {
	private Button btn_cancel;
	private TextView tv_progress;
	private DownloadBinder binder;
	private boolean isBinded;
	private MyProgressBar mProgressBar;
	private boolean isDestroy = true;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update);
		btn_cancel = (Button) findViewById(R.id.cancel);
		tv_progress = (TextView) findViewById(R.id.currentPos);
		mProgressBar = (MyProgressBar) findViewById(R.id.progressbar1);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				binder.cancel();
				binder.cancelNotification();
				finish();
			}
		});
	}

	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBinded = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (DownloadBinder) service;
			System.out.println("服务启动!!!");
			// 开始下载
			isBinded = true;
			binder.addCallback(callback);
			binder.start();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		if (isDestroy && AppConfig.getInstance().isDownload) {
			Intent it = new Intent(NotificationUpdateActivity.this,
					DownloadService.class);
			startService(it);
			bindService(it, conn, Context.BIND_AUTO_CREATE);
		}
		System.out.println(" notification  onresume");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (isDestroy && AppConfig.getInstance().isDownload) {
			Intent it = new Intent(NotificationUpdateActivity.this,
					DownloadService.class);
			startService(it);
			bindService(it, conn, Context.BIND_AUTO_CREATE);
		}
		System.out.println(" notification  onNewIntent");
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println(" notification  onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		isDestroy = false;
		System.out.println(" notification  onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isBinded) {
			System.out.println(" onDestroy   unbindservice");
			unbindService(conn);
		}
		if (binder != null && binder.isCanceled()) {
			System.out.println(" onDestroy  stopservice");
			Intent it = new Intent(this, DownloadService.class);
			stopService(it);
		}
	}

	private ICallbackResult callback = new ICallbackResult() {

		@Override
		public void OnBackResult(Object result) {
			if ("finish".equals(result)) {
				
				ExitThread exitThread = new ExitThread();
				exitThread.start();
				
//				finish();
				return;
			}
			int i = (Integer) result;
			mProgressBar.setProgress(i);
			mHandler.sendEmptyMessage(i);
		}

	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			tv_progress.setText("当前进度 ： " + msg.what + "%");
			mProgressBar.setProgress(msg.what);
		};
	};

	public interface ICallbackResult {
		public void OnBackResult(Object result);
	}
	
	/**
	 * 退出线程
	 * @author admin
	 *
	 */
	class ExitThread extends Thread {
		public Handler mHandler;

		@Override
		public void run() {
			Looper.prepare();
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					boolean flg = msg.getData().getBoolean("flg");
					if (flg) {
						System.out.println("安装程序后退出成功！");
					} else {
						System.out.println("安装程序后退出失败！");
					}

					AppConfig.getInstance().installCancle = true;
					finish();
				}
			};

			boolean flg = exitNet();
			Message msg = mHandler.obtainMessage();
			Bundle bundle = new Bundle();
			bundle.putBoolean("flg", flg);
			msg.setData(bundle);
			mHandler.sendMessage(msg);

			Looper.loop();
		}
	}
	
	/**
	 * 退出操作
	 */
	boolean exitNet() {

		/**
		 * 关闭蓝牙连接
		 */
		ChenZhong_Activity.closeBluetoothSocket();
		if (AppConfig.getInstance().bluetoothAdapter.isEnabled()) {
			AppConfig.getInstance().bluetoothAdapter.disable();
		}
		/////////////////////////////////////////////////////////////
		String request = Until.exit_req(AppConfig.getInstance().UserId);
		
		String EXIT_URL = 
				AppConfig.getInstance().serviceIp + 
				AppConfig.getInstance().serviceIpAfter + 
				"exitLogin";
		
		String response = HttpUtils.httpPut(EXIT_URL, request);

		// 服务器连接失败
		if (HttpUtils.HTTP_PUT_FAIL.equals(response)) {
			return false;
		}
		// 服务器连接成功
		else {
			String successFlag = Until.parseResult(response);

			// 退出成功
			if ("OK".equals(successFlag)) {
				return true;
			}
			// 退出失败
			else {
				return false;
			}
		}
	}


}
