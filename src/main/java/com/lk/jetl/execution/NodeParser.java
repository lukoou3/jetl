package com.lk.jetl.execution;

import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lk.jetl.execution.NodeType.*;

public class NodeParser {
    final Config rootConfig;
    final Map<String, Config> nodeConfigMap = new HashMap<>();
    final Map<String, NodeType> nodeTypeMap = new HashMap<>();
    final Map<String, Node> nodeMap = new HashMap<>();

    public NodeParser(Config rootConfig) {
        this.rootConfig = rootConfig;
    }

    public Node[] parseSinkNodes() {
        List<? extends Config> sourceConfigs = rootConfig.getConfigList("sources");
        List<? extends Config> transformConfigs = rootConfig.getConfigList("transforms");
        List<? extends Config> sinkConfigs = rootConfig.getConfigList("sinks");

        for (Config c : sourceConfigs) {
            nodeConfigMap.put(c.getString("name"), c);
            addNodeType(c.getString("name"), SOURCE);
        }
        for (Config c : transformConfigs) {
            nodeConfigMap.put(c.getString("name"), c);
            addNodeType(c.getString("name"), TRANSFORM);
        }
        for (Config c : sinkConfigs) {
            nodeConfigMap.put(c.getString("name"), c);
            addNodeType(c.getString("name"), SINK);
        }

        Node[] sinkNodes = new Node[sinkConfigs.size()];
        for (int i = 0; i < sinkConfigs.size(); i++) {
            sinkNodes[i] = parseNode(sinkConfigs.get(i));
        }

        return sinkNodes;
    }

    private Node parseNode(Config config) {
        String name = config.getString("name");
        String type = config.getString("type");

        NodeType nodeType = nodeTypeMap.get(name);
        Node node;
        if (nodeType == SOURCE) {
            node = new SourceNode(name, type, config);
            nodeMap.put(name, node);
            return node;
        }

        List<String> depends = config.getStringList("dependencies");
        Node[] dependencies = new Node[depends.size()];
        for (int i = 0; i < depends.size(); i++) {
            String depend = depends.get(i);
            Node dependency = nodeMap.get(depend);
            if (dependency == null) {
                if (!nodeConfigMap.containsKey(depend)) {
                    throw new IllegalArgumentException("unknown node:" + depend);
                }
                dependency = parseNode(nodeConfigMap.get(depend));
            }
            dependencies[i] = dependency;
        }

        if (nodeType == TRANSFORM) {
            node = new TransformNode(name, type, config, dependencies);
        } else {
            node = new SinkNode(name, type, config, dependencies);
        }
        nodeMap.put(name, node);
        return node;
    }

    private void addNodeType(String name, NodeType nodeType){
        if(nodeTypeMap.containsKey(name)){
            throw new IllegalArgumentException("not unique node:" + name);
        }
        nodeTypeMap.put(name, nodeType);
    }
}
