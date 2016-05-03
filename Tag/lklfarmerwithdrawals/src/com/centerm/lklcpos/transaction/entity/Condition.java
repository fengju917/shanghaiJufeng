package com.centerm.lklcpos.transaction.entity;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * 条件类
 * 通过条件id，确认下一个组件
 */
public class Condition implements Parcelable {

	private String value; // 条件值
	private String nextComponentNodeId; // 条件id对应的组件节点id

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getNextComponentNodeId() {
		return nextComponentNodeId;
	}

	public void setNextComponentNodeId(String nextComponentNodeId) {
		this.nextComponentNodeId = nextComponentNodeId;
	}

	public static final Parcelable.Creator<Condition> CREATOR = new Creator<Condition>() {

		@Override
		public Condition createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			Condition mCondition = new Condition();
			mCondition.value = source.readString();
			mCondition.nextComponentNodeId = source.readString();
			return mCondition;
		}

		@Override
		public Condition[] newArray(int size) {
			// TODO Auto-generated method stub
			return new Condition[size];
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
		dest.writeString(value);
		dest.writeString(nextComponentNodeId);
	}

}
