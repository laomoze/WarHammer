package wh.pipelinePlanet.core;

/**
 * 中文说明：生成阶段接口：每个 pass 实现 apply() 并声明名称。
 */
public interface GenPass{
    String name();

    void apply(GenContext ctx);
}
