package com.centerm.comm.persistence.entity;

public class ParamConfig {

	// term_id ==终端号,mchnt_cd==商户号,front_serv_addr==前置地址,front_serv_port前置地址端口,
	// cert_serv_addr==证书地址,cert_serv_port==证书地址端口

	private String tagname;// 参数名称
	private String tagval;// 参数值

	public ParamConfig() {

	}

	public ParamConfig(String tagname, String tagval) {
		this.tagname = tagname;
		this.tagval = tagval;
	}

	public String getTagname() {
		return tagname == null ? "" : tagname;
	}

	public void setTagname(String tagname) {
		if (tagname != null) {
			this.tagname = tagname.trim();
		}
	}

	public String getTagval() {
		return tagval == null ? "" : tagval;
	}

	public void setTagval(String tagval) {
		if (tagval != null) {
			this.tagval = tagval.trim();
		}
	}
}