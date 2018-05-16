package com.trc.android.common.modulize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AppMainPage {
    Class value();
}
