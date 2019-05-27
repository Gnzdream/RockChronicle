package zdream.rockchronicle.core.module.health;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>健康与生命管理的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (create)
 *   2019-05-10 (last modified)
 */
public abstract class HealthModule extends AbstractModule {
	
	public static final String NAME = "Health";

	public HealthModule(CharacterEntry ch) {
		super(ch);
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
		parent.removeSubscribe("outside_collision", this);
		parent.removeSubscribe("inside_recovery", this);
	}
	
	protected abstract void initHealthArguments(JsonValue root);
	
	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -0xE0;
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
	
	protected abstract void executeOuterDamage(CharacterEvent event);
	
	protected abstract void executeInnerRecovery(CharacterEvent event);

}
