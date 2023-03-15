public abstract class PriorityItem<T> {
    private T item;
    private int priority;

    PriorityItem(T item, int priority) {
        this.item = item;
        this.priority = priority;
    }

    public Object getItem() {
        return item;
    }

    public int getPriority() {
        return priority;
    }
}
