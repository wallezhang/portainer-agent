/*
 *  Copyright 2017 WalleZhang
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package cn.chinatelecom.portainer.agent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * @author WalleZhang
 */
public class Main {

    private static volatile CountDownLatch countDownLatch = new CountDownLatch(1);
    private static AsynchronousServerSocketChannel serverSocketChannel;

    public static void main(String[] args) throws IOException {
        String port = System.getenv("PORTAINER_AGENT_PORT") == null ? "5000" : System.getenv("PORTAINER_AGENT_PORT");
        serverSocketChannel = AsynchronousServerSocketChannel.open()
            .setOption(StandardSocketOptions.SO_REUSEADDR, true).bind(new InetSocketAddress(Integer.valueOf(port)));

        serverSocketChannel.accept(null, new AcceptHandler(serverSocketChannel));

        // register endpoint to portainer automatically
        try {
            PortainerApi.registerEndpoint();
        } catch (Exception e) {
            e.printStackTrace();
            Log.info("Register endpoint fail!");
        }

        // Because AIO is non-blocking, so this will prevent the main process exit.
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();
    }
}
