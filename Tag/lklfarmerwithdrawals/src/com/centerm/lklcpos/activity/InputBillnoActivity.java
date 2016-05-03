package com.centerm.lklcpos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.dao.TransRecordDao;
import com.centerm.comm.persistence.entity.TransRecord;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;
import com.centerm.comm.persistence.impl.TransRecordDaoImpl;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.lklcpos.util.Utility;
import com.lkl.farmerwithdrawals.R;

/**
 * @author zhouhui @da2013-7-3
 * 
 */
public class InputBillnoActivity extends BaseActivity {

	private TransRecord transRecordItem;
	private TransRecordDao transRecordDao;
	private ParamConfigDao paramConfigDao;
	private EditText input_billno;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inputbillno);
		init();
	}

	private void init() {
		inititle();

		transRecordDao = new TransRecordDaoImpl(this);
		input_billno = (EditText) this.findViewById(R.id.input_billno);

		final Button confirm = (Button) this.findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent();
				intent.setClass(InputBillnoActivity.this, LocalConfirmInformation.class);
				String billno = input_billno.getText().toString();
				if (billno.length() < 6) {
					billno = Utility.addZeroForNum(billno, 6);
				}
				String batchno = getBatchno();
				transRecordItem = transRecordDao.getTransRecordByCondition(batchno, billno);

				if (transRecordItem != null) {
					intent.putExtra("billno", billno);
					intent.putExtra("batchno", batchno);
					startActivity(intent);
					addActivityAnim();
					lklcposActivityManager.removeActivity(InputBillnoActivity.this);
					/*
					 * map = TransactionUtility.transformToMap(transRecordItem);
					 * map.put("isReprints", "true");//本类中全为重打印
					 * intent.putExtra("type",
					 * transRecordItem.getTransprocode());
					 * intent.putExtra("cardnum",
					 * transRecordItem.getPriaccount());
					 * intent.putExtra("amount",
					 * transRecordItem.getTransamount());
					 * intent.putExtra("time", Utility
					 * .printFormatDateTime(transRecordItem .getTranslocaldate()
					 * + transRecordItem.getTranslocaltime()));
					 * intent.putExtra("batchbillno",
					 * transRecordItem.getBatchbillno());
					 * startActivityForResult(intent, 1); addActivityAnim();
					 */
				} else {
					DialogFactory.showTips(InputBillnoActivity.this, "暂无该记录或凭证号有误！");
				}
			}
		});
		/*
		 * input_billno.setOnEditorActionListener(new
		 * TextView.OnEditorActionListener() {
		 * 
		 * @Override public boolean onEditorAction(TextView arg0, int actionId,
		 * KeyEvent arg2) { // TODO Auto-generated method stub
		 * InputMethodManager inputMethodManager = (InputMethodManager)
		 * getSystemService(Context.INPUT_METHOD_SERVICE);
		 * 
		 * inputMethodManager.hideSoftInputFromWindow(
		 * InputBillnoActivity.this.getCurrentFocus() .getWindowToken(),
		 * 
		 * InputMethodManager.HIDE_NOT_ALWAYS); confirm.requestFocus(); return
		 * true; } });
		 */
	}

	/**
	 * 从配置表里面读取批次号
	 * 
	 * @return
	 */
	private String getBatchno() {
		paramConfigDao = new ParamConfigDaoImpl(this);
		String batchnoNum = paramConfigDao.get("batchno");
		return batchnoNum;
	}

	/*
	 * @Override protected void onActivityResult(int requestCode, int
	 * resultCode, Intent data) { // TODO Auto-generated method stub
	 * input_billno.setText(""); try { printDev = new PrintDev();
	 * printDev.openDev(); } catch (Exception e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } if (requestCode == 1) { isPrinting = true;
	 * if (resultCode == RESULT_OK) { printDev.printData(map, new CallBack() {
	 * 
	 * @Override public void isPrintSecond() { printDev.close(); Intent intent =
	 * new Intent(); intent.setClass(InputBillnoActivity.this,
	 * PrintAgianDialog.class); intent.putExtra("code",
	 * transRecordItem.getTransprocode()); startActivityForResult(intent, 2); }
	 * 
	 * @Override public void printExcept(int code, Bundle b) { printDev.close();
	 * if(code==0x30){ creatExceptDialog(InputBillnoActivity.this,b); }else{
	 * DialogFactory.showTips(InputBillnoActivity.this, "打印机异常请稍候再试！"); } } });
	 * } isPrinting = false; } else if (requestCode == 2) { isPrinting = true;
	 * if(resultCode !=RESULT_CANCELED){ map.put("isSecond", "true"); // 设置打印第二联
	 * printDev.printData(map, new CallBack() {
	 * 
	 * @Override public void isPrintSecond() { printDev.close();
	 * lklcposActivityManager.removeActivity(InputBillnoActivity.this); }
	 * 
	 * @Override public void printExcept(int code, Bundle b) { printDev.close();
	 * if(code==0x30){ creatExceptDialog(InputBillnoActivity.this,b); }else{
	 * DialogFactory.showTips(InputBillnoActivity.this, "打印机异常请稍候再试！"); } } });
	 * } isPrinting = false; } super.onActivityResult(requestCode, resultCode,
	 * data); }
	 */
}
