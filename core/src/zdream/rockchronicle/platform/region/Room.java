package zdream.rockchronicle.platform.region;

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
	 */
	public int[][] terrains;

}
