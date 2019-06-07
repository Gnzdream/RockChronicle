package zdream.rockchronicle.core.module.puppet;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import zdream.rockchronicle.core.character.CharacterEntry;
import zdream.rockchronicle.core.character.event.CharacterEvent;
import zdream.rockchronicle.core.character.parameter.CharacterParameter;
import zdream.rockchronicle.core.module.AbstractModule;
import zdream.rockchronicle.core.move.IMovable;
import zdream.rockchronicle.utils.JsonUtils;

/**
 * <p>领队模块.
 * <p>为跟随者的主人, 所有其跟随者接受其管理.
 * 该类将在初始化时就生成它的跟随者.
 * <p>主人很大可能携带 {@link LeaderModule} 模块来管理它的跟随方,
 * 而跟随方很大可能携带 {@link FollowerModule} 模块来跟随主人.
 * <p>为了做到跟随方随着主人行动, 跟随方会将 {@link IMovable} 添加到主人的行动列表中,
 * 并设置负的优先度, 在主人移动完成之后, 对跟随方进行移动.
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-06-04 (created)
 *   2019-06-04 (last modified)
 */
public class LeaderModule extends AbstractModule {

	public LeaderModule(CharacterEntry parent) {
		super(parent);
	}

	@Override
	public String name() {
		return "Leader";
	}
	
	@Override
	public String description() {
		return "base";
	}
	
	@Override
	public int priority() {
		return -50;
	}
	
	/*
	 * 这里需要确定
	 * 1. 携带的跟随方的参数, 包含跟随方的初始位置 (锚点与本角色锚点位置之差)
	 * 如果是出场时自动添加上去的, 那在该模块 init 方法执行时,
	 * 创建跟随方的实体.
	 */
	Array<FollowerParam> followers = new Array<>(8);
	
	@Override
	public void init(FileHandle file, JsonValue value) {
		super.init(file, value);
		
		// 其它参数
		JsonValue oleader = value.get("leader");
		if (oleader != null) {
			initLeaderParam(oleader);
		}
		
		// 产生跟随的角色
		createFollowers(value);
		
		// 监听
		parent.addSubscribe("release_follower", this);
	}
	
	private void initLeaderParam(JsonValue oleader) {
		JsonValue afollowers = oleader.get("followers");
		if (afollowers != null) {
			for (JsonValue ofollower = afollowers.child; ofollower != null; ofollower = ofollower.next) {
				FollowerParam p = new FollowerParam(parent.id);
				
				p.offx = ofollower.getFloat("offsetX", 0);
				p.offy = ofollower.getFloat("offsetY", 0);
				p.name = ofollower.getString("name");
				p.param = ofollower.get("param");
				
				followers.add(p);
			}
		}
	}
	
	/**
	 * <p>产生跟随的角色
	 * <p>注: 这里不能直接从 Box 里面取锚点位置因为现在还在初始化阶段
	 * </p>
	 */
	private void createFollowers(JsonValue v) {
		// 确定位置
		JsonValue obox = v.get("box");
		JsonValue oanchor = obox.get("anchor");
		float x = oanchor.getFloat("x");
		float y = oanchor.getFloat("y");
		
		// 确定朝向
		JsonValue ostate = v.get("state");
		boolean orientation = (ostate == null) ? true : ostate.getBoolean("orientation", true);
		
		// 确定阵营
		JsonValue ocamp = v.get("camp");
		int camp = (ocamp == null) ? 0 : ocamp.getInt("camp", 0);

		for (int i = 0; i < followers.size; i++) {
			FollowerParam item = this.followers.get(i);
			float xx = x + item.offx;
			float yy = y + item.offy;
			
			CharacterEntry c = parent.createEntry(item.name,
					CharacterParameter.newInstance(JsonUtils.clone(item.param))
						.setBoxAnchor(xx, yy)
						.setStateOrientation(orientation)
						.setMotionFlipX(!orientation)
						.setCamp(camp)
						.get());
			
			item.followerId = c.id;
			
			// 将跟随者的 IMovable 放入领队的列表中
			AbstractModule af = c.getModule(FollowerModule.NAME);
			if (af != null && af instanceof FollowerModule) {
				FollowerModule f = (FollowerModule) af;
				item.movable = f;
				
				f.setFollowerParam(item);
				parent.getBoxModule().addMovable(f, -20);
			}
		}
	}
	
	/**
	 * (主动) 释放跟随者
	 */
	public void releaseFollower(int id) {
		for (int i = 0; i < followers.size; i++) {
			FollowerParam param = followers.get(i);
			
			if (param.followerId == id) {
				// 执行脱钩
				
				parent.getBoxModule().removeMovable(param.movable);
				// TODO
				
			}
			
		}
	}
	
	/**
	 * (被动) 释放跟随者
	 */
	public void detachFollower(int id) {
		
	}
	
	@Override
	public void receiveEvent(CharacterEvent event) {
		if ("release_follower".equals(event.name)) {
			System.out.println("LeaderModule: 释放跟随者 " + event.value.get("id"));
			
			// target 可能是: "all", "random", 和 id (integer), [id, ...] (array)
			JsonValue target = event.value.get("id");
			
			
			
		}
		super.receiveEvent(event);
	}

}
