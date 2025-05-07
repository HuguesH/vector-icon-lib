package org.architecture;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;

public class Config {
    public static String PROXY_HOST = "localhost";
    public static int    PROXY_PORT = 3128;

    public static Proxy proxy(){
        return Proxy.NO_PROXY;
        //return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(Config.PROXY_HOST, Config.PROXY_PORT));
    }

    public static ProxySelector proxySelector(){
        return ProxySelector.getDefault();
        //return ProxySelector.of(new InetSocketAddress(Config.PROXY_HOST, Config.PROXY_PORT));
    }
}
