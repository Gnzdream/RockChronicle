package zdream.rockchronicle.core.module.control;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.ObjectMap;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.utils.JsonUtils;

/**
 * <p>简单的控制模块.
 * <p>角色周期性做某种工作
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-02 (created)
 *   2019-06-02 (last modified)
 */
public class SimpleControlModule extends ControlModule {
	
	ObjectMap<String, Sequence> seqs = new ObjectMap<>(8);
	ObjectMap<String, ActionItem> actions = new ObjectMap<>(8);
	
	class Sequence {
		String name;
		int length; // 单位是步
		int loopIdx; // 单位是步
		IntMap<SequenceItem> seq;
	}
	class SequenceItem {
		int stamp;
		String action;
		SequenceItem next; // 链表形式
	}
	class ActionItem {
		String operation;
		JsonValue param;
		ActionItem next; // 链表形式
	}

	public SimpleControlModule(CharacterEntry ch) {
		super(ch);
	}
	
	@Override
	public String description() {
		return "simple";
	}
	
	/* **********
	 *  初始化  *
	 ********** */
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		JsonValue ocontrol = value.get("control");
		JsonValue asequences = ocontrol.get("sequences");
		initSequences(asequences);
		
		JsonValue aactions = ocontrol.get("actions");
		initActions(aactions);
	}
	
	private void initSequences(JsonValue asequences) {
		for (JsonValue entry = asequences.child; entry != null; entry = entry.next) {
			initSequence(entry);
		}
		
		if (this.seqs.size == 1) {
			curSeq = seqs.iterator().next().value;
		} else {
			// 需要配置默认的 seq TODO
		}
	}
	
	private void initSequence(JsonValue osequence) {
		Sequence seq = new Sequence();
		seq.name = osequence.getString("name");
		// 加 0.25 是非常粗略的四舍五入, 下同
		{
			float length = osequence.getFloat("length", -1);
			if (length > 0) {
				seq.length = (int) (length * LevelWorld.STEPS_PER_SECOND + 0.25f);
			} else {
				seq.length = -1;
			}
		}
		seq.loopIdx = (int) (osequence.getFloat("loopIdx", 0) * LevelWorld.STEPS_PER_SECOND + 0.25f);
		
		JsonValue array = osequence.get("sequence");
		seq.seq = new IntMap<>(array.size * 2);
		for (JsonValue entry = array.child; entry != null; entry = entry.next) {
			SequenceItem item = new SequenceItem();
			
			int stamp = (int) (entry.getFloat("stamp") * LevelWorld.STEPS_PER_SECOND + 0.25f);
			item.stamp = stamp;
			item.action = entry.getString("action");
			
			SequenceItem oitem = seq.seq.get(stamp);
			if (oitem != null) {
				for (oitem = oitem.next; oitem.next != null; oitem = oitem.next) {}
				oitem.next = item;
			} else {
				seq.seq.put(stamp, item);
			}
		}
		
		this.seqs.put(seq.name, seq);
	}

	private void initActions(JsonValue aactions) {
		for (JsonValue entry = aactions.child; entry != null; entry = entry.next) {
			initAction(entry);
		}
	}
	
	private void initAction(JsonValue oaction) {
		String name = oaction.getString("name");
		JsonValue acommands = oaction.get("commands");
		ActionItem first = null, last = null;
		
		for (JsonValue entry = acommands.child; entry != null; entry = entry.next) {
			if (first == null) {
				last = new ActionItem();
				first = last;
			} else {
				last.next = new ActionItem();
				last = last.next;
			}
			
			last.operation = entry.getString("operation");
			last.param = JsonUtils.clone(entry);
		}
		
		actions.put(name, first);
	}
	
	/* **********
	 *   执行   *
	 ********** */
	
	/**
	 * 在当前执行的序列中, 在哪个时间点
	 */
	int step;
	/**
	 * 当前的序列
	 */
	Sequence curSeq;
	
	@Override
	public void determine(LevelWorld world, int index, boolean hasNext) {
		super.determine(world, index, hasNext);
		
		SequenceItem item = curSeq.seq.get(step);
		for (; item != null; item = item.next) {
			this.execute(item.action);
		}
		
		++this.step;
		if (curSeq.length > 0 && this.step >= curSeq.length) {
			this.step = curSeq.loopIdx;
		}
	}
	
	private void execute(String action) {
		ActionItem item = actions.get(action);
		
		for (; item != null; item = item.next) {
			String operation = item.operation;
			switch (operation) {
			case "publish_event":
				executePublish(item.param);
				break;
			case "motion_select":
				executeMotionSelect(item.param);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 发布信息
	 * @param param
	 */
	private void executePublish(JsonValue param) {
		CharacterEvent event = new CharacterEvent(param.getString("event"));
		event.value = JsonUtils.clone(param.get("param"));
		
		parent.publish(event);
	}
	
	/**
	 * 行动修改
	 * @param param
	 */
	private void executeMotionSelect(JsonValue param) {
		CharacterEvent event = new CharacterEvent("motion_select");
		event.value = new JsonValue(ValueType.object);
		event.value.addChild("motion", param.get("motion"));
		
		/**
		 * TODO 还有 replace 模式未添加.
		 * 见方法 TextureSelect#replaceState
		 */
		
		parent.publish(event);
	}
	
}