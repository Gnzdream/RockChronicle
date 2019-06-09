package zdream.rockchronicle.core.module.camp;

import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * 默认的阵营模块
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-07 (create)
 */
public class BaseCampModule extends CampModule {

	public BaseCampModule(CharacterEntry ch) {
		super(ch, "base");
	}
	
	public static final int[][] defaultAcceptedTable = {
			{0, 1, 1},
			{0, 0, 1},
			{0, 1, 0}
	};
	
	@Override
	public void setCamp(int camp) {
		this.camp = camp;
		if (camp <= 2 && camp >= 0) {
			for (int i = 0; i <= 2; i++) {
				this.attackTable.put(i, defaultAcceptedTable[camp][i]);
				this.defenseTable.put(i, defaultAcceptedTable[i][camp]);
			}
		}
	}
	
	public boolean attackAccepted(int othersCamp) {
		return attackTable.get(othersCamp, 1) == 1;
	}
	
	public boolean defenseAccepted(int othersCamp) {
		return defenseTable.get(othersCamp, 1) == 1;
	}
}
