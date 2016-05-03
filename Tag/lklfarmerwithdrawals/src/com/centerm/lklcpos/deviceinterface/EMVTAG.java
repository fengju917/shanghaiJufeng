package com.centerm.lklcpos.deviceinterface;

import java.io.ByteArrayOutputStream;

public class EMVTAG {
	public static final byte[] EMVTAG_APP_PAN = combine(0x5A);
	public static final byte[] EMVTAG_APP_PAN_SN = combine(0x5F, 0x34);
	public static final byte[] EMVTAG_TRACK2 = combine(0x57);
	public static final byte[] EMVTAG_AC = combine(0x9F, 0x26);
	public static final byte[] EMVTAG_CID = combine(0x9F, 0x27);
	public static final byte[] EMVTAG_IAD = combine(0x9F, 0x10); // Issuer
																	// Application
																	// Data
	public static final byte[] EMVTAG_RND_NUM = combine(0x9F, 0x37); // Random
																		// Number
	public static final byte[] EMVTAG_ATC = combine(0x9F, 0x36);
	public static final byte[] EMVTAG_TVR = combine(0x95);
	public static final byte[] EMVTAG_TXN_DATE = combine(0x9A);
	public static final byte[] EMVTAG_TXN_TYPE = combine(0x9C);
	public static final byte[] EMVTAG_AMOUNT = combine(0x9F, 0x02);
	public static final byte[] EMVTAG_CURRENCY = combine(0x5F, 0x2A);
	public static final byte[] EMVTAG_AIP = combine(0x82);
	public static final byte[] EMVTAG_COUNTRY_CODE = combine(0x9F, 0x1A);
	public static final byte[] EMVTAG_OTHER_AMOUNT = combine(0x9F, 0x03);
	public static final byte[] EMVTAG_TERM_CAP = combine(0x9F, 0x33);
	public static final byte[] EMVTAG_CVM = combine(0x9F, 0x34);
	public static final byte[] EMVTAG_TERM_TYPE = combine(0x9F, 0x35);
	public static final byte[] EMVTAG_IFD = combine(0x9F, 0x1E);
	public static final byte[] EMVTAG_DF = combine(0x84);
	public static final byte[] EMVTAG_APP_VER = combine(0x9F, 0x09);
	public static final byte[] EMVTAG_TXN_SN = combine(0x9F, 0x41);
	public static final byte[] EMVTAG_CARD_ID = combine(0x9F, 0x63);
	public static final byte[] EMVTAG_AID = combine(0x4F);
	public static final byte[] EMVTAG_SCRIPT_RESULT = combine(0xDF, 0x31);
	public static final byte[] EMVTAG_ARC = combine(0x8A);
	public static final byte[] EMVTAG_ISS_COUNTRY_CODE = combine(0x5F, 0x28);
	public static final byte[] EMVTAG_EC_AUTH_CODE = combine(0x9F, 0x74);
	public static final byte[] EMVTAG_EC_BALANCE = combine(0x9F, 0x79);
	public static final byte[] EMVTAG_TSI = combine(0x9B);
	public static final byte[] EMVTAG_APP_LABEL = combine(0x50);
	public static final byte[] EMVTAG_APP_NAME = combine(0x9F, 0x12);
	public static final byte[] EMVTAG_CONTACT_NAME = combine(0x9F, 0x4E); // 商户名称

	private static byte[] combine(int... bytes) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(4);
		for (int i = 0; i < bytes.length; i++) {
			bout.write(bytes[bytes.length - i - 1]);
		}
		for (int i = 0; i < (4 - bytes.length); i++) {
			bout.write(0);
		}
		return bout.toByteArray();
	}

	/**
	 * 脚本结果通知上送报文中F55
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaScriptResultTag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x95));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x82));
		ous.write(combine(0xDF, 0x31));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9A));
		return ous.toByteArray();
	}

	public static byte[] getReadCardInfoTag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x5A));
		ous.write(combine(0x57));
		ous.write(combine(0x5F, 0x34));
		return ous.toByteArray();
	}

	/**
	 * 拉卡拉接口55域用法一 用于 联机消费
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaF55UseModeOneForOnlineSale() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x27));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x9C));
		ous.write(combine(0x9F, 0x02));
		ous.write(combine(0x5F, 0x2A));
		ous.write(combine(0x82));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x03));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x34));
		ous.write(combine(0x9F, 0x35));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x84));
		ous.write(combine(0x9F, 0x09));
		ous.write(combine(0x9F, 0x41));
		// ous.write(combine(0x8A)); // 联机消费，读取出8Atag值是，拉卡拉后台返回格式错误，故注掉
		ous.write(combine(0x9F, 0x74));
		ous.write(combine(0x91));
		ous.write(combine(0x71));
		ous.write(combine(0x72));
		return ous.toByteArray();
	}

	/**
	 * 拉卡拉接口55域用法一 用于 余额查询、消费、预授权
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaF55UseModeOne() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x27));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x9C));
		ous.write(combine(0x9F, 0x02));
		ous.write(combine(0x5F, 0x2A));
		ous.write(combine(0x82));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x03));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x34));
		ous.write(combine(0x9F, 0x35));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x84));
		ous.write(combine(0x9F, 0x09));
		ous.write(combine(0x9F, 0x41));
		ous.write(combine(0x8A));
		ous.write(combine(0x9F, 0x74));
		ous.write(combine(0x91));
		ous.write(combine(0x71));
		ous.write(combine(0x72));
		return ous.toByteArray();
	}

	/**
	 * 拉卡拉接口55域用法二 消费、预授权冲正
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaF55UseModeTwo() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0xDF, 0x31));
		return ous.toByteArray();
	}

	/**
	 * 拉卡拉接口55域用法三 脚本通知（接口中未用到）
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaF55UseModeThree() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x82));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0xDF, 0x31));
		return ous.toByteArray();
	}

	/**
	 * 现金充值、指定账户圈存、非指定账户圈存上送报文F55
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaTransferF55Tag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x27));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x9C));
		ous.write(combine(0x9F, 0x02));
		ous.write(combine(0x5F, 0x2A));
		ous.write(combine(0x82));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x03));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x34));
		ous.write(combine(0x9F, 0x35));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x84));
		ous.write(combine(0x9F, 0x09));
		ous.write(combine(0x9F, 0x41));
		ous.write(combine(0x9F, 0x63));
		ous.write(combine(0x91));
		ous.write(combine(0x71));
		ous.write(combine(0x72));
		return ous.toByteArray();
	}

	/**
	 * 现金充值撤销上送报文F55
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getLakalaCashValueVoidF55Tag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x03));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x34));
		ous.write(combine(0x9F, 0x35));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x84));
		ous.write(combine(0x9F, 0x09));
		ous.write(combine(0x9F, 0x41));
		ous.write(combine(0x9F, 0x63));
		ous.write(combine(0x91));
		ous.write(combine(0x71));
		ous.write(combine(0x72));
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x27));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x9C));
		ous.write(combine(0x9F, 0x02));
		ous.write(combine(0x5F, 0x2A));
		ous.write(combine(0x82));
		return ous.toByteArray();
	}

	/**
	 * 圈存冲正上送报文F55
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getReversalF55Tag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x95));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0xDF, 0x31));
		return ous.toByteArray();
	}

	/**
	 * 纯电子现金卡（AID号为06结尾的）
	 */
	public static byte[] getAidNo() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x4F)); // AID
		return ous.toByteArray();
	}

	/**
	 * 接触式读取内核数据，打印凭条使用
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getkernelDataForPrint() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x5F, 0x34)); // CSN
		ous.write(combine(0x4F)); // AID
		ous.write(combine(0x9F, 0x26)); // TC
		ous.write(combine(0x95)); // TVR
		ous.write(combine(0x9B)); // TSI
		ous.write(combine(0x9F, 0x36)); // ATC
		ous.write(combine(0x9F, 0x37)); // UNPR NUM
		ous.write(combine(0x82)); // AIP
		ous.write(combine(0x9F, 0x79)); // 卡片余额
		ous.write(combine(0x9F, 0x33)); // TermCap
		ous.write(combine(0x9F, 0x10)); // IAD
		ous.write(combine(0x50)); // APP LABEL
		ous.write(combine(0x9F, 0x12)); // APP PREFERRED NAME
		return ous.toByteArray();
	}

	/**
	 * 快速支付读取内核数据，打印凭条使用
	 * 
	 * @return
	 * @throws Exception
	 */
	public static byte[] getrfkernelDataForPrint() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x5F, 0x34)); // CSN
		ous.write(combine(0x4F)); // AID
		ous.write(combine(0x9F, 0x26)); // TC
		ous.write(combine(0x95)); // TVR
		ous.write(combine(0x9B)); // TSI
		ous.write(combine(0x9F, 0x36)); // ATC
		ous.write(combine(0x9F, 0x37)); // UNPR NUM
		ous.write(combine(0x82)); // AIP
		ous.write(combine(0x9F, 0x5D)); // 卡片余额
		ous.write(combine(0x9F, 0x33)); // TermCap
		ous.write(combine(0x9F, 0x10)); // IAD
		ous.write(combine(0x50)); // APP LABEL
		ous.write(combine(0x9F, 0x12)); // APP PREFERRED NAME
		return ous.toByteArray();
	}

	public static byte[] getLakalaQueryBalanceTag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x27));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x9C));
		ous.write(combine(0x9F, 0x02));
		ous.write(combine(0x5F, 0x2A));
		ous.write(combine(0x82));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x03));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x34));
		ous.write(combine(0x9F, 0x35));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x84));
		ous.write(combine(0x9F, 0x09));
		ous.write(combine(0x9F, 0x41));
		ous.write(combine(0x8A));
		ous.write(combine(0x9F, 0x74));
		ous.write(combine(0x91));
		ous.write(combine(0x71));
		ous.write(combine(0x72));
		return ous.toByteArray();
	}

	public static byte[] getBalanceTransferTag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		ous.write(combine(0x9F, 0x26));
		ous.write(combine(0x9F, 0x27));
		ous.write(combine(0x9F, 0x10));
		ous.write(combine(0x9F, 0x37));
		ous.write(combine(0x9F, 0x36));
		ous.write(combine(0x95));
		ous.write(combine(0x9A));
		ous.write(combine(0x9C));
		ous.write(combine(0x9F, 0x02));
		ous.write(combine(0x5F, 0x2A));
		ous.write(combine(0x82));
		ous.write(combine(0x9F, 0x1A));
		ous.write(combine(0x9F, 0x03));
		ous.write(combine(0x9F, 0x33));
		ous.write(combine(0x9F, 0x34));
		ous.write(combine(0x9F, 0x35));
		ous.write(combine(0x9F, 0x1E));
		ous.write(combine(0x84));
		ous.write(combine(0x9F, 0x09));
		ous.write(combine(0x9F, 0x41));
		// ous.write(combine(0x8A));圈存报文测试
		// ous.write(combine(0x9F,0x74));
		ous.write(combine(0x9F, 0x63));
		ous.write(combine(0x91));
		ous.write(combine(0x71));
		ous.write(combine(0x72));
		return ous.toByteArray();
	}

	public static byte[] getNullTag() throws Exception {
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		return ous.toByteArray();
	}

}
