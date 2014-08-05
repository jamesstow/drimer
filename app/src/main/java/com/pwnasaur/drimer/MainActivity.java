package com.pwnasaur.drimer;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	public static Context baseContext;

	private CountdownView _countdownView;
	private MediaPlayer _repeatPlayer;
	private MediaPlayer _finalPlayer;
	private Handler _updateHandler = new Handler();
	private Runnable _updateRunnable, _repeatPlayerRunnable;
	private GameManager _manager;
	private GameConfig _config;
	private long _currentTick;
	private int _currentDrink;
	private  boolean _isPaused;
	private IConfigSource _configSource;
	private CountdownStatus _status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    if(baseContext == null)
	    {
	        baseContext = this.getBaseContext();
	    }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initialise();
    }

    private void initialise(){
	    this._configSource = new MockConfigSource();
	    this._manager = new GameManager();
	    this._manager.setListener(this._gameHandler);
	    this._countdownView = (CountdownView)findViewById(R.id.countdown);
	    this._countdownView.addRingClickListener(new View.OnClickListener()
	    {
		    @Override
		    public void onClick(View view)
		    {
				pauseOrStart();
		    }
	    });


		this._updateHandler = new Handler(Looper.getMainLooper());
		this._updateRunnable = new Runnable() {
				public void run()
				{
					long ticksToCurrentDrink = _currentDrink * _config.millisecondsBetweenDrinks;
					long ticksToCurrentTime = _currentTick - ticksToCurrentDrink;
					long tickDiff = _config.millisecondsBetweenDrinks - ticksToCurrentTime;

					_status = new CountdownStatus(tickDiff, _config.millisecondsBetweenDrinks, _config.totalNumberOfDrinks, _currentDrink, _isPaused);
					_countdownView.updateUIWithStatus(_status);
				}
	    };

        this._repeatPlayerRunnable = new Runnable() {
	        public void run()
	        {
		        _repeatPlayer.start();
		        Toast.makeText(baseContext,"Drink!",Toast.LENGTH_SHORT).show();
	        }
        };

		this.changeConfig(this._configSource.getCurrent());
    }

	private void createRepeatPlayer(){
		MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				// mediaPlayer.release();
			}
		};

		try
		{
			this._repeatPlayer = new MediaPlayer();
			this._repeatPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this._repeatPlayer.setDataSource(this.getBaseContext(), this._config.repeatSoundUri);
			this._repeatPlayer.prepare();
			this._repeatPlayer.setOnCompletionListener(completionListener);
		}
		catch (Exception ex){
			Log.e(TAG,"Repeat media player initialisation failed", ex);
		}
	}

	private void createFinalPlayer(){
		MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				mediaPlayer.release();
				mediaPlayer = null;
			}
		};

		try
		{
			this._finalPlayer = new MediaPlayer();
			this._finalPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			this._finalPlayer.setDataSource(this.getBaseContext(), this._config.finalSoundUri);
			this._finalPlayer.prepare();

			this._finalPlayer.setOnCompletionListener(completionListener);
		}
		catch (Exception ex){
			Log.e(TAG,"Final media player initialisation failed", ex);
		}
	}

	private void intialiseSound(){
		createRepeatPlayer();
		createFinalPlayer();
	}

	private void changeConfig(GameConfig config){
		this._config = config;
		this.intialiseSound();
		this._manager.changeGameConfig(config);
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

	private void stop(){

	}

	private void pauseOrStart(){
		try
		{
			this._manager.pauseOrStart();
		}
		catch (Exception ex){
			Helpers.DebugLog(TAG, "Cannot start game manager", ex);
		}
	}

	private void pause(){

	}

	private Context getContext(){
		return  this.getBaseContext();
	}

	private final IGameHandler _gameHandler = new IGameHandler()
	{
		@Override
		public void onDrink(int drink, GameManager manager)
		{
			Helpers.DebugLog("MainActivity", "Drink - " + drink);
			_currentDrink = drink;
			_updateHandler.postDelayed(_repeatPlayerRunnable,0);
		}

		@Override
		public void onTick(long tick,int drinks, GameManager manager)
		{
			_currentDrink = drinks;
			_currentTick = tick;

			_updateHandler.postDelayed(_updateRunnable, 0);
		}

		@Override
		public void onFinish(GameManager manager)
		{
			Helpers.DebugLog("MainActivity", "Finish game");
			_finalPlayer.start();
		}

		@Override
		public void onPause(GameManager manager)
		{
			Helpers.DebugLog("MainActivity", "Pause game");
			_isPaused = true;
		}

		@Override
		public void onStart(GameManager manager)
		{
			Helpers.DebugLog("MainActivity", "Start game");
			_isPaused = false;
		}

		@Override
		public void onStop(GameManager manager)
		{
			Helpers.DebugLog("MainActivity", "Stop game");
		}
	};
}
