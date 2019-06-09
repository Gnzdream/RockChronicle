package zdream.rockchronicle.core.module.motion;

import static zdream.rockchronicle.platform.world.LevelWorld.TIME_STEP;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.move.LinearMovement;

/**
 * <p>线性移动的角色的行动模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-15 (created)
 *   2019-05-15 (last modified)
 */
public class LinearMotionModule extends MotionModule {
	
	/**
	 * 横坐标和纵坐标方向是否调转. 配置项
	 */
	boolean flipX, flipY;
	/**
	 * 启动时的横纵坐标的速度 (格 / 步). 配置项
	 */
	float vx, vy;
	
	LinearMovement movement;

	public LinearMotionModule(CharacterEntry ch) {
		super(ch, "linear");
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue omotion = value.get("motion");
		if (omotion != null) {
			flipX = omotion.getBoolean("flipX", false);
			flipY = omotion.getBoolean("flipY", false);
		}
		
		// 初始速度
		JsonValue ovel = omotion.get("velocity");
		if (ovel != null) {
			vx = ovel.getFloat("x", 0f) * TIME_STEP;
			vy = ovel.getFloat("y", 0f) * TIME_STEP;
		}
		
		movement = new LinearMovement();
		movement.vx = flipX ? -vx : vx;
		movement.vy = flipY ? -vy : vy;
		
		parent.getBoxModule().addMovable(movement, 0);
	}
}
