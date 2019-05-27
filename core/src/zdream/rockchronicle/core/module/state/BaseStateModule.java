package zdream.rockchronicle.core.module.state;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>一般角色的状态记录的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-27 (last modified)
 */
public class BaseStateModule extends StateModule {

	public BaseStateModule(CharacterEntry ch) {
		super(ch);
	}
	
	/**
	 * 硬直剩余时间 (步), 临时
	 */
	public int stiffnessRemain;
	
	/**
	 * 硬直默认持续时间 (步), 长期
	 */
	public int stiffnessDuration;
	
	/**
	 * 无敌剩余时间 (步), 临时
	 */
	public int immuneRemain;
	
	/**
	 * 无敌默认持续时间 (步), 长期
	 */
	public int immuneDuration;
	
	/**
	 * <p>行动状态, 状态项, 包含但不限于: "walk", "stop", 临时
	 * <p>补充:
	 * <li>攀爬状态: "climb", "climbTop1", "climbTop2"
	 * </li>
	 */
	public String motion;
	
	/**
	 * 是否朝右, 长期
	 */
	public boolean orientation = true;
	
	/**
	 * 本步是否发出过攻击动作, 临时
	 */
	public boolean attacking;
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);

		initStateArguments(value);

		parent.addSubscribe("after_damage", this);
		parent.addSubscribe("open_fire", this);
	}

	private void initStateArguments(JsonValue value) {
		JsonValue ostate = value.get("state");
		if (ostate == null) {
			return;
		}
		JsonValue oparam = ostate.get("param");
		if (oparam == null) {
			return;
		}
		
		stiffnessDuration = (int) (oparam.getFloat("stiffness", 0) * LevelWorld.STEPS_PER_SECOND);
		immuneDuration = (int) (oparam.getFloat("immune", 0) * LevelWorld.STEPS_PER_SECOND);
		
		setSituation();
	}
	
	private void setBaseState() {
		setState("state.stiffness", new JsonValue(stiffnessRemain));
		setState("state.immune", new JsonValue(immuneRemain));
		setState("state.motion", new JsonValue(motion));
		setState("state.attacking", new JsonValue(attacking));
	}
	
	private void setSituation() {
		setSituation("state.param.stiffness", new JsonValue(stiffnessDuration));
		setSituation("state.param.immune", new JsonValue(immuneDuration));
		setSituation("state.param.orientation", new JsonValue(orientation));
	}
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		if (this.stiffnessRemain > 0) {
			this.stiffnessRemain--;
		}
		if (this.immuneRemain > 0) {
			this.immuneRemain--;
		}
		
		setBaseState();
	}
	
	@Override
	public void stepPassed() {
		this.attacking = false;
		this.motion = "stop";
		
		super.stepPassed();
	}
	
	/*
	 * 接收事件, after_damage. 当判定伤害来自外部, 设置无敌与僵直参数
	 */
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("after_damage".equals(event.name)) {
			if ("accepted".equals(event.value.getString("result"))) {
				// TODO 判定伤害来自外部的逻辑, 现在缺失
			
				int damage = event.value.getInt("damage");
				if (this.stiffnessDuration > 0 && damage > 0) {
					this.stiffnessRemain = stiffnessDuration;
				}
				if (this.immuneDuration > 0) {
					this.immuneRemain = immuneDuration;
				}
				setBaseState();
			}
		} else if ("open_fire".equals(event.name)) {
			this.attacking = true;
			setBaseState();
		}
		super.receiveEvent(event);
	}

}
