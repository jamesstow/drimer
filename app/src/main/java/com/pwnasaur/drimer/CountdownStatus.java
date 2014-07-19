package com.pwnasaur.drimer;

/**
 * Created by user on 16/07/14.
 */
public class CountdownStatus
{
	public final float timeToNextDrink;
	public final float timeBetweenDrinks;
	public int totalDrinks;
	public int currentDrink;

	public CountdownStatus(float timeToNextDrink, float timeBetweenDrinks, int totalDrinks, int currentDrink){
		this.timeToNextDrink = timeToNextDrink;
		this.timeBetweenDrinks = timeBetweenDrinks;
		this.totalDrinks = totalDrinks;
		this.currentDrink = currentDrink;
	}
}
