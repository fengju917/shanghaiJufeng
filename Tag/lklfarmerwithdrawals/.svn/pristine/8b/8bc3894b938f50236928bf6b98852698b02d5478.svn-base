package com.centerm.lklcpos.settings.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.mid.util.M3Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * 终端参数设置Activity
 * 
 * @author Tianxiaobo
 *
 */
public class CommunicationParamSetting extends BaseActivity implements OnClickListener {

	private static final Logger log = Logger.getLogger(CommunicationParamSetting.class);
	// 公共组件
	private Map<String, String> dataMap = null; // 用来存储用户设置的各个参数的值
	private InputmethodCtrl ctrl = null; // 输入法控制类

	// 数据库操作接口
	private ParamConfigDao paramDao = null;
	// 右上角确认按钮
	private Button confirmSave = null;
	// =======GPRS参数设置
	private EditText jieruhaomaEt = null; // 接入号码
	private EditText apn1NameEt = null; // APN1接入名称
	private EditText mainMachine1AddressEt = null; // 主机1IP地址
	private EditText mainMachine1PortEt = null; // 主机1端口
	private EditText mainMachine2AddressEt = null; // 主机2Ip地址
	private EditText mainMachine2PortEt = null; // 主机2端口
	private ImageView isNeedUserName = null; // 是否需要用户名
	private EditText gprsUserNameEt = null; // gprs用户名
	private EditText gprsPasswordEt = null; // gprs密码
	// 通讯参数设置
	private EditText tpduEt = null; // tpdu
	private EditText connectAddressEt = null; // 连接地址
	private EditText downloadAddressEt = null; // 下载地址
	private EditText activateEditText = null; // 激活地址
	private EditText masterkeyEditText = null; // TMK地址

	private ImageView isprecall = null; // 是否预拨号
	private EditText transactionTimeoutEt = null; // 交易超时时间
	private EditText transactionRepeatTimeEt = null; // 交易重拨次数
	private EditText transactionFlushTimeEt = null; // 冲正次数

	// add by txb 20131219 用于通讯方式选择
	private TextView connectModeTv = null; // 当前通讯内容
	private String curTransUrl = null; // 当前交易地址
	private String curDownloadUrl = null; // 当前下载地址
	private String curActivateUrl = null; // 当前激活地址
	private String curMasterkeyUrl = null; // 当前TMK地址
	private String connectMode = "0";

	private String[] connectModes = new String[] { "3G-APN", "专网" };
	private int defaultSelectedIndex = 0; // 默认选中索引
	private int curSelectedIndex = -1; // 当前选中
	private AlertDialog.Builder connectModeDialog = null; // 通讯方式选择框
	private Dialog dialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.setContentView(R.layout.communication_param_setting);
		inititle(); // 调用父类中的方法，初始化返回按钮
		paramDao = new ParamConfigDaoImpl(this);
		dataMap = new HashMap<String, String>();
		ctrl = InputmethodCtrl.getInstance();
		// 设置标题栏内容为终端参数设置
		TextView transType = (TextView) super.findViewById(R.id.transType);
		transType.setText(R.string.commuTvVal); // 设置为终端通讯参数设置
		transType.setVisibility(View.VISIBLE); // 设置交易类型控件可见
		initComponent();

		connectMode = paramDao.get("connect_mode");
		curTransUrl = paramDao.get("transIp");
		curDownloadUrl = paramDao.get("caIp");
		curActivateUrl = paramDao.get("OneKeyActivate");
		curMasterkeyUrl = paramDao.get("MasterKeySev");

		// 根据当前的url决定什么那个处于选中状态
		if ("0".equals(connectMode)) { // 3G-APN 服务器
			defaultSelectedIndex = 0;
			// setEditText(connectAddressEt, false);
			// setEditText(downloadAddressEt, false);
			// setEditText(activateEditText, false);
			// setEditText(masterkeyEditText, false);
		} else if ("1".equals(connectMode)) {// 公网服务器
			defaultSelectedIndex = 1;
			// setEditText(connectAddressEt, false);
			// setEditText(downloadAddressEt, false);
			// setEditText(activateEditText, false);
			// setEditText(masterkeyEditText, false);
		} else {
			defaultSelectedIndex = 0;
			// setEditText(connectAddressEt, true);
			// setEditText(downloadAddressEt, true);
			// setEditText(activateEditText, true);
			// setEditText(masterkeyEditText, true);
		}
		log.debug("defaultSelectedIndex =" + defaultSelectedIndex);

		connectModeTv = (TextView) this.findViewById(R.id.connectModeTv);
		connectModeTv.setText(connectModes[defaultSelectedIndex]);
	}

	/**
	 * 方法描述：注册监听事件
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-1-1 下午3:02:19
	 */
	public void setListener() {
		confirmSave.setOnClickListener(this); // 确认按钮注册监听事件
	}

	/**
	 * 方法描述：组件初始化
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-11-1 上午9:45:29
	 */
	public void initComponent() {
		// 确认按钮
		confirmSave = (Button) super.findViewById(R.id.confirmSave); // 确认保存

		// gprs设置部分内容一律做未开通处理
		jieruhaomaEt = (EditText) super.findViewById(R.id.jieruhaomaEt); // 接入号码
		jieruhaomaEt.setText(R.string.such_function_hasnot_opened);
		jieruhaomaEt.setEnabled(false);
		apn1NameEt = (EditText) super.findViewById(R.id.apn1NameEt); // APN1接入名称
		apn1NameEt.setText(R.string.such_function_hasnot_opened);
		apn1NameEt.setEnabled(false);
		mainMachine1AddressEt = (EditText) super.findViewById(R.id.mainMachine1AddressEt); // 主机1IP地址
		mainMachine1AddressEt.setText(R.string.such_function_hasnot_opened);
		mainMachine1AddressEt.setEnabled(false);
		mainMachine1PortEt = (EditText) super.findViewById(R.id.mainMachine1PortEt); // 主机1端口
		mainMachine1PortEt.setText(R.string.such_function_hasnot_opened);
		mainMachine1PortEt.setEnabled(false);
		mainMachine2AddressEt = (EditText) super.findViewById(R.id.mainMachine2AddressEt); // 主机2Ip地址
		mainMachine2AddressEt.setText(R.string.such_function_hasnot_opened);
		mainMachine2AddressEt.setEnabled(false);
		mainMachine2PortEt = (EditText) super.findViewById(R.id.mainMachine2PortEt); // 主机2端口
		mainMachine2PortEt.setText(R.string.such_function_hasnot_opened);
		mainMachine2PortEt.setEnabled(false);
		isNeedUserName = (ImageView) super.findViewById(R.id.isNeedUserName); // 是否需要用户名
		// isNeedUserName.setEnabled(false);
		gprsUserNameEt = (EditText) super.findViewById(R.id.gprsUserNameEt); // gprs用户名
		gprsUserNameEt.setText(R.string.such_function_hasnot_opened);
		gprsUserNameEt.setEnabled(false);
		gprsPasswordEt = (EditText) super.findViewById(R.id.gprspasswordEt); // gprs密码
		gprsPasswordEt.setText(R.string.such_function_hasnot_opened);
		gprsPasswordEt.setEnabled(false);
		// 通讯参数模块内容需要进行处理
		String tpduVal = paramDao.get("tpdu").toString().trim();
		tpduEt = (EditText) super.findViewById(R.id.tpduEt); // 获取tpdu
		tpduEt.setText(tpduVal);
		// 连接地址
		String connectAddress = paramDao.get("transIp").toString().trim();
		connectAddressEt = (EditText) super.findViewById(R.id.connectAddressEt); // 连接地址
		connectAddressEt.setText(connectAddress);
		connectAddressEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					ctrl.setInputModePinyin(); // 获取焦点，设置输入法模式为拼音
				} else {
					ctrl.setInputMode123(); // 失去焦点，设置输入法输入模式为123
				}
			}
		});
		// 下载地址
		String downloadAddress = paramDao.get("caIp").toString().trim();
		downloadAddressEt = (EditText) super.findViewById(R.id.downloadAddressEt); // 下载地址
		downloadAddressEt.setText(downloadAddress);
		downloadAddressEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					ctrl.setInputModePinyin(); // 获取焦点，设置输入法模式为拼音
				} else {
					ctrl.setInputMode123(); // 失去焦点，设置输入法输入模式为123
				}
			}
		});

		// 激活地址
		String activatetAddress = paramDao.get("OneKeyActivate");
		activateEditText = (EditText) super.findViewById(R.id.activate_address_edit); // ¼¤»îµØÖ·
		activateEditText.setText(activatetAddress);
		activateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					ctrl.setInputModePinyin(); // 获取焦点，设置输入法模式为拼音
				} else {
					ctrl.setInputMode123(); // 失去焦点，设置输入法输入模式为123
				}
			}
		});

		// TMK地址
		String masterkeyAddress = paramDao.get("MasterKeySev");
		masterkeyEditText = (EditText) super.findViewById(R.id.masterkey_address_edit); // ¼¤»îµØÖ·
		masterkeyEditText.setText(masterkeyAddress);
		masterkeyEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					ctrl.setInputModePinyin(); // 获取焦点，设置输入法模式为拼音
				} else {
					ctrl.setInputMode123(); // 失去焦点，设置输入法输入模式为123
				}
			}
		});

		// 是否预拨号
		isprecall = (ImageView) super.findViewById(R.id.isprecall); // 是否预拨号
		// isprecall.setEnabled(false); //设置预拨号组件不可用
		// 交易超时时间
		transactionTimeoutEt = (EditText) super.findViewById(R.id.transactionTimeoutEt); // 交易超时时间
		String transactionTimeoutVal = paramDao.get("dealtimeout");
		transactionTimeoutEt.setText(transactionTimeoutVal);
		// 交易重连次数
		transactionRepeatTimeEt = (EditText) super.findViewById(R.id.transactionRepeatTimeEt); // 交易重连次数
		String reConnectTime = paramDao.get("reconntimes");
		transactionRepeatTimeEt.setText(reConnectTime);
		// 冲正次数
		transactionFlushTimeEt = (EditText) findViewById(R.id.transactionFlushesTimeEt);
		String flushTime = paramDao.get("reversetimes");
		transactionFlushTimeEt.setText(flushTime);
		// 组件初始化完成之后注册事件监听
		setListener();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		ctrl.setInputMode123(); // Activity退出的时候设置输入模式为123
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		initComponent();
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	/**
	 * 单击事件处理函数，用case进行区分点击那个按钮
	 */
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.confirmSave: // 确认修改所有参数
			// 收集当前界面显示的所有控件内容值，放入dataMap中进行存库
			String tpduVal = tpduEt.getText().toString().trim();
			if (this.isEmpty(tpduVal) || tpduVal.length() != 10) {
				DialogFactory.showTips(this, "tpdu为空或长度不足10位");
				return;
			}
			dataMap.put("tpdu", tpduVal); // tpdu内容存储

			String activateAddress = activateEditText.getText().toString(); // 激活地址
			if (this.isEmpty(activateAddress)) {
				DialogFactory.showTips(this, "激活地址不能为空");
				return;
			}
			dataMap.put("OneKeyActivate", activateAddress);
			dataMap.put("connect_mode", connectMode);

			String masterkeyAddress = masterkeyEditText.getText().toString(); // TMK地址
			if (this.isEmpty(masterkeyAddress)) {
				DialogFactory.showTips(this, "TMK地址不能为空");
				return;
			}
			dataMap.put("MasterKeySev", masterkeyAddress);

			String connectAddress = connectAddressEt.getText().toString().trim(); // 连接地址
			if (this.isEmpty(connectAddress)) {
				DialogFactory.showTips(this, "连接地址输入不能为空");
				return;
			}
			dataMap.put("transIp", connectAddress);

			String downloadAddress = downloadAddressEt.getText().toString().trim();
			if (this.isEmpty(downloadAddress)) {
				DialogFactory.showTips(this, "下载地址输入不能为空");
				return;
			}
			dataMap.put("caIp", downloadAddress);

			if ("0".equals(connectMode)) { // 3G-APN
				// if("200021".equals(paramDao.get("fid"))){ //测试
				if ("test".equals(paramDao.get("env"))) { // 测试
					dataMap.put("test_3gapn_actUrl", activateAddress);
					dataMap.put("test_3gapn_mastUrl", masterkeyAddress);
					dataMap.put("test_3gapn_tranUrl", connectAddress);
					dataMap.put("test_3gapn_certUrl", downloadAddress);
				} else {
					dataMap.put("produce_3gapn_actUrl", activateAddress);
					dataMap.put("proc_3gapn_mastUrl", masterkeyAddress);
					dataMap.put("produce_3gapn_tranUrl", connectAddress);
					dataMap.put("produce_3gapn_certUrl", downloadAddress);
				}
			} else if ("1".equals(connectMode)) { // 公网
				// if("200021".equals(paramDao.get("fid"))){ //测试
				if ("test".equals(paramDao.get("env"))) { // 测试
					dataMap.put("test_public_actUrl", activateAddress);
					dataMap.put("test_public_mastUrl", masterkeyAddress);
					dataMap.put("test_public_tranUrl", connectAddress);
					dataMap.put("test_public_certUrl", downloadAddress);
				} else {
					dataMap.put("produce_public_actUrl", activateAddress);
					dataMap.put("proc_public_mastUrl", masterkeyAddress);
					dataMap.put("produce_public_tranUrl", connectAddress);
					dataMap.put("produce_public_certUrl", downloadAddress);
				}
			}

			String transTimeout = transactionTimeoutEt.getText().toString().trim();
			if (this.isEmpty(transTimeout)) {
				DialogFactory.showTips(this, "交易超时时间输入不能为空");
				return;
			}
			int timeout = Integer.parseInt(transTimeout);
			if (timeout < 60 || timeout > 120) {
				DialogFactory.showTips(this, "交易超时时间范围不合法，应该为60s~120s之间");
				return;
			}
			dataMap.put("dealtimeout", transTimeout);
			String transRepeatTime = transactionRepeatTimeEt.getText().toString().trim();
			if (this.isEmpty(transRepeatTime)) {
				DialogFactory.showTips(this, "交易重连次数输入不能为空");
				return;
			}
			dataMap.put("reconntimes", transRepeatTime);
			String transFlushTime = transactionFlushTimeEt.getText().toString().trim();
			if (this.isEmpty(transFlushTime)) {
				DialogFactory.showTips(this, "冲正次数输入不能为空");
				return;
			}
			dataMap.put("reversetimes", transFlushTime);

			int updateResult = paramDao.update(dataMap);
			if (updateResult == dataMap.size()) {
				DialogFactory.showTips(this, "参数更新成功");
			} else {
				DialogFactory.showTips(this, "参数更新失败");
			}
			lklcposActivityManager.removeActivity(this);
			outActivityAnim();
			M3Utility.sync();
			break;
		default:
			return;
		}
	}

	/**
	 * 设置EditText属性
	 * 
	 * @param editText
	 * @param enabled
	 */
	// public static void setEditText(EditText editText , boolean enabled){
	//
	// editText.setCursorVisible(enabled); //设置输入框中的光标不可见
	// editText.setFocusable(enabled); //无焦点
	// editText.setFocusableInTouchMode(enabled); //触摸时也得不到焦点
	// if(enabled){
	// editText.setTextColor(Color.BLACK);
	// }else {
	// editText.setTextColor(Color.GRAY);
	// }
	// }

	/**
	 * 方法描述：判空操作
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-1-4 下午7:17:18
	 * @param val
	 * @return
	 */
	public boolean isEmpty(String val) {
		if (null == val || "".equals(val)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 方法描述：显示通讯模式选择框
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-12-19 上午10:17:35
	 * @param v
	 */
	public void showConnectMode(View v) {
		curSelectedIndex = -1;
		connectModeDialog = new AlertDialog.Builder(CommunicationParamSetting.this);
		connectModeDialog.setTitle("通讯方式");
		connectModeDialog.setSingleChoiceItems(connectModes, defaultSelectedIndex,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						curSelectedIndex = which;
						defaultSelectedIndex = which;
						connectModeTv.setText(connectModes[curSelectedIndex]);
						switch (which) {
						case 0:
							// 赋值当前文本框为选择对应的ip信息
							// if("200021".equals(paramDao.get("fid"))){ //测试
							if ("test".equals(paramDao.get("env"))) { // 测试
								connectAddressEt.setText(paramDao.get("test_3gapn_tranUrl"));
								downloadAddressEt.setText(paramDao.get("test_3gapn_certUrl"));
								activateEditText.setText(paramDao.get("test_3gapn_actUrl"));
								masterkeyEditText.setText(paramDao.get("test_3gapn_mastUrl"));
							} else {
								connectAddressEt.setText(paramDao.get("produce_3gapn_tranUrl"));
								downloadAddressEt.setText(paramDao.get("produce_3gapn_certUrl"));
								activateEditText.setText(paramDao.get("produce_3gapn_actUrl"));
								masterkeyEditText.setText(paramDao.get("proc_3gapn_mastUrl"));
							}
							// CommunicationParamSetting.setEditText(connectAddressEt,
							// false);
							// CommunicationParamSetting.setEditText(downloadAddressEt,
							// false);
							// CommunicationParamSetting.setEditText(activateEditText,
							// false);
							// CommunicationParamSetting.setEditText(masterkeyEditText,
							// false);

							break;
						case 1:
							// if("200021".equals(paramDao.get("fid"))){ //测试公网
							if ("test".equals(paramDao.get("env"))) { // 测试
								connectAddressEt.setText(paramDao.get("test_public_tranUrl"));
								downloadAddressEt.setText(paramDao.get("test_public_certUrl"));
								activateEditText.setText(paramDao.get("test_public_actUrl"));
								masterkeyEditText.setText(paramDao.get("test_public_mastUrl"));
							} else {
								connectAddressEt.setText(paramDao.get("produce_public_tranUrl"));
								downloadAddressEt.setText(paramDao.get("produce_public_certUrl"));
								activateEditText.setText(paramDao.get("produce_public_actUrl"));
								masterkeyEditText.setText(paramDao.get("proc_public_mastUrl"));
							}
							// CommunicationParamSetting.setEditText(connectAddressEt,
							// false);
							// CommunicationParamSetting.setEditText(downloadAddressEt,
							// false);
							// CommunicationParamSetting.setEditText(activateEditText,
							// false);
							// CommunicationParamSetting.setEditText(masterkeyEditText,
							// false);

							break;
						case 2:
							connectAddressEt.setText(paramDao.get("transIp"));
							downloadAddressEt.setText(paramDao.get("caIp"));
							activateEditText.setText(paramDao.get("OneKeyActivate"));
							masterkeyEditText.setText(paramDao.get("MasterKeySev"));

							// CommunicationParamSetting.setEditText(connectAddressEt,
							// true);
							// CommunicationParamSetting.setEditText(downloadAddressEt,
							// true);
							// CommunicationParamSetting.setEditText(activateEditText,
							// true);
							// CommunicationParamSetting.setEditText(masterkeyEditText,
							// true);
							break;
						default:
							break;
						}
						// Toast.makeText(CommunicationParamSetting.this,
						// "你选择的是" + connectModes[curSelectedIndex],
						// Toast.LENGTH_LONG).show();
						connectMode = "" + curSelectedIndex;
						dialog.dismiss();
					}

				});
		dialog = connectModeDialog.show();
		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME) {
					return true;
				}
				return false;
			}
		});
		return;
	}
}
