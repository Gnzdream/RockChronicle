package zdream.rockchronicle.core.module.puppet;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.move.IMovable;

/**
 * <p>领队模块.
 * <p>为跟随者的主人, 所有其跟随者接受其管理.
 * <p>主人很大可能携带 {@link LeaderModule} 模块来管理它的跟随方,
 * 而跟随方很大可能携带 {@link FollowerModule} 模块来跟随主人.
 * <p>为了做到跟随方随着主人行动, 跟随方会将 {@link IMovable} 添加到主人的行动列表中,
 * 并设置负的优先度, 在主人移动完成之后, 对跟随方进行移动.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-04 (created)
 *   2019-06-04 (last modified)
 */
public class LeaderModule extends AbstractModule {

	public LeaderModule(CharacterEntry parent) {
		super(parent);
	}

	@Override
	public String name() {
		return "Leader";
	}
	
	@Override
	public String description() {
		return "base";
	}
	
	@Override
	public int priority() {
		return -50;
	}
	
	/*
	 * 这里需要确定
	 * 1. 携带的跟随方的参数, 包含跟随方的初始位置 (锚点与本角色锚点位置之差)
	 * 如果是出场时自动添加上去的, 那在该模块 init 方法执行时,
	 * 创建跟随方的实体.
	 */
	Array<BoxParam> boxParams = new Array<>();
	
	class BoxParam {
		/**
		 * = 跟随方锚点 - 目标锚点
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
		 * 目标角色 id. 该参数只能在角色创建之后才有
		 */
		int id;
	}

}
