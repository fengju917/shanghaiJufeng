/*
 * package com.centerm.lklcpos.activity;
 * 
 * import java.util.ArrayList; import java.util.List;
 * 
 * import com.centerm.lklcpos.LklcposActivityManager; import
 * com.centerm.lklcpos.R; import com.centerm.lklcpos.view.CommuFragment; import
 * com.centerm.lklcpos.view.ConnectFragment; import
 * com.centerm.lklcpos.view.ConsumeFragment; import
 * com.centerm.lklcpos.view.MenuFragment; import
 * com.centerm.lklcpos.view.OtherFragment; import
 * com.centerm.lklcpos.view.ResetPawFragment; import
 * com.centerm.lklcpos.view.SettingsItem; import
 * com.centerm.lklcpos.view.SettingsItemAdapter; import
 * com.centerm.lklcpos.view.SysFragment;
 * 
 * import android.os.Bundle; import android.support.v4.app.FragmentActivity;
 * import android.view.KeyEvent; import android.view.View; import
 * android.view.Window; import android.view.WindowManager; import
 * android.view.View.OnClickListener; import android.widget.AdapterView; import
 * android.widget.Button; import android.widget.ListView; import
 * android.widget.TextView;
 * 
 * public class LklcposSettingsActivity extends FragmentActivity {
 * 
 * private ListView listView;
 * 
 * @Override protected void onCreate(Bundle savedInstanceState) { // TODO
 * Auto-generated method stub super.onCreate(savedInstanceState);
 * requestWindowFeature(Window.FEATURE_NO_TITLE);
 * setContentView(R.layout.lklcpos_settings_layout);
 * 
 * inititle(); LklcposActivityManager.getActivityManager().addActivity(this);
 * 
 * //动态加载Fragment ConsumeFragment fragment = new ConsumeFragment();
 * getSupportFragmentManager().beginTransaction().add(R.id.setting_context,
 * fragment).commit();
 * 
 * //左边列表填充以及点击事件 listView = (ListView)findViewById(R.id.settingsListView);
 * List<SettingsItem> lists = initSettingsItem(); SettingsItemAdapter
 * settingsItemAdapter = new SettingsItemAdapter(this, lists);
 * listView.setAdapter(settingsItemAdapter);
 * 
 * listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 * 
 * @Override public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 * long arg3) { // TODO Auto-generated method stub switch (arg2) { case 0:
 * ConsumeFragment consumeFragment = new ConsumeFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * consumeFragment).commit(); break; case 1: SysFragment sysFragment = new
 * SysFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * sysFragment).commit(); break; case 2: MenuFragment menuFragment = new
 * MenuFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * menuFragment).commit(); break; case 3: CommuFragment commuFragment = new
 * CommuFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * commuFragment).commit(); break; case 4: ConnectFragment connectFragment = new
 * ConnectFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * connectFragment).commit(); break; case 5: ResetPawFragment resetPawFragment =
 * new ResetPawFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * resetPawFragment).commit(); break; case 6: OtherFragment otherFragment = new
 * OtherFragment();
 * getSupportFragmentManager().beginTransaction().replace(R.id.setting_context,
 * otherFragment).commit(); break;
 * 
 * default: break; } }
 * 
 * }); }
 * 
 * 
 * private List<SettingsItem> initSettingsItem() { List<SettingsItem> items =
 * new ArrayList<SettingsItem>(); items.add(new
 * SettingsItem(R.drawable.set_consume_btn, R.string.contact_param_set));
 * items.add(new SettingsItem(R.drawable.menu_settings_btn,
 * R.string.menu_settings_text)); items.add(new
 * SettingsItem(R.drawable.connect_set_btn, R.string.connect_set_text));
 * items.add(new SettingsItem(R.drawable.set_btn, R.string.system_param_set));
 * items.add(new SettingsItem(R.drawable.communicate_btn,
 * R.string.commu_param_set)); items.add(new
 * SettingsItem(R.drawable.operator_settings_btn, R.string.operator_pwd_reset));
 * items.add(new SettingsItem(R.drawable.operator_settings_btn,
 * R.string.other_set)); return items; }
 * 
 * public void inititle() { // back返回按钮 Button back = (Button)
 * findViewById(R.id.back); back.setOnClickListener(new OnClickListener() {
 * 
 * @Override public void onClick(View v) {
 * LklcposActivityManager.getActivityManager().removeActivity(
 * LklcposSettingsActivity.this);
 * overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out); } });
 * // 版本号 String vernum = StandbyActivity.VERSION; TextView version = (TextView)
 * findViewById(R.id.version); version.setText(vernum); }
 * 
 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { // TODO
 * Auto-generated method stub if (keyCode == KeyEvent.KEYCODE_HOME) { return
 * true; }else if(keyCode == KeyEvent.KEYCODE_BACK){
 * LklcposActivityManager.getActivityManager().removeActivity(
 * LklcposSettingsActivity.this);
 * overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
 * return true; } return super.onKeyDown(keyCode, event); } }
 */