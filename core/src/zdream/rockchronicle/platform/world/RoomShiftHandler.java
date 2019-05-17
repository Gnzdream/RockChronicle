package zdream.rockchronicle.platform.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.GameRuntime;
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
		runtime.entries.forEach(ch -> {
			if (ch.id == runtime.player1) {
				param.entries.add(ch);
			}
		});
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
				} else {
					param.phase2Pos.set(destRoom.offsetx + destRoom.width - camera.viewportWidth - offsetx, dy);
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
			shiftPhase1();
			break;
		case 2:
			shiftPhase2();
			break;
		case 3:
			// 后续
			System.out.println("finished");
			break;

		default:
			throw new IllegalStateException("移屏阶段数据出错: " + param.phase);
		}
	}

	/*
	 * 镜头移动
	 * 移屏速度现在是定死的, 每秒 40 格 (每步 0.333333 格).
	 */
	private void shiftPhase1() {
		if (doShiftRoom(param.phase1Pos)) {
			param.phase = 2;
		}
	}
	
	/**
	 * 阶段二: 镜头移动 + 角色移动
	 */
	private void shiftPhase2() {
		if (doShiftRoom(param.phase2Pos)) {
			param.phase = 3; // 移屏全部结束, 需要设置新的房间
		}
	}
	
	private boolean doShiftRoom(Vector2 toPos) {
		boolean bx = param.currentPos.x != toPos.x;
		float delta = bx ? toPos.x - param.currentPos.x : toPos.y - param.currentPos.y;
		float step = 2.0f/3; // 上面的每秒 40 格
		boolean finished = false;
		System.out.println(delta);
		
		if (delta > 0) {
			if (delta < step) {
				param.currentPos.set(toPos);
				finished = true;
			} else if (bx) {
				param.currentPos.x += step;
			} else {
				param.currentPos.y += step;
			}
		} else if (delta < 0) {
			if (delta > -step) {
				param.currentPos.set(toPos);
				finished = true;
			} else if (bx) {
				param.currentPos.x -= step;
			} else {
				param.currentPos.y -= step;
			}
		} else {
			// 该阶段直接结束
			finished = true;
		}
		
		// 更新镜头位置
		RockChronicle app = RockChronicle.INSTANCE;
		OrthographicCamera camera = app.runtime.worldCamera;
		camera.position.x = app.width / 2.0f + param.currentPos.x;
		camera.position.y = app.height / 2.0f + param.currentPos.y;
		
		return finished;
	}
	
	/**
	 * 询问现在是否在房间切换中
	 * @return
	 */
	public boolean durationShift() {
		return param != null;
	}

}
