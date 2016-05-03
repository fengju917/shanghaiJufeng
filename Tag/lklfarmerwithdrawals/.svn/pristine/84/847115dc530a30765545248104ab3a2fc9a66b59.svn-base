package com.centerm.lklcpos.activity;

import java.util.ArrayList;
import java.util.List;

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

/**
 * 圈存菜单
 * 
 * @author Administrator
 *
 */
public class TransferMenuActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.reprintmainactivity);

		inititle();

		final GridView gridView = (GridView) this.findViewById(R.id.gridView);
		List<Shortcut> shortcut = new ArrayList<Shortcut>();

		SharedPreferences mSP = getSharedPreferences("menu_settings", 0);
		// 现金充值
		if (mSP.getBoolean("cashUp", false)) {
			Shortcut view0 = new Shortcut(this, R.drawable.cash_value_btn, R.string.cash_up, "transcation/T630000.xml");
			view0.setJudgestate(true);
			view0.setNative(false);
			view0.setJudgeMaxTransRecords(true);
			shortcut.add(view0);
		}
		// 指定账户圈存
		Shortcut view1 = new Shortcut(this, R.drawable.account_transfer_btn, R.string.account_transfer,
				"transcation/T600001.xml");
		view1.setJudgestate(true);
		view1.setNative(false);
		view1.setJudgeMaxTransRecords(true);
		shortcut.add(view1);

		// 非指定账户圈存
		Shortcut view2 = new Shortcut(this, R.drawable.non_account_transfer_btn, R.string.non_account_transfer,
				"transcation/T620000.xml");
		view2.setJudgestate(true);
		view2.setNative(false);
		view2.setJudgeMaxTransRecords(true);
		shortcut.add(view2);

		// 现金充值撤销
		if (mSP.getBoolean("cashUpViod", false)) {
			Shortcut view3 = new Shortcut(this, R.drawable.cash_value_viod_btn, R.string.cash_up_viod,
					"transcation/T170000.xml");
			view3.setJudgestate(true);
			view3.setNative(false);
			view3.setPower("admin");
			view3.setJudgeMaxTransRecords(true);
			shortcut.add(view3);
		}

		MenuGridAdapter adapter = new MenuGridAdapter(TransferMenuActivity.this, shortcut, true);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				Shortcut mShortcut = (Shortcut) gridView.getItemAtPosition(arg2);

				Intent intent = mShortcut.getmIntent();
				Transaction transaction = intent.getParcelableExtra("transaction");
				transaction.setProperties("transfer");
				intent.putExtra("transaction", transaction);
				mShortcut.setmIntent(intent);
				judgeState(TransferMenuActivity.this, mShortcut);
			}

		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (TradeBaseActivity.isTransStatus) { // 关闭交易状态，状态控制
			Log.i("ckh", "圈存主菜单关闭终端交易状态");
			TradeBaseActivity.isTransStatus = false;
			StandbyService.onOperate();
		}
		Log.i("ckh", "OtherActivity onResume()...");
	}
}
