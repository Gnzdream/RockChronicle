package zdream.rockchronicle.core.character;

/**
 * <p>健康与生命管理的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-06 (create)
 */
public class HealthModule extends AbstractModule {
	
	public static final String NAME = "Health";

	public HealthModule(CharacterEntry ch) {
		super(ch);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String name() {
		return NAME;
	}

}
