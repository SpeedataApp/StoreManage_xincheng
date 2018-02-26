package com.xuyulong.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.xuyulong.Store.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GradViewAdapter extends BaseAdapter {
	ArrayList<HashMap<String, Object>> m_ListData = null;
	protected LayoutInflater mInflater = null;

	public GradViewAdapter(ArrayList<HashMap<String, Object>> data,
			Context context) {
		m_ListData = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return m_ListData.size();
	}

	@Override
	public HashMap<String, Object> getItem(int position) {
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
				convertView = mInflater
						.inflate(R.layout.item_of_gridview, null);
				holder = new View_Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (View_Holder) convertView.getTag();
			}
			HashMap<String, Object> obj = m_ListData.get(position);
			holder.get_index_text().setImageResource(
					(Integer) obj.get("ItemImage"));
			holder.get_name_text().setText((String) obj.get("ItemText"));
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

		private ImageView mGroupText = null;

		public ImageView get_index_text() {
			if (mGroupText == null)
				mGroupText = (ImageView) parentView
						.findViewById(R.id.ItemImage);
			return mGroupText;
		}

		private TextView mItemText = null;

		public TextView get_name_text() {
			if (mItemText == null)
				mItemText = (TextView) parentView.findViewById(R.id.ItemText);
			return mItemText;
		}

	}
}
