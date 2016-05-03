﻿package com.centerm.comm.persistence.helper;

import org.apache.log4j.Logger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite数据的访问帮助器 作者：cxy 时间：2013.02.04
 */
public class DBOpenHelper extends SQLiteOpenHelper {

	private final Logger logger = Logger.getLogger(DBOpenHelper.class);

	private static final String DBNAME = "lklfarmer.db";// 数据库名称
	/**
	 * 数据库版本号,默认从1开始，IC卡开发时升级为2，需要数据库更新时，更改此值。
	 */
	private static final int VERSION = 1;// 数据库版本
	private static DBOpenHelper helper;// 数据库帮助器实例

	public static synchronized DBOpenHelper getInstance(Context context) {
		if (helper == null) {
			helper = new DBOpenHelper(context);
		}
		return helper;
	}

	public DBOpenHelper(Context context) {
		super(context.getApplicationContext(), DBNAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		logger.debug("创建数据库表开始");
		try {

			// ParamConfig（ParamConfig参数配置表）
			db.execSQL("CREATE TABLE IF NOT EXISTS param_config("
					// +"id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "tagname VARCHAR(32) PRIMARY KEY," + "tagval VARCHAR(128))");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('dealtimeout','60')");// 交易超时
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('standbytimeout','60')");// 待机超时,原生新添加记录
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('reversetimes','3')"); // 冲正次数
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('tpdu','6009030000')"); // TPDU
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('systracemax','500')"); // 流水上限
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('reconntimes','3')");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('redowntimes','3')");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('operatorcode','01')");// 操作员号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('operatorpwd','0000')");// 操作员密码
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('batchno','000001')");// 批次号

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('merid','')"); // 商户号860010030210008
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('mchntname','')"); // 商户名称
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('termid','')"); // 终端号
																						// 03000020
			db.execSQL(
					"INSERT INTO param_config(tagname,tagval) VALUES('operpswd','25D55AD283AA400AF464C76D713C07AD')"); // 运维密码
			db.execSQL(
					"INSERT INTO param_config(tagname,tagval) VALUES('adminpwd','E10ADC3949BA59ABBE56E057F20F883E')"); // 主管密码

			db.execSQL(
					"INSERT INTO param_config(tagname,tagval) VALUES('transIp','http://9.250.249.10:9888/LaKaLaNetSecurity/PadPosSev')"); // LKL3G网络
			db.execSQL(
					"INSERT INTO param_config(tagname,tagval) VALUES('caIp','http://9.250.249.10:9888/LaKaLaNetSecurity/MerCertDownloadSev')"); // 证书下载地址

			db.execSQL(
					"INSERT INTO param_config(tagname,tagval) VALUES('MerchantSev','http://9.250.249.10:9888/LaKaLaNetSecurity/MerchantSev')"); // 商终信息下载地址
			db.execSQL(
					"INSERT INTO param_config(tagname,tagval) VALUES('MasterKeySev','http://9.250.249.10:9888/LaKaLaNetSecurity/MasterKeySev')");// 主密钥下载地址

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('billno','000001')");
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('pinpadType','1')"); // 密码键盘类型
																								// --
																								// 0外置;1内置

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('enabled','0')");// 是否激活
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('enabled2','0')");// 是否确认激活
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('fid','YP')");// 厂商标识
																						// 测试200021
																						// ;
																						// 生产
																						// 207
																						// ;
																						// 现在只有YP
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('env','proc')");// 环境

			/** add for save settleData by chenkehui @2013.07.06 begin */
			// 打印状态，“”为无打印数据状态；“strans”为交易数据未打印完成；“settle”为结算数据未打印完成
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printsymbol','')");

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settlebatchno','')"); // 结算批次号
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('translocaldate','')"); // 结算日期
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('translocaltime','')"); // 结算时间
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('respcode','')"); // 结算结果
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('requestSettleData','')"); // 上送的结算信息
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settledata','')"); // 下发的结算信息

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('signsymbol','0')"); // 签到状态，“0”为未签到
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settlesymbol','0')"); // 结算状态，“0”为非结算状态

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('systraceno','000000')"); // pos流水号，范围1-999999

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('mkeyidsymbol','1')"); // 密钥是否改变标志
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('newmkeyid','3')"); // 新主密钥索引
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('oldmkeyid','3')"); // 旧主密钥索引

			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('posparamsymbol','1')"); // 第一次安装应用判断是否需弹出，未下发配置文件提示
			// 手续费配置
			db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('charge','5')");
			
			// 交易过程记录表
			db.execSQL("CREATE TABLE IF NOT EXISTS transrecord(" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "priaccount VARCHAR(19)," // 主账号
					+ "transprocode VARCHAR(6)," // 交易处理码
					+ "transamount  VARCHAR(12)," // 交易金额
					+ "systraceno VARCHAR(6)," // POS流水号
					+ "translocaltime VARCHAR(6)," // 交易时间
					+ "translocaldate VARCHAR(4)," // 交易日期
					+ "expireddate VARCHAR(4)," // 卡有效期
					+ "entrymode VARCHAR(3)," // POS输入方式码
					+ "seqnumber VARCHAR(3)," // 卡序列号
					+ "conditionmode VARCHAR(2)," // 服务条件码

					+ "updatecode VARCHAR(2)," // 更新标识码
					+ "track2data VARCHAR(38)," // 2磁道数据
					+ "track3data VARCHAR(104)," // 3磁道数据
					+ "refernumber VARCHAR(12)," // 系统参考号
					+ "idrespcode VARCHAR(6)," // 授权码
					+ "respcode VARCHAR(2)," // 返回码
					+ "terminalid VARCHAR(8)," // 终端号
					+ "acceptoridcode VARCHAR(15)," // 商户号
					+ "acceptoridname VARCHAR(40)," // 商户名称
					+ "addrespkey VARCHAR(64)," // 附加响应-密钥数据

					+ "adddataword VARCHAR(512)," // 附加数据-文字信息
					+ "transcurrcode VARCHAR(3)," // 交易货币代码
					+ "pindata VARCHAR(12)," // 个人密码PIN
					+ "secctrlinfo VARCHAR(16)," // 安全控制信息
					+ "balanceamount VARCHAR(26)," // 附加金额
					+ "icdata VARCHAR(255)," // IC卡数据域
					+ "adddatapri VARCHAR(100)," // 附件数据-私有
					+ "pbocdata VARCHAR(100)," // PBOC电子钱包标准的交易信息
					+ "loadparams VARCHAR(100)," // 参数下装信息
					+ "cardholderid VARCHAR(18)," // 持卡人身份证

					+ "batchbillno VARCHAR(12)," // 批次号票据号
					+ "settledata VARCHAR(126)," // 结算信息
					+ "mesauthcode VARCHAR(8)," // 消息认证码
					+ "statuscode VARCHAR(2)," // 交易结果状态码
					+ "reversetimes VARCHAR(2)," // 冲正次数
					+ "reserve1 VARCHAR(100)," // 应答的消息类型
					+ "reserve2 VARCHAR(100)," // 受理方标识码
					+ "reserve3 VARCHAR(100)," // TC、AAC、ARPC上送报文中F55
					+ "reserve4 VARCHAR(100)," // 基于PBOC的交易中需要在备注中打印的数据
					+ "reserve5 VARCHAR(100))");

			// 冲正表
			db.execSQL("CREATE TABLE IF NOT EXISTS reverse(" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "priaccount VARCHAR(19)," // 主账号
					+ "transprocode VARCHAR(6)," // 交易处理码
					+ "transamount  VARCHAR(12)," // 交易金额
					+ "systraceno VARCHAR(6)," // POS流水号
					+ "translocaltime VARCHAR(6)," // 交易时间
					+ "translocaldate VARCHAR(4)," // 交易日期
					+ "expireddate VARCHAR(4)," // 卡有效期
					+ "entrymode VARCHAR(3)," // POS输入方式码
					+ "seqnumber VARCHAR(3)," // 卡序列号
					+ "conditionmode VARCHAR(2)," // 服务条件码
					+ "updatecode VARCHAR(2)," // 更新标识码
					+ "track2data VARCHAR(38)," // 2磁道数据
					+ "track3data VARCHAR(104)," // 3磁道数据
					+ "refernumber VARCHAR(12)," // 系统参考号
					+ "idrespcode VARCHAR(6)," // 授权码
					+ "respcode VARCHAR(2)," // 返回码
					+ "terminalid VARCHAR(8)," // 终端号
					+ "acceptoridcode VARCHAR(15)," // 商户号
					+ "acceptoridname VARCHAR(40)," // 商户名称
					+ "addrespkey VARCHAR(64)," // 附加响应-密钥数据
					+ "adddataword VARCHAR(512)," // 附加数据-文字信息
					+ "transcurrcode VARCHAR(3)," // 交易货币代码
					+ "pindata VARCHAR(12)," // 个人密码PIN
					+ "secctrlinfo VARCHAR(16)," // 安全控制信息
					+ "balanceamount VARCHAR(26)," // 附加金额
					+ "icdata VARCHAR(255)," // IC卡数据域
					+ "adddatapri VARCHAR(100)," // 附件数据-私有
					+ "pbocdata VARCHAR(100)," // PBOC电子钱包标准的交易信息
					+ "loadparams VARCHAR(100)," // 参数下装信息
					+ "cardholderid VARCHAR(18)," // 持卡人身份证
					+ "batchbillno VARCHAR(12)," // 批次号票据号
					+ "settledata VARCHAR(126)," // 结算信息
					+ "mesauthcode VARCHAR(8)," // 消息认证码
					+ "statuscode VARCHAR(2)," // 交易结果状态码
					+ "reversetimes VARCHAR(2)," // 冲正次数
					+ "reserve1 VARCHAR(100)," + "reserve2 VARCHAR(100)," // 受理方标识码
					+ "reserve3 VARCHAR(100)," + "reserve4 VARCHAR(100)," + "reserve5 VARCHAR(100))");

			// 结算信息表
			db.execSQL("CREATE TABLE IF NOT EXISTS settledata(" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "priaccount VARCHAR(19)," // 主账号
					+ "transprocode VARCHAR(6)," // 交易处理码
					+ "transamount  VARCHAR(12)," // 交易金额
					+ "systraceno VARCHAR(6)," // POS流水号
					+ "translocaltime VARCHAR(6)," // 交易时间
					+ "translocaldate VARCHAR(4)," // 交易日期
					+ "expireddate VARCHAR(4)," // 卡有效期
					+ "entrymode VARCHAR(3)," // POS输入方式码
					+ "seqnumber VARCHAR(3)," // 卡序列号
					+ "conditionmode VARCHAR(2)," // 服务条件码
					+ "updatecode VARCHAR(2)," // 更新标识码
					+ "track2data VARCHAR(38)," // 2磁道数据
					+ "track3data VARCHAR(104)," // 3磁道数据
					+ "refernumber VARCHAR(12)," // 系统参考号
					+ "idrespcode VARCHAR(6)," // 授权码
					+ "respcode VARCHAR(2)," // 返回码
					+ "terminalid VARCHAR(8)," // 终端号
					+ "acceptoridcode VARCHAR(15)," // 商户号
					+ "acceptoridname VARCHAR(40)," // 商户名称
					+ "addrespkey VARCHAR(64)," // 附加响应-密钥数据
					+ "adddataword VARCHAR(512)," // 附加数据-文字信息
					+ "transcurrcode VARCHAR(3)," // 交易货币代码
					+ "pindata VARCHAR(12)," // 个人密码PIN
					+ "secctrlinfo VARCHAR(16)," // 安全控制信息
					+ "balanceamount VARCHAR(26)," // 附加金额
					+ "icdata VARCHAR(255)," // IC卡数据域
					+ "adddatapri VARCHAR(100)," // 附件数据-私有
					+ "pbocdata VARCHAR(100)," // PBOC电子钱包标准的交易信息
					+ "loadparams VARCHAR(100)," // 参数下装信息
					+ "cardholderid VARCHAR(18)," // 持卡人身份证
					+ "batchbillno VARCHAR(12)," // 批次号票据号
					+ "settledata VARCHAR(126)," // 结算信息
					+ "mesauthcode VARCHAR(8)," // 消息认证码
					+ "statuscode VARCHAR(2)," // 交易结果状态码
					+ "reversetimes VARCHAR(2)," // 冲正次数
					+ "reserve1 VARCHAR(100)," + "reserve2 VARCHAR(100)," + "reserve3 VARCHAR(100),"
					+ "reserve4 VARCHAR(100)," + "reserve5 VARCHAR(100))");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("创建数据库表异常", e);
		}
		logger.debug("创建数据库表结束");
		onUpgrade(db, 1, VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		logger.debug("旧版本: " + oldVersion + " || 新版本: " + newVersion);

		try {
			switch (oldVersion) { // 不同老版本号对应升级数据库
			case 1:
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_3gapn_actUrl','http://9.250.249.10:9211/LaKaLaNetSecurity/OneKeyActivate')"); // 激活地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_public_actUrl','http://180.166.12.106:9211/LaKaLaNetSecurity/OneKeyActivate')"); // 激活地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('produce_3gapn_actUrl','http://9.250.249.10:9888/LaKaLaNetSecurity/OneKeyActivate')"); // 激活地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('produce_public_actUrl','http://netpay.lakala.com/LaKaLaNetSecurity/OneKeyActivate')"); // 激活地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('OneKeyActivate','http://9.250.249.10:9211/LaKaLaNetSecurity/OneKeyActivate')"); // 激活地址
				db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('connect_mode','0')");// 通讯模式
			case 2: // 标准收单预留
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_3gapn_mercUrl','http://9.250.249.10:9211/LaKaLaNetSecurity/MerchantSev')"); // 商终下载地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_public_mercUrl','http://180.166.12.106:9211/LaKaLaNetSecurity/MerchantSev')"); // 商终下载地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('proc_3gapn_mercUrl','http://9.250.249.10:9888/LaKaLaNetSecurity/MerchantSev')"); // 商终下载地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('proc_public_mercUrl','http://netpay.lakala.com/LaKaLaNetSecurity/MerchantSev')"); // 商终下载地址

				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_3gapn_mastUrl','http://9.250.249.10:9211/LaKaLaNetSecurity/MasterKeySev')"); // 主密钥地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_public_mastUrl','http://180.166.12.106:9211/LaKaLaNetSecurity/MasterKeySev')"); // 主密钥地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('proc_3gapn_mastUrl','http://9.250.249.10:9888/LaKaLaNetSecurity/MasterKeySev')"); // 主密钥地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('proc_public_mastUrl','http://netpay.lakala.com/LaKaLaNetSecurity/MasterKeySev')"); // 主密钥地址

			case 3: // 标准收单预留
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('operpswd','6EB77D55F6C6EBB5EDDF310EAB6AA724')"); // 运维密码12369874

			case 4: // 标准收单预留
			case 5: // 标准收单预留
				/**
				 * 1~20//标准收单预留 20~wuxi
				 */
			case 20:
				db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('caversion','00')"); // 当前IC卡公钥版本
				db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('paramversion','00')"); // 当前IC卡参数版本
				db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('updatastatus','0')"); // 更新状态，“1”
																									// 公钥需要更新，
																									// “2”参数需要更新
				// “4” 公钥和参数都需要更新， “0”不需更新， ” “表示更新中断标志，签到后两者都更新
				// add by txb 把相关的预置卡号添加入库
				// 测试3GAPN服务器配置
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_3gapn_tranUrl','http://9.250.249.10:9211/LaKaLaNetSecurity/PadPosSev')"); // 交易地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_3gapn_certUrl','http://9.250.249.10:9211/LaKaLaNetSecurity/MerCertDownloadSev')"); // 证书地址
				// 测试公网服务器配置
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_public_tranUrl','http://180.166.12.106:9211/LaKaLaNetSecurity/PadPosSev')"); // 交易地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_public_certUrl','http://180.166.12.106:9211/LaKaLaNetSecurity/MerCertDownloadSev')"); // 证书地址
				// 测试内网服务器配置
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_inner_tranUrl','http://10.7.111.12:9111/LaKaLaNetSecurity/PadPosSev')"); // 交易地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('test_inner_certUrl','http://10.7.111.12:9111/LaKaLaNetSecurity/MerCertDownloadSev')"); // 证书地址
				// 生产公网服务器配置
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('produce_public_tranUrl','http://netpay.lakala.com/LaKaLaNetSecurity/PadPosSev')"); // 交易地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('produce_public_certUrl','http://netpay.lakala.com/LaKaLaNetSecurity/MerCertDownloadSev')"); // 证书地址
				// 生产3GAPN服务器配置
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('produce_3gapn_tranUrl','http://9.250.249.10:9888/LaKaLaNetSecurity/PadPosSev')"); // 交易地址
				db.execSQL(
						"Replace INTO param_config(tagname,tagval) VALUES('produce_3gapn_certUrl','http://9.250.249.10:9888/LaKaLaNetSecurity/MerCertDownloadSev')"); // 证书地址

				// 新增脚本表
				db.execSQL("CREATE TABLE IF NOT EXISTS scriptnotity(" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "priaccount VARCHAR(19)," // 主账号
						+ "transprocode VARCHAR(6)," // 交易处理码
						+ "transamount  VARCHAR(12)," // 交易金额
						+ "systraceno VARCHAR(6)," // POS流水号
						+ "translocaltime VARCHAR(6)," // 交易时间
						+ "translocaldate VARCHAR(4)," // 交易日期
						+ "expireddate VARCHAR(4)," // 卡有效期
						+ "entrymode VARCHAR(3)," // POS输入方式码
						+ "seqnumber VARCHAR(3)," // 卡序列号
						+ "conditionmode VARCHAR(2)," // 服务条件码
						+ "updatecode VARCHAR(2)," // 更新标识码
						+ "track2data VARCHAR(38)," // 2磁道数据
						+ "track3data VARCHAR(104)," // 3磁道数据
						+ "refernumber VARCHAR(12)," // 系统参考号
						+ "idrespcode VARCHAR(6)," // 授权码
						+ "respcode VARCHAR(2)," // 返回码
						+ "terminalid VARCHAR(8)," // 终端号
						+ "acceptoridcode VARCHAR(15)," // 商户号
						+ "acceptoridname VARCHAR(40)," // 商户名称
						+ "addrespkey VARCHAR(64)," // 附加响应-密钥数据
						+ "adddataword VARCHAR(512)," // 附加数据-文字信息
						+ "transcurrcode VARCHAR(3)," // 交易货币代码
						+ "pindata VARCHAR(12)," // 个人密码PIN
						+ "secctrlinfo VARCHAR(16)," // 安全控制信息
						+ "balanceamount VARCHAR(26)," // 附加金额
						+ "icdata VARCHAR(255)," // IC卡数据域
						+ "adddatapri VARCHAR(100)," // 附件数据-私有
						+ "pbocdata VARCHAR(100)," // PBOC电子钱包标准的交易信息
						+ "loadparams VARCHAR(100)," // 参数下装信息
						+ "cardholderid VARCHAR(18)," // 持卡人身份证
						+ "batchbillno VARCHAR(12)," // 批次号票据号
						+ "settledata VARCHAR(126)," // 结算信息
						+ "mesauthcode VARCHAR(8)," // 消息认证码
						+ "statuscode VARCHAR(2)," // 交易结果状态码
						+ "reversetimes VARCHAR(2)," // 冲正次数
						+ "reserve1 VARCHAR(100)," + "reserve2 VARCHAR(100)," // 受理方标识码
						+ "reserve3 VARCHAR(100)," + "reserve4 VARCHAR(100)," + "reserve5 VARCHAR(100))");

			case 21:
			default:
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("升级数据库表异常", e);
		}
		logger.debug("升级数据库表结束");
	}
}
