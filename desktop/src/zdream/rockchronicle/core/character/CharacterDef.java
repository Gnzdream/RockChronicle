package zdream.rockchronicle.core.character;

import java.util.HashMap;

/**
 * 人物启动参数
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
