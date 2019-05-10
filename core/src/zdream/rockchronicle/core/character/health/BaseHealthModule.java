package zdream.rockchronicle.core.character.health;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;

/**
 * <p>基础的健康与生命管理的模块
 * <p>该模块储存角色的生命值, 并进行管理
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-10 (create)
 *   2019-05-10 (last modified)
 */
public class BaseHealthModule extends HealthModule {
	
	/**
	 * 实际血量 = 显示血量 * 256 (左移 8 位)
	 */
	public int hp;
	/**
	 * 实际血量最大值 = 显示血量最大值 * 256 (左移 8 位)
	 * 28 * 256 = 7168
	 */
	public int hpMax;

	public BaseHealthModule(CharacterEntry ch) {
		super(ch);
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
	
	protected void executeDamageFromOutside(CharacterEvent event) {
		boolean immune = parent.getBoolean(new String[] {"state", "immune"}, false);
		
		if (immune) {
			event.value.addChild("result", new JsonValue("ignored"));
		} else {
			JsonValue v = event.value;
			float fdamage = v.getFloat("damage");
			int damage = (int) (fdamage * 256);
			
			hp -= damage;
			if (hp <= 0) {
				hp = 0;
				CharacterEvent ne = new CharacterEvent("health_exhausted");
				ne.value = new JsonValue(ValueType.object);
				parent.publish(ne); // 将在这个或下个周期生效

				// TODO 这部分代码可能要作出修改
				parent.willDestroy();
			}
			v.addChild("result", new JsonValue("accepted"));
		}
	}

}
