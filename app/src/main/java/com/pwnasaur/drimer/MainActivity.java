package com.pwnasaur.drimer;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	private CountdownView _countdownView;
	private CountdownViewManager _viewManager;

	private MediaPlayer _repeatPlayer;
	private MediaPlayer _finalPlayer;

	private Handler _updateHandler = new Handler();

	private long _currentTick;
	private int _currentDrink;

	private void handleDrink(int drink, GameManager manager){
		//Log.d("MainActivity","Drink - " + drink);
		this._currentDrink = drink;
		this.playSound(this._repeatPlayer);
	}

	private Runnable _updateRunnable;

	private void handleTick(long tick, int drinks, GameManager manager){
		this._currentDrink = drinks;
		this._currentTick = tick;

		this._updateHandler.postDelayed(this._updateRunnable, 0);
	}

	private void handleFinish(GameManager manager){
		//Log.d("MainActivity","Finish");
		this.playSound(this._finalPlayer);
	}

	private void handlePause(GameManager manager){}

	private void handleStart(GameManager manager){}

	private void handleStop(GameManager manager){}

	private GameManager _manager;
	private GameConfig _config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initialise();
    }

    private void initialise(){
	    this._manager = new GameManager();
	    this._manager.setListener(this._gameHandler);
	    this._countdownView = (CountdownView)findViewById(R.id.countdown);
	    this._countdownView.addOnStartListener(new View.OnClickListener()
	    {
		    @Override
		    public void onClick(View view)
		    {
			    countdownView_Start(view);
		    }
	    });

		this._updateHandler = new Handler(Looper.getMainLooper());
		this._updateRunnable = new Runnable() {
				public void run()
				{
					long ticksToCurrentDrink = _currentDrink * _config.millisecondsBetweenDrinks;
					long ticksToCurrentTime = _currentTick - ticksToCurrentDrink;
					long ticksToNextDrink = ticksToCurrentDrink + _config.millisecondsBetweenDrinks;
					long tickDiff = ticksToNextDrink - ticksToCurrentTime;

					CountdownStatus status = new CountdownStatus(tickDiff, _config.millisecondsBetweenDrinks, _config.totalNumberOfDrinks, _currentDrink);
					_countdownView.updateUiByStatus(status);
				}
	    };

	    this._viewManager = new CountdownViewManager(this._countdownView);

		this.changeConfig(ConfigLoader.getInstance().getCurrentConfig());
    }

	private void intialiseSound(){
		MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				mediaPlayer.release();
			}
		};

		this._repeatPlayer = MediaPlayer.create(this.getBaseContext(), this._config.repeatSoundUri);
		this._repeatPlayer.setOnCompletionListener(completionListener);

		this._finalPlayer = MediaPlayer.create(this.getBaseContext(), this._config.finalSoundUri);
		this._finalPlayer.setOnCompletionListener(completionListener);
	}

	private void changeConfig(GameConfig config){
		this._config = config;
		//this.intialiseSound();
		this._manager.changeGameConfig(config);
	}

	private void playSound(MediaPlayer player)
	{
		//player.start();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

	public void countdownView_Start(View view){
		this.btnStart_Click(view);
	}

    public void btnStart_Click(View view){
	    try
	    {
		    this._manager.start();
	    }
	    catch (Exception ex){
		    Log.d(TAG,"Cannot start game manager", ex);
	    }
    }

	private final IGameHandler _gameHandler = new IGameHandler()
	{
		@Override
		public void onDrink(int drink, GameManager manager)
		{
			handleDrink(drink, manager);
		}

		@Override
		public void onTick(long tick,int drinks, GameManager manager)
		{
			handleTick(tick, drinks,manager);
		}

		@Override
		public void onFinish(GameManager manager)
		{
			handleFinish(manager);
		}

		@Override
		public void onPause(GameManager manager)
		{
			handlePause(manager);
		}

		@Override
		public void onStart(GameManager manager)
		{
			handleStart(manager);
		}

		@Override
		public void onStop(GameManager manager)
		{
			handleStop(manager);
		}
	};
}
