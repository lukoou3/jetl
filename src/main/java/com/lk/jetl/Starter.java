package com.lk.jetl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lk.jetl.execution.*;
import com.lk.jetl.sql.DataFrame;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public class Starter {

    public static void main(String[] args) throws Exception {
        Config config;
        if (args.length < 1) {
            throw new IllegalArgumentException("please input config file path");
        }else{
            config = ConfigFactory.parseFile(new File(args[0]));
        }

        Node[] nodes = new NodeParser(config).parseSinkNodes();

        for (int i = 0; i < nodes.length; i++) {
            //System.out.println(JSON.toJSONString(nodes[i], JSONWriter.Feature.PrettyFormat));
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(nodes[i]));
            //System.out.println(nodes[i]);
        }

        DataFrame df = nodes[0].execute();
        JEtlContext.runJob(df);
    }




}
