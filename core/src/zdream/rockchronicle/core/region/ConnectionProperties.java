package zdream.rockchronicle.core.region;

/**
 * <p>在区域中的点的连接属性
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-18 (created)
 *   2019-05-18 (last modified)
 */
public class ConnectionProperties {

	/**
	 * 所属房间
	 */
	public Room room;
	
	/**
	 * 目标区域名
	 */
	public String destRegionName;
	
	/**
	 * 目标点位名
	 */
	public String destPoint;
	
	/**
	 * 方向.
	 * @see Gate#DIRECTION_LEFT
	 * @see Gate#DIRECTION_RIGHT
	 * @see Gate#DIRECTION_TOP
	 * @see Gate#DIRECTION_BOTTOM
	 */
	public byte direction;
	
	/**
	 * 在该房间中的位置
	 */
	public int x, y;
	
	/**
	 * <p>该数据转化成的 {@link Gate}. 开始时启用懒加载, 所以该值为 null.
	 * <p>如果不为 null, 说明在目标房间的大门列表 {@link Room#gates} 已经将该类所述的门添加进去了 
	 * </p>
	 */
	public Gate gate;

}
