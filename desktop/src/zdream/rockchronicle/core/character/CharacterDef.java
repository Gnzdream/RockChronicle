package zdream.rockchronicle.core.character;

import java.util.HashMap;

/**
 * 角色预定义的初始化数据封装类
 * @author Zdream
 * @since v0.0.1
 * @date 2019-05-05 (create)
 */
public class CharacterDef {
	
	public String name;
	public String className;
	public final HashMap<String, String> map = new HashMap<>();
	
	public String data;
	public String path;

	public CharacterDef() {
		super();
	}
	
	@Override
	public String toString() {
		
		return String.format("CharacterDef:%s", name == null ? path : name);
	}
}
