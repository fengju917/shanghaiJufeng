package com.centerm.lklcpos;

import java.util.Stack;

import com.centerm.lklcpos.activity.QueryTransactionDetails;

import android.app.Activity;

/**
 * @author zhouhui @da2013-7-7
 *
 */
public class LklcposActivityManager {

	private static Stack<Activity> activityStack;
	private static LklcposActivityManager instance;
	public boolean isRemoving; // 正在删除栈中多个的activities (add for
								// 输密界面，交易结束时，出现安全模块异常)

	/**
	 * 实例化一个activity管理类
	 * 
	 * @return
	 */
	public static synchronized LklcposActivityManager getActivityManager() {
		if (instance == null) {
			instance = new LklcposActivityManager();
			activityStack = new Stack<Activity>();
		}
		return instance;
	}

	/**
	 * 把activity加入到管理类堆栈底,如果已经存在堆栈中将推入栈顶
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		if (isRemoving)
			isRemoving = false;
		if (activity != null) {
			int i = activityStack.indexOf(activity);
			if (i != -1) {
				// activity存在栈中
				activityStack.add(activity);
			} else {
				// activity不存在栈中
				activityStack.push(activity);
			}

		}
	}

	/**
	 * 返回堆栈中activity的数量
	 * 
	 * @return
	 */
	public int getActivityNum() {
		return activityStack.size();
	}

	/**
	 * 将activity推入栈顶
	 * 
	 * @param activity
	 */
	public void pushActivity(Activity activity) {
		int i = activityStack.indexOf(activity);
		if (i != -1) {
			activityStack.remove(i);
			activityStack.push(activity);
		}
	}

	/**
	 * 获得栈顶的activity
	 * 
	 * @return
	 */
	public Activity getTopActivity() {
		if ((activityStack != null) && (activityStack.size() > 0)) {
			return (Activity) activityStack.lastElement();
		} else {
			return null;
		}
	}

	/**
	 * 退出栈中指定activity
	 * 
	 * @param activity
	 */
	public void removeActivity(Activity activity) {
		if (activity != null) {
			int i = activityStack.indexOf(activity);
			if (i != -1) {
				activity.finish();
				activityStack.remove(activity);
				System.out.println("remove:" + activity.getClass().getName().toString());
				System.out.println("activityStack.size:" + activityStack.size());
				activity = null;
			}
		}
	}

	/**
	 * 清除掉除cls以外的其他类 全部退出的时候要使用removeAllActivity()
	 * 
	 * @param cls
	 */
	public void removeAllActivityExceptOne(Class<?> cls) {
		int size = activityStack.size();
		isRemoving = true;
		for (int i = 0; i < size; i++) {
			Activity activity = getTopActivity();
			if (activity == null) {
				break;
			}
			if (activity.getClass().equals(cls)) {
				continue;
			}
			removeActivity(activity);
		}
	}

	/**
	 * 将栈中的activity自顶而下删掉，直到指定的Class
	 * 
	 * @param cls
	 */
	public void removeActivityTo(Class<?> cls) {
		int size = activityStack.size();
		isRemoving = true;
		for (int i = 0; i < size; i++) {
			Activity activity = getTopActivity();
			if (activity == null) {
				break;
			}
			if (activity.getClass().equals(cls)) {
				break;
			}
			removeActivity(activity);
		}
	}

	/**
	 * 计算栈中指定类(cls)对象的个数 add by chenkehui @2013.07.10
	 */
	public int activityCount(Class<?> cls) {
		int count = 0;
		int size = activityStack.size();
		Activity activity = null;
		for (int i = 0; i < size; i++) {
			activity = activityStack.get(i);
			if (activity.getClass().equals(cls)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 退出所有activity
	 */
	public void removeAllActivity() {
		int size = activityStack.size();
		isRemoving = true;
		for (int i = 0; i < size; i++) {
			Activity activity = getTopActivity();
			if (activity != null) {
				removeActivity(activity);
			}
		}
		System.gc();
	}

}
