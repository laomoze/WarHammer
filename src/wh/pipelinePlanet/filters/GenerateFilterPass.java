package wh.pipelinePlanet.filters;

import arc.struct.*;
import mindustry.maps.filters.*;
import wh.pipelinePlanet.core.*;

/**
 * 中文说明：将 Mindustry 过滤器以 pass 形式应用到当前地图。
 */
public class GenerateFilterPass implements GenPass{
    private final Seq<GenerateFilter> filters = new Seq<>();

    public GenerateFilterPass(Seq<GenerateFilter> filters){
        this.filters.addAll(filters);
    }

    public GenerateFilterPass(GenerateFilter... filters){
        this.filters.add(filters);
    }

    @Override
    public String name(){
        return "GenerateFilterPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(filters.isEmpty()) return;

        var in = new GenerateFilter.GenerateInput();
        in.begin(ctx.width(), ctx.height(), (x, y) -> ctx.tiles.getn(x, y));

        for(GenerateFilter filter : filters){
            filter.seed = ctx.seed;
            filter.apply(ctx.tiles, in);
        }
    }
}

