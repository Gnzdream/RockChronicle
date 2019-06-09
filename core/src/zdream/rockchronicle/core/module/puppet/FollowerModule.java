package zdream.rockchronicle.core.module.puppet;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
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
 * 当跟随方某一帧发现主人已经消失或者死亡, 则立即自毁 (默认情况下).
 * 如果跟随方需要自己离开主人, 需要向主人发送 release_follower 事件
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-04 (created)
 *   2019-06-04 (last modified)
 */
public class FollowerModule extends AbstractModule implements IMovable {
	
	public static final String NAME = "follower";

	public FollowerModule(CharacterEntry parent) {
		this(parent, "base");
	}

	protected FollowerModule(CharacterEntry parent, String desc) {
		super(parent, NAME, desc);
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
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		parent.addSubscribe("detach_leader", this);
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		// 当发现主人已经消失或者死亡, 则立即自毁
		int leaderId = param.leaderId;
		CharacterEntry entry = parent.findEntry(leaderId);
		if (entry == null || !entry.isExists()) {
			parent.willDestroy();
		}
	}

	@Override
	public void action(LevelWorld world, Box leaderBox, CharacterEntry leader) {
		// 该方法是由主人调用的, 用于同步主人和自己的位置.

		// 现在只支持单一盒子的角色
		Box box = parent.getBoxModule().getBox();
		box.setAnchor(leaderBox.anchor.x + param.offx, leaderBox.anchor.y + param.offy);
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("detach_leader".equals(event.name)) {
			// 需要将本模块删除了
			super.willDelete = true;
		}
		super.receiveEvent(event);
	}

}
