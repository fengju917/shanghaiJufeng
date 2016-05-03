package com.centerm.lklcpos.deviceinterface;

import com.centerm.lklcpos.deviceinterface.ExPinPadDevJsIfc.GetPinBack;
import com.centerm.lklcpos.util.ExPinPadException;

public interface PinPadInterface {

	public String openDev(); // 打开设备（该方法异常在内部处理）

	public void openDevice() throws Exception; // 打开设备（抛出异常）

	public String closeDev(); // 关闭设备 (该方法异常在方法内部处理)

	public void closeDevice() throws Exception; // 关闭设备 （抛出异常）

	public String cancelGetPin(); // 取消输密

	public void getPinWithMethodOne(final String cardno, final String amt, final GetPinBack callback); // 监听输密

	public String disperseMak(String mackey); // 发散mac工作密钥

	public void disperseMacKey(String mackey) throws Exception; // 发散mac工作密钥

	public String dispersePik(String pinkey); // 发散pin工作密钥

	public void dispersePinKey(String pinkey) throws Exception; // 发散pin工作密钥

	public byte[] getMac(byte[] mab) throws ExPinPadException; // 计算mac

	public void display(final String ShowDataLineOne, final String ShowDataLineTwo, final GetPinBack callback); // 显示余额

	public void operDownloadMkey(byte mkeyid, final byte[] tmk) throws Exception; // 主密钥灌装

	public void openOffDev(); // 打开输入脱机pin的设备

	public void loadOffMkeyAndWkey() throws Exception; // 导入脱机pin专用的主密钥和工作密钥

	public void getOffPin(String cardno, String amt, final GetPinBack callback); // 获取脱机pin

	public void closeOffDev(); // 关闭输入脱机pin的设备
}
