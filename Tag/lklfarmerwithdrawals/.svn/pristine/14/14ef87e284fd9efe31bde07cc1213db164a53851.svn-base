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
 * 连接设置页面
 */
public class ConnectFragment extends Fragment {

	private EditText connectEditText;
	private EditText downloadEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.connect_settings_layout, container, false);

		connectEditText = (EditText) view.findViewById(R.id.connect_address_edit);
		downloadEditText = (EditText) view.findViewById(R.id.download_address_edti);
		saveButton = (Button) view.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(getActivity());
		connectEditText.setText(mParamConfigDao.get("transIp"));
		downloadEditText.setText(mParamConfigDao.get("caIp"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("transIp", connectEditText.getText().toString());
				map.put("caIp", downloadEditText.getText().toString());
				int ret = mParamConfigDao.update(map);
				if (ret == 2) {
					DialogFactory.showTips(getActivity(),
							getActivity().getResources().getString(R.string.tip_save_success));
				} else if (ret != -1) {
					DialogFactory.showTips(getActivity(),
							getActivity().getResources().getString(R.string.tip_save_unsuccess));
				}
			}
		});
		return view;
	}
}
