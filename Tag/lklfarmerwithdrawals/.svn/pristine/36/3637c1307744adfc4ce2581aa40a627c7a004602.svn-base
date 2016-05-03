package com.centerm.lklcpos.deviceinterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class JsResponse {

	private static final Logger log = Logger.getLogger(JsResponse.class);

	public static final String state = "state";
	public static final String errMsg = "errMsg";
	public static final String retStatus = "retStatus";
	public static final String retData = "retData";
	public static final String suc_state = "1";
	public static final String fail_state = "0";

	private boolean isSuc = true;
	private String msg = "";
	private Map data = new HashMap();

	public JsResponse() {
		super();
	}

	public JsResponse(boolean isSuc, String msg, Map<String, String> data) {
		super();
		this.isSuc = isSuc;
		this.msg = msg;
		this.data = data;
	}

	public boolean isSuc() {
		return isSuc;
	}

	public void setSuc(boolean isSuc, String msg) {
		this.isSuc = isSuc;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public synchronized void addData(String key, Object value) {
		if (this.data == null) {
			data = new HashMap();
		}
		data.put(key, value);
	}

	public String toJson() {
		try {
			JSONObject responseJson = new JSONObject();
			JSONObject retStatusJson = new JSONObject();
			JSONObject retDataJson = new JSONObject();
			retStatusJson.put(state, isSuc ? suc_state : fail_state);
			retStatusJson.put(errMsg, msg);
			Iterator it = data.entrySet().iterator();
			while (it.hasNext()) {
				Entry entry = (Entry) it.next();
				retDataJson.put(entry.getKey().toString(), entry.getValue());
			}
			responseJson.put(retStatus, retStatusJson);
			responseJson.put(retData, retDataJson);
			return responseJson.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "{\"" + retStatus + "\":{\"" + state + "\":\"0\",\"" + errMsg + "\":\"锟斤拷锟斤拷锟届常\"}";
		}
	}

}
