package zdream.rockchronicle.core.character.parameter;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

/**
 * 角色初始化数据的创建工具
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-05 (create)
 *   2019-05-07 (last modify)
 */
public class CharacterParameter implements IValueCreator<JsonValue> {
	
	private JsonValue value;
	
	public JsonValue get() {
		return value;
	}
	
	private CharacterParameter() {}
	
	public CharacterParameter setBoxAnchor(float x, float y) {
		JsonValue current = value;
		current = createIfNotExist(current, "box");
		current = createIfNotExist(current, "anchor");
		
		current.addChild("x", new JsonValue(x));
		current.addChild("y", new JsonValue(y));
		return this;
	}
	
	public CharacterParameter setMotionOrientation(boolean orientation) {
		JsonValue current = value;
		current = createIfNotExist(current, "motion");
		
		current.addChild("orientation", new JsonValue(orientation));
		return this;
	}
	
	public CharacterParameter setCamp(int camp) {
		JsonValue current = value;
		current = createIfNotExist(current, "camp");
		
		current.addChild("camp", new JsonValue(camp));
		return this;
	}
	
	public static CharacterParameter newInstance() {
		CharacterParameter p = new CharacterParameter();
		p.value = new JsonValue(ValueType.object);
		return p;
	}
	
	private JsonValue createIfNotExist(JsonValue root, String key) {
		if (!root.has(key)) {
			root.addChild(key, new JsonValue(ValueType.object));
		}
		return root.get(key);
	}

}
