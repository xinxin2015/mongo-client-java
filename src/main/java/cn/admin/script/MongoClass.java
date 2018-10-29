package cn.admin.script;

import com.alibaba.fastjson.JSON;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.mozilla.javascript.*;
import org.mozilla.javascript.annotations.JSConstructor;
import org.mozilla.javascript.annotations.JSFunction;



public class MongoClass extends ScriptableObject {

    private String host;

    private static MongoClient mongoClient;

    private static ThreadLocal<MongoClient> clients = new ThreadLocal<>();

    public MongoClass() {
        this.host = "";
    }


    @JSConstructor
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                    Function ctorObj,
                                    boolean isNewExpr) {
        MongoClass result = new MongoClass();
        if (args.length == 0 || args[0] == Context.getUndefinedValue()) {
            result.put("host",result,"");
            mongoClient = clients.get();
            if (mongoClient == null) {
                mongoClient = new MongoClient("localhost:27017");
                clients.set(mongoClient);
            }
        } else {
            String host = Context.toString(args[0]);
            result.put("host",result,host);
            mongoClient = clients.get();
            if (mongoClient == null) {
                mongoClient = new MongoClient(host);
                clients.set(mongoClient);
            }
        }
        return result;
    }

    public MongoClass(String host) {
        this.host = host;
    }

    @Override
    public String getClassName() {
        return "Mongo";
    }


    @JSFunction
    public static synchronized Object find(Context cx, Scriptable thisObj,
                                  Object[] args, Function funcObj) {
        if (args.length != 7) {
            throw new IllegalArgumentException("find needs 7 args");
        }
        String ns = Context.toString(args[0]);
        String query = JSON.toJSONString(args[1]);
        String fields = Context.toString(args[2]);// not in use
        Integer limit = Integer.parseInt(Context.toString(args[3]));
        Integer skip = Integer.parseInt(Context.toString(args[4]));
        Integer batchSize = Integer.parseInt(Context.toString(args[5]));
        Integer options = Integer.parseInt(Context.toString(args[6])); //not in use
        int pointIndex = ns.indexOf('.');
        MongoCursor<Document> cursor = getMongoCollection(ns.substring(0,pointIndex),
                ns.substring(pointIndex + 1))
                .find(BsonDocument.parse(query))
                .limit(limit)
                .skip(skip)
                .batchSize(batchSize)
                .iterator();
        Scriptable jsCursor = cx.newObject(thisObj,"Cursor");
        ScriptableObject.putProperty(jsCursor,"cursor",cursor);
        return jsCursor;
    }

    @JSFunction
    public static boolean insert(Context cx, Scriptable thisObj,
                              Object[] args, Function funcObj) {
        if (args == null || args.length != 2) {
            throw new IllegalArgumentException("mongo_insert needs 2 args");
        }
        String ns = Context.toString(args[0]);
        String obj = JSON.toJSONString(args[1]);
        System.out.println(obj);
        int pointIndex = ns.indexOf('.');
        getMongoCollection(ns.substring(0,pointIndex),ns.substring(pointIndex + 1))
                .insertOne(Document.parse(obj));
        return true;
    }

    @JSFunction
    public static Object remove(Context cx, Scriptable thisObj,
                                Object[] args, Function funcObj) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            throw new IllegalArgumentException("mongo_remove needs 2 or 3 arguments");
        }

        boolean justOne = false;
        String ns = Context.toString(args[0]);
        String obj = JSON.toJSONString(args[1]);
        if (args.length > 2) {
            justOne = Context.toBoolean(args[2]);
        }
        int pointIndex = ns.indexOf('.');
        DeleteResult result;
        if (justOne) {
            result = getMongoCollection(ns.substring(0,pointIndex),ns.substring(pointIndex + 1))
                    .deleteOne(Document.parse(obj));
        } else {
            result = getMongoCollection(ns.substring(0,pointIndex),ns.substring(pointIndex + 1))
                    .deleteMany(Document.parse(obj));
        }

        return result.getDeletedCount();
    }

    private static MongoDatabase getMongoDatabase(String databaseName) {
        return mongoClient.getDatabase(databaseName);
    }

    private static MongoCollection<Document> getMongoCollection(String databaseName,
                                                         String collectionName) {
        return mongoClient.getDatabase(databaseName).getCollection(collectionName);
    }

    @JSFunction
    public static Object update(Context cx, Scriptable thisObj,
                                Object[] args, Function funcObj) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException("mongo_update needs at least 3 args");
        }

        String ns = Context.toString(args[0]);
        String query = JSON.toJSONString(args[1]);
        String obj = JSON.toJSONString(args[2]);
        boolean upsert = args.length > 3 && Context.toBoolean(args[3]);
        boolean multi = args.length > 4 && Context.toBoolean(args[4]);

        int pointIndex = ns.indexOf('.');

        if (multi) {
            getMongoCollection(ns.substring(0,pointIndex),ns.substring(pointIndex + 1))
                    .updateMany(Document.parse(query),Document.parse(obj),
                            new UpdateOptions().upsert(upsert));
        } else {
            getMongoCollection(ns.substring(0,pointIndex),ns.substring(pointIndex + 1))
                    .updateOne(Document.parse(query),Document.parse(obj),
                            new UpdateOptions().upsert(upsert));
        }
        return true;
    }

    public static class CursorClass extends ScriptableObject {

        private MongoCursor<Document> cursor;
        @Override
        public String getClassName() {
            return "Cursor";
        }

        public CursorClass() {
        }

        @JSConstructor
        public static Scriptable jsConstructor(Context cx, Object[] args,
                                               Function ctorObj,
                                               boolean isNewExpr) {
            return new CursorClass();
        }

        @JSFunction
        @SuppressWarnings("unchecked")
        public boolean hasNext() {
            cursor = (MongoCursor<Document>) get("cursor");
            return cursor.hasNext();
        }

        @JSFunction
        @SuppressWarnings("unchecked")
        public String next() {
            cursor = (MongoCursor<Document>) get("cursor");
            return cursor.next().toJson();
        }

    }

    @JSFunction
    public synchronized static Scriptable getDB(Context cx, Scriptable thisObj,
                            Object[] args, Function funcObj) {
        String name = "test";
        if (args != null && args.length != 0 && args[0] != Context.getUndefinedValue()) {
            name = Context.toString(args[0]);
        }
        System.out.println(name);
        Scriptable dbClass = cx.newObject(thisObj,"DB",
                new Object[]{thisObj,name});
        MongoDatabase database = mongoClient.getDatabase(name);
        ScriptableObject.putProperty(dbClass,"mongoDatabase",database);
        return dbClass;
    }

}
