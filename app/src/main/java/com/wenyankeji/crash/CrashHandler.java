package com.wenyankeji.crash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.xuyulong.Store.AppConfig;
import com.xuyulong.Store.ChenZhong_Activity;
import com.xuyulong.Store.Login_Activity;
import com.xuyulong.Store.R;
import com.xuyulong.util.ActivityCollector;
import com.xuyulong.util.HttpUtils;
import com.xuyulong.util.Until;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * 
 * @author user
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHandler";

	// CrashHandler 实例
	private static CrashHandler INSTANCE = new CrashHandler();

	// 程序的 Context 对象
	private Context mContext;

	// 系统默认的 UncaughtException 处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;

	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();

	// 用于格式化日期,作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	/** 保证只有一个 CrashHandler 实例 */
	private CrashHandler() {
	}

	/** 获取 CrashHandler 实例 ,单例模式 */
	public static CrashHandler getInstance() {
		return INSTANCE;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;

		// 获取系统默认的 UncaughtException 处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

		// 设置该 CrashHandler 为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当 UncaughtException 发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}

			LooperThread exitThread = new LooperThread();
			exitThread.start();
		}
	}

	class LooperThread extends Thread {
		public Handler mHandler;

		@Override
		public void run() {
			Looper.prepare();
			mHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					boolean flg = msg.getData().getBoolean("flg");
					if (flg) {
						System.out.println("捕获异常时退出成功！");
					} else {
						System.out.println("捕获异常时退出失败！");
					}

					Intent intent = new Intent(mContext, Login_Activity.class);
					PendingIntent restartIntent = PendingIntent.getActivity(
							mContext.getApplicationContext(), 0, intent,
							Intent.FLAG_ACTIVITY_NEW_TASK);
					// 退出程序
					AlarmManager mgr = (AlarmManager) mContext
							.getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC,
							System.currentTimeMillis() + 3000, restartIntent); // 1秒钟后重启应用
					ActivityCollector.finishAll();
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

	/**
	 * 自定义错误处理，收集错误信息，发送错误报告等操作均在此完成
	 * 
	 * @param ex
	 * @return true：如果处理了该异常信息；否则返回 false
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		// 使用 Toast 来显示异常信息
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();

				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.err_exit, null, false);
				Toast toast = new Toast(mContext);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.setView(layout);
				toast.show();

				Looper.loop();
			}
		}.start();

		// 收集设备参数信息
		collectDeviceInfo(mContext);
		// 保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}

	/**
	 * 收集设备参数信息
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);

			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}

		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex
	 */
	private void saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();

		String result = writer.toString();
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";

			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				String path = "/sdcard/wenyankeji/chansheng/crash/";
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				// ////////////////////////////
				File buglog = new File(path + fileName);
				if (!buglog.exists()) {
					buglog.createNewFile();
				}
				// ////////////////////////////
				FileOutputStream fos = new FileOutputStream(path + fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
				Log.e(TAG, sb.toString());
			}

		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}

	}
}
