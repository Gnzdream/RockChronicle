package zdream.rockchronicle.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

import zdream.rockchronicle.RockChronicle;
import zdream.rockchronicle.core.Config;
import zdream.rockchronicle.core.GameRuntime;
import zdream.rockchronicle.core.character.parameter.CharacterParameter;
import zdream.rockchronicle.platform.region.Region;
import zdream.rockchronicle.platform.region.Room;
import zdream.rockchronicle.platform.world.IPhysicsStep;
import zdream.rockchronicle.platform.world.LevelWorld;
import zdream.rockchronicle.sprite.character.megaman.MegamanInLevel;

public class LevelScreen implements Screen, IPhysicsStep {
	
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
	
	Region region;
	OrthogonalTiledMapRenderer mapRender;
	
	/*
	 * 缓存人物
	 */
	MegamanInLevel megaman;
	
	SpriteBatch batch = new SpriteBatch();
	
	public LevelScreen() {
		app = RockChronicle.INSTANCE;
		app.runtime.levelWorld = new LevelWorld();
		
		// tiled 地图
		region = app.runtime.regionBuilder.buildForTerrainOnly("mm1cut");
//		region = app.runtime.regionBuilder.build("mm1cut");
		
		worldCamera = new OrthographicCamera();
		worldCamera.setToOrtho(false, app.width, app.height); // y 轴方向朝上
//		worldCamera.combined.scale(Config.INSTANCE.blockWidth, Config.INSTANCE.blockHeight, 1);
//		worldCamera.combined.scale(2, 2, 1);
		
		symbolCamera = new OrthographicCamera();
		symbolCamera.setToOrtho(false, app.widthInPixel, app.heightInPixel);
	}

	@Override
	public void show() {
		GameRuntime runtime = app.runtime;
		
		/*
		 * 物理
		 */
		LevelWorld world = app.runtime.levelWorld;
		world.doCreate();
		world.setStepCallBack(this);
		world.doResume();
		
		// 设置 megaman 的初始位置, 到 room 所对应的 spawn 点
		// 人物设置必须晚于世界创建
		runtime.curRegion = region;
		runtime.room = region.spawnRoom;
		world.setCurrentRoom(runtime.curRegion.rooms[runtime.room]);
		
		megaman = (MegamanInLevel) app.runtime.characterBuilder.create("megaman",
				CharacterParameter.newInstance().setBoxAnchor(region.spawnx + 0.5f, region.spawny)
				.setCamp(1)
				.get());
//		megaman.load(Gdx.files.local("res\\characters\\megaman\\megaman.json"));
		runtime.putPlayer(1, megaman);
//		megaman.setBlockPos(region.spawnx, region.spawny);
		
		// 设置控制端. 这里不一定是 megaman 要注意
		megaman.bindController(app.input.p1);
		
		// 测试: 放进去小怪
		{
			runtime.addEntry(app.runtime.characterBuilder.create("testfoe",
					CharacterParameter.newInstance().setBoxAnchor(region.spawnx + 3.5f, region.spawny)
					.get()));

			runtime.addEntry(app.runtime.characterBuilder.create("testfoe",
					CharacterParameter.newInstance().setBoxAnchor(12, 8)
					.get()));
		}
		
//		Room curRoom = region.rooms[runtime.room];
		
		// 显示部分
		// mapRender
		mapRender = new OrthogonalTiledMapRenderer(region.tmx, 1f / Config.INSTANCE.blockWidth);
		fixMapRender();
		
		batch.setProjectionMatrix(worldCamera.combined);
		
		// 测试帧率部分
		frameTimestamp = System.currentTimeMillis();
	}
	
	// 测试的
	Body testBody;
	Fixture testFixture;
	
	
	int count = 0;
	
	/**
	 * 在界面启动时, 和每帧
	 * 修正 MapRender 渲染的瓦片地图的位置
	 */
	private void fixMapRender() {
		Room curRoom = region.rooms[app.runtime.room];
		
		mapRender.setView(worldCamera);
		Rectangle viewBounds = mapRender.getViewBounds();
		
		viewBounds.setX(viewBounds.x + curRoom.offsetx);
		viewBounds.setY(viewBounds.y + curRoom.offsety);
		mapRender.getBatch().getProjectionMatrix()
		.translate (
			-curRoom.offsetx,
			-curRoom.offsety, 0);
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		float realDelta = Math.min(delta, 0.1f); // 该值不会大于 0.1f
		
		
		// 镜头控制
//		count++;
//		if (count % 2 == 0) {
//			worldCamera.translate(0.05f, 0);
//		}
//		worldCamera.update();
		
		// 物理
		LevelWorld world = app.runtime.levelWorld;
		world.doPhysicsStep(realDelta);
		
		// 渲染部分
		// symbol 层是不能被 render 的
		fixMapRender();
		mapRender.render();
		
		batch.setProjectionMatrix(worldCamera.combined);
		app.runtime.drawEntries(batch, worldCamera);
		
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
		int displayHp = app.runtime.cast.megaman.hp;
		displayHp = (displayHp > 256) ? displayHp / 256 : (displayHp > 256) ? 1 : 0;
		
		app.font.draw(app.batch, String.format("帧率: %d HP:%d",
				lastFrameCount, displayHp), 10, 20);
		app.batch.end();
	}
	
	// 测试帧率的数据
	int frameCount;
	long frameTimestamp;
	int lastFrameCount;

	@Override
	public void step(LevelWorld world, int index, boolean hasNext) {
		app.runtime.onWorldSteped(index, hasNext);
	}
	
	@Override
	public void onStepFinished(LevelWorld world, boolean isPause) {
		app.runtime.onStepFinished(isPause);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}
	
	public void willDispose() {
		// 删除控制端. 这里不一定是 megaman 要注意
		megaman.unbindController();
//		game.runtime.levelWorld.setStepCallBack(null);
		app.runtime.levelWorld = null;
	}

	@Override
	public void dispose() {
		// 仅处理资源回收. 请把逻辑相关的收尾工作放在 willDispose 方法中
	}

}
