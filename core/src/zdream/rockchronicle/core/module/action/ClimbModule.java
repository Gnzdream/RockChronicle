package zdream.rockchronicle.core.module.action;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.world.LevelWorld;

import static zdream.rockchronicle.platform.world.LevelWorld.*;

/**
 * <p>攀爬模块, 指攀爬梯子
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-20 (created)
 *   2019-05-20 (last modified)
 */
public class ClimbModule extends AbstractModule {

	protected JsonCollector climbc, climbpc;
	protected JsonValue param;
	
	/**
	 * 攀爬速度, 每步攀爬的格子数. 配置项
	 */
	float climbVelocity;
	
	/*
	 * 以下参数不在 JsonCollector 中
	 */
	/**
	 * 0: 无, 1: 上, 2: 下,
	 * 每步结束时重置为 0
	 */
	byte upOrDown;
	
	public ClimbModule(CharacterEntry parent) {
		super(parent);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue v = value.get("climbParam");
		climbVelocity = v.getFloat("velocity") * TIME_STEP;
		
		parent.addSubscribe("ctrl_axis", this);
		addCollector(climbc = new JsonCollector(this::createClimbJson, "climb"));
		addCollector(climbpc = new JsonCollector(() -> param, "climbParam"));
	}

	@Override
	public String name() {
		return "Climb";
	}
	
	@Override
	public int priority() {
		return 0x81;
	}
	
	private JsonValue createClimbJson() {
		JsonValue v = new JsonValue(ValueType.object);
		return v;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		// 判断逻辑:
		// 这里需要判断角色盒子的中心点, 是否映射到世界中, 是梯子的地形块.
		// 如果不是, 结束判断;
		
		// 角色将改变形状 (共 3 个形状, 存储在 StateModule 中的 motion 字段)、
		// 姿势 (爬梯子状态, Box 里面的参数)
		
		// 角色如果原本不在爬梯子状态, 需要对角色的位置进行调整,
		// 平移到梯子上;
		
		// 将根据角色与梯子顶端的距离来设置爬梯子状态;
		
		// 角色是不能够攀爬房间区域以外的梯子的. 否则切换房间的判定将出现问题
		
	}
	
	@Override
	public void stepPassed() {
		super.stepPassed();
		upOrDown = 0;
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		switch (event.name) {
		case "ctrl_axis":
			recvCtrlMotion(event);
			break;

		default:
			super.receiveEvent(event);
			break;
		}
	}
	
	private void recvCtrlMotion(CharacterEvent event) {
		boolean up = event.value.getBoolean("up"),
				down = event.value.getBoolean("down");
		
		if (up) {
			upOrDown = 1;
		} else if (down) {
			upOrDown = 2;
		}
	}

}
