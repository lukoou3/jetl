package com.lk.jetl.configuration;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.typesafe.config.*;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConfigTest {

    /**
     * 会解析vm参数, 优先级比文件高
     * -DconfigurePath=hello
     */
    @Test
    public void test() throws Exception {
        //System.out.println(System.getProperties());
        System.setProperty("env.parallelism2", "2");
        // application, example
        Config conf = ConfigFactory.load("application.conf")
                .resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))
                .resolveWith(
                        ConfigFactory.systemProperties(),
                        ConfigResolveOptions.defaults().setAllowUnresolved(true));
        //System.out.println(conf);
        List<? extends Config> source = conf.getConfigList("sources");
        Map<String, Object> map = conf.root().unwrapped();
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            String key = iterator.next().getKey();
            if(!key.startsWith("source") && !key.startsWith("transform")&& !key.startsWith("sink")){
                iterator.remove();
            }
        }
        System.out.println(map);
        System.out.println(JSON.toJSONString(map, JSONWriter.Feature.PrettyFormat));
    }

    @Test
    public void test2() throws Exception {
        com.typesafe.config.Config config2 = com.typesafe.config.ConfigFactory.parseFile(
                        Paths.get(ConfigTest.class.getResource("/example2.conf").toURI())
                                .toFile());
        Config config = ConfigFactory.load("example2.conf")
                .resolve(ConfigResolveOptions.defaults().setAllowUnresolved(true))
                .resolveWith(
                        ConfigFactory.systemProperties(),
                        ConfigResolveOptions.defaults().setAllowUnresolved(true));
        String jsonString = config.root().render(ConfigRenderOptions.concise());
        JSONObject jsonObject = JSON.parseObject(jsonString);

        Config source = config.getConfig("sources").getConfig("Kafka");
        ReadonlyConfig options = ReadonlyConfig.fromConfig(source);
        Map<String, String> stringMap = options.get(Options.key("kafka.config")
                .mapType()
                .noDefaultValue());
        System.out.println(stringMap);
    }

}
