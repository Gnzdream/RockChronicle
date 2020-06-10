package zdream.rockchronicle.core.module;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 抽象模块父类
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-08 (last modified)
 */
public abstract class AbstractModule {
	
	public final String name;
	public final String description;
	
	public final CharacterEntry parent;
	
	/**
	 * 如果父类发现该模块需要删除, 将其设置为 true
	 */
	protected boolean willDelete;
	
	public AbstractModule(CharacterEntry parent, String name, String description) {
		this.parent = parent;
		this.name = name;
		this.description = description;
	}
	
	public void init(FileHandle file, JsonValue value) {
		// do nothing
	}
	
	/**
	 * 每一步来刷新一下状态
	 * @param world
	 *   关卡世界实体
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void determine(LevelWorld world, int index, boolean hasNext) {
		// do nothing
	}
	
	/**
	 * <p>用于每一步结束时的工作，包括重置临时参数、删除模块.
	 * <p>子类需要扩展的, 在方法的最后添加 super.stepPassed()
	 * </p>
	 */
	public void stepPassed() {
		if (willDelete) {
			willDestroy();
			parent.removeModule(this);
		}
	}

	/**
	 * 子类如果要继承, 请在方法最后加上 super.onStepFinished()
	 * @param world
	 */
	public void stepPaused(LevelWorld world) {
		
	}

	public void willDestroy() {
		
	}
	
	public void dispose() {
		// parent.unbindResource(this);
	}
	
	/**
	 * <p>优先级
	 * <p>优先级高的模块的 {@link #determine(LevelWorld, int, boolean)} 方法将先执行.
	 * </p>
	 * @return
	 */
	public int priority() {
		return 0;
	}
	
	/* **********
	 * 资源事件 *
	 ********** */
	
	public int getInt(String key, int defValue) {
		return parent.getInt(key, defValue);
	}

	public String getString(String key, String defValue) {
		return parent.getString(key, defValue);
	}

	public float getFloat(String key, float defValue) {
		return parent.getFloat(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return parent.getBoolean(key, defValue);
	}

	public JsonValue getJson(String key) {
		return parent.getJson(key);
	}

	public void setState(String key, JsonValue value) {
		parent.setState(key, value);
	}

	public void setSituation(String key, JsonValue value) {
		parent.setSituation(key, value);
	}

	public void removeState(String key) {
		parent.removeState(key);
	}

	public void removeSituation(String key) {
		parent.removeSituation(key);
	}

	public void receiveEvent(CharacterEvent event) {
		
	}
	
	/* **********
	 *   其它   *
	 ********** */
	
	@Override
	public String toString() {
		if (description != null) {
			return String.format("{M:%s-%s}", name, description);
		}
		return String.format("{M:%s}", name);
	}

}
