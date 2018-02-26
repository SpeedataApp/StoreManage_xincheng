package com.xuyulong.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.R.integer;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * Log统一管理类
 * 
 * @author way
 * 
 */
public class LogUtil {

	private static LogUtil INSTANCE = null;
	private static String PATH_LOG;
	private int mPid;
	private LogDumper mLogDumper;

	/**
	 * 初始化目录
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			// 优先保存到SD卡
			PATH_LOG = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + "chuzhi";
		} else {// 如果SD卡不存在，就保存到本应用的目录下
			PATH_LOG = context.getFilesDir().getAbsolutePath() + File.separator
					+ "miniGPS";
		}
		File file = new File(PATH_LOG);
		if (!file.exists()) {
			file.mkdirs();
		}

	}

	public static LogUtil getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new LogUtil(context);
		}
		return INSTANCE;
	}

	private LogUtil(Context context) {
		init(context);
		mPid = android.os.Process.myPid();
	}

	public void start() {
		if (mLogDumper == null)
			mLogDumper = new LogDumper(String.valueOf(mPid), PATH_LOG);
		mLogDumper.start();
	}

	public void stop() {
		if (mLogDumper != null) {
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	private class LogDumper extends Thread {

		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = true;
		String cmds = null;
		private String mPID;
		private FileOutputStream out = null;

		public LogDumper(String pid, String dir) {
			mPID = pid;
			try {
				out = new FileOutputStream(new File(dir, "GPS-"
						+ MyDate.getFileName() + ".log"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**
			 * 
			 * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
			 * 
			 * 显示当前mPID程序的 E和W等级的日志.
			 * 
			 * */

			cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
			// cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
			// cmds = "logcat -s way";//打印标签过滤信息
			// cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";

		}

		public void stopLogs() {
			mRunning = false;
		}

		@Override
		public void run() {
			try {
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(
						logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if (out != null && line.contains(mPID)) {
						out.write((MyDate.getDateEN() + "  " + line + "\n")
								.getBytes());
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (logcatProc != null) {
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null) {
					try {
						mReader.close();
						mReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out = null;
				}

			}

		}

	}

}
