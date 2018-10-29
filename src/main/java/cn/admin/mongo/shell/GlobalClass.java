package cn.admin.mongo.shell;

import org.mozilla.javascript.*;

public class GlobalClass extends ScriptableObject {

    public static void print(Context cx, Scriptable thisObj,
                             Object[] args, Function funObj)
    {
        for (int i=0; i < args.length; i++) {
            if (i > 0)
                System.out.print(" ");

            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);

            System.out.print(s);
        }
        System.out.println();
    }

    @Override
    public String getClassName() {
        return "global";
    }
}
