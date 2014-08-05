package com.pwnasaur.drimer;

import android.util.Log;

/**
 * Created by user on 05/08/14.
 */
public final class Helpers
{
	private Helpers(){}

	public static void DebugLog(String tag, String message){
		if(Settings.IsDebug())
		{
			Log.d(tag, message);
		}
	}

	public static void DebugLog(String tag, String message, Throwable e){
		if(Settings.IsDebug())
		{
			Log.d(tag, message, e);
		}
	}

	public static void ErrorLog(String tag, String message){
		if(Settings.LogErrors())
		{
			Log.e(tag, message);
		}
	}

	public static void ErrorLog(String tag, String message, Throwable e){
		if(Settings.LogErrors())
		{
			Log.e(tag, message, e);
		}
	}
}
