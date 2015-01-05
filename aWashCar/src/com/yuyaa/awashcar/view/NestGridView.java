package com.yuyaa.awashcar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NestGridView extends GridView {						// 此GridView能嵌套在ScrollView中
	public NestGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NestGridView(Context context) {
		super(context);
	}

	public NestGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}

}
