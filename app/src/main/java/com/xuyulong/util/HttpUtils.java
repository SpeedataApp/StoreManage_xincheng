package com.xuyulong.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

public class HttpUtils {

	public static final String HTTP_PUT_FAIL = "连接服务器失败，请确保网络开启！";
	public static final String HTTP_GET_FAIL = "连接服务器失败，请确保网络开启！";

	public static String httpPut(String url, String json) {

		HttpPut put = new HttpPut(url);
		put.setHeader("content-type", "application/json;charset=UTF-8");

		HttpParams params = put.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 20000);
		HttpConnectionParams.setSoTimeout(params, 20000);

		HttpClient httpClient = new DefaultHttpClient();

		try {
			StringEntity xpr = new StringEntity(json, HTTP.UTF_8);
			put.setEntity(xpr);
			HttpResponse response = httpClient.execute(put);

			int stateCode = response.getStatusLine().getStatusCode();
			StringBuffer sb = new StringBuffer();

			if (stateCode == HttpStatus.SC_OK) {
				HttpEntity result = response.getEntity();
				if (result != null) {
					InputStream is = result.getContent();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					String tempLine;
					while ((tempLine = br.readLine()) != null) {
						sb.append(tempLine);
					}
				}
			}
			put.abort();

			System.out.println(sb.toString());

			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return HTTP_PUT_FAIL;
	}

	// public static String httpPut(String url11, String json) {
	// try {
	// URL url = new URL(url11);
	// HttpURLConnection connection = (HttpURLConnection) url
	// .openConnection();
	// connection.setDoOutput(true);
	// connection.setDoInput(true);
	// connection.setRequestMethod("PUT");
	// connection.setUseCaches(false);
	// connection.setRequestProperty("Content-Type",
	// "application/json;charset=UTF-8");
	// OutputStream wr = connection.getOutputStream();
	// wr.write(json.getBytes());
	// wr.flush();
	// wr.close();
	//
	// // 返回数据
	// if (connection.getResponseCode() == 200) {
	// InputStream in = connection.getInputStream();
	// byte datas[] = new byte[1024];
	// while (in.read() != -1) {
	// in.read(datas);
	// }
	//
	// String res = new String(datas, 0, datas.length);
	// return res;
	// }
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (ProtocolException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return HTTP_PUT_FAIL;
	// }

	public static String httpGet(String url) {

		HttpGet get = new HttpGet(url);
		get.addHeader("content-type", "application/json;charset=UTF-8");
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpResponse response = httpClient.execute(get);

			int stateCode = response.getStatusLine().getStatusCode();
			StringBuffer sb = new StringBuffer();

			if (stateCode == HttpStatus.SC_OK) {
				HttpEntity result = response.getEntity();
				if (result != null) {
					InputStream is = result.getContent();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					String tempLine;
					while ((tempLine = br.readLine()) != null) {
						sb.append(tempLine);
					}
				}
			}
			get.abort();

			System.out.println(sb.toString());
			return sb.toString();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return HTTP_GET_FAIL;

	}
}
