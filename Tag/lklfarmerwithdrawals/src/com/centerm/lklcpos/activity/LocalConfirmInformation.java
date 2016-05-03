package com.centerm.lklcpos.activity;

import java.util.Map;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.deviceinterface.PrintDev;
import com.centerm.lklcpos.deviceinterface.PrintDev.CallBack;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.TransactionUtility;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui 本地交易信息确认组件 2013-6-26
 */
public class LocalConfirmInformation extends BaseActivity {

	private static Logger logger = Logger.getLogger(LocalConfirmInformation.class);

	private LinearLayout itmeLayout, titleback;
	private LayoutInflater inflater;
	private Button confirm;
	private boolean chageColor = true;
	private TextView titletype;
	private Map<String, String> map;
	private PrintDev printDev = null;
	private TransRecord item;
	private boolean isPrinting = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confirminfo);
		inititle();
		init();
	}

	private void init() {
		Intent intent = this.getIntent();//
		if (intent != null) {
			inflater = this.getLayoutInflater();
			itmeLayout = (LinearLayout) this.findViewById(R.id.itmeLayout);
			// titleback = (LinearLayout)this.findViewById(R.id.titleback);
			// titletype = (TextView)this.findViewById(R.id.titletype);

			// titleback.setBackgroundResource(R.drawable.topbg_yellow);
			// titletype.setText("消费");

			TransRecordDao transRecordDao = new TransRecordDaoImpl(this);
			String billno = intent.getStringExtra("billno");
			String batchno = intent.getStringExtra("batchno");
			item = transRecordDao.getTransRecordByCondition(batchno, billno);

			String transprocode = item.transprocode;
			String conditionmode = item.conditionmode;
			String msg_tp = item.reserve1;
			String transtype = null;

			if ("000000".equals(transprocode) && "00".equals(conditionmode)) { // 消费
				transtype = "消费";
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
			}

			// titletype.setText(transtype);

			if (!"".equals(transtype) && transtype != null) {
				itmeLayout.addView(viewAdd("交易", transtype));
			}
			itmeLayout.addView(viewAdd("卡号", Utility.formatCardno(item.getPriaccount())));
			/*
			 * if ("030000".equals(transprocode)) {
			 * itmeLayout.addView(viewAdd("卡号"
			 * ,Utility.formatCardNo(item.getPriaccount()))); } else {
			 * itmeLayout
			 * .addView(viewAdd("卡号",Utility.formatCardno(item.getPriaccount
			 * ()))); }
			 */
			String amount = Utility.unformatMount(item.getTransamount());
			itmeLayout.addView(viewAdd("金额", amount));
			itmeLayout.addView(
					viewAdd("交易时间", Utility.printFormatDateTime(item.getTranslocaldate() + item.getTranslocaltime())));
			itmeLayout.addView(viewAdd("参考号", item.refernumber));

			map = TransactionUtility.transformToMap(item);
			map.put("isReprints", "true");

			confirm = (Button) this.findViewById(R.id.confirm);
			confirm.setText("确定");
			confirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// confirm.setClickable(false);
					if (!isPrinting) {
						isPrinting = true;
						try {
							printDev = new PrintDev();
							printDev.openDev();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.error("", e);
						}
						printDev.printData(map, new CallBack() {

							@Override
							public void isPrintSecond() {
								// isPrinting = false;
								Intent intent = new Intent();
								intent.setClass(LocalConfirmInformation.this, PrintAgianDialog.class);
								intent.putExtra("code", item.getTransprocode());
								startActivityForResult(intent, 1);
							}

							@Override
							public void printExcept(int code, Bundle b) {
								printDev.close();
								if (code == 0x30) {
									creatExceptDialog(LocalConfirmInformation.this, b);
								} else {
									DialogFactory.showTips(LocalConfirmInformation.this, "打印机异常请稍候再试！");
								}
							}
						});

					} else {
						DialogFactory.showTips(LocalConfirmInformation.this, "正在打印中，请稍候再试！");
					}

				}
			});
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1) {
			if (resultCode != RESULT_CANCELED) {
				map.put("isSecond", "true"); // 设置打印第二联
				printDev.printData(map, new CallBack() {

					@Override
					public void isPrintSecond() {
						printDev.close();
						lklcposActivityManager.removeActivity(LocalConfirmInformation.this);
						outActivityAnim();
					}

					@Override
					public void printExcept(int code, Bundle b) {
						printDev.close();
						if (code == 0x30) {
							creatExceptDialog(LocalConfirmInformation.this, b);
						} else {
							DialogFactory.showTips(LocalConfirmInformation.this, "打印机异常请稍候再试！");
						}
					}
				});
			} else {
				lklcposActivityManager.removeActivity(LocalConfirmInformation.this);
				outActivityAnim();
			}

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		isPrinting = false;
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (!isPrinting) {
			super.onKeyDown(keyCode, event);
		}
		return true;
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
