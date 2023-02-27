package stringprop.analysis;

import jas.Pair;
import pascal.taie.analysis.MethodAnalysis;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.cfg.CFGBuilder;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.proginfo.FieldRef;
import pascal.taie.ir.stmt.DefinitionStmt;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.ir.stmt.StoreField;
import pascal.taie.language.classes.JField;
import utils.ResultHandler;

import java.util.ArrayList;

public class StaticFieldIdentifier extends MethodAnalysis<Void> {

    // declare field ID
    public static final String ID = "staticfieldidentifier";

    public StaticFieldIdentifier(AnalysisConfig config) {
        super(config);
    }

    @Override
    public Void analyze(IR ir) {
        CFG<Stmt> cfg = ir.getResult(CFGBuilder.ID);
//        if(!cfg.getIR().getMethod().getDeclaringClass().toString().contains("Configuration"))return null;

        /*
         * exclude classes other than hadoop and hbase
         */
//        if(!cfg.getMethod().getDeclaringClass().toString().contains("hadoop") || !cfg.getMethod().getDeclaringClass().toString().contains("hbase")){
//            return null;
//        }


        cfg.forEach(stmt->{
            if(stmt instanceof DefinitionStmt<?,?> definitionStmt){
                LValue lValue = definitionStmt.getLValue();
                RValue rValue = definitionStmt.getRValue();
                if(lValue instanceof Var lhs && rValue instanceof StringLiteral literal){

//                    Recorder.localVar.put(lhs,StringValue.makeConstant(literal.getString()));
//                    ValueInfo valueInfo = new ValueInfo(lhs.getMethod().getDeclaringClass(), lhs.getMethod(), lhs);
                    ResultHandler.localVars.put(lhs, literal.getString().replace("\"", ""));
                }
            }
            /*
             * handle Store
             */
            if(stmt instanceof StoreField storeField){
                FieldRef fieldRef = storeField.getFieldRef();
                if(fieldRef.isStatic()){
                    JField resolve = fieldRef.resolve();
                    Var rValue = storeField.getRValue();
//                    StringValue stringValue = in.get(rValue);
//                    ValueInfo valueInfo = new ValueInfo(rValue.getMethod().getDeclaringClass(), rValue.getMethod(), rValue);
                    String stringValue = ResultHandler.localVars.get(rValue);
                    /*
                     * put the static field into ResultHandler
                     */
                    //TODO use compute() to solve NullPointerException
                    ResultHandler.staticFields.compute(resolve, (k, oldValue) -> stringValue);
                    //FIXME will cause NullPointerException
//                    Recorder.staticField.put(resolve,stringValue);
                    FieldAccess lValue = storeField.getLValue();
//                    Recorder.staticField.put(lhs.getName(),StringValue.makeConstant(replace));
                }
            }

            /*
             * handle reflection
             */
            if(stmt instanceof Invoke invoke){
                String methodName = invoke.getMethodRef().getName();
                if(invoke.getMethodRef().getName().equals("forName")){



                }

                if(methodName.contains("getSimpleName")){

                    Var base = ((InvokeInstanceExp) invoke.getInvokeExp()).getBase();


                    /*
                     * get the method param count
                     */
                    int paramCount = 0;
                    for (Var param : cfg.getMethod().getIR().getParams()) {
                        paramCount++;
                        if(param.equals(base)){
                            break;
                        }
                    }
                    ResultHandler.mappingFromMethodToClassName.put(cfg.getMethod(),new Pair<>(paramCount,new ArrayList<>()));
                }
            }

        });
        return null;
    }
}
