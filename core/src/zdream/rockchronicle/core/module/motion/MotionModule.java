package zdream.rockchronicle.core.module.motion;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;

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
	
	public MotionModule(CharacterEntry ch) {
		super(ch);
		
		motionc = new JsonCollector(this::getMotionJson, "motion");
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);

		initMotion(value.get("motion"));
		addCollector(motionc);
	}

	protected void initMotion(JsonValue object) {
		
	}

	@Override
	public int priority() {
		return 256;
	}

	/* **********
	 * 资源事件 *
	 ********** */
	public JsonValue getMotionJson() {
		JsonValue v = new JsonValue(ValueType.object);
		return v;
	}
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		return parent.getBoxModule().getBox();
	}
}
