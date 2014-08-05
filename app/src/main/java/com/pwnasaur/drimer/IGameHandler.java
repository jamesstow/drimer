package com.pwnasaur.drimer;

/**
 * Created by user on 30/06/14.
 */
public interface IGameHandler
{
	void onTick(long currentTick, GameManager manager);
	void onDrink(int drink, GameManager manager);
	void onFinish(GameManager manager);
	void onPause(GameManager manager);
	void onStart(GameManager manager);
	void onStop(GameManager manager);
}