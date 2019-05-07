package zdream.rockchronicle.core.character;

/**
 * 角色预定义的初始化数据封装类
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-07 (create)
 */
public class ModuleDef {
	
	public String name;
	public String className;
	public String species;

	public ModuleDef() {
		super();
	}
	
	@Override
	public String toString() {
		
		return String.format("ModuleDef %s:%s", species, name);
	}
}
