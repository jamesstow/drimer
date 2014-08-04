package com.pwnasaur.drimer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

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
	private static final int DECIMAL_PLACES_FOR_ANGLE_CACHE = 1;
	private static final int ANGLE_CACHE_DP_MULTIPLIER = (int) Math.pow(10, CountdownView.DECIMAL_PLACES_FOR_ANGLE_CACHE);
	private static final float SLIVER_SIZE = 0.01f;

	// Defaults
	private static final int DEFAULT_RING_ELAPSED_COLOUR = 0xFFFF0000;
	private static final int DEFAULT_RING_INACTIVE_COLOUR = 0xFF222222;
	private static final int DEFAULT_RING_TEXT_COLOUR = DEFAULT_RING_INACTIVE_COLOUR;
	private static final int PAUSED_BLINK_RATE_MILLIS = 1000;

	private static final float DEFAULT_RING_STARTING_ANGLE = 270f; // 0 is at the bottom
	private static final boolean DEFAULT_DIRECTION_CLOCKWISE = true;
	private static final float RING_THICKNESS_TO_RADIUS_RATIO = 0.02f; // how thick the ring will be in relation to the circle's radius
	private static final float RING_MARKER_TO_THICKNESS_RATIO = 2.0f; // how thick the marker will be in relation to the circle's thickness
	private static final float RING_TEXT_TO_RING_RATIO = (float)(Math.PI / 10f); // how thick the ring will be in relation to the circle's radius
	private static final float RING_SIZE_TO_BUTTON_RATIO = 0.2f; // how big the delete / restart buttons will be relative to the ring

	// Ting.
	private Paint _ringElapsedPaint, _ringElapsedMarkerPaint, _ringInactivePaint, _ringTextPaint, _debugPaint;
	private int _ringElapsedColour, _ringElapsedMarkerColour, _ringInactiveColour, _ringTextColour;
	private int _paddingLeft, _paddingTop, _paddingRight, _paddingBottom;
	private float _ringCenterX, _ringCenterY, _ringRadius, _ringMarkerSizeW, _ringThickness, _ringStartingAngle, _height, _width;
	private RectF _ringRectangle, _deleteRectangle, _restartRectangle;
	private Rect _ringTextRectangle;
	private boolean _sizesEstablished, _rotateClockwise, _drawText;
	private float _currentMarkerPosition = 0;
	private GestureDetector _gestureDetector;
	private CountdownStatus _status;
	private TextView _tv = new TextView(this.getContext());

	private List<OnClickListener> _ringClickListener = new ArrayList<OnClickListener>();

	private int getColour(int ref){
		return  getResources().getColor(ref);
	}

	private void init()
	{
		this._gestureDetector = new GestureDetector(CountdownView.this.getContext(),
				new GestureDetector.SimpleOnGestureListener()
				{
					@Override
					public boolean onDown(MotionEvent e)
					{
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
		this._ringElapsedMarkerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		this._ringElapsedMarkerPaint.setColor(this._ringElapsedMarkerColour);

		this._ringTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._ringTextPaint.setStyle(Paint.Style.STROKE);
		this._ringTextPaint.setColor(this._ringTextColour);
		this._ringTextPaint.setTextAlign(Paint.Align.CENTER);

		this._debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._debugPaint.setStyle(Paint.Style.STROKE);
		this._debugPaint.setColor(getColour(R.color.Grey_1000));

	}

	private void syncUI()
	{
		invalidate();
		requestLayout();
	}

	private float degreesToRadians(float degrees)
	{
		// 2 pi == 360
		return (degrees / 360f) * 2f * (float) Math.PI;
	}

	private float normaliseAngle(float angle)
	{
		while (angle < 0)
		{
			angle = CountdownView.RING_CIRCUMFERENCE - angle;
		}

		while (angle > CountdownView.RING_CIRCUMFERENCE)
		{
			angle = angle - CountdownView.RING_CIRCUMFERENCE;
		}

		return angle;
	}

	public void addRingClickListener(OnClickListener listener)
	{
		this._ringClickListener.add(listener);
	}

	public CountdownView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownView, 0, 0);
		try
		{
			this._ringElapsedColour = a.getColor(R.styleable.CountdownView_ringElaspedColour, CountdownView.DEFAULT_RING_ELAPSED_COLOUR);
			this._ringElapsedMarkerColour = a.getColor(R.styleable.CountdownView_ringElaspedMarkerColour, this._ringElapsedColour);
			this._ringInactiveColour = a.getColor(R.styleable.CountdownView_ringInactiveColour, CountdownView.DEFAULT_RING_INACTIVE_COLOUR);
			this._ringTextColour = a.getColor(R.styleable.CountdownView_ringTextColour, CountdownView.DEFAULT_RING_TEXT_COLOUR);
			this._ringStartingAngle = a.getFloat(R.styleable.CountdownView_startingAngle, CountdownView.DEFAULT_RING_STARTING_ANGLE);
			this._rotateClockwise = a.getBoolean(R.styleable.CountdownView_countdownClockwise, CountdownView.DEFAULT_DIRECTION_CLOCKWISE);
		} catch (Exception e)
		{
			Log.e(CountdownView.LOG_TAG, "Error setting config", e);
		} finally
		{
			a.recycle();
		}

		this.init();
	}

	public void updateUIWithStatus(CountdownStatus status)
	{
		this._status = status;
		float angle = (status.timeToNextDrink / status.timeBetweenDrinks) * CountdownView.RING_CIRCUMFERENCE;
		angle = (float) (Math.floor(angle * CountdownView.ANGLE_CACHE_DP_MULTIPLIER + 0.5) / CountdownView.ANGLE_CACHE_DP_MULTIPLIER);
		this._currentMarkerPosition = angle % CountdownView.RING_CIRCUMFERENCE; //angle + CountdownView.DEFAULT_RING_STARTING_ANGLE;

		this.syncUI();
	}

	private void handleClick(float x, float y)
	{
		if (this._ringRectangle.contains(x, y))
		{
			for (OnClickListener listener : this._ringClickListener)
			{
				listener.onClick(this);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		boolean handled = this._gestureDetector.onTouchEvent(event);
		if (!handled)
		{
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				float touchX = event.getX();
				float touchY = event.getY();
				handleClick(touchX, touchY);

				handled = true;
			}
		}

		return handled;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);

		this._width = w;
		this._height = h;

		// Account for padding
		this._paddingLeft = getPaddingLeft();
		this._paddingRight = getPaddingRight();
		this._paddingTop = getPaddingTop();
		this._paddingBottom = getPaddingBottom();

		float xpad = (float) (this._paddingLeft + this._paddingRight);
		float ypad = (float) (this._paddingTop + this._paddingBottom);

		this._sizesEstablished = true;

		float ww = (float) w - xpad;
		float hh = (float) h - ypad;

		float smallestDimensionLessPadding = (float) Math.min(ww, hh);

		float maxRadiusTotal = smallestDimensionLessPadding / 2f;
		float markerToRadiusRatio = CountdownView.RING_MARKER_TO_THICKNESS_RATIO * CountdownView.RING_THICKNESS_TO_RADIUS_RATIO;

		this._ringRadius = maxRadiusTotal * (1 - markerToRadiusRatio);
		this._ringMarkerSizeW = (maxRadiusTotal * markerToRadiusRatio);
		// can implement SizeH here too if we want to be able to draw different markers.
		this._ringThickness = maxRadiusTotal * CountdownView.RING_THICKNESS_TO_RADIUS_RATIO;

		float maxMarkerSize = this._ringMarkerSizeW;
		float halfMarkerSize = maxMarkerSize / 2f;

		float rl, rt, rr, rb;
		rl = this._paddingLeft + halfMarkerSize + (this._ringThickness / 2f);
		rr = this._paddingLeft + (2 * this._ringRadius) + maxMarkerSize + (this._ringThickness / 2f);
		rt = this._paddingTop + (halfMarkerSize) + (this._ringThickness / 2f);
		rb = this._paddingTop + (2 * this._ringRadius) + maxMarkerSize + (this._ringThickness / 2f);

		// left, top, right, bottom
		this._ringRectangle = new RectF(rl, rt, rr, rb);
		this._ringTextRectangle = new Rect();

		float controlBoxSize = RING_SIZE_TO_BUTTON_RATIO *  ((2 * this._ringRadius) + maxMarkerSize);

		this._restartRectangle = new RectF(rl, rb - rt + controlBoxSize,rr - rl + controlBoxSize, rl + controlBoxSize);
		this._deleteRectangle = new RectF(rr - rl + controlBoxSize, rb - rt + controlBoxSize, rr - controlBoxSize, rl + controlBoxSize);

		this._ringCenterX = this._ringRectangle.centerX();
		this._ringCenterY = this._ringRectangle.centerY();

		this._ringElapsedPaint.setStrokeWidth(this._ringThickness);
		this._ringInactivePaint.setStrokeWidth(this._ringMarkerSizeW);
		this._ringTextRectangle = new Rect((int)this._ringCenterX, (int)this._ringCenterY, 0, 0);
		this._ringTextPaint.setTextSize(RING_TEXT_TO_RING_RATIO * this._ringRectangle.height());
	}

	private void renderCircle(Canvas canvas){
		float differentialGap = (this._ringMarkerSizeW * CountdownView.SLIVER_SIZE);
		// draw the circle
		float elapsedStart = 0;
		float elapsedEnd = this._currentMarkerPosition;
		float inactiveStart = elapsedEnd + differentialGap;

		float elapsedSweep = normaliseAngle(elapsedEnd - elapsedStart);
		float inactiveSweep = CountdownView.RING_CIRCUMFERENCE - elapsedSweep - differentialGap;

		canvas.drawArc(this._ringRectangle, elapsedStart + this._ringStartingAngle, elapsedSweep, false, this._ringElapsedPaint);
		canvas.drawArc(this._ringRectangle, inactiveStart + this._ringStartingAngle, inactiveSweep, false, this._ringInactivePaint);
	}

	private void renderMarker(Canvas canvas)
	{
		float markerX, markerY;

		float angle = this._currentMarkerPosition + this._ringStartingAngle;
		float hypotenuse = this._ringRadius + (this._ringThickness / 2f);

		markerY = ((float) Math.sin(degreesToRadians(angle)) * (hypotenuse)) + this._ringCenterY;
		markerX = ((float) Math.cos(degreesToRadians(angle)) * (hypotenuse)) + this._ringCenterX;
		canvas.drawCircle(markerX, markerY, this._ringMarkerSizeW, this._ringElapsedMarkerPaint);
	}

	private void renderRingText(Canvas canvas)
	{
		String text = null;

		if(this._status != null){
			if(this._status.isPaused){
				if(System.currentTimeMillis() % CountdownView.PAUSED_BLINK_RATE_MILLIS == 0){
					this._drawText = !this._drawText;
				}
			}
			else{
				this._drawText = true;
			}

			text = String.format("%.2f", this._status.timeToNextDrink / 1000f);
		}
		else{
			this._drawText = true;
			text = "Start";
		}

		if(this._drawText){
			this._ringTextPaint.getTextBounds(text, 0 ,text.length(),this._ringTextRectangle);

			int xPos = (int)this._ringCenterX ;//(canvas.getWidth() / 2);
			int yPos = (int) ((this._ringCenterY) - ((this._ringTextPaint.descent() + this._ringTextPaint.ascent()) / 2)) ;

			canvas.drawText(text,0,text.length(),xPos, yPos, this._ringTextPaint);
		}
	}

	private void renderButtons(Canvas canvas) {
		canvas.drawRect(this._deleteRectangle, this._debugPaint);
		canvas.drawRect(this._restartRectangle, this._debugPaint);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (this._sizesEstablished)
		{
			renderCircle(canvas);
			renderMarker(canvas);
			renderRingText(canvas);
			renderButtons(canvas);
		}
	}
}
