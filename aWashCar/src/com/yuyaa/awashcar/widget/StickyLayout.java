/**
The MIT License (MIT)

Copyright (c) 2014 singwhatiwanna
https://github.com/singwhatiwanna
http://blog.csdn.net/singwhatiwanna

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.yuyaa.awashcar.widget;

import java.util.NoSuchElementException;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class StickyLayout extends LinearLayout {
	public OnStickyLayoutTouchEventListener getStickyLayoutTouchEventListener() {
		return mStickyLayoutTouchEventListener;
	}

	public void setStickyLayoutTouchEventListener(
			OnStickyLayoutTouchEventListener mStickyLayoutTouchEventListener) {
		this.mStickyLayoutTouchEventListener = mStickyLayoutTouchEventListener;
	}

	public interface OnStickyLayoutTouchEventListener {
		public boolean giveUpTouchEvent(MotionEvent event);

		public void onHeadChanged();

	}

	private View mHeader;
	private View mContent;
	private OnStickyLayoutTouchEventListener mStickyLayoutTouchEventListener;

	// header的高度 单位：px
	public int mOriginalHeaderHeight;
	public int mHeaderHeight;
	public int offset = 0;

	public int mStatus = STATUS_EXPANDED;
	public static final int STATUS_EXPANDED = 1;
	public static final int STATUS_COLLAPSED = 2;

	private int mTouchSlop;

	private int mLastY = 0;

	private int mLastYIntercept = 0;

	private boolean mIsSticky = true;
	private boolean mInitDataSucceed = false;
	private boolean mDisallowInterceptTouchEventOnHeader = true;

	public StickyLayout(Context context) {
		super(context);
	}

	public StickyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public StickyLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus && (mHeader == null || mContent == null)) {
			initData();
		}
	}

	private void initData() {
		int headerId = getResources().getIdentifier("sticky_header", "id",
				getContext().getPackageName());
		int contentId = getResources().getIdentifier("sticky_content", "id",
				getContext().getPackageName());
		if (headerId != 0 && contentId != 0) {
			mHeader = findViewById(headerId);
			mContent = findViewById(contentId);
			mOriginalHeaderHeight = mHeader.getMeasuredHeight();
			mHeaderHeight = mOriginalHeaderHeight;
			mTouchSlop = ViewConfiguration.get(getContext())
					.getScaledTouchSlop();
			if (mHeaderHeight > 0) {
				mInitDataSucceed = true;
			}
		} else {
			throw new NoSuchElementException(
					"Did your view with id \"sticky_header\" or \"sticky_content\" exists?");
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int intercepted = 0;
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			mLastYIntercept = y;
			mLastY = y;
			intercepted = 0;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			int deltaY = y - mLastYIntercept;
			if (mDisallowInterceptTouchEventOnHeader && y <= getHeaderHeight()) {
				intercepted = 0;
			} else if (mStatus == STATUS_EXPANDED && deltaY >= mTouchSlop) {
				intercepted = 0;
			} else if (mStatus == STATUS_COLLAPSED && deltaY <= -mTouchSlop) {
				intercepted = 0;
			} else if (mStatus == STATUS_EXPANDED && deltaY <= -mTouchSlop) {
				intercepted = 1;
			} else if (mStickyLayoutTouchEventListener != null) {
				if (mStickyLayoutTouchEventListener.giveUpTouchEvent(event)
						&& deltaY >= mTouchSlop) {
					intercepted = 1;
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP: {
			intercepted = 0;
			mLastYIntercept = 0;
			break;
		}
		default:
			break;
		}

		return intercepted != 0 && mIsSticky;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsSticky) {
			return true;
		}
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			int deltaY = y - mLastY;
			mHeaderHeight += deltaY;
			setHeaderHeight(mHeaderHeight);
			break;
		}
		case MotionEvent.ACTION_UP: {
			// 这里做了下判断，当松开手的时候，会自动向两边滑动，具体向哪边滑，要看当前所处的位置
			int destHeight = 0 + offset;
			if (mHeaderHeight < mOriginalHeaderHeight * 0.5) {
				destHeight = 0 + offset;
				mStatus = STATUS_COLLAPSED;
			} else {
				destHeight = mOriginalHeaderHeight;
				mStatus = STATUS_EXPANDED;
			}
			// 慢慢滑向终点
			this.smoothSetHeaderHeight(mHeaderHeight, destHeight, 500);
			break;
		}
		default:
			break;
		}
		mLastY = y;
		return true;
	}

	public void smoothSetHeaderHeight(final int from, final int to,
			long duration) {
		smoothSetHeaderHeight(from, to, duration, false);
	}

	public void smoothSetHeaderHeight(final int from, final int to,
			long duration, final boolean modifyOriginalHeaderHeight) {
		final int frameCount = (int) (duration / 1000f * 30) + 1;
		final float partation = (to - from) / (float) frameCount;
		new Thread("Thread#smoothSetHeaderHeight") {

			@Override
			public void run() {
				for (int i = 0; i < frameCount; i++) {
					final int height;
					if (i == frameCount - 1) {
						height = to;
					} else {
						height = (int) (from + partation * i);
					}
					post(new Runnable() {
						public void run() {
							setHeaderHeight(height);
						}
					});
					try {
						sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (modifyOriginalHeaderHeight) {
					setOriginalHeaderHeight(to);
				}
			};

		}.start();
	}

	public void setOriginalHeaderHeight(int originalHeaderHeight) {
		mOriginalHeaderHeight = originalHeaderHeight;
	}

	public void setHeaderHeight(int height, boolean modifyOriginalHeaderHeight) {
		if (modifyOriginalHeaderHeight) {
			setOriginalHeaderHeight(height);
		}
		setHeaderHeight(height);
	}

	public void setHeaderHeight(int height) {
		if (!mInitDataSucceed) {
			initData();
		}

		if (height <= 0 + offset) {
			height = 0 + offset;
		}
		if (height >= mOriginalHeaderHeight) {
			height = mOriginalHeaderHeight;
		}

		if (height == 0 + offset) {
			mStatus = STATUS_COLLAPSED;
		} else {
			mStatus = STATUS_EXPANDED;
		}

		if (mHeader != null && mHeader.getLayoutParams() != null) {
			mHeader.getLayoutParams().height = height;
			mStickyLayoutTouchEventListener.onHeadChanged();
			mHeader.requestLayout();
			mHeaderHeight = height;
		}
	}

	public int getHeaderHeight() {
		return mHeaderHeight;
	}

	public void setSticky(boolean isSticky) {
		mIsSticky = isSticky;
	}

	public void requestDisallowInterceptTouchEventOnHeader(
			boolean disallowIntercept) {
		mDisallowInterceptTouchEventOnHeader = disallowIntercept;
	}

}