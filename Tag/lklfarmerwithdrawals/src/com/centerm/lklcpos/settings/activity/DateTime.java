package com.centerm.lklcpos.settings.activity;

import java.util.Calendar;
import java.util.Locale;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.centerm.lklcpos.activity.BaseActivity;
import com.centerm.lklcpos.util.DialogFactory;
import com.centerm.mid.imp.socketimp.DeviceFactory;
import com.centerm.mid.inf.PINPadDevInf;
import com.lkl.farmerwithdrawals.R;

public class DateTime extends BaseActivity {

	public static final Logger log = Logger.getLogger(DateTime.class);

	private int my_year, my_month, my_day, my_hour, my_minute;
	private PINPadDevInf pinPad = null;
	private DatePicker date_picker;
	private TimePicker time_picker;
	private Handler handler = null;
	// private TextView text_view;
	private Calendar calendar;

	private boolean isUpdateTimeSuc = false;

	// 时间设置
	private Button cancelBut = null; // 取消
	private Button setBut = null; // 设置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_time_setting);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 0:
					DialogFactory.showTips(DateTime.this, "时间设置成功");
					break;
				case 1:
					DialogFactory.showTips(DateTime.this, "时间设置失败");
					break;
				}
				DateTime.this.finish();
				outActivityAnim();
			}
		};

		cancelBut = (Button) super.findViewById(R.id.dateTimeSetCancelBut);
		setBut = (Button) super.findViewById(R.id.dateTimeSetBut);

		// 设置时间为中国
		calendar = Calendar.getInstance(Locale.CHINA);
		// 获取日期
		my_year = calendar.get(Calendar.YEAR);
		my_month = calendar.get(Calendar.MONTH);
		my_day = calendar.get(Calendar.DAY_OF_MONTH);
		my_hour = calendar.get(Calendar.HOUR_OF_DAY);
		my_minute = calendar.get(Calendar.MINUTE);
		// 获取控件
		date_picker = (DatePicker) findViewById(R.id.datepicker);
		date_picker.setCalendarViewShown(false);
		time_picker = (TimePicker) findViewById(R.id.timepicker);
		// 设置时间格式为24小时
		// time_picker.setIs24HourView(true);
		// 显示时间
		// text_view.setText (my_year + "/" + (my_month + 1) + "/" + my_day +
		// " " + my_hour + ":" + my_minute);
		// 日历控件
		date_picker.init(my_year, my_month, my_day, new OnDateChangedListener() {
			// 日期修改的单击事件
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				my_year = year;
				my_month = monthOfYear;
				my_day = dayOfMonth;
				// 显示时间
				// text_view.setText(my_year + "/" + (my_month + 1) +
				// "/"+ my_day + " " + my_hour + ":" + my_minute);
			}
		});
		// 为时间控件添加事件
		time_picker.setOnTimeChangedListener(new OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				log.debug("txb hourOfDay = " + hourOfDay);
				my_hour = hourOfDay;
				log.debug("txb my_hour = " + my_hour);
				my_minute = minute;
				// 显示时间
				// text_view.setText(my_year + "/" + (my_month + 1) + "/" +
				// my_day + " " + my_hour + ":" + my_minute);

			}
		});
		// 取消按钮单击事件
		cancelBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DateTime.this.finish();
				outActivityAnim();
			}
		});

		// 设置按钮单击事件
		setBut.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// 设置终端日期和时间
				updateDateAndTime();
				DateTime.this.finish();
				outActivityAnim();
			}
		});
	}

	/**
	 * 方法描述：更新系统日期和时间
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-1-4 上午11:36:29
	 * @param 日期字符串
	 * @param 时间字符串
	 * @return
	 */
	public boolean updateDateAndTime() {
		Calendar calendar = Calendar.getInstance();
		try {
			int second = 0; // 设置时间，秒为0
			try {
				pinPad = null;
				pinPad = DeviceFactory.getPINPadDev();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("", e);
			}
			calendar.set(my_year, my_month, my_day, my_hour, my_minute, second);
			final long when = calendar.getTimeInMillis();
			if (when / 1000 < Integer.MAX_VALUE) {
				// 最关键的一句话
				new Thread() {
					public void run() {
						isUpdateTimeSuc = SystemClock.setCurrentTimeMillis(when); // 更新时间
						try {
							pinPad.open();
							pinPad.setRTCTime(when);
							handler.sendEmptyMessage(0);
							// pinPad.close();
						} catch (Exception e) {
							handler.sendEmptyMessage(1);
							log.debug(e.getMessage());
						}
					}
				}.start();

				log.debug("txb 系统时间是否更新成功：" + isUpdateTimeSuc);
			}
		} catch (Exception e) {
			log.debug("txb 更新系统时间出现异常" + e);
		}
		return isUpdateTimeSuc;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			if (null != pinPad) {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(300); // 子线程休眠300ms再去关闭密码键盘，防止出现socket
												// close
							pinPad.close();
							pinPad = null;
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
							log.error("", e);
						}
					}
				}.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
		}
	}
}