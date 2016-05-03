
package com.centerm.lklcpos.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

/*
 * 主菜单可左右滑动功能的View
 */
public class MenuSpace extends LinearLayout {

	private static final int INVALID_SCREEN = -2;// 不需要滑动
	private final static int TOUCH_STATE_REST = 0;// 什么都没做的状态
	private final static int TOUCH_STATE_SCROLLING = 1;// 开始滑屏的状态

	private int mScrollingSpeed = 300;
	private int mScrollingBounce = 15;
	private static final int SNAP_VELOCITY = 600;

	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;

	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private float mLastMotionX;
	private float mLastMotionY;

	private int mTouchState = TOUCH_STATE_REST;
	private int mMaximumVelocity;
	private int mTouchSlop;
	private int mScrollX;
	private float mTouchX;
	// private static final float NANOTIME_DIV = 100000000.0f;
	// private float mSmoothingTime;
	private static final float BASELINE_FLING_VELOCITY = 1.f;
	private static final float FLING_VELOCITY_INFLUENCE = 0.01f;

	private MenuSpaceScreenNumView mNumView;

	public MenuSpace(Context context, AttributeSet attrs) {
		super(context, attrs);
		setHapticFeedbackEnabled(false);// 对触摸屏幕的处理
		initWorkspace();// 初始化设置
		requestFocus();// 请求触摸
	}

	private void initWorkspace() {
		mScroller = new Scroller(getContext());// 添加滑动功能
		mCurrentScreen = 0;// 当前的页面
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());// view的参数
		mTouchSlop = configuration.getScaledTouchSlop();// 移动多少距离才翻页
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();// 获得允许执行一个fling手势动作的最大速度值
	}

	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	protected Parcelable onSaveInstanceState() {// 保存之前的页面信息
		final SavedState state = new SavedState(super.onSaveInstanceState());
		state.currentScreen = mCurrentScreen;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {// 恢复之前的页面信息
		SavedState savedState = (SavedState) state;// 强转为保存状态类
		super.onRestoreInstanceState(savedState.getSuperState());
		if (savedState.currentScreen != -1) {
			mCurrentScreen = savedState.currentScreen;// 获取之前的屏幕状态
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ShortcutDock can only be used in EXACTLY mode.");
		}
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("ShortcutDock can only be used in EXACTLY mode.");
		}

		final int count = getScreenNum();// 根据子view的数量来判断
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		/*
		 * if (mFirstLayout) { setHorizontalScrollBarEnabled(false);
		 * scrollTo(mCurrentScreen * width, 0); mScroller.startScroll(0, 0,
		 * mCurrentScreen * width, 0, 0); mFirstLayout = false; }
		 */
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int childLeft = 0;
		final int count = getScreenNum();// 根据子view的数量来判断
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		boolean restore = false;
		int restoreCount = 0;

		// ViewGroup.dispatchDraw() supports many features we don't need:
		// clip to padding, layout animation, animation listener, disappearing
		// children, etc. The following implementation attempts to fast-track
		// the drawing dispatch by drawing only what we know needs to be drawn.

		// android.view.VelocityTracker主要用跟踪触摸屏事件
		// （flinging事件和其他gestures手势事件）的速率。
		// 用addMovement(MotionEvent)函数将Motion event加入到VelocityTracker类实例中.
		// 你可以使用getXVelocity() 或getXVelocity()获得横向和竖向的速率到速率时，
		// 但是使用它们之前请先调用computeCurrentVelocity(int)来初始化速率的单位 。

		boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
		// If we are not scrolling or flinging, draw only the current screen
		if (fastDraw) {
			if (getChildAt(mCurrentScreen) == null) {
				mCurrentScreen = 0;
				scrollTo(0, getScrollY());
			}
			drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
		} else {
			long drawingTime = getDrawingTime();
			int width = getWidth();
			float scrollPos = (float) getScrollX() / width;
			boolean endlessScrolling = true;

			int leftScreen;
			int rightScreen;
			boolean isScrollToRight = false;
			int childCount = getChildCount();
			if (scrollPos < 0 && endlessScrolling) {
				leftScreen = childCount - 1;
				rightScreen = 0;
			} else {
				leftScreen = Math.min((int) scrollPos, childCount - 1);
				rightScreen = leftScreen + 1;
				if (endlessScrolling) {
					rightScreen = rightScreen % childCount;
					isScrollToRight = true;
				}
			}

			if (isScreenNoValid(leftScreen)) {
				if (rightScreen == 0 && !isScrollToRight) { // 向左滑动，如果rightScreen是0
					int offset = childCount * width;
					canvas.translate(-offset, 0);
					drawChild(canvas, getChildAt(leftScreen), drawingTime);
					canvas.translate(+offset, 0);
				} else {
					drawChild(canvas, getChildAt(leftScreen), drawingTime);
				}
			}
			if (scrollPos != leftScreen && isScreenNoValid(rightScreen)) {
				if (endlessScrolling && rightScreen == 0 && isScrollToRight) {
					int offset = childCount * width;
					canvas.translate(+offset, 0);
					drawChild(canvas, getChildAt(rightScreen), drawingTime);
					canvas.translate(-offset, 0);
				} else {
					drawChild(canvas, getChildAt(rightScreen), drawingTime);
				}
			}
		}
	}

	private boolean isScreenNoValid(int screen) {
		return screen >= 0 && screen < getChildCount();
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		mTouchX = x;
		// mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} /*
			 * else if (mNextScreen != INVALID_SCREEN) { mCurrentScreen =
			 * Math.max(0, Math.min(mNextScreen, getScreenNum() - 1));
			 * mNextScreen = INVALID_SCREEN; }
			 */else if (mNextScreen != INVALID_SCREEN) {
			if (mNextScreen == -1) {
				mCurrentScreen = getChildCount() - 1;
				scrollTo(mCurrentScreen * getWidth(), getScrollY());
			} else if (mNextScreen == getChildCount()) {
				mCurrentScreen = 0;
				scrollTo(0, getScrollY());
			} else {
				mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
			}
			mNextScreen = INVALID_SCREEN;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		// 表示已经开始滑动了，不需要走该Action_MOVE方法了(第一次时可能调用)。
		// 该方法主要用于用户快速松开手指，又快速按下的行为。此时认为是出于滑屏状态的。
		if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);
			// final int touchSlop = mTouchSlop;
			final int touchSlop = 3;
			boolean xMoved = xDiff > touchSlop;
			boolean yMoved = yDiff > touchSlop;
			if (xMoved || yMoved) {
				if (xDiff > yDiff) {
					mTouchState = TOUCH_STATE_SCROLLING;
				}
			}
			break;
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();// 获得VelocityTracker类实例
		}
		mVelocityTracker.addMovement(ev);// 将事件加入到VelocityTracker类实例中
		final int action = ev.getAction();
		final float x = ev.getX();// 获得按下的X坐标
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 如果屏幕的动画还没结束，你就按下了，我们就结束上一次动画，即开始这次新ACTION_DOWN的动画
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x; // 记住开始落下的屏幕点
			break;

		case MotionEvent.ACTION_MOVE:
			final int deltaX = (int) (mLastMotionX - x);// 滑动的位移
			mLastMotionX = x;
			int tScrollX = getScrollX();
			if (getScreenNum() == 1) { // 单屏时，不能循环滑动
				int scrollX = deltaX;
				if (deltaX < 0) {
					if (scrollX < -mScrollingBounce) {
						scrollX = -mScrollingBounce;
					}
					if (tScrollX > -mScrollingBounce) {
						scrollBy(scrollX, 0);
					}
				} else if (deltaX > 0) {
					if (scrollX > mScrollingBounce) {
						scrollX = mScrollingBounce;
					}
					if (tScrollX < mScrollingBounce) {
						scrollBy(scrollX, 0);
					}
				}

			} else { // 循环滑动
				scrollBy((int) deltaX, 0);
				if (deltaX < 0) { // left
					if (mTouchX > 0) {
						mTouchX += Math.max(-mTouchX, deltaX);
						// mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						invalidate();
					} else if (true && mTouchX > -getWidth()) {
						mTouchX += deltaX;
						// mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						invalidate();
					}
				} else if (deltaX > 0) { // right
					final float availableToScroll = getChildAt(getChildCount() - 1).getRight() - mTouchX
							- (true ? 0 : getWidth());
					if (availableToScroll > 0) {
						mTouchX += Math.min(availableToScroll, deltaX);
						// mSmoothingTime = System.nanoTime() / NANOTIME_DIV;
						invalidate();
					}
				} else {
					awakenScrollBars();
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			/*
			 * final VelocityTracker velocityTracker = mVelocityTracker;
			 * velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			 * int velocityX = (int) velocityTracker.getXVelocity(); if
			 * (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
			 * snapToScreen(mCurrentScreen - 1); } else if (velocityX <
			 * -SNAP_VELOCITY && mCurrentScreen < getScreenNum() - 1) {
			 * snapToScreen(mCurrentScreen + 1); } else { if(mTouchState !=
			 * TOUCH_STATE_REST) { snapToDestination(); } } if (mVelocityTracker
			 * != null) { mVelocityTracker.recycle(); mVelocityTracker = null; }
			 * mTouchState = TOUCH_STATE_REST;
			 */
			if (getScreenNum() == 1) {
				if (mTouchState != TOUCH_STATE_REST) {
					snapToDestination();
					mTouchState = TOUCH_STATE_REST;
				}
				break;
			}
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;

				// computeCurrentVelocity(int units, float maxVelocity)
				// Compute the current velocity based on the points that have
				// been collected.
				// int units表示速率的基本时间单位。units值为1的表示是，一毫秒时间单位内运动了多少个像素，
				// units值为1000表示一秒（1000毫秒）时间单位内运动了多少个像素float maxVelocity表示速率的最大值
				// 设置maxVelocity值为0.01时，速率大于0.01时，显示的速率都是0.01,速率小于0.01时，显示正常
				// 使用getXVelocity ()、 getYVelocity ()函数来获得当前的速度

				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				final int velocityX = (int) velocityTracker.getXVelocity();// 计算速率
				final int screenWidth = getWidth();
				final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
				// final int whichScreen = (int)Math.floor((getScrollX() +
				// (screenWidth / 2.0)) / screenWidth);
				final float scrolledPos = (float) getScrollX() / screenWidth;
				// 滑动速率达到了一个标准(快速向右滑屏，返回上一个屏幕) 马上进行切屏处理
				if (velocityX > SNAP_VELOCITY && mCurrentScreen > -1) {
					// Fling hard enough to move left.
					// Don't fling across more than one screen at a time.
					final int bound = scrolledPos < whichScreen ? mCurrentScreen - 1 : mCurrentScreen;
					snapToScreen(Math.min(whichScreen, bound));
				} else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount()) { // 快速向左滑屏，返回下一个屏幕)
					// Fling hard enough to move right
					// Don't fling across more than one screen at a time.
					final int bound = scrolledPos > whichScreen ? mCurrentScreen + 1 : mCurrentScreen;
					snapToScreen(Math.max(whichScreen, bound));
				} else {
					if (mTouchState != TOUCH_STATE_REST) {// 我们是缓慢移动的，因此先判断是保留在本屏幕还是到下一屏幕
						snapToDestination();
					}
				}
			}
			mTouchState = TOUCH_STATE_REST;
			releaseVelocityTracker();

			break;

		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return true;
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void snapToDestination() {
		final int screenWidth = getWidth();
		// 当前的偏移位置
		// 判断是否超过下一屏的中间位置，如果达到就抵达下一屏，否则保持在原屏幕
		// 这样的一个简单公式意思是：假设当前滑屏偏移值即 scrollCurX 加上每个屏幕一半的宽度，除以每个屏幕的宽度就是
		// 我们目标屏所在位置了。 假如每个屏幕宽度为320dip, 我们滑到了500dip处，很显然我们应该到达第二屏
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
		snapToScreen(whichScreen);
	}

	public void snapToScreen(int whichScreen) {
		// whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() -
		// 1));
		whichScreen = Math.max(-1, Math.min(whichScreen, getChildCount()));
		mNextScreen = whichScreen;

		int gotoScreen = 0;
		if (mNextScreen < 0) {
			gotoScreen = getChildCount() - 1;
		} else if (mNextScreen > (getChildCount() - 1)) {
			gotoScreen = 0;
		} else {
			gotoScreen = mNextScreen;
		}
		mNumView.updateScreen(gotoScreen, getChildCount(), 0);

		View focusedChild = getFocusedChild();
		if (focusedChild != null && whichScreen != mCurrentScreen && focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}

		final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		int duration = (screenDelta + 1) * 100;

		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		duration += 100;

		awakenScrollBars(duration);
		mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();

		/*
		 * whichScreen = Math.max(0, Math.min(whichScreen, getScreenNum() - 1));
		 * boolean changingScreens = whichScreen != mCurrentScreen; mNextScreen
		 * = whichScreen; View focusedChild = getFocusedChild(); if
		 * (focusedChild != null && changingScreens && focusedChild ==
		 * getChildAt(mCurrentScreen)) { focusedChild.clearFocus(); } final int
		 * screenDelta = Math.abs(whichScreen - mCurrentScreen); int
		 * durationOffset = 1; if (screenDelta == 0) { durationOffset = 400; }
		 * final int duration = mScrollingSpeed + durationOffset; final int newX
		 * = whichScreen * getWidth(); final int delta = newX - getScrollX();
		 * mScroller.startScroll(getScrollX(), 0, delta, 0, duration);
		 * invalidate();
		 */
	}

	private void snapToScreen(int whichScreen, int velocity, boolean settle) {
		Log.i("ckh", "whichScreen == " + whichScreen + "   velocity == " + velocity);
		whichScreen = Math.max(-1, Math.min(whichScreen, getChildCount()));

		// clearVacantCache();
		// enableChildrenCache(mCurrentScreen, whichScreen);
		mNextScreen = whichScreen;
		int gotoScreen = 0;
		if (mNextScreen < 0) {
			gotoScreen = getChildCount() - 1;
		} else if (mNextScreen > (getChildCount() - 1)) {
			gotoScreen = 0;
		} else {
			gotoScreen = mNextScreen;
		}
		mNumView.updateScreen(gotoScreen, getChildCount(), 0);

		View focusedChild = getFocusedChild();
		if (focusedChild != null && whichScreen != mCurrentScreen && focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}

		final int screenDelta = Math.max(1, Math.abs(whichScreen - mCurrentScreen));
		final int newX = whichScreen * getWidth();
		final int delta = newX - mScrollX;
		int duration = (screenDelta + 1) * 100;

		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		velocity = Math.abs(velocity);
		velocity = velocity / 4;
		if (Math.abs(velocity) > 0) {
			duration += (duration / (Math.abs(velocity) / BASELINE_FLING_VELOCITY)) * FLING_VELOCITY_INFLUENCE;
		} else {
			duration += 100;
		}

		awakenScrollBars(duration);
		mScroller.startScroll(mScrollX, 0, delta, 0, duration);
		invalidate();
	}

	public static class SavedState extends BaseSavedState {// 用于保存页面状态的实现类
		int currentScreen = -1;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentScreen = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(currentScreen);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private int getScreenNum() {
		return getChildCount();
	}

	public void updateNumberView(MenuSpaceScreenNumView numview) {
		numview.initScreen(getChildCount(), mCurrentScreen, 0);
	}

	public void setNumberView(MenuSpaceScreenNumView numview) {
		mNumView = numview;
		mNumView.updateScreen(mCurrentScreen, getChildCount(), 0);
	}
}
