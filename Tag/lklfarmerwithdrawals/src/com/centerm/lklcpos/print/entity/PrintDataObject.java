package com.centerm.lklcpos.print.entity;

public class PrintDataObject {
	private String fontSize;
	private String isbold;
	private String iscenter;
	private String iswordWrap;
	private String lable;
	private String text;

	public PrintDataObject(String lable, String text) {
		this.lable = lable;
		this.text = text;
		this.fontSize = null;
		this.isbold = null;
		this.iscenter = null;
		this.iswordWrap = null;
	}

	public PrintDataObject(String lable, String text, String fontSize) {
		this.lable = lable;
		this.text = text;
		this.fontSize = fontSize;
		this.isbold = null;
		this.iscenter = null;
		this.iswordWrap = null;
	}

	public PrintDataObject(String lable, String text, String fontSize, String isbold, String iscenter,
			String iswordWrap) {
		this.lable = lable;
		this.text = text;
		this.fontSize = fontSize;
		this.isbold = isbold;
		this.iscenter = iscenter;
		this.iswordWrap = iswordWrap;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getIsbold() {
		return isbold;
	}

	public void setIsbold(String isbold) {
		this.isbold = isbold;
	}

	public String getIscenter() {
		return iscenter;
	}

	public void setIscenter(String iscenter) {
		this.iscenter = iscenter;
	}

	public String getIswordWrap() {
		return iswordWrap;
	}

	public void setIswordWrap(String iswordWrap) {
		this.iswordWrap = iswordWrap;
	}

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
