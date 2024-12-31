package wh.util;

import arc.func.*;

import java.lang.reflect.*;

import static mindustry.Vars.*;

@SuppressWarnings({"unchecked", "unused"})
public final class ReflectUtils {
    /** ReflectUtils should not be instantiated. */
    private ReflectUtils() {}

    public static <T> T getf(Object object, String type, String name) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getClassLoader().loadClass(type).getDeclaredField(name);
        field.setAccessible(true);

        return (T) field.get(object);
    }

    public static <T> T[] newArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    public static <T> T[] newArray(T[] oldType, int length) {
        return (T[]) Array.newInstance(oldType.getClass().getComponentType(), length);
    }

    public static boolean isWrapper(Class<?> type) {
        return type == Byte.class || type == Short.class || type == Integer.class || type == Long.class || type == Character.class || type == Boolean.class || type == Float.class || type == Double.class;
    }

    public static <T> Prov<T> cons(Class<T> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> c = type.getDeclaredConstructor();
        c.setAccessible(true);
        return (Prov<T>) c.newInstance();
    }

    public static <T> T get(Field field) throws IllegalAccessException {
        return get(null, field);
    }

    public static <T> T get(Object object, Field field) throws IllegalAccessException {
        return (T) field.get(object);
    }

    public static <T> T get(Class<?> type, Object object, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static <T> T get(Object object, String name) throws NoSuchFieldException, IllegalAccessException {
        return get(object.getClass(), object, name);
    }

    public static <T> T get(Class<?> type, String name) throws NoSuchFieldException, IllegalAccessException {
        return get(type, null, name);
    }

    public static void set(Class<?> type, Object object, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }

    public static void set(Object object, Field field, Object value) throws IllegalAccessException {
        field.set(object, value);
    }

    public static void set(Object object, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        set(object.getClass(), object, name, value);
    }

    public static void set(Class<?> type, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        set(type, null, name, value);
    }

    public static <T> T invoke(Class<?> type, Object object, String name, Object[] args, Class<?>... parameterTypes) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = type.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return (T) method.invoke(object, args);
    }

    public static <T> T invoke(Class<?> type, String name, Object[] args, Class<?>... parameterTypes) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return invoke(type, null, name, args, parameterTypes);
    }

    public static <T> T invoke(Class<?> type, String name) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return invoke(type, name, null);
    }

    public static <T> T invoke(Object object, String name, Object[] args, Class<?>... parameterTypes) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return invoke(object.getClass(), object, name, args, parameterTypes);
    }

    public static <T> T invoke(Object object, String name) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return invoke(object, name, null);
    }

    public static <T> T make(String type) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<T> c = (Class<T>) Class.forName(type);
        return c.getDeclaredConstructor().newInstance();
    }

    public static Class<?> box(Class<?> type) {
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == float.class) return Float.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        return type;
    }

    public static Class<?> unbox(Class<?> type) {
        if (type == Boolean.class) return boolean.class;
        if (type == Byte.class) return byte.class;
        if (type == Character.class) return char.class;
        if (type == Short.class) return short.class;
        if (type == Integer.class) return int.class;
        if (type == Float.class) return float.class;
        if (type == Long.class) return long.class;
        if (type == Double.class) return double.class;
        return type;
    }

    public static String def(Class<?> type) {
        String t = unbox(type).getSimpleName();
        return switch (t) {
            case "boolean" -> "false";
            case "byte", "char", "short", "int", "long" -> "0";
            case "float", "double" -> "0.0";
            default -> "null";
        };
    }

    /** Finds a class from the parent classes that has a specific field. */
    public static Class<?> findClassf(Class<?> type, String field) {
        for (type = type.isAnonymousClass() ? type.getSuperclass() : type; type != null; type = type.getSuperclass()) {
            try {
                type.getDeclaredField(field);
                break;
            } catch (NoSuchFieldException ignored) {}
        }

        return type;
    }

    /** Finds a class from the parent classes that has a specific method. */
    public static Class<?> findClassm(Class<?> type, String method, Class<?>... args) {
        for (type = type.isAnonymousClass() ? type.getSuperclass() : type; type != null; type = type.getSuperclass()) {
            try {
                type.getDeclaredMethod(method, args);
                break;
            } catch (NoSuchMethodException ignored) {}
        }

        return type;
    }

    /** Finds a class from the parent classes that has a specific constructor. */
    public static Class<?> findClassc(Class<?> type, Class<?>... args) {
        for (type = type.isAnonymousClass() ? type.getSuperclass() : type; type != null; type = type.getSuperclass()) {
            try {
                type.getDeclaredConstructor(args);
                break;
            } catch (NoSuchMethodException ignored) {}
        }

        return type;
    }

    public static Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, mods.mainLoader());
    }

    /** A utility function to find a field without throwing exceptions. */
    public static Field findField(Class<?> type, String field, boolean access) throws NoSuchFieldException {
        Field f = findClassf(type, field).getDeclaredField(field);
        if (access) f.setAccessible(true);

        return f;
    }

    /** Sets a field of a model without throwing exceptions. */
    public static void setField(Object object, Field field, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(object, value);
    }

    /** Gets a value from a field of a model without throwing exceptions. */
    public static <T> T getFieldValue(Object object, Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return (T) field.get(object);
    }

    /** Gets a value from a field of a model without throwing exceptions. */
    public static <T, F extends U, U> T getFieldValue(F object, Class<U> subclass, String fieldname) throws NoSuchFieldException, IllegalAccessException {
        Field field = subclass.getDeclaredField(fieldname);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    /** Gets a field of a model without throwing exceptions. */
    public static Field getField(Object object, String field) throws NoSuchFieldException {
        return object.getClass().getDeclaredField(field);
    }

    /** Gets a value from a field of a model without throwing exceptions. */
    public static <T> T getField(Object object, Field field) throws IllegalAccessException {
        return (T) field.get(object);
    }

    /** A utility function to find a method without throwing exceptions. */
    public static Method findMethod(Class<?> type, String methodName, boolean access, Class<?>... args) throws NoSuchMethodException {
        Method m = findClassm(type, methodName, args).getDeclaredMethod(methodName, args);
        if (access) m.setAccessible(true);

        return m;
    }

    /** Reflectively invokes a method without throwing exceptions. */
    public static <T> T invokeMethod(Object object, String method, Object... args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        return (T) findMethod(object.getClass(), method, true).invoke(object, args);
    }

    /** Reflectively invokes a method without throwing exceptions. */
    public static <T> T invokeMethod(Object object, Method method, Object... args) throws InvocationTargetException, IllegalAccessException {
        return (T) method.invoke(object, args);
    }

    /** A utility function to find a constructor without throwing exceptions. */
    public static <T> Constructor<T> findConstructor(Class<T> type, boolean access, Class<?>... args) throws NoSuchMethodException {
        Constructor<T> c = ((Class<T>) findClassc(type, args)).getDeclaredConstructor(args);
        if (access) c.setAccessible(true);

        return c;
    }

    /** Reflectively instantiates a type without throwing exceptions. */
    public static <T> T newInstance(Constructor<T> constructor, Object... args) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructor.newInstance(args);
    }

    public static Class<?> classCaller() throws ClassNotFoundException {
        Thread thread = Thread.currentThread();
        StackTraceElement[] trace = thread.getStackTrace();
        return Class.forName(trace[3].getClassName(), false, mods.mainLoader());
    }
}
