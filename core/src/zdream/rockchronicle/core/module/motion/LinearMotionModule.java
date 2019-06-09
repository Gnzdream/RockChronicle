package zdream.rockchronicle.core.module.motion;

import static zdream.rockchronicle.platform.world.LevelWorld.TIME_STEP;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.move.LinearMovement;
import zdream.rockchronicle.platform.body.Box;

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
		parent.addSubscribe("motion_aim_to", this);
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("motion_aim_to".equals(event.name)) {
			// 计算现在这里到目标的方向
			// 现在的位置
			Box box = parent.getBoxModule().getBox();
			float startX = box.anchor.x;
			float startY = box.anchor.y;
			
			// 目标位置
			float targetX = event.value.getFloat("x");
			float targetY = event.value.getFloat("y");
			float speed = event.value.getFloat("speed") * TIME_STEP;
			
			if (speed == 0 || startX == targetX && startY == targetY) {
				// 不改变方向
				return;
			}
			if (startX == targetX) {
				movement.vy = speed;
				return;
			}
			if (startY == targetY) {
				movement.vx = speed;
				return;
			}
			
			String formula = event.value.getString("formula", "simple");
			if ("pythagorean".equals(formula)) {
				aimToUsePythagorean(startX, startY, targetX, targetY, speed);
			} else {
				aimToUseSimple(startX, startY, targetX, targetY, speed);
			}
		} else {
			super.receiveEvent(event);
		}
	}
	
	public void aimToUseSimple(float startX,
			float startY,
			float targetX,
			float targetY,
			float speed) {
		// 默认采用两坐标相加法, 而非勾股定理方法
		
		float distance = Math.abs(targetX - startX) + Math.abs(targetY - startY);
		movement.vx = (targetX - startX) / distance * speed;
		movement.vy = (targetY - startY) / distance * speed;
	}
	
	/**
	 * 使用勾股定理的距离公式
	 */
	public void aimToUsePythagorean(float startX,
			float startY,
			float targetX,
			float targetY,
			float speed) {
		
		float deltaX = targetX - startX;
		float deltaY = targetY - startY;
		float distance = (float) Math.pow(deltaX * deltaX + deltaY * deltaY, 0.5);
		
		movement.vx = deltaX / distance * speed;
		movement.vy = deltaY / distance * speed;
	}
	
}
