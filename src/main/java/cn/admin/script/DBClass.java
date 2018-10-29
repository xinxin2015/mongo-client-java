package cn.admin.script;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.mozilla.javascript.*;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBClass extends ScriptableObject {

    private String supportCommands[] = {
            "addUser",
            "auth",
            "cloneDatabase",
            "commandHelp",
            "copyDatabase",
            "createCollection",
            "currentOp",
            "dropDatabase",
            "eval",
            "getCollection",
            "getCollectionNames",
            "getLastError",
            "getLastErrorObj",
            "getMongo",
            "getName",
            "getPrevError",
            "getProfilingLevel",
            "getProfilingStatus",
            "getReplicationInfo",
            "getSiblingDB",
            "isMaster",
            "killOp",
            "listCommands",
            "printCollectionStats",
            "printReplicationInfo",
            "printSlaveReplicationInfo",
            "printShardingStatus",
            "removeUser",
            "repairDatabase",
            "resetError",
            "runCommand",
            "serverStatus",
            "setProfilingLevel",
            "shutdownServer",
            "stats",
            "version",
            "toString",
            "valueOf",
            "__noSuchMethod__",
            "help",
            "__pwHash"
    };


    public MongoClass _mongo;
    public String _name;

    public DBClass() {
        this._name = "";
        this._mongo = new MongoClass();
    }

    public DBClass(MongoClass mongo, String name) {
        this._mongo = mongo;
        this._name = name;
    }

    @JSConstructor
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean isNewExpr) {
        DBClass result = new DBClass();
        if (args.length == 0 || args[0] == Context.getUndefinedValue() || args.length != 2) {
            result._name = "";
            result._mongo = new MongoClass();
        } else {
            if (args[0] instanceof MongoClass) {
                result._mongo = (MongoClass) args[0];
                result.put("_mongo", result, args[0]);
            }
            result._name = Context.toString(args[1]);
            result.put("_name", result, args[1]);
        }
        return result;
    }

    @Override
    public String getClassName() {
        return "DB";
    }

    @Override
    public Object get(String name, Scriptable start) {
        //TODO 更好的实现
        /**
         * mongo命令:db.coll.find()
         * coll为mongo的属性，重写此方法动态添加supportCommand不包含的property
         */
        List<String> supportCommand = Arrays.asList(supportCommands);
        if (!has(name, start) && !supportCommand.contains(name)) {
            Context cx = Context.getCurrentContext();
            Object mongo = super.get("_mongo", start);
            Object dbName = super.get("_name", start);
            String fullName = dbName + "." + name;
            Scriptable dbCollection = cx.newObject(start,
                    "DBCollection", new Object[]{mongo, this, name, fullName});
            put(name, start, dbCollection);
        }
        return super.get(name, start);
    }

    @JSFunction
    public static Object runCommand(Context cx, Scriptable thisObj,
                                    Object[] args, Function funcObj) {
        Map<String,Object> map = new HashMap<>();
        String result = null;
        if (args != null && args.length != 0) {
            if (args[0] instanceof String) {
                map.put(String.valueOf(args[0]),1);
                result = JSON.toJSONString(map);
            } else {
                result = JSON.toJSONString(args[0]);
            }
        }
        MongoDatabase mongoDatabase = (MongoDatabase) thisObj.get("mongoDatabase",thisObj);
        Document document = mongoDatabase.runCommand(BsonDocument.parse(result));
        return Context.javaToJS(document.toJson(),thisObj);
    }

}
