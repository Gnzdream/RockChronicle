package zdream.rockchronicle.core.character;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.module.ModuleDef;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.sprite.foes.base.BaseFoe;
import zdream.rockchronicle.utils.JsonUtils;

/**
 * <p>人物的 {@link CharacterEntry} 的默认创建工具.
 * <p>利用 Json 数据构造 CharacterEntry 以及其拥有的 Module
 * </p>
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-05 (create)
 *   2019-05-07 (last modified)
 */
public class CharacterBuilder {
	
	final JsonReader jreader = JsonUtils.jreader;
	private int idCount = 1;

	public void init() {
		loadModuleDefs();
		rescan(Paths.get("res", "characters"));
		
		Gdx.app.log("CharacterBuilder", String.format("扫描角色共 %d 个", defs.size));
	}
	
	/**
	 * 角色名 - 角色预定义的初始化数据封装类
	 */
	private final ObjectMap<String, CharacterDef> defs = new ObjectMap<>();
	
	/**
	 * 模块种类 - 该种类的模块列表
	 */
	private final ObjectMap<String, Array<ModuleDef>> mdefs = new ObjectMap<>(16);
	
	private void loadModuleDefs() {
		Path path = Paths.get("res", "conf", "modules.json");
		FileHandle f = Gdx.files.local(path.toString());
		
		if (!f.exists()) {
			throw new IllegalStateException(path + " 无法读取模块定义文件信息");
		}
		
		JsonValue v = jreader.parse(f);
		for (JsonValue entry = v.child; entry != null; entry = entry.next) {
			String species = entry.name;
			
			Array<ModuleDef> defs = new Array<>();
			// entry 是一个 array
			for (JsonValue item = entry.child; item != null; item = item.next) {
				ModuleDef def = new ModuleDef();
				def.name = item.getString("name");
				def.className = item.getString("class");
				def.species = species;
				defs.add(def);
			}
			mdefs.put(species, defs);
		}
	}
	
	/**
	 * <p>初始化人物创建工具, 加载所有的角色的初始化 json 文件
	 * <p>所有的相关文件在文件夹 [dir] 中的文件的一级目录中
	 * (满足 [dir]/?/?.json), 且 json 文件包含的说明有:
	 * <li>name : (string, 必需) 说明角色名称
	 * <li>type : (string, 必需) 说明角色类型
	 * <li>class : (string, 非必需) 说明角色创建的全类名
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
			throw new IllegalStateException(path + " 无法读取角色创建文件信息");
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
					def.type = v.getString("type");
					if (v.has("class")) {
						def.className = v.getString("class");
					} else {
						switch (def.type) {
						case "foe": case "bullet":
							def.className = BaseFoe.class.getName();
							break;
						default:
							throw new IllegalArgumentException(
									"未指定怪物" + def.name + "的全类名");
						}
					}
					
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
	public CharacterEntry create(String name, JsonValue customData, LevelWorld world) {
		CharacterDef def = defs.get(name);
		
		if (def == null) {
			throw new NullPointerException(String.format("不存在 %s 的角色数据", name));
		}
		
		Class<?> c;
		try {
			c = Class.forName(def.className);
			if (!CharacterEntry.class.isAssignableFrom(c)) {
				defs.remove(name);
				throw new IllegalStateException(String.format("%s 的角色数据中, 初始化类名错误", name));
			}
			
			@SuppressWarnings("unchecked")
			Class<? extends CharacterEntry> cc = (Class<? extends CharacterEntry>) c;
			Constructor<? extends CharacterEntry> constractors = cc.getConstructor(int.class, String.class);
			
			CharacterEntry entry = constractors.newInstance(idCount++, name);
			entry.type = def.type;
			
			// 合并 json
			JsonValue data = JsonUtils.mergeJson(jreader.parse(def.data), customData);
			
			// 添加 modules
			JsonValue modules = data.get("modules");
			if (modules != null) {
				for (JsonValue item = modules.child; item != null; item = item.next) {
					if (item.isNull()) {
						continue;
					}
					
					if (item.isArray()) {
						for (JsonValue item0 = item.child; item0 != null; item0 = item0.next) {
							AbstractModule m = createModule(entry, item.name, item.asString());
							if (m != null) {
								entry.addModule(m);
							}
						}
						continue;
					}
					
					AbstractModule m = createModule(entry, item.name, item.asString());
					if (m != null) {
						entry.addModule(m);
					}
				}
			}
			
			entry.world = world;
			entry.init(Gdx.files.local(def.path), data);
			Objects.requireNonNull(entry.getBoxModule(), "角色数据的行动模块为 null");
			Objects.requireNonNull(entry.type, "角色数据的类型为 null");
			
			return entry;
		} catch (Exception e) {
			defs.remove(name);
			throw new IllegalStateException(String.format("%s 的角色数据中, 初始化类创建失败", name), e);
		}
	}
	
	private AbstractModule createModule(CharacterEntry parent, String species, String name) {
		Array<ModuleDef> array = this.mdefs.get(species);
		if (array == null) {
			return null;
		}
		
		for (int i = 0; i < array.size; i++) {
			ModuleDef def = array.get(i);
			if (name.equals(def.name)) {
				try {
					return createModule(parent, def);
				} catch (Exception e) {
					throw new IllegalStateException(
							String.format("%s 的角色中的 %s 模块初始化错误", parent, def), e);
				}
			}
		}
		
		return null;
	}
	
	public AbstractModule createModule(CharacterEntry parent,
			String species,
			String name,
			JsonValue param) {
		AbstractModule m = createModule(parent, species, name);
		m.init(null, param);
		
		return m;
	}
	
	private AbstractModule createModule(CharacterEntry parent, ModuleDef def)
			throws Exception {
		Class<?> c = Class.forName(def.className);
		if (!AbstractModule.class.isAssignableFrom(c)) {
			throw new IllegalStateException(String.format("%s 的角色中的 %s 模块, 初始化类名错误", parent, def));
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends AbstractModule> cc = (Class<? extends AbstractModule>) c;
		
		Constructor<? extends AbstractModule> constractors = cc.getConstructor(CharacterEntry.class);
		AbstractModule m = constractors.newInstance(parent);
		
		return m;
	}
	
}
