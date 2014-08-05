package com.pwnasaur.drimer;

/**
 * Created by user on 05/08/14.
 */
public final class Settings
{
	private Settings(){}

	public static final int DEFAULT_RING_ELAPSED_COLOUR = 0xFFFF0000;
	public static final int DEFAULT_RING_INACTIVE_COLOUR = 0xFF222222;
	public static final int DEFAULT_VIEW_TEXT_COLOUR = 0xFFAAAAAA;

	public static boolean IsDebug(){
		return true;
	}

	public static boolean LogErrors(){
		return true;
	}
}
