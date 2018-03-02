package com.founder.xunwu.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @program: xunwu
 * @description: es配置类
 * @author: YangMing
 * @create: 2018-03-02 22:36
 **/
@Configuration
public class ElasticSearchConfig {

    @Value("${elasticsearch.cluster.name}")
    private String esName;
    @Value("${elasticsearch.host}")
    private String esHost;
    @Value("${elasticsearch.port}")
    private int esPort;

    @Bean
    public TransportClient  esClient() throws UnknownHostException{
        Settings settings = Settings.builder().put("cluster.name", this.esName)
                .put("client.transport.sniff", true)
                .build();

        InetSocketTransportAddress master = new InetSocketTransportAddress(InetAddress.getByName(this.esHost), esPort);

        TransportClient client = new PreBuiltTransportClient(settings).addTransportAddress(master);
        return client;
    }
}
