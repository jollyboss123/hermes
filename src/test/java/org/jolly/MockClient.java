package org.jolly;

import java.io.*;
import java.net.Socket;

public class MockClient {
    private final Socket socket;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    public MockClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    public byte[] sendCommand(String command) throws IOException {
        outputStream.write(command.getBytes());
        outputStream.flush();
        byte[] buf = new byte[1024];
        int n = inputStream.read(buf);
        byte[] res = new byte[n];
        System.arraycopy(buf, 0, res, 0, n);
        return res;
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
