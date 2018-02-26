package com.xuyulong.adapter;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xuyulong.Store.R;

public class ChenZhongListViewAdapter extends BaseAdapter implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1027000513895306830L;
	List<String> m_ListData = null;
	protected LayoutInflater mInflater = null;

	public ChenZhongListViewAdapter(List<String> data, Context context) {
		m_ListData = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return m_ListData.size();
	}

	@Override
	public String getItem(int position) {
		return m_ListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			View_Holder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.item_of_listview_bluetooth_devices, null);
				holder = new View_Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (View_Holder) convertView.getTag();
			}
			String deviceName = m_ListData.get(position);
			holder.getDeviceName().setText(deviceName);

			if (position % 2 == 0)
				holder.get_Rl().setBackgroundColor(0xffeeeeee);
			else
				holder.get_Rl().setBackgroundColor(0xffffffff);
		} catch (Exception e) {
			Log.e("err", e.getMessage());
		}
		return convertView;
	}

	public class View_Holder {
		private View parentView;

		public View_Holder(View view) {
			this.parentView = view;
		}

		private TextView deviceName = null;

		public TextView getDeviceName() {
			if (deviceName == null)
				deviceName = (TextView) parentView
						.findViewById(R.id.bluetoothDevice);
			return deviceName;
		}

		private LinearLayout deviceLinearLayout = null;

		public LinearLayout get_Rl() {
			if (deviceLinearLayout == null)
				deviceLinearLayout = (LinearLayout) parentView
						.findViewById(R.id.deviceLinearLayout);
			return deviceLinearLayout;
		}

	}

}
