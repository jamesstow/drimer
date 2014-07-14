package com.pwnasaur.drimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by user on 14/07/14.
 */
public class CountdownView extends View
{
	private static final int DEFAULT_RING_ELASPED_COLOUR = 0xCCFF0000;
	private static final int DEFAULT_RING_INACTIVE_COLOUR = 0xCCCCCCCC;
	private static final float DEFAULT_RING_STARTING_ANGLE = 0f;
	private static final float RING_CIRCUMFERENCE = 360f;
	private static final String LOG_TAG = "CountdownView";

	private Paint _ringElapsedPaint;
	private int _ringElapsedColour;
	private Paint _ringInactivePaint;
	private int _ringInactiveColour;
	private float _ringCenterX, _ringCenterY, _ringRadius, _ringWidth;
	private RectF _ringRectangle;

	private float _currentArcPosition = 0f;

	public CountdownView(Context context, AttributeSet attrs){
		super(context,attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownView, 0, 0);
		try
		{
			this._ringElapsedColour = a.getColor(R.styleable.CountdownView_ringElaspedColour, CountdownView.DEFAULT_RING_ELASPED_COLOUR);
			this._ringInactiveColour = a.getColor(R.styleable.CountdownView_ringInactiveColour, CountdownView.DEFAULT_RING_INACTIVE_COLOUR);
			this._currentArcPosition = a.getFloat(R.styleable.CountdownView_startingAngle, CountdownView.DEFAULT_RING_STARTING_ANGLE);
		}
		catch (Exception e)
		{
			Log.e(CountdownView.LOG_TAG,"Error setting config",e);
		}
		finally
		{
			a.recycle();
		}

		this.init();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.drawArc(this._ringRectangle, 0f, this._currentArcPosition, true, this._ringElapsedPaint);
		canvas.drawArc(this._ringRectangle, this._currentArcPosition, CountdownView.RING_CIRCUMFERENCE, true, this._ringElapsedPaint);


	}

	private void init(){
		this._currentArcPosition = DEFAULT_RING_STARTING_ANGLE;
		this._ringElapsedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._ringElapsedPaint.setStrokeWidth(this._ringWidth);
		this._ringElapsedPaint.setColor(this._ringElapsedColour);
	}

	private void syncUI(){
		invalidate();
		requestLayout();
	}
}
