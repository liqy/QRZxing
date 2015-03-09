package com.liqy.qrzxing.scanner;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.liqy.qrzxing.R;
import com.liqy.qrzxing.scanner.camera.CameraManager;

public final class ViewfinderView extends View {

	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
			128, 64 };
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final long ANIMATION_DELAY = 80L;
	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	private int scannerAlpha;
	private List<ResultPoint> possibleResultPoints;

	private int i = 0;
	private Rect mRect;
	private GradientDrawable mDrawable;
	private Drawable lineDrawable;

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultColor = resources.getColor(R.color.result_view);

		mRect = new Rect();
		int left = getResources().getColor(R.color.lightgreen);
		int center = getResources().getColor(R.color.green);
		int right = getResources().getColor(R.color.lightgreen);
		lineDrawable = getResources().getDrawable(R.drawable.zx_code_line);
		mDrawable = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, new int[] { left,
						left, center, right, right });

		scannerAlpha = 0;
		possibleResultPoints = new ArrayList<ResultPoint>(5);
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return;
		}

		Rect frame = cameraManager.getFramingRect();
		if (frame == null) {
			return;
		}

		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
		canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
		canvas.drawRect(0, frame.bottom, width, height, paint);

		if (resultBitmap != null) {
			paint.setAlpha(CURRENT_POINT_OPACITY);
			canvas.drawBitmap(resultBitmap, null, frame, paint);
		} else {
            int ww=50;
			paint.setColor(getResources().getColor(R.color.green));
			canvas.drawRect(frame.left, frame.top, frame.left + ww,
					frame.top + 5, paint);
			canvas.drawRect(frame.left, frame.top, frame.left + 5,
					frame.top + ww, paint);
			canvas.drawRect(frame.right - ww, frame.top, frame.right,
					frame.top + 5, paint);
			canvas.drawRect(frame.right - 5, frame.top, frame.right,
					frame.top + ww, paint);
			canvas.drawRect(frame.left, frame.bottom - 5, frame.left + ww,
					frame.bottom, paint);
			canvas.drawRect(frame.left, frame.bottom - ww, frame.left + 5,
					frame.bottom, paint);
			canvas.drawRect(frame.right - ww, frame.bottom - 5, frame.right,
					frame.bottom, paint);
			canvas.drawRect(frame.right - 5, frame.bottom - ww, frame.right,
					frame.bottom, paint);

			paint.setColor(getResources().getColor(R.color.green));
			paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;

			if ((i += 5) < frame.bottom - frame.top) {
                int hh=3;
				mRect.set(frame.left - hh, frame.top + i - hh, frame.right + hh,
						frame.top + hh + i);
				lineDrawable.setBounds(mRect);
				lineDrawable.draw(canvas);
				invalidate();
			} else {
				i = 0;
			}

			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);
		}
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}

	public void recycleLineDrawable() {
		if (mDrawable != null) {
			mDrawable.setCallback(null);
		}
		if (lineDrawable != null) {
			lineDrawable.setCallback(null);
		}
	}
}
