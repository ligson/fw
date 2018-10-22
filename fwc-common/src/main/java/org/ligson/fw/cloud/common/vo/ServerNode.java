package org.ligson.fw.cloud.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServerNode implements Serializable {
    private String ip;
    private int port;
    private String serviceId;
}
