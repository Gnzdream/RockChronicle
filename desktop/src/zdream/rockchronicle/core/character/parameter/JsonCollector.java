package zdream.rockchronicle.core.character.parameter;

import com.badlogic.gdx.utils.JsonValue;

/**
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public class JsonCollector {
	
	public JsonCollector(IJsonRebuild builder, String first) {
		this.builder = builder;
		this.first = first;
	}
	
	private JsonValue value;
	private final IJsonRebuild builder;
	public final String first;
	
	public JsonValue get() {
		if (value == null) {
			return value = builder.rebuild();
		}
		return value;
	}
	
	public void clear() {
		value = null;
	}
	
	public interface IJsonRebuild {
		public JsonValue rebuild();
	}
	
	// ****** 数据获取
	
	public int getInt(String[] path, int defValue) {
		JsonValue v = getJson(path);
		if (v != null) {
			return v.asInt();
		}
		return defValue;
	}
	
	public String getString(String[] path, String defValue) {
		JsonValue v = getJson(path);
		if (v != null) {
			return v.asString();
		}
		return defValue;
	}
	
	public float getFloat(String[] path, float defValue) {
		JsonValue v = getJson(path);
		if (v != null) {
			return v.asFloat();
		}
		return defValue;
	}
	
	public boolean getBoolean(String[] path, boolean defValue) {
		JsonValue v = getJson(path);
		if (v != null) {
			return v.asBoolean();
		}
		return defValue;
	}
	
	public JsonValue getJson(String[] path) {
		if (first.equals(path[0])) {
			if (path.length == 1) { 
				return get();
			}
			return getJson(path, 1, get());
		}
		
		return null;
	}

	private JsonValue getJson(String[] path, int startIdx, JsonValue v) {
		boolean last = path.length == startIdx + 1;
		
		switch (v.type()) {
		case object:
			return (last) ? v.get(path[startIdx]) : getJson(path, startIdx + 1, v.get(path[startIdx]));
		case array:
			int index = Integer.parseInt(path[startIdx]);
			return (last) ? v.get(index) : getJson(path, startIdx + 1, v.get(index));
		default:
			return null;
		}
	}

}
