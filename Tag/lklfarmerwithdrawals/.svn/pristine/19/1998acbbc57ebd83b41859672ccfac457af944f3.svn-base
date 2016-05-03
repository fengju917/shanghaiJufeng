package com.centerm.lklcpos.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lkl.farmerwithdrawals.R;

/*
 * 设置模块左边listitem数据适配器
 */
public class SettingsItemAdapter extends BaseAdapter {

	Context context;
	List<SettingsItem> data;

	public SettingsItemAdapter(Context context, List<SettingsItem> data) {
		super();
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
		}
		ImageView iconImageView = (ImageView) convertView.findViewById(R.id.list_item_icon);

		SettingsItem mSettingsItem = (SettingsItem) getItem(position);
		iconImageView.setBackgroundResource(mSettingsItem.getItem_icon());
		TextView textView = (TextView) convertView.findViewById(R.id.list_item_text);
		textView.setText(mSettingsItem.getItem_text());

		return convertView;
	}

}
