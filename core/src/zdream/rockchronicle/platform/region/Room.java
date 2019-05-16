package zdream.rockchronicle.platform.region;

import com.badlogic.gdx.utils.Array;

public class Room {
	
	/**
	 * 序号, 便于 {@link Region} 管理
	 */
	public int index;
	
	/**
	 * 左下角这个点在 {@link Region} 中的位置
	 */
	public int offsetx, offsety;
	
	/**
	 * 房间的宽高.
	 */
	public int width, height;
	
	/**
	 * [行][列]
	 * 地形数据
	 */
	public int[][] terrains;
	
	/**
	 * 地图四个方向和其它的房间相通的. 这里考虑当前房间边缘的哪些块可以通到其它房间, 房间号是多少
	 */
	public Gate[] transmitLeft, transmitRight, transmitTop, transmitBottom;
	
	/**
	 * 所有场的合集
	 */
	public final Array<Field> fields = new Array<>(4);
	
	/**
	 * 查看一个方形是否在该房间范围内, 或者和该房间的区域重合. 不考虑碰边的情况
	 * @param x
	 *   这个方形左下角在 {@link Region} 中的横坐标, 单位: 格子
	 * @param y
	 *   这个方形左下角在 {@link Region} 中的纵坐标, 单位: 格子
	 * @param w
	 *   这个方形的宽, 单位: 格子
	 * @param h
	 *   这个方形的高, 单位: 格子
	 * @return
	 */
	public boolean overlaps(float x, float y, float w, float h) {
		return offsetx < x + w
				&& offsetx + width > x
				&& offsety < y + h
				&& offsety + height > y;
	}

}
