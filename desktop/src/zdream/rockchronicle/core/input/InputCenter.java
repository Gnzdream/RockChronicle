package zdream.rockchronicle.core.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

/**
 * 配置键位
 * @author Zdream
 */
public class InputCenter {
	
	/*
	 * 键位映射部分
	 * 
	 * 需要映射的键位是这些 (下面会罗列游戏中有意义的操作键, 以及默认映射的键盘键位以及 XBox 360 手柄键位 [未完成])
	 */
	public static final int
			MAP_UP = 0x0001,
			MAP_DOWN = 0x0002,
			MAP_LEFT = 0x0003,
			MAP_RIGHT = 0x0004,
			MAP_BACK = 0x1001,
			MAP_START = 0x1002,
			MAP_ATTACK = 0x1003, // XBOX: X
			MAP_JUMP = 0x1004, // XBOX: A
			MAP_SPECIAL = 0x1005, // XBOX: Y
			MAP_STRONG = 0x1006, // XBOX: B, not used yet
			MAP_L1 = 0x1007, // XBOX: L1, not used yet
			MAP_RUSH = 0x1008, // XBOX: R1
			MAP_LSWITCH = 0x1009, // XBOX: L2
			MAP_RSWITCH = 0x100A; // XBOX: R2
	
	public static final int
			XBOX_A = 0,
			XBOX_B = 1,
			XBOX_X = 2,
			XBOX_Y = 3,
			XBOX_LB = 4,
			XBOX_RB = 5,
			XBOX_BACK = 6,
			XBOX_START = 7,
			XBOX_LT = 0x100, // 虚拟键位  是由 axis-4 来控制的, 值为正数
			XBOX_RT = 0x101; // 虚拟键位  是由 axis-4 来控制的, 值为负数
	
	public InputCenter() {
		p1 = new PlayerInput((byte) 1);
		p2 = new PlayerInput((byte) 2);
	}
	
	public final PlayerInput p1, p2;

	public void init() {
		Gdx.input.setInputProcessor(l);
	}
	
	class InputListener implements InputProcessor {

		@Override
		public boolean keyDown(int keycode) {
			p1.keyPressed(keycode);
			p2.keyPressed(keycode);
			return false;
		}

		@Override
		public boolean keyUp(int keycode) {
			p1.keyReleased(keycode);
			p2.keyReleased(keycode);
			return false;
		}

		@Override
		public boolean keyTyped(char character) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			// TODO 点击事件暂时不做
			return false;
		}

		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			// TODO 点击事件暂时不做
			return false;
		}

		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			// TODO 点击事件暂时不做
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			// TODO 鼠标移动事件暂时不做
			return false;
		}

		@Override
		public boolean scrolled(int amount) {
			// TODO 滚轮事件暂时不做
			return false;
		}
		
	}
	InputListener l = new InputListener();

}
