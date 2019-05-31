package zdream.rockchronicle.core.module.motion;

import static zdream.rockchronicle.platform.world.LevelWorld.TIME_STEP;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.move.ParabolaMovement;

/**
 * <p>二次 / 抛物线移动的角色的行动模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-15 (created)
 *   2019-05-15 (last modified)
 */
public class ParabolaMotionModule extends MotionModule {
	/**
	 * 启动时的横纵坐标的速度 (格 / 步). 配置项
	 */
	float vx, vy;
	/**
	 * 横纵坐标的加速度 (每步的速度变化量). 配置项
	 */
	float ax, ay;
	
	ParabolaMovement movement;

	public ParabolaMotionModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue omotion = value.get("motion");
		
		// 初始速度
		JsonValue ovel = omotion.get("velocity");
		if (ovel != null) {
			vx = ovel.getFloat("x", 0f) * TIME_STEP;
			vy = ovel.getFloat("y", 0f) * TIME_STEP;
		}
		
		movement = new ParabolaMovement();
		movement.vx = vx;
		movement.vy = vy;
		
		JsonValue oacc = omotion.get("acceleration");
		if (oacc != null) {
			ax = ovel.getFloat("x", 0f) * TIME_STEP * TIME_STEP;
			ay = ovel.getFloat("y", 0f) * TIME_STEP * TIME_STEP;
		}
		
		movement.ax = ax;
		movement.ay = ay;
		
		parent.getBoxModule().addMovable(movement, 0);
	}

}
