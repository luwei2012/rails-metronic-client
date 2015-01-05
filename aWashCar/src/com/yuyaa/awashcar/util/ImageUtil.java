package com.yuyaa.awashcar.util;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

import com.yuyaa.awashcar.MyApplication;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


public class ImageUtil {

	public static final float roundPix = 6;
	public static final int rate = 2;

	public static final int reflectionGap = 0;
	private static final int SOFT_CACHE_CAPACITY = 40;

	private final static LinkedHashMap<Integer, WeakReference<Bitmap>> sSoftBitmapCache = new LinkedHashMap<Integer, WeakReference<Bitmap>>(
			SOFT_CACHE_CAPACITY, 0.75f, true) {

		private static final long serialVersionUID = -7422412983039393262L;

		@Override
		protected boolean removeEldestEntry(
				Entry<Integer, WeakReference<Bitmap>> eldest) {
			// TODO Auto-generated method stub
			if (size() > SOFT_CACHE_CAPACITY) {

				return true;
			}
			return false;
		}

		@Override
		public WeakReference<Bitmap> put(Integer key,
				WeakReference<Bitmap> value) {
			// TODO Auto-generated method stub
			return super.put(key, value);
		}

	};

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return newbmp;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;

	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {

		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 255, 255, 255);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap createReflectionImageWithOrigin(Bitmap originalBitmap) {

		int width = originalBitmap.getWidth();

		int height = originalBitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap reflectionBitmap = Bitmap
				.createBitmap(originalBitmap, 0, height / ImageUtil.rate,
						width, height / ImageUtil.rate, matrix, false);
		Bitmap withReflectionBitmap = Bitmap.createBitmap(width, (height
				+ height / ImageUtil.rate + reflectionGap), Config.ARGB_8888);

		Canvas canvas = new Canvas(withReflectionBitmap);
		canvas.drawBitmap(originalBitmap, 0, 0, null);
		Paint defaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
		canvas.drawBitmap(reflectionBitmap, 0, height + reflectionGap, null);
		Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0,
				originalBitmap.getHeight(), 0,
				withReflectionBitmap.getHeight(), 0x70ffffff, 0x00ffffff,
				TileMode.MIRROR);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, height, width, withReflectionBitmap.getHeight(),
				paint);

		return withReflectionBitmap;
	}

	public static Bitmap getBitmapByID(int sourceID) {
		if (sSoftBitmapCache.containsKey(sourceID)) {
			return sSoftBitmapCache.get(sourceID).get();
		} else {
			Bitmap bm = BitmapFactory.decodeResource(MyApplication
					.getInstance().getResources(), sourceID);
			sSoftBitmapCache.put(sourceID, new WeakReference<Bitmap>(bm));
			return bm;
		}

	}

	public static Drawable getRoundedCornerDrawable(Drawable drawable,
			float roundPx) {
		Bitmap bitmap = drawableToBitmap(drawable);
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 255, 255, 255);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return new BitmapDrawable(MyApplication.getInstance().getResources(),
				output);
	}

}