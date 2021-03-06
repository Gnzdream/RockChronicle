package zdream.rockchronicle.core.module.control;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
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
	
	/**
	 * 是否删除角色
	 */
	boolean parentWillDelete = false;
	
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
	
	/**
	 * 收到消息的触发器
	 */
	class RecieveEventTrigger {
		/**
		 * 消息名称
		 */
		String eventName;
		/**
		 * 转入的 sequencem, 可能为 null
		 */
		String sequence;
		/**
		 * 刚触发时执行的 action
		 */
		String[] actions;
	}
	
	Array<RecieveEventTrigger> recvEventTs = null;

	public SimpleControlModule(CharacterEntry ch) {
		this(ch, "simple");
	}

	protected SimpleControlModule(CharacterEntry ch, String desc) {
		super(ch, desc);
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
		
		// defaultSequence
		String defaultSequence = ocontrol.getString("defaultSequence", null);
		if (defaultSequence != null) {
			curSeq = seqs.get(defaultSequence);
		}
		
		// triggers
		JsonValue atriggers = ocontrol.get("triggers");
		if (atriggers != null && atriggers.isArray()) {
			initTriggers(atriggers);
		}
	}

	private void initSequences(JsonValue asequences) {
		for (JsonValue entry = asequences.child; entry != null; entry = entry.next) {
			initSequence(entry);
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
	
	private void initTriggers(JsonValue atriggers) {
		for (JsonValue entry = atriggers.child; entry != null; entry = entry.next) {
			String when = entry.getString("when");
			switch (when) {
			case "recieve_event":
				initRecvEventTrigger(entry);
				break;

			default:
				break;
			}
		}
		
	}
	
	private void initRecvEventTrigger(JsonValue otrigger) {
		RecieveEventTrigger t = new RecieveEventTrigger();
		t.eventName = otrigger.getString("eventName");
		t.sequence = otrigger.getString("sequence", null);
		JsonValue actions = otrigger.get("actions");
		if (actions != null) {
			if (actions.isArray()) {
				t.actions = actions.asStringArray();
			} else {
				t.actions = new String[] {actions.asString()};
			}
		}
		
		if (recvEventTs == null) {
			recvEventTs = new Array<>(4);
		}
		recvEventTs.add(t);
		
		parent.addSubscribe(t.eventName, this);
	}
	
	@Override
	public int priority() {
		return -12;
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
		
		if (curSeq == null) {
			return;
		}
		
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
				executePublish(item.param, null);
				break;
			case "motion_select":
				executeMotionSelect(item.param);
				break;
			case "create_module":
				executeCreateModule(item.param);
				break;
			case "will_delete":
				executeWillDelete();
				break;
			case "set_situation":
				executeSetSituation(item.param);
				break;

			default:
				break;
			}
		}
	}
	
	private void execute(String action, CharacterEvent event) {
		ActionItem item = actions.get(action);
		
		for (; item != null; item = item.next) {
			String operation = item.operation;
			switch (operation) {
			case "publish_event":
				executePublish(item.param, event);
				break;
			case "motion_select":
				executeMotionSelect(item.param);
				break;
			case "create_module":
				executeCreateModule(item.param);
				break;
			case "will_delete":
				executeWillDelete();
				break;
			case "set_situation":
				executeSetSituation(item.param);
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
	private void executePublish(JsonValue param, CharacterEvent event0) {
		CharacterEvent event = new CharacterEvent(param.getString("event"));
		JsonValue value = param.get("param");
		if (value != null) {
			event.value = JsonUtils.clone(value);
		}
		
		if (param.has("param_expression")) {
			event.value = JsonUtils.mergeJson(event.value,
					handleExpression(param.get("param_expression"), event0));
		}
		
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
	
	/**
	 * 创建新的模块
	 * @param param
	 */
	private void executeCreateModule(JsonValue param) {
		String smodule = param.getString("module");
		String sname = param.getString("name");
		
		parent.addModule(smodule, sname, param.get("param"));
	}

	/**
	 * 删除角色. 删除角色将延迟到这步时间结束后执行
	 */
	private void executeWillDelete() {
		parentWillDelete = true;
	}

	/**
	 * 设置参数至 situation 中
	 * @param param
	 */
	private void executeSetSituation(JsonValue param) {
		String key = param.getString("key");
		JsonValue value = JsonUtils.clone(param.get("value"));
		
		setSituation(key, value);
	}
	
	/**
	 * 用表达式合成 Json 数据
	 * @param raw
	 * @param event
	 * @return
	 */
	private JsonValue handleExpression(JsonValue raw, CharacterEvent event) {
		JsonValue v;
		
		if (raw.isObject()) {
			v = handleExpressionObject(raw, event);
		} else if (raw.isArray()) {
			v = handleExpressionArray(raw, event);
		} else {
			v = handleExpressionValue(raw.asString(), event);
		}
		
		return v;
	}
	
	private JsonValue handleExpressionValue(String exp, CharacterEvent event) {
		if (exp.startsWith("event:")) {
			String path = exp.substring(6);
			return JsonUtils.clone(JsonUtils.find(event.value, path));
		}
		return null;
	}
	
	private JsonValue handleExpressionObject(JsonValue oraw,
			CharacterEvent event) {
		JsonValue v = new JsonValue(ValueType.object);
		for (JsonValue entry = oraw.child; entry != null; entry = entry.next) {
			v.addChild(entry.name, handleExpression(entry, event));
		}
		return v;
	}
	
	private JsonValue handleExpressionArray(JsonValue oraw,
			CharacterEvent event) {
		JsonValue v = new JsonValue(ValueType.array);
		for (JsonValue entry = oraw.child; entry != null; entry = entry.next) {
			v.addChild(handleExpression(entry, event));
		}
		return v;
	}
	
	@Override
	public void stepPassed() {
		if (parentWillDelete) {
			parent.willDestroy();
		}
		super.stepPassed();
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		String name = event.name;
		
		if (this.recvEventTs != null && !this.parentWillDelete) {
			for (int i = 0; i < recvEventTs.size; i++) {
				RecieveEventTrigger t = recvEventTs.get(i);
				if (t.eventName.equals(name)) {
					if (t.actions != null) {
						for (int j = 0; j < t.actions.length; j++) {
							execute(t.actions[j], event);
						}
					}
					
					switchSequence(t.sequence);
					return;
				}
			}
		}
		
		super.receiveEvent(event);
	}
	
	/**
	 * 切换现在的序列
	 * @param name
	 */
	private void switchSequence(String name) {
		if (name != null) {
			this.curSeq = seqs.get(name);
		} else {
			this.curSeq = null;
		}
		this.step = 0;
	}
	
	/* **********
	 *   回收   *
	 ********** */
	
	@Override
	public void willDestroy() {
		super.willDestroy();
		
		if (recvEventTs != null) {
			parent.removeSubscribe(this);
		}
	}
	
}
