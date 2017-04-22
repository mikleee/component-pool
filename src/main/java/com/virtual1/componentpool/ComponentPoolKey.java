package com.virtual1.componentpool;

/**
 * @author Mikhail Tkachenko
 */
class ComponentPoolKey {
    private final String name;

    ComponentPoolKey(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentPoolKey that = (ComponentPoolKey) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return String.format("name=%s", name);
    }
}
