package zdream.rockchronicle.platform.region;

import com.badlogic.gdx.utils.JsonValue;

/**
 * <p>在区域中标定的怪物生成的点位, 属于区域
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-02 (created)
 *   2019-06-02 (last modified)
 */
public class FoeDef {
	
	/**
	 * 在该区域中的位置
	 */
	public float x, y;
	
	/**
	 * 哪类敌人, 怪物的名字
	 */
	public String name;
	
	/**
	 * 怪物参数
	 */
	public JsonValue param;

}
