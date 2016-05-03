package com.centerm.lklcpos.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.centerm.lklcpos.transaction.entity.ComponentNode;
import com.centerm.lklcpos.transaction.entity.Condition;
import com.centerm.lklcpos.transaction.entity.Transaction;

public class ErrInfoHandler extends DefaultHandler {

	private String Tag_error_info = "error-info";
	private String Tag_item = "item";
	private String Tag_type = "type";
	private String Tag_errcode = "errcode";
	private String Tag_tip_info = "tip-info";

	private StringBuilder sb = new StringBuilder();
	private ErrInfo mErrInfo = null;
	private List<ErrInfo> errInfos = null;

	public List<ErrInfo> getErrInfos() {
		return errInfos;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		sb.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
		String value = sb.toString();
		if (Tag_item.equals(localName)) {
			errInfos.add(mErrInfo);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		sb.setLength(0);
		if (Tag_error_info.equals(localName)) {
			errInfos = new ArrayList<ErrInfo>();
		} else if (Tag_item.equals(localName)) {
			mErrInfo = new ErrInfo();
			mErrInfo.setType(attributes.getValue(Tag_type));
			mErrInfo.setErrcode(attributes.getValue(Tag_errcode));
			mErrInfo.setTip_info(attributes.getValue(Tag_tip_info));
		}
	}

}
