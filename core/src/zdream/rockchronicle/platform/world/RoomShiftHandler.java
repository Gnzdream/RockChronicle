package zdream.rockchronicle.platform.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntSet;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.region.Gate;
import zdream.rockchronicle.platform.region.Room;

/**
 * <p>房间切换处理方
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-17 (created)
 *   2019-05-17 (last modified)
 */
public class RoomShiftHandler {
	
	public RoomShiftParam param;
	
	public void doShift(Gate[] gates) {
		Gate g = gates[0];
		param = new RoomShiftParam();
		param.srcRoom = g.srcRoom;
		param.destRoom = g.destRoom;
		
		GameRuntime runtime = RockChronicle.INSTANCE.runtime;
		param.entries.add(runtime.getPlayer1());
		param.entriesPos = new Vector2[param.entries.size];
		param.phase2EntryWidth = new float[param.entries.size];
		
		runtime.levelWorld.doPause();
		countParam(gates, runtime);
		
		param.phase = 1;
	}
	
	/**
	 * <p>确定移屏的相关参数. 移屏都是摄像机在动.
	 * <p>移屏, 分两个阶段; 第一是摄像机在原房间摆正, 第二是摄像机从原房间移向目标房间.
	 * 如果关卡设计合理的话, 是没有第一阶段的.
	 * </p>
	 * @param gates
	 */
	private void countParam(Gate[] gates, GameRuntime runtime) {
		RockChronicle app = RockChronicle.INSTANCE;
		Room srcRoom = runtime.curRegion.rooms[param.srcRoom],
				destRoom = runtime.curRegion.rooms[param.destRoom];
		
		// TODO 如果 destRoom 来自其它的区域, 还需要修改数据
		
		OrthographicCamera camera = runtime.worldCamera;
		Vector2 pos = new Vector2(camera.position.x - app.width / 2.0f,
				camera.position.y - app.height / 2.0f); // 相对于当前房间
		param.currentPos.set(pos);
		switch (gates[0].direction) {
		case Gate.DIRECTION_LEFT: 
		case Gate.DIRECTION_RIGHT: {
			
			float dy;
			PHASE1 : { // 阶段一: 定 y 轴
				// 下面计算镜头的框的底边纵坐标的范围
				int offsety = srcRoom.offsety;
				int y1start = 0,
						y1end = y1start + srcRoom.height - (int) camera.viewportHeight;
				if (y1start == y1end) {
					param.phase1Pos.set(pos.x, dy = y1start);
					break PHASE1;
				}
				
				int y2start = destRoom.offsety - offsety,
						y2end = y2start + destRoom.height - (int) camera.viewportHeight - offsety;
				if (y2start == y2end) {
					param.phase1Pos.set(pos.x, dy = y2start);
					break PHASE1;
				}
				
				if (pos.y >= y1start && pos.y <= y1end && pos.y >= y2start && pos.y <= y2end) {
					param.phase1Pos.set(pos.x, dy = pos.y);
					break PHASE1;
				}
				if (pos.y < y1start || pos.y < y2start) {
					param.phase1Pos.set(pos.x, dy = Math.max(y1start, y2start));
					break PHASE1;
				}
				if (pos.y > y1end || pos.y > y2end) {
					param.phase1Pos.set(pos.x, dy = Math.min(y1end, y2end));
					break PHASE1;
				}
				// 如果运行到这里, 那就是出错了
				throw new IllegalStateException("移屏计算出错: 阶段一");
			}
			
			{ // 阶段二: 定 x 轴
				int offsetx = srcRoom.offsetx;
				if (gates[0].direction == Gate.DIRECTION_RIGHT) {
					param.phase2Pos.set(destRoom.offsetx - offsetx, dy);
					param.phase2CameraWidth = destRoom.offsetx - offsetx;
					
					for (int i = 0; i < param.entries.size; i++) {
						CharacterEntry entry = param.entries.get(i);
						Box box = entry.getBoxModule().getBox();
						Rectangle rect = box.getPosition();
						float xleft = rect.x;
						Vector2 anchor = box.anchor;
						
						param.phase2EntryWidth[i] = destRoom.offsetx - srcRoom.offsetx + gates[0].tox - xleft;
						param.entriesPos[i] = new Vector2(anchor.x + param.phase2EntryWidth[i], anchor.y);
					}
				} else {
					param.phase2Pos.set(destRoom.offsetx + destRoom.width - camera.viewportWidth - offsetx, dy);
					param.phase2CameraWidth = offsetx - destRoom.offsetx;
					
					for (int i = 0; i < param.entries.size; i++) {
						CharacterEntry entry = param.entries.get(i);
						Box box = entry.getBoxModule().getBox();
						Rectangle rect = box.getPosition();
						float xright = rect.x + rect.width; // 相对于 srcRoom
						Vector2 anchor = box.anchor;
						
						param.phase2EntryWidth[i] = (srcRoom.offsetx + xright) - (destRoom.offsetx + gates[0].tox + 1);
						param.entriesPos[i] = new Vector2(anchor.x - param.phase2EntryWidth[i], anchor.y);
					}
				}
			}
		} break;
			// TODO 向上下移屏的暂时不完成
		default:
			break;
		}
	}

	public void tickShift(float deltaTime) {
		switch (param.phase) {
		case 1:
			shiftPhase1(deltaTime);
			break;
		case 2:
			shiftPhase2(deltaTime);
			break;
		case 3:
			// 后续
			shiftPhase3();
			break;

		default:
			throw new IllegalStateException("移屏阶段数据出错: " + param.phase);
		}
	}

	/*
	 * 镜头移动
	 * 移屏速度现在是定死的, 每秒 40 格 (每步 0.333333 格).
	 */
	private void shiftPhase1(float deltaTime) {
		if (doShiftRoom(param.phase1Pos, deltaTime)) {
			param.phase = 2;
		}
	}
	
	/**
	 * 阶段二: 镜头移动 + 角色移动
	 */
	private void shiftPhase2(float deltaTime) {
		if (doShiftRoom(param.phase2Pos, deltaTime)) {
			param.phase = 3; // 移屏全部结束, 需要设置新的房间
		}
	}
	
	private boolean doShiftRoom(Vector2 toPos, float deltaTime) {
		boolean bx = param.currentPos.x != toPos.x;
		float delta = bx ? toPos.x - param.currentPos.x : toPos.y - param.currentPos.y;
		float step = 25 * deltaTime; // 上面的每秒 25 格
		boolean finished = false;
		
		if (delta > 0) {
			if (delta < step) {
				param.currentPos.set(toPos);
				finished = true;
				delta = 0;
			} else {
				if (bx) {
					param.currentPos.x += step;
				} else {
					param.currentPos.y += step;
				}
				delta += step;
			}
		} else if (delta < 0) {
			if (delta > -step) {
				param.currentPos.set(toPos);
				finished = true;
			} else {
				if (bx) {
					param.currentPos.x -= step;
				} else {
					param.currentPos.y -= step;
				}
				delta -= step;
			}
		} else {
			// 该阶段直接结束
			finished = true;
		}
		
		// 更新角色位置
		if (param.phase == 2) {
			float remain = Math.abs(delta) / param.phase2CameraWidth;
			for (int i = 0; i < param.entries.size; i++) {
				CharacterEntry entry = param.entries.get(i);
				Box box = entry.getBoxModule().getBox();
				
				if (bx) {
					if (delta >= 0) {
						box.setAnchorX(param.entriesPos[i].x - remain * param.phase2EntryWidth[i]);
					} else {
						box.setAnchorX(param.entriesPos[i].x + remain * param.phase2EntryWidth[i]);
					}
				} else {
					if (delta >= 0) {
						box.setAnchorY(param.entriesPos[i].y - remain * param.phase2EntryWidth[i]);
					} else {
						box.setAnchorY(param.entriesPos[i].y + remain * param.phase2EntryWidth[i]);
					}
				}
			}
		}
		
		
		// 更新镜头位置
		RockChronicle app = RockChronicle.INSTANCE;
		OrthographicCamera camera = app.runtime.worldCamera;
		camera.position.x = app.width / 2.0f + param.currentPos.x;
		camera.position.y = app.height / 2.0f + param.currentPos.y;
		
		return finished;
	}

	private void shiftPhase3() {
		RockChronicle app = RockChronicle.INSTANCE;
		GameRuntime runtime = app.runtime;
		
		// 清空 entry
		IntSet set = new IntSet(param.entries.size);
		param.entries.forEach(ch -> set.add(ch.id));
		runtime.entries.forEach(ch -> {
			if (!set.contains(ch.id)) {
				ch.willDestroy();
			}
		});
		runtime.handleAddAndRemove();
		
		runtime.setRoom(param.destRoom);
		Room srcRoom = runtime.curRegion.rooms[param.srcRoom];
		Room destRoom = runtime.curRegion.rooms[param.destRoom];
		
		int deltax = destRoom.offsetx - srcRoom.offsetx;
		int deltay = destRoom.offsety - srcRoom.offsety;
		
		// 设置角色位置
		for (int i = 0; i < param.entries.size; i++) {
			CharacterEntry entry = param.entries.get(i);
			Box box = entry.getBoxModule().getBox();
			box.addAnchorX(-deltax);
			box.addAnchorY(-deltay);
		}
		
		// 更新镜头位置
		OrthographicCamera camera = app.runtime.worldCamera;
		camera.position.x = app.width / 2.0f;
		camera.position.y = app.height / 2.0f;
		
		// 结束
		runtime.levelWorld.doResume();
		this.param = null;
	}
	
	/**
	 * 询问现在是否在房间切换中
	 * @return
	 */
	public boolean durationShift() {
		return param != null;
	}

}
