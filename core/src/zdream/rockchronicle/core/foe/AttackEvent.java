package zdream.rockchronicle.core.foe;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.GameRuntime;

public class AttackEvent {
	
	public AttackEvent(byte attackCamp, int damage, int damageLevel, Foe attacker) {
		super();
		this.attackCamp = attackCamp;
		this.damage = damage;
		this.damageLevel = damageLevel;
		this.attacker = attacker;
	}

	public byte attackCamp;
	public int damage;
	public int damageLevel;
	public Foe attacker;
	public ObjectMap<String, JsonValue> tags;
	
	public String recieveResponse;
	
	public FoeEvent createDamageEvent() {
		FoeEvent damageEvent = new FoeEvent("applyDamage");
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("recieved", new JsonValue(false));
		v.addChild("camp", new JsonValue(this.attackCamp));
		v.addChild("level", new JsonValue(damageLevel));
		v.addChild("damage", new JsonValue(damage));
		v.addChild("attacker", new JsonValue(attacker.name));
		v.addChild("tags", new JsonValue(ValueType.array));
		damageEvent.value = v;
		return damageEvent;
	}
	
	public static AttackEvent from(FoeEvent event, GameRuntime runtime) {
		if (!event.name.equals("applyDamage")) {
			throw new IllegalArgumentException(event.toString() + "--" + event.value);
		}
		
		JsonValue root = event.value;
		AttackEvent ae = new AttackEvent(root.getByte("camp"), root.getInt("damage"),
				root.getInt("level"), runtime.findEntry(root.getInt("attacker")));
		
		// TODO tags & recieveResponse
		return ae;
	}

}
