package conf.analysis;

import conf.Config;
import conf.fact.ConfFact;
import stringprop.fact.SPFact;
import pascal.taie.analysis.dataflow.analysis.AbstractDataflowAnalysis;
import pascal.taie.analysis.dataflow.analysis.AnalysisDriver;
import pascal.taie.analysis.dataflow.analysis.DataflowAnalysis;
import pascal.taie.analysis.dataflow.fact.NodeResult;
import pascal.taie.analysis.graph.cfg.CFG;
import pascal.taie.analysis.graph.icfg.ICFG;
import pascal.taie.config.AnalysisConfig;
import pascal.taie.ir.IR;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JMethod;
import stringprop.analysis.StringPropagation;
import utils.ResultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConfigReadSiteExtractor extends AnalysisDriver<Stmt, ConfFact> {

    // declare field ID
    public static final String ID = "crsextractor";

    public static List<Config<String, ?>> configs;

    public static List<String> configNames;

    //for handle inter-procedural results
    public static ICFG<JMethod,Stmt> icfg;

    public ConfigReadSiteExtractor(AnalysisConfig config) {
        super(config);
    }

    @Override
    protected DataflowAnalysis<Stmt, ConfFact> makeAnalysis(CFG<Stmt> cfg) {
        //TODO
//        return new Analysis(cfg);
        return new Analysis(cfg, this.getOptions().getBoolean("handle-inter"));
//        return null;
    }


    public static class Analysis extends AbstractDataflowAnalysis<Stmt, ConfFact> {

        NodeResult<Stmt, SPFact> stringConstantPropagationResult;
        private final boolean handleInter;



        public Analysis(CFG<Stmt> cfg, boolean handleInter) {
            super(cfg);
            this.handleInter = handleInter;

            /*
             * read all configs from config file
             */
            configs = ResultHandler.configs;
            configNames = configs.stream().map(Config::getConfigName).sorted().toList();

            /*
             * string constant result
             */
            stringConstantPropagationResult = cfg.getIR().getResult(StringPropagation.ID);
        }

        @Override
        public boolean isForward() {
            return true;
        }

        @Override
        public ConfFact newBoundaryFact() {
            return newBoundaryFact(this.cfg.getIR());
        }

        public ConfFact newBoundaryFact(IR ir) {

            if(stringConstantPropagationResult == null) return new ConfFact();

            ConfFact entryFact = getEntryFact(ir);

            return entryFact;
        }

        private ConfFact getEntryFact(IR ir) {
            //TODO Caused by: java.lang.UnsupportedOperationException: InvokeDynamic.getMethodRef() is unavailable
            /*
             * exclude classes other than hadoop
             */
//            if(!cfg.getMethod().getDeclaringClass().toString().contains("hadoop")){
//                /**
//                 * 此处会引发NullPointerException异常
//                 */
//                return newInitialFact();
////                return null;
//            }

            ConfFact entryFact = newInitialFact();

//            ir.getParams().stream().filter(Exps::holdsInt).forEach((p) -> {
//                entryFact.update(p, Value.getNAC());
//            });
//            return null;

//            String className = this.cfg.getMethod().getDeclaringClass().toString();
//            if(!className.contains("hadoop")){
////                System.err.println(className);
//                return entryFact;
//            }

            /*///////////////////////////////////////          test code          ////////////////////////////////////////*/
//            if(!className.contains("NameNode")){
//                return entryFact;
//            }

//            if(!className.contains("TestCase1")){
//                return entryFact;
//            }

//            if(!cfg.getMethod().getName().contains("getHttpAddress")){
//                return entryFact;
//            }
//            if(!cfg.getMethod().getDeclaringClass().toString().contains("JournalNode") || !cfg.getMethod().getName().toString().contains("getHttpAddress")){
//                return entryFact;
//            }

//            if(!cfg.getMethod().getDeclaringClass().toString().contains("DefaultSpeculator")|| !cfg.getMethod().toString().contains("<init>")){
//                return entryFact;
//            }

//            if(!cfg.getMethod().getDeclaringClass().toString().contains("S3AFileSystem")){
//                return entryFact;
//            }

//            if(!cfg.getMethod().getDeclaringClass().toString().contains("S3AUtils")|| !cfg.getMethod().toString().contains("intOption")){
//                return entryFact;
//            }

//            if(!cfg.getMethod().getName().contains("getSocketFactory") || !cfg.getMethod().getDeclaringClass().toString().contains("NetUtils")){
////                System.out.println("getSocketFactory");
//                return entryFact;
//            }


//            if(!cfg.getMethod().getName().contains("getProfileTaskRange") || !cfg.getMethod().getDeclaringClass().toString().contains("JobConf")){
////                System.out.println("getSocketFactory");
//                return entryFact;
//            }

            /*///////////////////////////////          identify main methods          /////////////////////////////////*/

            if(cfg.getMethod().toString().contains("main(java.lang.String[])") && !cfg.getMethod().toString().contains("examples")){
//                System.err.println(cfg.getMethod());
                ResultHandler.mainMethods.add(cfg.getMethod());
            }


            this.cfg.forEach(stmt -> {

                /*
                 * if stmt is invoke, and the arg of invoke is config name, then it is a config read site
                 * x = getXXX( ..., config)
                 */
                if(stmt instanceof Invoke invoke){


                    /*
                     * if invoke is get-method and is not invoke.dynamic (invokeDynamic.getMethodRef() is unavailable)
                     */
                    if(!invoke.isDynamic() && invoke.getMethodRef().toString().contains("get")){
//                        Recorder.getMethodCount++;
//                        System.out.println(invoke);

                        for (Var arg : invoke.getInvokeExp().getArgs()) {
                            //TODO
//                            MapFact<Var, StringValue> result = dataflowResult.getResult(invoke);

                            SPFact result = stringConstantPropagationResult.getResult(invoke);
                            Set<String> lookForSet = result.get(arg);
//                            StringValue lookFor = result.get(arg);
                            if(lookForSet == null){
                                //TODO
                                continue;
                            }


                            /*
                             * handle inter
                             */
//                            if(handleInter){
//                                if(lookFor.isNAC()){
//                                    icfg = World.get().getResult(ICFGBuilder.ID);
//
//                                    int paramCount = 0;
//                                    for (Var param : cfg.getMethod().getIR().getParams()) {
//                                        paramCount++;
//                                        if(param.equals(arg)){
//                                            break;
//                                        }
//                                    }
//                                    for (Stmt caller : icfg.getCallersOf(cfg.getMethod())) {
//
//                                        int argCount = 0;
//                                        for (Var argument : ((Invoke) caller).getInvokeExp().getArgs()) {
//                                            argCount++;
//                                            if(argCount== paramCount){
//                                                if(argument.isConst()){
//                                                    lookFor = StringValue.makeConstant(((ClassLiteral) argument.getConstValue()).getTypeValue().toString());
//                                                    break;
//                                                }else {
//                                                    lookFor = dataflowResult.getInFact(caller).get(argument);
//                                                    if(lookFor == null){
//                                                        continue;
//                                                    }
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }



                            lookForSet.forEach(suspect->{
                                suspect = suspect.replace("\"","");

//                                if(configs.contains(suspect)) {
                                if(configNames.contains(suspect)){

                                    Config<String, ?> suspectConfig = configs.get(configNames.indexOf(suspect));

                                    List<Var> valueInfoList = entryFact.get(suspect);
                                    /*
                                     * create list if it is the first time to add
                                     */
                                    if(valueInfoList == null){
                                        valueInfoList = new ArrayList<>();
                                    }
                                    /*
                                     * x is the config variable
                                     */
//                                    valueInfoList.add(new ValueInfo(ir.getMethod().getDeclaringClass(),this.cfg.getMethod(),invoke.getLValue()));
                                    valueInfoList.add(invoke.getLValue());
                                    boolean update = entryFact.update(suspect, valueInfoList);
//                                    ResultHandler.confFactList.add(entryFact);
                                    if(update){
//                                    System.err.print("config:"+suspect);
//                                    System.out.println(" ["+ invoke +"]");
//                                    System.err.println(suspect);
                                        ResultHandler.configExtractionList.add(suspect);

                                        ResultHandler.mappingFromMethodToConfig.put(suspect,invoke.getMethodRef().toString());
                                        /*
                                         * extract CRS
                                         */
                                        if(ResultHandler.CRSResult.containsKey(suspectConfig)){
                                            ResultHandler.CRSResult.get(suspectConfig).add(invoke);
                                        }else {
                                            List<Stmt> invokes = new ArrayList<>();
                                            invokes.add(invoke);
                                            ResultHandler.CRSResult.put(suspectConfig,invokes);
                                        }


                                    }

                                }
                            });


                        }
                    }

                }

            });
//            entryFact.forEach(Recorder.confFact::update);
            return entryFact;
        }

        @Override
        public ConfFact newInitialFact() {
            return new ConfFact();
        }

        @Override
        public void meetInto(ConfFact fact, ConfFact target) {
            //TODO not available
        }

        @Override
        public boolean transferNode(Stmt stmt, ConfFact in, ConfFact out) {
            //TODO not available
            return false;
        }
    }
}