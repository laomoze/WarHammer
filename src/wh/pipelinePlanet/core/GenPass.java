package wh.pipelinePlanet.core;

/**
 * 单个生成步骤。每个 pass 只负责一件明确的事。
 */
public interface GenPass{
    String name();

    void apply(GenContext ctx);
}
