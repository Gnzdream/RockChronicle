package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.event.CharacterEventCreator;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;
import zdream.rockchronicle.core.module.control.ControlModule;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanControlModule extends ControlModule {
	
	MegamanInLevel parent;
	CharacterEventCreator creator;

	boolean lastLeft, lastRight, lastUp, lastDown;
	boolean lastJump, lastAttack, lastSlide;
	
	public MegamanControlModule(MegamanInLevel ch) {
		super(ch);
		this.parent = ch;
		creator = new CharacterEventCreator();
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
	}

	@Override
	public void onKeyPressed(int mapkey, PlayerInput in) {
		// 这里忽略方向键
	}

	@Override
	public void onKeyReleased(int mapkey, PlayerInput in) {
		// 这里忽略方向键
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		if (index != 0) {
			return;
		}
		
		// 横向
		boolean left = in.isMapKeyDown(InputCenter.MAP_LEFT),
				right = in.isMapKeyDown(InputCenter.MAP_RIGHT);
		boolean up = in.isMapKeyDown(InputCenter.MAP_UP),
				down = in.isMapKeyDown(InputCenter.MAP_DOWN);
		if (left && right) {
			left = right = false;
		}
		if (up && down) {
			up = down = false;
		}
		if (left != lastLeft || right != lastRight || up != lastUp || down != lastDown) {
			parent.publish(creator.ctrlAxis(left, right, up, down).get());
			lastLeft = left;
			lastRight = right;
			lastUp = up;
			lastDown = down;
		}
		
		// 行动状态
		boolean attack = in.isMapKeyDown(InputCenter.MAP_ATTACK),
				jump = in.isMapKeyDown(InputCenter.MAP_JUMP),
				slide = in.isMapKeyDown(InputCenter.MAP_RUSH);
		if (attack != lastAttack || jump != lastJump || slide != lastSlide) {
			parent.publish(
					creator.ctrlMotion(attack, attack != lastAttack,
					jump, jump != lastJump, slide,
					slide != lastSlide).get());
			lastAttack = attack;
			lastJump = jump;
			lastSlide = slide;
		}
	}

}
