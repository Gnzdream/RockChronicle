package zdream.rockchronicle.core.module.state;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.JsonCollector;
import zdream.rockchronicle.platform.world.LevelWorld;

/**
 * <p>一般角色的状态记录的模块
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-12 (create)
 *   2019-05-12 (last modified)
 */
public class BaseStateModule extends StateModule {

	public BaseStateModule(CharacterEntry ch) {
		super(ch);
	}
	
	/**
	 * 硬直剩余时间 (步), 状态项
	 */
	public int stiffnessRemain;
	
	/**
	 * 硬直默认持续时间 (步), 配置项
	 */
	public int stiffnessDuration;
	
	/**
	 * 无敌剩余时间 (步), 状态项
	 */
	public int immuneRemain;
	
	/**
	 * 无敌默认持续时间 (步), 配置项
	 */
	public int immuneDuration;
	
	/**
	 * 行动状态, 状态项, 包含但不限于: "left", "right", "stop"
	 */
	public String motion;
	
	/**
	 * 本步是否发出过攻击动作, 状态项
	 */
	public boolean attacking;
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);

		initStateArguments(value);

		addCollector(new JsonCollector(this::createStateParamJson, "state_param")); // 一般视为静态的
		parent.addSubscribe("after_damage", this);
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
	}
	
	public JsonValue createStateJson() {
		JsonValue v = super.createStateJson();
		
		v.addChild("stiffness", new JsonValue(stiffnessRemain));
		v.addChild("immune", new JsonValue(immuneRemain));
		v.addChild("motion", new JsonValue(motion));
		v.addChild("attacking", new JsonValue(attacking));
		
		return v;
	}
	
	public JsonValue createStateParamJson() {
		JsonValue v = new JsonValue(ValueType.object);
		
		v.addChild("stiffness", new JsonValue(stiffnessDuration));
		v.addChild("immune", new JsonValue(immuneDuration));
		
		return v;
	}
	
	@Override
	protected boolean setJson(String first, JsonValue value) {
		if ("state".equals(first)) {
			setStateJson(value);
		}
		return super.setJson(first, value);
	}
	
	public void setStateJson(JsonValue value) {
		for (JsonValue entry = value.child; entry != null; entry = entry.next) {
			switch (entry.name) {
			case "motion":
				this.motion = entry.asString();
				break;
			case "attacking":
				this.attacking = entry.asBoolean();
				break;
			default:
				break;
			}
		}
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
		statec.clear();
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
			
				if (this.stiffnessDuration > 0) {
					this.stiffnessRemain = stiffnessDuration;
				}
				if (this.immuneDuration > 0) {
					this.immuneRemain = immuneDuration;
				}
				statec.clear();
			}
		}
		super.receiveEvent(event);
	}

}
