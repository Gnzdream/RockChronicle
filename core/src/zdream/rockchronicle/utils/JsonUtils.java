package zdream.rockchronicle.utils;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class JsonUtils {
	
	public static final JsonReader jreader = new JsonReader();
	
	public static JsonValue clone(JsonValue src) {
		String text = src.toJson(OutputType.minimal);
		return jreader.parse(text);
	}
	
	/**
	 * 合并 json 数据. 现阶段只合并 json 类型为 object 的数据
	 * @param src
	 * @param dest
	 */
	public static JsonValue mergeJson(JsonValue src, JsonValue dest) {
		if (dest == null) {
			return src;
		}
		if (src == null) {
			return clone(dest);
		}
		
		ValueType type = src.type();
		switch (type) {
		case nullValue:
		case booleanValue:
		case longValue:
		case doubleValue:
		case stringValue:
			switch (dest.type()) {
			case nullValue:
			case booleanValue:
			case longValue:
			case doubleValue:
			case stringValue:
				return clone(dest);
			default:
				return src;
			}
		case array:
			if (dest.type() == type) {
				return clone(dest);
			}
			break;
		case object: {
			if (dest.type() != type) {
				return src;
			}
			for (JsonValue child : dest) {
				String key = child.name;
				JsonValue v = mergeJson(src.get(key), child);
				if (v != src) {
					src.remove(key);
					v.next = v.prev = null; // 这是原引擎的 BUG
					src.addChild(key, v);
				}
			}
		} break;

		default:
			break;
		}
		
		return src;
	}

}
