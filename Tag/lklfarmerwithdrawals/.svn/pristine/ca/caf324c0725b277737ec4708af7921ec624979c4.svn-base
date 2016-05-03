package com.centerm.lklcpos;

import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

import com.centerm.comm.persistence.dao.ParamConfigDao;
import com.centerm.comm.persistence.impl.ParamConfigDaoImpl;

public class PosParamHandler extends DefaultHandler {

	private String Tag_termid = "termid";
	private String Tag_merid = "merid";
	private String Tag_mchntname = "mchntname";
	private String Tag_dealtimeout = "dealtimeout";
	private String Tag_reconntimes = "reconntimes";
	private String Tag_tpdu = "tpdu";
	private String Tag_printAppVersionId = "printAppVersionId";
	private String Tag_batchno = "batchno";
	private String Tag_billno = "billno";
	private String Tag_transIp = "transIp";
	private String Tag_caIp = "caIp";

	private StringBuilder sb = new StringBuilder();
	private ParamConfigDao mParamConfigDao;
	private int count = 0;

	public PosParamHandler(Context mContext) {
		mParamConfigDao = new ParamConfigDaoImpl(mContext);
	}

	public int getUpdateCount() {
		return count;
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
		if (Tag_termid.equals(localName) || Tag_merid.equals(localName) || Tag_mchntname.equals(localName)
				|| Tag_dealtimeout.equals(localName) || Tag_reconntimes.equals(localName) || Tag_tpdu.equals(localName)
				|| Tag_printAppVersionId.equals(localName) || Tag_batchno.equals(localName)
				|| Tag_billno.equals(localName)) {

			mParamConfigDao.update(localName, value);
			count++;

		} else if (Tag_transIp.equals(localName) || Tag_caIp.equals(localName)) {
			if (value.startsWith("http://")) {
				URL url;
				try {
					url = new URL(value);
					value = url.getAuthority();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mParamConfigDao.update(localName, value);
			count++;
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
		if (Tag_termid.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_merid.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_mchntname.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_dealtimeout.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_reconntimes.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_tpdu.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_printAppVersionId.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_batchno.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_billno.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_transIp.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		} else if (Tag_caIp.equals(localName)) {
			if (!mParamConfigDao.isExist(localName)) {
				mParamConfigDao.save(localName, "");
			}
		}
	}
}
