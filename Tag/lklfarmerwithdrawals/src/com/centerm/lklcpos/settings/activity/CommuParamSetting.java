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
import com.centerm.lklcpos.util.DialogFactory;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-26
 * 
 */
public class CommuParamSetting extends BaseActivity {

	private EditText tpudEditText;
	private EditText tranTimeEditText;
	private EditText flushesTimesEditText;
	// private EditText reconnTimesEditText;
	// private EditText cerDownloadsEditText;
	private Button saveButton;
	private ParamConfigDao mParamConfigDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commu_settings_layout);
		inititle();

		tpudEditText = (EditText) this.findViewById(R.id.tpud_edit);
		tranTimeEditText = (EditText) this.findViewById(R.id.tran_timeout_edit);
		flushesTimesEditText = (EditText) this.findViewById(R.id.flushes_times_edit);
		// reconnTimesEditText = (EditText) this
		// .findViewById(R.id.reconn_times_edit);
		// cerDownloadsEditText = (EditText) this
		// .findViewById(R.id.cer_downloads_edit);
		saveButton = (Button) this.findViewById(R.id.save);

		mParamConfigDao = new ParamConfigDaoImpl(this);
		tpudEditText.setText(mParamConfigDao.get("tpdu"));
		tranTimeEditText.setText(mParamConfigDao.get("dealtimeout"));
		flushesTimesEditText.setText(mParamConfigDao.get("reversetimes"));
		// reconnTimesEditText.setText(mParamConfigDao.get("reconntimes"));
		// cerDownloadsEditText.setText(mParamConfigDao.get("redowntimes"));

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int time = 0;
				// 对交易超时进行判定
				String transTime = tranTimeEditText.getText().toString();
				if (!"".equals(transTime) && transTime != null) {
					time = Integer.valueOf(tranTimeEditText.getText().toString());
					if (time > 120) {
						DialogFactory.showTips(CommuParamSetting.this, "交易超时不能设置超过120秒！");
						return;
					} else if (time < 60) {
						DialogFactory.showTips(CommuParamSetting.this, "交易超时不能设置低于60秒！");
						return;
					}
				} else {
					DialogFactory.showTips(CommuParamSetting.this, "交易超时不能设置为空！");
					return;
				}
				// 冲正次数设置判定
				String flushesTimes = flushesTimesEditText.getText().toString();
				if ("".equals(flushesTimes) || flushesTimes == null) {
					DialogFactory.showTips(CommuParamSetting.this, "冲正次数不能设置为空！");
					return;
				}
				// 重连次数
				// if(reconnTimesEditText.getText().toString().equals("")||reconnTimesEditText.getText().toString()==null){
				// DialogFactory.showTips(CommuParamSetting.this,
				// "重连次数不能设置为空！");
				// return ;
				// }
				// 证书下载次数
				// String cerDownTimes =
				// cerDownloadsEditText.getText().toString();
				// if("".equals(cerDownTimes)||cerDownTimes==null){
				// DialogFactory.showTips(CommuParamSetting.this,
				// "证书下载次数不能设置为空！");
				// return ;
				// } else if("0".equals(cerDownTimes) ||
				// "00".equals(cerDownTimes)) {
				// DialogFactory.showTips(CommuParamSetting.this,
				// "证书下载次数不能设置为0！");
				// return ;
				// }

				// TPDU
				String tpduString = tpudEditText.getText().toString();
				if ("".equals(tpduString) || tpduString == null) {
					DialogFactory.showTips(CommuParamSetting.this, "TPDU不能设置为空！");
					return;
				} else if (tpduString.length() != 10) {
					DialogFactory.showTips(CommuParamSetting.this, "TPDU设置长度不等于10位！");
					return;
				}

				Map<String, String> map = new HashMap<String, String>();
				map.put("tpdu", tpduString);
				map.put("dealtimeout", tranTimeEditText.getText().toString());
				map.put("reversetimes", flushesTimesEditText.getText().toString());
				// map.put("reconntimes",
				// reconnTimesEditText.getText().toString());
				// map.put("redowntimes", cerDownTimes);
				int ret = mParamConfigDao.update(map);
				if (ret == 3) {
					DialogFactory.showTips(CommuParamSetting.this,
							CommuParamSetting.this.getResources().getString(R.string.tip_save_success));
					lklcposActivityManager.removeActivity(CommuParamSetting.this);
					outActivityAnim();
				} else if (ret != -1) {
					DialogFactory.showTips(CommuParamSetting.this,
							CommuParamSetting.this.getResources().getString(R.string.tip_save_unsuccess));
				}
			}
		});
	}

}
