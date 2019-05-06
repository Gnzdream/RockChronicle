package zdream.rockchronicle.core.character;

/**
 * <p>管理阵营参数的模块
 * <p>它将指导判断子弹、其它怪物、陷阱等的伤害与效果施加是否生效.
 * <p>如果存在角色无该模块, 则认为该角色可能为中立的
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public class CampModule extends AbstractModule {
	
	public static final String NAME = "Camp";

	public CampModule(CharacterEntry ch) {
		super(ch);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String name() {
		return NAME;
	}

}
