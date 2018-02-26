package com.xuyulong.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * 所有活动管理器
 */
public class ActivityCollector {

	public static List<Activity> activities = new ArrayList<Activity>();

	/**
	 * 添加活动
	 * 
	 * @param activity
	 */
	public static void addActivity(Activity activity) {
		if (!activities.contains(activity)) {
			activities.add(activity);
		}
	}

	/**
	 * 移除活动
	 * 
	 * @param activity
	 */
	public static void removeActivity(Activity activity) {
		activities.remove(activity);
	}

	/**
	 * 结束所有活动
	 */
	public static void finishAll() {
		for (Activity activity : activities) {
			if (!activity.isFinishing()) {
				activity.finish();
			}
		}

		// 杀死该应用进程
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
