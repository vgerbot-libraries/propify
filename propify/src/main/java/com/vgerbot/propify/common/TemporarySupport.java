package com.vgerbot.propify.common;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@Documented
public @interface TemporarySupport {
    String value() default "";
}