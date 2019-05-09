package zdream.rockchronicle.core.character.health;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

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
 *   2019-05-09 (last modified)
 */
public class HealthModule extends AbstractModule {
	
	public static final String NAME = "Health";
	
	/**
	 * 实际血量 = 显示血量 * 256 (左移 8 位)
	 */
	public int hp;
	/**
	 * 实际血量最大值 = 显示血量最大值 * 256 (左移 8 位)
	 * 28 * 256 = 7168
	 */
	public int hpMax;

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
	
	protected void initHealthArguments(JsonValue root) {
		JsonValue v = root.get("health");
		
		this.hpMax = (int) (v.getFloat("hpMax") * 256);
		if (v.has("hp")) {
			this.hp = (int) (v.getFloat("hp") * 256);
		} else {
			this.hp = hpMax;
		}
		
	}
	
	public JsonValue getHealthJson() {
		JsonValue v = new JsonValue(ValueType.object);
		
		v.addChild("realHp", new JsonValue(hp));
		v.addChild("realHpMax", new JsonValue(hpMax));
		
		return v;
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -0x100;
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
		JsonValue v = event.value;
		float fdamage = v.getFloat("damage");
		int damage = (int) (fdamage * 256);
		
		hp -= damage;
		if (hp <= 0) {
			hp = 0;
			CharacterEvent ne = new CharacterEvent("health_exhausted");
			ne.value = new JsonValue(ValueType.object);
			parent.publish(ne); // 将在这个或下个周期生效
			
			// TODO 这部分代码不应该在这里写的
			parent.willDestroy();
		}
		
		v.addChild("result", new JsonValue("accepted"));
	}

}
