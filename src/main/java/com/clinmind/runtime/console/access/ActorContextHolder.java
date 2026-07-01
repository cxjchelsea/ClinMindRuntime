package com.clinmind.runtime.console.access;

public final class ActorContextHolder {

    private static final ThreadLocal<ActorContext> CONTEXT = new ThreadLocal<>();

    private ActorContextHolder() {
    }

    public static void set(ActorContext context) {
        CONTEXT.set(context);
    }

    public static ActorContext get() {
        return CONTEXT.get();
    }

    public static ActorContext getRequired() {
        ActorContext context = CONTEXT.get();
        if (context == null) {
            throw new ActorContextRequiredException("Actor context is not available");
        }
        return context;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
