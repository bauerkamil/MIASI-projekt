package interpreter;

public class BaseClass {
    public BaseClass(String stringValue, ClassType type) {
        this.setValue(stringValue, type);
    }
    public BaseClass(Object value, ClassType type) {
        this.value = value;
        this.type = type;
    }

    private Object value = null;

    private ClassType type;

    public Object getValue() {
        return value;
    }

    public void setValue(String stringValue, ClassType type) {
        switch (type)
        {
            case Int -> {
                this.value = Integer.valueOf(stringValue);
            }
            case Double -> {
                this.value = Double.valueOf(stringValue);
            }
            case Boolean -> {
                this.value = Boolean.valueOf(stringValue);
            }
        }
        this.type = type;
    }

    public ClassType getType() {
        return type;
    }

    public Boolean toBoolean() {
        if (this.type == ClassType.Boolean) {
            return (Boolean) value;
        } else {
            throw new ClassCastException();
        }
    }

    public Double toDouble() {
        switch (this.type) {
            case Double -> {
                return (Double) value;
            }
            case Int -> {
                return ((Integer) value).doubleValue();
            }
            default -> throw new ClassCastException();
        }
    }

    public Integer toInt() {
        switch (this.type) {
            case Double -> {
                return (Integer) value;
            }
            case Int -> {
                return ((Double) value).intValue();
            }
            default -> throw new ClassCastException();
        }
    }

    public boolean equals(BaseClass obj) {
        return obj.getType() == this.type && obj.getValue() == this.value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
