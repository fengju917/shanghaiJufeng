package com.centerm.lklcpos.transaction.entity;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;

/*
 * 主菜单上面的快捷方式
 */
public class Shortcut {

	private int trade_icon_id; // 图标资源id
	private int trade_text_id; // 字体资源id
	private String filePath = null; // 交易配置文件名
	private String power = null; // 权限标志 ， “oper”：运维人员权限；“admin”：管理员权限
	private Intent mIntent = null; // 快捷方式启动Intent
	private Transaction mTransaction; // 交易流程
	private boolean judgestate = false; // 是否进行状态的判断
	private boolean isNative = false;
	private boolean judgeMaxTransRecords = false; // 是否需要判断流水记录到达上限
	private Context mContext;

	public Shortcut() {

	}

	public Shortcut(int icon_id, int text_id) {
		trade_icon_id = icon_id;
		trade_text_id = text_id;
		filePath = null;
	}

	public Shortcut(Context context, int icon_id, int text_id, String xmlFile) {
		trade_icon_id = icon_id;
		trade_text_id = text_id;
		filePath = xmlFile;
		mContext = context;
	}

	public Shortcut(Context context, String xmlFile) {
		filePath = xmlFile;
		mContext = context;
	}

	public boolean isNative() {
		return isNative;
	}

	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}

	public boolean isJudgestate() {
		return judgestate;
	}

	public void setJudgestate(boolean judgestate) {
		this.judgestate = judgestate;
	}

	public boolean isJudgeMaxTransRecords() {
		return judgeMaxTransRecords;
	}

	public void setJudgeMaxTransRecords(boolean judgeMaxTransRecords) {
		this.judgeMaxTransRecords = judgeMaxTransRecords;
	}

	public int getTrade_icon_id() {
		return trade_icon_id;
	}

	public void setTrade_icon_id(int trade_icon_id) {
		this.trade_icon_id = trade_icon_id;
	}

	public int getTrade_text_id() {
		return trade_text_id;
	}

	public void setTrade_text_id(int trade_text_id) {
		this.trade_text_id = trade_text_id;
	}

	public Transaction getmTransaction() {
		if (filePath != null) {
			return geTransactionFromFileTransaction();
		}
		return mTransaction;
	}

	public void setmTransaction(Transaction mTransaction) {
		this.mTransaction = mTransaction;
	}

	public String getPower() {
		return power;
	}

	public void setPower(String power) {
		this.power = power;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	// 若filePath不为空，交易配置文件存在，读取配置流程，获取第一个节点作为启动的第一个组件
	public Intent getmIntent() {
		if (filePath != null && mIntent == null) {
			mIntent = new Intent();
			mIntent.setAction(getmTransaction().getFirstComponentNode().getComponentName());// 对应配置文件中的activity类
			// mIntent.setPackage("com.lkl.farmerwithdrawals");
			// mIntent.addCategory("com.lkl.help.farmers");
			mIntent.putExtra("transaction", getmTransaction()); // 组件之间传递交易流程对象
		}
		return mIntent;
	}

	public void setmIntent(Intent mIntent) {
		this.mIntent = mIntent;
	}

	// 从配置文件中读出交易流程
	private Transaction geTransactionFromFileTransaction() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			TransactionHandler handler = new TransactionHandler();
			reader.setContentHandler(handler);
			InputSource is = new InputSource(this.mContext.getAssets().open(filePath));// 取得本地xml文件
			reader.parse(is);
			return handler.getTransaction();
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
}
