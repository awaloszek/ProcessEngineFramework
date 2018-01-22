package com.bpmnengine.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * @author André Waloszek
 */
public class ListBuilder {

    public static ListBuilder of(List source) {
        return new ListBuilder(source);
    }

    private List elements;

    private List operations = new ArrayList();

    private int limit = -1;

    private ListBuilder(List source) {
        this.elements = new ArrayList(source.size());
        this.elements.addAll(source);
    }

    public ListBuilder filter(Predicate predicate) {
        operations.add(predicate);
        return this;
    }

    public ListBuilder first() {
        limit = 1;
        return this;
    }

    public ListBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public ListBuilder transform(Function function) {
        operations.add(function);
        return this;
    }

    private void applyOperations() {
        for (Object operation : operations) {

            if (operation instanceof Function) {
                applyFunction((Function) operation);

            } else if (operation instanceof Predicate) {
                applyPredicate((Predicate) operation);

            } else
                throw new UnsupportedOperationException();
        }
    }

    private void applyPredicate(Predicate predicate) {
        List target = new ArrayList();
        for (Object element : elements) {
            if (predicate.apply(element))
                target.add(element);
        }
        elements = target;
    }

    private void applyFunction(Function function) {
        List target = new ArrayList(elements.size());
        for (Object element : elements) {
            target.add(function.apply(element));
        }
        elements = target;
    }

    public List toList() {
        applyOperations();
        if (limit > 0)
            return elements.subList(0, limit);

        List result = new ArrayList(elements.size());
        result.addAll(elements);
        return result;
    }

    public Object toElement() {
        applyOperations();
        return elements.size() > 0 ? elements.get(0) : null;
    }
}
