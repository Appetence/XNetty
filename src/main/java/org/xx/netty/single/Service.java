package org.xx.netty.single;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: Netty
 * @description:
 * @author: liuhao
 * @date: 2024-10-10 10:08
 */
@Slf4j
public class Service {

    private static Selector selector;

    public static void main(String[] args) throws IOException {
        selector = Selector.open();
        ServerSocketChannel ssk = ServerSocketChannel.open();
        ssk.bind(new InetSocketAddress(8089));
        ssk.configureBlocking(false);
        ssk.register(selector, SelectionKey.OP_ACCEPT);

        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("serve wait event happen");
            // 等待事件发生
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                log.info("event happen " + key);
                if (!key.isValid()) {
                    key.cancel();
                    break;
                } else if (key.isAcceptable()) {
                    accept(key);

                } else if (key.isReadable()) {
                    read(key);
                } else if (key.isWritable()) {
                    // not touch
                    log.info("写事件");
                }
                iterator.remove();
            }
        }


    }

    private static void read(SelectionKey key) throws IOException {
        System.out.println("读事件");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer attachment = (ByteBuffer) key.attachment();
        int read = channel.read(attachment);
        if (read <= 0) {
            log.info("读事件：客户端断开连接 {}", channel.socket().getInetAddress());
            channel.close();
            return;
        }
        attachment.flip();
        String result = Charset.defaultCharset().decode((ByteBuffer) key.attachment()).toString();
        log.info("接收到的数据为：{} read = {}", result, read);
//        key.attach(null);
        channel.write(Charset.defaultCharset().encode("hello world rsk"));
    }

    private static void accept(SelectionKey key) throws IOException {

        System.out.println("连接请求事件");
        // 连接事件，由serverSocketChannel处理
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel accept = channel.accept();
        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(16));
        System.out.println("成功链接" + accept.getLocalAddress());
    }
}
