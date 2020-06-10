package zdream.rockchronicle.core.input;

import static zdream.rockchronicle.core.input.InputCenter.MAP_ATTACK;
import static zdream.rockchronicle.core.input.InputCenter.MAP_BACK;
import static zdream.rockchronicle.core.input.InputCenter.MAP_DOWN;
import static zdream.rockchronicle.core.input.InputCenter.MAP_JUMP;
import static zdream.rockchronicle.core.input.InputCenter.MAP_L1;
import static zdream.rockchronicle.core.input.InputCenter.MAP_LEFT;
import static zdream.rockchronicle.core.input.InputCenter.MAP_LSWITCH;
import static zdream.rockchronicle.core.input.InputCenter.MAP_RIGHT;
import static zdream.rockchronicle.core.input.InputCenter.MAP_RSWITCH;
import static zdream.rockchronicle.core.input.InputCenter.MAP_RUSH;
import static zdream.rockchronicle.core.input.InputCenter.MAP_SPECIAL;
import static zdream.rockchronicle.core.input.InputCenter.MAP_START;
import static zdream.rockchronicle.core.input.InputCenter.MAP_STRONG;
import static zdream.rockchronicle.core.input.InputCenter.MAP_UP;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.PovDirection;

/**
 * <p>玩家的键位设置.
 * <p>这里设置的是单个玩家或控制源的键位输入.
 * 控制游戏时, 键盘输入和手柄输入可以同时监听.
 * </p>
 * 
 * @author Zdream
 */
public class PlayerInput {
	
	/**
	 * 是否启用
	 */
	boolean enable;
	
	/**
	 * 序号, 指示你是 1P 还是 2P
	 */
	public final byte seq;
	
	byte device = DEVICE_KEYBOARD;
	final static byte DEVICE_KEYBOARD = 1;
	final static byte DEVICE_CONTROLLER = 2;
	
	/**
	 * 键盘方向键位
	 * 索引 1 分别是 null、上下左右;
	 * 每个第一索引指向的一维数组就是全部的控制这个方向的键位.<br>
	 * 
	 * 例如 dirKeyMap[InputCenter.MAP_UP] 是这样的一个数组 [Keys.W, Keys.UP],
	 * 就是说 W 和 UP 两个键位都是控制它的
	 */
	int[][] dirKeyMap = new int[5][];
	
	/**
	 * 键盘其它键位 (除方向键外的所有键)
	 */
	int[][] btnKeyMap = new int[11][];
	
	/**
	 * 这里建立了一个 value 到 key 的映射 (键盘)<br>
	 * 物理键位 - 游戏键位, 如: [W] 代表上
	 */
	HashMap<Integer, Integer> mapping = new HashMap<>();
	
	/**
	 * 手柄按钮键位 (手柄的方向键位不会改的)<br>
	 * 游戏键位 - 物理键位, 如: [W] 代表上
	 */
	int[] btnCtrlMap = new int[11];
	
	/**
	 * 手柄实体
	 */
	Controller ctrl;
	
	// 触发消息传播
	protected ArrayList<IControlListener>[] transmitDirection;

	protected ArrayList<IControlListener>[] transmitButton;
	
	@SuppressWarnings("unchecked")
	private void initTransmitArray() {
		transmitDirection = new ArrayList[5];
		for (int i = 1; i < 5; i++) {
			transmitDirection[i] = new ArrayList<>();
		}
		
		transmitButton = new ArrayList[11];
		for (int i = 1; i < 11; i++) {
			transmitButton[i] = new ArrayList<>();
		}
	}
	
	/**
	 * @param seq
	 *  序号. 其实就是你是 1P 还是 2P 的意思
	 */
	public PlayerInput(final byte seq) {
		this.seq = seq;
		defaultKeyMap();
		defaultCtrlMap();
		
		initTransmitArray();
	}
	
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	
	public boolean isKeyboard() {
		return device == DEVICE_KEYBOARD;
	}
	public boolean isController() {
		return device == DEVICE_CONTROLLER;
	}
	/**
	 * 设置将由键盘控制角色
	 */
	public void bindKeyboard() {
		device = DEVICE_KEYBOARD;

		if (this.ctrl != null) {
			Gdx.app.log("InputCenter", "用户 " + seq + " 解除绑定手柄: " + ctrl.getName());
			ctrl.removeListener(cl);
			this.ctrl = null;
		}
	}
	/**
	 * 设置将由控制器控制角色
	 */
	public void bindController(Controller ctrl) {
		device = DEVICE_CONTROLLER;
		this.ctrl = ctrl;
		
		Gdx.app.log("InputCenter", "用户 " + seq + " 绑定手柄: " + ctrl.getName());
		ctrl.addListener(cl);
	}
	
	public Controller getCtrl() {
		return ctrl;
	}
	
	void putKeyMap(int mapKey, int[] keyCodes) {
		if (mapKey < 0xF) {
			dirKeyMap[mapKey] = keyCodes;
		} else {
			btnKeyMap[mapKey & 0xF] = keyCodes;
		}
		
		for (int i = 0; i < keyCodes.length; i++) {
			mapping.put(keyCodes[i], mapKey);
		}
	}
	
	void defaultKeyMap() {
		mapping.clear();
		
		putKeyMap(MAP_UP, new int[] {Keys.W, Keys.UP});
		putKeyMap(MAP_DOWN, new int[] {Keys.S, Keys.DOWN});
		putKeyMap(MAP_LEFT, new int[] {Keys.A, Keys.LEFT});
		putKeyMap(MAP_RIGHT, new int[] {Keys.D, Keys.RIGHT});
		putKeyMap(MAP_BACK, new int[] {Keys.X, Keys.ESCAPE, Keys.BACKSPACE});
		putKeyMap(MAP_START, new int[] {Keys.Z, Keys.ENTER});
		putKeyMap(MAP_ATTACK, new int[] {Keys.J});
		putKeyMap(MAP_JUMP, new int[] {Keys.K, Keys.SPACE});
		
		putKeyMap(MAP_SPECIAL, new int[] {Keys.I});
		putKeyMap(MAP_RUSH, new int[] {Keys.U});
		putKeyMap(MAP_STRONG, new int[] {Keys.H});
		putKeyMap(MAP_LSWITCH, new int[] {Keys.N});
		putKeyMap(MAP_RSWITCH, new int[] {Keys.M});
	}
	
	void defaultCtrlMap() {
		btnCtrlMap[MAP_BACK & 0xF] = InputCenter.XBOX_BACK;
		btnCtrlMap[MAP_START & 0xF] = InputCenter.XBOX_START;
		btnCtrlMap[MAP_ATTACK & 0xF] = InputCenter.XBOX_X;
		btnCtrlMap[MAP_JUMP & 0xF] = InputCenter.XBOX_A;
		btnCtrlMap[MAP_SPECIAL & 0xF] = InputCenter.XBOX_Y;
		btnCtrlMap[MAP_STRONG & 0xF] = InputCenter.XBOX_B;
		btnCtrlMap[MAP_RUSH & 0xF] = InputCenter.XBOX_RB;
		btnCtrlMap[MAP_LSWITCH & 0xF] = InputCenter.XBOX_LT;
		btnCtrlMap[MAP_RSWITCH & 0xF] = InputCenter.XBOX_RT;
		btnCtrlMap[MAP_L1 & 0xF] = -1;
	}
	
	/**
	 * 接收到键盘的按键响应
	 * @param keyCode
	 */
	void keyPressed(int keyCode) {
		Integer i = mapping.get(keyCode);
		if (i == null) {
			return;
		}
		
		transmitPressed(i);
	}
	
	/**
	 * 接收到键盘的释放响应
	 * @param keyCode
	 */
	void keyReleased(int keyCode) {
		Integer i = mapping.get(keyCode);
		if (i == null) {
			return;
		}
		
		transmitReleased(i);
	}
	
	public boolean isMapKeyDown(int mapKey) {
		if (device == DEVICE_KEYBOARD) {
			int[] keys;
			
			if (mapKey < 0xF) {
				// 方向
				keys = dirKeyMap[mapKey];
			} else {
				// button
				keys = btnKeyMap[mapKey & 0xF];
			}
			
			for (int i = 0; i < keys.length; i++) {
				if (Gdx.input.isKeyPressed(keys[i])) {
					return true;
				}
			}
		} else {
			if (ctrl == null) {
				return false;
			}
			
			if (mapKey < 0xF) {
				// 现在确定方向. 十字键与左摇杆都可以, 但是两者会冲突.
				
				// 如果 Pov 有效, 则按照 Pov, 否则 Axis
				PovDirection dir = ctrl.getPov(0);
				if (dir != PovDirection.center) {
					switch (mapKey) {
					case MAP_UP:
						return (dir == PovDirection.north || dir == PovDirection.northEast || dir == PovDirection.northWest);
					case MAP_DOWN:
						return (dir == PovDirection.south || dir == PovDirection.southEast || dir == PovDirection.southWest);
					case MAP_LEFT:
						return (dir == PovDirection.west || dir == PovDirection.northWest || dir == PovDirection.southWest);
					case MAP_RIGHT:
						return (dir == PovDirection.east || dir == PovDirection.northEast || dir == PovDirection.southEast);
					default:
						return false;
					}
				}
				
				// Axis
				float y = ctrl.getAxis(0);
				float x = ctrl.getAxis(1);
				
				switch (mapKey) {
				case MAP_UP:
					return y < -0.2f;
				case MAP_DOWN:
					return y > 0.2f;
				case MAP_LEFT:
					return x < -0.2f;
				case MAP_RIGHT:
					return x > 0.2f;

				default:
					return false;
				}
			} else {
				// button
				int key = btnCtrlMap[mapKey & 0xF];
				if (key < 0) { // 没有对应的键位
					return false;
				}
				if (key > 0xFF) {
					// TODO 虚拟键位
					return false;
				}
				return ctrl.getButton(key);
			}
		}
		return false;
	}

	/**
	 * 由用户调用
	 * @param mapKeys
	 * @param l
	 */
	public void addControlListener(int[] mapKeys, IControlListener l) {
		int key, idx;
		boolean isDirection;
		
		for (int i = 0; i < mapKeys.length; i++) {
			key = mapKeys[i];
			isDirection = key < 0x1000;
			idx = (isDirection) ? key : key & 0xFFF;
			
			if (isDirection) {
				transmitDirection[idx].add(l);
			} else {
				transmitButton[idx].add(l);
			}
		}
	}
	
	/**
	 * 清除这个 IControlListener 的所有绑定
	 * @param l
	 */
	public void removeControlListener(IControlListener l) {
		int length = transmitDirection.length;
		for (int i = 1; i < length; i++) {
			transmitDirection[i].remove(l);
		}
		
		length = transmitButton.length;
		for (int i = 1; i < length; i++) {
			transmitButton[i].remove(l);
		}
	}
	
	private void transmitPressed(int mapKey) {
		ArrayList<IControlListener> ls;
		if (mapKey < 0xF) {
			ls = transmitDirection[mapKey];
		} else {
			ls = transmitButton[mapKey & 0xF];
		}
		
		int len = ls.size();
		for (int i = 0; i < len; i++) {
			IControlListener l = ls.get(i);
			l.onKeyPressed(mapKey, this);
		}
	}
	
	private void transmitReleased(int mapKey) {
		ArrayList<IControlListener> ls;
		if (mapKey < 0xF) {
			ls = transmitDirection[mapKey];
		} else {
			ls = transmitButton[mapKey & 0xF];
		}
		
		int len = ls.size();
		for (int i = 0; i < len; i++) {
			IControlListener l = ls.get(i);
			l.onKeyReleased(mapKey, this);
		}
	}
	
	/*
	 * Xbox 手柄说明:
	 * 
	 * --- 键位按下:
	 * A : button 0
	 * B : button 1
	 * X : button 2
	 * Y : button 3
	 * LB : button 4
	 * RB : button 5
	 * Back : button 6
	 * Start : button 7
	 * 左摇杆 : button 8
	 * 右摇杆 : button 9
	 * 
	 * --- 十字键
	 * axisMoved,
	 * 方向是八个再加没有动静, 共 9 种可能
	 * 
	 * --- 左右摇杆
	 * axisMoved,
	 * 当左摇杆上下移动 (y 变化), axisCode = 0, 上为负, 下为正, 范围 -1 至 1
	 * 当左摇杆左右移动 (x 变化), axisCode = 1, 左为负, 右为正, 范围 -1 至 1
	 * 当右摇杆上下移动 (y 变化), axisCode = 2, 上为负, 下为正, 范围 -1 至 1
	 * 当右摇杆左右移动 (x 变化), axisCode = 3, 左为负, 右为正, 范围 -1 至 1
	 * 
	 * --- 扳机
	 * axisMoved,
	 * 左右扳机合起来操纵 axisCode = 4 一个值, 具体是:
	 * 左扳机为正 (0 ~ 1), 右扳机为负 (-1 ~ 0), 如果两个一起按, axisCode = 左 + 右
	 */
	
	/**
	 * 只用于检测连接是否断了. 如果断了, 则换回键盘
	 */
	ControllerAdapter cl = new ControllerAdapter() {
		@Override
		public void disconnected(Controller controller) {
			PlayerInput.this.bindKeyboard();
		}
	};
	
}
