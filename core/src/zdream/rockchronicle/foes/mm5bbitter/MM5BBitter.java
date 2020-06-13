package zdream.rockchronicle.foes.mm5bbitter;

import static zdream.rockchronicle.core.foe.Box.block2P;

import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.SimpleBoxFoe;
import zdream.rockchronicle.core.foe.SingleBoxSpritePainter;

/**
 * <p>5 代会藏到土里面的炮台.
 * <p>
 * <li>不移动, 且位置固定的怪
 * <li>这里改成: 无碰撞伤害 (原版伤害4), 有敌人在面前时探出头打一枪. 速度比原版快 33%
 * <li>HP:1, 碰撞伤害:3, 碰撞等级:1
 * <li>侦测: 敌人到达横纵坐标差为 10 块时把蛋放下, 仅触发一次
 * </li></p>
 * 
 * @author Zdream
 * @date 2020-06-13
 */
public class MM5BBitter extends SimpleBoxFoe {

	public MM5BBitter() {
		super("mm5b_bitter", "foe", (byte) 2);
		
		box.setBox(-24576, 0, 49152, 16384); // 锚点在下底的中点
		box.flush();
		
		this.hp = 256 * 2;
//		this.damage = 0; // 不是 4 * 256
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
	
	/**
	 * 0 - 红色种
	 * 1 - 绿色种
	 */
	byte species;

	@Override
	protected void stepIfNotPause() {
		// TODO Auto-generated method stub

	}

}
