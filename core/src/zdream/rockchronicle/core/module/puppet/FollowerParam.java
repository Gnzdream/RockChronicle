package zdream.rockchronicle.core.module.puppet;

import com.badlogic.gdx.utils.JsonValue;

/**
 * 跟随方的参数
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-04 (created)
 *   2019-06-04 (last modified)
 */
class FollowerParam {
	public FollowerParam(int leaderId) {
		super();
		this.leaderId = leaderId;
	}
	/**
	 * 跟随方锚点 - 目标锚点
	 */
	float offx, offy;
	
	/**
	 * 目标角色名称
	 */
	String name;
	/**
	 * 目标角色参数
	 */
	JsonValue param;
	/**
	 * 跟随方 id. 该参数只能在角色创建之后才有
	 */
	int followerId;
	/**
	 * 领队 / 主人 id
	 */
	int leaderId;
}