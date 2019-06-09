package zdream.rockchronicle.core.module.buff;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.module.AbstractModule;

/**
 * <p>管理特殊状态参数的模块
 * <p>特殊状态一旦产生, 将向角色添加相应的模块, 并和 BuffModule 进行生命周期的绑定
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public class BuffModule extends AbstractModule {
	
	public static final String NAME = "buff";

	public BuffModule(CharacterEntry ch, String desc) {
		super(ch, NAME, desc);
		// TODO Auto-generated constructor stub
	}

}
