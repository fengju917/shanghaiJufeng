/*
 * package com.centerm.lklcpos.activity;
 * 
 * import com.centerm.lklcpos.LklcposActivityManager; import
 * com.centerm.lklcpos.R;
 * 
 * import android.os.Bundle; import android.support.v4.app.Fragment; import
 * android.support.v4.app.FragmentActivity; import android.view.KeyEvent; import
 * android.view.View; import android.view.Window; import
 * android.view.WindowManager; import android.view.View.OnClickListener; import
 * android.widget.Button; import android.widget.ImageView; import
 * android.widget.TextView;
 * 
 * 设置界面
 * 
 * public class SettingsActivity extends FragmentActivity implements
 * View.OnClickListener {
 * 
 * private TextView lableTextView; private ImageView clientImageView; private
 * ImageView sysImageView; private ImageView menuImageView; private ImageView
 * commuImageView; private ImageView resetImageView; private ImageView
 * connectImageView;
 * 
 * private Fragment[] mFragments;
 * 
 * @Override protected void onCreate(Bundle savedInstanceState) { // TODO
 * Auto-generated method stub super.onCreate(savedInstanceState);
 * requestWindowFeature(Window.FEATURE_NO_TITLE);
 * setContentView(R.layout.settings_layout);
 * //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
 * R.layout.title_layout);
 * 
 * inititle();
 * 
 * LklcposActivityManager.getActivityManager().addActivity(this);
 * 
 * clientImageView = (ImageView)findViewById(R.id.client_settings_icon);
 * sysImageView = (ImageView)findViewById(R.id.sys_settings_icon); menuImageView
 * = (ImageView)findViewById(R.id.menu_settings_icon); commuImageView =
 * (ImageView)findViewById(R.id.commu_settings_icon); resetImageView =
 * (ImageView)findViewById(R.id.resetpwd_settings_icon); connectImageView =
 * (ImageView)findViewById(R.id.connect_settings_icon);
 * 
 * clientImageView.setOnClickListener(this);
 * sysImageView.setOnClickListener(this);
 * menuImageView.setOnClickListener(this);
 * commuImageView.setOnClickListener(this);
 * resetImageView.setOnClickListener(this);
 * connectImageView.setOnClickListener(this);
 * 
 * mFragments = new Fragment[6]; mFragments[0] =
 * getSupportFragmentManager().findFragmentById(R.id.consume_settings);
 * mFragments[1] =
 * getSupportFragmentManager().findFragmentById(R.id.sys_settings);
 * mFragments[2] =
 * getSupportFragmentManager().findFragmentById(R.id.menu_settings);
 * 
 * mFragments[3] =
 * getSupportFragmentManager().findFragmentById(R.id.commu_settings);
 * mFragments[4] =
 * getSupportFragmentManager().findFragmentById(R.id.connect_settins);
 * mFragments[5] =
 * getSupportFragmentManager().findFragmentById(R.id.resetpwd_settings);
 * 
 * getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[1]).hide(mFragments[2])
 * .hide(mFragments[3]).hide(mFragments[4])
 * .hide(mFragments[5]).show(mFragments[0]).commit();
 * 
 * }
 * 
 * public void inititle() { // back返回按钮 Button back = (Button)
 * findViewById(R.id.back); back.setOnClickListener(new OnClickListener() {
 * 
 * @Override public void onClick(View v) { finish(); } }); // 版本号 String vernum
 * = StandbyActivity.VERSION; TextView version = (TextView)
 * findViewById(R.id.version); version.setText(vernum); }
 * 
 * @Override public void onClick(View v) { // TODO Auto-generated method stub
 * switch (v.getId()) { case R.id.client_settings_icon:
 * getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[1]).hide(mFragments[2])
 * .hide(mFragments[3]).hide(mFragments[4])
 * .hide(mFragments[5]).show(mFragments[0]).commit(); break; case
 * R.id.sys_settings_icon: getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[0]).hide(mFragments[2])
 * .hide(mFragments[3]).hide(mFragments[4]) .hide(mFragments[5])
 * .show(mFragments[1]).commit(); break; case R.id.menu_settings_icon:
 * getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[0]).hide(mFragments[1])
 * .hide(mFragments[3]).hide(mFragments[4]) .hide(mFragments[5])
 * .show(mFragments[2]).commit(); break; case R.id.commu_settings_icon:
 * getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[0]).hide(mFragments[1])
 * .hide(mFragments[2]).hide(mFragments[4]) .hide(mFragments[5])
 * .show(mFragments[3]).commit(); break; case R.id.connect_settings_icon:
 * getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[0]).hide(mFragments[1])
 * .hide(mFragments[2]).hide(mFragments[3]) .hide(mFragments[5])
 * .show(mFragments[4]).commit(); break; case R.id.resetpwd_settings_icon:
 * getSupportFragmentManager().beginTransaction()
 * .hide(mFragments[0]).hide(mFragments[1])
 * .hide(mFragments[2]).hide(mFragments[3]) .hide(mFragments[4])
 * .show(mFragments[5]).commit(); break; } }
 * 
 * 
 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { // TODO
 * Auto-generated method stub if (keyCode == KeyEvent.KEYCODE_HOME) { return
 * true; }else if(keyCode == KeyEvent.KEYCODE_BACK){
 * LklcposActivityManager.getActivityManager().removeActivity(SettingsActivity.
 * this);
 * 
 * return true; } return super.onKeyDown(keyCode, event); }
 * 
 * @Override protected void onDestroy() { // TODO Auto-generated method stub
 * super.onDestroy(); }
 * 
 * }
 */