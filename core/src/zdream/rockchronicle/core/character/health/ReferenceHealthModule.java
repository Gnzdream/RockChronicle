package zdream.rockchronicle.core.character.health;

import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.cast.CastList;
import zdream.rockchronicle.cast.lead.megaman.Megaman;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;

/**
 * <p>特殊角色的健康与生命管理的模块
 * <p>该模块获取角色生命值需要指定特殊角色的名字, 并在 {@link CastList} 中寻找该角色,
 * 将生命值的查找与设置重定向为 {@link CastList} 中的角色.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-10 (create)
 *   2019-05-10 (last modified)
 */
public class ReferenceHealthModule extends HealthModule {
	
	public String ref;
	
	/**
	 * 引用. 类名后面需要抽象 TODO
	 */
	Megaman hpRef;

	public ReferenceHealthModule(CharacterEntry ch) {
		super(ch);
	}

	@Override
	protected void initHealthArguments(JsonValue root) {
		JsonValue v = root.get("health");
	
		this.ref = v.getString("ref");
		Objects.requireNonNull(ref);
		
		// 以下部分需要作出修改 TODO
		if (ref.equals("megaman")) {
			hpRef = RockChronicle.INSTANCE.runtime.cast.megaman;
		}
	}

	@Override
	public JsonValue getHealthJson() {
		JsonValue v = new JsonValue(ValueType.object);

		v.addChild("realHp", new JsonValue(hpRef.hp));
		v.addChild("realHpMax", new JsonValue(hpRef.hpMax));
		
		return v;
	}

	@Override
	protected void executeDamageFromOutside(CharacterEvent event) {
		boolean immune = parent.getBoolean(new String[] {"state", "immune"}, false);
		
		if (immune) {
			event.value.addChild("result", new JsonValue("ignored"));
		} else {
			JsonValue v = event.value;
			float fdamage = v.getFloat("damage");
			int damage = (int) (fdamage * 256);
			Gdx.app.log("ReferenceHealthModule", String.format("%s 收到伤害 %d", ref, damage));
			
			hpRef.hp -= damage;
			if (hpRef.hp <= 0) {
				hpRef.hp = 0;
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
