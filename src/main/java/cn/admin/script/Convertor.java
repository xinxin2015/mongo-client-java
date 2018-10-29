package cn.admin.script;

import org.mozilla.javascript.Context;

public class Convertor {

    private Context context;

    public Convertor(Context context) {
        this.context = context;
    }

    public String toString(Object value) {
        return Context.toString(value);
    }

    public double toNumber(Object value) {
        return Context.toNumber(value);
    }

    public boolean toBoolean(Object value) {
        return Context.toBoolean(value);
    }

}
