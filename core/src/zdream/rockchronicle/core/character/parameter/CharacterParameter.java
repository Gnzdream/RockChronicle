package zdream.rockchronicle.core.character.parameter;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.utils.JsonUtils;

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
	
	public CharacterParameter setMotionVelocity(float x, float y) {
		JsonValue current = value;
		current = createIfNotExist(current, "motion");
		current = createIfNotExist(current, "velocity");
		
		current.addChild("x", new JsonValue(x));
		current.addChild("y", new JsonValue(y));
		return this;
	}
	
	public CharacterParameter setStateOrientation(boolean orientation) {
		JsonValue current = value;
		current = createIfNotExist(current, "state");
		
		current.addChild("orientation", new JsonValue(orientation));
		return this;
	}
	
	public CharacterParameter setCamp(int camp) {
		JsonValue current = value;
		current = createIfNotExist(current, "camp");
		
		current.addChild("camp", new JsonValue(camp));
		return this;
	}
	
	/**
	 * LinearMotionModule: motion.flipX
	 */
	public CharacterParameter setMotionFlipX(boolean flipX) {
		JsonValue current = value;
		current = createIfNotExist(current, "motion");
		
		current.addChild("flipX", new JsonValue(flipX));
		return this;
	}
	
	public static CharacterParameter newInstance() {
		CharacterParameter p = new CharacterParameter();
		p.value = new JsonValue(ValueType.object);
		return p;
	}
	
	/**
	 * @param param
	 *   内部 param 会克隆一次
	 * @return
	 */
	public static CharacterParameter newInstance(JsonValue param) {
		CharacterParameter p = new CharacterParameter();
		p.value = JsonUtils.clone(param);
		return p;
	}
	
	private JsonValue createIfNotExist(JsonValue root, String key) {
		if (!root.has(key)) {
			root.addChild(key, new JsonValue(ValueType.object));
		}
		return root.get(key);
	}

}
