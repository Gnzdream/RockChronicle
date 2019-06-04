package zdream.rockchronicle.core.module.puppet;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>跟随方模块.
 * <p>如果一个角色是跟随某个其它的角色出现而出现的,
 * 而且会跟着它行动而做出反应, 该角色就为跟随方, 而跟随的角色称为主人.
 * <p>主人很大可能携带 {@link LeaderModule} 模块来管理它的跟随方,
 * 而跟随方很大可能携带 {@link FollowerModule} 模块来跟随主人.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-04 (created)
 *   2019-06-04 (last modified)
 */
public class FollowerModule extends AbstractModule {

	public FollowerModule(CharacterEntry parent) {
		super(parent);
	}

	@Override
	public String name() {
		return "Follower";
	}
	
	@Override
	public String description() {
		return "base";
	}
	
	@Override
	public int priority() {
		return -50;
	}

}
