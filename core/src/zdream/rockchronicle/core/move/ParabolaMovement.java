package zdream.rockchronicle.core.move;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>抛物线 / 二次方程移动. 横坐标和纵坐标均可以设置成二次方程.
 * <p>该类不直接执行移动, 只是设置速度
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-01 (created)
 *   2019-06-01 (last modified)
 */
public class ParabolaMovement implements IMovable {
	
	/**
	 * 当前横纵坐标方向的速度
	 */
	public float vx, vy;
	/**
	 * 当前横纵坐标方向的加速度
	 */
	public float ax, ay;
	public boolean enable = true;

	@Override
	public void move(LevelWorld world, Box box, CharacterEntry entry) {
		if (enable) {
			vx += ax;
			vy += ay;
			if (vx != 0) {
				box.setVelocityX(box.velocity.x + vx);
			}
			if (vy != 0) {
				box.setVelocityY(box.velocity.y + vy);
			}
		}
	}

}
