package zdream.rockchronicle.utils;

import java.util.Objects;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class JsonUtils {
	
	public static final JsonReader jreader = new JsonReader();
	
	/**
	 * 克隆 (深复制) 的结果中 name = null, 也将多余的 child、parent、next、prev 的引用设置为 null
	 * @param src
	 * @return
	 */
	public static JsonValue clone(JsonValue src) {
		String text = src.toJson(OutputType.minimal);
		return jreader.parse(text);
	}
	
	/**
	 * 合并 json 数据. 现阶段只合并 json 类型为 object 的数据.
	 * 如果 src != null, 合并后 dest 的数据将会加入到 src 中.
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
	
	public static void clear(JsonValue v) {
		v.child = null;
		v.size = 0;
	}
	
	public static JsonValue replace(JsonValue map, String key, JsonValue value) {
		Objects.requireNonNull(key);
		JsonValue entry = map.child;
		value.prev = value.next = null;
		value.name = key;
		
		for (; entry != null; entry = entry.next) {
			if (!key.equals(value.name)) { 
				continue;
			}
			
			if (entry.prev != null) {
				value.prev = entry.prev;
				entry.prev.next = value;
				entry.prev = null;
			}
			
			if (entry.next != null) {
				value.next = entry.next;
				entry.next.prev = value;
				entry.next = null;
			}
			
			if (entry.parent != null) {
				if (entry.parent.child == entry) {
					entry.parent.child = value;
				}
				value.parent = entry.parent;
				entry.parent = null;
			}
			
			return entry;
		}
		
		// 放到链表最后面
		map.addChild(key, value);
		
		return null;
	}
	
	public static void delete(JsonValue map, String[] keys) {
		Objects.requireNonNull(keys);
		delete(map, keys, 0);
	}
	
	private static void delete(JsonValue map, String[] keys, int offset) {
		if (keys.length == offset + 1) {
			map.remove(keys[offset]);
			return;
		}
		delete(map.get(keys[offset]), keys, offset + 1);
	}

}
