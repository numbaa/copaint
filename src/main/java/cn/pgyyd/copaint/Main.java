package cn.pgyyd.copaint;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        //Vertx维护着一些EventLoop（Reactor)，当有event产生时，比如收到tcp连接请求、tcp连接收到新数据、连接断开、收到http请求...Vertx会执行我们给它设置好的回调函数
        Vertx vertx = Vertx.vertx();
        //处理逻辑都写在了Index类里...
        vertx.deployVerticle(Index.class.getName());
    }
}
