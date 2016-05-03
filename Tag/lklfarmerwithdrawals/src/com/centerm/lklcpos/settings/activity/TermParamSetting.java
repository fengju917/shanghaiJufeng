package com.centerm.lklcpos.settings.activity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.centerm.android.input.InputmethodCtrl;
import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.centerm.mid.util.M3Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * 终端参数设置Activity
 * 
 * @author Tianxiaobo
 *
 */
public class TermParamSetting extends BaseActivity implements OnClickListener {

	private static final Logger log = Logger.getLogger(TermParamSetting.class);
	// 公共组件
	private Map<String, String> dataMap = null; // 用来存储用户设置的各个参数的值
	private InputmethodCtrl ctrl = null; // 输入法控制类

	// 数据库操作接口
	private ParamConfigDao paramDao = null;
	// 右上角确认按钮
	private Button confirmSave = null;
	// 商户号设置组件
	private EditText merchntEt = null;
	private TextView merchtTip = null;
	// 终端参数设置组件
	private EditText termnoEt = null;
	private TextView termno_tip = null;
	// 商户名称
	private EditText merchntNameEt = null;
	// 年份设置
	private EditText curYearEt = null;
	// 流水号
	private EditText curSerialNoEt = null;
	// 批次号
	private EditText curBatchNoEt = null;
	// 当前票据号
	private EditText curTicketNoEt = null;
	// 最大退货金额
	private EditText maxRegoodAmountEt = null;
	// 提示打印明细
	private ImageView isPrintDetailInfo_slipSwitch = null;
	// 商行代码
	private EditText merchnt_hang_codeEt = null;
	// 本地地区码
	private EditText local_area_codeEt = null;
	// 签购单是否打印英文
	private ImageView isPrintEnglish_slipSwitch = null;
	// 抬头内容选择
	private ImageView taitouSelect_slipSwitch = null;
	// 默认交易设置
	private RadioButton id_consume = null;
	private RadioButton id_preauth = null;
	// 消费撤销是否用卡
	private ImageView isConsumeRevocationUseCard_slipSwitch = null;
	// 预授权完成撤销是否用卡
	private ImageView isAuthorizationCompleteRevocationUseCard_slipSwitch = null;
	// 消费撤销是否需要输入密码
	private ImageView isConsumeRevocationInputPassword_slipSwitch = null;
	// 预授权撤销是否需要输入密码
	private ImageView isAuthorizationRevocationInputPassword_slipSwitch = null;
	// 预授权完成撤销是否需要输入密码
	private ImageView isAuthorizationCompleteRevocationInputPassword_slipSwitch = null;
	// 预授权请求是否需要输入密码
	private ImageView isAuthorizationCompleteRequestInputPassword_slipSwitch = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		super.setContentView(R.layout.term_param_setting);
		inititle(); // 调用父类中的方法，初始化返回按钮
		paramDao = new ParamConfigDaoImpl(this);
		dataMap = new HashMap<String, String>();
		// 设置标题栏内容为终端参数设置
		TextView transType = (TextView) super.findViewById(R.id.transType);
		transType.setText(R.string.term_param_setting);
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
		// 商户号设置处理逻辑
		String merchValue = paramDao.get("merid"); // 获取商户号
		merchntEt = (EditText) super.findViewById(R.id.mechnt_No);
		merchntEt.setText(merchValue); // 显示商户号
		merchtTip = (TextView) super.findViewById(R.id.merchnt_tip);
		// 获取流水号
		String serialNo = Utility.addZeroForNum(paramDao.get("systraceno"), 6); // add
																				// by
																				// txb
																				// 补足6位
		/*
		 * if(!"0".equals(serialNo)){ //如果存在流水记录，则不允许修改商户号
		 * merchntEt.setEnabled(false); //不允许编辑
		 * merchtTip.setVisibility(View.VISIBLE);
		 * merchtTip.setText(R.string.mercht_tip); }else{
		 * merchntEt.setEnabled(true); merchtTip.setVisibility(View.GONE);
		 * //提示信息不可见 }
		 */
		// 终端号设置处理逻辑
		String termNoVal = paramDao.get("termid");
		termnoEt = (EditText) super.findViewById(R.id.term_No); // 终端号编辑框
		termnoEt.setText(termNoVal);
		termno_tip = (TextView) super.findViewById(R.id.termno_tip);
		/*
		 * if(!"0".equals(serialNo)){ //如果存在流水记录，则不允许修改商户号
		 * termnoEt.setEnabled(false); //不允许编辑
		 * termno_tip.setVisibility(View.VISIBLE);
		 * termno_tip.setText(R.string.mercht_tip); }else{
		 * termnoEt.setEnabled(true); termno_tip.setVisibility(View.GONE);
		 * //提示信息不可见 }
		 */
		// 商户名称
		String merchntNameVal = paramDao.get("mchntname"); // 商户名称
		merchntNameEt = (EditText) super.findViewById(R.id.merchntNameEt);
		ctrl = InputmethodCtrl.getInstance();
		merchntNameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if (hasFocus) {
					ctrl.setInputModePinyin();
				} else {
					ctrl.setInputMode123();
				}
			}
		});
		merchntNameEt.setText(merchntNameVal);
		// 当前年份作暂未开通处理
		// String curYearVal = paramDao.get("curyear"); //获取当前年份
		curYearEt = (EditText) super.findViewById(R.id.curYearEt); // 年份控件
		curYearEt.setText(R.string.such_function_hasnot_opened);
		curYearEt.setEnabled(false); // 设置不可用
		// 流水号处理
		curSerialNoEt = (EditText) super.findViewById(R.id.curSerialNoEt);
		curSerialNoEt.setText(serialNo);
		// 批次号
		curBatchNoEt = (EditText) super.findViewById(R.id.curBatchNoEt);
		String curBatchNoVal = paramDao.get("batchno");
		curBatchNoEt.setText(curBatchNoVal);
		// 当前票据号
		curTicketNoEt = (EditText) super.findViewById(R.id.curTicketNoEt);
		String curTicketNoVal = paramDao.get("billno");
		curTicketNoEt.setText(curTicketNoVal);
		// 最大退货金额
		maxRegoodAmountEt = (EditText) super.findViewById(R.id.maxRegoodAmountEt);
		maxRegoodAmountEt.setText(R.string.such_function_hasnot_opened);
		maxRegoodAmountEt.setEnabled(false);
		// 是否提示打印明细
		isPrintDetailInfo_slipSwitch = (ImageView) super.findViewById(R.id.isPrintDetailInfo_slipSwitch);
		// isPrintDetailInfo_slipSwitch.setEnabled(false);
		// 商行代码
		merchnt_hang_codeEt = (EditText) super.findViewById(R.id.merchnt_hang_codeEt);
		merchnt_hang_codeEt.setText(R.string.such_function_hasnot_opened);
		merchnt_hang_codeEt.setEnabled(false);
		// 本地地区码
		local_area_codeEt = (EditText) super.findViewById(R.id.local_area_codeEt);
		local_area_codeEt.setText(R.string.such_function_hasnot_opened);
		local_area_codeEt.setEnabled(false);
		// 签购单是否打印英文
		isPrintEnglish_slipSwitch = (ImageView) super.findViewById(R.id.isPrintEnglish_slipSwitch);
		// isPrintEnglish_slipSwitch.setEnabled(false);
		// 抬头内容选择
		taitouSelect_slipSwitch = (ImageView) super.findViewById(R.id.taitouSelect_slipSwitch);
		// taitouSelect_slipSwitch.setEnabled(false);
		// 默认交易设置
		id_consume = (RadioButton) super.findViewById(R.id.id_consume);
		id_consume.setEnabled(false);
		id_preauth = (RadioButton) super.findViewById(R.id.id_preauth);
		id_preauth.setEnabled(false);
		// 消费撤销是否用卡
		isConsumeRevocationUseCard_slipSwitch = (ImageView) super.findViewById(
				R.id.isConsumeRevocationUseCard_slipSwitch);
		// isConsumeRevocationUseCard_slipSwitch.setEnabled(false);
		// 预授权完成撤销是否用卡
		isAuthorizationCompleteRevocationUseCard_slipSwitch = (ImageView) super.findViewById(
				R.id.isAuthorizationCompleteRevocationUseCard_slipSwitch);
		// isAuthorizationCompleteRevocationUseCard_slipSwitch.setEnabled(false);
		// 消费撤销是否需要输入密码
		isConsumeRevocationInputPassword_slipSwitch = (ImageView) super.findViewById(
				R.id.isConsumeRevocationInputPassword_slipSwitch);
		// isConsumeRevocationInputPassword_slipSwitch.setEnabled(false);
		// 预授权撤销是否需要输入密码
		isAuthorizationRevocationInputPassword_slipSwitch = (ImageView) super.findViewById(
				R.id.isAuthorizationRevocationInputPassword_slipSwitch);
		// isAuthorizationRevocationInputPassword_slipSwitch.setEnabled(false);
		// 预授权完成撤销是否需要输入密码
		isAuthorizationCompleteRevocationInputPassword_slipSwitch = (ImageView) super.findViewById(
				R.id.isAuthorizationCompleteRevocationInputPassword_slipSwitch);
		// isAuthorizationCompleteRevocationInputPassword_slipSwitch.setEnabled(false);
		// 预授权请求是否需要输入密码
		isAuthorizationCompleteRequestInputPassword_slipSwitch = (ImageView) super.findViewById(
				R.id.isAuthorizationCompleteRequestInputPassword_slipSwitch);
		// isAuthorizationCompleteRequestInputPassword_slipSwitch.setEnabled(false);
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
			String mechntVal = merchntEt.getText().toString().trim();
			if (this.isEmpty(mechntVal) || mechntVal.length() != 15) {
				DialogFactory.showTips(this, "商户号长度为空或不足15位");
				return;
			}
			dataMap.put("merid", mechntVal); // 商户号
			String termNo = termnoEt.getText().toString().trim();
			if (this.isEmpty(termNo) || termNo.length() != 8) {
				DialogFactory.showTips(this, "终端号为空或不足8位");
				return;
			}
			dataMap.put("termid", termNo); // 终端号
			String mechntName = merchntNameEt.getText().toString().trim();
			if (this.isEmpty(mechntName)) {
				DialogFactory.showTips(this, "商户名称不能为空");
				return;
			}
			dataMap.put("mchntname", mechntName);// 商户名称
			String systraceno = curSerialNoEt.getText().toString().trim();
			if (this.isEmpty(systraceno) || systraceno.length() != 6) {
				DialogFactory.showTips(this, "流水号不能为空或长度不足6位");
				return;
			}
			dataMap.put("systraceno", systraceno); // 流水号
			String batchNo = curBatchNoEt.getText().toString().trim();
			if (this.isEmpty(batchNo) || batchNo.length() != 6) {
				DialogFactory.showTips(this, "批次号不能为空或长度不足6位");
				return;
			}
			dataMap.put("batchno", Utility.addZeroForNum(batchNo, 6)); // 批次号
			String billNo = curTicketNoEt.getText().toString().trim();
			if (this.isEmpty(billNo)) {
				DialogFactory.showTips(this, "凭证号不能为空");
				return;
			}
			dataMap.put("billno", Utility.addZeroForNum(billNo, 6)); // 凭证号
			int saveResult = paramDao.update(dataMap); // 保存当前List中的值
			if (dataMap.size() == saveResult) {
				DialogFactory.showTips(this, "参数保存成功");
			} else {
				DialogFactory.showTips(this, "参数保存失败");
			}
			lklcposActivityManager.removeActivity(this);
			outActivityAnim();
			M3Utility.sync(); // 执行同步操作
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
}
