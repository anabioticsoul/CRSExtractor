package main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pascal.taie.Main;
import utils.Arguments;
import utils.ResultHandler;

import java.util.Set;



public class TaieMain {


    public static void main(String[] args) {

//        long start = System.currentTimeMillis();

        ResultHandler.init();

        Main.main(Arguments.get());

        /*
         * dump extracted configs
         */
        ResultHandler.dumpExtractedConfigs();

        /*
         * dump taints
         */
//        ResultHandler.dumpTaintFlows();
        ResultHandler.dumpAccessControlConfigs();
//        ResultHandler.dumpConfigConstraints();

    }
}
