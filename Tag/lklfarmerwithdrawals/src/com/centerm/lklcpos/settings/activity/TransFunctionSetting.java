package com.centerm.lklcpos.settings.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.SharePreferenceUtils;
import com.centerm.mid.util.M3Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * 交易功能设置Activity
 * 
 * @author Tianxiaobo
 *
 */
public class TransFunctionSetting extends BaseActivity implements OnClickListener {

	private static final Logger log = Logger.getLogger(TransFunctionSetting.class);
	// 公共组件
	private Map<String, String> dataMap = null; // 用来存储用户设置的各个参数的值
	private InputmethodCtrl ctrl = null; // 输入法控制类

	// 数据库操作接口
	private ParamConfigDao paramDao = null;
	// 右上角确认按钮
	private Button confirmSave = null;

	// 屏蔽交易开关设置
	private CheckBox consumeCheckBox;
	private CheckBox undoCheckBox;
	private CheckBox consumeSearchCheckBox;
	private CheckBox recedeCheckBox;
	private CheckBox preWarrantCheckBox;
	private CheckBox preWarrantUndoCheckBox;
	private CheckBox preWarrantOverCheckBox;
	private CheckBox preWarrantOverUndoCheckBox;
	private CheckBox upcashCheckBox;
	private CheckBox offRefundCheckBox; // 脱机退货
	private CheckBox cashUpCheckBox; // 现金充值
	private CheckBox cashUpViodCheckBox; // 现金充值撤销

	// 结算后自动签退
	private ImageView settleAutoSingout = null;
	// 小费百分比
	private EditText xiaofeiEt = null;
	// 消息重发次数
	private EditText messageResendTime = null;
	private EditText printNumEt = null; // 打印张数
	private EditText maxTransNum = null; // 最大交易笔数
	private ImageView isSurpportManualInputCardNo = null; // 是否支持手工输入卡号
	private ImageView isShowOther = null;
	private SharedPreferences.Editor mEditor = null; // 写xml文件
	private SharedPreferences mSharedPreferences = null; //

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		mSharedPreferences = TransFunctionSetting.this.getSharedPreferences("menu_settings", 0);
		mEditor = mSharedPreferences.edit();
		super.setContentView(R.layout.transaction_function_setting);
		inititle(); // 调用父类中的方法，初始化返回按钮
		paramDao = new ParamConfigDaoImpl(this);
		dataMap = new HashMap<String, String>();
		ctrl = InputmethodCtrl.getInstance();
		// 设置标题栏内容为终端参数设置
		TextView transType = (TextView) super.findViewById(R.id.transType);
		transType.setText(R.string.trans_function_setting); // 设置为终端通讯参数设置
		transType.setVisibility(View.VISIBLE); // 设置交易类型控件可见
		initComponent();
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
		// 屏蔽交易
		this.dealTransSwitch();
		// 结算后自动签退
		settleAutoSingout = (ImageView) super.findViewById(R.id.settle_auto_signout_switch);
		settleAutoSingout.setBackgroundResource(R.drawable.choose);
		settleAutoSingout.setEnabled(false);
		// 小费比例
		xiaofeiEt = (EditText) super.findViewById(R.id.xiaofeiVal);
		xiaofeiEt.setEnabled(false);
		xiaofeiEt.setText(R.string.such_function_hasnot_opened);
		// 消息重发次数
		messageResendTime = (EditText) super.findViewById(R.id.messageresendtimeVal);
		messageResendTime.setEnabled(false);
		messageResendTime.setText(R.string.such_function_hasnot_opened);
		// 打印张数
		printNumEt = (EditText) super.findViewById(R.id.printnumVal);
		printNumEt.setEnabled(false);
		printNumEt.setText(R.string.such_function_hasnot_opened);
		// 打印张数
		// 最大交易笔数
		String maxRecords = paramDao.get("systracemax");
		maxTransNum = (EditText) super.findViewById(R.id.maxtransnumVal);
		maxTransNum.setText(maxRecords);
		// 是否支持手输卡号
		isSurpportManualInputCardNo = (ImageView) super.findViewById(R.id.support_manual_input_card_noVal);
		isSurpportManualInputCardNo.setEnabled(false);
		SharedPreferences mSharedPreferences = this.getSharedPreferences("menu_settings", 0);
		if (mSharedPreferences.getBoolean("handinputcarnum", true)) {
			isSurpportManualInputCardNo.setImageResource(R.drawable.choose);
		} else {
			isSurpportManualInputCardNo.setImageResource(R.drawable.choose_no);
		}
		isShowOther = (ImageView) super.findViewById(R.id.other_show_img);
		String showother = SharePreferenceUtils.getPrefString(getApplicationContext(), "showother", "0");
		if("0".equals(showother)){
			isShowOther.setImageResource(R.drawable.choose_no);
		}
		else{
			isShowOther.setImageResource(R.drawable.choose);
		}
		isShowOther.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String showother = SharePreferenceUtils.getPrefString(getApplicationContext(), "showother", "0");
				if("0".equals(showother)){
					isShowOther.setImageResource(R.drawable.choose);
					SharePreferenceUtils.setPrefString(getApplicationContext(), "showother", "1");
				}
				else{
					isShowOther.setImageResource(R.drawable.choose_no);
					SharePreferenceUtils.setPrefString(getApplicationContext(), "showother", "0");
				}
			}
		});
		/*
		 * final SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		 * isSurpportManualInputCardNo.setOnCheckedChangeListener(new
		 * CompoundButton.OnCheckedChangeListener(){ //复选框监听事件
		 * 
		 * @Override public void onCheckedChanged(CompoundButton buttonView,
		 * boolean isChecked) { if(isChecked){
		 * mEditor.putBoolean("handinputcarnum", true); }else{
		 * mEditor.putBoolean("handinputcarnum", false); } mEditor.commit();
		 * M3Utility.sync(); //执行同步，让保存立即生效 } });
		 */

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
			String maxRecordsVal = maxTransNum.getText().toString().trim();
			if (this.isEmpty(maxRecordsVal)) {
				DialogFactory.showTips(this, "最大交易笔数不能为空");
				return;
			}
			dataMap.put("systracemax", String.valueOf(Integer.parseInt(maxRecordsVal)));
			int result = paramDao.update(dataMap);
			mEditor.commit();
			M3Utility.sync(); // 执行同步让保存立即生效
			if (result == dataMap.size()) {
				DialogFactory.showTips(this, "参数保存成功");
			} else {
				DialogFactory.showTips(this, "参数保存失败");
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
	 * 方法描述：是否支持手工输入处理函数
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-11-19 下午4:09:17
	 */
	public void surpportManual(View v) {
		SharedPreferences mSharedPreferences = this.getSharedPreferences("menu_settings", 0);
		final SharedPreferences.Editor mEditor = mSharedPreferences.edit();
		boolean isChecked = mSharedPreferences.getBoolean("handinputcarnum", false);
		if (isChecked) {
			mEditor.putBoolean("handinputcarnum", false);
			isSurpportManualInputCardNo.setImageResource(R.drawable.choose_no);
		} else {
			mEditor.putBoolean("handinputcarnum", true);
			isSurpportManualInputCardNo.setImageResource(R.drawable.choose);
		}
		mEditor.commit();
		M3Utility.sync(); // 执行同步，让保存立即生效
	}

	/**
	 * 方法描述：结算后自动签退
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-11-20 上午9:49:53
	 * @param v
	 */
	public void settleAutoSignout(View v) {

	}

	// 交易开关组件处理函数
	public void dealTransSwitch() {
		consumeCheckBox = (CheckBox) this.findViewById(R.id.consume_checkbox);
		undoCheckBox = (CheckBox) this.findViewById(R.id.undo_checkbox);
		consumeSearchCheckBox = (CheckBox) this.findViewById(R.id.consume_search_checkbox);
		recedeCheckBox = (CheckBox) this.findViewById(R.id.recede_checkbox);
		preWarrantCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_checkbox);
		preWarrantUndoCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_undo_checkbox);
		preWarrantOverCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_over_checkbox);
		preWarrantOverUndoCheckBox = (CheckBox) this.findViewById(R.id.pre_warrant_over_undo_checkbox);
		upcashCheckBox = (CheckBox) this.findViewById(R.id.up_cash_checkbox);
		offRefundCheckBox = (CheckBox) this.findViewById(R.id.offline_refund_checkbox);
		cashUpCheckBox = (CheckBox) this.findViewById(R.id.cash_up_checkbox);
		cashUpViodCheckBox = (CheckBox) this.findViewById(R.id.cash_up_viod_checkbox);

		consumeCheckBox.setOnCheckedChangeListener(mChangeListener);
		undoCheckBox.setOnCheckedChangeListener(mChangeListener);
		consumeSearchCheckBox.setOnCheckedChangeListener(mChangeListener);
		recedeCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantUndoCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantOverCheckBox.setOnCheckedChangeListener(mChangeListener);
		preWarrantOverUndoCheckBox.setOnCheckedChangeListener(mChangeListener);
		upcashCheckBox.setOnCheckedChangeListener(mChangeListener);
		offRefundCheckBox.setOnCheckedChangeListener(mChangeListener);
		cashUpCheckBox.setOnCheckedChangeListener(mChangeListener);
		cashUpViodCheckBox.setOnCheckedChangeListener(mChangeListener);

		SharedPreferences mSP = this.getSharedPreferences("menu_settings", 0);
		consumeCheckBox.setChecked(mSP.getBoolean("consume", true));
		undoCheckBox.setChecked(mSP.getBoolean("undo", true));
		consumeSearchCheckBox.setChecked(mSP.getBoolean("consumeSearch", true));
		recedeCheckBox.setChecked(mSP.getBoolean("recede", false));
		preWarrantCheckBox.setChecked(mSP.getBoolean("preWarrant", false));
		preWarrantUndoCheckBox.setChecked(mSP.getBoolean("preWarrantUndo", false));
		preWarrantOverCheckBox.setChecked(mSP.getBoolean("preWarrantOver", false));
		preWarrantOverUndoCheckBox.setChecked(mSP.getBoolean("preWarrantOverUndo", false));
		upcashCheckBox.setChecked(mSP.getBoolean("upcash", true));
		offRefundCheckBox.setChecked(mSP.getBoolean("offRefund", false));
		cashUpCheckBox.setChecked(mSP.getBoolean("cashUp", false));
		cashUpViodCheckBox.setChecked(mSP.getBoolean("cashUpViod", false));
	}

	// 交易开关复选框监听器
	private CompoundButton.OnCheckedChangeListener mChangeListener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			int id = buttonView.getId();
			switch (id) {
			case R.id.consume_checkbox:
				mEditor.putBoolean("consume", consumeCheckBox.isChecked());
				break;
			case R.id.undo_checkbox:
				mEditor.putBoolean("undo", undoCheckBox.isChecked());
				break;
			case R.id.consume_search_checkbox:
				mEditor.putBoolean("consumeSearch", consumeSearchCheckBox.isChecked());
				break;
			case R.id.recede_checkbox:
				mEditor.putBoolean("recede", recedeCheckBox.isChecked());
				break;
			case R.id.pre_warrant_checkbox:
				mEditor.putBoolean("preWarrant", preWarrantCheckBox.isChecked());
				break;
			case R.id.pre_warrant_undo_checkbox:
				mEditor.putBoolean("preWarrantUndo", preWarrantUndoCheckBox.isChecked());
				break;
			case R.id.pre_warrant_over_checkbox:
				mEditor.putBoolean("preWarrantOver", preWarrantOverCheckBox.isChecked());
				break;
			case R.id.pre_warrant_over_undo_checkbox:
				mEditor.putBoolean("preWarrantOverUndo", preWarrantOverUndoCheckBox.isChecked());
				break;
			case R.id.up_cash_checkbox:
				mEditor.putBoolean("upcash", upcashCheckBox.isChecked());
				break;
			case R.id.offline_refund_checkbox:
				mEditor.putBoolean("offRefund", offRefundCheckBox.isChecked());
				break;
			case R.id.cash_up_checkbox:
				mEditor.putBoolean("cashUp", cashUpCheckBox.isChecked());
				break;
			case R.id.cash_up_viod_checkbox:
				mEditor.putBoolean("cashUpViod", cashUpViodCheckBox.isChecked());
				break;
			default:
				break;
			}
		}

	};
}
