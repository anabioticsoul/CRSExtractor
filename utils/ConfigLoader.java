package utils;

import conf.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class ConfigLoader {
    private static final HashSet<Config<String, ?>> allConfigs = new LinkedHashSet<>();

    private static final HashSet<Config<String, ?>> configs = new LinkedHashSet<>();


    private static boolean loadConfigs(){
//        for(int i=0;i<configFileNames.length;++i) {
//            File f = new File("./config_files/"+configFileNames[i]);
//        File f = new File("./src/main/java/conf/config_files/" + "hdfs-default.xml");
//        File f = new File("./src/main/resources/config_files/" + "hdfs-default.xml");




        /* **************************               XMLs of hadoop 2.7.1                  *************************** */

//        File f = new File("./src/main/resources/config_files/hadoop-2.7.1/" + "core-default.xml");
//        File f = new File("./src/main/resources/config_files/hadoop-2.7.1/" + "hdfs-default.xml");
//        File f = new File("./src/main/resources/config_files/hadoop-2.7.1/" + "mapred-default.xml");
//        File f = new File("./src/main/resources/config_files/hadoop-2.7.1/" + "yarn-default.xml");



        /* **************************               XMLs of hadoop 2.10.2                 *************************** */
//        File f = new File("./src/main/resources/config_files/hadoop-2.10.2/" + "core-default.xml");
//        File f = new File("./src/main/resources/config_files/hadoop-2.10.2/" + "hdfs-default.xml");
//        File f = new File("./src/main/resources/config_files/hadoop-2.10.2/" + "mapred-default.xml");
//        File f = new File("./src/main/resources/config_files/hadoop-2.10.2/" + "yarn-default.xml");

        /*
         *  test case of hadoop 2.10.2
         */
        /*///////////////////////////////////////          test code          ////////////////////////////////////////*/
        File f = new File("./src/main/resources/config_files/test/hadoop-2.10.2/" + "core-test.xml");
        /*///////////////////////////////////////          test code          ////////////////////////////////////////*/

        /* **************************               Classes of hbase 1.2.7                 ************************** */
//        File f = new File("./src/main/resources/config_files/hbase-1.2.7/" + "hbase-default.xml");

        /* **************************               Classes of hbase 2.4.13                 ************************* */
//        File f = new File("./src/main/resources/config_files/hbase-2.4.13/" + "hbase-default.xml");

        System.err.println("input config path:"+f.getAbsoluteFile());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = dBuilder.parse(f);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NodeList nList = doc.getElementsByTagName("property");


        for (int ii = 0; ii < nList.getLength(); ++ii) {
            Node node = nList.item(ii);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

//                String name = eElement.getElementsByTagName("name").item(0).getTextContent();
//                if (!configs.contains(name)) {
//                    configs.add(name);
//                }
                String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                Node valueNode = eElement.getElementsByTagName("value").item(0);
                String value = null;
                if(valueNode!=null){
                    value = eElement.getElementsByTagName("value").item(0).getTextContent();
                }
                Config<String, String> newConfig = new Config<>(name, value);
                configs.add(newConfig);
            }
        }
        return configs.size() != 0;
    }


    private static boolean loadAllConfigs() {


//        File f = new File("./src/main/java/conf/config_files/" + "hdfs-default.xml");

//        String[] configFileNames = {"core-default.xml","hdfs-default.xml"};

        String[] configFileNames = {"yarn-default.xml", "core-default.xml", "hdfs-default.xml", "mapred-default.xml",
//                 "hbase-default.xml", "hdfs-rbf-default.xml",
//                "zookeeper-default.xml", "alluxio-default.xml", "randoop-default.xml"
        };

        for (int i = 0; i < configFileNames.length; ++i) {
//            File f = new File("./src/main/java/conf/config_files/" + configFileNames[i]);
//            File f = new File("./src/main/resources/config_files/" + configFileNames[i]);

            /* ************************               XMLs of hadoop 2.7.1                  ************************* */
//            File f = new File("./src/main/resources/config_files/hadoop-2.7.1/" + configFileNames[i]);

            /* ************************               XMLs of hadoop 2.10.2                 ************************* */
            File f = new File("./src/main/resources/config_files/hadoop-2.10.2/" + configFileNames[i]);

            System.err.println("input config path:" + f.getAbsoluteFile());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = null;
            try {
                dBuilder = dbFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            Document doc = null;
            try {
                doc = dBuilder.parse(f);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            NodeList nList = doc.getElementsByTagName("property");


            for (int ii = 0; ii < nList.getLength(); ++ii) {
                Node node = nList.item(ii);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;
//                    String name = eElement.getElementsByTagName("name").item(0).getTextContent();
//                    if (!allConfigs.contains(name)) {
//                        allConfigs.add(name);
//                    }
                    String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    Node valueNode = eElement.getElementsByTagName("value").item(0);
                    String value = null;
                    if(valueNode!=null){
                        value = eElement.getElementsByTagName("value").item(0).getTextContent();
                    }
                    Config<String, String> newConfig = new Config<>(name, value);
                    allConfigs.add(newConfig);
                }
            }
        }
        return allConfigs.size() != 0;
    }

    public static List<Config<String, ?>> getConfigs() {
        if(!loadConfigs()){
            System.err.println("load configs failed");
        }
        return configs.stream().sorted(Comparator.comparing(Config::getConfigName)).toList();
//        return configs.stream().map(Config::getConfigName).sorted().toList();
    }

    public static List<Config<String, ?>> getAllConfigs() {
        if(!loadAllConfigs()){
            System.err.println("load all configs failed");
        }
//        return allConfigs.stream().map(Config::getConfigName).sorted().toList();

        /*
         * sort
         */
        return allConfigs.stream().sorted(Comparator.comparing(Config::getConfigName)).toList();
        /*
         * reversed sort by config name
         */
//        return allConfigs.stream().sorted((x,y)->y.getConfigName().compareTo(x.getConfigName())).toList();
    }


    public static List<String> getConfigsFromYAML(){
        InputStream inputStream;
        Yaml yaml = new Yaml();
        try {
            inputStream = new FileInputStream("E:\\Java project repositories\\CRSExtractor\\workspace\\cassandra-4.0.6\\conf\\cassandra.yaml");

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
//        return yaml.load(inputStream);

        Map<String, Object> map = yaml.load(inputStream);

//        Set<Config<String,?>> configSet = new HashSet<>();
//        map.forEach((key,value)->{configSet.add(new Config<>(key,value));});
//        return configSet.stream().map(Config::getConfigName).sorted().toList();

        return map.keySet().stream().toList();

    }

    public static List<String> getCassandraConfigs(){
        List<String> configList = new ArrayList<>();
        try {
            File file = new File("E:\\Java project repositories\\CRSExtractor\\src\\main\\resources\\config_files\\cassandra 4.0.6\\cassandra_configs_from_official_website.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String str;
            while ((str = bufferedReader.readLine()) != null)

                configList.add(str);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configList;
    }

}