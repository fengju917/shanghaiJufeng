package com.centerm.android.adapter;

import com.centerm.lklcpos.LklcposApplication;
import com.lkl.farmerwithdrawals.R;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MyAdapterMoney extends BaseAdapter {

	private int[] selectmoney;

	public MyAdapterMoney(int[] selectmoney) {
		this.selectmoney = selectmoney;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return selectmoney.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			convertView = View.inflate(LklcposApplication.lklcposAppContext, R.layout.money_grid_item, null);
			vh = new ViewHolder((TextView) convertView.findViewById(R.id.bt_money));
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.bt.setText(selectmoney[position] + "");
		return convertView;
	}

}
