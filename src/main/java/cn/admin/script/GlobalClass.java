package cn.admin.script;

import com.alibaba.fastjson.JSON;
import org.mozilla.javascript.*;
import org.mozilla.javascript.annotations.JSFunction;


public class GlobalClass extends ScriptableObject {

    @Override
    public String getClassName() {
        return "global";
    }

    public GlobalClass() {
        this.defineFunctionProperties(new String[]{"print","hex_md5"},getClass(),DONTENUM);
    }

    @JSFunction
    public static void print(Context cx, Scriptable thisObj,
                      Object[] args, Function funcObj) {
        for (Object object : args) {
            if (object instanceof NativeObject) {
                System.out.println(JSON.toJSON(object));
            } else {
                System.out.println(Context.toString(object));
            }
        }
    }

    @JSFunction
    public static String hex_md5(Context cx, Scriptable thisObj,
                                 Object[] args, Function funcObj) {
        return MD5Utils.hexMd5((String) args[0]);
    }

}
