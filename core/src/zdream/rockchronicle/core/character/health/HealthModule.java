package zdream.rockchronicle.core.character.health;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.module.AbstractModule;
import zdream.rockchronicle.core.character.parameter.JsonCollector;

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
		addCollector(new JsonCollector(this::getHealthJson, "health"));
		parent.addSubscribe("outside_collision", this);
	}
	
	@Override
	public void willDestroy() {
		super.willDestroy();
		parent.removeSubscribe("outside_collision", this);
	}
	
	protected abstract void initHealthArguments(JsonValue root);
	
	public abstract JsonValue getHealthJson();

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
		executeDamageFromOutside(event);
		
		// 后置处理
		CharacterEvent after = new CharacterEvent("after_damage");
		after.value = event.value;
		parent.publishNow(after);
	}
	
	protected abstract void executeDamageFromOutside(CharacterEvent event);

}
