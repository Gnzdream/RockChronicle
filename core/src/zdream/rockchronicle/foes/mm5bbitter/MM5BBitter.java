package zdream.rockchronicle.foes.mm5bbitter;

import static zdream.rockchronicle.core.foe.Box.P_PER_BLOCK;
import static zdream.rockchronicle.core.foe.Box.block2P;
import static zdream.rockchronicle.core.world.Ticker.STEPS_PER_SECOND;

import java.util.function.Predicate;

import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;
import zdream.rockchronicle.core.foe.Foe;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.foe.SingleBoxSpritePainter;

/**
 * <p>5 代会藏到土里面的炮台.
 * <p>
 * <li>不移动, 且位置固定的怪
 * <li>原版伤害4 , 这里改成: 探出头的过程中碰撞伤害 4, 等级 2, 其它时段无碰撞伤害.
 * 在除了藏起来的状态 (stage = 0 或 5), 其它状态炮台都是一个实体, 即不能通过.
 * <li>有敌人在面前时探出头打一枪. 速度比原版快一点
 * <li>HP:2 (512)
 * <li>侦测: 敌人到达面前, 高为2 长为5 的方形区域, 从普通模式转为激活模式.
 * 打一枪之后右缩回去, 转回普通模式.
 * 总的过程: 探出头(0.5 秒, 60 步) -> 等待(0.083 秒, 20 步) -> 发子弹
 * -> 等待(0.2 秒, 24 步) -> 缩回去(0.5 秒, 60 步) -> CD(0.5秒, 60 步)
 * <li>子弹: 7.5 块/秒 (4096 p/步)
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-13
 */
public class MM5BBitter extends SimpleBoxFoe {

	public MM5BBitter() {
		super("mm5b_bitter", "foe", (byte) 2);
		
		initBox();
		box.flush();
		
		this.hp = 256 * 2;
//		this.damage = 0; // 不是 4 * 256
		this.damageLevel = 2;
	}
	
	public MM5BBitter(JsonValue data) {
		this();
		
		if (data != null) {
			JsonValue iSpecies = data.get("species");
			if (iSpecies != null) {
				this.species = iSpecies.asByte();
			}
			
			// box
			JsonValue obox = data.get("box");
			if (obox != null) {
				JsonValue v = obox.get("orientation");
				if (v != null) {
					box.orientation = v.asBoolean();
				}
				
				v = obox.get("anchor");
				if (v != null) {
					box.setAnchor(block2P(v.getFloat("x")), block2P(v.getFloat("y")));
				}
			}
		}
	}
	
	private SingleBoxSpritePainter painter;
	
	@Override
	public void init(GameRuntime runtime) {
		super.init(runtime);
		
		String path = (species == 0) ? 
				"res/characters/mm5bbitter/mm5bbitter_sheet_a.json" :
				"res/characters/mm5bbitter/mm5bbitter_sheet_b.json";
		painter = createPainter(new String[] {path});
	}
	
	private ObjectMap<String, int[]> patterns = new ObjectMap<>();
	public String currentPattern;
	
	public void setCurrentPattern(String pattern) {
		int[] ps = patterns.get(pattern);
		
		if (ps != null) {
			currentPattern = pattern;
			
			box.setBox(ps[0], ps[1], ps[2], ps[3]);
		}
	}
	
	private void initBox() {
		patterns.put("normal", new int[] {-24576, 0, 49152, 16384});
		patterns.put("appear1", new int[] {-24576, 0, 49152, 40960});
		patterns.put("appear2", new int[] {-24576, 0, 49152, 76459});
		patterns.put("appear3", new int[] {-24576, 0, 49152, 109227});
		// 没有 4
		patterns.put("appear5", new int[] {-24576, 0, 49152, 95573});

		setCurrentPattern("normal");
	}
	
	/**
	 * 0 - 红色种
	 * 1 - 绿色种
	 */
	byte species;
	/**
	 * stage=0
	 * <br>探出头(0.417 秒, 60 步) stage=1
	 * <br>等待(0.083 秒, 20 步) stage=2
	 * <br>发子弹
	 * <br>等待(0.2 秒, 24 步) stage=3
	 * <br>缩回去(0.417 秒, 60 步) stage=4
	 * <br>CD(0.5秒, 60 步) stage=5
	 */
	int stage;
	/**
	 * 当前状态持续的时间
	 */
	int stageDuration;

	@Override
	protected void stepIfNotPause() {
		box.velocityX = box.velocityY = 0;
		
		switch (stage) {
		case 0: {
			// 计算是否被激活
			Foe enemy = foundEnemy();
			if (enemy != null) {
				// 激活
				stage = 1;
				stageDuration = 0;
				damage = 4 * 256;
				box.terrain = new IntIntMap();
				box.terrain.put(1, 2);
				box.terrain.put(3, 2);
				painter.setState("red_appear");
				setCurrentPattern("appear1");
			}
		} break;
		
		case 1: {
			stageDuration++;
			
			if (stageDuration == 12) {
				setCurrentPattern("appear2");
			} else if (stageDuration == 24) {
				setCurrentPattern("appear3");
			} else if (stageDuration >= 60) {
				setCurrentPattern("appear5");
				stage = 2;
				stageDuration = 0;
				damage = -1;
			}
		} break;
		
		case 2: {
			stageDuration++;
			if (stageDuration >= 20) {
				shot();
				stage = 3;
				stageDuration = 0;
			}
		} break;

		case 3: {
			stageDuration++;
			if (stageDuration >= 24) {
				stage = 4;
				stageDuration = 0;
				painter.setState("red_disappear");
			}
		} break;

		case 4: {
			stageDuration++;
			if (stageDuration == 36) {
				setCurrentPattern("appear2");
			} else if (stageDuration == 48) {
				setCurrentPattern("appear1");
			} else if (stageDuration >= 60) {
				setCurrentPattern("normal");
				stage = 5;
				stageDuration = 0;
				box.terrain = null;
			}
		} break;

		case 5: {
			stageDuration++;
			if (stageDuration >= 60) {
				stage = 0;
				stageDuration = 0;
			}
		} break;
		
		default:
			break;
		}
	}

	private Foe foundEnemy() {
		FindEnemy ch = new FindEnemy();
		ch.runtime = runtime;
		ch.camp = camp;
		
		int px = (box.orientation) ?
				box.posX : box.posX + box.posWidth - 5 * P_PER_BLOCK;
		
		runtime.world.overlaps(
				px, box.getCenterY() - P_PER_BLOCK,
				5 * P_PER_BLOCK, 2 * P_PER_BLOCK, ch, true, null);
		
		return ch.targetFoe;
	}
	
	static class FindEnemy implements Predicate<Box> {
		
		Foe targetFoe = null;
		GameRuntime runtime;
		byte camp;

		@Override
		public boolean test(Box box) {
			Foe foe = runtime.findEntry(box.id);
			
			if (foe.camp == this.camp) {
				return true;
			}
			
			switch (foe.type) {
			case "leader": case "foe": case "elite":
				targetFoe = foe;
				return false;

			default:
				return true;
			}
		}
		
	}
	
	private void shot() {
		MM2BBitterBullet bullet = new MM2BBitterBullet();
		
		int bulletX, bulletY = box.anchorY + (int) (1.041667f * P_PER_BLOCK), vx;
		
		if (box.orientation) {
			bulletX = box.anchorX + (int) (0.833333f * P_PER_BLOCK);
			vx = (int) (7.5f * P_PER_BLOCK / STEPS_PER_SECOND);
		} else {
			bulletX = box.anchorX - (int) (0.833333f * P_PER_BLOCK);
			vx = (int) (-7.5f * P_PER_BLOCK / STEPS_PER_SECOND);
		}
		
		bullet.set(bulletX, bulletY, vx);
		runtime.addFoe(bullet);
	}

}
