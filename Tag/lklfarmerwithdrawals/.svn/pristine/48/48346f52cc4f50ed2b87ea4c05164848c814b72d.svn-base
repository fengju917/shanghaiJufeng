/**
 * 
 */
package com.centerm.lklcpos.settings.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.centerm.comm.persistence.dao.ReverseDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.impl.ReverseDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.view.MenuGridAdapter;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-26
 * 
 */
public class OtherSetting extends BaseActivity {
	public static String CLEAR_DATA = "com.lkl.farmer.clearDataDailog";
	private ReverseDao reverseDao;
	private TransRecordDao transRecordDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.other_settings_layout);
		inititle();

		reverseDao = new ReverseDaoImpl(this);
		transRecordDao = new TransRecordDaoImpl(this);

		GridView gridView = (GridView) this.findViewById(R.id.gridView);
		List<Shortcut> shortcut = new ArrayList<Shortcut>();
		// 清除冲正流水
		Shortcut view1 = new Shortcut(R.drawable.clear_flushes_style, R.string.clear_flushes);
		shortcut.add(view1);
		// 清除交易流水
		Shortcut view2 = new Shortcut(R.drawable.clear_trade_style, R.string.clear_transaction);
		shortcut.add(view2);
		// 清除结算状态
		Shortcut view3 = new Shortcut(R.drawable.clear_settle_style, R.string.clear_settle);
		shortcut.add(view3);

		MenuGridAdapter adapter = new MenuGridAdapter(OtherSetting.this, shortcut, true);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent mIntent = new Intent();
				mIntent.setAction(CLEAR_DATA);
				switch (position) {
				case 0:
					if (reverseDao.getReverseCount() <= 0) {
						DialogFactory.showTips(OtherSetting.this, "无冲正记录！");
						return;
					}
					mIntent.putExtra("clear", "flushes");
					break;
				case 1:
					if (transRecordDao.getTransCount() <= 0) {
						DialogFactory.showTips(OtherSetting.this, "无交易流水记录！");
						return;
					}
					mIntent.putExtra("clear", "trade");
					break;
				case 2:
					mIntent.putExtra("clear", "settle");
					break;
				}
				startActivityForResult(mIntent, 0);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (resultCode) {
		case RESULT_OK:
			DialogFactory.showTips(OtherSetting.this, "清除成功！");
			break;

		default:
			break;
		}
	}

}
