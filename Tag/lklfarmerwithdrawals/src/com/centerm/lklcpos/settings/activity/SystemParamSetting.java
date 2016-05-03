/**
 * 
 */
package com.centerm.lklcpos.settings.activity;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-26
 *
 */
public class SystemParamSetting extends BaseActivity {

	private EditText standbyTimeEdit;
	private EditText secondBatchEdit;
	private EditText maxTransactionEdit;
	// private EditText operatorNoEdit;
	// private EditText operatorpPwdEdit;
	private EditText secondBillnoEdit;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sys_settings_layout);
		inititle();

		standbyTimeEdit = (EditText) this.findViewById(R.id.standby_time_edit);
		secondBatchEdit = (EditText) this.findViewById(R.id.second_batch_edit);
		secondBillnoEdit = (EditText) findViewById(R.id.second_billno_edit);
		maxTransactionEdit = (EditText) this.findViewById(R.id.max_transaction_edit);
		// operatorNoEdit = (EditText)this.findViewById(R.id.operator_no_edit);
		// operatorpPwdEdit =
		// (EditText)this.findViewById(R.id.operator_pwd_edit);
		saveButton = (Button) this.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(this);

		standbyTimeEdit.setText(mParamConfigDao.get("standbytimeout"));
		maxTransactionEdit.setText(mParamConfigDao.get("systracemax"));
		// operatorNoEdit.setText(mParamConfigDao.get("operatorcode"));
		// operatorpPwdEdit.setText(mParamConfigDao.get("operatorpwd"));
		secondBatchEdit.setText(mParamConfigDao.get("batchno"));
		secondBillnoEdit.setText(mParamConfigDao.get("billno"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String newStandbyTime = standbyTimeEdit.getText().toString();

				// 最大流水笔数
				if (maxTransactionEdit.getText().toString().equals("")
						|| maxTransactionEdit.getText().toString() == null) {
					DialogFactory.showTips(SystemParamSetting.this, "最大交易流水笔数不能设置为空！");
					return;
				}
				// 批次号
				String batchnoString = secondBatchEdit.getText().toString();
				if ("".equals(batchnoString) || batchnoString == null) {
					DialogFactory.showTips(SystemParamSetting.this, "批次号不能设置为空！");
					return;
				} else if (batchnoString.length() < 6) {
					batchnoString = Utility.addZeroForNum(batchnoString, 6);
				}
				if ("000000".equals(batchnoString)) {
					DialogFactory.showTips(SystemParamSetting.this, "批次号不能设置为0！");
					return;
				}
				// 凭证号
				String billnoString = secondBillnoEdit.getText().toString();
				if ("".equals(billnoString) || billnoString == null) {
					DialogFactory.showTips(SystemParamSetting.this, "凭证号不能设置为空！");
					return;
				} else if (billnoString.length() < 6) {
					billnoString = Utility.addZeroForNum(billnoString, 6);
				}
				if ("000000".equals(billnoString)) {
					DialogFactory.showTips(SystemParamSetting.this, "凭证号不能设置为0！");
					return;
				}
				/*
				 * //操作员号 String operatorcode =
				 * operatorNoEdit.getText().toString(); if
				 * ("".equals(operatorcode) || operatorcode==null) {
				 * DialogFactory.showTips(SystemParamSetting.this,
				 * "操作员号不能设置为空！"); return ; } else if(operatorcode.length() <
				 * 2){ DialogFactory.showTips(SystemParamSetting.this,
				 * "操作员号不能设置少于2位！"); return ; } //操作员密码 String operatorpwd =
				 * operatorpPwdEdit.getText().toString();
				 * if("".equals(operatorpwd) || operatorpwd==null) {
				 * DialogFactory.showTips(SystemParamSetting.this,
				 * "操作员密码不能设置为空！"); return ; }else if(operatorpwd.length() < 4){
				 * DialogFactory.showTips(SystemParamSetting.this,
				 * "操作员密码不能设置少于4位！"); return ; }
				 */

				Map<String, String> map = new HashMap<String, String>();
				map.put("standbytimeout", newStandbyTime);
				map.put("batchno", batchnoString);
				map.put("billno", billnoString);
				map.put("systracemax", maxTransactionEdit.getText().toString());
				// map.put("operatorcode", operatorcode);
				// map.put("operatorpwd", operatorpwd);

				int ret = mParamConfigDao.update(map);
				if (ret == 4) {
					if ("".equals(newStandbyTime) || newStandbyTime == null) {
						StandbyService.time_out = 0; // 若待机时间为空默认为无限时等待
					} else {
						StandbyService.time_out = Integer.parseInt(newStandbyTime) * 1000; // 更新待机服务超时时间
					}
					StandbyService.onOperate();
					DialogFactory.showTips(SystemParamSetting.this,
							SystemParamSetting.this.getResources().getString(R.string.tip_save_success));
					lklcposActivityManager.removeActivity(SystemParamSetting.this);
					outActivityAnim();
				} else {
					DialogFactory.showTips(SystemParamSetting.this,
							SystemParamSetting.this.getResources().getString(R.string.tip_save_unsuccess));
				}
			}

		});

	}

}
