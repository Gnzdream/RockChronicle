package zdream.rockchronicle.screen;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.foe.IFoePainter;
import zdream.rockchronicle.core.region.Region;
import zdream.rockchronicle.foes.megaman.Megaman;
import zdream.rockchronicle.foes.mm5bbitter.MM5BBitter;

public class LevelScreen implements Screen {
	
	final RockChronicle app;
	
	/**
	 * 查看该世界的镜头, 一般长宽为 25 x 14
	 * 这个锁定的是人物位置.
	 */
	public OrthographicCamera worldCamera;
	/**
	 * 查看整个屏幕的镜头, 用于放置字幕、头像、血槽等指示、辅助性物品.
	 * 一般呈现的物品浮于世界镜头之上
	 */
	OrthographicCamera symbolCamera;
	
	SpriteBatch batch = new SpriteBatch();
	
	/**
	 * 绘画降序排列
	 */
	Comparator<IFoePainter> comparator = (foe1, foe2) -> foe1.zIndex() - foe2.zIndex();
	
	public LevelScreen() {
		app = RockChronicle.INSTANCE;
		worldCamera = app.runtime.scene.camera;
		app.runtime.ticker.setFrameStartListener((count) -> this.frameStart(count));
		app.runtime.ticker.setFrameFinishedListener((count) -> this.frameFinished(count));
		
		// tiled 地图
		app.runtime.setCurrentRegion("mm1cut");
		app.runtime.levelStart();
		
		symbolCamera = new OrthographicCamera();
		symbolCamera.setToOrtho(false, app.widthInPixel, app.heightInPixel);
	}
	
	Megaman mm;

	@Override
	public void show() {
		GameRuntime runtime = app.runtime;
		Region region = runtime.world.curRegion;
		
		mm = new Megaman(region.spawnx + 0.5f, region.spawny);
		runtime.setPlayer1(mm);
		mm.getBoxes()[0].orientation = true;
		
		MM5BBitter sh = new MM5BBitter();
		runtime.addFoe(sh);
		sh.getBoxes()[0].setAnchor(65536 * 21, 65536 * 5);
		
		
		
		// 设置 megaman 的初始位置, 到 room 所对应的 spawn 点
		// 人物设置必须晚于世界创建
//		runtime.createWorld();
//		runtime.setSpawnRegion("mm1cut");
//		region = runtime.curRegion;
//		
//		
//		megaman = (MegamanInLevel) app.runtime.characterBuilder.create("megaman",
//				CharacterParameter.newInstance().setBoxAnchorP(
//						(int) ((region.spawnx + 0.5f) * Box.P_PER_BLOCK),
//						region.spawny * Box.P_PER_BLOCK)
//				.setCamp(1)
//				.get());
////		megaman.load(Gdx.files.local("res\\characters\\megaman\\megaman.json"));
//		runtime.putPlayer(1, megaman);
////		megaman.setBlockPos(region.spawnx, region.spawny);
//		
//		// 测试: 放进去小怪
//		{
////			runtime.addEntry(app.runtime.characterBuilder.create("testfoe",
////					CharacterParameter.newInstance().setBoxAnchor(region.spawnx + 3.5f, region.spawny)
////					.get()));
//
//			runtime.addEntry(app.runtime.characterBuilder.create("testfoe",
//					CharacterParameter.newInstance().setBoxAnchorP(12 * Box.P_PER_BLOCK, 8 * Box.P_PER_BLOCK)
//					.get()));
//			
//			runtime.addEntry(app.runtime.characterBuilder.create("mm2bird",
//					CharacterParameter.newInstance().setBoxAnchorP(20 * Box.P_PER_BLOCK, 7 * Box.P_PER_BLOCK)
//					.get()));
//		}
		
//		Room curRoom = region.rooms[runtime.room];
		
		// 显示部分
		// mapRender
		fixMapRender();
		
		// 测试帧率部分
		frameTimestamp = System.currentTimeMillis();
	}
	
	public void frameStart(int stepCount) {
		Gdx.gl.glClearColor(0.1f, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	public void frameFinished(int stepCount) {
		GameRuntime runtime = app.runtime;
		runtime.scene.updateCamera();
		fixMapRender();
		runtime.scene.renderMap();
		
		// 画所有 Foe
		Array<IFoePainter> painters = runtime.painters;
		painters.sort(comparator);
		for (int i = 0; i < painters.size; i++) {
			painters.get(i).draw(batch, worldCamera);
		}
	}
	
	/**
	 * 在界面启动时, 和每帧
	 * 修正 MapRender 渲染的瓦片地图的位置
	 */
	private void fixMapRender() {
		float dx = -worldCamera.position.x + worldCamera.viewportWidth / 2.0f;
		float dy = -worldCamera.position.y + worldCamera.viewportHeight / 2.0f;
		
		batch.setProjectionMatrix(worldCamera.combined);
		batch.getProjectionMatrix().translate(dx, dy, 0);
	}
	
	@Override
	public void render(float delta) {
		final GameRuntime runtime = app.runtime;
		runtime.tick(delta);
		
		
		// 镜头控制
//		count++;
//		if (count % 2 == 0) {
//			worldCamera.translate(0.05f, 0);
//		}
//		worldCamera.update();
		
		// 物理
		
		// 渲染部分
		// symbol 层是不能被 render 的
		
//		app.runtime.drawEntries(batch, worldCamera);
		
		// 帧率
		long now = System.currentTimeMillis();
		if (now - frameTimestamp > 1000) {
			lastFrameCount = frameCount;
			frameCount = 0;
			while (now - frameTimestamp > 1000) {
				frameTimestamp += 1000;
			}
		}
		frameCount++;
		app.batch.begin();
//		int displayHp = app.runtime.cast.megaman.hp;
//		displayHp = (displayHp > 256) ? displayHp / 256 : (displayHp > 256) ? 1 : 0;
		
		String debugText = String.format("帧率: %d, 时间: %d\n%s",
				lastFrameCount, app.runtime.ticker.count, app.runtime);
		app.font.setColor(Color.BLACK);
		app.font.draw(app.batch, debugText, 12, 38);
		app.font.setColor(Color.WHITE);
		app.font.draw(app.batch, debugText, 10, 40);
		app.batch.end();
	}
	
	// 测试帧率的数据
	int frameCount;
	long frameTimestamp;
	int lastFrameCount;

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
//		app.runtime.pauseWorld(); // TODO 应该是游戏整体暂停, 而非只有世界暂停
	}

	@Override
	public void resume() {
//		app.runtime.resumeWorld(); // TODO 应该是游戏整体复苏, 而非只有世界复苏
	}

	@Override
	public void hide() {
//		app.runtime.pauseWorld();
	}

	@Override
	public void dispose() {
		// 仅处理资源回收. 请把逻辑相关的收尾工作放在 willDispose 方法中
	}

}
