package zdream.rockchronicle.foes.megaman;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.Box;

/**
 * <p>洛克人白弹武器 (不是子弹这个 Foe)
 * <p>攻击规则:
 * 1. 屏幕上房间内只允许出现最多 3 个子弹. ( 房间外的不算, 屏幕外房间内的算 )
 * 2. 洛克人不在受伤硬直状态
 * 3. 洛克人不在滑铲状态 (注, 这里特殊, 是 slideDuration > 0 不让攻击)
 * <p>蓄力规则:
 * 1. 算洛 6 的蓄力弹
 * 2. 当受伤硬直时, 蓄力消失 ?
 * 3. 滑铲状态可以蓄力
 * </p>
 * 
 * @author Zdream
 * @date 2020-06-10
 */
public class BusterWeapon implements IMegamanWeapon {
	
	MegamanBuster[] slots = new MegamanBuster[3];
	
	final GameRuntime runtime;
	public BusterWeapon(GameRuntime runtime) {
		this.runtime = runtime;
	}
	
	/**
	 * 已蓄力的时间
	 */
	int restoreDuration = -1;
	
	@Override
	public boolean onAttackPressed(Megaman mm) {
		// 蓄力
		restoreDuration = 0;
		return false;
	}
	
	@Override
	public boolean onAttackReleased(Megaman mm) {
		if (hasEmptySlot() && mm.slideDuration < 0 && mm.stiffness == 0) {
			doFire(mm);
			return true;
		}
		return false;
	}
	
	public boolean canFire(Megaman mm) {
		if (!hasEmptySlot()) {
			return false;
		}
		
		if (mm.stiffness > 0 || mm.slideDuration > 0) {
			return false;
		}
		
		// 测一个问题 | 发出 3 个子弹后, 能否蓄力
		// 现在是蓄力可以, 但是不给你加蓄力时长. 如果 3 个子弹有消失自毁的, 可以加蓄力时长.
		
		return true;
	}

	@Override
	public void doFire(Megaman mm) {
		// TODO Auto-generated method stub
		// new 一个 MegamanBuster
		MegamanBuster b = new MegamanBuster();
		runtime.addFoe(b);
		
		int slot = (slots[0] == null) ? 0 :
			(slots[1] == null) ? 1 : 2;
		slots[slot] = b;
		
		// 子弹的方向、位置
		// y 是洛克人往上 0.75 块的位置
		// x 是洛克人面朝方向 1 块的位置
		Box busterBox = b.box;
		Box megamanBox = mm.box;
		busterBox.setAnchor(
				(megamanBox.orientation) ? megamanBox.anchorX + 65536 : megamanBox.anchorX - 65536,
				megamanBox.anchorY + 49152);
		busterBox.orientation = megamanBox.orientation;
		busterBox.flush();
		
		restoreDuration = -1;
	}
	
	/**
	 * 上面三个子弹的槽有没有空的
	 */
	private boolean hasEmptySlot() {
		return (slots[0] == null || slots[1] == null || slots[2] == null);
	}
	
	@Override
	public void tick(Megaman mm) {
		if (restoreDuration >= 0 && hasEmptySlot()) {
			restoreDuration++;
		}
		
		for (int i = 0; i < slots.length; i++) {
			if (slots[i] == null) {
				continue;
			}
			if (slots[i].isDisposed()) {
				slots[i] = null; // 留一个空位
			}
		}
	}

}
