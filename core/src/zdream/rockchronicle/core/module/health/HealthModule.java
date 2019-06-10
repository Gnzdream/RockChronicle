package zdream.rockchronicle.core.module.health;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>健康与生命管理的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (create)
 *   2019-06-10 (last modified)
 */
public abstract class HealthModule extends AbstractModule {
	
	public static final String NAME = "health";

	public HealthModule(CharacterEntry ch, String desc) {
		super(ch, NAME, desc);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		initHealthArguments(value);
		parent.addSubscribe("outside_collision", this);
		parent.addSubscribe("inside_recovery", this);
	}
	
	@Override
	public void willDestroy() {
		super.willDestroy();
		parent.removeSubscribe(this);
	}
	
	protected void initHealthArguments(JsonValue root) {
		JsonValue ohealth = root.get("health");
		
		int stiffnessDuration = (int) (ohealth.getFloat("stiffness", 0)
				* LevelWorld.STEPS_PER_SECOND);
		
		int immuneDuration = 6;
		float fImmune = ohealth.getFloat("immune", -1);
		if (fImmune >= 0) {
			immuneDuration = (int) (fImmune * LevelWorld.STEPS_PER_SECOND);
		}
		
		setSituation("health.param.stiffness", new JsonValue(stiffnessDuration));
		setSituation("health.param.immune", new JsonValue(immuneDuration));
	}
	
	@Override
	public int priority() {
		return -0xE0;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		int stiffnessRemain = getInt("health.stiffness", 0);
		if (stiffnessRemain > 0) {
			stiffnessRemain--;
			setSituation("health.stiffness", new JsonValue(stiffnessRemain));
		}
		int immuneRemain = getInt("health.immune", 0);
		if (immuneRemain > 0) {
			immuneRemain--;
			setSituation("health.immune", new JsonValue(immuneRemain));
		}
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		switch (event.name) {
		case "outside_collision":
			recvOutsideCollision(event);
			break;
			
		case "inside_recovery":
			recvInsideRecovery(event);
			break;

		default:
			super.receiveEvent(event);
			break;
		}
	}

	protected void recvOutsideCollision(CharacterEvent event) {
		// 前置判断
		CharacterEvent before = new CharacterEvent("before_damage");
		before.value = event.value;
		parent.publishNow(before);
		
		// 结算
		executeOuterDamage(event);
		
		// 后置处理
		CharacterEvent after = new CharacterEvent("after_damage");
		after.value = event.value;
		parent.publishNow(after);
	}
	
	protected void recvInsideRecovery(CharacterEvent event) {
		// TODO 前置判断

		// 结算
		executeInnerRecovery(event);

		// TODO 后置处理
	}
	
	protected void executeOuterDamage(CharacterEvent event) {
		if ("accepted".equals(event.value.getString("result"))) {
			int damage = event.value.getInt("damage");
			
			int stiffnessDuration = getInt("health.param.stiffness", 0);
			if (stiffnessDuration > 0 && damage > 0) {
				setSituation("health.stiffness", new JsonValue(stiffnessDuration));
			}
			int immuneDuration = getInt("health.param.immune", 0);
			if (immuneDuration > 0) {
				setSituation("health.immune", new JsonValue(immuneDuration));
			}
		}
	}
	
	protected void executeInnerRecovery(CharacterEvent event) {
		
	}

}
