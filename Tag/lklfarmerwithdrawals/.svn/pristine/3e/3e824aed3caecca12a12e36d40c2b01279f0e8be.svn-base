package com.centerm.lklcpos.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/*
 * 重置管理员密码页面
 */
public class ResetPawFragment extends Fragment {
	private EditText firstEditText;
	private EditText secondEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.resetpwd_settings_layout, container, false);
		firstEditText = (EditText) view.findViewById(R.id.first_pwd_edit);
		secondEditText = (EditText) view.findViewById(R.id.second_paw_edit);
		saveButton = (Button) view.findViewById(R.id.save);
		mParamConfigDao = new ParamConfigDaoImpl(getActivity());

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String firstPwd = firstEditText.getText().toString();
				String secondPwd = secondEditText.getText().toString();
				String tipString = null;
				if ("".equals(firstPwd) || "".equals(secondPwd)) {
					tipString = getActivity().getResources().getString(R.string.tip_reset_null);
				} else if (!firstPwd.equals(secondPwd)) {
					tipString = getActivity().getResources().getString(R.string.tip_reset_unsuccess);
				} else {
					int ret = mParamConfigDao.update("adminpwd", firstPwd);
					if (ret == 1)
						tipString = getActivity().getResources().getString(R.string.tip_reset_success);
				}
				DialogFactory.showTips(getActivity(), tipString);
			}
		});
		return view;
	}
}
