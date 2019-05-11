package zdream.rockchronicle.core.module.motion;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.parameter.IValueCreator;

/**
 * 行动模块 Box 参数的设置工具
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-07 (create)
 *   2019-05-07 (last modified)
 */
public class BoxSetter implements IValueCreator<JsonValue> {
	
	private JsonValue value;

	@Override
	public JsonValue get() {
		return value;
	}
	
	private BoxSetter() {}
	
	public BoxSetter setAnchor(float x, float y) {
		JsonValue current = value;
		current = createIfNotExist(current, "anchor");
		
		current.addChild("x", new JsonValue(x));
		current.addChild("y", new JsonValue(y));
		return this;
	}
	
	public BoxSetter setAnchorX(float x) {
		JsonValue current = value;
		current = createIfNotExist(current, "anchor");
		
		current.addChild("x", new JsonValue(x));
		return this;
	}
	
	public BoxSetter setAnchorY(float y) {
		JsonValue current = value;
		current = createIfNotExist(current, "anchor");
		
		current.addChild("y", new JsonValue(y));
		return this;
	}
	
	public BoxSetter setVelocity(float x, float y) {
		JsonValue current = value;
		current = createIfNotExist(current, "velocity");
		
		current.addChild("x", new JsonValue(x));
		current.addChild("y", new JsonValue(y));
		return this;
	}
	
	public BoxSetter setVelocityX(float x) {
		JsonValue current = value;
		current = createIfNotExist(current, "velocity");
		
		current.addChild("x", new JsonValue(x));
		return this;
	}
	
	public BoxSetter setVelocityY(float y) {
		JsonValue current = value;
		current = createIfNotExist(current, "velocity");
		
		current.addChild("y", new JsonValue(y));
		return this;
	}
	
	public static BoxSetter newInstance() {
		BoxSetter p = new BoxSetter();
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
