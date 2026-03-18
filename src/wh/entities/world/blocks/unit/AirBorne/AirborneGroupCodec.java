package wh.entities.world.blocks.unit.AirBorne;

import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.Strings;

/**
 * 编组配置的编解码与规范化工具。
 * 这里只处理纯数据，尽量不掺杂方块状态逻辑。
 */
public final class AirborneGroupCodec{
    private AirborneGroupCodec(){
    }

    public static Seq<IntSeq> parseGroups(String data, int planCount, int groupAmount){
        Seq<IntSeq> groups = new Seq<>();
        if(data == null) return groups;

        String text = data.trim();
        if(text.isEmpty()) return groups;

        for(String rawGroup : text.split(";", -1)){
            groups.add(parseGroup(rawGroup, planCount, groupAmount));
        }
        return groups;
    }

    public static void ensureRows(Seq<IntSeq> groups, int rows, int planCount, int groupAmount){
        int rowCount = Math.max(1, rows);
        while(groups.size < rowCount){
            groups.add(new IntSeq());
        }
        while(groups.size > rowCount){
            groups.remove(groups.size - 1);
        }

        for(IntSeq group : groups){
            normalizeGroup(group, planCount, groupAmount);
        }
    }

    public static void copyGroups(Seq<IntSeq> source, Seq<IntSeq> target){
        target.clear();
        for(IntSeq group : source){
            target.add(copyGroup(group));
        }
    }

    public static String groupsToString(Seq<IntSeq> groups, int rows, int planCount, int groupAmount){
        ensureRows(groups, rows, planCount, groupAmount);

        StringBuilder builder = new StringBuilder();
        for(int groupIndex = 0; groupIndex < groups.size; groupIndex++){
            if(groupIndex > 0) builder.append(';');

            IntSeq group = groups.get(groupIndex);
            for(int entryIndex = 0; entryIndex < group.size; entryIndex++){
                if(entryIndex > 0) builder.append(',');
                builder.append(group.get(entryIndex));
            }
        }
        return builder.toString();
    }

    private static IntSeq parseGroup(String rawGroup, int planCount, int groupAmount){
        IntSeq group = new IntSeq();
        String text = rawGroup == null ? "" : rawGroup.trim();
        if(text.isEmpty()) return group;

        for(String part : text.split(",")){
            if(group.size >= groupAmount) break;

            int planIndex = Strings.parseInt(part, -1);
            if(isValidPlanIndex(planIndex, planCount)){
                group.add(planIndex);
            }
        }
        return group;
    }

    private static void normalizeGroup(IntSeq group, int planCount, int groupAmount){
        for(int i = group.size - 1; i >= 0; i--){
            if(!isValidPlanIndex(group.get(i), planCount)){
                group.removeIndex(i);
            }
        }

        while(group.size > groupAmount){
            group.removeIndex(group.size - 1);
        }
    }

    private static IntSeq copyGroup(IntSeq source){
        IntSeq copy = new IntSeq();
        copy.addAll(source);
        return copy;
    }

    private static boolean isValidPlanIndex(int planIndex, int planCount){
        return planIndex >= 0 && planIndex < planCount;
    }
}
