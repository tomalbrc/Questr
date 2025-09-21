package de.tomalbrc.questr.impl.navigationbar;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class NavigationBarList extends ObjectArrayList<NavigationBar> {
    @Override
    public boolean add(NavigationBar data) {
        dirty = true;
        return super.add(data);
    }

    @Override
    public void add(int index, NavigationBar data) {
        dirty = true;
        super.add(index, data);
    }

    @Override
    public NavigationBar remove(int idx) {
        dirty = true;
        return super.remove(idx);
    }

    @Override
    public boolean remove(Object data) {
        dirty = true;
        return super.remove(data);
    }

    public boolean hasVisible() {
        for (NavigationBar data : this) {
            if (data.isVisible()) return true;
        }

        return false;
    }

    public void hideAll() {
        for (NavigationBar data : this) {
            data.setVisible(false);
        }
    }

    public boolean dirty = true;
}