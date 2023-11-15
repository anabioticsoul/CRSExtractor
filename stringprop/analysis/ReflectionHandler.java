package stringprop.analysis;

import stringprop.fact.SPFact;
import jas.Pair;
import pascal.taie.analysis.ClassAnalysis;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.CFGBuilder;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.exp.ClassLiteral;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JClass;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import utils.ResultHandler;

import java.util.Collection;
import java.util.List;

public class ReflectionHandler extends ClassAnalysis<Void> {



    public static final String ID = "reflection-handler";

    public ReflectionHandler(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Void analyze(JClass jClass) {

        if(!jClass.toString().equals("org.apache.hadoop.hdfs.DFSClient")){
            return null;
        }

        Collection<JField> declaredFields = jClass.getDeclaredFields();

        Collection<JMethod> declaredMethods = jClass.getDeclaredMethods();


        declaredMethods.forEach(jMethod -> {
            if(jMethod.isAbstract()){
               return;
            }


            CFG<Stmt> cfg = jMethod.getIR().getResult(CFGBuilder.ID);
            cfg.forEach(stmt -> {
                if(stmt instanceof Invoke invoke){
                    if(!invoke.getMethodRef().getDeclaringClass().toString().contains("hadoop")){
                        return;
                    }

                    if(!invoke.isDynamic()){
                        JMethod resolve = invoke.getMethodRef().resolve();

                        if(ResultHandler.mappingFromMethodToClassName.containsKey(resolve)){
                            Pair<Integer, List<String>> pair = ResultHandler.mappingFromMethodToClassName.get(resolve);
                            Integer index = pair.getO1();
                            int count = 0;
                            for (Var arg : invoke.getInvokeExp().getArgs()) {
                                count++;
                                if(index.equals(count)){
                                    String clazzName = null;
                                    if(arg.isConst()){
                                        if(arg.getConstValue() instanceof ClassLiteral classLiteral){
//                                            System.err.println(classLiteral.getTypeValue().getName());

//                                            clazzName = classLiteral.getTypeValue().getName();
                                            /*
                                             * getName() is full name
                                             */
                                            clazzName = classLiteral.getTypeValue().getName().split("\\.")[classLiteral.getTypeValue().getName().split("\\.").length-1];
                                        }
                                    }

                                    List<String> stringList = pair.getO2();
                                    stringList.add(clazzName);
                                    Pair<Integer, List<String>> suspect = new Pair<>(index, stringList);
                                    ResultHandler.mappingFromMethodToClassName.put(resolve,suspect);

                                }
                            }
                        }

                    }
                }

            });
        });

        return null;
    }

}
