package wh.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.gen.EffectState;
import mindustry.gen.Posc;
import mindustry.gen.Rotc;
import mindustry.world.blocks.RotBlock;

import static mindustry.Vars.state;

/**
 * 触手特效。
 * 1. 每个特效实例的运行时状态保存在 TentacleData 中；
 * 2. Drawn.coronaChain 只负责绘制，不负责状态更新。
 */
public class TentacleEffect extends Effect{
    private static final float MIN_VISIBLE_PROGRESS = 0.0001f;
    private static final long STRAND_SEED_STEP = 131L;
    private static final float ROTATION_OFFSET_MIN = -30f;
    private static final float ROTATION_OFFSET_MAX = 30f;

    // 外观基础参数
    public int rays = 1;
    public float length = 40f;
    public float width = 8f;
    public float spinSpeed = 0.3f;

    // 每条触手的分段范围
    public int minSegments = 8;
    public int maxSegments = 15;

    // 每条触手长度/宽度随机倍数
    public float minLenMul = 1.0f, maxLenMul = 1.5f;
    public float minWidthMul = 1.0f, maxWidthMul = 1.5f;

    // 角速度相关参数
    public float angleDrag = 0.1f;
    public float angularVelocityInherit = 0.1f;
    public float minAngularVelocity = 2.8f, maxAngularVelocity = 4.8f;

    // 触手分批出现延时
    public float strandDelay = 0.018f;

    // 初始形态参数
    public float initialAngleStepMin = 5f;
    public float initialAngleStepMax = 11f;
    public float initialAngleJitter = 15;

    // 根部公转速度范围
    public float orbitSpeedMin = 1f, orbitSpeedMax = 2f;

    public @Nullable Color colorFrom;
    public @Nullable Color colorTo;
    public getTentacleEffect get = effect -> {
    };

    public interface getTentacleEffect{
        void get(TentacleEffect effect);
    }

    public TentacleEffect(float lifetime, float length, float width, int rays, float spinSpeed, Color colorFrom, Color colorTo){
        this.lifetime = lifetime;
        this.length = length;
        this.width = width;
        this.rays = Math.max(1, rays);
        this.spinSpeed = spinSpeed;
        this.colorFrom = colorFrom;
        this.colorTo = colorTo;
        this.clip = 500 + length * maxLenMul;
    }

    public TentacleEffect layer(float layer){
        this.layer = layer;
        return this;
    }

    public TentacleEffect get(getTentacleEffect get){
        this.get = get;
        return this;
    }

    private void ensureInitialized(EffectContainer effect, TentacleData data){
        if(data.initialized) return;

        int strandCount = Math.max(1, rays);
        data.strands = new StrandState[strandCount];

        for(int strandIndex = 0; strandIndex < strandCount; strandIndex++){
            long strandSeed = effect.id + strandIndex * STRAND_SEED_STEP;
            data.strands[strandIndex] = createStrand(effect, strandSeed);
        }

        data.initialized = true;
    }

    private StrandState createStrand(EffectContainer effect, long strandSeed){
        Rand rand = new Rand(strandSeed);
        StrandState strand = new StrandState();

        int segmentCount = rand.random(minSegments, maxSegments);
        strand.nodeX = new float[segmentCount];
        strand.nodeY = new float[segmentCount];
        strand.renderAngleDeg = new float[segmentCount];
        strand.baseAngleDeg = new float[segmentCount];
        strand.angularVelocityDeg = new float[segmentCount];

        float lengthMul = rand.random(minLenMul, maxLenMul);
        strand.segmentLength = length * lengthMul / segmentCount;
        strand.rootWidth = width * rand.random(minWidthMul, maxWidthMul);

        float rotationOffset = rand.random(ROTATION_OFFSET_MIN, ROTATION_OFFSET_MAX);
        float strandRotationDeg = effect.rotation + rotationOffset;
        float rootAngleDeg = strandRotationDeg + rand.random(0f, 360f);
        float stepDeg = rand.random(initialAngleStepMin, initialAngleStepMax);
        float baseAngularVelocityDeg = rand.random(minAngularVelocity, maxAngularVelocity);

        int directionSign = randomSign(rand);
        int orbitSign = randomSign(rand);
        strand.orbitSpeedDeg = rand.random(orbitSpeedMin, orbitSpeedMax) * orbitSign;

        for(int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++){
            float velocityScale = calcVelocityScale(segmentIndex, segmentCount);
            float jitterDeg = segmentIndex == 0 ? 0f : rand.random(-initialAngleJitter, initialAngleJitter);

            float angleDeg = rootAngleDeg + directionSign * segmentIndex * stepDeg + jitterDeg;
            strand.baseAngleDeg[segmentIndex] = angleDeg;
            strand.renderAngleDeg[segmentIndex] = angleDeg;
            strand.angularVelocityDeg[segmentIndex] = baseAngularVelocityDeg * velocityScale * -directionSign;

            strand.nodeX[segmentIndex] = effect.x;
            strand.nodeY[segmentIndex] = effect.y;
        }

        return strand;
    }

    private static int randomSign(Rand rand){
        return rand.random(0, 1) > 0.5f ? -1 : 1;
    }

    private static float calcVelocityScale(int segmentIndex, int segmentCount){
        float segmentRatio = (segmentIndex + 1f) / segmentCount;
        float headBias = segmentIndex == 0 ? 1f - (1f / segmentCount) : 0f;
        return (segmentRatio + headBias) * (segmentIndex == 0 ? -1f : 1f);
    }

    /** 计算有符号最短角度差，范围 [-180, 180]。 */
    private static float angleDelta(float fromDeg, float toDeg){
        return Mathf.mod(toDeg - fromDeg + 540f, 360f) - 180f;
    }

    /**
     * 更新一条触手。
     * bendFactor: 1=完全弯曲，0=更接近拉直。
     */
    private void updateStrand(StrandState strand, float originX, float originY, float growthProgress, float bendFactor, float rotationOffsetDeg){
        float delta = Time.delta;
        float dragScale = Math.max(0f, 1f - angleDrag * delta);
        float bend = Mathf.clamp(bendFactor);
        float orbitSpeedDeg = strand.orbitSpeedDeg * spinSpeed * bend;
        float segmentStepLength = strand.segmentLength * growthProgress;

        float previousX = originX;
        float previousY = originY;
        int segmentCount = strand.nodeX.length;

        for(int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++){
            strand.baseAngleDeg[segmentIndex] += (strand.angularVelocityDeg[segmentIndex] * bend + orbitSpeedDeg) * delta;

            float headAngleDeg = strand.baseAngleDeg[0] + rotationOffsetDeg;
            float baseAngleDeg = strand.baseAngleDeg[segmentIndex] + rotationOffsetDeg;
            float angleDeg = Mathf.lerp(headAngleDeg, baseAngleDeg, bend);
            strand.renderAngleDeg[segmentIndex] = angleDeg;

            strand.nodeX[segmentIndex] = previousX + Angles.trnsx(angleDeg, segmentStepLength);
            strand.nodeY[segmentIndex] = previousY + Angles.trnsy(angleDeg, segmentStepLength);

            if(segmentIndex + 1 < segmentCount){
                strand.angularVelocityDeg[segmentIndex + 1] += strand.angularVelocityDeg[segmentIndex] * angularVelocityInherit;
            }
            strand.angularVelocityDeg[segmentIndex] *= dragScale;

            previousX = strand.nodeX[segmentIndex];
            previousY = strand.nodeY[segmentIndex];
        }
    }

    private void applyColor(EffectContainer effect){
        if(colorFrom != null || colorTo != null){
            Draw.color(
            colorFrom == null ? effect.color : colorFrom,
            colorTo == null ? effect.color : colorTo,
            effect.fin()
            );
        }else{
            Draw.color(effect.color);
        }
    }

    @Override
    public void render(EffectContainer effect){
        if(!(effect.data instanceof TentacleData data)) return;
        ensureInitialized(effect, data);

        boolean paused = state.isPaused();
        float effectX = effect.x, effectY = effect.y;
        float currentRotation = resolveCurrentRotation(effect, data);

        // 跟踪父对象旋转：只累计一个偏移量，避免每帧遍历并旋转全部段数组。
        if(!paused && data.hasLastRotation){
            data.rotationOffsetDeg += angleDelta(data.lastRotation, currentRotation);
            if(Math.abs(data.rotationOffsetDeg) > 360f){
                data.rotationOffsetDeg = Mathf.mod(data.rotationOffsetDeg, 360f);
            }
        }
        data.lastRotation = currentRotation;
        data.hasLastRotation = true;

        float appear = Mathf.curve(effect.fin(), 0f, 0.1f);
        float fade = effect.fout(Interp.pow2Out) * appear;
        float strandStep = strandDelay * spinSpeed;
        boolean canBreakOnInvisible = strandStep > 0f;

        applyColor(effect);

        for(int strandIndex = 0; strandIndex < data.strands.length; strandIndex++){
            float strandGrowth = fade - strandIndex * strandStep;
            if(strandGrowth <= MIN_VISIBLE_PROGRESS){
                if(canBreakOnInvisible) break;
                continue;
            }
            strandGrowth = Mathf.clamp(strandGrowth);

            StrandState strand = data.strands[strandIndex];
            if(!paused){
                updateStrand(strand, effectX, effectY, strandGrowth, fade, data.rotationOffsetDeg);
            }

            Drawn.coronaChain(
            effectX,
            effectY,
            strand.nodeX,
            strand.nodeY,
            strand.renderAngleDeg,
            strand.rootWidth,
            fade
            );
        }

        Draw.color();
    }

    private float resolveCurrentRotation(EffectContainer effect, TentacleData data){
        if(data.sourceData instanceof Rotc rotc){
            return rotc.rotation();
        }
        if(data.sourceData instanceof RotBlock rotBlock){
            return rotBlock.buildRotation();
        }
        if(data.sourceData instanceof Bullet bullet){
            return bullet.rotation();
        }
        return effect.rotation;
    }

    @Override
    protected void add(float x, float y, float rotation, Color color, Object data){
        EffectState effectState = EffectState.create();
        effectState.effect = this;
        effectState.rotation = baseRotation + rotation;
        effectState.lifetime = lifetime;
        effectState.set(x, y);
        effectState.color.set(color);
        TentacleData tentacleData = new TentacleData(data);
        effectState.data = tentacleData;

        if(followParent){
            Posc parentPos = resolveFollowParent(tentacleData.sourceData);
            if(parentPos != null){
                effectState.parent = parentPos;
                // 注意：父对象不是 Rotc/RotBlock 时，rotWithParent=true 会导致 ChildComp 不更新坐标。
                boolean parentSupportsRotation = parentPos instanceof Rotc || parentPos instanceof RotBlock;
                effectState.rotWithParent = rotWithParent && parentSupportsRotation;
            }
        }
        get.get(this);
        effectState.add();
    }

    private @Nullable Posc resolveFollowParent(Object sourceData){
        if(sourceData instanceof Posc posc){
            return posc;
        }
        return null;
    }

    private static class StrandState{
        float[] nodeX, nodeY, renderAngleDeg, baseAngleDeg, angularVelocityDeg;
        float segmentLength;
        float rootWidth;
        float orbitSpeedDeg;
    }

    private static class TentacleData{
        StrandState[] strands;
        boolean initialized;
        boolean hasLastRotation;
        float lastRotation;
        float rotationOffsetDeg;
        final Object sourceData;

        TentacleData(Object sourceData){
            this.sourceData = sourceData;
        }
    }
}
