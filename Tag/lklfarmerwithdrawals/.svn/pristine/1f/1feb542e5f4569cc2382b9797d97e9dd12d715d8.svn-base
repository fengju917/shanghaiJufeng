package com.centerm.lklcpos.transaction.entity;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
/*
 * 组件节点
 */

public class ComponentNode implements Parcelable {

	private String componentId; // 当前组件号，用于遍历组件集
	private String componentName; // 当前组件名
	private List<Condition> conditionsList = new ArrayList<Condition>();
	// 当前组件有可能产生的下一组件的情况条件集

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public List<Condition> getConditionsList() {
		return conditionsList;
	}

	public void setConditionsList(List<Condition> conditionsList) {
		this.conditionsList = conditionsList;
	}

	public static final Parcelable.Creator<ComponentNode> CREATOR = new Creator<ComponentNode>() {

		@Override
		public ComponentNode createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			ComponentNode mComponentNode = new ComponentNode();
			mComponentNode.componentId = source.readString();
			mComponentNode.componentName = source.readString();

			source.readList(mComponentNode.conditionsList, getClass().getClassLoader());

			return mComponentNode;
		}

		@Override
		public ComponentNode[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ComponentNode[size];
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
		dest.writeString(componentId);
		dest.writeString(componentName);
		dest.writeList(conditionsList);
	}
}
