package zdream.rockchronicle.foes.megaman;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.FoeEvent;
import zdream.rockchronicle.core.foe.ShapePainter;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;

public class Megaman extends Foe implements IInputBindable {

	public Megaman() {
		super("megaman");
		
		box = new Box(id);
		boxes = new Box[] {box};
		
		box.setBox(-27313, 0, 54613, 98304);
	}
	
	public Megaman(JsonValue json) {
		this();
	}
	
	/**
	 * @param bAnchorX 单位: 块
	 * @param bAnchorY 单位: 块
	 */
	public Megaman(float bAnchorX, float bAnchorY) {
		this();
		
		box.setAnchor(Box.block2P(bAnchorX), Box.block2P(bAnchorY));
	}
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		putPainter(new ShapePainter(box));
	}
	
	@Override
	public void step(boolean pause) {
		if (!pause) {
			if (input != null) {
				handleInput();
			}
		}
	}
	
	/* **********
	 *   盒子   *
	 ********** */
	
	Box box;
	Box[] boxes;

	@Override
	public Box[] getBoxes() {
		return boxes;
	}
	
	/* **********
	 *   控制   *
	 ********** */

	/**
	 * 上一步, 方向键是否按下
	 */
	boolean lastLeft, lastRight, lastUp, lastDown;
	/**
	 * 上一步, 行动键位是否按下
	 */
	boolean lastJump, lastAttack, lastSlide;
	
	private PlayerInput input;

	@Override
	public void bindController(PlayerInput input) {
		this.input = input;
	}

	@Override
	public void unbindController() {
		this.input = null;
	}
	
	private void handleInput() {
		// 横向
		boolean left = input.isMapKeyDown(InputCenter.MAP_LEFT),
				right = input.isMapKeyDown(InputCenter.MAP_RIGHT);
		boolean up = input.isMapKeyDown(InputCenter.MAP_UP),
				down = input.isMapKeyDown(InputCenter.MAP_DOWN);
		if (left && right) {
			left = right = false;
		}
		if (up && down) {
			up = down = false;
		}
		if (left != lastLeft || right != lastRight || up != lastUp || down != lastDown) {
			publish(axisInputEvent(left, right, up, down));
			lastLeft = left;
			lastRight = right;
			lastUp = up;
			lastDown = down;
		}
		
		// 行动状态
		boolean attack = input.isMapKeyDown(InputCenter.MAP_ATTACK),
				jump = input.isMapKeyDown(InputCenter.MAP_JUMP),
				slide = input.isMapKeyDown(InputCenter.MAP_RUSH);
		if (attack != lastAttack || jump != lastJump || slide != lastSlide) {
			publish(motionInputEvent(attack, attack != lastAttack,
					jump, jump != lastJump, slide, slide != lastSlide));
			lastAttack = attack;
			lastJump = jump;
			lastSlide = slide;
		}
	}
	
	private FoeEvent axisInputEvent(boolean left, boolean right, boolean up, boolean down) {
		FoeEvent event = new FoeEvent("ctrl_axis");
		JsonValue v = new JsonValue(ValueType.object);
		event.value = v;
		
		v.addChild("left", new JsonValue(left));
		v.addChild("right", new JsonValue(right));
		v.addChild("up", new JsonValue(up));
		v.addChild("down", new JsonValue(down));
		return event;
	}
	
	private FoeEvent motionInputEvent(
			boolean attack, boolean attackChange,
			boolean jump, boolean jumpChange,
			boolean slide, boolean slideChange) {
		FoeEvent event = new FoeEvent("ctrl_axis");
		JsonValue v = new JsonValue(ValueType.object);
		event.value = v;
		
		v.addChild("attack", new JsonValue(attack));
		v.addChild("attackChange", new JsonValue(attackChange));
		v.addChild("jump", new JsonValue(jump));
		v.addChild("jumpChange", new JsonValue(jumpChange));
		v.addChild("slide", new JsonValue(slide));
		v.addChild("slideChange", new JsonValue(slideChange));
		return event;
	}

}
