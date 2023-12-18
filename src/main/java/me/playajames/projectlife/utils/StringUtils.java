package me.playajames.projectlife.utils;

public class StringUtils {

    public static String trimStringAfterChar(String string, String charSequence) {
        if (string.contains(charSequence)) {
            String[] stringArray = string.split(charSequence);
            if (stringArray.length > 0)
                return string.split(charSequence)[0];
        }
        return string;
    }

}
