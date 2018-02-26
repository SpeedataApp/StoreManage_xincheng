package com.xuyulong.util;

import java.util.HashMap;
import java.util.Map;

import com.xuyulong.Store.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundUtils {

	public SoundPool sp;
	public Map<Integer, Integer> suondMap;
	public Context context;

	// 初始化声音池
	public SoundUtils(Context context) {
		this.context = context;
		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
		suondMap = new HashMap<Integer, Integer>();
		suondMap.put(R.raw.msg, sp.load(context, R.raw.msg, 1));
		suondMap.put(R.raw.cangkudibang,
				sp.load(context, R.raw.cangkudibang, 1));
		suondMap.put(R.raw.kaiqiyuyin, sp.load(context, R.raw.kaiqiyuyin, 1));
		suondMap.put(R.raw.lianjiedibang,
				sp.load(context, R.raw.lianjiedibang, 1));
		suondMap.put(R.raw.saomiaobiaoqian,
				sp.load(context, R.raw.saomiaobiaoqian, 1));
		suondMap.put(R.raw.saomiaochaka,
				sp.load(context, R.raw.saomiaochaka, 1));
		suondMap.put(R.raw.xuanzecangku,
				sp.load(context, R.raw.xuanzecangku, 1));
		suondMap.put(R.raw.beep,
				sp.load(context, R.raw.beep, 1));
	}

	// 播放声音池声音
	public void play(int sound) {

		AudioManager am = (AudioManager) context
				.getSystemService(context.AUDIO_SERVICE);
		// 返回当前AlarmManager最大音量
		float audioMaxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// 返回当前AudioManager对象的音量值
		float audioCurrentVolume = am
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float volumnRatio = audioCurrentVolume / audioMaxVolume;
		sp.play(suondMap.get(sound), // 播放的音乐Id
				audioCurrentVolume, // 左声道音量
				audioCurrentVolume, // 右声道音量
				1, // 优先级，0为最低
				0, // 循环次数，0无不循环，-1无永远循环
				1);// 回放速度，值在0.5-2.0之间，1为正常速度
	}
}
