package zdream.rockchronicle.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.ControlModule;
import zdream.rockchronicle.core.input.InputCenter;
import zdream.rockchronicle.core.input.PlayerInput;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanControlModule extends ControlModule {
	
	Megaman parent;
	
	MegamanMotionModule to;
	
	public MegamanControlModule(Megaman ch) {
		super(ch);
		this.parent = ch;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		to = parent.motion;
	}

	@Override
	public void onKeyPressed(int mapkey, PlayerInput in) {
		// 这里忽略方向键
		
		switch (mapkey) {
		case InputCenter.MAP_JUMP:
			to.recvControl(new String[] {MegamanMotionModule.INFO_JUMP});
			break;
			
		case InputCenter.MAP_ATTACK:
			to.recvControl(new String[] {MegamanMotionModule.INFO_ATTACK_BEGIN});
			break;

		default:
			break;
		}
	}

	@Override
	public void onKeyReleased(int mapkey, PlayerInput in) {
		// 这里忽略方向键
		
		switch (mapkey) {
		case InputCenter.MAP_JUMP:
			to.recvControl(new String[] {MegamanMotionModule.INFO_JUMP_END});
			break;

		default:
			break;
		}
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		if (index != 0) {
			return;
		}
		
		// 横向
		boolean left = in.isMapKeyDown(InputCenter.MAP_LEFT),
				right = in.isMapKeyDown(InputCenter.MAP_RIGHT);
		
		if (left && !right) {
			to.recvControl(new String[] {MegamanMotionModule.INFO_LEFT});
		} else if (!left && right) {
			to.recvControl(new String[] {MegamanMotionModule.INFO_RIGHT});
		}
		
		// 攻击状态
		if (in.isMapKeyDown(InputCenter.MAP_ATTACK)) {
			to.recvControl(new String[] {MegamanMotionModule.INFO_IN_ATTACK});
		}
		
		// 向 to 发送消息
		
	}

}
