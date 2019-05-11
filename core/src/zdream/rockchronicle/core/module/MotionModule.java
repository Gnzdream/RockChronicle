package zdream.rockchronicle.core.module;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 动作与碰撞检测模块. 暂时只处理洛克人的
 * @author Zdream
 */
public abstract class MotionModule extends AbstractModule {
	
	public static final String NAME = "Motion";
	protected JsonCollector motionc;
	
	/*
	 * 定义的运动:
	 * 
	 * normal / stand  站立
	 * turn left  朝向左
	 * turn right  朝向右
	 * move left
	 * move right
	 */
	
	/**
	 * 朝向,
	 * true: 右
	 * false: 左
	 */
	public boolean orientation = true;
	
	protected LevelWorld world;
	

	public MotionModule(CharacterEntry ch) {
		super(ch);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);

		initMotion(value.get("motion"));
		addCollector(motionc = new JsonCollector(this::getMotionJson, "motion"));
	}

	private void initMotion(JsonValue object) {
		if (object == null) {
			orientation = true;
			return;
		}
		orientation = object.getBoolean("orientation", true);
	}

	public final void doCreateBody(LevelWorld world) {
		this.world = world;
		this.createBody();
	}
	
	public final void doDestroyBody() {
		this.destroyBody();
	}
	
	/**
	 * 每一帧来刷新一下状态
	 * @param world
	 *   关卡世界实体
	 * @param index
	 *   本帧的第几次调用. 第一次为 0
	 * @param hasNext
	 *   本帧是否还会再调用
	 */
	public void resetPosition(LevelWorld world, int index, boolean hasNext) {
		// do nothing
	}

	protected abstract void createBody();
	protected abstract void destroyBody();
	
	@Override
	public int priority() {
		return 0x100;
	}

	/* **********
	 * 资源事件 *
	 ********** */
	/*
	 * 允许获取与修改:
	 * motion.orientation
	 */
	public JsonValue getMotionJson() {
		JsonValue v = new JsonValue(ValueType.object);
		v.addChild("orientation", new JsonValue(orientation));
		return v;
	}
}
