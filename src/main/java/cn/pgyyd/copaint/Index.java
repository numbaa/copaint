package cn.pgyyd.copaint;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.HashMap;
import java.util.Map;

public class Index extends AbstractVerticle {
    private Map<String, ServerWebSocket> wsConn = new HashMap<>();

    //main里那句vertx.deployVerticle(Index.class.getName())会调用这个start()
    @Override
    public void start() {
        //创建静态资源路由，这里设置成除了index.html，其它地址直接404
        //此处的vertx是继承而来，最终的实例就是我们在main创建那个
        Router staticResourceRouter = Router.router(vertx);
        staticResourceRouter.route("/index.html").handler(StaticHandler.create("web-root"));    //当请求index.html时，到web-root目录下找这个文件


        //1. 用vertx对象创建一个http服务器
        //2. 给该http服务器添加一个URI路由器，这个路由器只处理了index.html这个页面
        //3. 给该http服务器添加一个websocket处理器
        //PS: createHttpServer()返回一个HttpServer对象,requestHandler()和websocketHandler()都返回this，所以可以链式调用，一直添加处理器
        //PSS: websocketHandler()接受了一个lambda(匿名函数)做参数，这个调用类似这样websocketHandler(myHandler)，当建立新ws连接时，vertx会执行myHandler(ws)
        vertx.createHttpServer()
                .requestHandler(staticResourceRouter)
                .websocketHandler(ws -> {
                    System.out.println("ws connected");
                    String id = ws.binaryHandlerID();
                    if (!wsConn.containsKey(id)) {
                        wsConn.put(id, ws);
                    }
                    ws.frameHandler(frame -> {
                        System.out.println("ws receive data");
                        Buffer data = frame.binaryData();
                        for (Map.Entry<String, ServerWebSocket> entry : wsConn.entrySet()) {
                            if (id.equals(entry.getKey())) {
                                continue;
                            }
                            if (frame.isBinary()) {
                                System.out.println("data type is bin");
                                entry.getValue().writeBinaryMessage(
                                        frame.binaryData()
                                );
                            } else if (frame.isText()) {
                                System.out.println("data type is text");
                                entry.getValue().writeTextMessage(
                                        frame.textData()
                                );
                            }
                        }
                    });
                    ws.closeHandler(f -> {
                        wsConn.remove(id);
                        System.out.println("ws closed");
                    });
                })
                .listen(8080);
    }
}
