package zdream.rockchronicle.core.character;

import java.util.Iterator;
import java.util.Objects;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 角色
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-09 (last modified)
 */
public abstract class CharacterEntry {
	
	/**
	 * 是否存在. 如果该角色已被删除, 该值设为 false
	 */
	private boolean exists = true;
	
	public final int id;
	public final String name;
	public LevelWorld world;
	/**
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
	public String type;
	
	public CharacterEntry(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("Character#%d: %s", id, name);
	}
	
	/* **********
	 * 生命周期 *
	 ********** */
	/*
	 * 角色删除的步骤:
	 * 1. 自己或者其它角色调用 willDestroy 方法进行销毁
	 *    销毁的结果是:
	 *    a) 自己加入 GameRuntime 的销毁列表
	 *    b) 依次调用所有模块的 willDestroy 方法
	 *    c) 从 LevelWorld 中删除碰撞方块 (如果有). 这一步在行动模块调用 willDestroy 之后
	 *    d) 存在参数 exists 设为 false
	 * 2. 由 GameRuntime 调用 dispose 方法. 该方法只能由 GameRuntime 调用.
	 *    a) 依次调用所有模块的 dispose 方法
	 */
	
	/**
	 * @see #exists
	 */
	public boolean isExists() {
		return exists;
	}
	
	/**
	 * 所有的角色在该方法中执行所有模块的 init 方法
	 */
	protected void init(JsonValue value) {
		this.type = value.getString("type");
	}

	/* **********
	 * 事务执行 *
	 ********** */
	
	/**
	 * 状态确定
	 * @param world
	 *   世界实例
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void determine(LevelWorld world, int index, boolean hasNext) {
//		if (modules == null) {
//			sortModules();
//		}
//		AbstractModule[] ms = modules;
		
		// 清理上次的数据
//		for (int i = 0; i < ms.length; i++) {
//			ms[i].stepPassed();
//		}
		recent.clear();
		recent.putAll(states);
		states.clear();
		
		// 可能有删除的模块, 所以需要判断一下
//		if (modules == null) {
//			sortModules();
//			ms = modules;
//		}
//		
//		// 开始现在的操作流程
//		for (int i = 0; i < ms.length; i++) {
//			ms[i].determine(world, index, hasNext);
//			
//			// 事件
//			while (events.size != 0) {
//				CharacterEvent event = events.removeIndex(0);
//				Array<AbstractModule> array = this.subscribes.get(event.name);
//				if (array == null || array.size == 0) {
//					continue;
//				}
//				
//				AbstractModule[] subscribes = array.toArray(AbstractModule.class);
//				for (int j = 0; j < subscribes.length; j++) {
//					subscribes[j].receiveEvent(event);
//				}
//			}
//		}
	}
	
	/**
	 * 行动. 只调用盒子模块
	 * @param world
	 *   世界实例
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void step(LevelWorld world, int index, boolean hasNext) {
		// getBoxModule().action(world);
	}

	public void stepPaused(LevelWorld world) {
//		if (modules == null) sortModules();
//		
//		for (int i = 0; i < modules.length; i++) {
//			modules[i].stepPaused(world);
//		}
	}
	
	/**
	 * 绘画. 在屏幕上画出这个人物.
	 * 绘画的位置是由碰撞块的位置和纹理的属性 (纹理的锚点位置与碰撞块的有一定的差值) 来决定的
	 */
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
//		AbstractModule m = getModule(SpriteModule.NAME);
//		if (m != null) {
//			((SpriteModule) m).draw(batch, camera);
//		}
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
	public CharacterEntry createEntry(String name, JsonValue param) {
		CharacterEntry entry = RockChronicle.INSTANCE.runtime.characterBuilder
				.create(name, param);
		entry.setSituation("parent", new JsonValue(id));
		RockChronicle.INSTANCE.runtime.addEntry(entry);
		return entry;
	}
	
	/**
	 * 查询角色
	 * @param id
	 * @return
	 */
	public CharacterEntry findEntry(int id) {
		CharacterEntry entry = RockChronicle.INSTANCE.runtime.findEntry(id);
		if (entry == null) {
			entry = RockChronicle.INSTANCE.runtime.findEntryWaitingForAdd(id);
		}
		return entry;
	}
	
	/* **********
	 * 资源事件 *
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
	private ObjectMap<String, Array<AbstractModule>> subscribes = new ObjectMap<>();
	/*
	 * 所有的事件都是异步的. 等待中的事件列表
	 */
	private Array<CharacterEvent> events = new Array<>();
	
	/**
	 * <p>该角色保存的长期性状态数据
	 * <p>这类数据将从放入 Map 中将一直存在, 直到角色销毁或其它模块将其删除.
	 * <p>这里推荐模块从生成之后, 将其它模块或环境、角色需要查询的参数放在这里,
	 * 销毁时手动清除存放的数据; 这里的 key 推荐采用 'state.stiffness' 这样,
	 * 以点作为分割点整理层级关系.
	 * </p>
	 */
	private final ObjectMap<String, JsonValue> situations = new ObjectMap<>();
	
	/**
	 * <p>该角色保存的临时状态数据
	 * <p>这类数据将从放入 Map 中后只存在一步的时间, 每步结束后将自动清空.
	 * </p>
	 */
	private final ObjectMap<String, JsonValue> states = new ObjectMap<>();
	
	private final ObjectMap<String, JsonValue> recent = new ObjectMap<>();
	
	/**
	 * 添加订阅
	 * @param event
	 * @param module
	 */
	public void addSubscribe(String event, AbstractModule module) {
		Array<AbstractModule> array = subscribes.get(event);
		if (array == null) {
			array = new Array<>();
			subscribes.put(event, array);
		}
		if (!array.contains(module, true)) {
			array.add(module);
		}
	}
	
	/**
	 * 删除订阅
	 * @param event
	 * @param module
	 */
	public void removeSubscribe(String event, AbstractModule module) {
		Array<AbstractModule> array = subscribes.get(event);
		if (array == null) {
			return;
		}
		array.removeValue(module, true);
		if (array.size == 0) {
			subscribes.remove(event);
		}
	}
	
	/**
	 * 删除某个模块的所有的订阅
	 * @param module
	 */
	public void removeSubscribe(AbstractModule module) {
		for (Iterator<Entry<String, Array<AbstractModule>>> it = subscribes.iterator(); it.hasNext();){
			Entry<String, Array<AbstractModule>> item = it.next();
			Array<AbstractModule> array = item.value;
			
			array.removeValue(module, true);
			if (array.size == 0) {
				it.remove();
			}
		}
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
			v = situations.get(key);
		}
		return v;
	}
	
	/*
	 * 设置或替换值, 临时数据
	 */
	public void setState(String key, JsonValue value) {
		Objects.requireNonNull(value);
		states.put(key, value);
		recent.remove(key);
	}

	/**
	 * 设置或替换值, 永久数据
	 * @throws NullPointerException
	 *   当 value == null 时
	 */
	public void setSituation(String key, JsonValue value) {
		Objects.requireNonNull(value);
		situations.put(key, value);	
	}
	
	public void removeState(String key) {
		states.remove(key);
		recent.remove(key);
	}
	
	public void removeSituation(String key) {
		situations.remove(key);
	}
	
	/**
	 * 发布事件, 异步执行
	 */
	public void publish(CharacterEvent event) {
		this.events.add(event);
	}
	
	/**
	 * 立即发布事件
	 */
	public void publishNow(CharacterEvent event) {
		Array<AbstractModule> array = this.subscribes.get(event.name);
		if (array == null || array.size == 0) {
			return;
		}
		
		AbstractModule[] subscribes = array.toArray(AbstractModule.class);
		for (int j = 0; j < subscribes.length; j++) {
			subscribes[j].receiveEvent(event);
		}
	}

	/*
	 * 原先用 Modules 的方式写, 东西太复杂了, 现在越简单越好
	 */
	/* **********
	 *   盒子   *
	 ********** */
	
	/**
	 * 返回这个角色的盒子.
	 * @return
	 *   角色盒子数组.
	 */
	public abstract Box[] getBoxes();

}
