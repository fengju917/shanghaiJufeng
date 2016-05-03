package com.centerm.lklcpos.util;
/**
 *  Copyright 2013, Fujian Centerm Information Co.,Ltd.  All right reserved.
 *  THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF  FUJIAN CENTERM PAY CO.,
 *  LTD.  THE CONTENTS OF THIS FILE MAY NOT BE DISCLOSED TO THIRD
 *  PARTIES, COPIED OR DUPLICATED IN ANY FORM, IN WHOLE OR IN PART,
 *  WITHOUT THE PRIOR WRITTEN PERMISSION OF  FUJIAN CENTERM PAY CO., LTD.
 *
 *  TLV函数
 *  Edit History:
 *
 *    2013/09/11 - Created by Xrh.
 *    
 *  Edit History：
 *  
 *   2013/10/22 - Modified by Xrh.
 *   L字段长度改为无符号整型
 */

import java.util.HashMap;
import java.util.Map;

public class TlvUtil {

	public static Map<String, String> tlvToMap(String tlv) {
		return tlvToMap(hexStringToByte(tlv));
	}

	/**
	 * 若tag标签的第一个字节后四个bit为“1111”,则说明该tag占两个字节 例如“9F33”;否则占一个字节，例如“95”
	 * 
	 * @param tlv
	 * @return
	 */
	public static Map<String, String> tlvToMap(byte[] tlv) {
		Map map = new HashMap<String, String>();
		int index = 0;
		while (index < tlv.length) {
			if ((tlv[index] & 0x1F) == 0x1F) { // tag双字节
				byte[] tag = new byte[2];
				System.arraycopy(tlv, index, tag, 0, 2);
				index += 2;

				int length = 0;
				if (tlv[index] >> 7 == 0) { // 表示该L字段占一个字节
					length = tlv[index]; // value字段长度
					index++;
				} else { // 表示该L字段不止占一个字节

					int lenlen = tlv[index] & 0x7F; // 获取该L字段占字节长度
					index++;

					for (int i = 0; i < lenlen; i++) {
						length = length << 8;
						length += tlv[index] & 0xff; // value字段长度 &ff转为无符号整型
						index++;
					}
				}

				byte[] value = new byte[length];
				System.arraycopy(tlv, index, value, 0, length);
				index += length;
				map.put(bcd2str(tag), bcd2str(value));
			} else {// tag单字节
				byte[] tag = new byte[1];
				System.arraycopy(tlv, index, tag, 0, 1);
				index++;

				int length = 0;
				if (tlv[index] >> 7 == 0) { // 表示该L字段占一个字节
					length = tlv[index]; // value字段长度
					index++;
				} else { // 表示该L字段不止占一个字节

					int lenlen = tlv[index] & 0x7F; // 获取该L字段占字节长度
					index++;

					for (int i = 0; i < lenlen; i++) {
						length = length << 8;
						length += tlv[index] & 0xff; // value字段长度&ff转为无符号整型
						index++;
					}
				}

				byte[] value = new byte[length];
				System.arraycopy(tlv, index, value, 0, length);
				index += length;
				map.put(bcd2str(tag), bcd2str(value));
			}
		}

		return map;
	}

	public static String bcd2str(byte[] bcds) {
		char[] ascii = "0123456789abcdef".toCharArray();
		byte[] temp = new byte[bcds.length * 2];

		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString().toUpperCase();
	}

	public static byte[] hexStringToByte(String hex) {
		hex = hex.toUpperCase();
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}

	private static byte toByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	private static class TlvExcetion extends Exception {
		public TlvExcetion(String msg) {
			super(msg);
		}
	}

	public static void main(String[] args) {
		// String tlv_s =
		// "5A0A6222620110000034687F57126222620110000034687D49122019113440715F340101";
		String tlv_s = "9F2608B100D88AEC7D259C9F2701809F101307000103A00012010A010000050000810914A49F37046E33B3E09F36021942950500000010009A031309109C01009F02060000000001005F2A02015682027C009F1A0201569F03060000000000009F3303E0F9C89F34031E03009F3501229F1E0838333230494343008408A0000003330101029F090200209F410400000238";
		/*
		 * 9F26 08 B100D88AEC7D259C 9F27 01 80 9F10 13
		 * 07000103A00012010A010000050000810914A4 9F37 04 6E33B3E0 9F36 02 1942
		 * 95 05 0000001000 9A 03 130910 9C 01 00 9F02 06 000000000100 5F2A 02
		 * 0156 82 02 7C00 9F1A 02 0156 9F03 06 000000000000 9F33 03 E0F9C8 9F34
		 * 03 1E0300 9F35 01 22 9F1E 08 3833323049434300 84 08 A000000333010102
		 * 9F09 02 0020 9F41 04 00000238";
		 */
		Map<String, String> map = tlvToMap(tlv_s);
		for (String key : map.keySet()) {
			System.out.print("key = " + key);
			System.out.println(" ||  value = " + map.get(key));
		}
		System.out.println("------------------------------------------------");

		/**
		 * 9F06 05 A000000333 9F22 01 0B DF05 04 20301231 DF06 01 01 DF07 01 01
		 * DF02 81F8
		 * CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157DF040103
		 * DF03 14 BD331F9996A490B33C13441066A09AD3FEB5F66C
		 */

		String a = "9F0605A0000003339F22010BDF050420301231DF060101DF070101DF0281F8CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157DF040103DF0314BD331F9996A490B33C13441066A09AD3FEB5F66C";
		Map<String, String> map1 = tlvToMap(a);
		for (String key : map1.keySet()) {
			System.out.print("key = " + key);
			System.out.println(" ||  value = " + map1.get(key));
		}

	}
}