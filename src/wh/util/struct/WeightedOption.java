package wh.util.struct;

/**
 * Weighted random option.
 * weight controls probability, option is the callback to run when selected.
 */
public class WeightedOption{
    public float weight;
    public Runnable option;

    public WeightedOption(){
        this(0f, () -> {
        });
    }

    public WeightedOption(float weight, Runnable option){
        this.weight = Math.max(0f, weight);
        this.option = option == null ? () -> {
        } : option;
    }

    public WeightedOption set(float weight, Runnable option){
        this.weight = Math.max(0f, weight);
        this.option = option == null ? () -> {
        } : option;
        return this;
    }

    public void setWeight(float weight){
        this.weight = Math.max(0f, weight);
    }
}
