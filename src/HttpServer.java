import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class HttpServer extends Thread {
    public static final String ROOT = "e:/";
    private InputStream input;
    private OutputStream out;
    public HttpServer(Socket socket) {
        try {
            input = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String filePath = read();
        response(filePath);
    }

    private void response(String filePath) {
        File file = new File(ROOT + filePath);
        if (file.exists()) {
            // 1、资源存在，读取资源
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\r\n");//sb装着html源码
                }
                StringBuffer result = new StringBuffer();//result是webserver返回的相应，包含头和html源码
                result.append("HTTP /1.1 200 ok \r\n");
                result.append("Content-Type:text/html \r\n");
                result.append("Content-Length:" + file.length() + "\r\n");
                result.append("\r\n" + sb.toString());

                out.write(result.toString().getBytes());
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            // 2、资源不存在，提示 file not found
            StringBuffer error = new StringBuffer();
            error.append("HTTP /1.1 400 file not found /r/n");
            error.append("Content-Type:text/html \r\n");
            error.append("Content-Length:20 \r\n").append("\r\n");
            error.append("<h1 >File Not Found..</h1>");
            try {
                out.write(error.toString().getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String read() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try {
            // 读取请求头， 如：GET /index.html HTTP/1.1
            String readLine = reader.readLine();
            System.out.println("请求头"+readLine);
            String[] split = readLine.split(" ");
            if (split.length != 3) {
                return null;
            }

            return split[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}