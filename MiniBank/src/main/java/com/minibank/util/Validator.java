package com.minibank.util;

import com.minibank.model.annotation.MaxLength;
import com.minibank.model.annotation.Positive;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class Validator {
    private Validator() {}

    public static String[] validate(Object obj) {
        List<String> errors = new ArrayList<>();
        if (obj == null) return new String[]{"Object cannot be null."};

        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (field.isAnnotationPresent(Positive.class) && value instanceof Number number) {
                    Positive rule = field.getAnnotation(Positive.class);
                    if (number.longValue() <= 0) {
                        errors.add(field.getName() + ": " + rule.message());
                    }
                }

                if (field.isAnnotationPresent(MaxLength.class) && value instanceof String str) {
                    int max = field.getAnnotation(MaxLength.class).value();
                    if (str.length() > max) {
                        errors.add(field.getName() + ": maximum length is " + max);
                    }
                }
            } catch (ReflectiveOperationException | RuntimeException ex) {
                errors.add("Could not validate field " + field.getName());
            }
        }
        return errors.toArray(new String[0]);
    }
}
