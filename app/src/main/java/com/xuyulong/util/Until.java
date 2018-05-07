package com.xuyulong.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.wenyankeji.entity.AppInfo;
import com.wenyankeji.entity.Label;
import com.wenyankeji.entity.NetRequest;
import com.wenyankeji.entity.ResultMessage;
import com.wenyankeji.entity.User;
import com.xuyulong.Store.AppConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Until {

	static public void setPricePoint(final EditText editText) {
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.toString().contains(".")) {
					if (s.length() - 1 - s.toString().indexOf(".") > 2) {
						s = s.toString().subSequence(0,
								s.toString().indexOf(".") + 3);
						editText.setText(s);
						editText.setSelection(s.length());
					}
				}
				if (s.toString().trim().substring(0).equals(".")) {
					s = "0" + s;
					editText.setText(s);
					editText.setSelection(2);
				}

				if (s.toString().startsWith("0")
						&& s.toString().trim().length() > 1) {
					if (!s.toString().substring(1, 2).equals(".")) {
						editText.setText(s.subSequence(0, 1));
						editText.setSelection(1);
						return;
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}

		});

	}

	static public HashMap<String, Object> JsonToHashMap(String json) {
		HashMap<String, Object> arr = new HashMap<String, Object>();
		try {
			JSONObject obj = new JSONObject(json);
			Iterator it = obj.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = obj.getString(key);
				arr.put(key, value);
			}
		} catch (Exception e) {
			L.d(e.toString());
		}
		return arr;
	}

	static public HashMap<String, Object> JsonToHashMap(JSONObject obj) {
		HashMap<String, Object> arr = new HashMap<String, Object>();
		try {
			Iterator it = obj.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = obj.getString(key);
				arr.put(key, value);
			}
		} catch (Exception e) {
			L.d(e.toString());
		}
		return arr;
	}

	/**
	 * 解析获取仓库信息成功后返回的json
	 * 
	 * @param json
	 * @return
	 */
	static public HashMap<String, Object> getWarehouseInfo(String json) {
		HashMap<String, Object> arr = new HashMap<String, Object>();
		try {

			JSONObject obj = new JSONObject(json);
			JSONObject resultMessage = obj.getJSONObject("resultMessage");

			JSONArray jarr = resultMessage.getJSONArray("warehouses");
			/**
			 * 减1的原因是返回的数据 最后一项都是废数据 服务端是为了返回arr所以最后一项都废物数据
			 */
			// 仓库名字
			String[] repositoryName = new String[jarr.length() - 1];
			// 仓库id
			int[] repositoryId = new int[jarr.length() - 1];
			// 库位名字
			String[][] spaceName = new String[jarr.length() - 1][];
			// 库位id
			int[][] spaceId = new int[jarr.length() - 1][];

			for (int i = 0; i < jarr.length() - 1; i++) {

				// 一个仓库
				JSONObject one = jarr.getJSONObject(i);
				repositoryName[i] = one.getString("repositoryName");
				repositoryId[i] = one.getInt("repositoryId");
				// 库位数组
				JSONArray jarr2 = one.getJSONArray("warehouseLocations");
				System.out.println("jarr2.length()：" + jarr2.length());
				spaceName[i] = new String[jarr2.length() - 1];
				spaceId[i] = new int[jarr2.length() - 1];

				for (int j = 0; j < jarr2.length() - 1; j++) {
					JSONObject two = jarr2.getJSONObject(j);
					spaceId[i][j] = two.getInt("spaceId");
					spaceName[i][j] = two.getString("spaceName");
				}
			}

			arr.put("repositoryName", repositoryName);
			arr.put("spaceName", spaceName);
			arr.put("repositoryId", repositoryId);
			arr.put("spaceId", spaceId);
		} catch (Exception e) {
			System.out.println("******************error****************");
			System.out.println(e);
		}
		return arr;
	}

	/**
	 * 共通方法
	 * 
	 * @param json
	 * @return
	 */
	static public String parseResult(String json) {
		String result = "";
		try {
			JSONObject obj = new JSONObject(json);
			JSONObject resultMessage = obj.getJSONObject("resultMessage");

			ResultMessage res = null;
			res = JSON.parseObject(resultMessage.toString(),
					ResultMessage.class);

			if (res.isSuccess()) {
				result = "OK";
			} else {
				result = res.getMessage();
			}
		} catch (Exception e) {
			result = e.toString();
		}
		return result;
	}

	/**
	 * 获取标签信息
	 * 
	 * @param json
	 * @return
	 */
	static public HashMap<String, Object> getLabelCode(String json) {
		HashMap<String, Object> result = null;
		try {
			JSONObject data = new JSONObject(json);
			JSONObject resultMessage = data.getJSONObject("resultMessage");
			JSONObject label = resultMessage.getJSONObject("label");
			result = JsonToHashMap(label);
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 解析标签事件json
	 * 
	 * @param json
	 * @return
	 */
	static public HashMap<String, Object> queryLabelEvent(String json) {
		HashMap<String, Object> result = null;
		try {
			JSONObject data = new JSONObject(json);

			JSONObject resultMessage = data.getJSONObject("resultMessage");

			result = JsonToHashMap(resultMessage.getJSONObject("label")
					.toString());

			if (resultMessage.has("labelEvents")) {
				ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

				JSONArray arr = resultMessage.getJSONArray("labelEvents");
				/**
				 * 减1的原因是返回的数据 最后一项都是废数据 服务端是为了返回arr所以最后一项都废物数据
				 */
				for (int i = 0; i < arr.length() - 1; i++) {
					list.add(JsonToHashMap(arr.getJSONObject(i)));
				}
				result.put("labelEvents", list);
			}

		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 获取标签信息的请求json
	 * 
	 * @param qrcode
	 * @param userId
	 * @param operator
	 * @return
	 */
	static public String getLabelInfo_req(String qrcode, BigDecimal userId,
			int operator) {

		NetRequest req = new NetRequest();
		Label label = new Label();
		label.setQrCode(qrcode);
		label.setPlatform(AppConfig.getInstance().Platform);
		label.setUserId(userId);
		label.setOperator(operator);
		label.setEnterpriseId(AppConfig.getInstance().enterpriseId);
		req.setLabel(label);

		return JSON.toJSONString(req);
	}

	/**
	 * 获取标签历史事件的请求json
	 * 
	 * @param qrcode
	 * @param userId
	 * @return
	 */
	static public String getLabelEvent_req(String qrcode, BigDecimal userId) {

		NetRequest req = new NetRequest();
		Label label = new Label();
		label.setQrCode(qrcode);
		label.setUserId(userId);
		req.setLabel(label);

		System.out.println(JSON.toJSONString(req));
		return JSON.toJSONString(req);
	}

	/**
	 * 登陆的请求json
	 * @param userName  登录名称
	 * @param pwd  登录密码
	 * @param deviceId  设备唯一码
	 * @return
	 */
	static public String login_req(String userName, String pwd,String deviceId) {

		NetRequest req = new NetRequest();
		User user = new User();
		user.setUsername(userName);
		user.setPassword(pwd);
		user.setDeviceid(deviceId);
		user.setPlatform(AppConfig.getInstance().Platform);

		req.setUser(user);

		return JSON.toJSONString(req);
	}

	/**
	 * 登录成功，从返回的json里获取用户信息：userId,企业id
	 * 
	 * @param json
	 * @return
	 */
	static public ResultMessage getUserInfo(String json) {
		ResultMessage res = null;

		try {
			JSONObject obj = new JSONObject(json);
			JSONObject resultMessage = obj.getJSONObject("resultMessage");
			res = JSON.parseObject(resultMessage.toString(),
					ResultMessage.class);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 获取仓库仓位信息的请求json
	 * 
	 * @param enterpriseId
	 * @return
	 */
	static public String getCK_req(BigDecimal enterpriseId) {
		String result = "";

		JSONObject obj = new JSONObject();
		JSONObject receiveMessage = new JSONObject();
		try {
			receiveMessage.put("enterpriseId", enterpriseId);
			obj.put("receiveMessage", receiveMessage);
			result = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解析App版本的json
	 * 
	 * @param json
	 * @return
	 */
	static public AppInfo getAppInfo(String json) {
		AppInfo result = null;
		try {
			JSONObject data = new JSONObject(json);
			JSONObject resultMessage = data.getJSONObject("resultMessage");
			JSONObject appInfo = resultMessage.getJSONObject("appInfo");

			result = JSON.parseObject(appInfo.toString(), AppInfo.class);
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 获取车辆信息的请求json
	 * 
	 * @param rfid
	 * @return
	 */
	static public String getCar_req(String rfid) {
		String result = "";

		JSONObject obj = new JSONObject();
		JSONObject receiveMessage = new JSONObject();
		try {
			receiveMessage.put("rfid", rfid);
			receiveMessage.put("enterpriseId",
					AppConfig.getInstance().enterpriseId);
			obj.put("receiveMessage", receiveMessage);
			result = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 解析车卡信息返回的json
	 * 
	 * @param json
	 * @return
	 */
	static public ResultMessage geCarInfo(String json) {
		ResultMessage result = null;
		try {
			JSONObject data = new JSONObject(json);
			JSONObject resultMessage = data.getJSONObject("resultMessage");

			result = JSON.parseObject(resultMessage.toString(),
					ResultMessage.class);
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 获取App信息的请求json
	 * 
	 * @return
	 */
	static public String getApp_req() {
		String result = "";

		JSONObject obj = new JSONObject();
		JSONObject receiveMessage = new JSONObject();
		try {
			receiveMessage.put("platForm", AppConfig.getInstance().Platform);// 平台
			// 思必拓
			if (AppConfig.getInstance().handPhone == 2) {
				receiveMessage
						.put("pdaType", AppConfig.getInstance().handPhone);
			}
			receiveMessage.put("handInput", AppConfig.getInstance().handInput);

			obj.put("receiveMessage", receiveMessage);
			result = obj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 退出的请求json
	 * 
	 * @return
	 */
	static public String exit_req(BigDecimal UserId) {

		NetRequest req = new NetRequest();
		User user = new User();
		user.setUserId(UserId);

		req.setUser(user);

		return JSON.toJSONString(req);
	}
}
