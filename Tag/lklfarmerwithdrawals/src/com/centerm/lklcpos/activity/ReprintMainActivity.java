package com.centerm.lklcpos.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.PrintDev;
import com.centerm.lklcpos.deviceinterface.PrintDev.CallBack;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.TransactionUtility;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.MenuGridAdapter;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-6-30 重打印按钮主界面
 */
public class ReprintMainActivity extends BaseActivity {

	private static Logger logger = Logger.getLogger(ReprintMainActivity.class);

	private TransRecordDao transRecordDao;
	private ParamConfigDao paramConfigDao;
	private PrintDev printDev;
	private Map<String, String> settleDataMap, lastTransMap;
	private boolean isPinting = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reprintmainactivity);

		inititle();

		transRecordDao = new TransRecordDaoImpl(this);
		paramConfigDao = new ParamConfigDaoImpl(this);

		GridView gridView = (GridView) this.findViewById(R.id.gridView);
		List<Shortcut> shortcut = new ArrayList<Shortcut>();
		// 重打印结算
		Shortcut view1 = new Shortcut(R.drawable.reprint_settlement, R.string.reprint_settlement);
		shortcut.add(view1);
		// 重打印交易
		Shortcut view2 = new Shortcut(R.drawable.reprint_transaction, R.string.reprint_transaction);
		shortcut.add(view2);
		// 重打印上一笔
		Shortcut view3 = new Shortcut(R.drawable.reprint_last, R.string.reprint_last);
		shortcut.add(view3);
		// 打印当前批次统计单
		Shortcut view4 = new Shortcut(R.drawable.print_total_btn, R.string.print_all);
		shortcut.add(view4);
		// 打印交易明细单
		Shortcut view5 = new Shortcut(R.drawable.print_list_btn, R.string.print_list);
		shortcut.add(view5);

		MenuGridAdapter adapter = new MenuGridAdapter(ReprintMainActivity.this, shortcut, true);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// Toast.makeText(ReprintMainActivity.this,
				// "position:"+position, Toast.LENGTH_LONG).show();
				if (!isPinting) {
					switch (position) {
					case 0:// 重打印结算
						if (!Utility.getSignStatus(ReprintMainActivity.this)
								&& !"".equals(paramConfigDao.get("settlebatchno"))
								&& paramConfigDao.get("settlebatchno") != null) {
							openPrintDev();
							settleDataMap = new HashMap<String, String>();
							settleDataMap.put("transprocode", "900000");
							settleDataMap.put("batchbillno", paramConfigDao.get("settlebatchno"));
							settleDataMap.put("translocaldate", paramConfigDao.get("translocaldate"));
							settleDataMap.put("translocaltime", paramConfigDao.get("translocaltime"));
							settleDataMap.put("requestSettleData", paramConfigDao.get("requestSettleData"));
							settleDataMap.put("settledata", paramConfigDao.get("settledata"));
							settleDataMap.put("respcode", paramConfigDao.get("respcode"));
							settleDataMap.put("isReprints", "true");
							printDev.printData(settleDataMap, new CallBack() {

								@Override
								public void isPrintSecond() {
									isPinting = false;
									printDev.close();
									/*
									 * modify by chenkehui for
									 * 需求更改：重打印结算时，不弹出是否打印明细 @20130814
									 */
									// Intent intent = new Intent();
									// intent.putExtra("code", "900000");
									// //code为dialog识别是否为打印结算数据标志
									// intent.setClass(ReprintMainActivity.this,
									// PrintAgianDialog.class);
									// startActivityForResult(intent, 1);
									// addActivityAnim();
								}

								@Override
								public void printExcept(int code, Bundle b) {
									isPinting = false;
									printDev.close();
									if (code == 0x30) {
										creatExceptDialog(ReprintMainActivity.this, b);
									} else {
										DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！");
									}
								}
							});
						} else {
							isPinting = false;
							DialogFactory.showTips(ReprintMainActivity.this, "暂无结算信息，无法打印！");
						}

						break;
					case 1:// 重打印交易
						if (transRecordDao.getLastTransRecord() != null) {
							Intent intent = new Intent();
							intent.setClass(ReprintMainActivity.this, InputBillnoActivity.class);
							startActivity(intent);
							addActivityAnim();
							lklcposActivityManager.removeActivity(ReprintMainActivity.this);
						} else {
							isPinting = false;
							DialogFactory.showTips(ReprintMainActivity.this, "暂无交易记录，无法打印！");
						}

						break;
					case 2:// 重打印上一笔
						lastTransMap = TransactionUtility.transformToMap(transRecordDao.getLastTransRecord());
						if (lastTransMap != null) {
							openPrintDev();
							lastTransMap.put("isReprints", "true");
							printDev.printData(lastTransMap, new CallBack() {

								@Override
								public void isPrintSecond() {
									isPinting = false;
									printDev.close();
									Intent intent = new Intent();
									intent.setClass(ReprintMainActivity.this, PrintAgianDialog.class);
									startActivityForResult(intent, 2);
									addActivityAnim();
								}

								@Override
								public void printExcept(int code, Bundle b) {
									isPinting = false;
									printDev.close();
									if (code == 0x30) {
										creatExceptDialog(ReprintMainActivity.this, b);
									} else {
										DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！");
									}
								}
							});
						} else {
							isPinting = false;
							DialogFactory.showTips(ReprintMainActivity.this, "暂无交易记录，无法打印！");
						}
						break;
					case 3: // 打印交易汇总
						openPrintDev();
						Map<String, String> dataMap = new HashMap<String, String>();
						dataMap.put("printtype", "alltrans");
						printDev.printData(dataMap, new CallBack() {

							@Override
							public void isPrintSecond() {
								// TODO Auto-generated method stub
								isPinting = false;
								printDev.close();
							}

							@Override
							public void printExcept(int code, Bundle b) {
								// TODO Auto-generated method stub
								isPinting = false;
								printDev.close();
								if (code == 0x30) {
									creatExceptDialog(ReprintMainActivity.this, b);
								} else {
									DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！");
								}
							}

						});
						break;
					case 4: // 打印交易明细
						openPrintDev();
						Map<String, String> translistMap = new HashMap<String, String>();
						translistMap.put("printDetails", "true");
						printDev.printData(translistMap, new CallBack() {

							@Override
							public void isPrintSecond() {
								// TODO Auto-generated method stub
								isPinting = false;
								printDev.close();
							}

							@Override
							public void printExcept(int code, Bundle b) {
								// TODO Auto-generated method stub
								isPinting = false;
								printDev.close();
								if (code == 0x30) {
									creatExceptDialog(ReprintMainActivity.this, b);
								} else {
									DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！");
								}
							}

						});
						break;
					}
				} else {
					DialogFactory.showTips(ReprintMainActivity.this, "正在打印，请稍候！");
				}
			}
		});
	}

	private void openPrintDev() {
		try {
			printDev = new PrintDev();
			printDev.openDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		isPinting = true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (!isPinting) {
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		try {
			printDev = new PrintDev();
			printDev.openDev();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}
		isPinting = true;
		/*
		 * if(requestCode==1){ if(resultCode!=RESULT_CANCELED){
		 * settleDataMap.put("isReprints", "true");
		 * settleDataMap.put("printDetails", "true");
		 * printDev.printData(settleDataMap, new CallBack() {
		 * 
		 * @Override public void isPrintSecond() { printDev.close(); isPinting =
		 * false; }
		 * 
		 * @Override public void printExcept(int code, Bundle b) {
		 * printDev.close(); isPinting = false; if(code==0x30){
		 * creatExceptDialog(ReprintMainActivity.this,b); }else{
		 * DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！"); } }
		 * }); } else { settleDataMap.put("isReprints", "true");
		 * settleDataMap.put("printDetails", "false");
		 * printDev.printData(settleDataMap, new CallBack() {
		 * 
		 * @Override public void isPrintSecond() { isPinting = false;
		 * printDev.close(); }
		 * 
		 * @Override public void printExcept(int code, Bundle b) { isPinting =
		 * false; printDev.close(); if(code==0x30){
		 * creatExceptDialog(ReprintMainActivity.this,b); }else{
		 * DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！"); } }
		 * }); } }else
		 */ if (requestCode == 2) {
			if (resultCode != RESULT_CANCELED) {
				lastTransMap.put("isReprints", "true");
				lastTransMap.put("isSecond", "true");
				printDev.printData(lastTransMap, new CallBack() {

					@Override
					public void isPrintSecond() {
						isPinting = false;
						printDev.close();
					}

					@Override
					public void printExcept(int code, Bundle b) {
						isPinting = false;
						printDev.close();
						if (code == 0x30) {
							creatExceptDialog(ReprintMainActivity.this, b);
						} else {
							DialogFactory.showTips(ReprintMainActivity.this, "打印机异常请稍候再试！");
						}
					}
				});
			} else {
				printDev.close();
				isPinting = false;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}