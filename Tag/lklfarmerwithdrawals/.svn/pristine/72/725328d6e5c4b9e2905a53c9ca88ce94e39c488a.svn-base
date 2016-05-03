package com.centerm.lklcpos.view;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.lklcpos.transaction.entity.Shortcut;
import com.lkl.farmerwithdrawals.R;

/*
 * 主菜单上快捷方式的数据适配器
 */
public class MyListViewAdapter extends BaseAdapter {

	Context context;
	List<Shortcut> data;
	boolean hasText = true;

	public MyListViewAdapter(Context context, List<Shortcut> data) {
		super();
		this.context = context;
		this.data = data;
	}

	public MyListViewAdapter(Context context, List<Shortcut> data, boolean hasText) {
		super();
		this.context = context;
		this.data = data;
		this.hasText = hasText;
	}

	@Override
	public int getCount() {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_my, null);
		}
		ImageView iconImageView = (ImageView) convertView.findViewById(R.id.grid_item_icon_2);

		Shortcut mShortcut = (Shortcut) getItem(position);
		iconImageView.setBackgroundResource(mShortcut.getTrade_icon_id());

		if (hasText) {
			TextView textView = (TextView) convertView.findViewById(R.id.grid_item_name_2);
			textView.setText(mShortcut.getTrade_text_id());
		}

		return convertView;
	}
}
