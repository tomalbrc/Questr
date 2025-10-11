package de.tomalbrc.questr.impl.navigationbar.component.type;

import de.tomalbrc.questr.impl.navigationbar.NavigationBarConfig;
import de.tomalbrc.questr.impl.navigationbar.component.NavigationBarComponent;

public class NavigationBarComponentType<T extends NavigationBarComponent> {

    @FunctionalInterface
    public interface Creator<T> {
        T create(NavigationBarConfig config, int line);
    }

    final Creator<T> creator;

    public NavigationBarComponentType(Creator<T> creator) {
        this.creator = creator;
    }

    public T create(NavigationBarConfig config, int line) {
        return this.creator.create(config, line);
    }
}
