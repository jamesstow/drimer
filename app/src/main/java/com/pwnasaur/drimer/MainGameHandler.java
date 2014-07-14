package com.pwnasaur.drimer;

import android.os.Handler;
import android.widget.TextView;

/**
 * Created by user on 01/07/14.
 */
public class MainGameHandler implements IGameHandler
{
	TextView _tvTimeToNextDrink;
	GameConfig _config;
	int _currentDrink = 0;
	long _currentTick = 0;
	String _test;
	Handler _handler;
	Runnable _tickRunnable;
	Runnable _drinkRunnable;

	public MainGameHandler(MainActivity activity){

		this._tickRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				long ticksToCurrentDrink = _currentDrink * _config.millisecondsBetweenDrinks;
				long ticksToCurrentTime = _currentTick - ticksToCurrentDrink;
				long ticksToNextDrink = ticksToCurrentDrink + _config.millisecondsBetweenDrinks;
				long tickDiff = ticksToNextDrink - ticksToCurrentTime;
				float timeToNextDrinkInSeconds = (float)(tickDiff / 1000f);

				_tvTimeToNextDrink.setText(String.format("%.1f", timeToNextDrinkInSeconds));
			}
		};

		this._drinkRunnable = new Runnable()
		{
			@Override
			public void run()
			{

			}
		};

		this._handler = new Handler();
	}
	public void onTick(long currentTick, int drinks, GameManager manager){
		this._currentTick = currentTick;


		this._handler.post(this._tickRunnable);
	}
	public void onDrink(int drink, GameManager manager){
		this._currentDrink = drink;

		this._handler.post(this._drinkRunnable);
	}
	public void onFinish(GameManager manager){

	}
	public void onPause(GameManager manager){

	}
	public void onStart(GameManager manager){

	}
	public void onStop(GameManager manager){

	}
}
