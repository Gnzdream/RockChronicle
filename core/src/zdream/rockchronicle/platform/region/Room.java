package zdream.rockchronicle.platform.region;

import com.badlogic.gdx.utils.Array;

/**
 * <p>房间
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-18 (last modified)
 */
public class Room {
	
	public Room(Region region, int index) {
		super();
		this.region = region;
		this.index = index;
	}

	/**
	 * 所属的区域
	 */
	public final Region region;
	
	/**
	 * 序号, 便于 {@link Region} 管理
	 */
	public final int index;
	
	/**
	 * 左下角这个点在 {@link Region} 中的位置
	 */
	public int offsetx, offsety;
	
	/**
	 * 房间的宽高.
	 */
	public int width, height;
	
	/**
	 * [x][y]
	 * 地形数据
	 */
	public byte[][] terrains;
	
	/**
	 * <p>地图四个方向和其它的房间相通的. 这里考虑当前房间边缘的哪些块可以通到其它房间, 房间号是多少.
	 * <p>注意, 所有跨区域的房间都是懒加载的. 只有当角色进入相关的房间, 这些跨区域的门才会加载进来.
	 * 系统需要读 {@link Region#points} 中的 {@link RegionPoint#conn} 参数, 来初始化这些门的信息
	 * </p>
	 */
	public Array<Gate> gates = new Array<>(4);
	
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

	/**
	 * 查看一个点是否在该房间范围内, 或者压住房间的边缘线.
	 * @param x
	 *   这个点在 {@link Region} 中的横坐标, 单位: 格子
	 * @param y
	 *   这个点在 {@link Region} 中的纵坐标, 单位: 格子
	 * @return
	 */
	public boolean contain(float x, float y) {
		return this.offsetx <= x && this.offsetx + this.width >= x && this.offsety <= y && this.offsety + this.height >= y;
	}
	
	/**
	 * 查看一个区域点块是否在该房间范围内
	 * @param x
	 *   这个点在 {@link Region} 中的横坐标, 单位: 格子
	 * @param y
	 *   这个点在 {@link Region} 中的纵坐标, 单位: 格子
	 * @return
	 */
	public boolean contain(int x, int y) {
		return this.offsetx <= x && this.offsetx + this.width > x && this.offsety <= y && this.offsety + this.height > y;
	}
	
	@Override
	public String toString() {
		return String.format("Room:%s[%d]", region == null ? "" : region.name, index);
	}

}
