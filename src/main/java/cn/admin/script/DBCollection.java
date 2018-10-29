package cn.admin.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSConstructor;

public class DBCollection extends ScriptableObject {

    public String shortName;
    public String fullName;
    public MongoClass mongo;
    public DBClass db;

    public DBCollection() {

    }

    @Override
    public String getClassName() {
        return "DBCollection";
    }

    public DBCollection(MongoClass mongo,DBClass db,String shortName,String fullName) {
        this.mongo = mongo;
        this.db = db;
        this.shortName = shortName;
        this.fullName = fullName;
    }

    @JSConstructor
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean isNewExpr) {
        DBCollection result = new DBCollection();
        if (args.length == 0 || args[0] == Context.getUndefinedValue() || args.length != 4) {
            result.shortName = "";
            result.mongo = new MongoClass();
            result.db = new DBClass();
            result.fullName = ".";
        } else {
            if (args[0] instanceof MongoClass) {
                result.mongo = (MongoClass) args[0];
                result.put("_mongo",result,args[0]);
            }
            if (args[1] instanceof DBClass) {
                result.db = (DBClass) args[1];
                result.put("_db",result,args[1]);
            }
            result.shortName = Context.toString(args[2]);
            result.fullName = Context.toString(args[3]);
            result.put("_shortName",result,args[2]);
            result.put("_fullName",result,args[3]);
        }
        return result;
    }

    @Override
    public Object get(String name, Scriptable start) {
        return super.get(name, start);
    }
}
