
#	角色内部事件的说明
Character Events Description

@since v0.0.1

@date 2019-05-06 (create)

---

相关类

*	zdream.rockchronicle.core.character.AbstractModule
	事件发布主体 (发布者) 和接收主体 (订阅者)
	
*	zdream.rockchronicle.core.character.CharacterEntry
	事件的发布与接收中心，用于协调发布与接收事件
	
*	zdream.rockchronicle.core.character.event.CharacterEvent
	发布的消息

---

控制主体发布的消息 ControlModule

##### 发布消息: ctrl_axis

当方向有变化则发布一次

发布数据:

*	left
	<br/>boolean
	<br/>左键是否按下了

*	right
	<br/>boolean
	<br/>右键是否按下了

*	up
	<br/>boolean
	<br/>上键是否按下了

*	down
	<br/>boolean
	<br/>下键是否按下了

##### 发布消息: ctrl_motion

当攻击键和跳跃键有变化则发布一次

*	attack
	<br/>boolean
	<br/>攻击键是否按下了

*	attackChange
	<br/>boolean
	<br/>如果本帧攻击键发生了变化, 从按下到抬起或者从抬起到变化, 则该值为 true

*	jump
	<br/>boolean
	<br/>跳跃键是否按下了

*	jumpChange
	<br/>boolean
	<br/>如果本帧跳跃键发生了变化, 从按下到抬起或者从抬起到变化, 则该值为 true

*	slide
	<br/>boolean
	<br/>滑铲键是否按下了. 注意, 如果配置正确, 下加跳也构成滑铲键

*	slideChange
	<br/>boolean
	<br/>如果本帧滑铲键发生了变化, 从按下到抬起或者从抬起到变化, 则该值为 true

##### 发布消息: outside_collision

当角色作为防御方，被其它攻击方进行碰撞伤害时，由攻击方向防御方发布该消息。指定调用 publishNow() 方法发布消息。

*	attackId
	<br/>integer
	<br/>攻击方的角色 id

*	attackCamp
	<br/>integer
	<br/>攻击方的阵营
	
*	damage
	<br/>integer
	<br/>实际伤害

*	properties
	<br/>array {object} | undefined
	<br/>其它效果
	
*	result
	<br/>string
	<br/>效果结果。该值由防御方接收到消息之后填写。
	*	"accepted"：效果被接受
	*	"ignored"：效果被忽略，即不接受。默认是它。
	*	"immune"：效果被接受，但是不触发效果。比如子弹攻击被弹飞这类。

##### 发布消息: health_exhausted

当生命值耗尽时触发。届时角色自己的碰撞应该不能再生效。

##### 发布消息: before_damage

##### 发布消息: after_damage

##### 发布消息: inside_recovery

补血效果

*	health
	<br/>integer
	<br/>补充生命值，实际血量是多少。

