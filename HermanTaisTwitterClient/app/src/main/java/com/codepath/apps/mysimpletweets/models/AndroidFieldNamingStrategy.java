package com.codepath.apps.mysimpletweets.models;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

public class AndroidFieldNamingStrategy implements FieldNamingStrategy {
    @Override
    public String translateName(Field f) {
        String name = f.getName();
        if (name.length() >= 2) {
            if (name.charAt(0) == 'm' && Character.isUpperCase(name.charAt(1))) {
                // get rid of the "m" prefix (that means member variables in Android)
                name = name.substring(1);

                StringBuilder translation = new StringBuilder();
                for (int i = 0; i < name.length(); i++) {
                    char character = name.charAt(i);
                    if (Character.isUpperCase(character) && translation.length() != 0) {
                        translation.append("_");
                    }
                    translation.append(character);
                }
                return translation.toString().toLowerCase();
            }
        }
        return name;
    }
}
