package zdream.rockchronicle.core;

import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.IFoePainter;
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
			
			// pause 需要更新
			pause = ticker.pause;
		}
		
		// 移动部分
		for (int i = 0; i < foes.size; i++) {
			try {
				foes.get(i).step(pause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		// 进行删增工作
		handleFoeAddAndRemove();
		
		// 确定状态部分
		for (int i = 0; i < foes.size; i++) {
			try {
				foes.get(i).submit(pause);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		
		// debug
		if (foes.size != foeCount) {
			System.out.println(String.format("%d: Foe: %d, Box: %d, Painter: %d",
					ticker.count, foes.size, boxes.size, painters.size));
			foeCount = foes.size;
		}
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
		world.setCurrentRoom(room);
		this.scene.onRoomChanged();
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
		foesWaitingForAdd.clear();
	}
	
	public void roomShiftingFinished() {
		ticker.worldResume();
	}
	
	/**
	 * 查看是否发生房间切换 (房间的大门触发)
	 */
	private Gate checkGateTriggered() {
		Foe c1 = player1;
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
	
	public Array<Foe> foes = new Array<>();
	public Array<Box> boxes = new Array<>();
	public Array<IFoePainter> painters = new Array<>();
	private final Array<Foe> foesWaitingForAdd = new Array<>();
	private final Array<Foe> foesWaitingForRemove = new Array<>();
	public Foe player1;
	
	// debug 用
	int foeCount = 0;
	
	/**
	 * 用 id 来寻找角色.
	 * @param id
	 * @return
	 *   可能为 null
	 */
	public Foe findEntry(int id) {
		for (int i = 0; i < foes.size; i++) {
			Foe entry = foes.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		for (int i = 0; i < foesWaitingForAdd.size; i++) {
			Foe entry = foesWaitingForAdd.get(i);
			if (entry.id == id) {
				return entry;
			}
		}
		return null;
	}
	
	public void setPlayer1(Foe entry) {
		this.player1 = entry;
		if (entry instanceof IInputBindable) {
			((IInputBindable) entry).bindController(RockChronicle.INSTANCE.input.p1);
		}
		addFoe(entry);
	}
	
	public void addFoe(Foe entry) {
		if (entry != null)
			foesWaitingForAdd.add(entry);
	}
	
	public void removeFoe(Foe entry) {
		if (entry != null)
			foesWaitingForRemove.add(entry);
	}
	
	public void destroyFoeNow(Foe entry) {
		entry.onDispose();
		foes.removeValue(entry, true);
	}
	
	private void handleFoeAddAndRemove() {
		foesWaitingForRemove.forEach((foe) -> foe.onDispose());
		foes.removeAll(foesWaitingForRemove, true);
		foesWaitingForRemove.clear();
		
		if (foesWaitingForAdd.size > 0) {
			Foe[] foeadds = foesWaitingForAdd.toArray(Foe.class);
			
			for (int i = 0; i < foeadds.length; i++) {
				Foe foeadd = foeadds[i];
				foeadd.init(this);
				foes.add(foeadd);
				foesWaitingForAdd.removeValue(foeadd, true);
			}
		}
	}

	public void addBox(Box box) {
		boxes.add(box);
	}

	public void removeBox(Box box) {
		boxes.removeValue(box, true);
	}

	public void addPainter(IFoePainter painter) {
		painters.add(painter);
	}

	public void removePainter(IFoePainter painter) {
		painters.removeValue(painter, true);
	}
	
}
