package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class Arguments {

    private static final int CLASS_PATH_LENTH = 1;
//    private static final int MAIN_METHOD_LENTH = 1;
    private static final int ALLOW_PHANTOM_LENTH = 1;
    private static final int SCOPE_LENTH = 1;

    private static final int INPUT_CLASS_LENTH = 1;

    private static final int CLASS_DUMPER_LENTH = 1;

    /**
     * real path
     */
    private final static String sourceDirectory = System.getProperty("user.dir");


    /**
     * test path
     */
//    private final static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes";

    public static String [] get(){


        String [] arguments = new String[CLASS_PATH_LENTH + getAnalysis().length + ALLOW_PHANTOM_LENTH + SCOPE_LENTH + INPUT_CLASS_LENTH];
//        String [] arguments = new String[CLASS_PATH_LENTH + MAIN_METHOD_LENTH + getAnalysis().length + ALLOW_PHANTOM_LENTH + SCOPE_LENTH + INPUT_CLASS_LENTH];


        arguments[0] = getClassPath();

        /*
         * without main classes specified
         */
//        arguments[1] = getMainMethod();


//        for (int i = CLASS_PATH_LENTH + MAIN_METHOD_LENTH; i < getAnalysis().length + CLASS_PATH_LENTH + MAIN_METHOD_LENTH; i++) {
        for (int i = CLASS_PATH_LENTH; i < getAnalysis().length + CLASS_PATH_LENTH; i++) {
//            arguments[i] = getAnalysis()[i - CLASS_PATH_LENTH - MAIN_METHOD_LENTH];
            arguments[i] = getAnalysis()[i - CLASS_PATH_LENTH];
        }
        arguments[arguments.length - ALLOW_PHANTOM_LENTH - SCOPE_LENTH - INPUT_CLASS_LENTH] = getAllowPhantom();

        arguments[arguments.length - SCOPE_LENTH - INPUT_CLASS_LENTH] = getScope();

        arguments[arguments.length - INPUT_CLASS_LENTH] = getInputClass();

//        arguments[arguments.length - CLASS_DUMPER_LENTH] = getClassDumper();

        /*///////////////////////////////////////          test code          ////////////////////////////////////////*/
//        System.out.println("arguments:");
//        for (String argument : arguments) {
//            System.err.println(argument);
//        }
        /*///////////////////////////////////////          test code          ////////////////////////////////////////*/

        return arguments;

    }


    /**
     * core & environment
     */
    public static String getClassPath(){

        List<String> jarList = new ArrayList<>();

        final StringBuffer classPath = new StringBuffer();
        /*
         * --classpath
         */
        classPath.append("-cp=");

        try {
            /* **************************               XMLs of hadoop 2.7.1                  ****************************/
            jarList.addAll(getHadoopJars(sourceDirectory + File.separator + "workspace" + File.separator + "hadoop-2.7.1" + File.separator + "hadoop"));
//
//            jarList.addAll(getHbaseJars(sourceDirectory + File.separator + "workspace" + File.separator + "hbase-1.2.7" + File.separator + "lib"));


            /* **************************               XMLs of hadoop 2.10.2                 ****************************/
//            jarList.addAll(getHadoopJars(sourceDirectory + File.separator + "workspace" + File.separator + "hadoop-2.10.2" + File.separator + "hadoop"));

//            jarList.addAll(getHbaseJars(sourceDirectory + File.separator + "workspace" + File.separator + "hbase-2.4.13" + File.separator + "lib"));



        } catch (IOException e) {
            throw new RuntimeException("[ERROR] cannot get the class path!");
        }

        /*
         * add jar files to class path
         */
        for (int i = 0; i < jarList.size(); i++) {
            classPath.append(jarList.get(i));
            if(i != jarList.size() - 1){
                classPath.append(File.pathSeparator);
            }
        }

        if(jarList.size() == 0){
            System.err.println("[ERROR] cannot get the class path!");
            System.exit(-1);
        }
        return classPath.toString();
    }

    public static List<String> getHadoopJars(String dir) throws IOException {

        List<String> allJars = new LinkedList<>();
        File sdir = new File(dir);
        if(!sdir.isDirectory()){
            System.err.println(dir + "is not a directory!");
        }

        /*
         * filter out directories
         */
//        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
//            paths.filter(Files::isDirectory)
//                    .forEach(System.out::println);
//        }

        /*
         * filter with suffix
         */
        try (Stream<Path> paths = Files.walk(Paths.get(dir), 10)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".jar"))
                    /*
                     * filter *-test.jar
                     */
                    .filter(not(f->f.contains("test")))
//                    .forEach(System.out::println);
                    .forEach(allJars::add);

        }

        /*
         * filter jars only with hadoop
         */
        List<String> jars = new LinkedList<>();
        allJars.forEach(jar->{
            //            if(new File(jar).getName().contains("hadoop")){
            /*
             * filter *-sources.jar
             */
            if(new File(jar).getName().contains("hadoop") && !new File(jar).getName().contains("sources")){

                jars.add(jar);
            }
        });

        System.err.println("input jars path: " + dir);


//        return allJars;
        /* //////////////////////////////////// test Hadoop Common  /////////////////////////////////////// */
//        ArrayList<String> testJar = new ArrayList<>();
//        Iterator<String> iterator = jars.iterator();
//        int i = 0;
//        while (iterator.hasNext() && i < 10){
//            i++;
//            testJar.add(iterator.next());
//        }
//        return testJar;
        /* //////////////////////////////////// test Hadoop Common  /////////////////////////////////////// */

        return jars;
    }

    public static List<String> getHbaseJars(String dir) throws IOException {

        List<String> allJars = new LinkedList<>();
        File sdir = new File(dir);
        if(!sdir.isDirectory()){
            System.err.println(dir + "is not a directory!");
        }

        /*
         * filter out directories
         */
//        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
//            paths.filter(Files::isDirectory)
//                    .forEach(System.out::println);
//        }

        /*
         * filter with suffix
         */
        try (Stream<Path> paths = Files.walk(Paths.get(dir), 10)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".jar"))
                    /*
                     * filter *-test.jar
                     */
                    .filter(not(f->f.contains("test")))
//                    .forEach(System.out::println);
                    .forEach(allJars::add);

        }

        /*
         * filter jars only with hbase
         */
        List<String> jars = new LinkedList<>();
        allJars.forEach(jar->{
            //            if(new File(jar).getName().contains("hbase")){
            /*
             * filter *-sources.jar
             */
            if(new File(jar).getName().contains("hbase") && !new File(jar).getName().contains("sources")){

                jars.add(jar);
            }
        });

//        return allJars;
        return jars;
    }

    public static List<String> getCassandraJars(String dir) throws IOException {

        List<String> allJars = new LinkedList<>();
        File sdir = new File(dir);
        if(!sdir.isDirectory()){
            System.err.println(dir + "is not a directory!");
        }

        /*
         * filter out directories
         */
//        try (Stream<Path> paths = Files.walk(Paths.get(dir))) {
//            paths.filter(Files::isDirectory)
//                    .forEach(System.out::println);
//        }

        /*
         * filter with suffix
         */
        try (Stream<Path> paths = Files.walk(Paths.get(dir), 10)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".jar"))
                    /*
                     * filter *-test.jar
                     */
//                    .filter(not(f->f.contains("test")))
//                    .forEach(System.out::println);
                    .forEach(allJars::add);

        }

        /*
         * filter jars only with cassandra
         */
        List<String> jars = new LinkedList<>();
        allJars.forEach(jar->{
            if(new File(jar).getName().contains("cassandra")){

                jars.add(jar);
            }
        });

        return allJars;
//        return jars;
    }

    @Deprecated
    private static String getMainMethod(){
        StringBuilder mainMethod = new StringBuilder("-m=");

        /* **************************               XMLs of hadoop 2.7.1                  *************************** */
        File file = new File(sourceDirectory + File.separator + "input" + File.separator + "hadoop-2.7.1" + File.separator + "hadoopMainMethod.csv");


        /* **************************               XMLs of hadoop 2.10.2                 *************************** */
//        File file = new File(sourceDirectory + File.separator + "input" + File.separator + "hadoop-2.10.2" + File.separator + "hadoopMainMethod.csv");


        /* **************************               XMLs of hbase 1.2.7                   *************************** */
//        File file = new File(sourceDirectory + File.separator + "input" + File.separator + "hbase-1.2.7" + File.separator + "hbaseMainMethod.csv");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            System.err.println("input main method: " + file.getAbsoluteFile());
            String str;

            while ((str = bufferedReader.readLine()) != null)

                mainMethod.append(str);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("hadoop main methods file not found!");
        } catch (IOException e) {
            throw new RuntimeException("hadoop main methods file read error!");
        }

        return mainMethod.toString();

    }

    /**
     * all analyses
     * @return analysis
     */
    private static String[] getAnalysis(){
        /*
         * CRSExtractor
         */
//        String []analyses = {
//                "-a=staticfieldidentifier",
////                "-a=icfg",
//                "-a=reflection-handler",
//                "-a=stringprop=handle-reflection:true",
////                "-a=configreadpointextraction=handle-inter:true",
////                "-a=stringprop",
//                "-a=configreadpointextraction"
//
//        };

        /*
         * ConfigACE
         */
        String []analyses = {
                "-a", "staticfieldidentifier",
                "-a", "reflection-handler",
                "-a", "stringprop=handle-reflection:true",
                "-a", "crsextractor",
                "-a", "pta=propagate-types:[int,long,float,null,reference];plugins:[taint.analysis.TaintPropagation];cs:1-call",
                "-a", "constraint",
        };


//        String[]analyses = {
//        /*
//         * dump class in .tir
//         */
//                "-a", "ir-dumper=dump-dir:./output",
//
//                /*
//                 * test method
//                 */
////                "-a=staticfieldrecorder",
////                "-a=stringprop",
////                "-a=mymethod"
////                "-a=myclass"
//        };
        return analyses;
    }

    /**
     * --allow-phantom
     * @return arg
     */
    private static String getAllowPhantom(){

        return "-ap";
    }

    private static String getScope(){

//        return "-scope=REACHABLE";
//        return "-scope=ALL";
        return "-scope=APP";
    }

    public static String getInputClass(){

        StringBuilder inputClass = new StringBuilder("--input-classes=");

        String[] inputFiles = {
                /* **************************               Classes of hadoop 2.7.1                 *************************** */
                sourceDirectory + File.separator + "input" + File.separator + "hadoop-2.7.1" + File.separator + "hadoopClazz.csv",
                /* **************************               Classes of hbase 1.2.7                 *************************** */
//                sourceDirectory + File.separator + "input" + File.separator + "hbase-1.2.7" + File.separator + "hbaseClazz.csv",


                /* **************************               Classes of hadoop 2.10.2               *************************** */
//                sourceDirectory + File.separator + "input" + File.separator + "hadoop-2.10.2" + File.separator + "hadoopClazz.csv",
                /* **************************               Classes of hadoop 2.4.13               *************************** */
//                sourceDirectory + File.separator + "input" + File.separator + "hbase-2.4.13" + File.separator + "hbaseClazz.csv",

        };



        for (String file : inputFiles) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                System.err.println("input class file: " + file);
                String str;

                while ((str = bufferedReader.readLine()) != null)

                    inputClass.append(str);

            } catch (FileNotFoundException e) {
                throw new RuntimeException("hadoop input class file not found!");
            } catch (IOException e) {
                throw new RuntimeException("hadoop input class file read error!");
            }
        }


        return inputClass.toString();
    }

}
