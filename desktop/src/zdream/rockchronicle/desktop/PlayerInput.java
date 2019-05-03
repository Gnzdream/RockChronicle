package zdream.rockchronicle.desktop;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.controllers.Controller;

/**
 * 玩家的键位设置
 * @author Zdream
 *
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
		this.ctrl = null;
		// TODO
	}
	/**
	 * 设置将由控制器控制角色
	 */
	public void bindController(Controller ctrl) {
		device = DEVICE_CONTROLLER;
		this.ctrl = ctrl;
		// TODO
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
		
		putKeyMap(InputCenter.MAP_UP, new int[] {Keys.W, Keys.UP});
		putKeyMap(InputCenter.MAP_DOWN, new int[] {Keys.S, Keys.DOWN});
		putKeyMap(InputCenter.MAP_LEFT, new int[] {Keys.A, Keys.LEFT});
		putKeyMap(InputCenter.MAP_RIGHT, new int[] {Keys.D, Keys.RIGHT});
		putKeyMap(InputCenter.MAP_BACK, new int[] {Keys.X, Keys.ESCAPE, Keys.BACKSPACE});
		putKeyMap(InputCenter.MAP_START, new int[] {Keys.Z, Keys.ENTER});
		putKeyMap(InputCenter.MAP_ATTACK, new int[] {Keys.J});
		putKeyMap(InputCenter.MAP_JUMP, new int[] {Keys.K, Keys.SPACE});
		
		putKeyMap(InputCenter.MAP_SPECIAL, new int[] {Keys.I});
		putKeyMap(InputCenter.MAP_RUSH, new int[] {Keys.U});
		putKeyMap(InputCenter.MAP_STRONG, new int[] {Keys.H});
		putKeyMap(InputCenter.MAP_LSWITCH, new int[] {Keys.N});
		putKeyMap(InputCenter.MAP_RSWITCH, new int[] {Keys.M});
	}
	
	void defaultCtrlMap() {
		btnCtrlMap[InputCenter.MAP_BACK & 0xF] = InputCenter.XBOX_BACK;
		btnCtrlMap[InputCenter.MAP_START & 0xF] = InputCenter.XBOX_START;
		btnCtrlMap[InputCenter.MAP_ATTACK & 0xF] = InputCenter.XBOX_X;
		btnCtrlMap[InputCenter.MAP_JUMP & 0xF] = InputCenter.XBOX_A;
		btnCtrlMap[InputCenter.MAP_SPECIAL & 0xF] = InputCenter.XBOX_Y;
		btnCtrlMap[InputCenter.MAP_STRONG & 0xF] = InputCenter.XBOX_B;
		btnCtrlMap[InputCenter.MAP_RUSH & 0xF] = InputCenter.XBOX_RB;
		btnCtrlMap[InputCenter.MAP_LSWITCH & 0xF] = InputCenter.XBOX_LT;
		btnCtrlMap[InputCenter.MAP_RSWITCH & 0xF] = InputCenter.XBOX_RT;
		btnCtrlMap[InputCenter.MAP_L1 & 0xF] = -1;
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
				// 方向
				// TODO
				
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

}
