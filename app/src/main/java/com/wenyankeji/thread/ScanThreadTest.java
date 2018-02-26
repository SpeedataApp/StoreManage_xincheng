//package com.wenyankeji.thread;
//
//import java.io.IOException;
//
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//
//import AppConfig;
//import com.xuyulong.ui.MySpinnerButton;
//
///**
// * 山寨机 扫描条码的线程（二维码&条形码）
// * 
// * @author Jiayu
// * 
// */
//public class ScanThreadTest extends Thread {
//
//	private Handler mHandler;
//
//	/**
//	 * if throw exception, serialport initialize fail.
//	 * 
//	 * @throws SecurityException
//	 * @throws IOException
//	 */
//	public ScanThreadTest(Handler mHandler) {
//		this.mHandler = mHandler;
//	}
//
//	@Override
//	public void run() {
//		try {
//			int size = 0;
//			byte[] buffer = new byte[2048];
//			int available = 0;
//			while (!isInterrupted()) {
//				available = AppConfig.getInstance().is.available();
//				if (available > 0) {
//					size = AppConfig.getInstance().is.read(buffer);
//					// if(size > 0){
//					if (size > 1) {
//						sendMessege(buffer, size);
//					}
//				}
//
//			}
//		} catch (IOException e) {
//			// 返回错误信息
//			e.printStackTrace();
//		}
//		super.run();
//	}
//
//	private void sendMessege(byte[] data, int dataLen) {
//		AppConfig.getInstance().mSerialPort.scaner_trigoff();
//		String dataStr = new String(data, 0, dataLen);
//		System.out.println("二维码是：" + dataStr);
//
//		Bundle bundle = new Bundle();
//		bundle.putString("labelCode", dataStr);
//		Message msg = new Message();
//		msg.what = MySpinnerButton.SCAN_MODE_SCAN_HEAD;
//		msg.setData(bundle);
//
//		mHandler.sendMessage(msg);
//	}
//}
