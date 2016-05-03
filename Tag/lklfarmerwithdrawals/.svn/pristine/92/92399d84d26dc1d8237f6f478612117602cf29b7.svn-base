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
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/*
 * 商户参数设置页面
 */
public class ConsumeFragment extends Fragment {

	private EditText contactNumEditText;
	private EditText termNumEditText;
	private EditText contactNameEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.client_settings_layout, container, false);

		contactNumEditText = (EditText) view.findViewById(R.id.contact_num);
		termNumEditText = (EditText) view.findViewById(R.id.term_num);
		contactNameEditText = (EditText) view.findViewById(R.id.contact_name_text);
		saveButton = (Button) view.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(getActivity());
		contactNumEditText.setText(mParamConfigDao.get("merid"));
		termNumEditText.setText(mParamConfigDao.get("termid"));
		contactNameEditText.setText(mParamConfigDao.get("mchntname"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("merid", contactNumEditText.getText().toString());
				map.put("termid", termNumEditText.getText().toString());
				map.put("mchntname", contactNameEditText.getText().toString());
				int ret = mParamConfigDao.update(map);
				if (ret == 3) {
					DialogFactory.showTips(getActivity(),
							getActivity().getResources().getString(R.string.tip_save_success));
				} else {
					DialogFactory.showTips(getActivity(),
							getActivity().getResources().getString(R.string.tip_save_unsuccess));
				}
			}
		});
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

}
