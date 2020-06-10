package zdream.rockchronicle.core;

import java.util.Comparator;

import zdream.rockchronicle.core.character.CharacterEntry;

/**
 * <p>处理所有 {@link CharacterEntry} 的绘画顺序, 按照绘画顺序对角色进行排序的排序器
 * <p>排序靠前的先画, 其角色将在底层; 反之排序靠后的后画, 其角色将在顶层.
 * 排序的数值由 {@link #entryPriority(CharacterEntry)} 进行定义.
 * <p>排序的顺序参照 {@link CharacterEntry#type} 项, 一般排序的顺序是 (从底层至顶层):
 * <li>场 (field), 贴图 (texture), 陷阱 (trap)
 * <li>小怪 (foe), 重要角色 (leader) (其中控制角色最后画)
 * <li>道具 (mass), 子弹 (bullet)
 * </li>
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-14 (created)
 *   2019-05-14 (last modified)
 */
public class CharacterPaintComparator implements Comparator<CharacterEntry> {
	
	final GameRuntime runtime;

	public CharacterPaintComparator(GameRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public int compare(CharacterEntry o1, CharacterEntry o2) {
		return entryPriority(o2) - entryPriority(o1);
	}
	
	/**
	 * 绘画优先度. 优先度高的先画, 放在底层
	 * @param c
	 * @return
	 *   绘画优先度
	 */
	public int entryPriority(CharacterEntry c) {
		if (c == null) {
			return -1;
		}
		
		switch (c.type) {
		case "field": return 320;
		case "texture": return 310;
		case "trap": return 300;
		
		case "foe": return 250;
		case "leader": {
			if (c.id == runtime.player1) return 201;
			// player2
			return 210;
		}
		
		case "mass": return 10;
		case "bullet": return 0;
		}
		return 500;
	}

}
