package zdream.rockchronicle.core.character;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

/**
 * <p>人物的 {@link CharacterEntry} 的默认创建工具.
 * <p>利用 Json 数据构造 CharacterEntry 以及其拥有的 Module
 * </p>
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-05 (create)
 */
public class CharacterBuilder {
	
	final JsonReader jreader = new JsonReader();

	{
		
	}
	
	public void init() {
		rescan(Paths.get("res", "characters"));
	}
	
	private final HashMap<String, CharacterDef> defs = new HashMap<>();
	
	/**
	 * <p>初始化人物创建工具, 加载所有的 module 的初始化 json 文件
	 * <p>所有的相关文件在文件夹 [dir] 中的文件的一级目录中
	 * (满足 [dir]/?/?.json), 且 json 文件包含的说明有:
	 * <li>name : (string, 必需) 说明人物名称
	 * <li>class : (string, 必需) 说明人物创建的全类名
	 * <li>modules : (object{string : string}, 不必需)
	 *     如果模块需要按照某些模板来创建的话, 则将 key=模块属性 (比如 "sprite", "control" 等)
	 *     value=模块模板名称 放入 modules 中.
	 * </li>
	 * @param dir
	 *   搜索的路径
	 */
	
	public void rescan(Path path) {
		FileHandle f = Gdx.files.local(path.toString());
		
		if (!f.exists() || !f.isDirectory()) {
			throw new IllegalStateException(path + " 无法读取人物创建文件信息");
		}
		
		FileHandle[] children = f.list();
		for (int i = 0; i < children.length; i++) {
			FileHandle child = children[i];
			
			if (!child.isDirectory()) {
				continue;
			}
			
			FileHandle[] files = child.list();
			for (int j = 0; j < files.length; j++) {
				FileHandle fjson = files[j];
				
				if (fjson.isDirectory()) {
					continue;
				}
				
				String ext = fjson.extension();
				if (!"json".equals(ext.toLowerCase())) {
					continue;
				}
				
				try {
					JsonValue v = jreader.parse(fjson);
					
					CharacterDef def = new CharacterDef();
					def.name = v.getString("name");
					def.className = v.getString("class");
					def.path = fjson.path();
					def.data = v.toJson(OutputType.minimal);
					
					if (v.has("modules")) {
						JsonValue modules = v.get("modules");
						
						for (JsonValue pair : modules) {
							def.map.put(pair.name, pair.asString());
						}
					}
					
					defs.put(def.name, def);
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean has(String name) {
		return defs.containsKey(name);
	}
	
	/**
	 * <p>创建人物类实例
	 * <p>创建的步骤如下:
	 * <li>按照 json 配置的 class 生成人物类实例 (如果类不是 CharacterEntry 的派生类则不会被生成)
	 * <li>按照 json 配置的 modules 说明逐个创建模块并加入到人物实例中
	 * <li>调用人物类实例的 init 方法, 将 json 文件作为参数传入
	 * </li>
	 * </p>
	 * @param name
	 *   人物名称
	 * @param customData
	 *   用户数据. 可以为 null
	 * @return 
	 * @throws NullPointerException
	 *   当 name 对应的人物数据不存在时
	 */
	public CharacterEntry create(String name, JsonValue customData) {
		CharacterDef def = defs.get(name);
		
		if (def == null) {
			throw new NullPointerException(String.format("不存在 %s 的人物数据", name));
		}
		
		Class<?> c;
		try {
			c = Class.forName(def.className);
			if (!CharacterEntry.class.isAssignableFrom(c)) {
				defs.remove(name);
				throw new IllegalArgumentException(String.format("%s 的人物数据中, 初始化类名错误", name));
			}
			
			@SuppressWarnings("unchecked")
			Class<? extends CharacterEntry> cc = (Class<? extends CharacterEntry>) c;
			CharacterEntry entry = cc.newInstance();
			
			// 合并 json
			JsonValue data = mergeJson(jreader.parse(def.data), customData);
			
			// 添加 modules
			// TODO
			
			entry.init(Gdx.files.local(def.path), data);
			return entry;
			
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			defs.remove(name);
			throw new IllegalArgumentException(String.format("%s 的人物数据中, 初始化类创建失败", name), e);
		}
	}
	
	/**
	 * 合并 json 数据. 现阶段只合并 json 类型为 object 的数据
	 * @param src
	 * @param dest
	 */
	private JsonValue mergeJson(JsonValue src, JsonValue dest) {
		if (dest == null) {
			return src;
		}
		if (src == null) {
			return getJsonClone(dest);
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
				return getJsonClone(dest);
			default:
				return src;
			}
		case array:
			if (dest.type() == type) {
				return getJsonClone(dest);
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
	
	public JsonValue getJsonClone(JsonValue src) {
		return jreader.parse(src.toJson(OutputType.minimal));
	}

}
