package com.centerm.lklcpos.view;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/*
 * 系统参数设置页面
 * 
 */
public class SysFragment extends Fragment {

	private EditText standbyTimeEdit;
	private EditText secondBatchEdit;
	private EditText maxTransactionEdit;
	// private EditText operatorNoEdit;
	// private EditText operatorpPwdEdit;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sys_settings_layout, container, false);

		standbyTimeEdit = (EditText) view.findViewById(R.id.standby_time_edit);
		secondBatchEdit = (EditText) view.findViewById(R.id.second_batch_edit);
		maxTransactionEdit = (EditText) view.findViewById(R.id.max_transaction_edit);
		// operatorNoEdit = (EditText)view.findViewById(R.id.operator_no_edit);
		// operatorpPwdEdit =
		// (EditText)view.findViewById(R.id.operator_pwd_edit);
		saveButton = (Button) view.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(getActivity());

		// 原生数据库新添待机超时时间，需sava记录
		String standby_timeout = mParamConfigDao.get("standbytimeout");
		if (standby_timeout == null || "".equals(standby_timeout)) {
			mParamConfigDao.update("standbytimeout", "60");
		}

		standbyTimeEdit.setText(mParamConfigDao.get("standbytimeout"));
		maxTransactionEdit.setText(mParamConfigDao.get("systracemax"));
		// operatorNoEdit.setText(mParamConfigDao.get("operatorcode"));
		// operatorpPwdEdit.setText(mParamConfigDao.get("operatorpwd"));
		secondBatchEdit.setText(mParamConfigDao.get("batchno"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("standbytimeout", standbyTimeEdit.getText().toString());
				map.put("batchno", secondBatchEdit.getText().toString());
				map.put("systracemax", maxTransactionEdit.getText().toString());
				// map.put("operatorcode", operatorNoEdit.getText().toString());
				// map.put("operatorpwd",
				// operatorpPwdEdit.getText().toString());
				String tipString = null;
				int ret = mParamConfigDao.update(map);
				if (ret == 3) {
					StandbyService.time_out = Integer.parseInt(standbyTimeEdit.getText().toString()) * 1000; // 更新待机服务超时时间
					tipString = getActivity().getResources().getString(R.string.tip_reset_success);
				} else {
					tipString = getActivity().getResources().getString(R.string.tip_reset_unsuccess);
				}
				DialogFactory.showTips(getActivity(), tipString);
			}

		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
}
