package zdream.rockchronicle.core.module.scout;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>是否对墙壁进行碰撞的侦查模块, 其实判断是否有对角色有反作用力的检测.
 * <p>检测的方法是, 根据上一次速度 {@link Box#lastVelocityX}, {@link Box#lastVelocityY},
 * 判断角色的运动方向. 然后判断角色在运动方向上是否碰边 (相撞).
 * 相撞则发布 "block_impact" 消息
 * <p>该模块需要在 {@link zdream.rockchronicle.core.module.motion.TerrainMotionModule}
 * 等将 {@link Box#bottomStop} 等参数设置的模块之后执行.
 * 该模块无法检测在非运动方向上碰边的情形.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-09 (created)
 *   2019-06-09 (last modified)
 * @see Box
 */
public class BlockImpactScoutModule extends AbstractModule {

	public BlockImpactScoutModule(CharacterEntry parent) {
		super(parent, "scout", "blockImpact");
	}
	
	@Override
	public int priority() {
		return -20;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		Box box = parent.getBoxModule().getBox();
		
		boolean impactTop = (box.lastVelocityY > 0 && box.topStop);
		boolean impactBottom = (box.lastVelocityY < 0 && box.bottomStop);
		boolean impactLeft = (box.lastVelocityX < 0 && box.leftStop);
		boolean impactRight = (box.lastVelocityX > 0 && box.rightStop);
		
		if (impactTop || impactBottom || impactLeft || impactRight) {
			CharacterEvent event = new CharacterEvent("block_impact");
			
			JsonValue v = new JsonValue(ValueType.object);
			JsonValue array = new JsonValue(ValueType.array);
			if (impactTop) {
				array.addChild(new JsonValue("top"));
			}
			if (impactBottom) {
				array.addChild(new JsonValue("bottom"));
			}
			if (impactLeft) {
				array.addChild(new JsonValue("left"));
			}
			if (impactRight) {
				array.addChild(new JsonValue("right"));
			}
			
			v.addChild("direction", array);
			event.value = v;
			
			parent.publish(event);
		}
	}

}
