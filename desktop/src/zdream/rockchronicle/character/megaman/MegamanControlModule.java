package zdream.rockchronicle.character.megaman;

import zdream.rockchronicle.character.ControlModule;
import zdream.rockchronicle.desktop.InputCenter;
import zdream.rockchronicle.desktop.PlayerInput;
import zdream.rockchronicle.platform.world.LevelWorld;

public class MegamanControlModule extends ControlModule {
	
	Megaman parent;
	
	MegamanMotionModule to;
	
	public MegamanControlModule(Megaman ch) {
		super(ch);
		this.parent = ch;
	}
	
	@Override
	public void init() {
		super.init();
		to = parent.motion;
	}

	@Override
	public void onKeyPressed(int mapkey, PlayerInput in) {
		// 这里忽略方向键
		
		switch (mapkey) {
		case InputCenter.MAP_JUMP:
			to.recvControl(new String[] {MegamanMotionModule.INFO_JUMP});
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
	public void step(LevelWorld world, int index, boolean hasNext) {
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
		
		// 向 to 发送消息
		
	}

}
