package com.pwnasaur.drimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 14/07/14.
 */
public class CountdownView extends View
{
	// Constants
	public static final float RING_CIRCUMFERENCE = 360f;
	private static final String LOG_TAG = CountdownView.class.getSimpleName();

	// Defaults
	private static final int DEFAULT_RING_ELAPSED_COLOUR = 0xEEFF0000;
	private static final int DEFAULT_RING_INACTIVE_COLOUR = 0xCC222222;
	private static final float DEFAULT_RING_STARTING_ANGLE = 180f; // 0 is at the bottom
	private static final float DEFAULT_RING_ENDING_ANGLE = CountdownView.DEFAULT_RING_STARTING_ANGLE + CountdownView.RING_CIRCUMFERENCE;
	private static final boolean DEFAULT_DIRECTION_CLOCKWISE = true;
	private static final float RING_THICKNESS_TO_RADIUS_RATIO = 0.03f; // how thick the ring will be in relation to the circle's radius
	private static final float RING_MARKER_TO_THICKNESS_RATIO = 2.0f; // how thick the ring will be in relation to the circle's radius

	// Ting.
	private Paint _ringElapsedPaint,_ringElapsedMarkerPaint;
	private int _ringElapsedColour, _ringElapsedMarkerColour;
	private Paint _ringInactivePaint;
	private int _ringInactiveColour;
	private float _ringCenterX, _ringCenterY, _ringRadius, _ringMarkerSizeW, _ringThickness;
	private RectF _ringRectangle;
	private boolean _countdownClockwise, _sizesEstablished;
	private float _currentMarkerPosition = DEFAULT_RING_STARTING_ANGLE;

	private GestureDetector _gestureDetector;

	private List<OnClickListener> _pauseListeners = new ArrayList<OnClickListener>();
	private List<OnClickListener> _restartListeners = new ArrayList<OnClickListener>();
	private List<OnClickListener> _startListeners = new ArrayList<OnClickListener>();
	private List<OnClickListener> _stopListeners = new ArrayList<OnClickListener>();

	private void init(){
		this._gestureDetector = new GestureDetector(CountdownView.this.getContext(),
			new GestureDetector.SimpleOnGestureListener(){
				@Override
				public boolean onDown(MotionEvent e) {
					return true;
				}
			}
		);

		this._ringElapsedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._ringElapsedPaint.setStyle(Paint.Style.STROKE);
		this._ringElapsedPaint.setColor(this._ringElapsedColour);

		this._ringInactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._ringInactivePaint.setStyle(Paint.Style.STROKE);
		this._ringInactivePaint.setColor(this._ringInactiveColour);

		this._ringElapsedMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._ringElapsedMarkerPaint .setStyle(Paint.Style.FILL_AND_STROKE);
		this._ringElapsedMarkerPaint.setColor(this._ringElapsedMarkerColour);
	}

	private void syncUI(){
		invalidate();
		requestLayout();
	}

	private  float degreesToRadians(float degrees){
		// 2 pi == 360
		return (degrees / 360f) * 2f * (float)Math.PI;
	}

	public void addOnStartListener(OnClickListener listener){
		this._startListeners.add(listener);
	}

	public CountdownView(Context context, AttributeSet attrs){
		super(context,attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownView, 0, 0);
		try
		{
			this._ringElapsedColour = a.getColor(R.styleable.CountdownView_ringElaspedColour, CountdownView.DEFAULT_RING_ELAPSED_COLOUR);
			this._ringElapsedMarkerColour = a.getColor(R.styleable.CountdownView_ringElaspedMarkerColour, this._ringElapsedColour);
			this._ringInactiveColour = a.getColor(R.styleable.CountdownView_ringInactiveColour, CountdownView.DEFAULT_RING_INACTIVE_COLOUR);
			this._currentMarkerPosition = a.getFloat(R.styleable.CountdownView_startingAngle, CountdownView.DEFAULT_RING_STARTING_ANGLE);
			this._countdownClockwise = a.getBoolean(R.styleable.CountdownView_countdownClockwise, CountdownView.DEFAULT_DIRECTION_CLOCKWISE);
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

	public void setMarkerPosition(float angle){
		this._currentMarkerPosition = angle + CountdownView.DEFAULT_RING_STARTING_ANGLE;
		this.syncUI();
	}

	private void handleClick(float x, float y){
		if(this._ringRectangle.contains(x, y))
		{
			for(OnClickListener listener : this._startListeners){
				listener.onClick(this);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = this._gestureDetector.onTouchEvent(event);
		if (!result) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				float touchX = event.getX();
				float touchY = event.getY();
				handleClick(touchX,touchY);

				result = true;
			}
		}

		return result;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// Account for padding
		int lp = getPaddingLeft();
		int rp = getPaddingRight();
		int tp = getPaddingTop();
		int bp = getPaddingBottom();

		float xpad = (float)(lp + rp);
		float ypad = (float)(tp + bp);

		this._sizesEstablished = true;
		super.onSizeChanged(w, h, oldw, oldh);

		float smallestDimension = (float)Math.min(w,h);

		float ww = (float)w - xpad;
		float hh = (float)h - ypad;

		float smallestDimensionLessPadding = (float)Math.min(ww, hh);

		float maxRadiusIncludingMarker = smallestDimensionLessPadding / 2f;
		float markerToRadiusRatio = CountdownView.RING_THICKNESS_TO_RADIUS_RATIO * CountdownView.RING_MARKER_TO_THICKNESS_RATIO;

		this._ringRadius =maxRadiusIncludingMarker / (1 + (markerToRadiusRatio)/2f);
		this._ringMarkerSizeW = (maxRadiusIncludingMarker * markerToRadiusRatio);
		// can implement SizeH here too if we want to be able to draw different markers.
		this._ringThickness = maxRadiusIncludingMarker * CountdownView.RING_THICKNESS_TO_RADIUS_RATIO;

		float maxMarkerSize = this._ringMarkerSizeW;
		float halfMarkerSize = maxMarkerSize / 2f;

		float rl,rt,rr,rb;
		rl = lp + halfMarkerSize;
		rr = smallestDimension - (ypad + halfMarkerSize);
		rt = tp + (halfMarkerSize);
		rb = smallestDimension - (xpad + halfMarkerSize);

		// left, top, right, bottom
		this._ringRectangle= new RectF(rl,rt,rr,rb);

		this._ringCenterX = (float)rl + this._ringRectangle.width()/2f;
		this._ringCenterY = (float)rt + this._ringRectangle.height()/2f;
		//this._ringCenterY = (float)hh / 2f;

		this._ringElapsedPaint.setStrokeWidth(this._ringThickness);
		this._ringInactivePaint.setStrokeWidth(this._ringMarkerSizeW);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if(this._sizesEstablished)
		{
			// draw the circle
			float elapsedStart = CountdownView.DEFAULT_RING_STARTING_ANGLE;
			float elapsedEnd = CountdownView.DEFAULT_RING_STARTING_ANGLE - this._currentMarkerPosition;
			float inactiveStart = elapsedEnd % CountdownView.RING_CIRCUMFERENCE;
			float inactiveEnd = CountdownView.DEFAULT_RING_ENDING_ANGLE % CountdownView.RING_CIRCUMFERENCE;

			canvas.drawArc(this._ringRectangle, elapsedStart, elapsedEnd, false, this._ringElapsedPaint);
			canvas.drawArc(this._ringRectangle, inactiveStart, inactiveEnd, false, this._ringInactivePaint);

			boolean showMarker = true;
			if (showMarker)
			{
				float markerX, markerY, relativeX, relativeY;

				relativeX = (this._ringRadius - this._ringThickness / 2f) * (float) Math.sin(degreesToRadians(this._currentMarkerPosition));
				relativeY = (this._ringRadius - this._ringThickness / 2f) * (float) Math.cos(degreesToRadians(this._currentMarkerPosition));

				markerX = relativeX + this._ringCenterX;
				markerY = relativeY + this._ringCenterY;

				canvas.drawCircle(markerX, markerY, this._ringMarkerSizeW, this._ringElapsedMarkerPaint);
			}
		}
	}
}
