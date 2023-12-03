package me.playajames.projectlife.utils;

public class StringUtils {

    public static String trimStringAfterChar(String string, String charSequence) {
        if (string.contains(charSequence))
            return string.split(charSequence)[0];
        return string;
    }

}
