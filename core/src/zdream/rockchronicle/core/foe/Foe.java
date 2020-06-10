package zdream.rockchronicle.core.foe;

import java.util.Objects;
import java.util.function.Consumer;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import zdream.rockchronicle.core.GameRuntime;

/**
 * <p>角色. 
 * <p>原类名为: CharacterEntry
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-09 (last modified)
 */
public abstract class Foe {
	
	protected GameRuntime runtime;
	/**
	 * 是否存在. 如果该角色已被删除, 该值设为 true
	 */
	private boolean disposed = false;
	
	public final int id;
	public final String name;
	
	private static int ID = 1;
	
	/*
	 * 角色类型. 包括但不限于:
	 * 
	 * <li>重要角色 "leader": 含玩家控制角色、重要官邸 BOSS 以及有分量的 NPC
	 * <li>小怪 "foe": 含敌方小怪以及我方、敌方、中立方等召唤的喽啰
	 * <li>子弹 "bullet": 含小怪以及重要角色释放的具有攻击或触发效果的物体角色
	 * <li>陷阱 "trap": 除了静态地形以外的机关等
	 * <li>道具 "mass": 含掉落物、购买商品以及其它可拾取物品,
	 *         怪物或角色死后的收尾画面及图像
	 * <li>场 "field": 虚拟区域, 当目标角色接触后则激活的虚拟角色
	 * <li>贴图 "texture": 也包括剑气等传统意义的近战武器释放的物体, 以及其它没有实际意义的物体
	 * </li>
	 */
	public Foe(String name) {
		synchronized (Foe.class) {
			this.id = ID ++;
		}
		this.name = name;
	}
	
	public boolean isDisposed() {
		return disposed;
	}
	
	public void dispose() {
		// TODO Auto-generated method stub
		disposed = true;
	}
	
	public void init(GameRuntime runtime) {
		this.runtime = runtime;
		Box[] boxes = getBoxes();
		for (int i = 0; i < boxes.length; i++) {
			putBoxOnWorld(boxes[i]);
		}
	}
	
	@Override
	public String toString() {
		return String.format("Foe: %s", name);
	}

	/* **********
	 * 事务执行 *
	 ********** */
	
	/*
	 * 所有的角色实例的数据均能转变成 Json 数据格式.
	 * 
	 * 每个模块在启动时绑定一个或多个一级数据源, 它等同于该 Json 数据的一级子数据集
	 * 所有对相应的资源数据的查询、设置数据都将指向该模块.
	 */
//	private HashMap<String, AbstractModule> resources = new HashMap<>();
	/*
	 * 模拟 RSS 订阅
	 */
	private ObjectMap<String, Array<Consumer<FoeEvent>>> subscribes = new ObjectMap<>();
	/*
	 * 所有的事件都是异步的. 等待中的事件列表
	 */
	private Array<FoeEvent> events = new Array<>();
	
	/**
	 * <p>该角色保存的长期性状态数据
	 * <p>这类数据将从放入 Map 中将一直存在, 直到角色销毁或其它模块将其删除.
	 * <p>这里推荐模块从生成之后, 将其它模块或环境、角色需要查询的参数放在这里,
	 * 销毁时手动清除存放的数据; 这里的 key 推荐采用 'state.stiffness' 这样,
	 * 以点作为分割点整理层级关系.
	 * </p>
	 */
	private final ObjectMap<String, JsonValue> props = new ObjectMap<>();
	
	/**
	 * <p>该角色保存的临时状态数据
	 * <p>这类数据将从放入 Map 中后只存在一步(或指定寿命)的时间.
	 * 寿命存在 JsonValue 中, 获取方法: value.getInt("_remain")
	 * </p>
	 */
	private final ObjectMap<String, JsonValue> states = new ObjectMap<>();
	
	/**
	 * 行动. 只调用盒子模块
	 * @param pause
	 *   世界是否暂停
	 */
	public void step(boolean pause) {
		// 所有状态数值寿命减一
		for (Entries<String, JsonValue> it = states.iterator(); it.hasNext;) {
			Entry<String, JsonValue> a = it.next();
			
			JsonValue value = a.value;
			JsonValue remain = value.get("_remain");
			if (remain == null || remain.asInt() <= 1) {
				it.remove();
			} else {
				remain.set(remain.asInt() - 1, null);
			}
		}
	}
	
	/**
	 * 状态确定.
	 * 刚创建的 Foe 第一步时间里不会调用 step 方法, 直接调用 submit.
	 * @param pause
	 *   世界是否暂停
	 */
	public void submit(boolean pause) {
		
	}
	
	/**
	 * 添加订阅
	 * @param event
	 * @param module
	 */
	public void addSubscribe(String event, Consumer<FoeEvent> consumer) {
		Array<Consumer<FoeEvent>> array = subscribes.get(event);
		if (array == null) {
			array = new Array<>();
			subscribes.put(event, array);
		}
		if (!array.contains(consumer, true)) {
			array.add(consumer);
		}
	}
	
	/**
	 * 删除订阅
	 * @param event
	 * @param module
	 */
	public void removeSubscribe(String event, Consumer<FoeEvent> consumer) {
		Array<Consumer<FoeEvent>> array = subscribes.get(event);
		if (array == null) {
			return;
		}
		array.removeValue(consumer, true);
	}
	
	public int getInt(String key, int defValue) {
		JsonValue json = getJson(key);
		return (json != null) ? json.asInt() : defValue;
	}
	public String getString(String key, String defValue) {
		JsonValue json = getJson(key);
		return (json != null) ? json.asString() : defValue;
	}
	public float getFloat(String key, float defValue) {
		JsonValue json = getJson(key);
		return (json != null) ? json.asFloat() : defValue;
	}
	public boolean getBoolean(String key, boolean defValue) {
		JsonValue json = getJson(key);
		return (json != null) ? json.asBoolean() : defValue;
	}
	public JsonValue getJson(String key) {
		JsonValue v = states.get(key);
		
		if (v == null) {
			v = props.get(key);
		}
		return v;
	}
	
	/**
	 * 设置或替换临时数据
	 */
	public void setState(String key, JsonValue value) {
		Objects.requireNonNull(value);
		states.put(key, value);
	}
	
	/**
	 * 设置或替换临时数据, 附上寿命
	 */
	public void setState(String key, JsonValue value, int remain) {
		Objects.requireNonNull(value);
		value.addChild("_remain", new JsonValue(remain));
		states.put(key, value);
	}

	/**
	 * 设置或替换值, 永久数据
	 * @throws NullPointerException
	 *   当 value == null 时
	 */
	public void setProp(String key, JsonValue value) {
		Objects.requireNonNull(value);
		props.put(key, value);	
	}
	
	public void removeState(String key) {
		states.remove(key);
	}
	
	public void removeSituation(String key) {
		props.remove(key);
	}
	
	/**
	 * 发布事件, 异步执行
	 */
	public void publish(FoeEvent event) {
		this.events.add(event);
	}
	
	/* **********
	 * 角色补充 *
	 ********** */
	
	/**
	 * 产生一个新的角色, 并交给 runtime 管理.
	 * 但是该角色不会马上添加进世界中.
	 * @param name
	 * @param param
	 * @return
	 */
	public Foe createFoe(String name, JsonValue param) {
//		CharacterEntry entry = RockChronicle.INSTANCE.runtime.characterBuilder
//				.create(name, param);
//		entry.setSituation("parent", new JsonValue(id));
//		RockChronicle.INSTANCE.runtime.addEntry(entry);
//		return entry;
		return null;
	}
	
	public Foe createFoe(Foe foe) {
		// TODO 向世界添加
		return foe;
	}
	
	/**
	 * 查询角色
	 * @param id
	 * @return
	 */
	public Foe findEntry(GameRuntime world, int id) {
//		CharacterEntry entry = RockChronicle.INSTANCE.runtime.findEntry(id);
//		if (entry == null) {
//			entry = RockChronicle.INSTANCE.runtime.findEntryWaitingForAdd(id);
//		}
//		return entry;
		return null;
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	/**
	 * 返回这个角色的盒子.
	 * @return
	 *   角色盒子数组.
	 */
	public abstract Box[] getBoxes();
	
	public void putBoxOnWorld(Box box) {
		runtime.addBox(box);
	}
	public void removeBoxFromWorld(Box box) {
		runtime.removeBox(box);
	}
	
	/* **********
	 *   绘画   *
	 ********** */

	public void putPainter(IFoePainter painter) {
		runtime.addPainter(painter);
	}
	
	public void removePainter(IFoePainter painter) {
		runtime.removePainter(painter);
	}
	

}
