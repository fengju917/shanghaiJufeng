package com.centerm.lklcpos.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FilesystemUtil {

	public static final int ERROR = -2;// ����
	public static final int NOT_FOUND = -1;// û���ҵ�
	public static final int SUCCESS = 0;// �ɹ�
	public static final int FAILURE = 1;// ʧ��
	public static final int EXCEPTION = 2;// �쳣
	public static final int STOPED = 3;// ֹͣ״̬

	/**
	 * ��rootȨ��ִ������
	 * 
	 * @param cmd
	 *            ����
	 * @return
	 */
	public static int executeCmd(String[] cmd) {
		int exitVal = FAILURE;
		Process process = null;
		try {
			Runtime rt = Runtime.getRuntime();
			process = rt.exec(cmd);
			exitVal = process.waitFor() == 0 ? SUCCESS : FAILURE;
		} catch (Exception e) {
			exitVal = EXCEPTION;
			Log.e("mtms.tm", "����ִ�з����쳣", e);
		} finally {
			try {
				if (process != null) {
					process.destroy();
				}
			} catch (Exception e) {
				Log.e("mtms.tm", "������ٷ����쳣", e);
			}
		}
		return exitVal;
	}

	/**
	 * �����ļ���
	 * 
	 * @param dirPath
	 *            �ļ��е�·��
	 * @return
	 */
	public static int makeDir(String dirPath) {
		int exitVal = FAILURE;
		String[] cmd = new String[] { "mkdir", dirPath };
		exitVal = executeCmd(cmd);
		return exitVal;
	}

	/**
	 * �ı��ļ�����Ȩ��
	 * 
	 * @param filePath
	 *            �ļ�·��
	 * @param permission
	 *            ����Ȩ��
	 * @return
	 */
	public static int chmodFile(String filePath, String permission) {
		int exitVal = FAILURE;
		String[] cmd = new String[] { "chmod", "-R", permission, filePath };
		exitVal = executeCmd(cmd);
		return exitVal;
	}

	public static int mountUSB(String devNo, String dirPath) {
		int exitVal = FAILURE;
		String[] cmd = new String[] { "mount", "-t", "vfat", devNo, dirPath };
		exitVal = executeCmd(cmd);
		return exitVal;
	}

	public static int unmountUSB(String dirPath) {
		int exitVal = FAILURE;
		String[] cmd = new String[] { "umount", dirPath };
		exitVal = executeCmd(cmd);
		return exitVal;
	}

	/**
	 * ����system����Ϊ�ɶ���д
	 * 
	 * @return int
	 */
	public static int remountToRW() {
		int exitVal = FAILURE;
		String[] cmd = new String[] { "mount", "-o", "remount", ",", "rw", "/system" };
		exitVal = executeCmd(cmd);
		return exitVal;
	}

	/**
	 * ����system����Ϊֻ��
	 * 
	 * @return int
	 */
	public static int remountToRO() {
		int exitVal = FAILURE;
		String[] cmd = new String[] { "mount", "-o", "remount", ",", "ro", "/system" };
		exitVal = executeCmd(cmd);
		return exitVal;
	}

	/**
	 * �ⲿ�洢�Ƿ����
	 * 
	 * @return
	 */
	public static boolean externalMemoryAvailable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 * ��ȡ�ֻ�RAM���ÿռ��С
	 * 
	 * @param ctx
	 * @return
	 */
	public static long getAvailableRamSize(Context ctx) {
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo ramInfo = new ActivityManager.MemoryInfo();
		am.getMemoryInfo(ramInfo);
		return ramInfo.availMem;
	}

	/**
	 * ��ȡ�ֻ�RAM�ռ��С
	 * 
	 * @return
	 */
	public static long getTotalRamSize() {
		long size = ERROR;
		String filePath = "/proc/meminfo";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath), 8192);
			String str = reader.readLine();
			reader.close();
			String[] subArr = str.split("\\s+");
			size = Long.valueOf(subArr[1]).longValue() * 1024;
		} catch (Exception e) {
			Log.e("mtms.tm", "��ȡRAM���ܴ�С�����쳣", e);
		}
		return size;
	}

	/**
	 * ��ȡ�ֻ�ROM���ÿռ��С
	 * 
	 * @return
	 */
	public static long getAvailableRomSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * ��ȡ�ֻ�ROM�ռ��С
	 * 
	 * @return
	 */
	public static long getTotalRomSize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * ��ȡ�ֻ��ⲿ���ÿռ��С
	 * 
	 * @return
	 */
	public static long getAvailableExternalMemorySize() {
		long size = ERROR;
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			size = availableBlocks * blockSize;
		}
		return size;
	}

	/**
	 * ��ȡ�ֻ��ⲿ�ռ��С
	 * 
	 * @return
	 */
	public static long getTotalExternalMemorySize() {
		long size = ERROR;
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			size = totalBlocks * blockSize;
		}
		return size;
	}

	/**
	 * ��ʽ��
	 * 
	 * @param size
	 * @return
	 */
	public static String format(long size) {
		String suffix = null;
		if (size >= 1024) {
			suffix = "KB";
			size /= 1024;
			if (size >= 1024) {
				suffix = "MB";
				size /= 1024;
				if (size >= 1024) {
					suffix = "GB";
					size /= 1024;
				}
			}
		}
		StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
		int commaOffset = resultBuffer.length() - 3;
		while (commaOffset > 0) {
			resultBuffer.insert(commaOffset, ',');
			commaOffset -= 3;
		}
		if (suffix != null)
			resultBuffer.append(suffix);
		return resultBuffer.toString();
	}

}
