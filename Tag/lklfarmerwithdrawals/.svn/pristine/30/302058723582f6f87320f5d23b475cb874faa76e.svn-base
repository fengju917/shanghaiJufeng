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
 * 通讯参数设置页面
 */
public class CommuFragment extends Fragment {

	private EditText tpudEditText;
	private EditText tranTimeEditText;
	private EditText flushesTimesEditText;
	// private EditText reconnTimesEditText;
	// private EditText cerDownloadsEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.commu_settings_layout, container, false);

		tpudEditText = (EditText) view.findViewById(R.id.tpud_edit);
		tranTimeEditText = (EditText) view.findViewById(R.id.tran_timeout_edit);
		flushesTimesEditText = (EditText) view.findViewById(R.id.flushes_times_edit);
		// reconnTimesEditText =
		// (EditText)view.findViewById(R.id.reconn_times_edit);
		// cerDownloadsEditText =
		// (EditText)view.findViewById(R.id.cer_downloads_edit);
		saveButton = (Button) view.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(getActivity());
		tpudEditText.setText(mParamConfigDao.get("tpdu"));
		tranTimeEditText.setText(mParamConfigDao.get("dealtimeout"));
		flushesTimesEditText.setText(mParamConfigDao.get("reversetimes"));
		// reconnTimesEditText.setText(mParamConfigDao.get("reconntimes"));
		// cerDownloadsEditText.setText(mParamConfigDao.get("redowntimes"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Map<String, String> map = new HashMap<String, String>();
				map.put("tpdu", tpudEditText.getText().toString());
				map.put("dealtimeout", tranTimeEditText.getText().toString());
				map.put("reversetimes", flushesTimesEditText.getText().toString());
				// map.put("reconntimes",
				// reconnTimesEditText.getText().toString());
				// map.put("redowntimes",
				// cerDownloadsEditText.getText().toString());
				int ret = mParamConfigDao.update(map);
				if (ret == 3) {
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
