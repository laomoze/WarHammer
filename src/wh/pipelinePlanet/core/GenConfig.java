package wh.pipelinePlanet.core;

/**
 * 生成器配置。建议由你的具体星球生成器按需覆写默认值。
 */
public class GenConfig{
    /** 水域占比阈值，可用于判断是否需要启用海图相关逻辑。 */
    public float waterThreshold = 0.19f;
    /** 普通房间最小数量。 */
    public int minRooms = 0;
    /** 普通房间最大数量。 */
    public int maxRooms = 2;
    /** 敌方房间数量倍率（会乘以威胁值）。 */
    public float enemyRoomScale = 1f;
    /** 是否启用随机湖泊。 */
    public boolean enableLakes = false;
    /** 是否启用遗迹/装饰强化。 */
    public boolean enableRuins = true;
    /** 是否打印每个 pass 的耗时。 */
    public boolean enablePassTimingLog = true;

    /** 是否启用科技地板网格（等价原版 tech() 风格）。 */
    public boolean enableTechGrid = true;
    /** 科技网格单元尺寸。原版默认 20。 */
    public int techGridCellSize = 20;
    /** 科技网格噪声阈值 A。原版默认 0.63。 */
    public float techGridThresholdA = 0.63f;
    /** 科技网格噪声阈值 B。原版默认 0.6。 */
    public float techGridThresholdB = 0.6f;
    /** 科技墙替换概率。原版默认 0.7。 */
    public float techGridWallChance = 0.7f;
    /** 面板内圈偏移（越大，floor2 覆盖范围越大）。原版默认 2。 */
    public float techGridInnerOffset = 2f;
}
