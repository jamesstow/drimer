package com.pwnasaur.drimer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by user on 16/07/14.
 */
public class CountdownViewManager
{
	private class CountdownViewRunnable implements Runnable {
		CountdownStatus _status;
		CountdownView _view;

		public CountdownViewRunnable(CountdownStatus status, CountdownView view)
		{
			this._status = status;
			this._view = view;
		}

		@Override
		public void run()
		{
			float angle = (this._status.timeToNextDrink / this._status.timeBetweenDrinks) * CountdownView.RING_CIRCUMFERENCE;
			this._view.setMarkerPosition(angle);
		}
	}

	private Handler _handler;
	private CountdownView _view;

	public CountdownViewManager(CountdownView view)
	{
		this._view = view;
		this.init();
	}

	private  void init(){
		this._handler = new Handler(Looper.getMainLooper()){
			@Override
			public void handleMessage(Message msg)
			{
				if(msg.obj == null){
					return;
				}

				CountdownStatus status = (CountdownStatus)msg.obj;
				new CountdownViewRunnable(status,_view).run();
			}
		};
	}

	public void postMessage(CountdownStatus status){
		Message m = new Message();
		m.obj = status;
		this._handler.handleMessage(m);
	}
}
