package zdream.rockchronicle.core.textures;

/**
 * 纹理序列
 * @author Zdream
 * @since v0.0.1
 * @date
 *   2019-05-10 (create)
 *   2019-05-10 (last modified)
 */
public class TextureSequence {
	public String[] seqs;
	/**
	 * 指向 seqs 的索引. 如果序列没有循环段, 该值为 -1
	 */
	public int loopIdx = -1;
	
	public String state;
	public int step;
}
