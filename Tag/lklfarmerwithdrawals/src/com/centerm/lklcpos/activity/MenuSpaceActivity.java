﻿package com.centerm.lklcpos.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.settings.activity.SettingMainActivity;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.DialogMessage;
import com.centerm.lklcpos.util.Utility;
import com.centerm.lklcpos.view.MenuGridAdapter;
import com.centerm.lklcpos.view.MenuSpace;
import com.centerm.lklcpos.view.MenuSpaceScreenNumView;
import com.lkl.farmerwithdrawals.R;

/*
 * 主菜单界面
 */
public class MenuSpaceActivity extends BaseActivity {
	public String ACTION_FINISH = "com.centerm.androidlklpos.finish";
	// public static boolean isHasMenuSpace = false;
	public Handler mStandbyHandler;

	private MenuSpace menuSpace;
	private MenuSpaceScreenNumView mNumView;
	private Context mContext;
	private Shortcut mShortcut;
	private LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menuspace_layout);

		inititle();
		mContext = this;

		menuSpace = (MenuSpace) findViewById(R.id.menu_space);
		mNumView = (MenuSpaceScreenNumView) findViewById(R.id.Menuspace_appscreen_num);
		menuSpace.setNumberView(mNumView);

		inflater = this.getLayoutInflater();
	}

	@Override
	public void inititle() {
		// TODO Auto-generated method stub
		super.inititle();
		Button back = (Button) this.findViewById(R.id.back);
		back.setVisibility(View.GONE);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i("ckh", "MenuSpaceActivity onStart()...");
		menuSpace.removeAllViews();

		// 加载主菜单图标
		List<Shortcut> mList = initList();
		// 需要生成几页
		int cellNum = (mList.size() / 9) + (mList.size() % 9 == 0 ? 0 : 1);
		for (int i = 0; i < cellNum; i++) {// 循环每页
			if (i == cellNum - 1) {// 最后一页处理
				// addCellLayout用于生成gridview，然后填充到菜单中
				menuSpace.addView(addCellLayout(mList.subList(i * 9, mList.size())));// 获取集合中剩余条目
				break;
			}
			menuSpace.addView(addCellLayout(mList.subList(i * 9, (i + 1) * 9)));// 每页9个获取条目
		}
		mList = null;

		menuSpace.updateNumberView(mNumView);
	}

	private View addCellLayout(List<Shortcut> mlist) {
		View cellView = inflater.inflate(R.layout.celllayout, null);
		final GridView gridview = (GridView) cellView.findViewById(R.id.gridview);
		MenuGridAdapter mGridAdapter = new MenuGridAdapter(this, mlist); // =======
		gridview.setAdapter(mGridAdapter);
		gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mShortcut = (Shortcut) gridview.getItemAtPosition(arg2);
				if (mShortcut.getTrade_icon_id() == R.drawable.menu_sign_out_btn) { // 判断点击的是否为退出
					startActivity(mShortcut.getmIntent());
					return;
				}
				// 判断是否为结算，做弹出对话框处理
				judgeState();
			}
		});

		return cellView;
	}

	/**
	 * 对状态进行判断
	 */
	private void judgeState() {
		// 支持重复签到
		if ("transcation/T910000.xml".equals(mShortcut.getFilePath())) {
			startActivity(mShortcut.getmIntent());
			addActivityAnim();
			return;
		}

		// 判断是否点击结算按钮，进行提示并且进行状态的判断
		if ("transcation/T900000.xml".equals(mShortcut.getFilePath())) {
			Dialog dialog = new DialogMessage(this).alert("提示", "确定进行结算？", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (Utility.getSignStatus(MenuSpaceActivity.this)) {
						Shortcut settle = new Shortcut(MenuSpaceActivity.this, "transcation/T900000.xml");
						startActivity(settle.getmIntent());
						Utility.setSettleStatus(MenuSpaceActivity.this, true);
					} else {
						DialogFactory.showTips(MenuSpaceActivity.this, "请先签到再执行结算操作！");
					}
				}
			}, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});
			return;
		}

		// 判断是否需要验证权限
		if (mShortcut.getPower() != null) {
			Intent authorityIntent = new Intent();
			if (mShortcut.getPower().equals("oper")) { // 调用验证运维人员密码
				authorityIntent.setClass(mContext, OperpswdActivity.class);
			} else if (mShortcut.getPower().equals("admin")) { // 调用验证管理员密码
				Log.i("ckh", "开启终端交易状态");
				TradeBaseActivity.isTransStatus = true; // add for bug14717
														// 验证管理员权限时，超时无对话框弹出问题
														// by chenkehui
														// @2013.07.27
				StandbyService.onOperate();
				authorityIntent.setClass(mContext, AuthorizeActivity.class);
			}
			if (mShortcut.isJudgestate()) {// 既需要验证权限，又要判定签到状态
				startActivityForResult(authorityIntent, 0);
			} else {
				startActivityForResult(authorityIntent, 2);
			}
			addActivityAnim();

		} else if (mShortcut.isJudgestate()) { // 只需要验证签到状态

			if (Utility.getSignStatus(MenuSpaceActivity.this)) {
				if (!"002308".equals(mShortcut.getmTransaction().getMctCode())) {
					// 判断是否结算完成
					if (Utility.getSettleStatus(MenuSpaceActivity.this) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(MenuSpaceActivity.this, "有未清算的数据，请先执行清算操作！");
						return;
					}

					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(MenuSpaceActivity.this)) {
							DialogFactory.showTips(MenuSpaceActivity.this, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					startActivity(mShortcut.getmIntent());
					addActivityAnim();
				} else {
					DialogFactory.showTips(MenuSpaceActivity.this, "终端已签到！");
				}

			} else {
				if (!mShortcut.getmTransaction().getMctCode().equals("002308")) {
					startActivityForResult(Utility.autoSign(), 1);
					addActivityAnim();
				} else {
					// 判断是否结算完成
					if (Utility.getSettleStatus(MenuSpaceActivity.this) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(MenuSpaceActivity.this, "有未清算的数据，请先执行清算操作！");
						return;
					}

					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(MenuSpaceActivity.this)) {
							DialogFactory.showTips(MenuSpaceActivity.this, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					Intent mIntent = mShortcut.getmIntent();
					if (mIntent != null) {
						startActivity(mIntent);
						addActivityAnim();
					}
				}
			}
		} else {
			// 判断是否结算完成
			if (Utility.getSettleStatus(MenuSpaceActivity.this) && !mShortcut.isNative()
					&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
				DialogFactory.showTips(MenuSpaceActivity.this, "有未清算的数据，请先执行清算操作！");
				return;
			}

			if (mShortcut.isJudgeMaxTransRecords()) {
				if (Utility.isMaxCount(MenuSpaceActivity.this)) {
					DialogFactory.showTips(MenuSpaceActivity.this, "当批次交易笔数已达上限，请先结算！");
					return;
				}
			}
			Intent mIntent = mShortcut.getmIntent();
			if (mIntent != null) {
				startActivity(mIntent);
				addActivityAnim();
			}
		}

	}

	// 权限验证返回结果处理
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Intent mIntent = shortcutIntent;
		// shortcutIntent = null;
		switch (requestCode) {
		case 0:
			TradeBaseActivity.isTransStatus = false;// add for bug14717
													// 验证管理员权限时，超时无对话框弹出问题 by
													// chenkehui @2013.07.27
			StandbyService.onOperate();
			if (resultCode == RESULT_OK) { // 权限验证成功，开始判断签到状态
				if (Utility.getSignStatus(MenuSpaceActivity.this)) {

					// 判断是否结算完成
					if (Utility.getSettleStatus(MenuSpaceActivity.this) && !mShortcut.isNative()
							&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
						DialogFactory.showTips(MenuSpaceActivity.this, "有未清算的数据，请先执行清算操作！");
						return;
					}

					if (mShortcut.isJudgeMaxTransRecords()) {
						if (Utility.isMaxCount(MenuSpaceActivity.this)) {
							DialogFactory.showTips(MenuSpaceActivity.this, "当批次交易笔数已达上限，请先结算！");
							return;
						}
					}
					startActivity(mShortcut.getmIntent());
					addActivityAnim();
				} else {
					startActivityForResult(Utility.autoSign(), 1);
					addActivityAnim();
				}
			} else {
				// TradeBaseActivity.isTransStatus = false;//add for bug14717
				// 验证管理员权限时，超时无对话框弹出问题 by chenkehui @2013.07.27
				// StandbyService.onOperate();
				// DialogFactory.showTips(MenuSpaceActivity.this, "权限验证失败！");
			}

			break;
		case 1:
			if (resultCode == RESULT_OK) { // 自动签到成功
				// DialogFactory.showTips(MenuSpaceActivity.this, "自动签到成功");
				// 判断是否结算完成
				if (Utility.getSettleStatus(MenuSpaceActivity.this) && !mShortcut.isNative()
						&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
					DialogFactory.showTips(MenuSpaceActivity.this, "有未清算的数据，请先执行清算操作！");
					return;
				}

				if (mShortcut.isJudgeMaxTransRecords()) {
					if (Utility.isMaxCount(MenuSpaceActivity.this)) {
						DialogFactory.showTips(MenuSpaceActivity.this, "当批次交易笔数已达上限，请先结算！");
						return;
					}
				}
				startActivity(mShortcut.getmIntent());
				addActivityAnim();
			} else {
				DialogFactory.showTips(MenuSpaceActivity.this, "自动签到失败！");
			}
			break;
		case 2: // 只需要验证权限
			if (resultCode == RESULT_OK) {
				// 判断是否结算完成
				if (Utility.getSettleStatus(MenuSpaceActivity.this) && !mShortcut.isNative()
						&& !"transcation/T900000.xml".equals(mShortcut.getFilePath())) {
					DialogFactory.showTips(MenuSpaceActivity.this, "有未清算的数据，请先执行清算操作！");
					return;
				}
				if (mShortcut.isJudgeMaxTransRecords()) {
					if (Utility.isMaxCount(MenuSpaceActivity.this)) {
						DialogFactory.showTips(MenuSpaceActivity.this, "当批次交易笔数已达上限，请先结算！");
						return;
					}
				}
				startActivity(mShortcut.getmIntent());
				addActivityAnim();
			} /*
				 * else { DialogFactory.showTips(MenuSpaceActivity.this,
				 * "权限验证失败！"); }
				 */
			break;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (TradeBaseActivity.isTransStatus) { // 关闭交易状态，状态控制
			Log.i("ckh", "关闭终端交易状态");
			TradeBaseActivity.isTransStatus = false;
			StandbyService.onOperate();
		}
		Log.i("ckh", "MenuSpaceActivity onResume()...");

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		/*
		 * if (keyCode == KeyEvent.KEYCODE_HOME) { return true; }
		 */
		if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK) {
			StandbyService.onOperate();
			if (menuSpace.getCurrentScreen() != 0) {
				menuSpace.snapToScreen(0);
				return true;
			} else {
				Intent mIntent = new Intent();
				mIntent.setAction("com.lkl.farmer.dailog");
				// mIntent.setPackage("com.lkl.farmerwithdrawals");
				// mIntent.addCategory("android.intent.category.APP_EMAIL");
				startActivity(mIntent);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	// 填充主菜单快捷方式集
	private List<Shortcut> initList() {
		List<Shortcut> data = new ArrayList<Shortcut>();
		SharedPreferences mSP = getSharedPreferences("menu_settings", 0);
		// 助农取款
		if (mSP.getBoolean("consume", true)) {
			Shortcut consumeShortcut = new Shortcut(this, R.drawable.menu_consume_btn, R.string.consume_text,
					"transcation/O000003.xml");
			consumeShortcut.setJudgestate(true);
			consumeShortcut.setNative(false);
			consumeShortcut.setJudgeMaxTransRecords(true);
			data.add(consumeShortcut);
		}

		// 余额查询
		if (mSP.getBoolean("consumeSearch", true)) {
			Shortcut inquShortcut = new Shortcut(this, R.drawable.menu_balance_inquiry_btn,
					R.string.consume_search_text, "transcation/T300000.xml");
			inquShortcut.setJudgestate(true);
			inquShortcut.setNative(false);
			data.add(inquShortcut);
		}

		// 系统设置

		Shortcut settingShortcut = new Shortcut(R.drawable.menu_app_settings_btn, R.string.settings_text);
		settingShortcut.setmIntent(new Intent().setClass(MenuSpaceActivity.this, SettingMainActivity.class)
				.putExtra("goback_title", "主菜单"));
		settingShortcut.setJudgestate(false);
		settingShortcut.setNative(true);
		settingShortcut.setPower("oper");
		data.add(settingShortcut);

		// 签到
		Shortcut signShortcut = new Shortcut(this, R.drawable.menu_sign_in_btn, R.string.registration_text,
				"transcation/T910000.xml");
		signShortcut.setJudgestate(true);
		data.add(signShortcut);

		// 签退
		Shortcut settleShortcut = new Shortcut(this, R.drawable.menu_balance_btn, R.string.settlement_text,
				"transcation/T900000.xml");
		settleShortcut.setNative(false);
		data.add(settleShortcut);

		/*
		 * // 消费撤销 if (mSP.getBoolean("undo", true)) { Shortcut undoShortcut =
		 * new Shortcut(this, R.drawable.menu_revocate_consume,
		 * R.string.undo_text, "transcation/T200000.xml");
		 * undoShortcut.setPower("admin"); undoShortcut.setJudgestate(true);
		 * undoShortcut.setNative(false);
		 * undoShortcut.setJudgeMaxTransRecords(true); data.add(undoShortcut); }
		 */

		/*
		 * // 交易查询 Shortcut transShortcut = new Shortcut(
		 * R.drawable.menu_balance_search_btn,
		 * R.string.transaction_inquiries_text); transShortcut.setmIntent(new
		 * Intent().setClass(MenuSpaceActivity.this,
		 * QueryTransactionDetails.class)); transShortcut.setNative(true);
		 * data.add(transShortcut);
		 */

		/*
		 * // 退货 if (mSP.getBoolean("recede", false)) { // data.add(new
		 * Shortcut(R.drawable.logo, R.string.recede_text)); Shortcut
		 * returnGoods = new Shortcut(this, R.drawable.menu_return_goods,
		 * R.string.recede_text, "transcation/M200000.xml");
		 * returnGoods.setPower("admin"); returnGoods.setJudgestate(true);
		 * returnGoods.setNative(false);
		 * returnGoods.setJudgeMaxTransRecords(true); data.add(returnGoods); }
		 */

		/*
		 * // 预授权 if (mSP.getBoolean("preWarrant", false)) { Shortcut pre_Aoth =
		 * new Shortcut(this, R.drawable.menu_preauth, R.string.pre_warrant,
		 * "transcation/T030000.xml"); pre_Aoth.setJudgestate(true);
		 * pre_Aoth.setNative(false); pre_Aoth.setJudgeMaxTransRecords(true);
		 * data.add(pre_Aoth); }
		 */

		/*
		 * // 退出 Intent exitIntent = new Intent();
		 * exitIntent.setAction("com.lkl.farmer.dailog"); Shortcut exitShortcut
		 * = new Shortcut(R.drawable.menu_sign_out_btn, R.string.exit_text);
		 * exitShortcut.setmIntent(exitIntent); exitShortcut.setNative(true);
		 * data.add(exitShortcut);
		 */
		/*
		 * // 预授权完成 if (mSP.getBoolean("preWarrantOver", false)) { Shortcut
		 * pre_Aoth_over = new Shortcut(this, R.drawable.menu_preauth_complete,
		 * R.string.pre_warrant_over, "transcation/T000001.xml");
		 * pre_Aoth_over.setJudgestate(true); pre_Aoth_over.setNative(false);
		 * pre_Aoth_over.setJudgeMaxTransRecords(true); data.add(pre_Aoth_over);
		 * }
		 */

		/*
		 * // 预授权撤销 if (mSP.getBoolean("preWarrantUndo", false)) { Shortcut
		 * preWOShortcut = new Shortcut(this, R.drawable.menu_preauth_revocate,
		 * R.string.pre_warrant_undo, "transcation/T200001.xml");
		 * preWOShortcut.setPower("admin"); preWOShortcut.setJudgestate(true);
		 * preWOShortcut.setNative(false);
		 * preWOShortcut.setJudgeMaxTransRecords(true); data.add(preWOShortcut);
		 * }
		 */
		/*
		 * // 预授权完成撤销 if (mSP.getBoolean("preWarrantOverUndo", false)) {
		 * Shortcut preWOUShortcut = new Shortcut(this,
		 * R.drawable.menu_preauth_complete_revocate,
		 * R.string.pre_warrant_over_undo, "transcation/T200002.xml");
		 * preWOUShortcut.setPower("admin"); preWOUShortcut.setJudgestate(true);
		 * preWOUShortcut.setNative(false);
		 * preWOUShortcut.setJudgeMaxTransRecords(true);
		 * data.add(preWOUShortcut); }
		 * 
		 * // 管理 // data.add(new Shortcut(R.drawable.manage_btn,
		 * R.string.manage_text)); // note by txb 20130703 // Shortcut
		 * manageShortcut = new Shortcut(this, R.drawable.manage_btn, //
		 * R.string.manage_text, "transcation/M900004.xml"); //
		 * manageShortcut.setPower("admin"); //设置权限 // data.add(manageShortcut);
		 * 
		 * /* // 重打印 Shortcut reprintShortcut = new
		 * Shortcut(R.drawable.menu_reprint_btn, R.string.reprint_text);
		 * reprintShortcut.setmIntent(new Intent().setClass(
		 * MenuSpaceActivity.this, ReprintMainActivity.class)
		 * .putExtra("goback_title", "主菜单"));
		 * reprintShortcut.setJudgestate(false);
		 * reprintShortcut.setNative(true); data.add(reprintShortcut);
		 */

		/*
		 * //电子现金 if (mSP.getBoolean("upcash", true)) { Shortcut other = new
		 * Shortcut(R.drawable.menu_other_btn, R.string.upcash);
		 * other.setmIntent(new Intent().setClass( MenuSpaceActivity.this,
		 * OtherActivity.class) .putExtra("goback_title", "主菜单"));
		 * other.setJudgestate(false); other.setNative(true); data.add(other); }
		 */
		return data;
	}
}
