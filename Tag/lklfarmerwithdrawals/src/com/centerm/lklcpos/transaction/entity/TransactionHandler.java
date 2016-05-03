package com.centerm.lklcpos.transaction.entity;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class TransactionHandler extends DefaultHandler {

	// xml文件中标签定义
	private final String Tag_Condition = "Condition";
	private final String Tag_componentName = "componentName";
	private final String Tag_ComponentNode = "ComponentNode";
	private final String Tag_Transaction = "Transaction";
	private final String Tag_mctCode = "mctCode";
	private final String Tag_curNodeId = "curNodeId";
	private final String Tag_value = "value";

	private StringBuilder sb = new StringBuilder();

	private Transaction mTransaction = null;
	private ComponentNode mComponentNode = null;
	private Condition mCondition = null;

	public Transaction getTransaction() {
		return mTransaction;
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		Log.i("ckh", "ReadTradeFile startDocument()...");
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		sb.setLength(0);
		if (Tag_Transaction.equals(localName)) {
			mTransaction = new Transaction();
			mTransaction.setMctCode(attributes.getValue(Tag_mctCode));
		} else if (Tag_ComponentNode.equals(localName)) {
			mComponentNode = new ComponentNode();
			mComponentNode.setComponentId(attributes.getValue(Tag_curNodeId));
		} else if (Tag_Condition.equals(localName)) {
			mCondition = new Condition();
			mCondition.setValue(attributes.getValue(Tag_value));
		}
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
		Log.i("ckh", "ReadTradeFile endDocument()...");
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);

		String value = sb.toString();
		if (Tag_ComponentNode.equals(localName)) {
			mTransaction.getComponentNodeList().add(mComponentNode);
		} else if (Tag_Condition.equals(localName)) {
			mCondition.setNextComponentNodeId(value);
			mComponentNode.getConditionsList().add(mCondition);
		} else if (Tag_componentName.equals(localName)) {
			mComponentNode.setComponentName(value);
		} else if (Tag_curNodeId.equals(localName)) {
			mComponentNode.setComponentId(value);
		}
	}

}
