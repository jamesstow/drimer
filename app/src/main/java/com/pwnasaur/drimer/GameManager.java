package com.pwnasaur.drimer;

import android.os.CountDownTimer;
import android.os.Handler;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by user on 30/06/14.
 */
public class GameManager
{
	private final Set<IGameHandler> _listeners;
	private CountDownTimer _timer;
	private GameConfig _currentConfig;
	private int _drinksRemaining;
	private long _currentTick;
	private long _startingTick = -1;
	private int _currentDrink;
	private int _tickUpdateRate;
	private boolean _isPaused = false;
	private boolean _isRunning = false;

	public GameManager()
	{
		this(100);
	}

	public GameManager(int tickUpdateRate)
	{
		this._listeners = new LinkedHashSet<IGameHandler>();
		this._tickUpdateRate = tickUpdateRate;
	}

	private void initialiseTimer()
	{
		long totalMilliseconds = this._currentConfig.totalNumberOfDrinks * this._currentConfig.millisecondsBetweenDrinks;
		totalMilliseconds -= this._currentTick;

		this._timer = new CountDownTimer(totalMilliseconds, 1)
		{
			@Override
			public void onTick(long l)
			{
				timer_tick(l);
			}

			@Override
			public void onFinish()
			{
				finish();
			}
		};
	}

	private void timer_tick(long l)
	{
		if (this._startingTick == -1)
		{
			this._startingTick = l;
		}

		this._currentTick = this._startingTick - l;
		if (this._currentTick % this._tickUpdateRate == 0)
		{
			this.tick();
			if (this._currentTick != 0 && this._currentTick % this._currentConfig.millisecondsBetweenDrinks == 0)
			{
				this._currentDrink = (int) this._currentTick / (int) this._currentConfig.millisecondsBetweenDrinks;
				this.drink();
			}
		}
	}

	private void finish()
	{
		for (IGameHandler listener : this._listeners)
		{
			listener.onFinish(this);
		}
	}

	private void tick()
	{
		for (IGameHandler listener : this._listeners)
		{
			listener.onTick(this._currentTick,this._currentDrink, this);
		}
	}

	private void drink()
	{
		/*if (this._drinksRemaining == 0)
		{
			this.finish();
		} else
		{*/
		for (IGameHandler listener : this._listeners)
		{
			listener.onDrink(this._currentDrink, this);
		}
		//}
	}

	public void addListener(IGameHandler listener)
	{
		this._listeners.add(listener);
	}

	public void removeListener(IGameHandler listener)
	{
		this._listeners.remove(listener);
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

	public void start()
	{
		if (!this._isRunning)
		{
			initialiseTimer();

			this._isPaused = false;
			this._isRunning = true;

			this._timer.start();

			for (IGameHandler listener : this._listeners)
			{
				listener.onStart(this);
			}
		}
	}

	public void stop()
	{
		if (this._isRunning)
		{
			if (this._timer != null)
			{
				this._timer.cancel();
			}

			for (IGameHandler listener : this._listeners)
			{
				listener.onStop(this);
			}
		}
	}

	public void pause()
	{
		if (this._isRunning)
		{
			if (this._timer != null)
			{
				this._timer.cancel();
			}

			this._isPaused = true;

			for (IGameHandler listener : this._listeners)
			{
				listener.onPause(this);
			}
		}
	}
}
