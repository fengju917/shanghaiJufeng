package com.centerm.lklcpos.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.lkl.farmerwithdrawals.R;

public class MenuSpaceScreenNumView extends LinearLayout {

	private Context mContext;
	private MenuSpace mMenuSpace;
	Drawable background = null;
	Drawable forwordground = null;

	private int mTotal = 1;
	private int mCurrent;

	private float mOffset;

	public boolean isDragFlag = false;

	private int ITEM_WIDTH;

	private int ITEM_HEIGHT;

	private int leftMargin;

	private int rightMargin;

	private int totalLength;

	// private static final int CURRENT_ITEM_WIDTH = 18 ;
	// private static final int CURRENT_ITEM_HEIGHT = 17 ;

	private Drawable mNormalPic = null;

	private Drawable mCurrentPic = null;

	private int mCurrentNum = -1;

	private int mScreenNum = -1;

	private int mPushAppScreen = -1;

	private Animation oldChildAnimationScale = null;

	private Animation newChildAnimationScale = null;

	public static final int OFFSET_LEFT = -1;
	public static final int OFFSET_RIGHT = 1;

	public MenuSpaceScreenNumView(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public MenuSpaceScreenNumView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	public void init() {
		initResources();// 初始化标记当前页面的小圆点样式
		ITEM_WIDTH = (int) mNormalPic.getIntrinsicWidth();
		ITEM_HEIGHT = (int) mNormalPic.getIntrinsicHeight();
		leftMargin = (int) mContext.getResources().getDimension(R.dimen.left_margin);// 获取小圆点之间的间距值
		// rightMargin =
		// (int)mContext.getResources().getDimension(R.dimen.right_margin);
		// totalLength =
		// (int)mContext.getResources().getDimension(R.dimen.total_length);
	}

	public MenuSpaceScreenNumView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = context;
	}

	// 功能选项的集合
	ArrayList<MenuScreenNumViewFrameLayout> childList = new ArrayList<MenuScreenNumViewFrameLayout>();

	private void recreate() {
		removeAllViews();
		childList.clear();
		setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ITEM_WIDTH, ITEM_HEIGHT);
		// marginLeft=getWidth()/2 - mScreenNum*ITEM_WIDTH/2 +
		// (mScreenNum-1)*ITEM_WIDTH/2;
		int y = 0;
		int t = 0;
		if (this.mScreenNum > 15) {
			lp.leftMargin = leftMargin;
			lp.rightMargin = rightMargin;
			if (mScreenNum - mCurrentNum > 3) {
				if (mCurrentNum > 3) {
					y = mCurrentNum - 3;
					t = mCurrentNum + 4;
				} else {
					y = 0;
					t = 7;
				}
			} else {
				t = mScreenNum;
				y = mScreenNum - 7;
			}
		} else {
			// lp.leftMargin =totalLength/mScreenNum;
			// lp.rightMargin = totalLength/mScreenNum;
			y = 0;
			t = mScreenNum;
		}
		lp.leftMargin = leftMargin;

		for (int i = 0; i < mScreenNum; i++) {
			MenuScreenNumViewFrameLayout child = new MenuScreenNumViewFrameLayout(mContext);
			if (i == mCurrentNum) {
				child.getImageViewBack().setImageDrawable(getNormalDrawable());
				child.getImageViewFront().setImageDrawable(getCurrentDrawable());
			} else {
				child.getImageViewFront().setImageDrawable(getNormalDrawable());
			}
			final int num = i;
			child.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (mMenuSpace != null) {
						mMenuSpace.snapToScreen(num);
					}
				}
			});
			childList.add(child);
			if (i >= y && i < t) {
				addView(child, lp);
			}
		}
		if (mScreenNum > 7 && mScreenNum - mCurrentNum > 3) {

		}
	}

	private void updateState() {
		for (int i = 0; i < mScreenNum; i++) {
			MenuScreenNumViewFrameLayout child = childList.get(i);
			if (i == mCurrentNum) {
				child.getImageViewBack().setImageDrawable(getNormalDrawable());
				child.getImageViewFront().setImageDrawable(getCurrentDrawable());
			} else {
				child.getImageViewFront().setImageDrawable(getNormalDrawable());
			}
		}
		if (mScreenNum > 7 && mScreenNum - mCurrentNum > 3) {

		}

	}

	public int initScreen(int mScreenNum, int mCurrentNum, int offset) {
		if (mScreenNum == 0) {
			return 0;
		}
		mCurrentNum += offset;
		mCurrentNum %= mScreenNum;

		mCurrentNum = mCurrentNum < 0 ? 0 : mCurrentNum;

		this.mCurrentNum = mCurrentNum;
		this.mScreenNum = mScreenNum;

		if (mScreenNum != childList.size()) {
			this.recreate();
		} else {
			this.updateState();
		}

		return mCurrentNum;
	}

	public void updateScreen(int gotoIndex, int totalNum, int offset) {
		int oldScreen = mCurrentNum < 0 ? 0 : mCurrentNum;
		gotoIndex = initScreen(totalNum, gotoIndex, offset);

		if (childList == null || childList.size() == 0 || gotoIndex > mScreenNum || oldScreen == mCurrentNum
				|| gotoIndex > (childList.size() - 1) || oldScreen > (childList.size() - 1)) {
			return;
		}
		MenuScreenNumViewFrameLayout newChild = childList.get(gotoIndex);
		if (newChild == null) {
			return;
		}
		MenuScreenNumViewFrameLayout oldChild = childList.get(oldScreen);
		if (oldChild == null) {
			return;
		}
		initAnimation();
		newChild.getImageViewBack().setImageDrawable(getNormalDrawable());
		newChild.getImageViewFront().setImageDrawable(getCurrentDrawable());
		newChild.getImageViewFront().startAnimation(newChildAnimationScale);

		oldChild.getImageViewBack().setImageDrawable(getNormalDrawable());
		oldChild.getImageViewFront().setImageDrawable(getCurrentDrawable());
		oldChild.getImageViewFront().startAnimation(oldChildAnimationScale);

		invalidate();
		requestLayout();
	}

	public void initAnimation() {
		if (newChildAnimationScale == null) {
			newChildAnimationScale = AnimationUtils.loadAnimation(mContext, R.anim.new_screenview);
		}
		if (oldChildAnimationScale == null) {
			oldChildAnimationScale = AnimationUtils.loadAnimation(mContext, R.anim.old_screenview);
			oldChildAnimationScale.setFillAfter(true);
		}
	}

	private void initResources() {
		mNormalPic = getNormalDrawable();
		mCurrentPic = getCurrentDrawable();
	}

	private Drawable getNormalDrawable() {
		return getResources().getDrawable(R.drawable.screen_dot_normal);
	}

	private Drawable getCurrentDrawable() {
		return getResources().getDrawable(R.drawable.screen_dot_current);
	}

	public String toString() {
		return "TotalNum: " + mScreenNum + "   currenScreen: " + mCurrentNum;
	}

	@Override
	public void setChildrenDrawingCacheEnabled(boolean enabled) {
		final int count = getChildCount();

		setDrawingCacheEnabled(enabled);
		// Update the drawing caches
		buildDrawingCache(true);

		for (int i = 0; i < count; i++) {
			final View view = getChildAt(i);
			view.setDrawingCacheEnabled(enabled);
			// Update the drawing caches
			view.buildDrawingCache(true);
		}
	}

	@Override
	public void setChildrenDrawnWithCacheEnabled(boolean enabled) {
		super.setChildrenDrawnWithCacheEnabled(enabled);
	}

	public MenuSpace getmMenuSpace() {
		return mMenuSpace;
	}

	public void setmMenuSpace(MenuSpace mMenuSpace) {
		this.mMenuSpace = mMenuSpace;
	}

}
