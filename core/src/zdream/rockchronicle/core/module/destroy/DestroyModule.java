package zdream.rockchronicle.core.module.destroy;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>角色销毁模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-15 (create)
 *   2019-05-15 (last modified)
 */
public class DestroyModule extends AbstractModule {
	
	/**
	 * 角色存在的生命, 单位: 步, 配置项
	 * 超过这个时间, 角色将销毁. 默认为 -1 表示永远不消失
	 */
	public int life = -1;
	/**
	 * 角色在该世界已经存在的时间, 单位: 步, 状态项
	 */
	public int age = 0;
	
	public static final String NAME = "Destroy";

	public DestroyModule(CharacterEntry parent) {
		super(parent);
	}

	@Override
	public String name() {
		return NAME;
	}
	
	@Override
	public int priority() {
		return -500;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue odestroyParam = value.get("destroyParam");
		if (odestroyParam != null) {
			float flife = odestroyParam.getFloat("life", -1);
			if (flife <= 0) {
				this.life = -1;
			} else {
				this.life = (int) (LevelWorld.STEPS_PER_SECOND * flife + 0.1f);
			}
		}
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		age++;
		// 检查寿命
		if (life > 0 && age > life) {
			parent.willDestroy();
		}
	}

}
