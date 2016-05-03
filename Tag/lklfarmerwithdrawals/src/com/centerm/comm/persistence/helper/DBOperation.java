package com.centerm.comm.persistence.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.entity.ParamConfig;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.LklcposApplication;
import com.centerm.lklcpos.util.MD5Util;
import com.lkl.farmerwithdrawals.R;

public class DBOperation {

	private static final Logger log = Logger.getLogger(DBOperation.class);

	private Context context;
	private DBOpenHelper openHelper;
	private ParamConfigDao mParamConfigDao;

	private SQLiteDatabase db;

	public DBOperation() {
		this.context = LklcposApplication.lklcposAppContext;
		openHelper = DBOpenHelper.getInstance(this.context);
		mParamConfigDao = new ParamConfigDaoImpl(this.context);
		this.db = openHelper.getReadableDatabase();
	}

	public DBOperation(Context context) {
		this.context = context;
		openHelper = DBOpenHelper.getInstance(this.context);
		mParamConfigDao = new ParamConfigDaoImpl(this.context);
		this.db = openHelper.getReadableDatabase();
	}

	@SuppressLint("NewApi")
	public int dropAllTable() {
		int result = 0;
		log.debug("删除数据库表开始 :db = " + openHelper.getDatabaseName());
		// SQLiteDatabase db = openHelper.getReadableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("drop table if exists param_config");
			db.execSQL("drop table if exists transrecord");
			db.execSQL("drop table if exists settledata");
			db.execSQL("drop table if exists reverse");
			result = 1;
		} catch (Exception e) {
			log.error("删除数据库异常", e);
			result = -1;
		} finally {
			db.endTransaction();
			db.close();
		}
		log.debug("删除数据库表结束 = " + result);
		return result;
	}

	@SuppressLint("NewApi")
	public void buildAllTable() {
		SQLiteDatabase db = openHelper.getReadableDatabase();
		log.debug("创建数据库表开始 :db = " + openHelper.getDatabaseName());
		openHelper.onCreate(db);
	}

	public int rebuildTable() {
		int result = mParamConfigDao.dropTable();
		if (1 == result) {
			// buildAllTable();
			new Thread() {
				@Override
				public void run() {
					long begin = System.currentTimeMillis();
					posparamToSqlite();
					sysparamToSqlite();
					log.debug("数据库重构之后同步耗时:" + (System.currentTimeMillis() - begin) + "ms");
				}
			}.start();
		}
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		return result;
	}

	public void posparamToSqlite() {
		long startime = System.currentTimeMillis();
		File xmlfile = null;
		try {
			xmlfile = new File(context.getResources().getString(R.string.baseSystemUri) + "conf/posparam.xml");
			Log.i("ckh", "xmlfile.exists() == " + xmlfile.exists());
			if (!xmlfile.exists()) {
				log.warn("posparam.xml不存在,参数不同步...");
				Log.i("ckh", "!xmlfile.exists()...");
				if ("1".equals(mParamConfigDao.get("posparamsymbol"))) {
					Log.i("ckh", "dialog");
					mParamConfigDao.update("posparamsymbol", "0");
					Intent intent = new Intent();
					intent.setAction("com.centerm.lklcpos.posparam");
					// sendBroadcast(intent);
				}
				// createXml();
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("ckh", "catch exception...");
			log.error("", e);
			return;
		}
		ParamConfigDaoImpl paramConfigDao = new ParamConfigDaoImpl(this.context);
		String md5 = paramConfigDao.get("paramXmlMd5"); // 获取数据库中保存的MD5值

		if (md5 != null) {
			try {
				String md5String = MD5Util.getFileMD5(xmlfile);
				if (md5.equals(md5String)) {
					Log.i("ckh", "文件MD5值与数据库一致,不需要同步");
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("", e);
			}
		}
		Log.i("ckh", "开始同步配置到sqlite数据库 .....");
		log.info("开始同步配置到sqlite数据库 .....");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream ins = null;
			ins = new FileInputStream(
					new File(context.getResources().getString(R.string.baseSystemUri) + "conf/posparam.xml"));
			Document doc = builder.parse(ins);
			Node root = doc.getDocumentElement();

			List<ParamConfig> list = new ArrayList<ParamConfig>();
			if (root.hasChildNodes()) {
				NodeList rlist = root.getChildNodes();

				for (int i = 0, j = rlist.getLength(); i < j; i++) {
					Node childNode = rlist.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String key = childNode.getNodeName();
						String value = childNode.getTextContent();
						Log.i("ckh", "开始同步数据key= " + key + "&& value= " + value);
						if ("termid".equals(key) || "merid".equals(key) || "mchntname".equals(key)
								|| "dealtimeout".equals(key) || "reconntimes".equals(key) || "tpdu".equals(key)
								|| "batchno".equals(key) || "billno".equals(key) || "transIp".equals(key)
								|| "caIp".equals(key) || "pinpadType".equals(key) || "fid".equals(key)
								|| "MerchantSev".equals(key) || "MasterKeySev".equals(key)) {

							ParamConfig config = new ParamConfig();
							config.setTagname(key);
							config.setTagval(value);
							list.add(config);

							// if("200021".equals(paramConfigDao.get("fid"))){
							// //测试
							if ("test".equals(paramConfigDao.get("env"))) { // 测试
								if ("transIp".equals(key)) {
									list.add(new ParamConfig("test_3gapn_tranUrl", value));
								}
								if ("caIp".equals(key)) {
									list.add(new ParamConfig("test_3gapn_certUrl", value));
								}
								if ("MerchantSev".equals(key)) {
									list.add(new ParamConfig("test_3gapn_mercUrl", value));
								}
							} else {
								if ("transIp".equals(key)) {
									list.add(new ParamConfig("produce_3gapn_tranUrl", value));
								}
								if ("caIp".equals(key)) {
									list.add(new ParamConfig("produce_3gapn_certUrl", value));
								}
								if ("MerchantSev".equals(key)) {
									list.add(new ParamConfig("proc_3gapn_mercUrl", value));
								}
							}

							// if (paramConfigDao.isExist(key)) {
							// paramConfigDao.update(key, value);
							// } else {
							// paramConfigDao.save(key, value);
							// }
						}
					}
				}
			}
			int num = paramConfigDao.syncXmlUpdate(list);
			log.info("同步XML参数成功数量 = " + num);

			if (ins != null) {
				ins.close();
				ins = null;
			}

			try {
				String fileMd5 = MD5Util.getFileMD5(xmlfile);
				int saveResult = 0;
				if (paramConfigDao.isExist("paramXmlMd5")) {
					saveResult = paramConfigDao.update("paramXmlMd5", fileMd5);
				} else {
					saveResult = paramConfigDao.save("paramXmlMd5", fileMd5);
				}
				Log.i("ckh", saveResult + "更新数据库MD5值 = [" + fileMd5 + "]");
			} catch (IOException e1) {
				log.error("计算posparam.xml文件MD5值失败..");
			}

			Log.i("ckh", "同步配置到sqlite数据库结束 .....");
			log.info("同步posparam.xml耗时：" + (System.currentTimeMillis() - startime));
		} catch (Exception e) {
			log.error("同步配置到sqlite数据库发生异常.....", e);
			e.printStackTrace();
		}
	}

	public void sysparamToSqlite() {
		long startime = System.currentTimeMillis();

		InputStream ins = null;
		try {
			ins = context.getResources().getAssets().open("conf/sysparam.xml");
			if (ins == null) {
				Log.i("ckh", "!file.exists()...");
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("", e);
		}

		ParamConfigDaoImpl paramConfigDao = new ParamConfigDaoImpl(this.context);
		String md5 = paramConfigDao.get("sysparamXmlMd5"); // 获取数据库中保存的MD5值

		if (md5 != null) {
			try {
				String md5String = MD5Util.getInputStreamMD5(ins);

				if (ins != null) {
					ins.close();
					ins = null;
				}

				if (md5.equals(md5String)) {
					Log.i("ckh", "文件MD5值与数据库一致,不需要同步");
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("", e);
			}
		}

		Log.i("ckh", "开始同步配置到sqlite数据库 .....");
		log.info("开始同步配置到sqlite数据库 .....");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			ins = context.getResources().getAssets().open("conf/sysparam.xml");
			Document doc = builder.parse(ins);
			Node root = doc.getDocumentElement();

			List<ParamConfig> list = new ArrayList<ParamConfig>();
			if (root.hasChildNodes()) {
				NodeList rlist = root.getChildNodes();

				for (int i = 0, j = rlist.getLength(); i < j; i++) {
					Node childNode = rlist.item(i);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String key = childNode.getNodeName();
						String value = childNode.getTextContent();
						Log.i("ckh", "开始同步sysparam数据key= " + key + "&& value= " + value);
						if ("dealtimeout".equals(key) || "logParentpath".equals(key) || "standbytimeout".equals(key)
								|| "logpath".equals(key) || "rootLogLevel".equals(key) || "islogFile".equals(key)
								|| "logLevel".equals(key) || "swipecard_timeout".equals(key)
								|| "pinpad_timeout".equals(key) || "pswdlength".equals(key) || "httpEncode".equals(key)
								|| "printAppVersionId".equals(key) || "tmkKeyId_setOff".equals(key)) {

							ParamConfig config = new ParamConfig();
							config.setTagname(key);
							config.setTagval(value);
							list.add(config);

							// if (paramConfigDao.isExist(key)) {
							// paramConfigDao.update(key, value);
							// } else {
							// paramConfigDao.save(key, value);
							// }
						}
					}
				}
			}
			int num = paramConfigDao.syncXmlUpdate(list);
			log.info("同步XML参数成功数量 = " + num);

			try {
				if (ins != null) {
					ins.close();
					ins = null;
				}
				ins = context.getResources().getAssets().open("conf/sysparam.xml");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("", e);
			}
			try {
				String fileMd5 = MD5Util.getInputStreamMD5(ins);
				if (ins != null) {
					ins.close();
					ins = null;
				}
				int saveResult = 0;
				if (paramConfigDao.isExist("sysparamXmlMd5")) {
					saveResult = paramConfigDao.update("sysparamXmlMd5", fileMd5);
				} else {
					saveResult = paramConfigDao.save("sysparamXmlMd5", fileMd5);
				}
				Log.i("ckh", saveResult + "更新数据库sysparam MD5值 = [" + fileMd5 + "]");

			} catch (IOException e1) {
				log.error("计算sysparam.xml文件MD5值失败..");
			}
			log.info("同步sysparam.xml耗时：" + (System.currentTimeMillis() - startime));
			Log.i("ckh", "同步配置到sqlite数据库结束 .....");
		} catch (Exception e) {
			log.error("同步配置到sqlite数据库发生异常.....", e);
			e.printStackTrace();
		}
	}

}
