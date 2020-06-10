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
	 * 左下角这个点在 {@link Region} 中的位置. 单位: 块
	 */
	public int offsetx, offsety;
	public int poffsetx, poffsety;
	
	/**
	 * 房间的宽高. 单位: 块
	 */
	public int width, height;
	public int pwidth, pheight;
	
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
	 * 所有敌人怪物点位
	 */
	public Array<FoeDef> foes = new Array<>();
	
	/**
	 * 所有场的合集
	 */
	public final Array<FieldDef> fields = new Array<>(4);
	
	/**
	 * 查看一个方形是否在该房间范围内, 或者和该房间的区域重合. 不考虑碰边的情况
	 * @param x
	 *   这个方形左下角在 {@link Region} 中的横坐标, 单位: 块
	 * @param y
	 *   这个方形左下角在 {@link Region} 中的纵坐标, 单位: 块
	 * @param w
	 *   这个方形的宽, 单位: 块
	 * @param h
	 *   这个方形的高, 单位: 块
	 * @return
	 */
	public boolean overlaps(float bx, float by, float bw, float bh) {
		return offsetx < bx + bw
				&& offsetx + pwidth > bx
				&& offsety < by + bh
				&& offsety + pheight > by;
	}

	/**
	 * 查看一个点是否在该房间范围内, 或者压住房间的边缘线.
	 * @param x
	 *   这个点在 {@link Region} 中的横坐标, 单位: 块
	 * @param y
	 *   这个点在 {@link Region} 中的纵坐标, 单位: 块
	 * @return
	 */
	public boolean contain(float x, float y) {
		return this.offsetx <= x && this.offsetx + this.width >= x && this.offsety <= y && this.offsety + this.height >= y;
	}

	/**
	 * 查看一个点是否在该房间范围内, 或者压住房间的边缘线.
	 * @param bx
	 *   这个点在当前房间中的横坐标, 单位: 块
	 * @param by
	 *   这个点在当前房间中的纵坐标, 单位: 块
	 * @return
	 *   1: 房间内
	 *   0: 在边缘线
	 *   -1: 房间外
	 */
	public int containInRoomForBlock(float bx, float by) {
		if (bx > width || bx < 0 || by > height || by < 0) {
			return -1;
		}
		return (0 < bx && width > bx && 0 < by && height > by) ? 1 : 0;
	}

	/**
	 * 查看一个点是否在该房间范围内, 或者压住房间的边缘线.
	 * @param px
	 *   这个点在当前房间中的横坐标, 单位: p
	 * @param py
	 *   这个点在当前房间中的纵坐标, 单位: p
	 * @return
	 *   1: 房间内
	 *   0: 在边缘线
	 *   -1: 房间外
	 */
	public int containInRoom(int px, int py) {
		if (pwidth == 0) {
			throw new IllegalStateException("Room.containInRoom(int,int)");
		}
		if (px > pwidth || px < 0 || py > pheight || py < 0) {
			return -1;
		}
		return (0 < px && pwidth > px && 0 < py && pheight > py) ? 1 : 0;
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
