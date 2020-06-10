package zdream.rockchronicle.core.foe;

import com.badlogic.gdx.utils.JsonValue;

/**
 * 事件主体
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-06 (create)
 *   2019-05-07 (last modify)
 */
public class FoeEvent {

	public final String name;
	public JsonValue value;
	
	public FoeEvent(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("{E:%s}", name);
	}
	
}
