package com.zhuoli.earning.Utility;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 8/31/16.
 */
public class RetryManager<T,R>{

    Function<T, R> function = null;

    public RetryManager(Function<T,R> function)
    {
        this.function = function;
    }

    public R Execute(T t) throws Exception {
        Exception firstExc = null;
        for (int i = 0; i < 3; i++) {
            try {
                return this.function.apply(t);
            }
            catch (Exception exc)
            {
                if (firstExc != null)
                    firstExc = exc;
                Logger.getGlobal().log(Level.SEVERE, "Retry " + i, exc);
                continue;
            }
        }
        throw firstExc;
    }
}
