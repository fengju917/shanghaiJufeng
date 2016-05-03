package com.centerm.lklcpos.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import com.lkl.farmerwithdrawals.R;

/**
 * 系统参数配置
 * 
 * @author Administrator
 *
 */
public class Config {
	private Context context;
	private static Config config;
	private static final String defaultDomainList = "busid,termid,tdtm,series,rnd,pan,inpan,billno,amount";

	public synchronized static Config getInstance(Context context) {
		if (config == null) {
			try {
				config = new Config(context);
			} catch (Exception e) {
				e.printStackTrace();
				// System.exit(0);//配置错误,程序退出
			}
		}
		return config;
	}

	private Config(Context context) throws Exception {
		this.context = context;
	}

	/**
	 * 获取MAC
	 * 
	 * @param json
	 * @return
	 */
	public String getMac(String json) throws Exception {
		String domainStr = getConfig("macDomain", defaultDomainList);
		List<String> temp = new ArrayList<String>();
		String[] domainNameArr = domainStr.split(",");
		JSONObject jsonObj = new JSONObject(json);
		for (int i = 0; i < domainNameArr.length; i++) {
			try {
				temp.add(jsonObj.getString(domainNameArr[i]));
			} catch (Exception e) {
			}
		}
		String macBlock = Utility.getMacBlock(temp);
		return macBlock;
	}

	public String getConfig(String tag, String defaultValue) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream ins = null;
			if (context.getResources().getString(R.string.basepath).equals("file:///android_asset/")) {
				ins = context.getResources().getAssets().open("conf/sysparam.xml");
			} else {
				ins = new FileInputStream(
						new File(context.getResources().getString(R.string.baseSystemUri) + "conf/sysparam.xml"));
			}
			// 输入流编码设置，避免乱码的问题
			BufferedInputStream bis = new BufferedInputStream(ins);
			InputSource is = new InputSource(bis);
			is.setEncoding("utf-8");
			Document doc = builder.parse(is);
			NodeList eles = doc.getElementsByTagName(tag);
			if (eles == null || eles.getLength() == 0) {
				return defaultValue;
			} else {
				return eles.item(0).getChildNodes().item(0).getTextContent();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public void setPosParamConfig(String tag, String value) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream ins = null;

			if (context.getResources().getString(R.string.basepath).equals("file:///android_asset/")) {
				ins = context.getResources().getAssets().open("conf/posparam.xml");
			} else {
				ins = new FileInputStream(
						new File(context.getResources().getString(R.string.baseSystemUri) + "conf/posparam.xml"));
			}
			Document doc = builder.parse(ins);
			NodeList eles = doc.getElementsByTagName(tag);
			if (eles == null || eles.getLength() == 0) {
				Log.i("ckh", "创建新节点 tag= [" + tag + "],value=[" + value + "]");
				Element el = doc.createElement(tag);
				el.setTextContent(value);
				Node root = doc.getDocumentElement();
				root.appendChild(el);
			} else {
				Log.i("ckh", "修改节点  tag= [" + tag + "],value=[" + value + "]");
				eles.item(0).setTextContent(value);
			}

			// 保存xml文件
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(doc);
			// 设置编码类型
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			StreamResult result = new StreamResult(new FileOutputStream(
					context.getResources().getString(R.string.baseSystemUri) + "conf/posparam.xml"));
			// 把DOM树转换为xml文件
			transformer.transform(domSource, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
