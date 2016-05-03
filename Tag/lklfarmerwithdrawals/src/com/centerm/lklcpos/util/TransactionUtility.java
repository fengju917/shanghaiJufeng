/**
 * 
 */
package com.centerm.lklcpos.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhui @da2013-7-3
 *
 */
public class TransactionUtility {

	/**
	 * 把TransRecord转HashMap
	 * 
	 * @param record
	 * @return
	 */
	public static Map<String, String> transformToMap(Object record) {
		Map<String, String> map = new HashMap<String, String>();
		if (record == null)
			return null;
		java.lang.reflect.Field[] fields = record.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			String key = fields[i].getName();
			if (fields[i].getType().getName().equals(java.lang.String.class.getName())) {
				// String 类型
				try {
					String value = String.valueOf(fields[i].get(record));
					map.put(key, value);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

		}
		return map;
	}

}
