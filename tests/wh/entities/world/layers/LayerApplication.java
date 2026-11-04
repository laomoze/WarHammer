package wh.entities.world.layers;

import arc.Application;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * Preserves the active layer for work posted through the application.
 */
final class LayerApplication implements InvocationHandler {
    private final Application delegate;
    private final LayerContext layer;

    private LayerApplication(Application delegate, LayerContext layer) {
        this.delegate = delegate;
        this.layer = layer;
    }

    static Application create(Application delegate, LayerContext layer) {
        Objects.requireNonNull(delegate, "application");
        while (Proxy.isProxyClass(delegate.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(delegate);
            if (!(handler instanceof LayerApplication)) break;
            delegate = ((LayerApplication) handler).delegate;
        }
        return (Application) Proxy.newProxyInstance(Application.class.getClassLoader(), new Class<?>[]{Application.class},
                new LayerApplication(delegate, layer));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("post") && method.getParameterTypes().length == 1) {
            Runnable task = (Runnable) args[0];
            delegate.post(() -> layer.runPosted(task));
            return null;
        }
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException failure) {
            throw failure.getCause();
        }
    }
}
