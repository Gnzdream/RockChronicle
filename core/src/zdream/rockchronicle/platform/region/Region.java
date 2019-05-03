package zdream.rockchronicle.platform.region;

import com.badlogic.gdx.maps.tiled.TiledMap;

/**
 * 
 * @author Zdream
 * @since v0.1
 */
public class Region {

	/**
	 * 包内生成
	 */
	Region() {}
	
	/**
	 * 指代的 tmx
	 */
	public TiledMap tmx;
	
	/**
	 * 房间数
	 */
	public Room[] rooms;

	/**
	 * 出生点位 (传送到该区域时的位置) 在哪个 room 中, 相对位置是多少
	 */
	public int spawnRoom = -1, spawnx, spawny;
}
