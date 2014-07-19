package com.pwnasaur.drimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by user on 14/07/14.
 */
public class CountdownView extends View
{
	// Constants
	public static final float RING_CIRCUMFERENCE = 360f;
	private static final String LOG_TAG = CountdownView.class.getSimpleName();
	private static final int DECIMAL_PLACES_FOR_ANGLE_CACHE = 1;
	private static final int ANGLE_CACHE_DP_MULTIPLIER = (int)Math.pow(10,CountdownView.DECIMAL_PLACES_FOR_ANGLE_CACHE);
	private static final float SLIVER_SIZE = 0.01f;

	// Defaults
	private static final int DEFAULT_RING_ELAPSED_COLOUR = 0xFFFF0000;
	private static final int DEFAULT_RING_INACTIVE_COLOUR = 0xFF222222;
	private static final float DEFAULT_RING_STARTING_ANGLE = 270f; // 0 is at the bottom
	private static final boolean DEFAULT_DIRECTION_CLOCKWISE = true;
	private static final float RING_THICKNESS_TO_RADIUS_RATIO = 0.03f; // how thick the ring will be in relation to the circle's radius
	private static final float RING_MARKER_TO_THICKNESS_RATIO = 2.0f; // how thick the marker will be in relation to the circle's thickness

	private Hashtable<Float, Tuple<Float, Float>> _angleCache = new Hashtable<Float, Tuple<Float, Float>>();

	// Ting.
	private Paint _ringElapsedPaint,_ringElapsedMarkerPaint;
	private int _ringElapsedColour, _ringElapsedMarkerColour, _paddingLeft, _paddingTop, _paddingRight, _paddingBottom;
	private Paint _ringInactivePaint;
	private int _ringInactiveColour;
	private float _ringCenterX, _ringCenterY, _ringRadius, _ringMarkerSizeW, _ringThickness, _ringStartingAngle;
	private RectF _ringRectangle;
	private boolean _sizesEstablished;
	private float _currentMarkerPosition = 0;
	private GestureDetector _gestureDetector;

	private List<OnClickListener> _ringClickListener = new ArrayList<OnClickListener>();

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

	private float degreesToRadians(float degrees){
		// 2 pi == 360
		return (degrees / 360f) * 2f * (float)Math.PI;
	}

	private float normaliseAngle(float angle){
		while(angle < 0){
			angle = CountdownView.RING_CIRCUMFERENCE - angle;
		}

		while(angle > CountdownView.RING_CIRCUMFERENCE){
			angle = angle - CountdownView.RING_CIRCUMFERENCE;
		}

		return angle;
	}

	public void addRingClickListener(OnClickListener listener){
		this._ringClickListener.add(listener);
	}

	public CountdownView(Context context, AttributeSet attrs){
		super(context,attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownView, 0, 0);
		try
		{
			this._ringElapsedColour = a.getColor(R.styleable.CountdownView_ringElaspedColour, CountdownView.DEFAULT_RING_ELAPSED_COLOUR);
			this._ringElapsedMarkerColour = a.getColor(R.styleable.CountdownView_ringElaspedMarkerColour, this._ringElapsedColour);
			this._ringInactiveColour = a.getColor(R.styleable.CountdownView_ringInactiveColour, CountdownView.DEFAULT_RING_INACTIVE_COLOUR);
			this._ringStartingAngle= a.getFloat(R.styleable.CountdownView_startingAngle, CountdownView.DEFAULT_RING_STARTING_ANGLE);
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
		angle = (float)(Math.floor(angle * CountdownView.ANGLE_CACHE_DP_MULTIPLIER + 0.5) / CountdownView.ANGLE_CACHE_DP_MULTIPLIER);
		this._currentMarkerPosition = angle % CountdownView.RING_CIRCUMFERENCE; //angle + CountdownView.DEFAULT_RING_STARTING_ANGLE;
		this.syncUI();
	}

	public void updateUiByStatus(CountdownStatus status){
		float angle = (status.timeToNextDrink / status.timeBetweenDrinks) * CountdownView.RING_CIRCUMFERENCE;
		this.setMarkerPosition(angle);
	}

	private void handleClick(float x, float y){
		if(this._ringRectangle.contains(x, y))
		{
			for(OnClickListener listener : this._ringClickListener){
				listener.onClick(this);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean handled = this._gestureDetector.onTouchEvent(event);
		if (!handled) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				float touchX = event.getX();
				float touchY = event.getY();
				handleClick(touchX,touchY);

				handled = true;
			}
		}

		return handled;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// Account for padding
		this._paddingLeft = getPaddingLeft();
		this._paddingRight = getPaddingRight();
		this._paddingTop = getPaddingTop();
		this._paddingBottom = getPaddingBottom();

		float xpad = (float)(this._paddingLeft + this._paddingRight);
		float ypad = (float)(this._paddingTop + this._paddingBottom);

		this._sizesEstablished = true;
		super.onSizeChanged(w, h, oldw, oldh);

		float smallestDimension = (float)Math.min(w,h);

		float ww = (float)w - xpad;
		float hh = (float)h - ypad;

		float smallestDimensionLessPadding = (float)Math.min(ww, hh);

		float maxRadiusIncludingMarker = smallestDimensionLessPadding / 2f;
		float markerToRadiusRatio = CountdownView.RING_THICKNESS_TO_RADIUS_RATIO * CountdownView.RING_MARKER_TO_THICKNESS_RATIO;

		this._ringRadius = maxRadiusIncludingMarker / (1 + (markerToRadiusRatio)/2f);
		this._ringMarkerSizeW = (maxRadiusIncludingMarker * markerToRadiusRatio);
		// can implement SizeH here too if we want to be able to draw different markers.
		this._ringThickness = maxRadiusIncludingMarker * CountdownView.RING_THICKNESS_TO_RADIUS_RATIO;

		float maxMarkerSize = this._ringMarkerSizeW;
		float halfMarkerSize = maxMarkerSize / 2f;

		float rl,rt,rr,rb; // come back to this - i think it's slightly off
		rl = this._paddingLeft + halfMarkerSize;
		rr = this._paddingLeft + (2 * this._ringRadius) + maxMarkerSize; //smallestDimension - (ypad + halfMarkerSize);
		rt = this._paddingTop + (halfMarkerSize);
		rb = this._paddingTop + (2 * this._ringRadius) + maxMarkerSize;//smallestDimension - (xpad + halfMarkerSize);

		// left, top, right, bottom
		this._ringRectangle= new RectF(rl,rt,rr,rb);

		this._ringCenterX = (float)rl + this._ringRectangle.width() / 2f;
		this._ringCenterY = (float)rt + this._ringRectangle.height() / 2f;

		this._ringElapsedPaint.setStrokeWidth(this._ringThickness);
		this._ringInactivePaint.setStrokeWidth(this._ringMarkerSizeW);

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if(this._sizesEstablished)
		{
			float differentialGap = (this._ringMarkerSizeW * CountdownView.SLIVER_SIZE);
			// draw the circle
			float elapsedStart = 0;
			float elapsedEnd = this._currentMarkerPosition;
			float inactiveStart = elapsedEnd + differentialGap;
			float inactiveEnd = elapsedStart;

			float elapsedSweep = normaliseAngle(elapsedEnd - elapsedStart);
			float inactiveSweep = CountdownView.RING_CIRCUMFERENCE - elapsedSweep - differentialGap;

			canvas.drawArc(this._ringRectangle, elapsedStart + this._ringStartingAngle, elapsedSweep, false, this._ringElapsedPaint);
			canvas.drawArc(this._ringRectangle, inactiveStart + this._ringStartingAngle, inactiveSweep, false, this._ringInactivePaint);

			boolean showMarker = true;
			if (showMarker)
			{
				float markerX, markerY;

				float offsetX = this._paddingLeft + (this._ringMarkerSizeW) + this._ringRadius;//(float)this._ringRadius + (float)this._paddingLeft;// - this._ringMarkerSizeW / 2f;
				float offsetY = this._paddingTop + (this._ringMarkerSizeW) + this._ringRadius;//(float)this._ringRadius + (float)this._paddingTop;// - this._ringMarkerSizeW / 2f;

				float angle = this._currentMarkerPosition + this._ringStartingAngle;
				float hypotenuse = this._ringRadius + (this._ringMarkerSizeW / 2f);

				markerY = ((float)Math.sin(degreesToRadians(angle)) * (hypotenuse)) + offsetY;
				markerX = ((float)Math.cos(degreesToRadians(angle)) * (hypotenuse)) + offsetX;

				canvas.drawCircle(markerX, markerY, this._ringMarkerSizeW, this._ringElapsedMarkerPaint);
				canvas.drawCircle(offsetX, offsetY, this._ringMarkerSizeW, this._ringElapsedMarkerPaint);
			}
		}
	}
}
