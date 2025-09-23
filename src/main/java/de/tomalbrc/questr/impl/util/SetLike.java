package de.tomalbrc.questr.impl.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface SetLike<T> extends Iterable<T> {
    boolean contains(T element);

    @Override
    @NotNull
    Iterator<T> iterator();
}