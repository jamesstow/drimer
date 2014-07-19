package com.pwnasaur.drimer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 19/07/14.
 */
public class MockConfigSource implements IConfigSource{

	List<GameConfig> _configs;

	public MockConfigSource(){
		this._configs = new ArrayList<GameConfig>();
	}

	@Override
	public List<GameConfig> getAll()
	{
		return this._configs;
	}

	@Override
	public void Save(GameConfig config)
	{
		this.Delete(config);
		this._configs.add(config);
	}

	@Override
	public void Delete(GameConfig config)
	{
		GameConfig existing = null;
		for(GameConfig c : this._configs){
			if(c.name.equals(config.name)){
				existing = c;
				break;
			}
		}

		if(existing != null){
			this._configs.remove(existing);
		}
	}

	@Override
	public GameConfig getCurrent()
	{
		return  new GameConfig();
	}
}
