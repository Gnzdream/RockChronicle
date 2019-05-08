package zdream.rockchronicle.core.character.event;

import com.badlogic.gdx.utils.JsonValue;

/**
 * 事件主体
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (create)
 *   2019-05-07 (last modify)
 */
public class CharacterEvent {

	public final String name;
	public JsonValue value;
	
	public CharacterEvent(String name) {
		super();
		this.name = name;
	}
	
}
