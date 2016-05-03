package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.HexUtil;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

public class InputVoucherActivity extends TradeBaseActivity {

	private TextView mTextView, title_text;
	private EditText mEditText;
	private Button mButton;
	private ParamConfigDao mParamConfigDao;
	private TransRecordDao mTransRecordDao;
	private String batchno;
	private String voucher;
	private String oldPriaccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		final String code = mTransaction.getMctCode();
		setContentView(R.layout.inputvoucher);

		inititle();
		title_text = (TextView) findViewById(R.id.title_text);
		mTextView = (TextView) findViewById(R.id.show_batch_no);
		mEditText = (EditText) findViewById(R.id.input_voucher);
		mButton = (Button) findViewById(R.id.confirm);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		mTransRecordDao = new TransRecordDaoImpl(this);

		if ("002303".equals(code) || "002315".equals(code) || "002324".equals(code)) { // 消费撤销、预授权完成撤销、电子现金撤销
			if ("002303".equals(code)) {
				title_text.setText("消费撤销");
			} else if ("002315".equals(code)) {
				title_text.setText("预授权完成撤销");
			} else if ("002324".equals(code)) {
				title_text.setText("现金充值撤销");
			}

			batchno = mParamConfigDao.get("batchno");
			mTextView.setText("请输入原交易凭证号");
			mButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					voucher = mEditText.getText().toString();
					if (voucher == null || "".equals(voucher)) {
						// Toast.makeText(InputVoucherActivity.this,
						// "原交易凭证号输入不能为空！", Toast.LENGTH_SHORT).show();
						DialogFactory.showTips(InputVoucherActivity.this, "原交易凭证号输入不能为空！");
						return;
					}
					if (voucher.length() < 6) {
						voucher = Utility.addZeroForNum(voucher, 6);
					}

					TransRecord mTransRecord = mTransRecordDao.getConsumeByCondition(batchno, voucher);
					if (mTransRecord == null) {
						DialogFactory.showTips(InputVoucherActivity.this, "原交易不存在！");
						return;
					} else {
						oldPriaccount = mTransRecord.priaccount;
					}

					TransRecord mTransRemoveRecord = mTransRecordDao.getTransRevokeByCondition(batchno, voucher);
					if (mTransRemoveRecord != null) {
						DialogFactory.showTips(InputVoucherActivity.this, "原交易已撤销！");
						oldPriaccount = null;
						return;
					}

					if ("002324".equals(code)) { // 判断是否为现金充值交易
						if (!"630000".equals(mTransRecord.transprocode)) {
							DialogFactory.showTips(InputVoucherActivity.this, "交易类型不匹配！");
							oldPriaccount = null;
							return;
						}
						mTransaction.getDataMap().put("transamount", Utility.unformatMount(mTransRecord.transamount)); // 获取原交易金额
					} else {
						// 判断如果不是消费撤销或预授权完成撤销交易;
						Log.i("XRH", "conditionmode= " + mTransRecord.conditionmode + " code= " + code);
						if (!("06".equals(mTransRecord.conditionmode) && "000000".equals(mTransRecord.transprocode)
								&& "002315".equals(code))
								&& !("00".equals(mTransRecord.conditionmode)
										&& "000000".equals(mTransRecord.transprocode) && "002303".equals(code))) {
							DialogFactory.showTips(InputVoucherActivity.this, "交易类型不匹配！");
							oldPriaccount = null;
							return;
						}
					}

					if (!"".equals(oldPriaccount) && oldPriaccount != null) {
						mTransaction.getDataMap().put("oldpriaccount", oldPriaccount);
					}

					Intent nextIntent = forward("1");
					mTransaction.getDataMap().put("batchbillno", batchno + voucher);
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				}
			});
		} else if ("002314".equals(code) || "002316".equals(code)) { // 预授权完成、预授权撤销
			if ("002314".equals(code)) {
				title_text.setText("预授权完成");
			} else {
				title_text.setText("预授权撤销");
			}
			mTextView.setText("请输入原交易授权号");
			mEditText.setHint("原交易授权号");
			mButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					String idrespcode = mEditText.getText().toString();
					if (idrespcode == null || "".equals(idrespcode)) {
						DialogFactory.showTips(InputVoucherActivity.this, "原交易授权号输入不能为空！");
						return;
					}
					if (idrespcode.length() > 6) {
						DialogFactory.showTips(InputVoucherActivity.this, "原交易授权号长度超过上限！");
						return;
					}
					Intent nextIntent = forward("1");
					mTransaction.getDataMap().put("idrespcode",
							HexUtil.bcd2str(Utility.addZeroForNum(idrespcode, 6).getBytes()));
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				}
			});
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		mEditText.setText("");
		super.onStart();
	}

}
