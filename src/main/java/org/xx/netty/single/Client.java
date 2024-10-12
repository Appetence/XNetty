package org.xx.netty.single;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @program: Netty
 * @description:
 * @author: liuhao
 * @date: 2024-10-10 10:08
 */
@Slf4j
public class Client {
    public static void main(String[] args) throws IOException {

        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(new InetSocketAddress(8089));
        Selector selector = Selector.open();
        // 先注册
        sc.register(selector, SelectionKey.OP_CONNECT);
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);

        //键盘输入
        Scanner scanner = new Scanner(System.in);
        //单独开个线程让客户端输入信息到服务端
//        subThread(sc);


        while (true) {
            log.info("client wait event happen");
            // 后等待
            selector.select();
            log.info("client event happen");
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                System.out.println("event happent key:" + key);
                if (key.isConnectable()) {
                    System.out.println("client linked server success");
                    // 刷新相关属性信息
                    sc.finishConnect();
                    sc.register(selector, SelectionKey.OP_READ);
                    write(sc);
                }
                if (key.isReadable()) {
                    System.out.println("读事件");
                    SocketChannel channel = (SocketChannel) key.channel();
                    int read = channel.read(byteBuffer);
                    if (read < 0) {
                        log.info("触发通道关闭逻辑 {}", channel);
                        channel.close();
                    }
                    byteBuffer.flip();
                    String result = Charset.defaultCharset().decode(byteBuffer).toString();
                    log.info("read event result : {} read = {}", result, read);
                    byteBuffer.clear();
                }
                if (key.isWritable()) {
                    System.out.println("写事件");
                    SocketChannel channel = (SocketChannel) key.channel();

                    write(channel);
                }
                iterator.remove();

            }
        }
    }

    private static void subThread(SocketChannel sc) {

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
//                    String message = scanner.nextLine();
                String message = "hello world";
                ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
                sc.write(writeBuffer);
                log.info("client message send end");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    private static void write(SocketChannel channel) throws IOException {
        log.info("client writ begin");
        try {
            TimeUnit.SECONDS.sleep(3);
//                    String message = scanner.nextLine();
            String message = "hello world";
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
            channel.write(writeBuffer);
            log.info("client message send end");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("client writ end");
    }
}
