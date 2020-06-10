package zdream.rockchronicle.platform.region;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

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
	Region(String name) {
		this.name = name;
	}
	
	/**
	 * 名称
	 */
	public final String name;
	
	/**
	 * 指代的 tmx
	 */
	public TiledMap tmx;
	
	/**
	 * 房间数
	 */
	public Room[] rooms;

	/**
	 * 出生点位 (传送到该区域时的位置) 在哪个 room 中, 相对位置是多少.
	 * 单位: 块
	 */
	public int spawnRoom = -1, spawnx, spawny;
	
	/**
	 * 连接点位
	 */
	public Array<RegionPoint> points = new Array<>();
	
	public RegionPoint findPoint(String name) {
		for (int i = 0; i < points.size; i++) {
			RegionPoint p = points.get(i);
			if (p.name.equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	/**
	 * 查看这个点在哪个房间中
	 * @param x
	 * @param y
	 * @return
	 */
	public Room of(int x, int y) {
		for (int i = 0; i < rooms.length; i++) {
			Room r = rooms[i];
			if (r.contain(x, y)) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * 查看这个点在哪个房间中
	 * @param x
	 * @param y
	 * @return
	 */
	public Room of(float x, float y) {
		for (int i = 0; i < rooms.length; i++) {
			Room r = rooms[i];
			if (r.contain(x, y)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Region other = (Region) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
