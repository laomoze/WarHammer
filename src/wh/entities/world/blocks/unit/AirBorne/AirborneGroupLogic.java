package wh.entities.world.blocks.unit.AirBorne;

import arc.struct.*;
import arc.util.*;

/**
 * 编组槽位与请求统计的纯逻辑工具。
 * 不依赖方块实例状态，便于复用和单独阅读。
 */
public final class AirborneGroupLogic{
    private AirborneGroupLogic(){
    }

    public static int configuredCapacity(Seq<IntSeq> groups, int groupAmount, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int defaultFallbackPlan){
        IntIntMap requests = collectRequests(groups, groupAmount, plans, groupAmount, defaultFallbackPlan);
        int capacity = 0;

        for(IntIntMap.Entry entry : requests){
            if(!isValidRequest(entry, plans)) continue;
            capacity += planSpace(plans, entry.key) * entry.value;
        }
        return capacity;
    }

    public static IntIntMap collectRequests(Seq<IntSeq> groups, int requestGroups, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount, int defaultFallbackPlan){
        IntIntMap requests = new IntIntMap();
        if(groups == null || groups.isEmpty()) return requests;

        IntSeq firstGroup = groups.first();
        int fallbackPlan = resolveFallbackPlan(firstGroup, defaultFallbackPlan);
        int groupCount = Math.max(1, requestGroups);

        for(int groupIndex = 0; groupIndex < groupCount; groupIndex++){
            IntSeq group = groupIndex < groups.size ? groups.get(groupIndex) : null;
            collectGroupRequests(requests, group, firstGroup, fallbackPlan, plans, groupAmount);
        }

        return requests;
    }

    public static int slotPlan(@Nullable IntSeq group, int slot, @Nullable IntSeq firstGroup, int fallback, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        int planIndex = planAtVisualSlot(group, slot, plans, groupAmount);
        if(planIndex >= 0) return planIndex;

        planIndex = planAtVisualSlot(firstGroup, slot, plans, groupAmount);
        if(planIndex >= 0) return planIndex;

        return fallback;
    }

    public static int slotLeadPlan(@Nullable IntSeq group, int slot, @Nullable IntSeq firstGroup, int fallback, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        int planIndex = leadPlanAtVisualSlot(group, slot, plans, groupAmount);
        if(planIndex >= 0) return planIndex;

        planIndex = leadPlanAtVisualSlot(firstGroup, slot, plans, groupAmount);
        if(planIndex >= 0) return planIndex;

        return isValidPlanIndex(fallback, plans) && slot == 0 ? fallback : -1;
    }

    public static int groupUsedSlots(IntSeq group, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        int used = 0;
        for(int i = 0; i < group.size; i++){
            int planIndex = group.get(i);
            if(!isValidPlanIndex(planIndex, plans)) continue;

            used += planSpace(plans, planIndex);
            if(used >= groupAmount) return groupAmount;
        }
        return used;
    }

    public static int entryAtVisualSlot(IntSeq group, int slot, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        int cursor = 0;
        for(int i = 0; i < group.size; i++){
            int planIndex = group.get(i);
            if(!isValidPlanIndex(planIndex, plans)) continue;

            int span = planSpace(plans, planIndex);
            int start = cursor;
            int end = Math.min(groupAmount, cursor + span) - 1;
            if(slot >= start && slot <= end){
                return i;
            }

            cursor += span;
            if(cursor >= groupAmount) break;
        }
        return -1;
    }

    public static boolean isLeadVisualSlot(IntSeq group, int slot, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        int cursor = 0;
        for(int i = 0; i < group.size; i++){
            int planIndex = group.get(i);
            if(!isValidPlanIndex(planIndex, plans)) continue;

            int span = planSpace(plans, planIndex);
            if(slot == cursor) return true;
            if(slot > cursor && slot < cursor + span) return false;

            cursor += span;
            if(cursor >= groupAmount) break;
        }
        return false;
    }

    private static void collectGroupRequests(IntIntMap requests, @Nullable IntSeq group, @Nullable IntSeq firstGroup, int fallbackPlan, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        for(int slot = 0; slot < groupAmount; slot++){
            int planIndex = slotLeadPlan(group, slot, firstGroup, fallbackPlan, plans, groupAmount);
            if(isValidPlanIndex(planIndex, plans)){
                requests.put(planIndex, requests.get(planIndex, 0) + 1);
            }
        }
    }

    private static int planAtVisualSlot(@Nullable IntSeq group, int slot, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        if(group == null) return -1;

        int entryIndex = entryAtVisualSlot(group, slot, plans, groupAmount);
        return entryIndex >= 0 && entryIndex < group.size ? group.get(entryIndex) : -1;
    }

    private static int leadPlanAtVisualSlot(@Nullable IntSeq group, int slot, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int groupAmount){
        if(group == null) return -1;

        int entryIndex = entryAtVisualSlot(group, slot, plans, groupAmount);
        if(entryIndex < 0 || entryIndex >= group.size) return -1;

        return isLeadVisualSlot(group, slot, plans, groupAmount) ? group.get(entryIndex) : -1;
    }

    private static int resolveFallbackPlan(@Nullable IntSeq firstGroup, int defaultFallbackPlan){
        return firstGroup != null && !firstGroup.isEmpty() ? firstGroup.get(0) : defaultFallbackPlan;
    }

    private static int planSpace(Seq<AirborneUnitCallBlock.UnitSpacePlan> plans, int planIndex){
        return Math.max(1, plans.get(planIndex).space);
    }

    private static boolean isValidPlanIndex(int planIndex, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans){
        return plans != null && planIndex >= 0 && planIndex < plans.size;
    }

    private static boolean isValidRequest(IntIntMap.Entry entry, Seq<AirborneUnitCallBlock.UnitSpacePlan> plans){
        return entry.value > 0 && isValidPlanIndex(entry.key, plans);
    }
}
