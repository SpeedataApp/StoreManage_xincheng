package com.xuyulong.adapter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.entity.ReceiveMessage;
import com.wenyankeji.entity.RukuCs;
import com.xuyulong.Store.AppConfig;
import com.xuyulong.Store.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListViewRuKuAdapter extends BaseAdapter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7492984959307101887L;
	ArrayList<HashMap<String, Object>> m_ListData = null;
	protected LayoutInflater mInflater = null;
	private boolean mBCheckShow = false;

	public ListViewRuKuAdapter(ArrayList<HashMap<String, Object>> data,
			Context context) {
		m_ListData = data;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ArrayList<HashMap<String, Object>> getData() {
		return m_ListData;
	}

	public void Cancle() {
		for (int i = m_ListData.size() - 1; i >= 0; i--) {
			if (m_ListData.get(i).containsKey("select")) {
				m_ListData.get(i).put("select", false);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 入库列表界面向称重页面传送的json
	 * 
	 * @return
	 */
	public NetRequest GetJsonResultRuku() {
		NetRequest req = new NetRequest();
		ReceiveMessage receiveMessage = new ReceiveMessage();
		RukuCs rukuCs = new RukuCs();
		LinkedList<HashMap<String, Object>> csRukuDetails = new LinkedList<HashMap<String, Object>>();

		Double weightAll = 0.0;
		for (int i = 0; i < m_ListData.size(); i++) {
			HashMap<String, Object> one = new HashMap<String, Object>();

			one.put("qrCode", m_ListData.get(i).get("qrCode"));
			one.put("weight", m_ListData.get(i).get("weight"));
			csRukuDetails.add(one);

			weightAll += Double.parseDouble((String) m_ListData.get(i).get(
					"weight"));
		}
		receiveMessage.setCsRukuDetails(csRukuDetails);
		receiveMessage.setEnterpriseType(AppConfig.getInstance().Platform);

		// 总重量
		rukuCs.setWeight(new BigDecimal(weightAll));
		// 企业id
		rukuCs.setEntId(AppConfig.getInstance().enterpriseId);
		// 企业名字
		rukuCs.setEntName(AppConfig.getInstance().enterpriseName);
		// 用户id
		rukuCs.setUserId(AppConfig.getInstance().UserId);
		// 用户名字
		rukuCs.setUserName(AppConfig.getInstance().UserName);
		// 危废id
		rukuCs.setWasteId(new BigDecimal((String) m_ListData.get(0).get(
				"wasteId")));
		// 危废名字
		rukuCs.setWasteName((String) m_ListData.get(0).get("wasteName"));
		// 危废Code
		rukuCs.setWasteCode((String) m_ListData.get(0).get("wasteCode"));
		// 危废Type
		rukuCs.setWasteType((String) m_ListData.get(0).get("wasteType"));
		//
		rukuCs.setXingtai("xingtai_test");
		//
		rukuCs.setCarNo("carNo_test");
		//
		rukuCs.setCreatedAt("CreatedAt_test");
		// 有害成分
		rukuCs.setDangerFeatures((String) m_ListData.get(0).get("dangerCase"));
		// 数量
		rukuCs.setQty(m_ListData.size() + "");

		receiveMessage.setRukuCs(rukuCs);

		req.setReceiveMessage(receiveMessage);

		return req;
	}

	public boolean GetCheck() {
		return mBCheckShow;
	}

	public void ShowCheck(boolean show) {
		mBCheckShow = show;
		notifyDataSetChanged();
	}

	public int DeleteSelect() {
		int count = 0;
		for (int i = m_ListData.size() - 1; i >= 0; i--) {
			if (m_ListData.get(i).containsKey("select")
					&& (Boolean) m_ListData.get(i).get("select")) {
				m_ListData.remove(i);
				count++;
			}
		}
		notifyDataSetChanged();
		return count;
	}

	public void DeleteHash(int pos) {
		m_ListData.remove(pos);
		notifyDataSetChanged();
	}
	
	public void DeleteAllHash() {
		m_ListData.clear();
		notifyDataSetChanged();
	}

	public void AddHash(HashMap<String, Object> mp) {
		m_ListData.add(mp);
		notifyDataSetChanged();
	}

	public void SetData(ArrayList<HashMap<String, Object>> data) {
		m_ListData = data;
		notifyDataSetChanged();
	}

	public void replaceHash(HashMap<String, Object> mp, int position) {
		m_ListData.remove(position);
		m_ListData.add(0, mp);
		notifyDataSetChanged();
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
			final int pos = position;
			View_Holder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_of_listview_ruku,
						null);
				holder = new View_Holder(convertView);
				convertView.setTag(holder);
			} else {
				holder = (View_Holder) convertView.getTag();
			}
			HashMap<String, Object> obj = m_ListData.get(position);
			if (mBCheckShow) {
				holder.get_check().setVisibility(View.VISIBLE);
				if (obj.containsKey("select") && ((Boolean) obj.get("select")))
					holder.get_check().setChecked(true);
				else
					holder.get_check().setChecked(false);

				holder.get_check().setOnCheckedChangeListener(
						new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								m_ListData.get(pos).put("select", isChecked);
							}
						});
			} else
				holder.get_check().setVisibility(View.INVISIBLE);
			holder.get_name_text1().setText((String) obj.get("qrCode"));
			holder.get_name_text2().setText((String) obj.get("wasteName"));
			holder.get_name_text3().setText((String) obj.get("weight"));
			if (position % 2 == 0)
				holder.get_Rl().setBackgroundColor(0xffeeeeee);
			else
				holder.get_Rl().setBackgroundColor(0xffffffff);
		} catch (Exception e) {
			// Log.e("err", e.getMessage());
			System.out.println("err" + e.getMessage());
		}
		return convertView;
	}

	public class View_Holder {
		private View parentView;

		public View_Holder(View view) {
			this.parentView = view;
		}

		private LinearLayout mRelativeLayout = null;

		public LinearLayout get_Rl() {
			if (mRelativeLayout == null)
				mRelativeLayout = (LinearLayout) parentView
						.findViewById(R.id.r1);
			return mRelativeLayout;
		}

		private CheckBox mGroupText = null;

		public CheckBox get_check() {
			if (mGroupText == null)
				mGroupText = (CheckBox) parentView.findViewById(R.id.c1);
			return mGroupText;
		}

		private TextView mItemText = null;

		public TextView get_name_text1() {
			if (mItemText == null)
				mItemText = (TextView) parentView.findViewById(R.id.tx1);
			return mItemText;
		}

		private TextView mItemText2 = null;

		public TextView get_name_text2() {
			if (mItemText2 == null)
				mItemText2 = (TextView) parentView.findViewById(R.id.tx2);
			return mItemText2;
		}

		private TextView mItemText3 = null;

		public TextView get_name_text3() {
			if (mItemText3 == null)
				mItemText3 = (TextView) parentView.findViewById(R.id.tx3);
			return mItemText3;
		}
	}
}
