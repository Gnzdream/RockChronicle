package zdream.rockchronicle.foes.megaman;

/**
 * 洛克人的武器
 * 
 * @author Zdream
 * @date 2020-06-10
 */
public interface IMegamanWeapon {
	
	default void onAttackPressed(Megaman mm) {
		
	}
	
	default void onAttackReleased(Megaman mm) {
		
	}
	
	/**
	 * 比如在蓄力途中换武器, 就要调用它
	 */
	default void onAttackCanceled(Megaman mm) {
		
	}
	
	void doFire(Megaman mm);
	
	/**
	 * 这个就算不攻击, 每帧也调用一次. 在 onAttack* 之前调用.
	 * @param mm
	 */
	default void tick(Megaman mm) {}

}
