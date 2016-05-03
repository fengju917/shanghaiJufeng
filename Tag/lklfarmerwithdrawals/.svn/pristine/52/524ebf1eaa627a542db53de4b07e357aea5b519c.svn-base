package com.centerm.lklcpos.transaction.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * 交易流程类
 */
public class Transaction implements Parcelable {
	private String mct_code; // 当前交易流程对应的8583组解包的码

	private String curNodeId; // 当前组件节点号

	private String properties; // 流程属性，默认为本地，“D”为第三方调用

	private List<ComponentNode> componentNodeList = new ArrayList<ComponentNode>(); // 当前交易涉及的所有节点集

	private Map<String, String> dataMap = new HashMap<String, String>();// 交易流程中存储输入数据

	private Map<String, String> resultMap = new HashMap<String, String>();// 交易流程中存储结果数据

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public String getMctCode() {
		return mct_code;
	}

	public void setMctCode(String mct_code) {
		this.mct_code = mct_code;
	}

	public String getCurNodeId() {
		return curNodeId;
	}

	public void setCurNodeId(String curNodeId) {
		this.curNodeId = curNodeId;
	}

	public List<ComponentNode> getComponentNodeList() {
		return componentNodeList;
	}

	public void setComponentNodeList(List<ComponentNode> componentNodeList) {
		this.componentNodeList = componentNodeList;
	}

	public Map<String, String> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, String> dataMap) {
		this.dataMap = dataMap;
	}

	public Map<String, String> getResultMap() {
		return resultMap;
	}

	public void setResultMap(Map<String, String> resultMap) {
		this.resultMap = resultMap;
	}

	/*
	 * 获取该交易流程的第一个节点
	 */
	public ComponentNode getFirstComponentNode() {
		return componentNodeList.get(0);
	}

	/*
	 * 判断下一步组件
	 */
	public ComponentNode getNextComponentNode(String value) {
		for (ComponentNode curComponentNode : componentNodeList) {
			if (curNodeId.equals(curComponentNode.getComponentId())) {
				List<Condition> mList = curComponentNode.getConditionsList();
				for (Condition mCondition : mList) {
					if (value.equals(mCondition.getValue())) {
						String nextNodeId = mCondition.getNextComponentNodeId();
						for (ComponentNode nextComponentNode : componentNodeList) {
							if (nextNodeId.equals(nextComponentNode.getComponentId()))
								return nextComponentNode;
						}
					}
				}
			}
		}
		return null;
	}

	public static final Parcelable.Creator<Transaction> CREATOR = new Creator<Transaction>() {

		@Override
		public Transaction createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			Transaction mTransaction = new Transaction();
			mTransaction.curNodeId = source.readString();
			mTransaction.mct_code = source.readString();
			mTransaction.properties = source.readString();
			mTransaction.dataMap = (Map<String, String>) source.readHashMap(getClass().getClassLoader());
			mTransaction.resultMap = (Map<String, String>) source.readHashMap(getClass().getClassLoader());
			source.readList(mTransaction.componentNodeList, getClass().getClassLoader());
			return mTransaction;
		}

		@Override
		public Transaction[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Transaction[size];
		}

	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(curNodeId);
		dest.writeString(mct_code);
		dest.writeString(properties);
		dest.writeMap(dataMap);
		dest.writeMap(resultMap);
		dest.writeList(componentNodeList);
	}

}
