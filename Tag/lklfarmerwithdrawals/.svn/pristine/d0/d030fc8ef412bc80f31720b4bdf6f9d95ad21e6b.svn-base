package com.centerm.lklcpos.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.transaction.entity.Transaction;
import com.centerm.lklcpos.view.MenuGridAdapter;
import com.lkl.farmerwithdrawals.R;

public class OtherActivity extends BaseActivity {

	private Logger log = Logger.getLogger(OtherActivity.class);

	private GridView gridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.otheractivity);

		inititle();
		gridView = (GridView) this.findViewById(R.id.gridView);
		List<Shortcut> shortcut = new ArrayList<Shortcut>();

		Shortcut qcashup = new Shortcut(this, R.drawable.cashup_qsale_btn, R.string.qcashup_sale,
				"transcation/O000001.xml");
		qcashup.setJudgestate(true);
		qcashup.setNative(false);
		qcashup.setJudgeMaxTransRecords(true);
		shortcut.add(qcashup);

		Shortcut cashup = new Shortcut(this, R.drawable.cashup_sale_btn, R.string.cashup_sale,
				"transcation/O000002.xml");
		cashup.setJudgestate(true);
		cashup.setNative(false);
		cashup.setJudgeMaxTransRecords(true);
		shortcut.add(cashup);

		shortcut.add(new Shortcut(R.drawable.transfer_btn, R.string.transfer));

		shortcut.add(new Shortcut(R.drawable.other_balance_btn, R.string.consume_search_text));

		shortcut.add(new Shortcut(R.drawable.other_inqu_btn, R.string.transaction_inquiries_text));

		SharedPreferences mSP = getSharedPreferences("menu_settings", 0);

		// 脱机退货
		if (mSP.getBoolean("offRefund", false)) {
			Shortcut view5 = new Shortcut(this, R.drawable.offline_refund_btn, R.string.offline_refund,
					"transcation/T280000.xml");
			view5.setJudgestate(true);
			view5.setNative(false);
			view5.setPower("admin");
			view5.setJudgeMaxTransRecords(true);
			shortcut.add(view5);
		}

		MenuGridAdapter adapter = new MenuGridAdapter(OtherActivity.this, shortcut, true);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				switch (position) {
				case 0:
				case 1:
				case 5:
					Shortcut mShortcut = (Shortcut) gridView.getItemAtPosition(position);

					Intent intent = mShortcut.getmIntent();
					Transaction transaction = intent.getParcelableExtra("transaction");
					transaction.setProperties("other");
					intent.putExtra("transaction", transaction);
					mShortcut.setmIntent(intent);
					judgeState(OtherActivity.this, mShortcut);
					break;

				case 2: // 圈存
					Intent intent2 = new Intent();
					intent2.setClass(OtherActivity.this, TransferMenuActivity.class).putExtra("goback_title", "电子现金");
					;
					startActivity(intent2);
					addActivityAnim();
					break;

				case 3: // 脱机余额查询
					Intent intent0 = new Intent();
					intent0.setClass(OtherActivity.this, NonContactActivity.class);
					startActivity(intent0);
					addActivityAnim();
					break;

				case 4: // 脱机明细查询
					log.info("NonContactReadLogActivity ..");

					Intent intent1 = new Intent();
					intent1.setClass(OtherActivity.this, NonContactReadLogActivity.class);
					startActivity(intent1);
					addActivityAnim();
					break;
				}
			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (TradeBaseActivity.isTransStatus) { // 关闭交易状态，状态控制
			Log.i("ckh", "设置主菜单关闭终端交易状态");
			TradeBaseActivity.isTransStatus = false;
			StandbyService.onOperate();
		}
		Log.i("ckh", "OtherActivity onResume()...");
	}

}
