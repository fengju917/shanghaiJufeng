package com.centerm.lklcpos.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.centerm.lklcpos.service.StandbyService;
import com.centerm.lklcpos.transaction.entity.ComponentNode;
import com.centerm.lklcpos.transaction.entity.Condition;
import com.centerm.lklcpos.transaction.entity.Transaction;

/*
 * 交易组件的基类
 */
public class TradeBaseActivity extends BaseActivity {
	public static boolean isTransStatus = false;
	public static boolean isHttp = false;
	protected Transaction mTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 开启交易状态,状态控制
		if (!isTransStatus) {
			isTransStatus = true;
			StandbyService.onOperate();
			Log.i("ckh", "开启终端交易状态...");
		}

		// 获得交易流程对象
		mTransaction = getIntent().getParcelableExtra("transaction");

		// 设置交易流程对象中的当前组件节点号属性
		String action = getIntent().getAction();
		for (ComponentNode mComponentNode : mTransaction.getComponentNodeList()) {
			if (mComponentNode.getComponentName().equals(action)) {
				mTransaction.setCurNodeId(mComponentNode.getComponentId());
			}
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/*
	 * 当前组件驱动下一个组件
	 */
	public Intent forward(String value) {

		Intent nextIntent = new Intent();
		String nodeId = mTransaction.getCurNodeId();
		if (nodeId == null)
			return null;
		List<ComponentNode> nodeList = mTransaction.getComponentNodeList();

		// 遍历组件节点集，找出当前节点组件
		for (ComponentNode mComponentNode : nodeList) {
			if (nodeId.equals(mComponentNode.getComponentId())) {
				List<Condition> conditionList = mComponentNode.getConditionsList();
				// 遍历当前组件节点的条件集，找出条件集中条件值一样的条件对象
				for (Condition mCondition : conditionList) {
					if (value.equals(mCondition.getValue())) {
						String nextNodeId = mCondition.getNextComponentNodeId();
						// 下一个组件节点号为”0“，返回主菜单
						if ("0".equals(nextNodeId)) {
							nextIntent.setAction(mainMuneAction);
							// nextIntent.setPackage("com.lkl.farmerwithdrawals");
							// nextIntent.addCategory("com.lkl.help.farmers");
							return nextIntent;
						}
						// 再遍历组件节点集中，找出条件对象中组件节点号所对应的组件节点
						for (ComponentNode nextComponentNode : nodeList) {
							if (nextComponentNode.getComponentId().equals(nextNodeId)) {
								nextIntent.setAction(nextComponentNode.getComponentName());
								// nextIntent.setPackage("com.lkl.farmerwithdrawals");
								// nextIntent.addCategory("com.lkl.help.farmers");
								return nextIntent;
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if ("D".equals(mTransaction.getProperties())) { // 第三方调用时，home键响应返回交易失败给第三方
			if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KEYCODE_HOME) {
				lklcposActivityManager.removeAllActivityExceptOne(WebViewActivity.class);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
