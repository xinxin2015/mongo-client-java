package cn.admin.serialize;

import java.io.Serializable;

public class Test implements Serializable {

    public String data = "data";

    public String code = "code";

    @Override
    public String toString() {
        return data + ":" + code;
    }
}
