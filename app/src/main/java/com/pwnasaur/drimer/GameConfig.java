package com.pwnasaur.drimer;

import android.net.Uri;

/**
 * Created by user on 20/03/14.
 */
public class GameConfig {

    public GameConfig(){
        this.totalNumberOfDrinks = 100;
        this.millisecondsBetweenDrinks = 60 * 1000;
        this.repeatSoundUri = Uri.parse(this.repeatSound);
        this.finalSoundUri = Uri.parse(this.finalSound);
    }

    public String name = "Centurion";
    public int totalNumberOfDrinks;
    public int millisecondsBetweenDrinks;
    public String repeatSound = "repeat.mp3";
    public String finalSound = "final.mp3";

    public Uri repeatSoundUri;
    public Uri finalSoundUri;
}
