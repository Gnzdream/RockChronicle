package zdream.rockchronicle.core.world;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * <p>用于时间管理.
 * <p>一般而言, 每秒走 120 步. 平摊下来每帧时间大约走 2 步.
 * </p>
 * 
 * @author Zdream
 * @date 2020-06-08
 */
public class Ticker {
	
	public static final int STEP_PRE_SECOND = 120;
	public static final float WORLD_STEP = 1f / STEP_PRE_SECOND;
	
	/**
	 * 包括暂停时间的所有步数
	 */
	public int count = 0;
	/**
	 * 不包括暂停时间的步数
	 */
	public int runCount = 0;
	
	private float accumulate = 0;
	
	/**
	 * 时间每走一步调用的
	 */
	private Consumer<Boolean> stepListener;
	/**
	 * 时间每走一帧最后调用的
	 */
	private IntConsumer frameStartListener, frameFinishedListener;
	/**
	 * 是否暂停. 这个参数不能直接修改
	 */
	public boolean pause;
	
	public void tick(float delta) {
		float realDelta = Math.min(delta, 0.1f);
		
		if (frameStartListener != null) {
			frameStartListener.accept(count);
		}
		
		accumulate += realDelta;
		while (accumulate > 0) {
			if (stepListener != null) {
				stepListener.accept(pause);
			}
			count++;
			if (!pause) {
				runCount++;
			}
			accumulate -= WORLD_STEP;
		}
		
		if (frameFinishedListener != null) {
			frameFinishedListener.accept(count);
		}
	}
	
	public void setStepListener(Consumer<Boolean> stepListener) {
		this.stepListener = stepListener;
	}
	
	public void setFrameStartListener(IntConsumer frameListener) {
		this.frameStartListener = frameListener;
	}
	
	public void setFrameFinishedListener(IntConsumer frameListener) {
		this.frameFinishedListener = frameListener;
	}
	
	public void worldPause() {
		this.pause = true;
	}
	public void worldResume() {
		this.pause = false;
	}
}
