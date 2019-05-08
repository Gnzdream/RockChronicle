package zdream.rockchronicle.core.character.module;

import zdream.rockchronicle.core.character.CharacterEntry;

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
	
	public static final String NAME = "Weapon";

	public WeaponModule(CharacterEntry ch) {
		super(ch);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String name() {
		return NAME;
	}

}
