package com.pwnasaur.drimer;

import java.util.List;

/**
 * Created by user on 19/07/14.
 */
public interface IConfigSource
{
	List<GameConfig> getAll();
	void Save(GameConfig config);
	void Delete(GameConfig config);
	GameConfig getCurrent();
}
