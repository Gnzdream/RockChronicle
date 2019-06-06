package zdream.rockchronicle.core.module.puppet;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

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
public class FollowerModule extends AbstractModule implements IMovable {
	
	public static final String NAME = "Follower";

	public FollowerModule(CharacterEntry parent) {
		super(parent);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public String description() {
		return "base";
	}
	
	@Override
	public int priority() {
		return -50;
	}
	
	FollowerParam param;
	
	public void setFollowerParam(FollowerParam param) {
		this.param = param;
	}

	@Override
	public void action(LevelWorld world, Box leaderBox, CharacterEntry leader) {
		// 该方法是由主人调用的, 用于同步主人和自己的位置.

		// 现在只支持单一盒子的角色
		Box box = parent.getBoxModule().getBox();
		box.setAnchor(leaderBox.anchor.x + param.offx, leaderBox.anchor.y + param.offy);
	}

}
