package com.xuyulong.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.xuyulong.Store.R;
import com.xuyulong.ui.HVListView;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChaXunListViewAdapter extends BaseAdapter {
	ArrayList<HashMap<String, Object>> m_ListData = null;
	protected LayoutInflater mInflater = null;
	private HVListView mListView = null;

	public void SetData(ArrayList<HashMap<String, Object>> data) {
		m_ListData = data;
		notifyDataSetChanged();
	}

	public ChaXunListViewAdapter(ArrayList<HashMap<String, Object>> data,
			Context context, HVListView listView) {
		m_ListData = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListView = listView;
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
				convertView = mInflater.inflate(
						R.layout.item_of_listview_chaxun, null);
				holder = new View_Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (View_Holder) convertView.getTag();
			}
			HashMap<String, Object> obj = m_ListData.get(position);
			holder.get_No_text().setText((position + 1) + "");
			holder.get_index_text().setText((String) obj.get("createdAt"));
			holder.get_name_text().setText((String) obj.get("eventName"));
			holder.get_Operator_text().setText((String) obj.get("handleName"));
			if (position % 2 == 0)
				holder.get_Rl().setBackgroundColor(0xffeeeeee);
			else
				holder.get_Rl().setBackgroundColor(0xffffffff);
		} catch (Exception e) {
			Log.e("err", e.getMessage());
		}

		// 校正（处理同时上下和左右滚动出现错位情况）
		View child = ((ViewGroup) convertView).getChildAt(1);
		int head = mListView.getHeadScrollX();
		if (child.getScrollX() != head) {
			child.scrollTo(mListView.getHeadScrollX(), 0);
		}
		return convertView;
	}

	public class View_Holder {
		private View parentView;

		public View_Holder(View view) {
			this.parentView = view;
		}

		private TextView mGroupText = null;

		public TextView get_index_text() {
			if (mGroupText == null)
				mGroupText = (TextView) parentView.findViewById(R.id.tx1);
			return mGroupText;
		}

		private LinearLayout mRelativeLayout = null;

		public LinearLayout get_Rl() {
			if (mRelativeLayout == null)
				mRelativeLayout = (LinearLayout) parentView
						.findViewById(R.id.rL);
			return mRelativeLayout;
		}

		private TextView mItemText = null;

		public TextView get_name_text() {
			if (mItemText == null)
				mItemText = (TextView) parentView.findViewById(R.id.tx2);
			return mItemText;
		}

		private TextView NoText = null;

		public TextView get_No_text() {
			if (NoText == null)
				NoText = (TextView) parentView.findViewById(R.id.Notv);
			return NoText;
		}

		private TextView operatorText = null;

		public TextView get_Operator_text() {
			if (operatorText == null)
				operatorText = (TextView) parentView
						.findViewById(R.id.operator);
			return operatorText;
		}

	}
}
