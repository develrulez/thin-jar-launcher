package org.develrulez.thinjar.util;

public abstract class LazyInitializer<T> {

    private static final Object NO_INIT = new Object();

    @SuppressWarnings("unchecked")
    /** Stores the managed object. */
    private volatile T object = (T) NO_INIT;

    /**
     * Returns the object wrapped by this instance. On first access the object
     * is created. After that it is cached and can be accessed pretty fast.
     *
     * @return the object initialized by this {@code LazyInitializer}
     * @throws ConcurrentException if an error occurred during initialization of
     *                             the object
     */
    public T get() {
        // use a temporary variable to reduce the number of reads of the
        // volatile field
        T result = object;

        if (result == NO_INIT) {
            synchronized (this) {
                result = object;
                if (result == NO_INIT) {
                    object = result = initialize();
                }
            }
        }

        return result;
    }

    /**
     * Creates and initializes the object managed by this {@code
     * LazyInitializer}. This method is called by {@link #get()} when the object
     * is accessed for the first time. An implementation can focus on the
     * creation of the object. No synchronization is needed, as this is already
     * handled by {@code get()}.
     *
     * @return the managed data object
     * @throws ConcurrentException if an error occurs during object creation
     */
    protected abstract T initialize();
}
