package com.centerm.lklcpos.view;

public class SettingsItem {
	private int item_icon;
	private int item_text;

	public SettingsItem() {

	}

	public SettingsItem(int icon, int text) {
		item_icon = icon;
		item_text = text;
	}

	public int getItem_icon() {
		return item_icon;
	}

	public void setItem_icon(int item_icon) {
		this.item_icon = item_icon;
	}

	public int getItem_text() {
		return item_text;
	}

	public void setItem_text(int item_text) {
		this.item_text = item_text;
	}
}
