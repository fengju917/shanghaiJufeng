package com.centerm.lklcpos.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.PrintDev;
import com.centerm.lklcpos.deviceinterface.PrintDev.CallBack;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.TransactionUtility;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-6-30 查询交易 2013-8-27 修改交易查询游标的问题
 */
public class QueryTransactionDetails extends BaseActivity {

	private static Logger logger = Logger.getLogger(QueryTransactionDetails.class);

	private LayoutInflater inflater;
	private boolean chageColor = true;
	private LinearLayout itmeLayout;
	private TextView count, page;
	private TransRecordDao transRecordDao;
	private ParamConfigDao paramConfigDao;
	private List<TransRecord> transRecord;
	private int listNum = 0;
	private int firstx, lastx;
	private PrintDev printDev;
	private String billno;
	private TransRecord transRecordItem;
	private Map<String, String> map;
	private boolean isPrinting = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query_transaction_details);

		init();
	}

	private void init() {

		inititle();

		transRecordDao = new TransRecordDaoImpl(this);

		transRecord = transRecordDao.getEntities();

		LinearLayout noitem = (LinearLayout) this.findViewById(R.id.noitem);
		LinearLayout dataitem = (LinearLayout) this.findViewById(R.id.dataitem);

		if (transRecord.size() == 0) {
			noitem.setVisibility(View.VISIBLE);
			dataitem.setVisibility(View.GONE);
			Button back_confirm = (Button) this.findViewById(R.id.back_confirm);
			back_confirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// QueryTransactionDetails.this.finish();
					lklcposActivityManager.removeActivity(QueryTransactionDetails.this);
					outActivityAnim();
				}
			});
		} else {

			noitem.setVisibility(View.GONE);
			dataitem.setVisibility(View.VISIBLE);
			inflater = this.getLayoutInflater();
			itmeLayout = (LinearLayout) this.findViewById(R.id.itmeLayout);
			count = (TextView) this.findViewById(R.id.count);
			page = (TextView) this.findViewById(R.id.page);

			Button returnitem = (Button) this.findViewById(R.id.returnitem);
			returnitem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO 上一条
					if (!isPrinting) {
						backItem();
					}
				}
			});

			Button nextitem = (Button) this.findViewById(R.id.nextitem);
			nextitem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 下一条
					if (!isPrinting) {
						nextItem();
					}
				}
			});

			Button homepage = (Button) this.findViewById(R.id.homepage);
			homepage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 首页
					if (!isPrinting) {
						listNum = 0;
						addRecord(transRecord.get(0), 1);
					}
				}
			});

			Button reprint = (Button) this.findViewById(R.id.reprint);
			reprint.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!isPrinting) {
						try {
							printDev = new PrintDev();
							printDev.openDev();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						map = TransactionUtility.transformToMap(transRecord.get(listNum));
						map.put("isReprints", "true");// 本类中全为重打印
						isPrinting = true;// 修改打印状态标志，开始打印
						printDev.printData(map, new CallBack() {

							@Override
							public void isPrintSecond() {
								isPrinting = false;// 修改打印状态标志，开始打印
								Intent intent = new Intent();
								intent.setClass(QueryTransactionDetails.this, PrintAgianDialog.class);
								intent.putExtra("code", transRecord.get(listNum).getTransprocode());
								startActivityForResult(intent, 1);
							}

							@Override
							public void printExcept(int code, Bundle b) {
								isPrinting = false;// 修改打印状态标志，开始打印
								printDev.close();
								if (code == 0x30) {
									creatExceptDialog(QueryTransactionDetails.this, b);
								} else {
									DialogFactory.showTips(QueryTransactionDetails.this, "打印机异常请稍候再试！");
								}
							}
						});

					} else {
						DialogFactory.showTips(QueryTransactionDetails.this, "正在打印中，请等待打印结束！");
					}

				}
			});

			Button search = (Button) this.findViewById(R.id.search);
			search.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 搜索
					if (!isPrinting) {
						searchDialog();
					}
				}
			});

			Button lastpage = (Button) this.findViewById(R.id.lastpage);
			lastpage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 尾页
					if (!isPrinting) {
						listNum = transRecord.size() - 1;
						addRecord(transRecord.get(transRecord.size() - 1), transRecord.size());
					}

				}
			});

			addRecord(transRecord.get(0), 1);
			count.setText("共" + transRecord.size() + "条");

			itmeLayout.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View arg0, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						firstx = (int) event.getX(); // 取得按下时的坐标x
						return true;
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						lastx = (int) event.getX(); // 取得松开时的坐标x;
						if (lastx - firstx > 100) {
							nextItem();
						} else if (firstx - lastx > 100) {
							backItem();
						}
						return true;
					}

					return false;
				}
			});
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (requestCode == 1) {
			if (resultCode != RESULT_CANCELED) {
				/*
				 * try { //printDev = null; printDev = new PrintDev();
				 * printDev.openDev(); } catch (Exception e) { // TODO
				 * Auto-generated catch block e.printStackTrace(); }
				 */
				isPrinting = true;
				map.put("isSecond", "true"); // 设置打印第二联
				printDev.printData(map, new CallBack() {

					@Override
					public void isPrintSecond() {
						isPrinting = false;// 修改打印状态标志，开始打印
						printDev.close();
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDev.close();
						isPrinting = false;// 修改打印状态标志，开始打印
						// System.out.println("QueryTransactionDetails.code:"+code);
						if (code == 0x30) {
							creatExceptDialog(QueryTransactionDetails.this, b);
						} else {
							DialogFactory.showTips(QueryTransactionDetails.this, "打印机异常请稍候再试！");
						}
					}
				});
			}

		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (!isPrinting) {
			super.onKeyDown(keyCode, event);
		} else {
			// DialogFactory.showTips(QueryTransactionDetails.this,
			// "正在打印，请稍候！");
		}
		return true;
	}

	private void searchDialog() {

		final AlertDialog dialog = new AlertDialog.Builder(QueryTransactionDetails.this).create();
		dialog.setView(
				QueryTransactionDetails.this.getLayoutInflater().inflate(R.layout.query_transaction_dialog, null));
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.show();
		dialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_HOME) {
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_BACK) {
					dialog.dismiss();
					return true;
				}
				return false;
			}
		});
		// dialog.getWindow().setType(
		// WindowManager.LayoutParams.TYPE_KEYGUARD);

		dialog.setContentView(R.layout.query_transaction_dialog);
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.CENTER);
		dialog.setCanceledOnTouchOutside(true);

		final EditText editText = (EditText) dialogWindow.findViewById(R.id.editText);
		editText.setFocusable(true);

		editText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
				// TODO Auto-generated method stub
				// System.out.println("action:"+event.getKeyCode());
				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					// imm.toggleSoftInput(0,
					// InputMethodManager.HIDE_NOT_ALWAYS);
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
				}
				return true;
			}
		});

		InputMethodManager inputManager = (InputMethodManager) getApplication()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

		Button comfirm = (Button) dialogWindow.findViewById(R.id.comfirm);
		comfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				billno = editText.getText().toString();
				if ("".equals(billno)) {
					DialogFactory.showTips(QueryTransactionDetails.this, "凭证号不能为空！");
				} else {
					if (billno.length() < 7) {
						dialog.dismiss();
						billno = Utility.addZeroForNum(billno, 6);
						transRecordItem = transRecordDao.getTransRecordByCondition(getBatchno(), billno);
						if (transRecordItem != null) {
							listNum = getIndex(transRecordItem);
							addRecord(transRecordItem, listNum + 1);

							// System.out.println("listNum:"+listNum);
							// listNum = getIndex(transRecordItem)-1; //note by
							// xrh @20130827
							// addRecord(transRecordItem,listNum+1);
						} else {
							DialogFactory.showTips(QueryTransactionDetails.this, "暂无该记录或凭证号有误！");
						}
					} else {
						DialogFactory.showTips(QueryTransactionDetails.this, "凭证号不能超过6位，请重新输入！");
					}
				}

			}
		});
		Button cancel = (Button) dialogWindow.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}

	private int getIndex(TransRecord record) {
		int index = 0;
		for (int i = 0; i < transRecord.size(); i++) {
			if (record.getId() == transRecord.get(i).getId()) {
				index = i;
			}
		}
		return index;

	}

	/**
	 * 从配置表里面读取批次号
	 * 
	 * @return
	 */
	private String getBatchno() {
		paramConfigDao = new ParamConfigDaoImpl(this);
		String batchnoNum = paramConfigDao.get("batchno");
		return batchnoNum;
	}

	private void backItem() {
		if (listNum > 0) {
			listNum = listNum - 1;
			addRecord(transRecord.get(listNum), listNum + 1);
		} else {
			DialogFactory.showTips(QueryTransactionDetails.this, "已经到第一条！");
		}
	}

	private void nextItem() {
		if (listNum < transRecord.size() - 1) {
			listNum = listNum + 1;
			addRecord(transRecord.get(listNum), listNum + 1);
		} else {
			DialogFactory.showTips(QueryTransactionDetails.this, "已经到最后一条！");
		}
	}

	/**
	 * 根据交易记录存储类recorditem来读取记录信息加入数据
	 * 
	 * @param recorditem
	 *            交易记录
	 * @param num
	 *            当前是第几条数据
	 */
	private void addRecord(TransRecord recorditem, int num) {
		// 先清空linearLayout中的数据再加入
		itmeLayout.removeAllViews();
		// 重置颜色
		chageColor = true;

		String transprocode = recorditem.getTransprocode();
		String msg_tp = recorditem.getReserve1(); // 获取交易下发的消息类型
		String conditionmode = recorditem.getConditionmode();
		String transtype = null;

		if ("010000".equals(transprocode) && "00".equals(conditionmode)) { // 消费
			transtype = "助农取款";
		} else if ("000000".equals(transprocode) && "06".equals(conditionmode)) { // 预授权完成
			transtype = "预授权完成";
		} else if ("030000".equals(transprocode)) { // 预授权
			transtype = "预授权";
		} else if ("200000".equals(transprocode) && "00".equals(conditionmode) && "0230".equals(msg_tp)) { // 退货
			transtype = "退货";
		} else if ("200000".equals(transprocode) && "00".equals(conditionmode) && "0210".equals(msg_tp)) { // 消费撤销
			transtype = "消费撤销";
		} else if ("200000".equals(transprocode) && "06".equals(conditionmode) && "0210".equals(msg_tp)) { // 预授权完成撤销
			transtype = "预授权完成撤销";
		} else if ("200000".equals(transprocode) && "06".equals(conditionmode) && "0110".equals(msg_tp)) { // 预授权撤销
			transtype = "预授权撤销";
		} else if ("600000".equals(transprocode)) {
			transtype = "指定账户圈存";
		} else if ("630000".equals(transprocode)) {
			transtype = "现金充值";
		} else if ("620000".equals(transprocode)) {
			transtype = "非指定账户圈存";
		} else if ("170000".equals(transprocode)) {
			transtype = "现金充值撤销";
		} else if ("000000".equals(transprocode) && "01".equals(conditionmode)) {
			transtype = "脱机退货";
		}
		// System.out.println("Batchbillno:" + recorditem.getBatchbillno());
		String billno = recorditem.getBatchbillno().substring(6, 12);
		TransRecord item = null;
		item = transRecordDao.getTransRevokeByCondition(getBatchno(), billno);
		String systraceno = null;
		if (item != null) {
			systraceno = billno + "【凭证号】" + "(已撤销)";
		} else {
			systraceno = billno + "【凭证号】";
		}

		itmeLayout.addView(viewAdd(transtype, systraceno));

		String cardNum = recorditem.getPriaccount();
		itmeLayout.addView(viewAdd("卡号", Utility.formatCardno(cardNum)));
		/*
		 * if ("030000".equals(transprocode)) { itmeLayout.addView(viewAdd("卡号",
		 * Utility.formatCardNo(cardNum))); } else {
		 * itmeLayout.addView(viewAdd("卡号", Utility.formatCardno(cardNum))); }
		 */

		String amount = recorditem.getTransamount();
		itmeLayout.addView(viewAdd("金额", Utility.unformatMount(amount)));

		String time = recorditem.getTranslocaldate() + recorditem.getTranslocaltime();

		itmeLayout.addView(viewAdd("交易时间", Utility.printFormatDateTime(time)));

		String refernumber = recorditem.getRefernumber();
		if (refernumber != null && !"".equals(refernumber)) {
			itmeLayout.addView(viewAdd("系统参考号", refernumber));
		}
		page.setText("当前记录第" + num + "条");
	}

	/**
	 * 时间格式化 例如：20130701185024转2013-07-01 18:50:24
	 * 
	 * @param time
	 * @return
	 */
	private String formatDate(String time) {

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formatime = null;
		try {
			formatime = format1.format(format.parse(time));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("", e);
		}

		return formatime;

	}

	private View viewAdd(String key, String value) {

		View view = inflater.inflate(R.layout.confirminfo_item, null);
		TextView keyView = (TextView) view.findViewById(R.id.key);
		keyView.setText(key);
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		if (chageColor) {
			keyView.setBackgroundColor(getResources().getColor(R.color.changetrue));
			valueView.setBackgroundColor(getResources().getColor(R.color.changetrue));
			chageColor = false;
		} else {
			keyView.setBackgroundColor(getResources().getColor(R.color.changefalse));
			valueView.setBackgroundColor(getResources().getColor(R.color.changefalse));
			chageColor = true;
		}
		return view;

	}

}
