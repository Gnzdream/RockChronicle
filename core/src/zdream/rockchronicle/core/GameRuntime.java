package zdream.rockchronicle.core;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.IPainter;
import zdream.rockchronicle.core.input.IInputBindable;
import zdream.rockchronicle.core.region.Gate;
import zdream.rockchronicle.core.region.Region;
import zdream.rockchronicle.core.region.Room;
import zdream.rockchronicle.core.world.LevelWorld;
import zdream.rockchronicle.core.world.SceneDesigner;
import zdream.rockchronicle.core.world.Ticker;

public class GameRuntime {
	
	public void init() {
		world.init();
		scene.init();
		
		ticker.setStepListener((pause) -> this.step(pause));
	}
	
	public void tick(float delta) {
		ticker.tick(delta);
	}
	
	/*
	 * 补充的方法在这里
	 */
	
	/* **********
	 *  计时器  *
	 ********** */
	/*
	 * 计时器和暂停管理等
	 * 包括世界状态:
	 * 1. 房间切换
	 * 2. 自由控制
	 */
	public Ticker ticker = new Ticker();
	
	public void step(byte pause) {
		
		// 如果房间还在切换中, 就切换吧. 当前 pause = 2;
		if (scene.durationShift()) {
			scene.tickShift();
		} else {
			// 检查是否需要发生房间切换
			Gate gate = checkGateTriggered();
			if (gate != null) {
				scene.doShift(gate);
			}
		}
		
		// pause 可能已经被更新过了
		world.tick(ticker.pause);
	}
	
	@Override
	public String toString() {
		int zoneSum = world.zoneSum();
		
		return String.format("Foe: %d, Box: %d, Painter: %d, 区块: %d",
				world.foes.size, world.boxes.size, painters.size, zoneSum);
	}
	
	/* **********
	 *   地图   *
	 ********** */
	
	public final LevelWorld world = new LevelWorld(this);

	/**
	 * 创建一个 {@link Region}, 设置为当前活动的地区, 并且当前房间为默认的初始房间
	 * @param name
	 *   这个初始的 {@link Region} 的名字
	 */
	public void setCurrentRegion(String name) {
		world.setCurrentRegion(name);
	}

	public Room getCurrentRoom() {
		return world.getCurrentRoom();
	}
	
	/**
	 * 当前房间发生替换
	 * @param room
	 */
	public void setCurrentRoom(int room) {
		// 下面顺序不能调换
		world.setCurrentRoom(room); // 一定要先 world (管理 box 的)
		this.scene.onRoomChanged(); // 然后再 scene (添加 box 的)
	}

	public void setCurrentRoom(Room destRoom) {
		if (destRoom.region != world.curRegion) {
			throw new IllegalArgumentException(destRoom.toString());
		}
		setCurrentRoom(destRoom.index);
	}

	public void levelStart() {
		ticker.worldResume();
	}
	
	public void roomShiftingStarted() {
		ticker.roomShifting();
		world.roomShiftingStarted();
	}
	
	public void roomShiftingFinished() {
		ticker.worldResume();
		world.roomShiftingFinished();
	}
	
	/**
	 * 查看是否发生房间切换 (房间的大门触发)
	 */
	private Gate checkGateTriggered() {
		Foe c1 = world.player1;
		if (c1 == null) {
			return null;
		}
		Gate gate = world.checkGateTouched(c1.getBoxes()[0]);
		if (gate != null && world.checkShift(c1, gate)) {
//			shift.doShift(gate);
			return gate;
		}
		return null;
	}
	
	/* **********
	 *   画面   *
	 ********** */
	
	public SceneDesigner scene = new SceneDesigner(this);

	/* **********
	 *   角色   *
	 ********** */
	
	public Array<IPainter> painters = new Array<>();
	
	/**
	 * 用 id 来寻找角色.
	 * @param id
	 * @return
	 *   可能为 null
	 */
	public Foe findEntry(int id) {
		return world.findEntry(id);
	}
	
	public void setPlayer1(Foe entry) {
		world.setPlayer1(entry);
		if (entry instanceof IInputBindable) {
			((IInputBindable) entry).bindController(RockChronicle.INSTANCE.input.p1);
		}
	}
	
	public void addFoe(Foe entry) {
		world.addFoe(entry);
	}
	
	public void removeFoe(Foe entry) {
		world.removeFoe(entry);
	}
	
	public void destroyFoeNow(Foe entry) {
		world.destroyFoeNow(entry);
	}

	public void addBox(Box box) {
		world.addBox(box);
	}

	public void removeBox(Box box) {
		world.removeBox(box);
	}

	public void addPainter(IPainter painter) {
		painters.add(painter);
	}

	public void removePainter(IPainter painter) {
		painters.removeValue(painter, true);
	}
	
}
