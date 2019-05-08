package zdream.rockchronicle.core.character.collision;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.module.AbstractModule;
import zdream.rockchronicle.core.character.module.MotionModule;
import zdream.rockchronicle.core.character.motion.IBoxHolder;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>碰撞模块
 * <p>产生伤害以及其它效果的源头. 优先级 -128
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-08 (create)
 */
public class CollisionModule extends AbstractModule {
	
	public static final String NAME = "Collision";

	public CollisionModule(CharacterEntry parent) {
		super(parent);
		
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -0x80;
	}
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		MotionModule mm = parent.getMotion();
		if (mm instanceof IBoxHolder) {
			return ((IBoxHolder) mm).getBox();
		}
		return null;
	}
	
	protected void searchOverlapsBox(Box box, LevelWorld world) {
		world.overlaps(box, this::doForOverlapsBox);
	}
	
	/**
	 * 对碰撞的、重合的其它角色的碰撞盒子进行判断.
	 * @param box
	 * @return
	 *   如果还需要判断其它的盒子, 则返回 true; 如果不再判断其它盒子, 返回 false
	 */
	protected boolean doForOverlapsBox(Box box) {
		// 需要子类覆盖
		return false;
	}

}
