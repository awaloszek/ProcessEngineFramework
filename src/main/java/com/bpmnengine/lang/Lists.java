package com.bpmnengine.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class Lists {

    public static List filter(List source, Predicate predicate) {
        List target = new ArrayList();
        for (Object element : source) {
            if (predicate.apply(element))
                target.add(element);
        }
        return target;
    }

    public static Object first(List source, Predicate predicate) {
        for (Object element : source) {
            if (predicate.apply(element))
                return element;
        }
        return null;
    }
}
