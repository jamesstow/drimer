package com.pwnasaur.drimer;

import android.app.Application;
import android.content.res.Resources;
import android.net.Uri;

/**
 * Created by user on 20/03/14.
 */
public class GameConfig  {

    public GameConfig(){
	    //Resources resources = MainActivity.baseContext.getResources();//this. Resources.getSystem();
	    String final_sound = "android.resource://com.pwnasaur.drimer/" + R.raw.final_sound;
	    String repeat_sound  = "android.resource://com.pwnasaur.drimer/" + R.raw.repeat_sound;
	    //String repeat_sound = resources.getResourceEntryName(R.raw.repeat_sound);

	    this.name = "Centurion";
        this.totalNumberOfDrinks = 100;
        this.millisecondsBetweenDrinks = 60 * 1000;
        this.repeatSoundUri = Uri.parse(repeat_sound);
        this.finalSoundUri = Uri.parse(final_sound);
    }

    public String name;
    public int totalNumberOfDrinks;
    public int millisecondsBetweenDrinks;
    public Uri repeatSoundUri;
    public Uri finalSoundUri;
}
