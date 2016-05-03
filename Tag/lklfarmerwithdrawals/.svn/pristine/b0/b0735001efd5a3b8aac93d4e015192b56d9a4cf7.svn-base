package com.centerm.lklcpos.deviceinterface;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.centerm.lklcpos.LklcposApplication;
import com.centerm.lklcpos.util.Constant;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.TlvUtil;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.exception.CentermApiException;
import com.centerm.mid.exception.CentermApiException.SocketReadTimeoutException;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.ICCardFinancialAppCmdInf;
import com.centerm.mid.inf.ICCardFinancialAppCmdInf.ICCardAppMsgReport;
import com.centerm.mid.inf.ICCardFinancialKernelCmdInf;
import com.centerm.mid.inf.ICCardLoadToKernelRequstCmdInf;
import com.centerm.mid.util.M3HexUtil;

/**
 * PBOC 接口
 * 
 * @author Administrator
 *
 */
public class PbocDevJsIfc extends AbstractDevJsIfc {

	private static final Logger log = Logger.getLogger(PbocDevJsIfc.class);

	private ICCardFinancialAppCmdInf icappCmdInf = null;
	private ICCardFinancialKernelCmdInf kernelCmdInf = null;
	private ICCardLoadToKernelRequstCmdInf loadRequestCmdInf = null;
	private boolean isSearchCard = false;
	private boolean isRunPboc = false;
	private int resetType = -1;

	public PbocDevJsIfc(Context context, Handler handler) throws Exception {
		super(context, handler);
	}

	@Override
	public void init() throws Exception {

	}

	@Override
	public String openDev() throws Exception {
		return null;
	}

	@Override
	public String closeDev() throws Exception {
		log.debug("正在关闭closeDev()。。。。。。。。。。。。。。");
		if (icappCmdInf != null) {
			log.info("正在取消检卡。。。。。。。。。。。。。");
			if (isSearchCard) {
				icappCmdInf.cancelCheckCard();// 取消检卡
				isSearchCard = false;
			}
			icappCmdInf.release();// 释放资源
			icappCmdInf = null;
		}
		if (kernelCmdInf != null) {
			if (isRunPboc) {
				isRunPboc = false;
				if (resetType == 0) {
					log.debug("插卡复位");
					kernelCmdInf.resetKernel((byte) 0);// 内核操作插卡复位
				} else if (resetType == 1) {
					log.debug("非接复位");
					kernelCmdInf.resetKernel((byte) 1);// 内核操作非接复位
				}
			}
			kernelCmdInf.release();
			// log.info("kernelCmdInf命令释放。。。。。。。。。。。。");
			kernelCmdInf = null;
		}
		if (loadRequestCmdInf != null) {
			loadRequestCmdInf.release();
			// log.info("loadRequestCmdInf命令释放。。。。。。。。。。。。");
			loadRequestCmdInf = null;
		}
		return null;
	}

	public void playSwipeCardVoice(int result) {
		if (result == 1 || result == 0) {
			LklcposApplication.sounderPlayer.playSound(result);
		}
	}

	/**
	 * 检卡接口
	 * 
	 * @param type
	 *            0x01刷磁条卡 0x02插入接触式IC卡 0x08挥非接卡
	 * @param timeout
	 *            超时时间
	 * @param callback
	 *            回调函数
	 */
	public void searchCard(final int type, final int timeout) {
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				try {
					log.debug("开始检卡..............");
					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}
					icappCmdInf.paramsSettings((byte) 0x01, (byte) 0x01);

					isSearchCard = true;
					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}
					icappCmdInf.checkCard((byte) type, (byte) timeout, new ICCardAppMsgReport() {
						@Override
						public void handleMessage(int eventid, byte[] data) {

							log.debug("检卡命令执行成功,eventid = [" + eventid + "]");
							log.debug("检卡命令执行成功,data = [" + HexUtil.bcd2str(data) + "]");

							switch (eventid) {
							case 0: // 纯磁卡刷卡事件

								break;
							case 1: // 带芯片的卡片刷卡事件

								break;
							case 2: // 接触式插卡事件
								playSwipeCardVoice(0);
								response.addData("type", "0");
								resetType = 0;
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_check_card, true, "0"));
								break;
							case 3: // 非接触式挥卡事件
								playSwipeCardVoice(0);
								response.addData("type", "1");
								resetType = 1;
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_check_card, true, "1"));
								break;
							case 4: // 检卡超时
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_check_card, false, "检卡超时"));
								break;
							case 5: // 磁道数据解析出错
								playSwipeCardVoice(1);
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_check_card, false, "检卡异常"));
								break;
							case 6:
								break;
							case 7: // 其他错误
								playSwipeCardVoice(1);
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_check_card, false, "检卡异常"));
								break;
							default:
								break;
							}
							isSearchCard = false;
						}
					});
				} catch (Exception e) {
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						Log.e("xrh", "异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));
					}

					e.printStackTrace();
					log.error("检卡发送错误....", e);
					isSearchCard = false;
					String reason = null;
					if (e instanceof SocketReadTimeoutException) {
						reason = "检卡超时";
					} else {
						reason = "检卡出错";
					}
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_check_card, false, reason));

				}
				log.debug("检卡结束..............");
			};
		}.start();
	}

	/**
	 * 停止检卡
	 * 
	 * @param callback
	 */
	public int stopSearchCard() {
		try {
			log.info("正在停止检卡。。。。。。。");
			if (icappCmdInf == null) {
				icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
			}
			if (isSearchCard) {
				icappCmdInf.cancelCheckCard();
				isSearchCard = false;
			}
			icappCmdInf.release();
			log.info("取消检卡命令icappCmdInf释放。。。。。。。。。。。。。");
			icappCmdInf = null;
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
			return 0;
		}
	}

	/**
	 * 读取卡片脱机余额
	 * 
	 * @param data
	 * @return
	 */
	public void readCardOfflineBalance(final byte data, final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					isRunPboc = true;
					byte[] balance = null;
					balance = kernelCmdInf.readCardOfflineBalance(data,
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							// TODO Auto-generated method stub
							log.debug(
									"readCardOfflineBalance() 返回 id == " + arg0 + " data == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 4: // 请求多AID应用选择
								dataMap.put("aidSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"getAIDSelect"));
								break;
							case 5: // 请求单应用
								dataMap.put("oneAIDSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"oneAIDSelect"));
								break;
							case 9: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							}
						}
					});
					kernelCmdInf.release();
					kernelCmdInf = null;
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_readCardOfflineBalance, true,
							balance == null ? null : HexUtil.bcd2str(balance)));
				} catch (Exception e) {
					log.error("readCardOfflineBalance err", e);
					e.printStackTrace();
				}
			}
		}.start();

	}

	/**
	 * 读取卡片日志
	 * 
	 * @param data
	 * @return
	 */
	public void readCardLog(final byte data, final HashMap<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					isRunPboc = true;
					byte[] cardLog = null;
					cardLog = kernelCmdInf.readCardLog(data, new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							// TODO Auto-generated method stub
							Log.d("ckh", "readCardLog() 返回 id == " + arg0 + " data == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 4: // 请求多AID应用选择
								Log.w("ckh", "请求多AID应用选择");
								dataMap.put("aidSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"getAIDSelect"));
								break;
							case 5: // 请求单应用
								Log.w("ckh", "请求单应用选择");
								dataMap.put("oneAIDSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"oneAIDSelect"));
								break;
							case 9: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							}
						}
					});
					kernelCmdInf.release();
					kernelCmdInf = null;
					log.info("读取卡片日志,cardLog=[" + (cardLog != null ? HexUtil.bcd2str(cardLog) : "") + "]");
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_readCardLog, true,
							cardLog == null ? null : HexUtil.bcd2str(cardLog)));
				} catch (Exception e) {
					log.error("readCardLog err ...", e);
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_readCardLog, false, "读取卡片异常，请重试"));
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 读卡信息 参数：dataMap存放卡片信息
	 */
	public void readICCard(final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					log.warn("读卡信息开始");
					isRunPboc = true;
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream arg1_ous = new ByteArrayOutputStream();
					arg1_ous.write(0x05);
					arg1_ous.write(0x01);
					arg1_ous.write(EMVTAG.EMVTAG_APP_PAN);
					arg1_ous.write(EMVTAG.EMVTAG_TRACK2);
					arg1_ous.write(EMVTAG.EMVTAG_APP_PAN_SN);
					kernelCmdInf.standardDebitAndCreditStartProcInf(arg1_ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							Log.d("ckh", "readICCard() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 4: // 请求多AID应用选择
								Log.w("ckh", "startProc请求多AID应用选择");
								dataMap.put("aidSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"getAIDSelect"));
								break;
							case 5: // 请求单应用
								Log.w("ckh", "startProc请求单应用选择");
								dataMap.put("oneAIDSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"oneAIDSelect"));
								break;
							case 9: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							case 128: // 执行成功
								Map<String, String> resMap = TlvUtil.tlvToMap(HexUtil.bcd2str(arg1));
								log.info(resMap.get("5A").replace("F", "") + " " + resMap.get("57") + " "
										+ resMap.get("5F34"));
								dataMap.put("priaccount", resMap.get("5A").replace("F", ""));
								dataMap.put("track2data", resMap.get("57").replace("F", ""));
								dataMap.put("seqnumber", resMap.get("5F34"));
								dataMap.put("posInputType", "051");

								// 判断是否纯电子现金卡，与后台账户有关的交易不可进行
								try {
									String aid_no = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getAidNo()));
									log.debug("判断纯电子现金卡  aidno:" + aid_no);
									if ("4F08A000000333010106".equals(aid_no)) {
										handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_read_card, false,
												"纯电子现金卡，不可联机"));
										return;
									}
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									log.error("", e);
									e.printStackTrace();
								}
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_read_card, true, ""));
								break;
							case 132: // fallback，转磁条交易
								dataMap.put("posInputType", "801");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_read_card, false, "fallback"));
								break;
							case 133: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_read_card, false, "交易终止"));
								break;
							default:
								Log.w("ckh", "未知情况");
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_read_card, false, ""));
								break;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("read cardinfo error..........................", e);
					resetPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_read_card, false, "交易终止"));
				}
				// resetPboc(); //内核复位
				log.warn("读卡信息结束");
			}
		}.start();
	}

	/**
	 * PBOC流程第一步，用于消费
	 * 
	 * @param dataMap
	 * @param type
	 *            "1"主菜单消费、 "0"电子现金普通消费
	 */

	public void cashup_startProc(final Map<String, String> dataMap, final int type) {
		new Thread() {
			public void run() {
				try {
					log.debug(" cashup_startProc called..................");
					isRunPboc = true;
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream arg1_ous = new ByteArrayOutputStream();
					arg1_ous.write(0x01); // 交易类型
					arg1_ous.write(0x00); // 助农取款不支持电子现金
					kernelCmdInf.standardDebitAndCreditStartProcInf(arg1_ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							Log.d("ckh", "cashup_startProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 0: // 请求输入金额
								String money = dataMap.get("transamount");
								log.warn("导入金额");
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									ByteArrayOutputStream ous = new ByteArrayOutputStream();
									ous.write(1);
									ous.write(formartAmount(money));
									loadRequestCmdInf.amountByUser(ous.toByteArray());// 导入金额
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("导入金额发送错误.......", e);
								}
								break;
							case 4: // 请求多AID应用选择
								Log.w("ckh", "cashup_startProc请求多AID应用选择");
								dataMap.put("aidSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"getAIDSelect"));
								break;
							case 5: // 请求单应用确定（默认选择1，确认）
								Log.w("ckh", "cashup_startProc请求单应用选择");
								dataMap.put("oneAIDSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"oneAIDSelect"));
								break;
							case 7: // 是否需要电子现金消费
								Log.w("ckh", "startProc请求电子现金选择");
								if (type == 0) {
									loadResultOfMessage((byte) 0x01);
								} else if (type == 1) {
									handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction,
											true, "CashSaleSelect"));
								}
								break;
							case 9: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							case 128: // 执行成功
								if (type == 1 && "0".equals(dataMap.get("isUseCashFun"))) {// 判断是否纯电子现金卡
									try {
										String aid_no = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getAidNo()));
										log.debug("判断纯电子现金卡  aidno:" + aid_no);
										if ("4F08A000000333010106".equals(aid_no)) {
											handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_read_card,
													false, "纯电子现金卡，不可联机"));
											return;
										}
									} catch (Exception e) {
										// TODO Auto-generated
										// catch block
										e.printStackTrace();
										log.error("", e);
									}
								}
								try {
									String cardInfo = HexUtil
											.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getReadCardInfoTag()));
									Map<String, String> resMap = TlvUtil.tlvToMap(cardInfo);
									log.info(resMap.get("5A").replace("F", "") + " " + resMap.get("57") + " "
											+ resMap.get("5F34"));
									dataMap.put("priaccount", resMap.get("5A").replace("F", ""));
									dataMap.put("track2data", resMap.get("57").replace("F", ""));
									dataMap.put("seqnumber", resMap.get("5F34"));
									dataMap.put("posInputType", "051");
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									log.error("从内核中读取卡信息异常");
									e.printStackTrace();
								}
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, true, ""));
								break;
							case 132: // fallback，转磁条交易
								dataMap.put("posInputType", "801");
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false,
										"fallback"));
								break;
							case 133: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, "交易终止"));
								break;
							default:
								Log.w("ckh", "未知情况");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, ""));
								break;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("cashup_startProc pboc error..........................", e);
					resetPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, "交易终止"));
				}
			}
		}.start();
	}

	/**
	 * PBOC完整流程第一步,用于联机交易 mode :0 不支持电子现金；1 支持电子现金，且弹出选择是否使用电子现金功能
	 */
	public void startProcTransfer(final int transType, final int mode, final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					log.debug("start pboc called..................");
					isRunPboc = true;
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					Log.i("ckh", "transType == " + transType);
					ByteArrayOutputStream arg1_ous = new ByteArrayOutputStream();
					arg1_ous.write(transType);
					if (mode == 0) {
						arg1_ous.write(0); // 不支持电子现金消费
					} else {
						arg1_ous.write(0x01); // 支持电子现金消费
					}
					kernelCmdInf.standardDebitAndCreditStartProcInf(arg1_ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							Log.d("ckh", "startProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 0: // 请求输入金额
								String money = dataMap.get("transamount");
								log.warn("导入金额");
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									ByteArrayOutputStream ous = new ByteArrayOutputStream();
									ous.write(1);
									ous.write(formartAmount(money));
									loadRequestCmdInf.amountByUser(ous.toByteArray());// 导入金额
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("导入金额发送错误.......", e);
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
									// false, ""));
								}
								break;
							case 4: // 请求多AID应用选择
								log.debug("请求多AID应用选择");
								String index = dataMap.get("AIDIndex");
								loadResultOfAIDSelect(index);
								break;
							case 5: // 请求单应用确定（默认选择1，确认）
								log.debug("请求单应用确定");
								loadResultOfMessage((byte) 1);
								break;
							case 7:
								log.debug("是否使用电子现金");
								if (mode == 1) {
									handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction,
											true, "CashSaleSelect"));
								} else {
									loadResultOfMessage((byte) 0x0); // 不使用
								}
								break;
							case 9: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							case 128: // 执行成功
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc, true, ""));
								break;
							case 132: // fallback，转磁条交易
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, "校验卡片失败，交易终止"));
								resetPboc();
								break;
							case 133: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, "交易终止"));
								resetPboc();
								break;
							default:
								log.debug("eventid 未知情况");
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, ""));
								resetPboc();
								break;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("start pboc error..........................", e);
					resetPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, "交易终止"));
				}
			}
		}.start();
	}

	// PBOC完整流程第二步，用于电子现金普通消费
	public void cashup_kernelProc(final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					log.debug("cashup_kernelProc called..................");
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.standardDebitAndCreditKernelProcInf(EMVTAG.getLakalaF55UseModeOne(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {

							Log.d("ckh", "cashup_kernelProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));

							switch (arg0) {
							case 1:// 请求导入PIN
								String data = HexUtil.bcd2str(arg1);
								log.debug("cashup_kernelProc() 请求导入PIN   pintype == " + data.substring(0, 2));
								if (!data.startsWith("03")) { // 请求输入脱机pin
									handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction,
											true, "getOfflinePin"));
								} else { // 请求输入联机pin
									pinByUser("26888888FFFFFFFF");
								}
								break;
							case 3: // 请求持卡人身份认证
								log.debug("cashup_kernelProc() 请求持卡人身份认证");
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									ByteArrayOutputStream ous = new ByteArrayOutputStream();
									ous.write(1);
									ous.write(0);
									loadRequestCmdInf.resultOfUserAuthentication(ous.toByteArray());
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("导入身份证认证错误.......", e);
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc,
									// false, ""));
								}
								break;

							case 5: // 未找到所需公钥
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"Special CAPK not Found"));
								break;
							case 129: // 允许脱机
								byte[] offlineSaleUpF55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, offlineSaleUpF55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(offlineSaleUpF55));
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true, "offline"));
								break;
							case 130: // 交易拒绝
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, "交易拒绝"));
								resetPboc();
								break;
							case 131: // 交易联机
								// 判断是否纯电子现金卡
								try {
									String aid_no = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getAidNo()));
									log.debug("判断纯电子现金卡  aidno:" + aid_no);
									if ("4F08A000000333010106".equals(aid_no)) {
										handler.sendMessage(Utility.createCallbackMsg(
												Constant.msg.msg_cashup_kernelProc, false, "纯电子现金卡，不可联机"));
										return;
									}
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
									log.error("", e);
								}
								byte[] field55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, field55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(field55));
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true, "online"));
								break;
							case 132: // fallback，转磁条卡交易
								dataMap.put("posInputType", "801");
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false,
										"fallback"));
								resetPboc();
								break;
							case 133: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, "交易终止"));
								resetPboc();
								break;
							default:
								Log.w("ckh", "未知情况");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, ""));
								resetPboc();
								break;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("cashup_kernelProc error..........................", e);
					resetPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, "交易终止"));
				}
			}
		}.start();
	}

	// PBOC完整流程第二步
	public void kernelProc(final byte[] F55taglist, final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					log.debug("kernelProc called..................");
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.standardDebitAndCreditKernelProcInf(F55taglist,
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {

							Log.d("ckh", "kernelProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));

							switch (arg0) {
							case 1:// 请求导入PIN
								String data = HexUtil.bcd2str(arg1);
								log.warn("kernelProc() 请求导入PIN   pintype == " + data.substring(0, 2));
								if (!data.startsWith("03")) { // 请求输入脱机pin
									handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction,
											true, "getOfflinePin"));
								} else { // 请求输入联机pin
									if ((!"".equals(dataMap.get("pindata")) && dataMap.get("pindata") != null)
											|| "002321".equals(dataMap.get("transType"))) {
										pinByUser("26888888FFFFFFFF"); // 已输入联机PIN，默认导入“26888888FFFFFFFF”（内核不验证联机pin）
									} else {
										pinByUser("entry"); // 未输入联机PIN
									}
								}
								break;
							case 3: // 请求持卡人身份认证
								log.debug("kernelProc() 请求持卡人身份认证");
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									ByteArrayOutputStream ous = new ByteArrayOutputStream();
									ous.write(1);
									ous.write(0);
									loadRequestCmdInf.resultOfUserAuthentication(ous.toByteArray());
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("导入身份证认证错误.......", e);
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc,
									// false, ""));
								}
								break;

							case 5: // 未找到所需公钥
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"Special CAPK not Found"));
								break;
							case 129: // 允许脱机
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, true, "offline"));
								break;
							case 130: // 交易拒绝
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, false, "交易拒绝"));
								resetPboc();
								break;
							case 131: // 交易联机
								byte[] field55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, field55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(field55));
								log.info("1field55 =" + HexUtil.bcd2str(field55));
								log.info("2field55 =" + new String(field55));
								log.info("3field55 =" + HexUtil.bcd2str(new String(field55).getBytes()));

								// 读取ARQC值
								String arqc = null;
								try {
									arqc = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.EMVTAG_AC));
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
									log.error("", e);
								}
								log.info("kernelProc读取 arqc =" + arqc);
								dataMap.put("arqc", arqc);

								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, true, "online"));
								break;
							case 132: // fallback，转磁条卡交易
								dataMap.put("posInputType", "801");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, false, "读取卡片信息失败"));
								resetPboc();
								break;
							case 133: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, false, "交易终止"));
								resetPboc();
								break;
							default:
								Log.w("ckh", "未知情况");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, false, "交易终止"));
								resetPboc();
								break;
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("kernelProc error..........................", e);
					resetPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_kernel_pboc, false, "交易终止"));
				}
			}
		}.start();
	}

	// PBOC完整流程第三步， 网络交互返回结果时调用
	public void inputOnlineRespData(final Bundle b, final int onlineResult, final boolean isTransSucess,
			final String resIcdata) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream ous = new ByteArrayOutputStream();
					ous.write(onlineResult);
					if (isTransSucess) {
						log.info("交易成功");
						ous.write(HexUtil.hexStringToByte("8A023030"));
					} else {
						log.info("交易失败");
						ous.write(HexUtil.hexStringToByte("8A023035"));
					}
					if (!"".equals(resIcdata)) {
						ous.write(HexUtil.hexStringToByte(resIcdata));
					}
					// 导入联机结果
					kernelCmdInf.inputOnlineRespDataInf(ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							Log.d("ckh",
									"inputOnlineRespData() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							if (arg0 == 128) { // 接口执行成功
								Message msg = Utility.createCallbackMsg(Constant.msg.msg_inputOnlineRespData, true, "");
								msg.getData().putBundle("bundle", b);
								handler.sendMessage(msg);
							} else if (arg0 == 133) { // 标准借贷记，交易中止
								resetPboc();
								Message msg = Utility.createCallbackMsg(Constant.msg.msg_inputOnlineRespData, false,
										"交易终止");
								msg.getData().putBundle("bundle", b);
								handler.sendMessage(msg);
							}
						}
					});
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					log.error("inputOnlineRespData error..........................", e);
					resetPboc();
					Message msg = Utility.createCallbackMsg(Constant.msg.msg_inputOnlineRespData, false, "交易终止");
					msg.getData().putBundle("bundle", b);
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	// PBOC完整流程第四步， 第三步执行成功时调用
	public void completeProc(final Bundle b, final byte[] taglist) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.standardDebitAndCreditCompleteProcInf(taglist,
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						public void handleMessage(int arg0, byte[] arg1) {

							Log.d("ckh", "completeProc() eventid == " + arg0 + "    data == " + HexUtil.bcd2str(arg1));
							try {
								// 读取内核数据，脚本处理结果
								String scriptExeResponse = HexUtil
										.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getLakalaScriptResultTag()));
								log.info("读取脚本上送数据 == " + scriptExeResponse);

								// 读取内核数据，F55用法一，用于CT或AAC、ARPC上送
								String F55Data = HexUtil
										.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getLakalaF55UseModeOne()));
								Log.i("ckh", "读取内核 F55Data == " + F55Data);

								Message msg = Utility.createCallbackMsg(Constant.msg.msg_comple_pboc, true, "");
								msg.getData().putString("scriptExeResponse", scriptExeResponse);
								msg.getData().putString("F55Data", F55Data);
								msg.getData().putInt("completeResult", arg0);
								msg.getData().putBundle("bundle", b);
								handler.sendMessage(msg);
							} catch (Exception e) {
								e.printStackTrace();
								log.error("", e);
								resetPboc();
								Message msg = Utility.createCallbackMsg(Constant.msg.msg_comple_pboc, false, "");
								msg.getData().putBundle("bundle", b);
								handler.sendMessage(msg);
							}
						}
					});
				} catch (Exception e) {
					// TODO: handle exception
					log.error("", e);
					resetPboc();
					Message msg = Utility.createCallbackMsg(Constant.msg.msg_comple_pboc, false, "");
					msg.getData().putBundle("bundle", b);
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	/**
	 * 读取内核数据
	 * 
	 * @param taglist
	 * @return
	 */
	public String readEMVData(final byte[] taglist) {
		String emvData = null;
		try {
			if (kernelCmdInf == null) {
				kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
			}
			emvData = HexUtil.bcd2str(kernelCmdInf.readKernelData(taglist));
		} catch (Exception e) {
			log.error("读取内核数据异常");
			e.printStackTrace();
		}
		return emvData;
	}

	/**
	 * 清除所有公钥
	 */
	public void clearAllCa() {
		new Thread() {
			public void run() {
				try {
					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}

					icappCmdInf.updateCAPKParams((byte) 0x03, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						Log.e("xrh", "异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));

					}
					log.error("IC卡公钥清除异常");
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 清除所有aid参数
	 */
	public void clearAllAID() {
		new Thread() {
			public void run() {
				try {
					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}

					icappCmdInf.updateAIDParams((byte) 0x03, null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						Log.e("xrh", "异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));

					}
					log.error("IC卡公钥清除异常");
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * IC卡公钥更新
	 * 
	 * @param caData
	 *            公钥TLV格式
	 */
	public void updataICCA(final String caData, final PbocCallBack pbocCallBack) {
		new Thread() {
			public void run() {
				try {
					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}

					icappCmdInf.updateCAPKParams((byte) 0x01, HexUtil.hexStringToByte(caData));
					pbocCallBack.sucessDownLoad();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						Log.e("xrh", "异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));

					}
					log.error("IC卡公钥更新异常");
					e.printStackTrace();
					pbocCallBack.exceptionDownLoad();
				}
			}
		}.start();
	}

	/**
	 * AID参数更新
	 * 
	 * @param paramData
	 *            AID参数TLV格式
	 * @param pbocCallBack
	 *            回调接口
	 */
	public void updataICParam(final String paramData, final PbocCallBack pbocCallBack) {
		new Thread() {
			public void run() {
				try {
					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}
					icappCmdInf.updateAIDParams((byte) 0x01, HexUtil.hexStringToByte(paramData));
					pbocCallBack.sucessDownLoad();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error("IC卡AID参数更新异常");
					if (e instanceof CentermApiException.IndicationException) {
						int devid = ((CentermApiException.IndicationException) e).getDevId();
						int errorID = ((CentermApiException.IndicationException) e).getEventId();
						log.error("devID : " + devid);
						log.error("errorID : " + errorID);
						Log.e("xrh", "异常退出 : devid: " + Integer.toHexString(devid & 0xFF) + "   errorID: "
								+ Integer.toHexString(errorID & 0xFF));

					}
					e.printStackTrace();
					pbocCallBack.exceptionDownLoad();
				}
			}
		}.start();
	}

	/**
	 * 将输入的明文pin转化之后导入内核, 为空则代表导入失败
	 * 
	 * @param pin
	 *            例如“123456”
	 */
	public void pinByUser(String pin) {
		String pinblock = null;
		if ("entry".equals(pin)) { // 直接按确认
			pinblock = "null";
		} else {
			pinblock = pin;
		}
		try {
			if (loadRequestCmdInf == null) {
				loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
			}
			final ByteArrayOutputStream ous = new ByteArrayOutputStream();
			if (pinblock == null) {
				ous.write(0);
				ous.write(0);
				log.warn("按取消");
			} else if ("null".equals(pinblock)) {
				log.warn("按确认");
				ous.write(1);
				//无密码也过 by qzh
				ous.write(HexUtil.hexStringToByte("26888888FFFFFFFF"));
			} else {
				ous.write(1);
				ous.write(HexUtil.hexStringToByte(pinblock));
			}
			loadRequestCmdInf.pinByUser(ous.toByteArray());
			return;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("导入PIN发送错误.......", e);
		}
	}

	/**
	 * 将输入的pin明文转化成pinblock，模式为2
	 * 
	 * @param pin
	 * @return
	 */
	private String makePinBlock(String pin) {
		if (pin == null) {
			return null;
		}
		int l = pin.length();
		String len = String.valueOf(l);
		String fstr = "FFFFFFFFFFFFFF".substring(l);

		return "2" + len + pin + fstr;
	}

	/**
	 * 导入多应用选择结果
	 * 
	 * @param index
	 *            选择的应用索引值, 为空则代表导入失败
	 */
	public void loadResultOfAIDSelect(String index) {
		try {
			if (loadRequestCmdInf == null) {
				loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
			}
			final ByteArrayOutputStream ous = new ByteArrayOutputStream();
			if (index == null) {
				ous.write(0);
				ous.write(0);
			} else {
				ous.write(1);
				ous.write(Byte.valueOf(index));
			}
			loadRequestCmdInf.resultOfAIDSelect(ous.toByteArray());
			return;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("导入多应用选择错误.......", e);
		}
	}

	/**
	 * 配置应用参数
	 * 
	 * @param data
	 *            TLV格式，下发给内核的参数
	 */
	public void appConfigData(final String data) {
		try {
			if (loadRequestCmdInf == null) {
				loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
			}
			final ByteArrayOutputStream ous = new ByteArrayOutputStream();
			if (data == null) {
				ous.write(0x00);
			} else {
				ous.write(0x01);
				ous.write(M3HexUtil.hexStringToByte(data));
			}
			loadRequestCmdInf.appConfigData(ous.toByteArray());
			return;
		} catch (Exception e) {
			log.error("appConfigData命令，发送错误.......", e);
			e.printStackTrace();
		}
	}

	/**
	 * 提示信息确认
	 * 
	 * @param result
	 *            1、确认；0、取消
	 */
	public void loadResultOfMessage(byte result) {
		try {
			if (loadRequestCmdInf == null) {
				loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
			}
			log.info("导入内核的结果：" + result);
			loadRequestCmdInf.resultOfMessageExcute(result);
			return;
		} catch (Exception e) {
			log.error("resultOfMessageExcute(提示信息确认)命令，发送错误.......", e);
			e.printStackTrace();
		}
	}

	/**
	 * 非接预处理流程
	 * 
	 * @param type
	 *            交易类型，如圈存为：0x0E
	 */
	public void rfPreProcess(final byte type, final String money) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream arg1_ous = new ByteArrayOutputStream();
					arg1_ous.write(type);// 交易类型
					kernelCmdInf.rfPretreatmentProcess(arg1_ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							log.debug("rfPreProcess() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 0x00:
								// 请求金额
								ByteArrayOutputStream ous = new ByteArrayOutputStream();
								ous.write(1);
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									ous.write(formartAmount(money));
									loadRequestCmdInf.amountByUser(ous.toByteArray());// 导入金额
								} catch (Exception e) {
									log.info("非接导入金额失败");
									handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_rf_pre_progress,
											false, "请求金额失败"));
								}
								log.info("非接导入金额成功");
								return;
							case 0x90: // ****预初始化执行成功
								log.info("预处理成功");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_rf_pre_progress, true, ""));
								break;
							case 0x95: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_rf_pre_progress, false, "交易终止"));
								break;
							case 0x96: // 采用其他界面进行交易
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_rf_pre_progress, false,
										"采用其他界面进行交易"));
								break;
							default:
								log.info("未知情况");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_rf_pre_progress, false, "交易终止"));
								break;
							}
						}
					});
				} catch (Exception e) {
					log.info("预处理失败");
				}
			}
		}.start();
	}

	/**
	 * 非接startProc接口,用于非接界面 参数：dataMap存放卡片信息
	 */
	public void cashup_rfStartProc(final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.rfStartProcInf(EMVTAG.getNullTag(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							log.debug("cashup_rfStartProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 0x02: // 请求用户选取账户类型
								log.warn("请求用户选取账户类型,未处理");
								break;
							case 0x04: // 请求多AID应用选择
								log.warn("请求多AID应用选择");
								dataMap.put("aidSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"getAIDSelect"));
								break;
							case 0x05: // 请求单应用确认
								log.warn("请求单应用确认");
								dataMap.put("oneAIDSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"oneAIDSelect"));
								break;
							case 0x07: // 请求提示信息
								log.warn("请求提示信息,未处理");
								break;
							case 0x09: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							case 0x80: // 非接标准PBOC交易
								// 判断是否纯电子现金卡
								try {
									String aid_no = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getAidNo()));
									log.debug("判断纯电子现金卡  aidno:" + aid_no);
									if ("4F08A000000333010106".equals(aid_no)) {
										handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc,
												false, "纯电子现金卡，不可联机"));
										return;
									}
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
									log.error("", e);
								}
								try { // 读取内核卡信息
									String online_cardInfo = HexUtil
											.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getReadCardInfoTag()));
									Map<String, String> resMap = TlvUtil.tlvToMap(online_cardInfo);
									log.info(resMap.get("5A").replace("F", "") + " " + resMap.get("57") + " "
											+ resMap.get("5F34"));
									dataMap.put("priaccount", resMap.get("5A").replace("F", ""));
									dataMap.put("track2data", resMap.get("57").replace("F", ""));
									dataMap.put("seqnumber", resMap.get("5F34"));
									dataMap.put("posInputType", "071");
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									log.error("从内核中读取卡信息异常");
									e.printStackTrace();
								}
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, true, "online"));
								break;
							case 0x90: // 脱机交易
								try { // 读取内核卡信息
									String offline_cardInfo = HexUtil
											.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getReadCardInfoTag()));
									log.info("offline_cardInfo == " + offline_cardInfo);
									Map<String, String> resMap = TlvUtil.tlvToMap(offline_cardInfo);
									log.info(resMap.get("5A") + " " + resMap.get("57") + " " + resMap.get("5F34"));
									if (resMap.get("5A") == null) {
										String track2data = resMap.get("57");
										String card = track2data.split("D")[0];
										resMap.put("5A", card);
									}
									dataMap.put("priaccount", resMap.get("5A").replace("F", ""));
									dataMap.put("track2data", resMap.get("57").replace("F", ""));
									dataMap.put("seqnumber", resMap.get("5F34"));
									dataMap.put("posInputType", "071");
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									log.error("从内核中读取卡信息异常");
									e.printStackTrace();
								}
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, true, "offline"));
								break;
							case 0x92: // 交易 拒绝
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, "交易拒绝"));
								break;
							case 0x94: // ***fallback，转磁条交易
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false,
										"fallback"));
								resetRfPboc();
								break;
							case 0x95: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, "交易终止"));
								break;
							case 0x96:
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false,
										"请换其他界面交易"));
								break;
							default:
								log.info("未知情况");
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, ""));
								break;
							}
						}

					});
				} catch (Exception e) {
					log.error("", e);
					e.printStackTrace();
					resetRfPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_startproc, false, ""));
				}
			}
		}.start();
	}

	/**
	 * 非接startProc接口,用于网络交互 参数：dataMap存放卡片信息
	 */
	public void rfStartProc(final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {

					if (icappCmdInf == null) {
						icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
					}
					icappCmdInf.paramsSettings((byte) 0x01, (byte) 0x01);

					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.rfStartProcInf(EMVTAG.getNullTag(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							log.debug("rfStartProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							switch (arg0) {
							case 0x02: // 请求用户选取账户类型
								log.warn("请求用户选取账户类型,未处理");
								break;
							case 0x04: // 请求多AID应用选择
								log.warn("请求多AID应用选择");
								dataMap.put("aidSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"getAIDSelect"));
								break;
							case 0x05: // 请求单应用确认
								log.warn("请求单应用确认,未处理");
								dataMap.put("oneAIDSelectData", HexUtil.bcd2str(arg1));
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"oneAIDSelect"));
								break;
							case 0x07: // 请求提示信息
								log.warn("请求提示信息,未处理");
								break;
							case 0x09: // 应用参数请求事件
								String param = Utility.makeParamToEMV(context);
								appConfigData(param);
								break;
							case 0x80: // 联机
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, true, "online"));
								break;
							case 0x90: // qPBOC流程
								byte[] offlineSaleUpF55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, offlineSaleUpF55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(offlineSaleUpF55));
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, true, "offline"));
								break;
							case 0x92: // 交易 拒绝
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, "交易拒绝"));
								break;
							case 0x94: // ***fallback，转磁条交易
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, "fallback"));
								resetRfPboc();
								break;
							case 0x95: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, "交易终止"));
								break;
							default:
								log.info("未知情况");
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, ""));
								break;
							}
						}

					});
				} catch (Exception e) {
					log.error("", e);
					e.printStackTrace();
					resetRfPboc();
					handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc, false, ""));
				}
			}
		}.start();
	}

	/**
	 * 非接kernelProc接口
	 * 
	 * @param dataMap
	 */
	public void rfkernelProc(final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.rfKernelProcInf(EMVTAG.getLakalaF55UseModeOne(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							// TODO Auto-generated method stub
							log.debug("rfkernelProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));
							byte[] field55 = null;
							switch (arg0) {
							case 0x01: // 请求导入PIN
								String data = HexUtil.bcd2str(arg1);
								log.warn("rfkernelProc() 请求导入PIN   pintype == " + data.substring(0, 2));
								if (!data.startsWith("03")) { // 请求输入脱机pin
									handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction,
											true, "getOfflinePin"));
								} else { // 请求输入联机pin
									if ((!"".equals(dataMap.get("pindata")) && dataMap.get("pindata") != null)
											|| "000001".equals(dataMap.get("transType"))) {
										pinByUser("26888888FFFFFFFF"); // 已输入联机PIN，默认导入“26888888FFFFFFFF”（内核不验证联机pin）
									} else {
										pinByUser("entry"); // 未输入联机PIN
									}
								}
								break;
							case 0x05: // 未找到所需公钥
								log.warn("未找到所需公钥");
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_emv_interaction, true,
										"Special CAPK not Found"));
								break;
							case 0x06: // 强迫联机确认
								log.warn("强迫联机确认，未处理");
								break;
							case 0x07: // 请求提示信息
								log.warn("请求提示信息, 未处理");
								break;

							case 0x83: // 非接PBOC联机, 获取F55
								// 判断是否纯电子现金卡
								try {
									String aid_no = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getAidNo()));
									log.debug("判断纯电子现金卡  aidno:" + aid_no);
									if ("4F08A000000333010106".equals(aid_no)) {
										handler.sendMessage(Utility.createCallbackMsg(
												Constant.msg.msg_cashup_kernelProc, false, "纯电子现金卡，不可联机"));
										return;
									}
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
									log.error("", e);
								}

								// 冲正时所用F55数据
								log.info("0x83  非接标准PBOC联机接受 run -----------------------");
								field55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, field55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(field55));
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true, "online"));
								break;
							case 0x85: // 非接PBOC联机交易拒绝
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, "交易拒绝"));
								break;
							case 0x91: // 交易接受
								log.info("0x91 脱机接受 run -----------------------");
								field55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, field55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(field55));
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true, "offline"));
								break;
							case 0x92: // 交易拒绝
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, "交易拒绝"));
								break;
							case 0x93: // QPBOC，交易联机
								// 判断是否纯电子现金卡
								try {
									String aid_no = HexUtil.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getAidNo()));
									log.debug("判断纯电子现金卡  aidno:" + aid_no);
									if ("4F08A000000333010106".equals(aid_no)) {
										handler.sendMessage(Utility.createCallbackMsg(
												Constant.msg.msg_cashup_kernelProc, false, "纯电子现金卡，不可联机"));
										return;
									}
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									e.printStackTrace();
									log.error("", e);
								}

								field55 = new byte[arg1.length - 21];
								System.arraycopy(arg1, 21, field55, 0, arg1.length - 21);
								dataMap.put("icdata", HexUtil.bcd2str(field55));
								try { // 读取内核卡信息
									String online_cardInfo = HexUtil
											.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getReadCardInfoTag()));
									log.info("online_cardInfo == " + online_cardInfo);
									Map<String, String> resMap = TlvUtil.tlvToMap(online_cardInfo);
									String track2data = resMap.get("57").replace("F", "");
									String priaccount = track2data.substring(0, track2data.indexOf("D"));
									dataMap.put("priaccount", priaccount);
									dataMap.put("track2data", track2data);
									dataMap.put("seqnumber", resMap.get("5F34"));
									log.info("seqnumber == " + resMap.get("5F34"));
									dataMap.put("posInputType", "071");
								} catch (Exception e) {
									// TODO Auto-generated catch
									// block
									log.error("从内核中读取卡信息异常");
									e.printStackTrace();
								}
								try {
									if (icappCmdInf == null) {
										icappCmdInf = DeviceFactory.getICCardFinancialAppDev();
									}
									icappCmdInf.paramsSettings((byte) 0x01, (byte) 0x00);

								} catch (Exception e) {
									// TODO: handle exception
								}

								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true,
										"QPBOConline"));
								break;
							case 0x94: // fallback,转磁条卡交易
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false,
										"fallback"));
								break;
							case 0x95: // 交易终止
								handler.sendMessage(
										Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false, "交易终止"));
								break;
							case 0x96: // 采用其他界面进行交易
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false,
										"采用其他界面进行交易"));
								break;
							case 0x97: // QPBOC应用失效，交易拒绝
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false,
										"QPBOC应用失效，交易拒绝"));
								break;
							case 0x98: // 黑名单卡，交易拒绝
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, false,
										"黑名单卡，交易拒绝"));
								break;
							case 0x99: // QPBOC联机交易
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true,
										"QPBOConline"));
								break;
							case 0x9a: // MSD联机交易
								handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_cashup_kernelProc, true,
										"MSDonline"));
								break;
							}
						}

					});
				} catch (Exception e) {
					// TODO: handle exception
					log.error("", e);
				}
			}
		}.start();
	}

	// 【非接调用】 PBOC完整流程 非接最后一步
	public void rfCompleteProc(final Bundle b, final Map<String, String> dataMap) {
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					kernelCmdInf.rfCompleteProcInf(EMVTAG.getLakalaScriptResultTag(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {

						public void handleMessage(int arg0, byte[] arg1) {
							log.debug("rfCompleteProc() 执行id == " + arg0 + "  返回数据 == " + HexUtil.bcd2str(arg1));

							try {
								// 读取内核数据，脚本处理结果
								String scriptExeResponse = HexUtil
										.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getLakalaScriptResultTag()));
								Log.i("ckh", "读取内核 scriptData == " + scriptExeResponse);

								// 读取内核数据，F55用法一，用于CT或AAC、ARPC上送
								String F55Data = HexUtil
										.bcd2str(kernelCmdInf.readKernelData(EMVTAG.getLakalaF55UseModeOne()));
								Log.i("ckh", "读取内核 F55Data == " + F55Data);

								Message msg = Utility.createCallbackMsg(Constant.msg.msg_comple_pboc, true, "");
								msg.getData().putString("scriptExeResponse", scriptExeResponse);
								msg.getData().putString("F55Data", F55Data);
								msg.getData().putInt("completeResult", arg0);
								msg.getData().putBundle("bundle", b);
								handler.sendMessage(msg);
							} catch (Exception e) {
								// TODO Auto-generated catch
								// block
								e.printStackTrace();
								log.error("", e);
							}
						}
					});
				} catch (Exception e) {
					log.error("", e);
					// TODO: handle exception
					resetRfPboc();
					Message msg = Utility.createCallbackMsg(Constant.msg.msg_comple_pboc, false, "");
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	/**
	 * IC卡公钥、参数更新回调接口
	 * 
	 * @author chenkehui
	 */
	public interface PbocCallBack {
		void sucessDownLoad();

		void exceptionDownLoad();
	}

	// ================================ 以下为北京便民参考代码 ========================

	/**
	 * 读取卡片信息
	 * 
	 * @param callback
	 *            回调函数
	 */
	public void readCardInfo(final String pinblock, final String callback) {
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				try {
					log.debug("start pboc called..................");
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream arg1_ous = new ByteArrayOutputStream();
					arg1_ous.write(5);
					arg1_ous.write(0);// 不支持电子现金
					// arg1_ous.write(EMVTAG.EMVTAG_APP_PAN);
					// arg1_ous.write(EMVTAG.EMVTAG_TRACK2);
					// arg1_ous.write(EMVTAG.EMVTAG_APP_PAN_SN);
					kernelCmdInf.standardDebitAndCreditStartProcInf(arg1_ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							response.setSuc(true, "接收事件成功...");
							log.debug("start pboc handlemessage  arg 1 ==============" + HexUtil.bcd2str(arg1));
							if (arg0 == 0x05) {
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									loadRequestCmdInf.resultOfMessageExcute((byte) 1);
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("发送错误.......", e);
									response.setSuc(false, "发生错误........");
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
									// callback,
									// response.toJson()));
								}
							} else if (arg0 == 0x80) {
								try {
									kernelCmdInf.standardDebitAndCreditKernelProcInf(EMVTAG.getReadCardInfoTag(),
											new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
										@Override
										public void handleMessage(int arg0, byte[] arg1) {
											response.setSuc(true, "接收事件成功...");
											if (arg0 == 0x01) { // 请求导入PIN
												try {
													if (loadRequestCmdInf == null) {
														loadRequestCmdInf = DeviceFactory
																.getICCardLoadToKernelRequstCmdInf();
													}
													ByteArrayOutputStream ous = new ByteArrayOutputStream();
													ous.write(1);
													log.debug("pinblcok ==============" + pinblock);
													ous.write(HexUtil.hexStringToByte(pinblock));
													loadRequestCmdInf.pinByUser(ous.toByteArray());
													return;
												} catch (Exception e) {
													e.printStackTrace();
													log.error("导入PIN发送错误.......", e);
													response.setSuc(false, "导入PIN发生错误........");
													// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
													// callback,
													// response.toJson()));
												}
											} else if (arg0 == 0x03) {
												try {
													if (loadRequestCmdInf == null) {
														loadRequestCmdInf = DeviceFactory
																.getICCardLoadToKernelRequstCmdInf();
													}
													ByteArrayOutputStream ous = new ByteArrayOutputStream();
													ous.write(1);
													ous.write(0);
													loadRequestCmdInf.resultOfUserAuthentication(ous.toByteArray());
													return;
												} catch (Exception e) {
													log.error("", e);
													e.printStackTrace();
													response.setSuc(false, "身份认证导入错误.......");
													// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
													// callback,
													// response.toJson()));
												}

											} else {
												response.addData("eventid", arg0);
												response.addData("data", HexUtil.bcd2str(arg1));
												// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
												// callback,
												// response.toJson()));
											}
											return;
										}
									});
								} catch (Exception e) {
									log.error("kernel pboc error..........................", e);
									response.setSuc(false, "kernel pboc 发生错误................");
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
									// callback,
									// response.toJson()));
									return;
								}
							} else {
								Log.i("ckh", "eventid == " + arg0 + "  data == " + HexUtil.bcd2str(arg1));
								// response.addData("eventid",
								// arg0);
								// response.addData("data",
								// HexUtil.bcd2str(arg1));
								log.debug("接收到data  =============" + new String(arg1));
								Message msg = Utility.createCallbackMsg(Constant.msg.msg_read_card, true, "");
								msg.getData().putInt("eventid", arg0);
								msg.getData().putString("data", HexUtil.bcd2str(arg1));
								handler.sendMessage(msg); // 读取卡号信息
								// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
								// callback,
								// response.toJson()));
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("start pboc error..........................", e);
					response.setSuc(false, "startpboc 发生错误................");
					// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
					// callback, response.toJson()));
				}

			};
		}.start();
	}

	/**
	 * 开始PBOC
	 * 
	 * @param transType
	 *            交易类型0x01 货物 0x05查询
	 * @param money
	 *            交易金额
	 * @param callback
	 *            回调函数
	 */
	public void startPboc(final int transType, final String money, final String pinblock, final String callback) {
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				try {
					log.debug("start pboc called..................");
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream arg1_ous = new ByteArrayOutputStream();
					arg1_ous.write(transType);
					arg1_ous.write(0);// 不支持电子现金
					// arg1_ous.write(EMVTAG.EMVTAG_APP_PAN);
					kernelCmdInf.standardDebitAndCreditStartProcInf(arg1_ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							response.setSuc(true, "接收事件成功...");
							log.debug("start pboc handlemessage  arg 1 ==============" + HexUtil.bcd2str(arg1));
							if (arg0 == 0x00) { // 请求导入金额
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									ByteArrayOutputStream ous = new ByteArrayOutputStream();
									ous.write(1);
									ous.write(formartAmount(money));
									loadRequestCmdInf.amountByUser(ous.toByteArray());// 导入金额
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("导入金额发送错误.......", e);
									response.setSuc(false, "导入金额发生错误........");
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
									// callback,
									// response.toJson()));
								}
							} else if (arg0 == 0x05) {
								try {
									if (loadRequestCmdInf == null) {
										loadRequestCmdInf = DeviceFactory.getICCardLoadToKernelRequstCmdInf();
									}
									loadRequestCmdInf.resultOfMessageExcute((byte) 1);
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("发送错误.......", e);
									response.setSuc(false, "发生错误........");
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
									// callback,
									// response.toJson()));
								}
							} else if (arg0 == 0x80) {
								try {
									kernelCmdInf.standardDebitAndCreditKernelProcInf(EMVTAG.getLakalaQueryBalanceTag(),
											new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
										@Override
										public void handleMessage(int arg0, byte[] arg1) {
											response.setSuc(true, "接收事件成功...");
											if (arg0 == 0x01) { // 请求导入PIN
												try {
													if (loadRequestCmdInf == null) {
														loadRequestCmdInf = DeviceFactory
																.getICCardLoadToKernelRequstCmdInf();
													}
													ByteArrayOutputStream ous = new ByteArrayOutputStream();
													ous.write(1);
													log.debug("pinblcok ==============" + pinblock);
													ous.write(HexUtil.hexStringToByte(pinblock));
													loadRequestCmdInf.pinByUser(ous.toByteArray());
													return;
												} catch (Exception e) {
													e.printStackTrace();
													log.error("导入PIN发送错误.......", e);
													response.setSuc(false, "导入PIN发生错误........");
													// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
													// callback,
													// response.toJson()));
												}
											} else if (arg0 == 0x03) {
												try {
													if (loadRequestCmdInf == null) {
														loadRequestCmdInf = DeviceFactory
																.getICCardLoadToKernelRequstCmdInf();
													}
													ByteArrayOutputStream ous = new ByteArrayOutputStream();
													ous.write(1);
													ous.write(0);
													loadRequestCmdInf.resultOfUserAuthentication(ous.toByteArray());
													return;
												} catch (Exception e) {
													log.error("", e);
													e.printStackTrace();
													response.setSuc(false, "身份认证导入错误.......");
													// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
													// callback,
													// response.toJson()));
												}

											} else {
												response.addData("eventid", arg0);
												response.addData("data", HexUtil.bcd2str(arg1));
												// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
												// callback,
												// response.toJson()));
											}
											return;
										}
									});
								} catch (Exception e) {
									log.error("kernel pboc error..........................", e);
									response.setSuc(false, "kernel pboc 发生错误................");
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
									// callback,
									// response.toJson()));
									return;
								}
							} else {
								response.addData("eventid", arg0);
								response.addData("data", arg1);
								log.debug("接收到data  =============" + new String(arg1));
								// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
								// callback,
								// response.toJson()));
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("start pboc error..........................", e);
					response.setSuc(false, "startpboc 发生错误................");
					// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_start_pboc,
					// callback, response.toJson()));
				}
			};
		}.start();
	}

	/**
	 * 转换金额
	 * 
	 * @param amount
	 * @return
	 */
	private static byte[] formartAmount(String amount) {
		if ("".equals(amount))
			return null;
		BigDecimal input = new BigDecimal(amount);
		BigDecimal bigformart = new BigDecimal("100");
		BigDecimal result = input.multiply(bigformart);
		DecimalFormat format = new DecimalFormat("##0");
		String amountI = format.format(result.doubleValue());
		String amountStr = "000000000000";
		amountStr += amountI;
		amountStr = amountStr.substring(amountStr.length() - 12, amountStr.length());
		return HexUtil.hexStringToByte(amountStr);
	}

	/**
	 * 交易结束，导入交易结果。
	 * 
	 * @param result
	 * @param response
	 * @param callback
	 */
	public void endPboc(final int result, final String responseScript, final String callback) {
		final JsResponse response = new JsResponse();
		new Thread() {
			public void run() {
				try {
					if (kernelCmdInf == null) {
						kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
					}
					ByteArrayOutputStream ous = new ByteArrayOutputStream();
					ous.write(result);
					ous.write(HexUtil.hexStringToByte(responseScript));
					// 导入联机结果。
					kernelCmdInf.inputOnlineRespDataInf(ous.toByteArray(),
							new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
						@Override
						public void handleMessage(int arg0, byte[] arg1) {
							if (arg0 == 0x80) {// end事件执行成功
								ByteArrayOutputStream ous_arg0 = new ByteArrayOutputStream();
								try {
									ous_arg0.write(EMVTAG.getLakalaQueryBalanceTag());
									kernelCmdInf.standardDebitAndCreditCompleteProcInf(ous_arg0.toByteArray(),
											new ICCardFinancialKernelCmdInf.ICCardKernelMsgReport() {
										public void handleMessage(int arg0, byte[] arg1) {
											resetPboc();// 复位
											response.setSuc(true, "执行 complete pboc 成功.....");
											response.addData("type", arg0);
											response.addData("data", HexUtil.bcd2str(arg1));
											try {
												response.addData("scriptExeResponse", HexUtil.bcd2str(kernelCmdInf
														.readKernelData(EMVTAG.getLakalaScriptResultTag())));
											} catch (Exception e) {
												log.error("", e);
												e.printStackTrace();
												response.setSuc(false, "执行 complete pboc失败.....");
											}
											// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_end_pboc,
											// callback,
											// response.toJson()));
										};
									});
									return;
								} catch (Exception e) {
									e.printStackTrace();
									log.error("complete pboc出错............................", e);
									response.setSuc(false, "complete pboc出错.....");
									// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_end_pboc,
									// callback,
									// response.toJson()));
								}
							} else {
								response.setSuc(true, "执行 导入联机命令成功.....");
								response.addData("type", arg0);
								response.addData("data", arg1);
								// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_end_pboc,
								// callback,
								// response.toJson())); }
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					log.error("导入联机结果出错............................", e);
					response.setSuc(false, "导入联机结果出错.....");
					// handler.sendMessage(Utility.createCallbackMsg(Constant.msg.msg_end_pboc,
					// callback, response.toJson()));
				}
			};
		}.start();
	}

	/**
	 * IC内核复位
	 */
	public int resetPboc() {
		try {
			if (kernelCmdInf == null) {
				kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
			}
			log.debug("开始复位pboc........");
			kernelCmdInf.resetKernel((byte) 0);
			log.debug("结束复位pboc.........");
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
			return 0;
		}
	}

	/**
	 * 非接内核复位
	 */
	public int resetRfPboc() {
		try {
			if (kernelCmdInf == null) {
				kernelCmdInf = DeviceFactory.getICCardFinancialKernelDev();
			}
			log.debug("开始复位rf 内核pboc........");
			kernelCmdInf.resetKernel((byte) 1);
			log.debug("结束复位rf 内核pboc.........");
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
			return 0;
		}
	}
}
