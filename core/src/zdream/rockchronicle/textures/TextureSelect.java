package zdream.rockchronicle.textures;

import java.util.Objects;

/**
 * <p>用于选择纹理序列中, 要使用的是哪个纹理
 * </p>
 * 
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-16 (created)
 *   2019-05-16 (last modified)
 */
public class TextureSelect {
	
	private TextureSheet sheet;
	private TextureSequence currentSeq;
	
	private int current = 0;
	private int remain = 0;
	
	public TextureSheet getSheet() {
		return sheet;
	}

	public void setSheet(TextureSheet sheet) {
		this.sheet = sheet;
		String state = sheet.defaultState;
		setState(state);
	}
	
	public void setState(String state) {
		currentSeq = sheet.sequences.get(state);
		this.current = 0;
		if (currentSeq == null) {
			this.remain = 0;
		} else {
			this.remain = currentSeq.step;
		}
	}

	public TextureSequence getSequence() {
		return currentSeq;
	}
	
	/**
	 * 向前倒退时间
	 * @param step
	 *   时间单位: 步, 正整数
	 */
	public TextureSelect turnBack(int step) {
		if (currentSeq == null) {
			return this;
		}
		this.remain += step;
		
		int delta = currentSeq.step;
		if (delta > 0) {
			while (this.remain > delta) {
				if (currentSeq.loopIdx < 0) {
					current = (current <= 0) ? 0 : current - 1;
				} else {
					if (current > currentSeq.loopIdx) {
						current --;
					} else if (current == currentSeq.loopIdx) {
						current = currentSeq.seqs.length - 1;
					} else {
						current = (current <= 0) ? 0 : current - 1;
					}
				}
			}
		}
		
		return this;
	}
	
	/**
	 * 告诉时间过了多少
	 * @param step
	 *   时间单位: 步, 正整数
	 */
	public TextureSelect tick(int step) {
		if (currentSeq == null) {
			return this;
		}
		
		this.remain -= step;
		
		int delta = currentSeq.step;
		if (delta > 0) {
			while (this.remain <= 0) {
				current += 1;
				if (current >= currentSeq.seqs.length) {
					if (currentSeq.loopIdx < 0) {
						current = currentSeq.seqs.length - 1;
					} else {
						current = currentSeq.loopIdx;
					}
				}
				this.remain += delta;
			}
		}
		
		return this;
	}
	
	public TextureSheetEntry select() {
		Objects.requireNonNull(sheet, "纹理为 null");
		
		if (this.currentSeq == null) {
			return null;
		}
		String name = this.currentSeq.seqs[this.current];
		return this.sheet.entrys.get(name);
	}

}
