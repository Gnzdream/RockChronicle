package zdream.rockchronicle.core.foe;

/**
 * 用于在检测碰撞盒子是否碰边时, 记录碰撞盒子数据的
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-18 (created)
 *   2019-05-18 (last modified)
 */
public class BoxOccupation {
	
	/**
	 * 盒子左侧的横坐标, 单位: 块, 向下取整.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 2
	 */
	public int xleft;
	
	/**
	 * 盒子的左侧横坐标是否是整数
	 */
	public boolean xleftTightly;
	
	/**
	 * 盒子右侧的横坐标, 单位: 块, 向下取整. 如果盒子右侧横坐标为整数, 则减一.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 1
	 */
	public int xright;
	
	/**
	 * 盒子的右侧横坐标是否是整数
	 */
	public boolean xrightTightly;
	
	/**
	 * 盒子上侧的纵坐标, 单位: 块, 向下取整. 如果盒子上侧纵坐标为整数, 则减一.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 1
	 */
	public int ytop;
	
	/**
	 * 盒子的上侧纵坐标, 是否是整数
	 */
	public boolean ytopTightly;
	
	/**
	 * 盒子下侧的纵坐标, 单位: 块, 向下取整.
	 * 
	 * 比如 0.5 -> 0
	 * 比如 -0.5 -> -1
	 * 比如 2 -> 2
	 */
	public int ybottom;
	
	/**
	 * 盒子的下侧纵坐标是否是整数
	 */
	public boolean ybottomTightly;
	
}
