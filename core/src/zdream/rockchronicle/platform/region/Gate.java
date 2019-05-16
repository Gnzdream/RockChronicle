package zdream.rockchronicle.platform.region;

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
 *   2019-05-06 (last modified)
 */
public class Gate {
	
	public static final byte DIRECTION_LEFT = 0;
	public static final byte DIRECTION_RIGHT = 1;
	public static final byte DIRECTION_TOP = 2;
	public static final byte DIRECTION_BOTTOM = 3;

	/**
	 * 从哪个房间
	 */
	public int srcRoom;
	
	/**
	 * 到哪个房间
	 */
	public int destRoom;
	
	/**
	 * 位置
	 */
	public int x, y;
	
	/**
	 * 方向
	 */
	public byte direction;
	
	/**
	 * TODO 规则，比如向上必须是爬梯子等
	 */

}
