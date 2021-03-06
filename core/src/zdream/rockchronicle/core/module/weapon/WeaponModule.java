package zdream.rockchronicle.core.module.weapon;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>武器管理的模块
 * <p>它将指导发射子弹
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public class WeaponModule extends AbstractModule {
	
	public static final String NAME = "weapon";

	public WeaponModule(CharacterEntry ch, String desc) {
		super(ch, NAME, desc);
	}

}
