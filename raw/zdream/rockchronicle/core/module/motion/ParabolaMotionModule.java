package zdream.rockchronicle.core.module.motion;

import static zdream.rockchronicle.platform.world.LevelWorld.TIME_STEP;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.move.ParabolaMovement;
import zdream.rockchronicle.platform.body.Box;

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
	 * 启动时的横纵坐标的速度 (p / 步). 配置项
	 */
	int vx, vy;
	/**
	 * 横纵坐标的加速度 (每步的速度变化量). 配置项
	 */
	int ax, ay;
	/**
	 * 横坐标和纵坐标方向是否调转. 配置项
	 */
	boolean flipX, flipY;
	
	ParabolaMovement movement;

	public ParabolaMotionModule(CharacterEntry ch) {
		super(ch, "parabola");
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
			vx = (int) (ovel.getFloat("x", 0f) * TIME_STEP * Box.P_PER_BLOCK);
			vy = (int) (ovel.getFloat("y", 0f) * TIME_STEP * Box.P_PER_BLOCK);
		}
		
		movement = new ParabolaMovement();
		movement.vx = (flipX) ? -vx : vx;
		movement.vy = (flipY) ? -vy : vy;
		
		JsonValue oacc = omotion.get("acceleration");
		if (oacc != null) {
			ax = (int) (oacc.getFloat("x", 0f) * TIME_STEP * TIME_STEP * Box.P_PER_BLOCK);
			ay = (int) (oacc.getFloat("y", 0f) * TIME_STEP * TIME_STEP * Box.P_PER_BLOCK);
		}
		
		movement.ax = (flipX) ? -ax : ax;
		movement.ay = (flipY) ? -ay : ay;
		
		JsonValue orange = omotion.get("range");
		float scale = TIME_STEP * Box.P_PER_BLOCK;
		if (orange != null) {
			for (JsonValue entry = orange.child; entry != null; entry = entry.next) {
				switch (entry.name) {
				case "maxX":
					movement.maxX = (int) ((flipX) ? -entry.asFloat() * scale :
						entry.asFloat() * scale);
					break;
				case "minX":
					movement.minX = (int) ((flipX) ? -entry.asFloat() * scale :
						entry.asFloat() * scale);
					break;
				case "maxY":
					movement.maxY = (int) ((flipY) ? -entry.asFloat() * scale :
						entry.asFloat() * scale);
					break;
				case "minY":
					movement.minY = (int) ((flipY) ? -entry.asFloat() * scale :
						entry.asFloat() * scale);
					break;
				}
			}
		}
		
		parent.getBoxModule().addMovable(movement, 0);
	}

}
