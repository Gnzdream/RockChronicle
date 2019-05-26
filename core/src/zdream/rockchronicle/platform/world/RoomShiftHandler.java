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

import static zdream.rockchronicle.platform.region.Gate.*;

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
	
	/**
	 * <p>切换房间的额外判定
	 * <p>已经满足有大门的条件了, 还需要判定:
	 * <p>如果顺着重力及其它环境力的合力方向，碰到边缘就可以切房间；
	 * 如果逆着合力方向，就需要额外判定，比如需要在攀爬状态、踩在指定的角色上等
	 * </p>
	 * @param entry
	 * @param g
	 * @return
	 */
	public boolean checkShift(CharacterEntry entry, Gate g) {
		if (g.direction == DIRECTION_LEFT || g.direction == DIRECTION_RIGHT) {
			return true;
		}
		
		Box box = entry.getBoxModule().getBox();
		boolean gravityDown = box.gravityDown && box.gravityScale > 0
				|| !box.gravityDown && box.gravityScale < 0;
		
		if (gravityDown && g.direction == DIRECTION_BOTTOM
				|| !gravityDown && g.direction == DIRECTION_TOP) {
			// 顺着重力（合力）往下掉的
			return true;
		}
		
		// TODO 这里判断比如需要在攀爬状态、踩在指定的角色上等
		if (entry.getBoolean("climb.climbing", false)) {
			return true;
		}
		
		return false;
	}
	
	public void doShift(Gate g) {
		param = new RoomShiftParam();
		param.gate = g;
		
		GameRuntime runtime = RockChronicle.INSTANCE.runtime;
		param.entries.add(runtime.getPlayer1());
		param.entriesPos = new Vector2[param.entries.size];
		param.phase2EntryWidth = new float[param.entries.size];
		
		runtime.levelWorld.doPause();
		countParam(g, runtime);
		
		param.phase = 1;
	}
	
	/**
	 * <p>确定移屏的相关参数. 移屏都是摄像机在动.
	 * <p>移屏, 分两个阶段; 第一是摄像机在原房间摆正, 第二是摄像机从原房间移向目标房间.
	 * 如果关卡设计合理的话, 是没有第一阶段的.
	 * </p>
	 * @param gates
	 */
	private void countParam(Gate g, GameRuntime runtime) {
		RockChronicle app = RockChronicle.INSTANCE;
		Room srcRoom = g.srcRoom, destRoom = g.destRoom;
		
		// TODO 如果 destRoom 来自其它的区域, 还需要修改数据
		
		OrthographicCamera camera = runtime.scene.camera;
		Vector2 pos = new Vector2(camera.position.x - app.width / 2.0f,
				camera.position.y - app.height / 2.0f); // 相对于当前房间
		param.currentPos.set(pos);
		switch (g.direction) {
		case DIRECTION_LEFT: 
		case DIRECTION_RIGHT: {
			
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
				
				int y2start = destRoom.offsety + g.offsetYOfRegion - offsety,
						y2end = y2start + destRoom.height - (int) camera.viewportHeight;
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
				if (g.direction == DIRECTION_RIGHT) {
					param.phase2Pos.set(destRoom.offsetx - g.offsetXOfRegion - offsetx, dy);
					param.phase2CameraWidth = param.phase2Pos.x - param.phase1Pos.x;
					
					for (int i = 0; i < param.entries.size; i++) {
						CharacterEntry entry = param.entries.get(i);
						Box box = entry.getBoxModule().getBox();
						Rectangle rect = box.getPosition();
						float xleft = rect.x;
						Vector2 anchor = box.anchor;
						
						// 向右移屏后, 角色左边需要等于 1
						param.phase2EntryWidth[i] = destRoom.offsetx - g.offsetXOfRegion - srcRoom.offsetx + 1 - xleft;
						param.entriesPos[i] = new Vector2(anchor.x + param.phase2EntryWidth[i], anchor.y);
					}
				} else {
					param.phase2Pos.set(destRoom.offsetx - g.offsetXOfRegion + destRoom.width
							- camera.viewportWidth - offsetx, dy);
					param.phase2CameraWidth = param.phase1Pos.x - param.phase2Pos.x;
					
					for (int i = 0; i < param.entries.size; i++) {
						CharacterEntry entry = param.entries.get(i);
						Box box = entry.getBoxModule().getBox();
						Rectangle rect = box.getPosition();
						float xright = rect.x + rect.width; // 相对于 srcRoom
						Vector2 anchor = box.anchor;

						// 向左移屏后, 角色右边需要等于目标房间的宽 - 1
						param.phase2EntryWidth[i] = (srcRoom.offsetx + xright)
								- (destRoom.offsetx - g.offsetXOfRegion + destRoom.width - 1);
						param.entriesPos[i] = new Vector2(anchor.x - param.phase2EntryWidth[i], anchor.y);
					}
				}
			}
		} break;
		
		case DIRECTION_BOTTOM:
		case DIRECTION_TOP: {
			float dx;
			PHASE1 : { // 阶段一: 定 x 轴
				// 下面计算镜头的框的底边纵坐标的范围
				int offsetx = srcRoom.offsetx;
				int x1start = 0,
						x1end = x1start + srcRoom.width - (int) camera.viewportWidth;
				if (x1start == x1end) {
					param.phase1Pos.set(dx = x1start, pos.y);
					break PHASE1;
				}
				
				int x2start = destRoom.offsetx + g.offsetXOfRegion - offsetx,
						x2end = x2start + destRoom.height - (int) camera.viewportWidth;
				if (x2start == x2end) {
					param.phase1Pos.set(dx = x2start, pos.y);
					break PHASE1;
				}
				
				if (pos.x >= x1start && pos.x <= x1end && pos.x >= x2start && pos.x <= x2end) {
					param.phase1Pos.set(dx = pos.x, pos.y);
					break PHASE1;
				}
				if (pos.x < x1start || pos.x < x2start) {
					param.phase1Pos.set(dx = Math.max(x1start, x2start), pos.y);
					break PHASE1;
				}
				if (pos.x > x1end || pos.x > x2end) {
					param.phase1Pos.set(dx = Math.min(x1end, x2end), pos.y);
					break PHASE1;
				}
				// 如果运行到这里, 那就是出错了
				throw new IllegalStateException("移屏计算出错: 阶段一");
			}
			
			{ // 阶段二: 定 y 轴
				int offsety = srcRoom.offsety;
				if (g.direction == DIRECTION_BOTTOM) { // 向下
					param.phase2Pos.set(dx, destRoom.offsety - g.offsetYOfRegion
							+ destRoom.height - camera.viewportHeight - offsety);
					param.phase2CameraWidth = param.phase1Pos.y - param.phase2Pos.y;
					
					for (int i = 0; i < param.entries.size; i++) {
						CharacterEntry entry = param.entries.get(i);
						Box box = entry.getBoxModule().getBox();
						Rectangle rect = box.getPosition();
						float ybottom = rect.y;
						Vector2 anchor = box.anchor;
						
						// 向下移屏后, 角色上边需要等于目标房间高 - 0.1
						param.phase2EntryWidth[i] = (srcRoom.offsety + ybottom)
								- (destRoom.offsety - g.offsetYOfRegion + destRoom.height - 0.1f - rect.height);
						param.entriesPos[i] = new Vector2(anchor.x, anchor.y - param.phase2EntryWidth[i]);
					}
				} else {
					param.phase2Pos.set(dx, destRoom.offsety - g.offsetYOfRegion - offsety);
					param.phase2CameraWidth = param.phase2Pos.y - param.phase1Pos.y;
					
					for (int i = 0; i < param.entries.size; i++) {
						CharacterEntry entry = param.entries.get(i);
						Box box = entry.getBoxModule().getBox();
						Rectangle rect = box.getPosition();
						float ybottom = rect.y; // 相对于 srcRoom
						Vector2 anchor = box.anchor;

						// 向上移屏后, 角色下边需要等于 0.1
						param.phase2EntryWidth[i] = srcRoom.height + 0.1f - ybottom;
						param.entriesPos[i] = new Vector2(anchor.x, anchor.y + param.phase2EntryWidth[i]);
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
		OrthographicCamera camera = app.runtime.scene.camera;
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
		
		Room srcRoom = param.gate.srcRoom;
		Room destRoom = param.gate.destRoom;
		runtime.setRoom(destRoom);
		
		// 设置角色位置
		int deltax = destRoom.offsetx - srcRoom.offsetx;
		int deltay = destRoom.offsety - srcRoom.offsety;
		if (srcRoom.region == destRoom.region) {
			for (int i = 0; i < param.entries.size; i++) {
				CharacterEntry entry = param.entries.get(i);
				Box box = entry.getBoxModule().getBox();
				box.addAnchorX(-deltax);
				box.addAnchorY(-deltay);
			}
		} else {
			Gate gate = param.gate;
			for (int i = 0; i < param.entries.size; i++) {
				CharacterEntry entry = param.entries.get(i);
				Box box = entry.getBoxModule().getBox();
				box.addAnchorX(-deltax + gate.offsetXOfRegion);
				box.addAnchorY(-deltay + gate.offsetYOfRegion);
			}
		}
		
		// 镜头位置的更新将交给 SceneDesigner
//		OrthographicCamera camera = app.runtime.scene.camera;
//		camera.position.x = app.width / 2.0f;
//		camera.position.y = app.height / 2.0f;
		
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
