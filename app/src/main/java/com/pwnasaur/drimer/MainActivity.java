package com.pwnasaur.drimer;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
	private TextView _tvTimeToNextDrink;
	private TextView _tvDrinksLeft;
	private TextView _tvDrinksSoFar;
	private TextView _tvTotalDrinks;
	private Button _btnStart;
	private CountdownView _countdownView;

	private MediaPlayer _repeatPlayer;
	private MediaPlayer _finalPlayer;

	private Handler _handler;

	private long _currentTick;
	private int _currentDrink;

	private void handleDrink(int drink, GameManager manager){

		Log.d("MainActivity","Drink");
		this._tvTimeToNextDrink.setText(String.format("%.1f", (float)(this._config.millisecondsBetweenDrinks / 1000)));
		this._tvDrinksLeft.setText(String.valueOf(this._config.totalNumberOfDrinks - drink));
		this._tvDrinksSoFar.setText(String.valueOf(drink));

		this.playSound(this._repeatPlayer);
	}

	private Runnable _tickRunnable;

	private void handleTick(long tick, int drinks, GameManager manager){
		this._currentDrink = drinks;
		this._currentTick = tick;

		this._countdownView.post(new Runnable()
		{
			@Override
			public void run()
			{
				long ticksToCurrentDrink = _currentDrink * _config.millisecondsBetweenDrinks;
				long ticksToCurrentTime = _currentTick - ticksToCurrentDrink;
				long ticksToNextDrink = ticksToCurrentDrink + _config.millisecondsBetweenDrinks;
				long tickDiff = ticksToNextDrink - ticksToCurrentTime;
				float timeToNextDrinkInSeconds = (float)(tickDiff / 1000f);

				float angle = ((float)tickDiff / (float)ticksToNextDrink) * CountdownView.RING_CIRCUMFERENCE;
				_countdownView.setMarkerPosition(angle);
			}
		});

		//this._handler.post(this._tickRunnable);
	}

	private void handleFinish(GameManager manager){
		Log.d("MainActivity","Finish");
		this.playSound(this._finalPlayer);
	}

	private void handlePause(GameManager manager){
	}

	private void handleStart(GameManager manager){

	}

	private void handleStop(GameManager manager){

	}

	private GameManager _manager;
	private GameConfig _config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initialise();

        this.updateUIWithConfig();
    }

    private void initialise(){
	    this._handler = new Handler();

	    this._tickRunnable = new Runnable()
	    {
		    @Override
		    public void run()
		    {
			    Log.d("MainActivity", "Tick");

			    long ticksToCurrentDrink = _currentDrink * _config.millisecondsBetweenDrinks;
			    long ticksToCurrentTime = _currentTick - ticksToCurrentDrink;
			    long ticksToNextDrink = ticksToCurrentDrink + _config.millisecondsBetweenDrinks;
			    long tickDiff = ticksToNextDrink - ticksToCurrentTime;
			    float timeToNextDrinkInSeconds = (float)(tickDiff / 1000f);

			    _tvTimeToNextDrink.setText(String.format("%.1f", timeToNextDrinkInSeconds));
		    }
	    };

	    this._manager = new GameManager();
	    this._manager.addListener(this._gameHandler);

	    this._tvTimeToNextDrink = (TextView)findViewById(R.id.tvTimeToNextDrink);
	    this._tvDrinksLeft = (TextView)findViewById(R.id.tvDrinksLeft);
	    this._tvDrinksSoFar = (TextView)findViewById(R.id.tvDrinksSoFar);
	    this._tvTotalDrinks = (TextView)findViewById(R.id.tvTotalDrinks);
	    this._btnStart = (Button)findViewById(R.id.btnStart);

	    this._countdownView = (CountdownView)findViewById(R.id.countdown);
	    this._countdownView.addOnStartListener(new View.OnClickListener()
	    {
		    @Override
		    public void onClick(View view)
		    {
			    countdownView_Start(view);
		    }
	    });

		this.changeConfig(ConfigLoader.getInstance().getCurrentConfig());
    }

	private void changeConfig(GameConfig config){
		this._config = config;

		MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				mediaPlayer.release();
			}
		};

		this._repeatPlayer = MediaPlayer.create(this.getBaseContext(), this._config.repeatSoundUri);
		//this._repeatPlayer.setOnCompletionListener(completionListener);

		this._finalPlayer = MediaPlayer.create(this.getBaseContext(), this._config.finalSoundUri);
		//this._finalPlayer.setOnCompletionListener(completionListener);

		this._manager.changeGameConfig(config);
	}

	private void playSound(MediaPlayer player)
	{
		//player.start();
	}

    private void updateUIWithConfig(){
        this._tvTimeToNextDrink.setText(String.format("%.2f", (float)(this._config.millisecondsBetweenDrinks / 1000)));
        this._tvDrinksLeft.setText(String.valueOf(this._config.totalNumberOfDrinks));
        this._tvDrinksSoFar.setText("0");
        this._tvTotalDrinks.setText(String.valueOf(this._config.totalNumberOfDrinks));
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
        this._manager.start();
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
