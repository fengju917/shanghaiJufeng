package com.centerm.lklcpos.util;

import java.io.File;
import java.io.IOException;

/**
 * @author zhouhui @da2013-7-9
 *
 */
public class InterruptPower {

	private static InterruptPower instance;

	public static synchronized InterruptPower getInterruptPower() {

		if (instance == null) {
			instance = new InterruptPower();
		}
		return instance;
	}

	/**
	 * 是否为第一次启动
	 * 
	 * @return true为第一次启动，false不是第一次启动
	 */
	public boolean isFirst() {
		boolean result = true;
		if (hasSign()) {
			result = false;
		} else {
			if (creatSign()) {
				result = true;
			} else {
				result = true;
			}
		}

		return result;
	}

	/**
	 * 在tmp下创建名lklcposcentermSign.txt为标志
	 * 
	 * @return 返回true标志创建成功，返回false创建标志失败
	 */
	private boolean creatSign() {
		boolean result = false;
		File file = new File("/tmp/lklcposcentermSign.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
				result = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 判断是否有文件
	 * 
	 * @return true为存在，false为不存在
	 */
	private boolean hasSign() {
		boolean result = false;
		File file = new File("/tmp/lklcposcentermSign.txt");
		if (file.exists()) {
			result = true;
		}
		return result;

	}

	/**
	 * 删除标志
	 * 
	 * @return 返回0为不存在标志，1为删除成功，-1为删除失败
	 */
	public boolean deleteSign() {
		boolean result = false;
		File file = new File("/tmp/lklcposcentermSign.txt");
		if (file.exists()) {
			if (file.delete()) {
				result = true;
			} else {
				result = false;
			}
		}

		return result;

	}

}
