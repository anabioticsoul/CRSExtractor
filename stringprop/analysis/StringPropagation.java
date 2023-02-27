package stringprop.analysis;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.ir.proginfo.FieldResolutionFailedException;
import stringprop.fact.SPFact;
import pascal.taie.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import pascal.taie.analysis.dataflow.analysis.AnalysisDriver;
import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.icfg.ICFG;
import pascal.taie.analysis.pta.PointerAnalysisResult;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.*;
import pascal.taie.ir.stmt.DefinitionStmt;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import utils.ResultHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringPropagation extends AnalysisDriver<Stmt, SPFact> {

    // declare field ID
    public static final String ID = "stringprop";

    private static final Logger logger = LogManager.getLogger(StringPropagation.class);

    //for handle reflection
    public static PointerAnalysisResult pta;
    public static ICFG<JMethod,Stmt> icfg;

    public StringPropagation(AnalysisConfig config) {
        super(config);
    }

    @Override
    protected DataflowAnalysis<Stmt, SPFact> makeAnalysis(CFG<Stmt> cfg) {
        return new Analysis(cfg, this.getOptions().getBoolean("handle-reflection"));
    }

    public static class Analysis extends AbstractDataflowAnalysis<Stmt, SPFact> {

        private final boolean handleReflection;


        protected Analysis(CFG<Stmt> cfg, boolean handleReflection) {
            super(cfg);
            this.handleReflection = handleReflection;
        }

        @Override
        public boolean isForward() {
            return true;
        }

        @Override
        public SPFact newBoundaryFact() {
            return newBoundaryFact(this.cfg.getIR());
        }

        public SPFact newBoundaryFact(IR ir) {
            SPFact entryFact = this.newInitialFact();
            ir.getParams().stream().filter(Exps::holdsReference).forEach((p) -> {
                // TODO: defines the parameter as null
                entryFact.update(p, new HashSet<>());
            });
            return entryFact;
        }

        @Override
        public SPFact newInitialFact() {
            return new SPFact();
        }

        @Override
        public void meetInto(SPFact fact, SPFact target) {
            /*
             * Union the two facts
             */
            fact.forEach((var, value) ->
                    target.update(var, unionValue(value, target.get(var))));
        }

        public Set<String> unionValue(Set<String> v1,Set<String> v2) {

            /*
             * UNDEF need to be stored, so null is stored as undef
             */
            if(v1 == null && v2 == null){
                System.err.println("meetValue: v1 and v2 are null!");
//                System.exit(1);
                return new HashSet<>();
            }

            if(v1 == null){
                return v2;
            }else if(v2 == null) {
                return v1;
            }
            Set<String> meetValueSet = new HashSet<>();

            meetValueSet.addAll(v1);
            meetValueSet.addAll(v2);

            return meetValueSet;
        }


        @Override
        public boolean transferNode(Stmt stmt, SPFact in, SPFact out) {
            // FIXME: Exception in thread "main" java.lang.IllegalStateException: 'com.google.protobuf.MessageOrBuilder' already has phantom field 'alwaysUseFieldBuilders'
            // FIXME: Exception in thread "main" java.lang.IllegalStateException: java.lang.IllegalStateException: 'com.google.protobuf.LazyStringArrayList' already has phantom field 'EMPTY'
            if(cfg.getMethod().getDeclaringClass().toString().contains("google") || cfg.getMethod().toString().contains("google")){
                return false;
            }

            /*
             * exclude classes except hadoop
             */
//            if(!cfg.getMethod().getDeclaringClass().toString().contains("hadoop")){
//                return false;
//            }

            if (stmt instanceof DefinitionStmt) {

                Exp lvalue = ((DefinitionStmt<?, ?>) stmt).getLValue();
                if (lvalue instanceof Var lhs) {
                    Exp rhs = ((DefinitionStmt<?, ?>) stmt).getRValue();
                    boolean changed = false;
                    for (Var inVar : in.keySet()) {
                        if (!inVar.equals(lhs)) {
                            changed |= out.update(inVar, in.get(inVar));
                        }
                    }
                    return Exps.holdsReference(lhs) ?
                            out.update(lhs, evaluate(rhs, in)) || changed :
                            changed;

//                    if(lhs.getType().getName().equals("java.lang.String")){
//                        return out.update(lhs, evaluate(rhs, in)) || changed;
//                    }else {
//                        return changed;
//                    }

                }
            }
            return out.copyFrom(in);
        }

        public Set<String> evaluate(Exp exp, SPFact in) {
            HashSet<String> singleValueSet = new HashSet<>();
            /*
             * if exp is a string literal, return the value
             */
            if(exp instanceof StringLiteral stringLiteral){
                String replace = stringLiteral.toString().replace("\"","");
                singleValueSet.add(replace);
                return singleValueSet;
            }
            /*
             * if exp is a StringBuilder, return "", in order to operate on the string later
             */
            if(exp instanceof NewInstance newInstance){
                if(newInstance.getType().getName().contains("StringBuilder")){
                    singleValueSet.add("");
//                    return StringValue.makeConstant("");
                    return singleValueSet;
                }
            }
            /*
             * if exp is a variable, return the value of the variable
             */
            if (exp instanceof Var var) {
                Set<String> inSet = in.get(var);
                if(inSet == null){
                    return new HashSet<>();
                }else {
                    return inSet;
                }
            }
            /*
             * if exp is a static field, return the value of the static field
             */
            if(exp instanceof StaticFieldAccess staticFieldAccess){
//                String field = staticFieldAccess.getFieldRef().toString();
                //TODO solve the resolution problem
                try {
                    JField resolve = staticFieldAccess.getFieldRef().resolve();
                    /*
                     * get string value from static field identifier
                     */
                    String stringValue = ResultHandler.staticFields.get(resolve);
//                System.err.println("static field:" + field);
//                return StringValue.makeConstant(field);
                    if(stringValue == null){
                        return singleValueSet;
                    }
                    singleValueSet.add(stringValue);
                    return singleValueSet;
                }catch (FieldResolutionFailedException e){
                    logger.info("FieldResolutionFailedException: Cannot resolve" + staticFieldAccess.getFieldRef().toString());
                }

            }

            if (exp instanceof InvokeExp invokeExp) {
                String methodName = invokeExp.getMethodRef().toString();
                /*
                 * handle String.concat & String.append
                 */
                if (methodName.contains("concat") || methodName.contains("append")){
//                    InvokeExp invokeExp = invoke.getInvokeExp();
                    if(invokeExp instanceof InvokeInstanceExp invokeInstanceExp){
                        Var base = invokeInstanceExp.getBase();
                        /*
                         * handle the case that the method is called without parameters
                         */
                        if(invokeExp.getArgCount() <= 0){
                            return in.get(base);
                        }
                        Var arg = invokeExp.getArg(0);
//                        String str = base.getConstValue().toString() + arg.getConstValue().toString();
                        /*
                         * remove ""
                         */
                        Set<String> strSet1 = in.get(base);
                        Set<String> strSet2 = in.get(arg);
//                        StringValue str1 = in.get(base);
//                        StringValue str2 = in.get(arg);
                        if(strSet1==null || strSet1.size() == 0){
                            return strSet2;
                        }
                        if(strSet2==null || strSet2.size() == 0){
                            return strSet1;
                        }
                        //FIXME
//                        for (String str1 : strSet1) {
//                            for (String str2 : strSet2) {
//                                singleValueSet.add(str1 + str2);
//                            }
//                        }
                        //TODO: need to traverse all kinds of cases
                        singleValueSet.add(strSet1.iterator().next()+ strSet2.iterator().next());

                        return singleValueSet;
                    }

                }
                /*
                 * handle String.format
                 */
                if(methodName.contains("format(")){
                    String res = "";
                    List<Var> args = invokeExp.getArgs();
                    /*
                     * handle %s
                     */
                    if(args == null || args.size() == 0){
                        return new HashSet<>();
                    }
                    try {
                        Var var = args.get(0);
                        Set<String> stringValueSet = new HashSet<>();
                    if(var.isConst()){
                        /*
                         * stringValueSet = {"%s %s %s"}
                         */
                        Literal constValue = var.getConstValue();
                        stringValueSet.add(constValue.toString());
                    }else {
                        stringValueSet = in.get(var);
                        if(stringValueSet == null){
                            stringValueSet = new HashSet<>();
                        }
                    }
                    if(stringValueSet.size() == 0){
                        return stringValueSet;
                    }

                        /*
                         * format "xx%syy%szz%s"
                         * split = {"xx","yy","zz"}
                         */
                    String format = stringValueSet.iterator().next();
                    String[] split = format.split("%s");

                    if(split.length == 0){
                        return stringValueSet;
                    }
                    String stringValue = null;
                        /*
                         * arg1,arg2,arg3
                         * res = "xxarg1yyarg2zzarg3"
                         */
                    for (int i = 1; i < args.size() && i - 1 < split.length; i++) {
                        Var str = args.get(i);
                        if(str.isConst()){
                            Literal constValue = str.getConstValue();
                            stringValue =  constValue.toString();
                        }else {
                            //FIXME: need to traverse all kinds of cases
                            if(in.get(str) != null){
                                stringValue = "";
                                if(in.get(str).iterator().hasNext()){
                                    stringValue = in.get(str).iterator().next();
                                }
                            }else {
                                stringValue = "";
                            }

                        }
                        res += split[i-1];
                        if(stringValue != null){
                            res += stringValue;
                        }
//                        res += stringValue.toString();
                    }

                    HashSet<String> resultSet = new HashSet<>();
                    resultSet.add(res);
                        return resultSet;

                    }catch (NullPointerException | ArrayIndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                }


                /*
                 * handle String.toString
                 */
                if(methodName.contains("toString")){
                    if(invokeExp instanceof InvokeInstanceExp invokeInstanceExp){
                        Var base = invokeInstanceExp.getBase();
                        return in.get(base);
                    }
                }


                /*
                 * handle reflection by intra
                 */
                if(handleReflection){
//                    StringValue suspectValue = null;

                    if(methodName.contains("getSimpleName")){

                        Set<String> suspectValueSet = new HashSet<>();
                        //TODO meetValue

                        for (String clazzName : ResultHandler.mappingFromMethodToClassName.get(cfg.getMethod()).getO2()) {
                            suspectValueSet.add(clazzName);
                        }
//                        Recorder.mappingFromMethodToClassName.put(cfg.getMethod(),new Pair<>(paramCount,null));

                        return suspectValueSet;

                    }
                }

            }
            return new HashSet<>();
        }
    }
}
