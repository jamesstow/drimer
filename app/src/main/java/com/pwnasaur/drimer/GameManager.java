package com.pwnasaur.drimer;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by user on 30/06/14.
 */
public class GameManager
{
	private static final String TAG = "GameManager";

	private IGameHandler _listener;
	//private CountDownTimer _timer;
	private GameConfig _currentConfig;
	private int _drinksRemaining;
	private long _currentTick;
	private long _startingTick = -1;
	private int _currentDrink;
	private int _tickUpdateRate;
	private boolean _isPaused = false;
	private boolean _isRunning = false;
	private Runnable _stopwatchRunnable;
	private Handler _stopwatchHandler = new Handler();

	public GameManager()
	{
		this(10);
	}

	public GameManager(int tickUpdateRate)
	{
		this._tickUpdateRate = tickUpdateRate;
	}

	private void checkListener() throws Exception{
		if(this._listener == null){
			throw new Exception("No listener defined!");
		}
	}

	private void initialiseTimer()
	{
		this._startingTick = System.currentTimeMillis();
		this._stopwatchRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				_currentTick = System.currentTimeMillis() - _startingTick;
				long updateMod = _currentTick % _tickUpdateRate;
				boolean recur = true;

				if (_currentTick != 0 && updateMod == 0)
				{
					notifyTick();
				}

				int estimatedDrink = (int)(_currentTick / (int)_currentConfig.millisecondsBetweenDrinks);
				if (estimatedDrink != _currentDrink)
				{
					if (estimatedDrink != _currentConfig.totalNumberOfDrinks)
					{
						_currentDrink = estimatedDrink;
						notifyDrink();
					}
					else
					{
						recur = false;
						notifyFinish();
					}
				}

				if(recur)
				{
					_stopwatchHandler.postDelayed(this, 0);
				}
			}
		};

		long totalMilliseconds = this._currentConfig.totalNumberOfDrinks * this._currentConfig.millisecondsBetweenDrinks;
		totalMilliseconds -= this._currentTick;

		/*this._timer = new CountDownTimer(totalMilliseconds, 1)
		{
			@Override
			public void onTick(long l)
			{
				timer_tick(l);
			}

			@Override
			public void onFinish()
			{
				notifyFinish();
			}
		};*/
	}

	private void timer_tick(long l)
	{
		if (this._startingTick == -1)
		{
			this._startingTick = l;
		}

		this._currentTick = this._startingTick - l;
		long updateMod = this._currentTick % this._tickUpdateRate;

		if (this._currentTick != 0 && updateMod == 0)
		{
			this.notifyTick();

		}

		long mod = this._currentTick % this._currentConfig.millisecondsBetweenDrinks;
		if (mod == 0)
		{
			this._currentDrink = (int) this._currentTick / (int) this._currentConfig.millisecondsBetweenDrinks;
			this.notifyDrink();
		}
	}

	private void notifyFinish()
	{
		Log.d(TAG,"Finish!");
		this._listener.onFinish(this);
	}

	private void notifyTick()
	{
		Log.d(TAG,"Tick: " + this._currentTick);
		this._listener.onTick(this._currentTick,this._currentDrink, this);
	}

	private void notifyDrink()
	{
		Log.d(TAG,"Drink: " + this._currentDrink);
		this._listener.onDrink(this._currentDrink, this);
	}

	public void setListener(IGameHandler listener)	{		this._listener = listener;	}

	public void removeListener()
	{
		this._listener = null;
	}

	public void changeGameConfig(GameConfig config)
	{
		this.stop();

		this._isPaused = false;
		this._currentConfig = config;
		this._currentTick = 0;
		this._currentDrink = 0;

		this.initialiseTimer();
	}

	public void start() throws Exception
	{
		this.checkListener();
		if (!this._isRunning)
		{
			initialiseTimer();

			this._isPaused = false;
			this._isRunning = true;

			this._stopwatchHandler.postDelayed(this._stopwatchRunnable, 0);

			this._listener.onStart(this);
		}
	}

	public void stop()
	{
		if (this._isRunning)
		{
			this._stopwatchHandler.removeCallbacks(this._stopwatchRunnable);

			this._listener.onStop(this);
		}
	}

	public void pause()
	{
		if (this._isRunning)
		{
			this._stopwatchHandler.removeCallbacks(this._stopwatchRunnable);

			this._isPaused = true;
			this._listener.onPause(this);
		}
	}
}
