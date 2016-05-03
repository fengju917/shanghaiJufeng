package com.centerm.lklcpos.settings.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.view.MenuGridAdapter;
import com.centerm.lklcpos.view.MyListViewAdapter;
import com.lkl.farmerwithdrawals.R;

public class PosKeyManager extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reprintmainactivity);

		inititle();

		ListView listview1 = (ListView) this.findViewById(R.id.listview1);
		List<Shortcut> shortcut = new ArrayList<Shortcut>();
		// 母POS导入密钥
		Shortcut view1 = new Shortcut(R.drawable.settings_posmanager_pos_btn, R.string.pos_input_key);
		shortcut.add(view1);
		// 其他设置
		Shortcut view2 = new Shortcut(R.drawable.settings_posmanager_setmeyid_btn, R.string.pos_setmkeyid);
		shortcut.add(view2);
		// 手工输入主密钥
		Shortcut view3 = new Shortcut(R.drawable.settings_posmanager_inputmak_btn, R.string.pos_input_mainkey);
		shortcut.add(view3);

		MyListViewAdapter adapter = new MyListViewAdapter(PosKeyManager.this, shortcut, true);

		listview1.setAdapter(adapter);
		listview1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent intent = new Intent();
				switch (position) {
				case 0:
					DialogFactory.showTips(PosKeyManager.this, "暂不支持该操作");
					break;
				case 1:
					intent.setClass(PosKeyManager.this, SetMkeyId.class);
					startActivity(intent);
					addActivityAnim();
					break;
				case 2:
					intent.setClass(PosKeyManager.this, OperLoadDownMak.class);
					startActivity(intent);
					addActivityAnim();
				default:
					break;
				}

			}

		});
	}

}
