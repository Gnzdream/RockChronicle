package zdream.rockchronicle.character;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * 动作与碰撞检测模块. 暂时只处理洛克人的
 * @author Zdream
 */
public abstract class MotionModule extends AbstractModule {
	
	public static final String NAME = "Motion";
	
	/*
	 * 定义的运动:
	 * 
	 * normal / stand  站立
	 * turn left  朝向左
	 * turn right  朝向右
	 * move left
	 * move right
	 */
	
	/**
	 * 朝向,
	 * true: 右
	 * false: 左
	 */
	protected boolean orientation = true;
	
	/**
	 * 在碰撞盒子中记录的, 自己的 ID
	 */
	protected int collisionId;
	
	/*
	 * 新补充数据
	 */
	protected Shape shape;
	

	public MotionModule(CharacterEntry ch) {
		super(ch);
	}

	@Override
	public String name() {
		return NAME;
	}

	public abstract void initCollideRect(JsonValue rectArray);
	
	/**
	 * 收到从控制端 (一般直接从 {@link ControlModule}) 中的消息, 更改这个人物的状态.
	 * @param infos
	 *   消息列表
	 */
	public abstract void recvControl(String[] infos);

	public abstract void createBody(LevelWorld world);
	
}
