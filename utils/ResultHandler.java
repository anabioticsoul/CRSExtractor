package utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import conf.Config;
import conf.fact.ConfFact;
import constraint.config.Constraint;
import jas.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.World;
import pascal.taie.analysis.pta.PointerAnalysis;
import pascal.taie.analysis.pta.PointerAnalysisResult;
import pascal.taie.analysis.pta.core.heap.MockObj;
import pascal.taie.analysis.pta.core.heap.Obj;
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import taint.config.TaintManager;
import taint.fact.Source;
import taint.fact.TaintFlow;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ResultHandler {

    private static final Logger logger = LogManager.getLogger(ResultHandler.class);

    /*///////////////////////////////////////          CRSExtractor          ////////////////////////////////////////*/


    /**
     * all configs
     */
//    public static List<String> configs;
    public static List<Config<String, ?>> configs;

    /**
     * static fields in classes for string constant propagation
     */
    public static Map<JField, String> staticFields;

    /**
     * local variables in classes
     */
    public static Map<Var, String> localVars;


    /**
     * The map from a config to the list of caller methods
     */
    public static Map<String, String> mappingFromMethodToConfig;


    /**
     * The map from a config to the list of CRS statements
     */
    public static Map<Config<?, ?>, List<Stmt>> CRSResult;


    /**
     * handle reflection
     */
    public static Map<JMethod, Pair<Integer, List<String>>> mappingFromMethodToClassName;


    /*///////////////////////////////////////          ConfigACE          ////////////////////////////////////////*/
    public static PointerAnalysisResult pta;

    /**
     * all main methods in ICFG
     */
    public static Set<JMethod> mainMethods;

    /**
     * mapping from configs to sources
     */
    public static Map<Config<?, ?>, Set<Source>> configToSources;

    /**
     * taint propagation results
     */
    public static Set<TaintFlow> taintFlows;

    /**
     * mapping from configs to taint flows
     */
    public static Map<Config<?, ?>, Set<TaintFlow>> configToTaintFlows;

    /*
     * taint manager
     */
    public static TaintManager taintManager;

    /**
     * taint vars
     */
    public static Set<Var> taintVars;

    /**
     * has constraint
     */
    public static Set<Var> constraintVars;

    /*
     * config constraints
     */
    public static Map<Config<?, ?>, Set<Constraint>> configConstraints;


    /*///////////////////////////////////////          test code          ////////////////////////////////////////*/

    /**
     * count the number of get methods
     */
    public static Integer getMethodCount;

    public static Integer countDetection;

    /**
     * config fact
     */
//    public static ConfFact confFact;
    public static List<ConfFact> confFactList;

    public static List<String> configExtractionList;

    /*///////////////////////////////////////          test code          ////////////////////////////////////////*/


//    static{
//        init();
//    }

    public static void init() {


        /*
         * initialization
         */

        configs = ConfigLoader.getConfigs();
//        configs = ConfigLoader.getAllConfigs();

        staticFields = new ConcurrentHashMap<>();

        localVars = new ConcurrentHashMap<>();


        mappingFromMethodToConfig = new ConcurrentHashMap<>();

        configExtractionList = new LinkedList<>();

        mappingFromMethodToClassName = new ConcurrentHashMap<>();

        //        confFact = new ConfFact();
        confFactList = new ArrayList<>();

        CRSResult = new ConcurrentHashMap<>();

        mainMethods = new HashSet<>();

        configToSources = new ConcurrentHashMap<>();

        taintFlows = new HashSet<>();

        configToTaintFlows = new ConcurrentHashMap<>();

        configConstraints = new ConcurrentHashMap<>();

        taintVars = new HashSet<>();

        constraintVars = new HashSet<>();

        getMethodCount = 0;

        countDetection = 0;


    }


    /*///////////////////////////////////////          CRSExtractor          ////////////////////////////////////////*/

    /**
     * dump all extracted configs & CRSs
     */
    public static void dumpExtractedConfigs() {

        System.err.println("=======================================================================================");

//        logger.info("[INFO] Number of CRSs:" + ResultHandler.configExtractionList.size());

        System.err.println("[INFO] Number of CRSs:" + ResultHandler.configExtractionList.size());

//        ResultHandler.configExtractionList.stream().sorted().forEach(System.err::println);

        /*
         * remove duplicate configs
         */
//        TreeSet<String> configSet = new TreeSet<>(ResultHandler.configExtractionList);

//        System.out.println("Number of configs:" + configSet.size());

//        configSet.stream().sorted().forEach(System.err::println);

        /*
         * dump all extracted configs & CRSs
         */
        CRSResult.forEach((config, stmts) -> {
            System.err.println("Config: ");
            System.err.println(config);
            System.err.println("CRS: ");
            stmts.forEach(System.err::println);
        });

        System.err.println("=======================================================================================");
    }

    /*////////////////////////////////////////          ConfigACE            ////////////////////////////////////////*/


    /*///////////////////////////////////////          taint utils           ////////////////////////////////////////*/


    public static void dumpPointerAnalysisInfo() {

        System.err.println("=======================================================================================");

        System.err.println("Pointer Analysis Result:");

        PointerAnalysisResult pointerAnalysisResult = World.get().getResult(PointerAnalysis.ID);

        int callGraphNodes = pointerAnalysisResult.getCallGraph().getNumberOfNodes();

        System.err.println("Number of call graph nodes: " + callGraphNodes);

        System.err.println("=======================================================================================");

    }

    public static void collectConfigTaints(Set<TaintFlow> taintFlows) {


        System.err.printf("Detected %d taint flow(s) in total%n", taintFlows.size());

        configToSources.forEach((config, sources) -> {
            sources.forEach(source -> {
                taintFlows.stream()
                        .filter(taintFlow -> taintFlow.sourceCall().getMethodRef().resolve().equals(source.method()))
                        .forEach(taintFlow -> {
                            boolean isCRS = false;
                            for (Stmt stmt : CRSResult.get(config)) {
                                if (((Invoke) stmt).getContainer().equals(taintFlow.sourceCall().getContainer())) {
                                    isCRS = true;
                                    break;
                                }
                            }
                            if (isCRS) {
                                HashSet<TaintFlow> taintFlowSet = null;
                                if (configToTaintFlows.containsKey(config)) {
                                    taintFlowSet = new HashSet<>(configToTaintFlows.get(config));
                                } else {
                                    taintFlowSet = new HashSet<>();
                                }
                                taintFlowSet.add(taintFlow);
                                configToTaintFlows.put(config, taintFlowSet);
                            }
                        });
            });
        });
    }

    public static void dumpTaintFlows(String file) {

        System.err.println("=======================================================================================");
        PrintWriter output = null;
//        try {
//            output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file)));
//        } catch (FileNotFoundException e) {
//            System.err.println("Cannot open file: " + file);
//        }
//        if (output == null) {
            output = new PrintWriter(System.err);
//        }

        HashSet<Invoke> CRSs = new HashSet<>();

        int countConfig = 0;
        int countTaintFlow = 0;
        int countCRS = 0;
        for (Map.Entry<Config<?, ?>, Set<TaintFlow>> configSetEntry : configToTaintFlows.entrySet()) {
            Config<?, ?> config = configSetEntry.getKey();
            Set<TaintFlow> taintFlows = configSetEntry.getValue();
            if(taintFlows.size() > 0){
                countConfig++;
                int index = 1;
                output.printf("Config: \n%s%n", config);
                output.printf("Detected %s Taint flows: %n", taintFlows.size());
                for (TaintFlow taintFlow : taintFlows) {
                    countTaintFlow++;
                    Invoke invoke = taintFlow.sourceCall();
                    CRSs.add(invoke);
                    output.printf("[%s] %s%n", index++, taintFlow);
                }
                output.flush();
            }
        }

        countCRS = CRSs.size();

        System.err.printf("Detected %d config(s) with taint flow(s)%n", countConfig);
        System.err.printf("Detected %d CRS(s) with taint flow(s)%n", countCRS);
        System.err.printf("Detected %d taint flow(s)%n", countTaintFlow);
        System.err.println("=======================================================================================");
        output.close();
    }

    public static void dumpAccessControlConfigs() {

        if (pta == null) {
            System.err.println("[WARN] Pointer Analysis has not been performed.");
            return;
        }
        taintFlows = pta.getResult("taint.analysis.TaintPropagation");
        System.err.printf("Detected %d taint flow(s):%n", taintFlows.size());

        configToSources.forEach((config, sources) -> {

            sources.forEach(source -> {
                if (taintFlows.stream()
                        .filter(taintFlow -> taintFlow.sourceCall().getMethodRef().resolve().equals(source.method())).toList().size() > 0) {
                    System.err.printf("Config: %s%n", config);
                    System.err.printf("Source: %s%n", source);
                }
            });
        });
    }

    /*/////////////////////////////////////         constraint utils          ////////////////////////////////////////*/

    public static Config<?, ?> findConfigBySource(Invoke invoke) {
        for (Map.Entry<Config<?, ?>, Set<Source>> entry : configToSources.entrySet()) {
            Config<?, ?> config = entry.getKey();
            Set<Source> sources = entry.getValue();
            for (Source source : sources) {
                if (source.method().equals(invoke.getMethodRef().resolve())) {
                    return config;
                }
            }
        }
        return null;
    }

    public static Set<Var> calculateTaintPointerVars(){
        if (pta == null) {
            System.err.println("[WARN] Pointer Analysis has not been performed.");
            return new HashSet<>();
        }
        pta.getVars().forEach(var -> {
            Set<Obj> pointsToSet = pta.getPointsToSet(var);
            pointsToSet.forEach(obj -> {
                System.out.print("");
                if(obj instanceof MockObj mockObj){
                    String description = mockObj.getDescriptor().string();
                    if(description.contains("TaintObj") && var.getMethod().toString().contains("hadoop")){
                        taintVars.add(var);
                    }
                }});
        });

        taintVars.forEach(var -> {
            System.out.println(var.getMethod().toString() +": "+ var);
        });

        System.err.printf("Collected %s taint var(s)%n", taintVars.size());
        return taintVars;
    }
    public static void dumpConfigConstraints() {
        configConstraints.forEach((config, constraints) -> {
            System.err.printf("Config: %s%n", config);
            System.err.printf("Collected %s constraint(s)%n", constraints.size());
            constraints.forEach(constraint -> {
                System.err.printf("Constraint: %s%n", constraint.toString());
            });
        });
    }





}
