package wh.func;

@SuppressWarnings("unchecked")
public interface VariableFunc<T, R> {
    R apply(T... args);
}
