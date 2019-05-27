package zdream.rockchronicle.core.module.health;

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
	 * <p>实际血量 = 显示血量 * 256 (左移 8 位)
	 * <p>存储位置 situation:health.hp
	 * </p>
	 */
	public int hp;
	/**
	 * <p>实际血量最大值 = 显示血量最大值 * 256 (左移 8 位)
	 * 28 * 256 = 7168
	 * <p>存储位置 situation:health.hpMax
	 * </p>
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
		
		setSituation("health.hp", new JsonValue(hp));
		setSituation("health.hpMax", new JsonValue(hpMax));
	}
	
	protected void executeOuterDamage(CharacterEvent event) {
		boolean immune = getBoolean("state.immune", false);
		
		if (immune) {
			event.value.addChild("result", new JsonValue("ignored"));
		} else {
			JsonValue v = event.value;
			int damage = v.getInt("damage");
			
			hp -= damage;
			setSituation("health.hp", new JsonValue(hp));
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
	
	@Override
	protected void executeInnerRecovery(CharacterEvent event) {
		int value = event.value.getInt("health");
		
		if (hp + value >= hpMax) {
			hp = hpMax;
		} else {
			hp += value;
		}
		setSituation("health.hp", new JsonValue(hp));
	}

}
