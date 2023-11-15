package conf;

public record Config<T1, T2>(T1 first, T2 second) implements  Comparable<Config<T1, T2>> {

    public Config(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public String toString() {
        return "<" + this.first + ", " + this.second + ">";
    }

    public T1 getConfigName() {
        return this.first;
    }

    public T2 getConfigValue() {
        return this.second;
    }

    @Override
    public int compareTo(Config<T1, T2> config) {
        return this.first.toString().compareTo(config.first.toString());
    }
}
