package br.org.sistemafieg.aquecimentoapp.util;

public class UtilConvert {

    public static String convertIntegerToString(Integer value) {
        if(value == null){
            return "";
        }

        return value.toString();
    }

    public static Float convertDoubleToFloat(Double value) {
        if(value == null){
            return 0.0f;
        }

        return value.floatValue();
    }



}
