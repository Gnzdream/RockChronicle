package zdream.rockchronicle.platform.region;

import com.badlogic.gdx.maps.tiled.TiledMap;

/**
 * <p>区域. 一个区域由多个房间组成, 而多个区域可以作为一个关卡.
 * <p>每个区域可以由多个 TMX 文件定义, 而它们可能需要进行拼接工作,
 * 在系统加载时合并成一张大的地图.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (last modified)
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
