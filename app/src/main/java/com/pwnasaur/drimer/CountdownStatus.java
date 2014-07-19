package com.pwnasaur.drimer;

/**
 * Created by user on 16/07/14.
 */
public class CountdownStatus
{
	public boolean isPaused = false;
	public float timeToNextDrink;
	public float timeBetweenDrinks;
	public int totalDrinks;
	public int currentDrink;

	public CountdownStatus(float timeToNextDrink, float timeBetweenDrinks, int totalDrinks, int currentDrink, boolean isPaused ){
		this.timeToNextDrink = timeToNextDrink;
		this.timeBetweenDrinks = timeBetweenDrinks;
		this.totalDrinks = totalDrinks;
		this.currentDrink = currentDrink;
		this.isPaused = isPaused;
	}
}
