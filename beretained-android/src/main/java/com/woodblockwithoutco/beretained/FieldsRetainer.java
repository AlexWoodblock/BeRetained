package com.woodblockwithoutco.beretained;

/**
 * Internal interface for classes that can save/re
 */
interface FieldsRetainer<T> {
    void onCreate(T source);
    void save(T source);
    boolean restore(T target);
}
