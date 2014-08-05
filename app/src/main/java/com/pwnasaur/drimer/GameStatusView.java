package com.pwnasaur.drimer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james.stow on 14/07/14.
 */
public class GameStatusView extends View
{
	// Constants
	private static final String LOG_TAG = GameStatusView.class.getSimpleName();

	// Defaults
	private static final float MAGIC_NUMBER_I_DONT_UNDESTAND = 0.3f;
	private static final float ARBITRARY_CONSTANT_1 = 0.5f;

	// Ting.
	private Paint _textPaint;
	private int _textColour;
	private int _paddingLeft, _paddingTop, _paddingRight, _paddingBottom;
	private float _height, _width;
	private Rect _textRectangle;
	private boolean _sizesEstablished;
	private GestureDetector _gestureDetector;
	private CountdownStatus _status;
	private TextView _tv = new TextView(this.getContext());

	private void init()
	{
		this._gestureDetector = new GestureDetector(GameStatusView.this.getContext(),
				new GestureDetector.SimpleOnGestureListener()
				{
					@Override
					public boolean onDown(MotionEvent e)
					{
						return true;
					}
				}
		);

		this._textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		this._textPaint.setStyle(Paint.Style.STROKE);
		this._textPaint.setColor(this._textColour);
	}

	private void syncUI()
	{
		invalidate();
		requestLayout();
	}

	public GameStatusView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownView, 0, 0);
		try
		{
			this._textColour = a.getColor(R.styleable.CountdownView_ringElaspedColour, Settings.DEFAULT_VIEW_TEXT_COLOUR);
		} catch (Exception e)
		{
			Helpers.DebugLog(GameStatusView.LOG_TAG, "Error setting config", e);
		} finally
		{
			a.recycle();
		}

		this.init();
	}

	public void updateUIWithStatus(CountdownStatus status)
	{
		this._status = status;

		// TODO

		this.syncUI();
	}

	private void handleClick(float x, float y)
	{
		// does this want to do anything?
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

		this._width = smallestDimensionLessPadding > ww ? hh : ww;
		this._height = smallestDimensionLessPadding > ww ? ww : hh; // handle rotation

		float rl, rt, rr, rb;
		rl = this._paddingLeft;
		rr = this._width + this._paddingLeft;
		rt = this._paddingTop;
		rb = this._height + this._paddingBottom;

		// left, top, right, bottom
		this._textRectangle = new Rect((int)rl, (int)rt, (int)rr, (int)rb);
		this._textPaint.setTextSize(this._textRectangle.height() * GameStatusView.ARBITRARY_CONSTANT_1);
	}

	private void render(Canvas canvas){
		//canvas.drawRect(this._textRectangle,this._textPaint);
		//canvas.drawCircle(cX,cY,10f, this._textPaint);

		String current = this._status == null ? " - " : Integer.toString(this._status.currentDrink);
		String total = this._status == null ? " - " : Integer.toString(this._status.totalDrinks);
		String text = current + " / " + total;

		float textWidth = this._textPaint.measureText(text);

		int cX = this._textRectangle.centerX();
		int cY = this._textRectangle.centerY();

		canvas.drawText(text, cX-(textWidth/2), cY+(int)(this._textPaint.getTextSize() * GameStatusView.MAGIC_NUMBER_I_DONT_UNDESTAND), this._textPaint);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		if (this._sizesEstablished)
		{
			render(canvas);
		}
	}
}
