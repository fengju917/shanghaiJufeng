package com.centerm.lklcpos.deviceinterface;

import org.apache.log4j.Logger;

import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.Utility;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public abstract class AbstractDevJsIfc {
	private static Logger logger = Logger.getLogger(AbstractDevJsIfc.class);

	private static final int time_out = 1000;
	protected Context context;
	protected Handler handler;
	private int timeout = time_out;
	protected boolean isopen = false;
	protected boolean is_gettingValue = false;

	public AbstractDevJsIfc(Context context, Handler handler) throws Exception {
		this.context = context;
		this.handler = handler;
		// init();
	}

	public abstract void init() throws Exception;

	protected void sendDevErrorMsg(String position, String status) {
		try {
			Message msg = Utility.createMesage(Constant.msg.msg_dev_error);
			msg.getData().putString(Constant.msg.dev_position, position);
			msg.getData().putString(Constant.msg.dev_status, status);
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("", e);
		}
	}

	public void setTimeOut(int timeout) {
		this.timeout = time_out;
	}

	public boolean isopen() {
		return isopen;
	}

	public abstract String openDev() throws Exception;

	public abstract String closeDev() throws Exception;

}
