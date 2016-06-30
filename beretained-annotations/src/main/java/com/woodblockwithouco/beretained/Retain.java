package com.woodblockwithouco.beretained;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by aleksandr on 6/11/16.
 */
@Target(value = ElementType.FIELD)
public @interface Retain {}
