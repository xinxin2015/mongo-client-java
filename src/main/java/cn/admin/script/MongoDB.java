package cn.admin.script;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Scanner;


public class MongoDB {

    static class MyFactory extends ContextFactory
    {
        @Override
        protected boolean hasFeature(Context cx, int featureIndex)
        {
            if (featureIndex == Context.FEATURE_DYNAMIC_SCOPE) {
                return false;
            }
            return super.hasFeature(cx, featureIndex);
        }
    }

    static {
        ContextFactory.initGlobal(new MyFactory());
    }

    public static void main(String[] args) throws InterruptedException {

        Context context = Context.enter();
        try {
            GlobalClass globalClass = new GlobalClass();
            Scriptable scope = context.initStandardObjects(globalClass);

            ScriptableObject.defineClass(globalClass, MongoClass.CursorClass.class);
            ScriptableObject.defineClass(globalClass,MongoClass.class);
            ScriptableObject.defineClass(globalClass,DBClass.class);
            ScriptableObject.defineClass(globalClass,DBCollection.class);
            ScriptableObject.defineClass(globalClass,NumberLong.class);
            ScriptableObject.defineClass(globalClass,ObjectId.class);
            new MongoDB().processSource(context,scope);
            Object result = context.evaluateString(globalClass,"connect('localhost/test','local'," +
                    "'local')","shell",1,null);
            ScriptableObject.putProperty(globalClass,"db",result);
            Object t = ScriptableObject.getProperty(globalClass,"db");
            context.evaluateString(globalClass,"print(new Mongo().getDBs())","shell",1,
                    null);
            context.evaluateString(globalClass,"print(db.getCollection('lll').help())","shell",1,
                    null);
            context.evaluateString(globalClass,"print(db.test1111.findOne())","shell",1,null);
            context.evaluateString(globalClass,"db.test.insert({'name':'1111'})","shell",1,null);
            Scanner s = new Scanner(System.in);

            Thread thread1 = new Thread(() -> {
                Context context1 = Context.enter();
               Scriptable threadScope = context1.newObject(globalClass);
                threadScope.setPrototype(globalClass);
                threadScope.setParentScope(null);
                Object thread1Result = context1.evaluateString(threadScope,"connect" +
                        "('localhost/test1111" +
                        "','local'," +
                        "'local')","shell",1,null);
                ScriptableObject.putProperty(threadScope,"db",thread1Result);
                context1.evaluateString(threadScope,"print(db)","shell",1,
                        null);
            });

            Thread thread2 = new Thread(() -> {
                Context context2 = Context.enter();
                Scriptable threadScope = context2.newObject(globalClass);
                threadScope.setPrototype(globalClass);
                threadScope.setParentScope(null);
                Object thread2Result = context2.evaluateString(threadScope,"connect" +
                        "('localhost/test111" +
                        "','local'," +
                        "'local')","shell",1,null);
                ScriptableObject.putProperty(threadScope,"db",thread2Result);
                context2.evaluateString(threadScope,"print(db)","shell",1,
                        null);
            });

            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            context.evaluateString(globalClass,"print(db)","shell",1,
                    null);

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            Context.exit();
        }
    }

    private void processSource(Context context, Scriptable scope) {
        String path =
                Objects.requireNonNull(this.getClass().getClassLoader().getResource("shell")).getPath().substring(1);
        File parent = new File(path);
        File[] files = parent.listFiles();
        if (files != null) {
            for (File f : files) {
                FileReader in = null;
                try {
                    in = new FileReader(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    context.evaluateReader(scope,in,"<stdin>",0,null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
