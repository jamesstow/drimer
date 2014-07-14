package com.pwnasaur.drimer;

import android.content.Context;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 20/03/14.
 */
public class ConfigLoader {
    private static ConfigLoader _instance = new ConfigLoader();

    public static ConfigLoader getInstance() {
        return _instance;
    }

    private static String configLocation = "config_location.dump";

    public ArrayList<GameConfig> configurations = new ArrayList<GameConfig>();

    private ConfigLoader() {
        this.configurations.add((new GameConfig())); // add the default one.
        //FileInputStream fsi = openFileInput(configLocation, Context.MODE_PRIVATE);

    }

    public GameConfig getCurrentConfig(){
        return this.configurations.get(0);
    }

    public void saveConfig(GameConfig config){

    }

    public void deleteConfig(GameConfig config){

    }
}
