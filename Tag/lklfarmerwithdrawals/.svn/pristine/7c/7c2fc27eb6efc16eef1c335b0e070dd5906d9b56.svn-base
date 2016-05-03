package com.centerm.lklcpos.activity;

import java.text.DecimalFormat;
import java.util.Map;

import org.apache.log4j.Logger;

import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author zhouhui 信息确认组件 2013-6-26
 */
public class ConfirmInformation extends TradeBaseActivity {

	private static Logger logger = Logger.getLogger(ConfirmInformation.class);

	private LinearLayout itmeLayout, titleback;
	private LayoutInflater inflater;
	private boolean chageColor = true;
	private TextView titletype,tip_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confirminfo);
		// add by txb
		inititle();
		init();
	}

	private void init() {

		inflater = this.getLayoutInflater();
		itmeLayout = (LinearLayout) this.findViewById(R.id.itmeLayout);
		tip_tv = (TextView)this.findViewById(R.id.tip_tv);
		// titleback = (LinearLayout)this.findViewById(R.id.titleback);
		// = (TextView)this.findViewById(R.id.titletype);
		// 判断交易类型
		// String code = mTransaction.getDataMap().get("transprocode");
		String code = mTransaction.getMctCode();
		Map<String, String> dataMap = mTransaction.getDataMap();
		if ("002302".equals(code)) {// 消费操作请求
			// titleback.setBackgroundResource(R.drawable.btopbg_yellow);
			// titletype.setText("助农取款");
			// itmeLayout.addView(viewAdd("交易","助农取款"));
			String strmoney = dataMap.get("transamount");
//			String charge = dataMap.get("charge");
//			System.out.println("strmoney=" + strmoney);
//			System.out.println("charge=" + charge);
//			String tempMoney = strmoney;
//			try {
//				DecimalFormat df = new DecimalFormat("#0.00");
//				double f = Double.valueOf(strmoney);
//				if (charge != null && !"".equals(charge)) {
//					Double c = Double.valueOf(charge);
//					double a = f - c;
//					tempMoney = df.format(a);
//					System.out.println("a=" + a);
//					System.out.println("ma=" + df.format(a));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			String cardno = dataMap.get("priaccount");
			itmeLayout.addView(viewAdd("卡号:", cardno));
			itmeLayout.addView(viewAdd("金额:", strmoney + "元"));
//			itmeLayout.addView(viewAdd("手续费:", charge + "元"));
//			itmeLayout.addView(viewAdd("本次刷卡金额:", strmoney + "元"));
		} else if ("002303".equals(code) || "002324".equals(code)) {// 消费撤销操作、电子现金充值操作
			TransRecordDao mTransRecordDao = new TransRecordDaoImpl(this);
			String batchbillno = dataMap.get("batchbillno");
			TransRecord mTransRecord = mTransRecordDao.getConsumeByCondition(batchbillno.substring(0, 6),
					batchbillno.substring(6, 12));
			String cardno = mTransRecord.priaccount;
			String amount = Utility.unformatMount(mTransRecord.transamount);
			mTransaction.getDataMap().put("transamount", amount);

			// titleback.setBackgroundResource(R.drawable.btopbg_red);
			if ("002303".equals(code)) {
				// titletype.setText("消费撤销");
				itmeLayout.addView(viewAdd("原交易", "消费"));
			} else if ("002324".equals(code)) {
				// titletype.setText("现金充值撤销");
				itmeLayout.addView(viewAdd("原交易", "现金充值"));
			}
			itmeLayout.addView(viewAdd("卡号", Utility.formatCardNo(cardno)));
			itmeLayout.addView(viewAdd("金额", amount));
			itmeLayout.addView(viewAdd("凭证号", batchbillno.substring(6, 12)));
			itmeLayout.addView(viewAdd("授权码", mTransRecord.idrespcode));
		} /*
			 * else if ("000001".equals(code) || "000002".equals(code)){ //
			 * 电子现金普通消费和快速支付--联机 mTransaction.setMctCode("002302"); // 转消费流程
			 * titleback.setBackgroundResource(R.drawable.btopbg_yellow); if
			 * ("000002".equals(code)) { titletype.setText("电子现金普通消费");
			 * itmeLayout.addView(viewAdd("交易","电子现金普通消费")); } else if
			 * ("000001".equals(code)) { titletype.setText("电子现金快速支付");
			 * itmeLayout.addView(viewAdd("交易","电子现金快速支付")); }
			 * titletype.setText("消费"); itmeLayout.addView(viewAdd("交易","消费"));
			 * String cardno = dataMap.get("priaccount");
			 * itmeLayout.addView(viewAdd("卡号",Utility.formatCardNo(cardno)));
			 * itmeLayout.addView(viewAdd("金额",dataMap.get("transamount"))); }
			 */

		Button confirm = (Button) this.findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					Intent nextIntent = forward("1");
					nextIntent.putExtra("transaction", mTransaction);
					startActivity(nextIntent);
					addActivityAnim();
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("数据库操作异常", e);
				}

			}
		});

	}

	private View viewAdd(String key, String value) {

		View view = inflater.inflate(R.layout.confirminfo_item, null);
		TextView keyView = (TextView) view.findViewById(R.id.key);
		keyView.setText(key);
		TextView valueView = (TextView) view.findViewById(R.id.value);
		valueView.setText(value);
		// if(chageColor){
		// keyView.setBackgroundColor(getResources().getColor(R.color.changetrue));
		// valueView.setBackgroundColor(getResources().getColor(R.color.changetrue));
		// chageColor = false;
		// }else{
		// keyView.setBackgroundColor(getResources().getColor(R.color.changefalse));
		// valueView.setBackgroundColor(getResources().getColor(R.color.changefalse));
		// chageColor = true;
		// }
		return view;

	}
//	/**
//	 * 初始化手续费tip
//	 */
//	private void initTip(){
//		SpannableStringBuilder builder = new SpannableStringBuilder(tip_tv.getText().toString());  
//		  
//		//ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色  
//		ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);  
//		ForegroundColorSpan whiteSpan = new ForegroundColorSpan(Color.WHITE);  
//		ForegroundColorSpan blueSpan = new ForegroundColorSpan(Color.BLUE);  
//		ForegroundColorSpan greenSpan = new ForegroundColorSpan(Color.GREEN);  
//		ForegroundColorSpan yellowSpan = new ForegroundColorSpan(Color.YELLOW);  
//		  
//		  
//		  
//		builder.setSpan(redSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
//		builder.setSpan(whiteSpan, 1, 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
//		builder.setSpan(blueSpan, 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
//		builder.setSpan(greenSpan, 3, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
//		builder.setSpan(yellowSpan, 4,5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
//		  
//		tip_tv.setText(builder);  
//	}
}
