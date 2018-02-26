package com.wenyankeji.popwindow;

import net.tsz.afinal.FinalDb;

import com.xuyulong.Store.AppConfig;
import com.xuyulong.Store.R;
import com.wenyankeji.entity.Cangku;
import com.wenyankeji.wheelview.views.ArrayWheelAdapter;
import com.wenyankeji.wheelview.views.OnWheelChangedListener;
import com.wenyankeji.wheelview.views.WheelView;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SelectPicPopupWindow extends PopupWindow {

	private Button btn_yes, btn_cancel, btn_default;
	private View mMenuView;
	private WheelView wheelLeft;
	private WheelView wheelRight;
	private TextView selectRes;
	private String[] left;
	private String[][] right;
	private int[] repositoryIds;
	private int[][] spaceIds;
	private Context context;
	

	public SelectPicPopupWindow(Activity context, String[] left,
			final String[][] right, TextView selectRes,
			int[] repositoryIds,
			int[][] spaceIds) {
		super(context);
		this.selectRes = selectRes;
		this.left = left;
		this.right = right;
		this.spaceIds = spaceIds;
		this.repositoryIds = repositoryIds;
		this.context = context;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.alert_dialog, null);

		btn_yes = (Button) mMenuView.findViewById(R.id.btn_yes);
		btn_cancel = (Button) mMenuView.findViewById(R.id.btn_cancel);
		btn_default = (Button) mMenuView.findViewById(R.id.btn_default);

		wheelLeft = (WheelView) mMenuView.findViewById(R.id.leftWheelView);
		wheelRight = (WheelView) mMenuView.findViewById(R.id.rightWheelView);

		// 取消按钮
		btn_cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 销毁弹出框
				dismiss();
			}
		});
		// 设置按钮监听
		btn_yes.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectPicPopupWindow.this.dismiss();
				returnResForOnce();


			}
		});
		// 设置按钮监听
		btn_default.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SelectPicPopupWindow.this.dismiss();
				returnResForSetDefault();
			}
		});

		// 设置WheelView
		wheelLeft.setVisibleItems(5);
		wheelLeft.setCyclic(false);
		wheelLeft.setAdapter(new ArrayWheelAdapter<String>(left));

		wheelRight.setVisibleItems(5);
		wheelRight.setCyclic(false);

		// //////////////////////////
		wheelRight.setAdapter(new ArrayWheelAdapter<String>(right[0]));

		wheelLeft.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				wheelRight.setAdapter(new ArrayWheelAdapter<String>(
						right[newValue]));
				wheelRight.setCurrentItem(right[newValue].length / 2);
			}
		});
		// //////////////////////////////////////////////////////////

		// 设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		// 设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		// 设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		// 设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		// 设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimBottom);
		// 实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		// 设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);
		// mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mMenuView.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});
	}
	
	/**
	 * 确认时候返回的选择的结果
	 * 
	 */
	private void returnResForOnce() {
		
		int repositoryId = repositoryIds[GetLeftPos()];
		int spaceId = spaceIds[GetLeftPos()][GetRingthPos()];
		String repositoryName = left[GetLeftPos()];
		String spaceName = right[GetLeftPos()][GetRingthPos()];
		selectRes.setText(repositoryName + "\n" + spaceName);
		
		FinalDb db = AppConfig.getInstance().db;
		Cangku cangku = new Cangku();
		cangku.setUserId(AppConfig.getInstance().UserId.toString());
		cangku.setSpaceId(spaceId);
		cangku.setRepositoryId(repositoryId);
		cangku.setSpaceName(spaceName);
		cangku.setRepositoryName(repositoryName);
		cangku.setCangkuDefault(false);
		
		if (db.findById(cangku.getUserId(), Cangku.class) == null) {
			db.save(cangku);
		}else{
			db.update(cangku);
		}
	}

	/**
	 * 返回选择的结果
	 * 
	 * @return
	 */
	private void returnResForSetDefault() {
		int repositoryId = repositoryIds[GetLeftPos()];
		int spaceId = spaceIds[GetLeftPos()][GetRingthPos()];
		String repositoryName = left[GetLeftPos()];
		String spaceName = right[GetLeftPos()][GetRingthPos()];
		selectRes.setText(repositoryName + "\n" + spaceName);
		
		FinalDb db = AppConfig.getInstance().db;
		Cangku cangku = new Cangku();
		cangku.setUserId(AppConfig.getInstance().UserId.toString());
		cangku.setSpaceId(spaceId);
		cangku.setRepositoryId(repositoryId);
		cangku.setSpaceName(spaceName);
		cangku.setRepositoryName(repositoryName);
		cangku.setCangkuDefault(true);
		
		if (db.findById(cangku.getUserId(), Cangku.class) == null) {
			db.save(cangku);
		}else{
			db.update(cangku);
		}
	}

	public int GetLeftPos() {
		return wheelLeft.getCurrentItem();
	}

	public int GetRingthPos() {
		return wheelRight.getCurrentItem();

	}

}
