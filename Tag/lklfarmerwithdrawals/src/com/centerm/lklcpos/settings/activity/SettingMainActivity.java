package com.centerm.lklcpos.settings.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.activity.MenuSpaceActivity;
import com.centerm.lklcpos.activity.TradeBaseActivity;
import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.transaction.entity.Transaction;
import com.centerm.lklcpos.view.MenuGridAdapter;
import com.centerm.lklcpos.view.MyListViewAdapter;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-25
 * 
 */
public class SettingMainActivity extends BaseActivity {

	private final Logger log = Logger.getLogger(SettingMainActivity.class);
	private ParamConfigDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingmainactivity);
		dao = new ParamConfigDaoImpl(this);
		inititle();

		ListView listview1 = (ListView) this.findViewById(R.id.listview1);
		List<Shortcut> shortcut = new ArrayList<Shortcut>();
		// 下载证书
		Shortcut view7 = new Shortcut(R.drawable.manage_download, R.string.downloadcertificate);
		shortcut.add(view7);
		// 终端参数设置
		Shortcut view0 = new Shortcut(R.drawable.term_param_setting_style, R.string.term_param_setting);
		shortcut.add(view0);
		// 通讯参数设置
		Shortcut view1 = new Shortcut(R.drawable.commu_param_setting_style, R.string.communication_param_setting);
		shortcut.add(view1);
		// 交易功能设置
		Shortcut view2 = new Shortcut(R.drawable.trans_function_setting_style, R.string.trans_function_setting);
		shortcut.add(view2);
		// 终端密钥管理
		Shortcut view3 = new Shortcut(R.drawable.settings_posmanager_btn, R.string.term_key_management);
		shortcut.add(view3);
		// 修改密码
		Shortcut view4 = new Shortcut(R.drawable.reset_paw_setting_style, R.string.change_password);
		shortcut.add(view4);
		// 其他功能
		Shortcut view5 = new Shortcut(R.drawable.other_function_style, R.string.other_function);
		shortcut.add(view5);
		// IC卡参数设置
		Shortcut view6 = new Shortcut(R.drawable.ic_card_param_setting_style, R.string.ic_param_management);
		shortcut.add(view6);

		// 自助开通管理
		Shortcut view8 = new Shortcut(R.drawable.selfopen, R.string.self_open);
		shortcut.add(view8);

		// MenuGridAdapter adapter = new
		// MenuGridAdapter(SettingMainActivity.this,
		// shortcut, true);

		MyListViewAdapter adapter = new MyListViewAdapter(SettingMainActivity.this, shortcut, true);

		listview1.setAdapter(adapter);
		// gridView.setAdapter(adapter);
		listview1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent mIntent = new Intent();
				switch (position) {
				case 0:
					Shortcut mShortcut = new Shortcut(SettingMainActivity.this, "transcation/M900004.xml"); // 下载证书
					mIntent = mShortcut.getmIntent();
					Transaction transaction = mIntent.getParcelableExtra("transaction");
					transaction.setProperties("settings");
					mIntent.putExtra("transaction", transaction);
					break;
				case 1:
					mIntent.setClass(SettingMainActivity.this, TermParamSetting.class).putExtra("goback_title", "设置");
					; // 启动终端参数设置界面
					break;
				case 2:
					// mIntent.setClass(SettingMainActivity.this,
					// MerchantParamSetting.class);
					mIntent.setClass(SettingMainActivity.this, CommunicationParamSetting.class).putExtra("goback_title",
							"设置");
					; // 通讯参数设置界面
					break;
				case 3:
					mIntent.setClass(SettingMainActivity.this, TransFunctionSetting.class).putExtra("goback_title",
							"设置"); // 交易功能设置
					break;
				case 4:
					mIntent.setClass(SettingMainActivity.this, PosKeyManager.class).putExtra("goback_title", "设置"); // 终端密钥管理
					break;
				case 5:
					mIntent.setClass(SettingMainActivity.this, ChangeTermPassword.class).putExtra("goback_title", "设置"); // 修改密码
					break;
				case 6: // 其他功能
					mIntent.setClass(SettingMainActivity.this, OtherFunctionSetting.class);
					break;
				case 7: // IC卡参数
					mIntent.setClass(SettingMainActivity.this, IcCardParamManager.class).putExtra("goback_title", "设置");
					break;

				case 8:
					mIntent.setClass(SettingMainActivity.this, SelfOpenActivity.class).putExtra("goback_title", "设置");
					break;
				/*
				 * case 9: mIntent.setClass(SettingMainActivity.this,
				 * IcCardParamManager.class).putExtra("goback_title", "设置");
				 * break;
				 */
				}
				startActivity(mIntent);
				addActivityAnim();
				mIntent = null;
			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (TradeBaseActivity.isTransStatus) { // 关闭交易状态，状态控制
			Log.i("ckh", "设置菜单关闭终端交易状态");
			TradeBaseActivity.isTransStatus = false;
			StandbyService.onOperate();
		}
		Log.i("ckh", "SettingMainActivity onResume()...");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		log.debug("激活状态 enabled = " + dao.get("enabled"));
		if (!"1".equals(dao.get("enabled"))) { // 未激活状态
			switch (keyCode) {
			case KEYCODE_HOME:
			case KeyEvent.KEYCODE_HOME:
			case KeyEvent.KEYCODE_BACK:
				gotoOneshotWelcome();
				break;
			default:
				break;
			}
		} else { // 已激活，回到主菜单
			int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
			if (count == 0) {
				Intent intent = new Intent();
				intent.setClass(this, MenuSpaceActivity.class);
				startActivity(intent);
				outActivityAnim();
			} else {
				lklcposActivityManager.removeActivity(this);
				outActivityAnim();
			}
			lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
		}
		return true;
	}

	public void inititle() {
		// back返回按钮
		Button back = (Button) findViewById(R.id.back);

		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.debug("激活状态 enabled = " + dao.get("enabled"));
				if (!"1".equals(dao.get("enabled"))) { // 未激活状态
					gotoOneshotWelcome();
				} else { // 已激活，回到主菜单
					int count = lklcposActivityManager.activityCount(MenuSpaceActivity.class);
					if (count == 0) {
						Intent intent = new Intent();
						intent.setClass(SettingMainActivity.this, MenuSpaceActivity.class);
						startActivity(intent);
						outActivityAnim();
					} else {
						lklcposActivityManager.removeActivity(SettingMainActivity.this);
						outActivityAnim();
					}
					lklcposActivityManager.removeAllActivityExceptOne(MenuSpaceActivity.class);
				}
			}
		});
	}

}
