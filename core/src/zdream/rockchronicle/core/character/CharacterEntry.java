package zdream.rockchronicle.core.character;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.module.MotionModule;
import zdream.rockchronicle.core.module.sprite.SpriteModule;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 角色
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (modify)
 */
public abstract class CharacterEntry {
	
	/**
	 * 是否存在. 如果该角色已被删除, 该值设为 false
	 */
	private boolean exists = true;
	/**
	 * 是否完成初始化
	 */
	private boolean inited = false;
	
	public final int id;
	public final String name;
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
	protected void init(FileHandle file, JsonValue value) {
		if (inited) {
			return;
		}
		
		this.type = value.getString("type");
		sortModules();
		
		AbstractModule[] ms = modules.toArray(AbstractModule.class);
		for (int i = 0; i < ms.length; i++) {
			modules.get(i).init(file, value);
		}
		
		inited = true;
	}
	
	/**
	 * 向世界里放置该角色的碰撞单位
	 * @return
	 */
	public void createBody(LevelWorld world) {
		getMotion().doCreateBody(world);
	}
	
	/**
	 * 向世界里删除该角色的碰撞单位.
	 * 注意, 不是删除角色, 所以 exists 不一定变化
	 * @return
	 */
	public void destroyBody() {
		getMotion().doDestroyBody();
	}
	
	/**
	 * 自行销毁, 等待 GameRuntime 回收
	 */
	public void willDestroy() {
		RockChronicle.INSTANCE.runtime.removeEntry(this);
		moduleMap.forEach((n, m) -> {
			try {
				m.willDestroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		destroyBody();
		exists = false;
	}
	
	/**
	 * 回收. 该方法由系统 (GameRuntime) 调用, 自己不能调用
	 */
	public void dispose() {
		moduleMap.forEach((n, m) -> {
			try {
				m.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/* **********
	 * 模块管理 *
	 ********** */
	/*
	 * 各种模块的管理器. 有:
	 * 碰撞模块, 处理 2D 碰撞的;
	 * 动作模块, 判断它在走路、攻击, 并且持续了多久了. 这些数据将更新 texture (使每一帧的样子都可能不同)
	 * 能力模块, 储存 AT、HP、DF 以及 BUFF、技能 等这些数据
	 * 控制模块, 让 AI 或者用户的键盘或手柄控制它
	 * 等等
	 */
	protected final HashMap<String, AbstractModule> moduleMap = new HashMap<>();
	private Array<AbstractModule> modules;
	
	public void addModule(AbstractModule module) {
		moduleMap.put(module.name(), module);
		if (inited) {
			sortModules();
		}
	}
	
	/**
	 * 删除模块. 请尽量在 onStepFinished 方法中执行删除, 而不要在 determined 方法执行
	 * @param module
	 */
	public void removeModule(AbstractModule module) {
		moduleMap.remove(module.name());
		
		// 清理工作
		this.removeSubscribe(module);
		this.unbindResource(module);
		
		if (inited) {
			sortModules();
		}
	}
	
	public AbstractModule getModule(String name) {
		return moduleMap.get(name);
	}
	
	private void sortModules() {
		modules = new Array<>(moduleMap.size());
		moduleMap.values().forEach(m -> modules.add(m));
		
		modules.sort((a, b) -> {
			return b.priority() - a.priority();
		});
	}
	
	/**
	 * <p>获得行动模块.
	 * <p>规定任何一个角色都必须含有行动模块
	 * </p>
	 * @return
	 */
	public MotionModule getMotion() {
		return ((MotionModule) getModule(MotionModule.NAME));
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
		AbstractModule[] ms = modules.toArray(AbstractModule.class);
		for (int i = 0; i < ms.length; i++) {
			ms[i].determine(world, index, hasNext);
			
			// 事件
			while (events.size != 0) {
				CharacterEvent event = events.removeIndex(0);
				Array<AbstractModule> array = this.subscribes.get(event.name);
				if (array == null || array.size == 0) {
					continue;
				}
				
				AbstractModule[] subscribes = array.toArray(AbstractModule.class);
				for (int j = 0; j < subscribes.length; j++) {
					subscribes[j].receiveEvent(event);
				}
			}
		}
		
		for (int i = 0; i < ms.length; i++) {
			ms[i].stepPassed();
		}
	}
	
	/**
	 * 行动. 只调用行动模块
	 * @param world
	 *   世界实例
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void step(LevelWorld world, int index, boolean hasNext) {
		getMotion().resetPosition(world, index, hasNext);
	}

	public void onStepFinished(LevelWorld world, boolean isPause) {
		AbstractModule[] ms = modules.toArray(AbstractModule.class);
		for (int i = 0; i < ms.length; i++) {
			ms[i].onStepFinished(world, isPause);
		}
	}
	
	/**
	 * 绘画. 在屏幕上画出这个人物.
	 * 绘画的位置是由碰撞块的位置和纹理的属性 (纹理的锚点位置与碰撞块的有一定的差值) 来决定的
	 */
	public void draw(SpriteBatch batch, OrthographicCamera camera) {
		AbstractModule m = getModule(SpriteModule.NAME);
		if (m != null) {
			((SpriteModule) m).draw(batch, camera);
		}
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
	private HashMap<String, AbstractModule> resources = new HashMap<>();
	/*
	 * 模拟 RSS 订阅
	 */
	private HashMap<String, Array<AbstractModule>> subscribes = new HashMap<>();
	/*
	 * 所有的事件都是异步的. 等待中的事件列表
	 */
	private Array<CharacterEvent> events = new Array<>();
	
	/**
	 * 绑定模块与资源的一级子数据集
	 * @param key
	 * @param module
	 * @throws IllegalArgumentException
	 *   当 key 已经被绑定时
	 */
	public void bindResource(String key, AbstractModule module) {
		if (resources.containsKey(key)) {
			throw new IllegalArgumentException(
					String.format("角色 %s 的资源键 %s 已经被模块 %s 绑定, 无法与 %s 绑定",
					this, key, resources.get(key), module));
		}
		resources.put(key, module);
	}
	
	/**
	 * 解绑模块与资源的一级子数据集
	 * @param key
	 */
	public void unbindResource(String key) {
		resources.remove(key);
	}
	
	/**
	 * 解绑某个模块的所有的资源
	 * @param module
	 */
	public void unbindResource(AbstractModule module) {
		for (Iterator<Entry<String, AbstractModule>> it = resources.entrySet().iterator(); it.hasNext();){
			Entry<String, AbstractModule> item = it.next();
		    if (item.getValue() == module)
		    	it.remove();
		}
	}
	
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
		for (Iterator<Entry<String, Array<AbstractModule>>> it = subscribes.entrySet().iterator(); it.hasNext();){
			Entry<String, Array<AbstractModule>> item = it.next();
			Array<AbstractModule> array = item.getValue();
			
			array.removeValue(module, true);
			if (array.size == 0) {
				it.remove();
			}
		}
	}
	
	public int getInt(String[] path, int defValue) {
		AbstractModule module = resources.get(path[0]);
		return (module != null) ? module.getInt(path, defValue) : defValue;
	}
	public String getString(String[] path, String defValue) {
		AbstractModule module = resources.get(path[0]);
		return (module != null) ? module.getString(path, defValue) : defValue;
	}
	public float getFloat(String[] path, float defValue) {
		AbstractModule module = resources.get(path[0]);
		return (module != null) ? module.getFloat(path, defValue) : defValue;
	}
	public boolean getBoolean(String[] path, boolean defValue) {
		AbstractModule module = resources.get(path[0]);
		return (module != null) ? module.getBoolean(path, defValue) : defValue;
	}
	public JsonValue getJson(String[] path) {
		AbstractModule module = resources.get(path[0]);
		return (module != null) ? module.getJson(path) : null;
	}
	
	/*
	 * 返回值: 是否修改被允许 (accepted)
	 */
	public boolean setJson(String first, JsonValue value) {
		AbstractModule module = resources.get(first);
		return (module != null) ? module.setJson0(first, value) : false;
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

}
