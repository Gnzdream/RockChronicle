package zdream.rockchronicle.core.character.event;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.parameter.IValueCreator;

/**
 * 事件的生成器
 * 
 * @author Zdream
 * @since v0.0.1
 * @see CharacterEvent
 * @date
 *   2019-05-06 (create)
 *   2019-05-07 (last modify)
 */
public class CharacterEventCreator implements IValueCreator<CharacterEvent> {
	
	private CharacterEvent event;

	@Override
	public CharacterEvent get() {
		return event;
	}
	
	/**
	 * 清除事件数据的缓存
	 */
	public void clear() {
		this.event = null;
	}
	
	/**
	 * 创建当方向有变化则发布一次的事件
	 * @return
	 */
	public CharacterEventCreator ctrlAxis(boolean left, boolean right, boolean up, boolean down) {
		event = new CharacterEvent("ctrl_axis");
		JsonValue v = new JsonValue(ValueType.object);
		event.value = v;
		
		v.addChild("left", new JsonValue(left));
		v.addChild("right", new JsonValue(right));
		v.addChild("up", new JsonValue(up));
		v.addChild("down", new JsonValue(down));
		
		return this;
	}
	
	/**
	 * 创建当攻击键和跳跃键有变化则发布一次的事件
	 * @return
	 *   this
	 */
	public CharacterEventCreator ctrlMotion(
			boolean attack, boolean attackChange,
			boolean jump, boolean jumpChange,
			boolean slide, boolean slideChange) {
		event = new CharacterEvent("ctrl_motion");
		JsonValue v = new JsonValue(ValueType.object);
		event.value = v;
		
		v.addChild("attack", new JsonValue(attack));
		v.addChild("attack_change", new JsonValue(attackChange));
		v.addChild("jump", new JsonValue(jump));
		v.addChild("jump_change", new JsonValue(jumpChange));
		v.addChild("slide", new JsonValue(slide));
		v.addChild("slide_change", new JsonValue(slideChange));
		
		return this;
	}

//	private stat

}
