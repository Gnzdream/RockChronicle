package zdream.rockchronicle.sprite.character.megaman;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.CharacterParameter;
import zdream.rockchronicle.core.module.weapon.WeaponModule;
import zdream.rockchronicle.platform.body.Box;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.sprite.bullet.base.MMBuster;

public class MegamanWeaponModule extends WeaponModule {
	
	MegamanInLevel parent;
	
	/**
	 * 攻击暂存. attackBegin 为暂存, inAttack 为状态
	 */
	boolean attackBegin, inAttack;
	
	/*
	 * 临时参数, 包括武器参数 (暂存)
	 */
	public IntArray weaponEntryIds = new IntArray(8);

	public MegamanWeaponModule(MegamanInLevel parent) {
		super(parent);
		this.parent = parent;
	}
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		parent.addSubscribe("ctrl_motion", this);
	}
	
	@Override
	public int priority() {
		return 20;
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		handleAttack(world, index, hasNext);
	}
	
	private void handleAttack(LevelWorld world, int index, boolean hasNext) {
		Box box = getSingleBox();
		
		boolean stiffness = parent.getBoolean(new String[] {"state", "stiffness"}, false);
		
		// 其它 : 是否攻击
		if (weaponEntryIds.size < 3 && attackBegin && !stiffness) {
			boolean climbing = parent.getBoolean(new String[] {"climb", "climbing"}, false);
			
			// 计算朝向
			boolean orientation = parent.getBoolean(new String[] {"situation", "orientation"}, true); // true : 向右
			if (climbing) {
				boolean left = parent.ctrl.lastLeft;
				boolean right = parent.ctrl.lastRight;
				
				if (orientation && left || !orientation && right) {
					// 转向
					orientation = right;
					JsonValue v = new JsonValue(ValueType.object);
					v.addChild("orientation", new JsonValue(right));
					parent.setJson("situation", v);
				}
			}
			
			float x = (orientation) ? box.anchor.x + 1 : box.anchor.x - 1;
			MMBuster buster = (MMBuster) RockChronicle.INSTANCE.runtime.characterBuilder.create("megaman_buster",
					CharacterParameter.newInstance()
						.setBoxAnchor(x, box.anchor.y + 0.85f)
						.setMotionOrientation(orientation)
						.setMotionFlipX(!orientation)
						.setCamp(parent.getInt(new String[] {"camp", "camp"}, 0))
						.get());
			weaponEntryIds.add(buster.id);
			RockChronicle.INSTANCE.runtime.addEntry(buster);
			
			parent.publish(new CharacterEvent("open_fire")); // 暂时没有附加信息
		}
	}
	
	@Override
	public void stepPassed() {
		super.stepPassed();
		
		// 重置参数
		this.attackBegin = false;
		
		// 子弹数重置
		if (weaponEntryIds.size > 0) {
			GameRuntime runtime = RockChronicle.INSTANCE.runtime;
			for (int i = 0; i < weaponEntryIds.size; i++) { // weaponEntryIds.size 是变动的
				int id = weaponEntryIds.get(i);
				// 检查子弹是否存在. 后面还需要补充它是否弹开等描述子弹是否还有效的属性
				
				CharacterEntry entry = runtime.findEntry(id);
				if (entry == null) entry = runtime.findEntryWaitingForAdd(id);
				
				if (entry == null) {
					weaponEntryIds.removeIndex(i);
					i--;
				}
			}
		}
	}
	
	/**
	 * 如果本角色只有一个碰撞盒子, 则调用该方法来获取其碰撞盒子
	 * @return
	 */
	protected Box getSingleBox() {
		return parent.getBoxModule().getBox();
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		switch (event.name) {
		case "ctrl_motion":
			recvCtrlMotion(event);
			break;

		default:
			super.receiveEvent(event);
			break;
		}
	}
	
	private void recvCtrlMotion(CharacterEvent event) {
		inAttack = event.value.getBoolean("attack");
		boolean attackChange = event.value.getBoolean("attackChange");
//		boolean slide = event.value.getBoolean("slide");
//		boolean slideChange = event.value.getBoolean("slideChange");
		
		attackBegin = (inAttack && attackChange);
	}

}