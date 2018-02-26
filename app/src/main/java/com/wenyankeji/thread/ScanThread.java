//package com.wenyankeji.thread;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import cn.pda.serialport.SerialPort;
//
//import com.xuyulong.ui.MySpinnerButton;
//
///**
// * 山寨机 扫描条码的线程（二维码&条形码）
// * 
// * @author Jiayu
// * 
// */
//public class ScanThread extends Thread {
//
//	private SerialPort mSerialPort;
//	private InputStream is;
//	private OutputStream os;
//	/* serialport parameter */
//	private int port = 0;
//	private int baudrate = 9600;
//	private int flags = 0;
//	private Handler handler;
//
//	/**
//	 * if throw exception, serialport initialize fail.
//	 * 
//	 * @throws SecurityException
//	 * @throws IOException
//	 */
//	public ScanThread(Handler handler) throws SecurityException, IOException {
//		this.handler = handler;
//		mSerialPort = new SerialPort(port, baudrate, flags);
//		mSerialPort.scaner_poweron();
//		is = mSerialPort.getInputStream();
//		os = mSerialPort.getOutputStream();
//		try {
//			Thread.sleep(50);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		/** clear useless data **/
//		byte[] temp = new byte[128];
//		is.read(temp);
//	}
//
//	@Override
//	public void run() {
//		try {
//			int size = 0;
//			byte[] buffer = new byte[2048];
//			int available = 0;
//			while (!isInterrupted()) {
//				available = is.available();
//				if (available > 0) {
//					size = is.read(buffer);
//					// if(size > 0){
//					if (size > 1) {
//						sendMessege(buffer, size);
//					}
//				}
//			}
//		} catch (IOException e) {
//			// 返回错误信息
//			e.printStackTrace();
//		}
//		super.run();
//	}
//
//	private void sendMessege(byte[] data, int dataLen) {
//		mSerialPort.scaner_trigoff();
//		String dataStr = new String(data, 0, dataLen);
//		System.out.println("二维码是：" + dataStr);
//
//		Bundle bundle = new Bundle();
//		bundle.putString("labelCode", dataStr);
//		Message msg = new Message();
//		msg.what = MySpinnerButton.SCAN_MODE_SCAN_HEAD;
//		msg.setData(bundle);
//
//		handler.sendMessage(msg);
//	}
//
//	public void scan() {
//		if (mSerialPort.scaner_trig_stat() == true) {
//			mSerialPort.scaner_trigoff();
//		} else {
//			mSerialPort.scaner_trigon();
//		}
//	}
//
//	public void close() {
//		if (mSerialPort != null) {
//			mSerialPort.scaner_poweroff();
//			try {
//				if (is != null) {
//					is.close();
//				}
//				if (os != null) {
//					os.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			mSerialPort.close(port);
//		}
//	}
//}
