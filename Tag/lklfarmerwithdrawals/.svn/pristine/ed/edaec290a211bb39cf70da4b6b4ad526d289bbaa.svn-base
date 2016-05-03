package com.centerm.lklcpos.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.centerm.basestation.dao.BaseStationDao;
import com.centerm.basestation.impl.BaseStationFactory;
import com.centerm.basestation.util.PropertiesUtil;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.SettleDataDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.SettleDataDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.PinPadDevJsIfc;
import com.centerm.lklcpos.http.ErrInfo;
import com.centerm.lklcpos.http.ErrInfoHandler;
import com.centerm.lklcpos.transaction.entity.ComponentNode;
import com.centerm.lklcpos.transaction.entity.Transaction;
import com.centerm.mid.bean.PINPadUtil.GetPINPadVersion;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.SystemDevInf;
import com.centerm.mid.util.M3Utility;

public class Utility {

	private static Logger log = Logger.getLogger(Utility.class);

	public static Map<String, String> jsonToMap(String json) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		JSONObject jsonPacket = new JSONObject(json);
		Iterator it = jsonPacket.keys();
		while (it.hasNext()) {
			String temp = it.next().toString();
			map.put(temp, jsonPacket.getString(temp));
		}
		return map;
	}

	/**
	 * 创建消息
	 * 
	 * @param msg
	 * @return
	 */
	public static Message createMesage(String msg) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.msg.msg, msg);
		message.setData(bundle);
		return message;
	}

	/**
	 * 获取IP地址
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getLocalIpAddress() throws Exception {

		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {

				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			log.error("获取IP地址失败....................", e);
			return null;
		}
		return null;
	}

	/**
	 * 获取SIM卡号
	 * 
	 * @param context
	 * @return
	 */
	public static String getPhoneNo(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = tm.getSubscriberId();
		Log.d("Utility", "getImsiNo=" + imsi);
		return imsi;
	}

	private static boolean comparetime(int hour, int minute) {
		log.debug("同步时钟时间: hour = " + hour + " minute =" + minute);
		if (new Date().getHours() > hour) {
			log.debug("当前时间小时比设置的大....");
			return true;
		} else if (new Date().getHours() < hour) {
			log.debug("当前时间小时比设置的小....");
			return false;
		} else {
			if (new Date().getMinutes() > minute) {
				log.debug("当前时间分钟比设置的大....");
				return true;
			} else {
				log.debug("当前时间分钟比设置的小或者相等....");
				return false;
			}
		}
	}

	public static long getJobSeculeTime(long secutime) {
		return new Date().getTime() + secutime;
		// Calendar calendar = Calendar.getInstance();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:ss");
		// if(comparetime(hour,minute)){
		// calendar.add(Calendar.DAY_OF_YEAR, 1);
		// }else{
		// calendar.add(Calendar.DAY_OF_YEAR, 0);
		// }
		// calendar.set(Calendar.HOUR_OF_DAY, hour);
		// calendar.set(Calendar.MINUTE, minute);
		// calendar.set(Calendar.SECOND, 0);
		// Log.v("lakala",calendar.getTime().toLocaleString());
		// return calendar.getTimeInMillis();
	}

	public static Message createCallbackMsg(String msg, String callback, String jsresponse) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.msg.msg, msg);
		bundle.putString(Constant.msg.callback, callback);
		bundle.putString(Constant.msg.jsresponse, jsresponse);
		message.setData(bundle);
		return message;
	}

	public static Message createCallbackMsg(String msg, boolean result, String reason) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.msg.msg, msg);
		bundle.putBoolean("result", result);
		bundle.putString("reason", reason);
		message.setData(bundle);
		return message;
	}

	/*
	 * handler杩斿洖鏁版嵁澶勭悊
	 */
	public static Message createMsg(Bundle b) {
		Message message = new Message();
		Bundle bundle = new Bundle();
		// bundle.putString(Constant.msg.msg,msg);
		// bundle.put(Constant.msg.jsresponse, reJsResponse.getData());
		bundle.putAll(b);
		message.setData(bundle);
		return message;
	}

	/**
	 * 计算MAC域
	 * 
	 * @param domainJson
	 * @return
	 * @throws Exception
	 */
	public static String getMacBlock(List domainList) throws Exception {
		if (domainList == null || domainList.isEmpty()) {
			throw new IllegalArgumentException("domain json is null");
		}
		StringBuffer macBlock = new StringBuffer();
		Iterator it = domainList.iterator();
		while (it.hasNext()) {
			String temp = (String) it.next();
			// 判断域是否为空，若为空，则不选择
			if (temp == null || "".equals(temp.trim()) || temp.equals("null") || temp.equals("undefined")) {
				continue;
			} else {
				String macdomain = getMacDomain(temp);
				if (macdomain.equals("")) {
					continue;
				} else {
					macBlock.append(macdomain);
					if (it.hasNext()) {
						macBlock.append(" ");
					}
				}
			}
		}
		Log.d("MAB", "MAB=" + macBlock.toString());
		// byte[] orgMacBlock = macBlock.toString().getBytes();
		// byte[] filledMacBlock = SecurityUtil.fillBytes(orgMacBlock);
		return macBlock.toString();
	}

	/**
	 * 银行卡号校验规则
	 * 
	 * @param cardno
	 * @return
	 */
	public static int PaymentENumCheck(String cardno) {
		char[] ChkData = cardno.toCharArray();
		int ChkDataLen = cardno.length();
		int i;
		int sum = 0;
		int tmp1, tmp2;
		int chk = 10;
		chk = ChkData[ChkDataLen - 1] - '0';
		for (i = ChkDataLen - 2; i >= 0; i--) {
			tmp2 = 0;
			tmp1 = ChkData[i] - '0';
			if ((ChkDataLen - 2 - i) % 2 == 0) {
				tmp1 *= 2;
				if (tmp1 / 10 != 0) {
					tmp2 = tmp1 % 10;
					tmp1 /= 10;
				}
			}
			sum += tmp1 + tmp2;
		}
		if (chk > 9 || chk < 0 || ((10 - sum % 10) % 10 != chk)) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * 过滤MAC域
	 * 
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public static String getMacDomain(String source) throws Exception {
		if (source == null || "".equals(source.trim())) {
			throw new IllegalArgumentException("source data empty get domain fail");
		}
		// 取出左右空格
		String temp = source.trim();
		StringBuffer destString = new StringBuffer();
		char[] srcCharArr = temp.toCharArray();
		for (int i = 0; i < srcCharArr.length; i++) {
			char tc = srcCharArr[i];
			// 筛选指定字符
			if ((tc <= 'Z' && tc >= 'A') || (tc <= 'z' && tc >= 'a') || (tc <= '9' && tc >= '0') || (tc == ' ')
					|| (tc == ',') || (tc == '.')) {
				destString.append(tc);
			}
		}
		return destString.toString();
	}

	/**
	 * 根据IMSI号码获取运营商类型
	 * 
	 * @param imsi
	 * @return
	 * @throws Exception
	 */
	public static int getNetWorkType(String imsi) throws Exception {
		if (imsi == null || imsi.length() != 15) {
			throw new IllegalArgumentException("非法的imsi号码.....");
		}
		if (imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007")) {
			return Constant.CHINA_CMCC;
		} else if (imsi.startsWith("46001")) {
			return Constant.CHINA_UNICOME;
		} else if (imsi.startsWith("46003")) {
			return Constant.CHINA_TELECOME;
		} else {
			return Constant.UNKNOWN_NET;
		}
	}

	/**
	 * 根据运营商类型获取不同的IP地址
	 * 
	 * @return
	 */
	// public static String getIp(Context context,String defaultIp){
	// try
	// {
	// log.debug("开始获取IP地址....");
	// int netype = Config.getInstance(context).getNetWorkType();
	// log.debug("获取运营商类型 ==============["+netype+"]");
	// switch(netype){
	// case Constant.CHINA_CMCC: return
	// Config.getInstance(context).getConfig("ydip", defaultIp) ;
	// case Constant.CHINA_TELECOME: return
	// Config.getInstance(context).getConfig("dxip", defaultIp) ;
	// case Constant.CHINA_UNICOME: return
	// Config.getInstance(context).getConfig("ip", defaultIp) ;
	// case Constant.UNKNOWN_NET: return
	// Config.getInstance(context).getConfig("ip", defaultIp) ;
	// default:return Config.getInstance(context).getConfig("ip", defaultIp) ;
	// }
	// }catch(Exception e){
	// log.error("获取IP地址出错.....................", e);
	// return Config.getInstance(context).getConfig("ip", defaultIp) ;
	// }
	// }

	/**
	 * 根据运营商类型获取不同的端口
	 * 
	 * @return
	 */
	/*
	 * public static int getPort(Context context,int defaultPort){ try {
	 * log.debug("开始获取端口...."); int netype =
	 * Config.getInstance(context).getNetWorkType(); log.debug(
	 * "获取运营商类型 ==============["+netype+"]"); switch(netype){ case
	 * Constant.CHINA_CMCC: return
	 * Integer.parseInt(Config.getInstance(context).getConfig("ydport",
	 * String.valueOf(defaultPort))) ; case Constant.CHINA_TELECOME: return
	 * Integer.parseInt(Config.getInstance(context).getConfig("dxport",
	 * String.valueOf(defaultPort))) ; case Constant.CHINA_UNICOME: return
	 * Integer.parseInt(Config.getInstance(context).getConfig("port",
	 * String.valueOf(defaultPort))) ; case Constant.UNKNOWN_NET: return
	 * Integer.parseInt(Config.getInstance(context).getConfig("port",
	 * String.valueOf(defaultPort))) ; default:return
	 * Integer.parseInt(Config.getInstance(context).getConfig("port",
	 * String.valueOf(defaultPort))) ; } }catch(Exception e){
	 * log.error("获取端口出错.....................", e); return
	 * Integer.parseInt(Config.getInstance(context).getConfig("port",
	 * String.valueOf(defaultPort))) ; } }
	 */

	/**
	 * 临时函数,替代小波的DataConverter.jsonStrToMap函数bug
	 * 
	 * @param jsonStr
	 * @return
	 */
	public static Map jsonStrToMap(String jsonStr) {
		log.info("jsonStrToMap =[" + jsonStr + "]");
		Map<String, String> map = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject(jsonStr);
			Iterator it = json.keys();
			while (it.hasNext()) {
				String key = String.valueOf(it.next());
				String value = String.valueOf(json.get(key));
				map.put(key, value);
			}
		} catch (JSONException e) {
			log.error("json字符串转map发生异常,json=[" + jsonStr + "]", e);
		}
		return map;
	}

	/*
	 * HashMap转String
	 */
	public static String mapTojsonStr(Map<String, String> map) {
		String string = "{";
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Entry e = (Entry) it.next();
			string += "'" + e.getKey() + "':";
			string += "'" + e.getValue() + "',";
		}
		string = string.substring(0, string.lastIndexOf(","));
		string += "}";
		return string;
	}

	/**
	 * @末尾填充FF长度满8字节整数倍
	 * @return
	 */
	public static String fillBackChar(String src, char character) {
		if (src == null || src.length() % 16 == 0) {
			return src;
		}
		StringBuffer sbf = new StringBuffer(src);
		while (sbf.length() % 16 != 0) {
			sbf.append(character);
		}
		return sbf.toString(); // 将补位后的值返回
	}

	/*
	 * 数字不足6位，左边补“0”
	 */
	public static String addZeroForNum(String str, int strLength) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				sb.append("0").append(str);// 左补0
				// sb.append(str).append("0");//右补0
				str = sb.toString();
				strLen = str.length();
			}
		}
		return str;
	}

	/*
	 * 0000000001250转化成12.50
	 */
	public static String unformatMount(String mount) {
		if ("".equals(mount) || mount == null) {
			return "0.00";
		}
		double money = (double) (Long.parseLong(mount) * 0.01);
		if (money > 0) {
			DecimalFormat df = new DecimalFormat("##0.00");
			Log.i("ckh", "unformatMount == " + df.format(money));
			return df.format(money);

		} else {
			return "0.00";
		}
	}

	/*
	 * 12.50转化成0000000001250
	 */
	public static String formatMount(String mount) {
		if ("".equals(mount) || mount == null) {
			return "000000000000";
		}
		// float fmoney = Float.valueOf(mount)*100;
		mount = mount.replace(".", "");
		Log.i("ckh", "formatMount == " + addZeroForNum(mount, 12));
		return addZeroForNum(mount, 12);
	}

	/*
	 * 将卡号中间部分用*代替显示
	 */
	public static String formatCardno(String cardno) {
		if (cardno.length() < 16 || cardno == null || "".equals(cardno)) {
			return cardno;
		}
		String midString = "********************".substring(0, cardno.length() - 10);
		String preString = cardno.substring(0, 6);
		String lasString = cardno.substring(cardno.length() - 4, cardno.length());
		return preString + midString + lasString;
	}

	/*
	 * 获取系统当前日期，转化成组包12、13域数据
	 */
	public static String getTransLocalDate() {
		Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料

		t.setToNow(); // 取得系统时间
		String month = String.valueOf(t.month);
		if (!"".equals(month) && month != null) { // 月份取出来要加1
			month = String.valueOf(Integer.valueOf(month) + 1);
		}
		String date = String.valueOf(t.monthDay);
		if (month.length() < 2) {
			month = "0" + month;
		}
		if (date.length() < 2) {
			date = "0" + date;
		}
		return month + date;
	}

	/*
	 * 获取系统当前时间，转化成组包12、13域数据
	 */
	public static String getTransLocalTime() {
		Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料

		t.setToNow(); // 取得系统时间
		String hour = String.valueOf(t.hour); // 0-23
		String minute = String.valueOf(t.minute);
		String second = String.valueOf(t.second);
		if (hour.length() < 2) {
			hour = "0" + hour;
		}
		if (minute.length() < 2) {
			minute = "0" + minute;
		}
		if (second.length() < 2) {
			second = "0" + second;
		}
		return hour + minute + second;
	}

	// 打印凭条上面的时间
	public static String printFormatDateTime(String datetime) {
		Calendar c = Calendar.getInstance();
		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = datetime.substring(0, 2);
		String day = datetime.substring(2, 4);

		String hour = datetime.substring(4, 6);
		String min = datetime.substring(6, 8);
		String sec = datetime.substring(8, 10);

		datetime = year + "/" + month + "/" + day + " " + hour + ":" + min + ":" + sec;

		return datetime;
	}

	/**
	 * 格式化日期，如：“131031”转化成“2013/10/31”
	 * 
	 * @param date
	 * @return
	 */
	public static String formatDate(String date) {
		String year = date.substring(0, 2);
		String month = date.substring(2, 4);
		String day = date.substring(4, 6);

		return "20" + year + "/" + month + "/" + day;
	}

	/**
	 * 格式化时间，如:"011119"转化成“01:11:19”
	 * 
	 * @param time
	 * @return
	 */
	public static String formatTime(String time) {
		String hour = time.substring(0, 2);
		String min = time.substring(2, 4);
		String sec = time.substring(4, 6);

		return hour + ":" + min + ":" + sec;
	}

	/**
	 * 方法描述：执行自动签到操作
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-6 下午10:34:23
	 */
	public static Intent autoSign() {
		Transaction autoSignTransaction = new Transaction();
		autoSignTransaction.setMctCode("002308");
		List<ComponentNode> componentNodeList = new ArrayList<ComponentNode>();
		ComponentNode progressNode = new ComponentNode();
		progressNode.setComponentId("1");
		progressNode.setComponentName("com.lkl.farmer.progress");
		componentNodeList.add(progressNode);
		autoSignTransaction.setComponentNodeList(componentNodeList);

		Intent autoSignIntent = new Intent();
		autoSignIntent.setAction(progressNode.getComponentName());
		autoSignIntent.putExtra("transaction", autoSignTransaction);
		autoSignIntent.putExtra("isAuto", true);
		return autoSignIntent;
	}

	/**
	 * 执行自动脱机上送
	 */
	public static Intent autoUpOfflineSale() {
		Intent autoUpIntent = new Intent();
		Transaction autoUpTransaction = new Transaction();
		autoUpTransaction.setMctCode("002326");
		List<ComponentNode> componentNodeList = new ArrayList<ComponentNode>();
		ComponentNode progressNode = new ComponentNode();
		progressNode.setComponentId("1");
		progressNode.setComponentName("com.lkl.farmer.progress");
		componentNodeList.add(progressNode);
		autoUpTransaction.setComponentNodeList(componentNodeList);

		autoUpIntent.setAction(progressNode.getComponentName());
		autoUpIntent.putExtra("transaction", autoUpTransaction);
		autoUpIntent.putExtra("isAuto", true);
		return autoUpIntent;
	}

	/**
	 * 方法描述：执行操作反激活
	 * 
	 * @createtor：Xrh
	 * @date 2015-11-26 09:17:26
	 */
	public static Intent autoAuti() {
		Transaction autoAuti = new Transaction();
		autoAuti.setMctCode("002329");
		List<ComponentNode> componentNodeList = new ArrayList<ComponentNode>();
		ComponentNode progressNode = new ComponentNode();
		progressNode.setComponentId("1");
		progressNode.setComponentName("com.lkl.farmer.progress");
		componentNodeList.add(progressNode);
		autoAuti.setComponentNodeList(componentNodeList);

		Intent autoAutiIntent = new Intent();
		autoAutiIntent.setAction(progressNode.getComponentName());
		autoAutiIntent.putExtra("transaction", autoAuti);
		autoAutiIntent.putExtra("isAuto", true);
		return autoAutiIntent;
	}

	// 设置签到状态
	public static void setSignStatus(Context mContext, boolean status) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		mParamConfigDao.update("signsymbol", status ? "1" : "0");
	}

	// 获取签到状态
	public static boolean getSignStatus(Context mContext) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		return "1".equals(mParamConfigDao.get("signsymbol")) ? true : false;
	}

	// 设置结算状态
	public static void setSettleStatus(Context mContext, boolean status) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		mParamConfigDao.update("settlesymbol", status ? "1" : "0");
	}

	// 获取结算状态
	public static boolean getSettleStatus(Context mContext) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		return "1".equals(mParamConfigDao.get("settlesymbol")) ? true : false;
	}

	// 设置打印状态
	public static void setPrintStatus(Context mContext, String status) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		mParamConfigDao.update("printsymbol", status);
	}

	// 获取打印状态
	public static String getPrintStatus(Context mContext) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		String status = mParamConfigDao.get("printsymbol");
		return status;
	}

	// 获取外部版本号
	public static String getVersion(Context mContext) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(mContext);
		return mParamConfigDao.get("printAppVersionId");
	}

	// 读取"conf/errcode.xml"目录中的错误信息数据
	public static List<ErrInfo> getErrInfoFromFile(Context context) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			ErrInfoHandler handler = new ErrInfoHandler();
			reader.setContentHandler(handler);
			InputSource is = new InputSource(context.getAssets().open("conf/errcode.xml"));
			reader.parse(is);
			return handler.getErrInfos();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// 计算字符串中含中文字符串长度
	/*
	 * public static int getLength(String str){ str = str.replaceAll(
	 * "[^x00-xff]" , "xx" ); return str.length(); }
	 */
	public static int length(String value) {
		int valueLength = 0;
		String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int i = 0; i < value.length(); i++) {
			/* 获取一个字符 */
			String temp = value.substring(i, i + 1);
			/* 判断是否为中文字符 */
			if (temp.matches(chinese)) {
				/* 中文字符长度为2 */
				valueLength += 2;
			} else {
				/* 其他字符长度为1 */
				valueLength += 1;
			}
		}
		return valueLength;
	}

	/*
	 * 用途：字符串格式化输出,固定长度前补空格 输入：data字符串,长度length 返回：data
	 * 
	 * @20130717 modify by chenkehui
	 */
	public static String printFillSpace(String str, int length) {

		int str_length = length(str);
		if (str_length < length) {
			for (; str_length < length; length--) {
				str = " " + str;
			}
		} else {
			str = str.substring(0, length);
		}
		return str;
	}

	/*
	 * 字符串str，左补空格/右补空格
	 */
	public static String strFillSpace(String str, int strLength, boolean isLeft) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				if (isLeft) {
					sb.append(" ").append(str);
				} else {
					sb.append(str).append(" ");
				}
				str = sb.toString();
				strLen = str.length();
			}
		}
		return str;
	}

	/*
	 * 打印结算信息时，交易笔数格式
	 */
	public static String printInteger(String intStr) {
		intStr = String.valueOf(Integer.valueOf(intStr));
		return intStr;
	}

	/**
	 * 结算成功之后自动签退功能
	 */
	public static void autoSingOut(final Context context) {
		/*
		 * Thread thread = new Thread () {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 */ setSignStatus(context, false); // 设置为签退
		setSettleStatus(context, false); // 设置为无结算

		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(context);
		TransRecordDao mTransRecordDao = new TransRecordDaoImpl(context);
		SettleDataDao mSettleDataDao = new SettleDataDaoImpl(context);
		String batchno = mParamConfigDao.get("batchno");
		if ("".equals(batchno) || batchno == null) {
			batchno = "000001";
		}
		long bot = Long.valueOf(batchno); // 批次号加1
		if (bot >= 999999) {
			bot = 1;
		} else {
			bot = bot + 1;
		}
		String botString = addZeroForNum(String.valueOf(bot), 6);
		mParamConfigDao.update("batchno", botString);

		mTransRecordDao.deleteAll(); // 清空交易流水表
		/*
		 * }
		 * 
		 * }; thread.start();
		 */
	}

	/**
	 * add for修复结算成功打印凭条，结算状态和打印状态切换时断电导致状态判断出错bug by chenkehui @20130821
	 * 主要功能：标记签退，标记为非结算状态，标记为有结算数据未打印状态，批次号加1 清除交易流水
	 */
	public static void settleSucessful(final Context context) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(context);
		String batchno = mParamConfigDao.get("batchno");
		if ("".equals(batchno) || batchno == null) {
			batchno = "000001";
		}
		long bot = Long.valueOf(batchno); // 批次号加1
		if (bot >= 999999) {
			bot = 1;
		} else {
			bot = bot + 1;
		}
		String botString = addZeroForNum(String.valueOf(bot), 6);
		Map<String, String> updateMap = new HashMap<String, String>();
		updateMap.put("signsymbol", "0");
		updateMap.put("settlesymbol", "0");
		updateMap.put("printsymbol", "settle");
		updateMap.put("batchno", botString);
		mParamConfigDao.update(updateMap);
		updateMap = null;
		TransRecordDao mTransRecordDao = new TransRecordDaoImpl(context);
		mTransRecordDao.deleteAll(); // 清空交易流水表
	}

	/**
	 * 查询流水表中交易条数是否达到上限
	 */

	public static boolean isMaxCount(Context context) {
		int count = 0;
		int maxCount = 0;
		TransRecordDao mTransRecordDao = new TransRecordDaoImpl(context);
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(context);
		count = mTransRecordDao.getTransCount();

		String maxStr = mParamConfigDao.get("systracemax");
		if ("".equals(maxStr) || maxStr == null) {
			maxCount = 500; // 当批次最大笔数上限为空时，默认为500
		} else {
			maxCount = Integer.valueOf(maxStr);
		}
		return count >= maxCount;
	}

	public static String getPinPadDevSymbol(Context context) {
		ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl(context);
		return mParamConfigDao.get("pinpadType");
	}

	/**
	 * 手输入卡号格式化
	 * 
	 * @param carno
	 * @author Xrh @20130815
	 * @return
	 */
	public static String formatCardNo(String cardno) {
		if (cardno == null || "".equals(cardno)) {
			return cardno;
		}
		log.debug("待格式化卡号 ：cardno = " + cardno);
		cardno = cardno.replaceAll(" ", "");
		log.debug("待格式化卡号 ：cardno = " + cardno);

		if (cardno.indexOf("-") != -1) {
			return cardno;
		}
		int size = ((cardno.length()) % 4 == 0) ? ((cardno.length()) / 4) : ((cardno.length()) / 4 + 1);

		String card = "";

		for (int i = 0; i < size; i++) {
			int endIndex = (i + 1) * 4;
			if ((i + 1) == size) {
				endIndex = cardno.length();
			}
			if (i == 0) {
				card += cardno.substring(i, endIndex);
			} else {
				card += "  " + cardno.substring(i * 4, endIndex);
			}
		}

		log.debug("格式化后卡号 ：card = " + card);
		return card;
	}

	/*
	 * 不同交易类型对应完整或者简化pboc流程
	 */
	public static Map<String, Boolean> transTypeMap = new HashMap<String, Boolean>() {
		{
			put("002301", true);
			put("002302", true);
			put("002303", false);
			put("002313", true);
			put("002314", false);
			put("002315", false);
			put("002316", false);
			put("002317", false);
			put("002322", true);
		}
	};

	public static boolean isThatMsg(Message message, String msg) {
		return msg.equals((message.getData().getString(Constant.msg.msg)));
	}

	/**
	 * 获取基站信息
	 * 
	 * @param context
	 * @return
	 */
	public static String getBaseStationInfo(Context context) {

		String g3ModuleType = PropertiesUtil.getSystemProperties("ct.3g.product");
		String baseStationInfo = "";
		try {
			if ("MU509".equals(g3ModuleType)) { // 联通、 华为
				/** 调用API获取基站信息 */
				TelephonyManager mTelNet = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				GsmCellLocation location = (GsmCellLocation) mTelNet.getCellLocation();
				if (location == null)
					return null;

				String operator = mTelNet.getNetworkOperator();
				if (operator == null || "".equals(operator)) {
					return null;
				}
				byte mnc = Byte.parseByte(operator.substring(3));
				int cid = location.getCid();
				int lac = location.getLac();
				String MNC = HexUtil.byteToHex(mnc);

				String LAC = HexUtil.bcd2str(Integer.toHexString(lac).getBytes());
				String CID = HexUtil.bcd2str(Integer.toHexString(cid).getBytes());
				log.warn("cid :" + cid + "   lac :" + lac + "   CID :" + CID + "    LAC :" + LAC);
				baseStationInfo = MNC + LAC + CID;

			} else if ("MC509".equals(g3ModuleType) || "MC2716".equals(g3ModuleType)) { // 电信、华为
																						// //电信、
																						// 中兴

				BaseStationDao bsd = BaseStationFactory.getBaseStationInstance(context);
				// add by txb 20140626 防止不装3G卡应用挂掉
				if (null != bsd) {
					String bs = bsd.getCurrBs(); // MCC + SID+ NID+BID
					log.warn("获取基站 BS = " + bs);

					String[] baseIdArray = bs.split("-");

					String SID = Integer.toHexString(Integer.parseInt(baseIdArray[1]));
					// String NID =
					// HexUtil.byteToHex(Byte.valueOf(baseIdArray[2]));
					String NID = Integer.toHexString(Integer.parseInt(baseIdArray[2]));
					String BID = Integer.toHexString(Integer.parseInt(baseIdArray[3]));

					baseStationInfo = SID + NID + BID;
				}

			} else if (g3ModuleType.equals("AD3812")) { // 联通 、中兴

			} else { // 未知
			}
		} catch (Exception e) {
			log.error("获取基站信息失败", e);
			e.printStackTrace();
		}

		log.info("baseStationInfo = " + baseStationInfo);
		return baseStationInfo;
	}

	// 获取终端SN号用于收单一键激活
	public static String getLklCposSN() {

		String sn = null;
		try {
			SystemDevInf sdi = DeviceFactory.getSystemDev();
			if (sysForSH()) {
				// sn = sdi.readManuSerialNum();
				sn = sdi.readSerialNum((byte) 0x0e);
			} else {
				sn = sdi.readSerialNum();
			}
			log.info("SN = " + sn);
			log.info("SN1 = " + sdi.readSerialNum());
			log.info("SN2 = " + sdi.readSerialNum((byte) 0x0e));
		} catch (Exception e) {
			log.error("获取系统设备号异常", e);
			e.printStackTrace();
		}
//		return "YP610000000011";// 测试使用的SN
		return sn;
	}

	public static boolean sysForSH() {
		log.debug("系统版本号:[" + android.os.Build.BRAND + "]");
		return android.os.Build.BRAND.contains("Src");// 上海特有的版本号包含Src
	}

	/**
	 * 获取系统版本
	 * 
	 * @return
	 */
	public static String getSystemVersion() {
		log.info("系统版本号：[" + android.os.Build.BRAND + "]");
		return android.os.Build.BRAND;
	}

	// 生成随机密钥
	public static String getCryptData() {
		char[] ascii = "0123456789ABCDEF".toCharArray();
		Random rd = new Random();
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < 16; i++) {
			Integer index = rd.nextInt(16);
			res.append(ascii[index]);
		}
		return res.toString();
	}

	// 生成随机密钥
	public static byte[] getCryptByte() {
		byte[] data = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
		byte[] random = new byte[8];
		for (int i = 0; i < 8; i++) {
			Integer h = new Random().nextInt(16);
			Integer l = new Random().nextInt(16);
			random[i] = (byte) (data[h] << 4 | l);
		}
		return random;
	}

	public static boolean check1850Version(Context context) {
		ParamConfigDao paramConfigDao = new ParamConfigDaoImpl(context);
		String pinpadType = paramConfigDao.get("pinpadType");
		GetPINPadVersion ver = null;
		boolean flag = true;
		if ("0".equals(pinpadType)) { // 外置暂无要求

		} else { // 内置
			String appver = "0.00";
			try {
				PinPadDevJsIfc pindev = new PinPadDevJsIfc(context, null);
				ver = pindev.getVersion();
				log.debug("BootHardVer :" + ver.getBootHardVer());
				log.debug("BootSoftVer :" + ver.getBootSoftVer());
				log.debug("BootBuildTime :" + ver.getBootBuildTime());
				log.debug("AppHardVer :" + ver.getAppHardVer());
				log.debug("AppSoftVer :" + ver.getAppSoftVer());
				log.debug("AppBuildTime :" + ver.getAppBuildTime());
				String appSoftVer = ver.getAppSoftVer();
				appver = appSoftVer.substring(10, 14);
				log.debug("appSoftVer " + appSoftVer);
				log.debug("appver " + appver);
			} catch (Exception e) {
				log.error("获取内置密码键盘版本异常", e);
			}
			// 1850版本小于1.04不支持无checkValue,要求升级
			if (Double.parseDouble(appver) < 1.04) {
				log.debug("不支持无checkValue");
				flag = false;
			}
		}
		return flag;
	}

	// 判断是否有需要结算的数据
	public static boolean isNeedSettle(Context context) {
		TransRecordDao transRecordDao = new TransRecordDaoImpl(context);
		int count = transRecordDao.getTransCount();
		return count > 0 ? true : false;
	}

	// 判断脱机交易是否已达自动上送积累笔数(默认10笔)的限制
	public static boolean isAutoUp(Context context) {
		TransRecordDao transRecordDao = new TransRecordDaoImpl(context);
		int count = transRecordDao.getOfflineSaleCount();
		return count >= 10 ? true : false;
	}

	// 配置应用参数数据
	public static String makeParamToEMV(Context context) {
		ParamConfigDao paramConfigDao = new ParamConfigDaoImpl(context);
		String mchntname = paramConfigDao.get("mchntname"); // 商户名称
		if ("".equals(mchntname) || mchntname == null)
			return null;
		String tag = "9F4E";
		String mchntname_hex = null;
		String len = null;
		try {
			mchntname_hex = HexUtil.bcd2str(mchntname.getBytes("GBK"));
			len = Integer.toHexString(mchntname.getBytes("GBK").length);
			if (len.length() % 2 != 0) {
				len = "0" + len;
			}
			log.info("商户名称 tlv = " + tag + len + mchntname_hex);
			return tag + len + mchntname_hex;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error("配置应用参数数据异常");
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 判断当前终端
	 * 
	 * @return I代\II代分别返回1,2
	 */
	public static int currentDevice() {
		String brand = android.os.Build.BRAND; // 系统版本号
		if (brand.startsWith("1") || brand.startsWith("2")) { // I代终端
			return 1;
		} else if (brand.startsWith("3")) { // II代终端
			return 2;
		}
		return 1;
	}

	// 保存到shared_prefs
	public static void saveShared_prefs(Context context, Map<String, String> map) {
		SharedPreferences sp = context.getSharedPreferences("OneKeyActivate", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor mEditor = sp.edit();
		for (String key : map.keySet()) {
			mEditor.putString(key, map.get(key));
		}
		mEditor.commit();
	}

	public static void clearShared_prefs(Context context) {
		SharedPreferences sp = context.getSharedPreferences("OneKeyActivate", Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor mEditor = sp.edit();
		mEditor.clear();
		mEditor.commit();
	}

	public boolean checkApkExist(Context context, String packageName) {

		if (TextUtils.isEmpty(packageName)) {
			return false;
		}
		final PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo = null;
		try {
			packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (packageInfo == null) {
			log.debug(packageName + " is not install ");
			return false;
		} else {
			log.debug(packageName + " is installed");
			return true;
		}
	}
	
	/**
	 * 获取当前时间戳
	 * 
	 * @return
	 */
	public static String getTimeStamp() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(new Date());
		return time;
	}

}
