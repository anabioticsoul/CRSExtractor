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
import pascal.taie.ir.exp.Var;
import pascal.taie.ir.stmt.Invoke;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JField;
import pascal.taie.language.classes.JMethod;
import taint.config.TaintManager;
import taint.fact.Source;
import taint.fact.TaintFlow;

import java.io.PrintStream;
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
    public static Map<Config<?,?>, List<Stmt>> CRSResult;


    /**
     * handle reflection
     */
    public static Map<JMethod, Pair<Integer,List<String>>> mappingFromMethodToClassName;


    /*///////////////////////////////////////          ConfigACE          ////////////////////////////////////////*/

    /**
     * all main methods in ICFG
     */
    public static Set<JMethod> mainMethods;

    /**
     * mapping from configs to sources
     */
    public static Map<Config<?,?>, Set<Source>> configToSources;

    /**
     * taint propagation results
     */
    public static Set<TaintFlow> taintFlows;

    /*
     * taint manager
     */
    public static TaintManager taintManager;

    /*
     * config constraints
     */
    public static Map<Config<?,?>, Set<Constraint>> configConstraints;


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

    public static void init(){


        /*
         * initialization
         */

//        configs = ConfigLoader.getConfigs();
        configs = ConfigLoader.getAllConfigs();

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

        configConstraints = new ConcurrentHashMap<>();

        getMethodCount = 0;

        countDetection = 0;


    }


    /*///////////////////////////////////////          CRSExtractor          ////////////////////////////////////////*/
    /**
     * dump all extracted configs & CRSs
     */
    public static void dumpExtractedConfigs() {

        logger.info("=======================================================================================");

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



    }



    /*///////////////////////////////////////          taint utils          ////////////////////////////////////////*/



    public static void dumpTaintFlows() {
        PointerAnalysisResult pointerAnalysisResult = World.get().getResult(PointerAnalysis.ID);
        taintFlows = pointerAnalysisResult.getResult("taint.analysis.TaintPropagation");
        System.err.printf("Detected %d taint flow(s):%n", taintFlows.size());

        configToSources.forEach((config, sources) -> {
            System.err.printf("Config: %s%n", config);
            sources.forEach(source -> {
                System.err.printf("Source: %s%n", source);
                taintFlows.stream()
                        .filter(taintFlow -> taintFlow.sourceCall().getMethodRef().resolve().equals(source.method()))
                        .forEach(taintFlow -> System.out.printf("Taint flow: %s%n", taintFlow));
            });
        });
    }

    public static void dumpAccessControlConfigs() {
        PointerAnalysisResult pointerAnalysisResult = World.get().getResult(PointerAnalysis.ID);
        if(pointerAnalysisResult == null){
            System.err.println("[WARN] Pointer Analysis has not been performed.");
            return;
        }
        taintFlows = pointerAnalysisResult.getResult("taint.analysis.TaintPropagation");
        System.err.printf("Detected %d taint flow(s):%n", taintFlows.size());

        configToSources.forEach((config, sources) -> {

            sources.forEach(source -> {
                if(taintFlows.stream()
                        .filter(taintFlow -> taintFlow.sourceCall().getMethodRef().resolve().equals(source.method())).toList().size() > 0){
                    System.err.printf("Config: %s%n", config);
                    System.err.printf("Source: %s%n", source);
                }
            });
        });
    }

    public static Config<?,?> findConfigBySource(Invoke invoke){
        for (Map.Entry<Config<?, ?>, Set<Source>> entry : configToSources.entrySet()) {
            Config<?, ?> config = entry.getKey();
            Set<Source> sources = entry.getValue();
            for (Source source : sources) {
                if (source.method().equals(invoke.getMethodRef().resolve())){
                    return config;
                }
            }
        }
        return null;
    }

    public static void dumpConfigConstraints(){
        configConstraints.forEach((config, constraints) -> {
            System.err.printf("Config: %s%n", config);
            constraints.forEach(constraint -> {
                System.err.printf("Constraint: %s%n", constraint);
            });
        });
    }
    /*///////////////////////////////////////          taint utils          ////////////////////////////////////////*/

}
