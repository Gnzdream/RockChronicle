package zdream.rockchronicle.core.region;

/**
 * <p>在区域中标定的点, 属于区域
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-18 (created)
 *   2019-05-18 (last modified)
 */
public class RegionPoint {
	
	public String name, type;
	
	/**
	 * 在该区域中的位置
	 */
	public int x, y;
	
	/**
	 * 连接参数. 可能为 null
	 */
	public ConnectionProperties conn;

}
