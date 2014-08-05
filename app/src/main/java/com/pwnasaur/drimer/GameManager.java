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
	private GameConfig _currentConfig;
	private int _drinksRemaining;
	private long _currentTick;
	private long _startingTick = -1;
	private int _currentDrink;
	private int _tickUpdateRate;
	private boolean _isPaused = false;
	private boolean _isRunning = false;
	private Runnable _stopwatchRunnable, _finishRunnable, _tickRunnable, _drinkRunnable;
	private Handler _stopwatchHandler = new Handler(), _gameHandler= new Handler();

	private final GameManager _thisRef = this;

	public GameManager()
	{
		this(10);
	}

	public GameManager(int tickUpdateRate)
	{
		this._tickUpdateRate = tickUpdateRate;
		GameManager thisRef = this;
		this._finishRunnable = new Runnable()
		{
			@Override
			public void run() {
				Helpers.DebugLog(TAG, "Finish!");
				_listener.onFinish(_thisRef);
			}
		};

		this._tickRunnable = new Runnable()
		{
			@Override
			public void run() {
				_listener.onTick(_currentTick, _thisRef);
			}
		};

		this._drinkRunnable = new Runnable()
		{
			@Override
			public void run() {
				Helpers.DebugLog(TAG, "Drink: " + _currentDrink);
				_listener.onDrink(_currentDrink, _thisRef);
			}
		};
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

				int drink = (int)(_currentTick / _currentConfig.millisecondsBetweenDrinks); // TODO: write a better way of getting the drink out. so its RELIABLE.
				if (drink != _currentDrink)
				{
					_currentDrink = drink;
					if (_currentDrink != _currentConfig.totalNumberOfDrinks)
					{
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
	}

	private void notifyFinish()
	{
		//_gameHandler.postDelayed(_finishRunnable,0);
		Helpers.DebugLog(TAG, "Finish!");
		_listener.onFinish(_thisRef);
	}

	private void notifyTick()
	{
		//_gameHandler.postDelayed(_tickRunnable, 0);
		_listener.onTick(_currentTick, _thisRef);
	}

	private void notifyDrink()
	{
		//_gameHandler.postDelayed(_drinkRunnable, 0);
		Helpers.DebugLog(TAG, "Drink: " + _currentDrink);
		_listener.onDrink(_currentDrink, _thisRef);
	}

	public void setListener(IGameHandler listener)	{ this._listener = listener; }

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

	public void pauseOrStart() throws Exception
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
		else{
			if(!this._isPaused){
				this._isPaused = true;
				this._stopwatchHandler.removeCallbacks(this._stopwatchRunnable);
				this._listener.onPause(this);
			}
			else
			{
				this._isPaused = false;

				//this._startingTick = System.currentTimeMillis();
				_startingTick = System.currentTimeMillis() - _currentTick;
				this._stopwatchHandler.postDelayed(this._stopwatchRunnable, 0);

				this._listener.onStart(this);
			}
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
}
