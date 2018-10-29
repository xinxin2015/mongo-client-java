package cn.admin.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSFunction;

public class ObjectId extends ScriptableObject {

    private static org.bson.types.ObjectId objectId;

    @Override
    public String getClassName() {
        return "ObjectId";
    }

    @JSFunction
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean isNewExpr) {
        ObjectId result = new ObjectId();

        objectId = new org.bson.types.ObjectId();
        ScriptableObject.putProperty(result,"str",objectId.toString());
        return result;
    }

}
