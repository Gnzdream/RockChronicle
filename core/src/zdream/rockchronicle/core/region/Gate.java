package zdream.rockchronicle.core.region;

/**
 * <p>大门
 * <p>地图四个方向和其它的房间相通的.
 * 这里考虑当前房间边缘的哪些块可以通到其它房间, 这些块统称为大门.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-15 (created)
 *   2019-05-18 (last modified)
 */
public class Gate {
	
	public Gate(Room srcRoom, Room destRoom) {
		this.srcRoom = srcRoom;
		this.destRoom = destRoom;
	}
	
	public static final byte DIRECTION_LEFT = 0;
	public static final byte DIRECTION_RIGHT = 1;
	public static final byte DIRECTION_TOP = 2;
	public static final byte DIRECTION_BOTTOM = 3;

	/**
	 * 从哪个房间
	 */
	public final Room srcRoom;
	
	/**
	 * 到哪个房间
	 */
	public final Room destRoom;
	
	/**
	 * <p>与切换房间的方向垂直的坐标分量上, 两个房间的坐标差值.
	 * <p>如果从 srcRoom 的 (x1, 24) 坐标往右进入 destRoom 的 (x2, 1) 位置,
	 * 差值为 x2 - x1.
	 * <p>如果不是左右切换房间, 而是上下切换房间, 那么该值就是纵坐标的差值.
	 * </p>
	 */
	public int offset;
	
	/**
	 * <p>如果两个房间来源于不同的区域, 则这两个区域的原点之间的差值.
	 * destRegion - srcRegion = (offsetXOfRegion, offsetYOfRegion)
	 * <p>如果两个房间来源于同一个区域, 两个值为 0
	 * </p>
	 */
	public int offsetXOfRegion, offsetYOfRegion;
	
	/**
	 * 方向
	 */
	public byte direction;
	
	/**
	 * <p>在源房间 (srcRoom) 中, 哪些房间是可以作为移屏的位置的
	 * <p>该列表记录了一系列的坐标, 仍然是与切换房间的方向垂直的坐标分量上的,
	 * 即上下切房间是记录横坐标, 左右切房间是记录纵坐标
	 * </p>
	 */
	public int[] exits;
	
	/*
	 * TODO 规则，比如向上必须是爬梯子等
	 */

	
	@Override
	public String toString() {
		String s;
		switch (direction) {
		case DIRECTION_LEFT: s = "LEFT"; break;
		case DIRECTION_RIGHT: s = "RIGHT"; break;
		case DIRECTION_TOP: s = "TOP"; break;
		case DIRECTION_BOTTOM: s = "BOTTOM"; break;
		default: s = ""; break;
		}
		return String.format("Gate:%s->(%s)->%s", srcRoom, s, destRoom);
	}
}
