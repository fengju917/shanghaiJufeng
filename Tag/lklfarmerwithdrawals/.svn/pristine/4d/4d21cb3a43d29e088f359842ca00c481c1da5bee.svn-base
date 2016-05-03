package com.centerm.lklcpos;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.centerm.lklcpos.util.Config;
import com.centerm.lklcpos.util.FilesystemUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.ShellMoniter;
import com.centerm.mid.util.MySoundPlayer;
import com.lkl.farmerwithdrawals.R;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LklcposApplication extends Application {

	public static MySoundPlayer sounderPlayer = new MySoundPlayer();
	public static Context lklcposAppContext;

	@Override
	public void onCreate() {
		super.onCreate();
		sounderPlayer.initSounds(getApplicationContext());
		sounderPlayer.addSound(0, R.raw.success);
		sounderPlayer.addSound(1, R.raw.failure);
		sounderPlayer.addSound(2, R.raw.pineffect);

		lklcposAppContext = getApplicationContext();
		// 配置Log4j日志
		try {
			configAndroidLog4j();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				Log.e("lklcpos", "程序发生异常退出......", ex);
				Log.e("lklcpos", ex.getMessage());
			}
		});

	}

	/**
	 * 删除指定文件夹下所有文件
	 * 
	 * @param path
	 * @return
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 删除文件夹
	 * 
	 * @param folderPath
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方法描述：配置log4j日志
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-7-24 上午11:34:02
	 */
	private void configAndroidLog4j() {
		final LogConfigurator logConfigurator = new LogConfigurator();
		// 设置日志文件存放路径
		String logpath = Config.getInstance(getApplicationContext()).getConfig("logpath", null);
		String parentlogpath = Config.getInstance(getApplicationContext()).getConfig("logParentpath", null);

		boolean del = delAllFile(parentlogpath);
		Log.i("[delete]", "" + del);

		String EXTSD_LOG_FILE = "/mnt/extsd/mtms/log/lklcpos/lklcpos.log";
		String SDCARD_LOG_FILE = "/mnt/sdcard/mtms/log/lklcpos/lklcpos.log";

		String sysVsn = Utility.getSystemVersion();
		Log.i("[logpath]", "" + sysVsn);

		if (!StringUtils.isEmpty(sysVsn)) { // 一期
			if (sysVsn.startsWith("0") || sysVsn.startsWith("1") || '9' < sysVsn.charAt(0) || '0' > sysVsn.charAt(0)) {
				logpath = EXTSD_LOG_FILE;
			} else { // 二期及其以后
				logpath = SDCARD_LOG_FILE;
			}
		}

		Log.i("[logpath]", "" + logpath);

		if (parentlogpath != null) {
			try {
				ShellMoniter moniter = DeviceFactory.getShellMoniter();
				String cmd = "chmod -R 777 /data/lklcpos";
				Log.d("[command]", "" + cmd);
				int chomodResult = moniter.excuteCmd(cmd);
				Log.d("[command]", "chomodResult =" + chomodResult);
			} catch (Exception e) {
				Log.e("[command]", "chomodResult =" + e.getMessage());
				e.printStackTrace();
			}
			FilesystemUtil.chmodFile(parentlogpath, "777");
		}
		if (logpath != null)
			FilesystemUtil.chmodFile(logpath, "777");

		if (logpath != null)
			logConfigurator.setFileName(logpath);
		String rootLevl = Config.getInstance(getApplicationContext()).getConfig("rootLogLevel", "DEBUG");
		String level = Config.getInstance(getApplicationContext()).getConfig("logLevel", "ERROR");
		logConfigurator.setRootLevel(getLogLevl(rootLevl));
		logConfigurator.setLevel("org.apache", getLogLevl(level));
		logConfigurator.setMaxFileSize(1024 * 1024 * 5); // 设置日志文件大小5M
		String islogFile = Config.getInstance(getApplicationContext()).getConfig("islogFile", "true");
		logConfigurator.setUseFileAppender(islogFile.equals("true") ? true : false);// 设置成true,即可输出到文件
		logConfigurator.configure();
	}

	private Level getLogLevl(String level) {
		if (level == null) {
			return Level.INFO;
		}
		if (level.equals("DEBUG")) {
			return Level.DEBUG;
		} else if (level.equals("INFO")) {
			return Level.INFO;
		} else if (level.equals("ERROR")) {
			return Level.ERROR;
		} else if (level.equals("WARN")) {
			return Level.WARN;
		} else {
			return Level.INFO;
		}
	}
}
