package zdream.rockchronicle.core.module.destroy;

import static zdream.rockchronicle.platform.world.LevelWorld.STEPS_PER_SECOND;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>判断角色是否在房间外, 如果是则销毁的销毁模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-15 (create)
 *   2019-05-15 (last modified)
 */
public class OutsideDestroyModule extends DestroyModule {
	
	/**
	 * 角色销毁的保护时间, 单位: 步, 配置项
	 */
	public int outsideTrialPeriod;
	/**
	 * 角色判断时长, 单位: 步, 配置项
	 */
	public int outsideThreshold;
	/**
	 * 角色在房间外的时长, 单位: 步, 状态项
	 */
	public int outsideDuration;
	
	public OutsideDestroyModule(CharacterEntry parent) {
		super(parent, "outside");
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue odestroyParam = value.get("destroyParam");
		if (odestroyParam != null) {
			outsideTrialPeriod = (int) (STEPS_PER_SECOND * odestroyParam.getFloat("outsideTrialPeriod", 0) + 0.1f);
			if (outsideTrialPeriod <= 0) {
				outsideTrialPeriod = 0;
			}
			
			outsideThreshold = (int) (STEPS_PER_SECOND * odestroyParam.getFloat("outsideDuration", 0) + 0.1f);
			if (outsideThreshold <= 0) {
				outsideThreshold = 0;
			}
		}
	}
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		return parent.getBoxModule().getBox();
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (age > outsideTrialPeriod) {
			Box box = getSingleBox();
			Rectangle pos = box.getPosition();
			if (!new Rectangle(0, 0, world.currentRoom.width, world.currentRoom.height).overlaps(pos)) {
				// 如果跑到房间外, 则直接删掉
				outsideDuration ++;
			} else {
				outsideDuration = 0;
			}
			
			if (outsideDuration > outsideThreshold) {
				parent.willDestroy();
			}
		}
	}

}
