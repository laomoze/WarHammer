package wh.pipelinePlanet.core;

import arc.struct.*;
import arc.util.*;

/**
 * 中文说明：按顺序执行 GenPass，可选输出每个 pass 的耗时日志。
 */
public class PassRunner{
    private final Seq<GenPass> passes = new Seq<>();

    public PassRunner add(GenPass pass){
        passes.add(pass);
        return this;
    }

    public Seq<GenPass> passes(){
        return passes;
    }

    public void run(GenContext ctx){
        for(GenPass pass : passes){
            if(ctx.cfg.enablePassTimingLog){
                long start = System.nanoTime();
                pass.apply(ctx);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
                Log.info("[modgen] pass @ done in @ ms", pass.name(), elapsedMs);
            }else{
                pass.apply(ctx);
            }
        }
    }
}
