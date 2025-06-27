package com.tennisfolio.Tennisfolio.util;

import java.util.Objects;

public class FiledUpdateUtil {

    private FiledUpdateUtil(){}

    public static <T> T updated(T oldValue, T newValue){
        return (!Objects.equals(oldValue, newValue))? newValue : oldValue;
    }
}
