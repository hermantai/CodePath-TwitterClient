package com.codepath.apps.mysimpletweets.models;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

public class AndroidFieldNamingStrategy implements FieldNamingStrategy {
    @Override
    public String translateName(Field f) {
        String name = f.getName();
        if (name.equals("mUid")) {
            // Tweet class use "mUid" for "id" in Twitter API because Model (from ActiveAndroid)
            // has a private field called "mId", which confuses Gson.
            return "id";
        }
        if (name.equals("mId")) {
            // Mode (from ActiveAndroid) has a field called mId, so we need to map it to some random
            // field name for Gson.
            return "active_android_id";
        }
        if (name.length() >= 2) {
            if (name.charAt(0) == 'm' && Character.isUpperCase(name.charAt(1))) {
                // get rid of the "m" prefix (that means member variables in Android)
                name = name.substring(1);
            }
        }
        return camelCaseToLowercaseUnderscores(name);
    }

    private String camelCaseToLowercaseUnderscores(String s) {
        StringBuilder translation = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char character = s.charAt(i);
            if (Character.isUpperCase(character) && translation.length() != 0) {
                translation.append("_");
            }
            translation.append(character);
        }
        return translation.toString().toLowerCase();
    }
}
