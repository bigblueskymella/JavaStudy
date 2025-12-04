package webPage_06;
import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class webPage_01 {
	public static void main(String[] args) throws IOException{
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", (exchange) -> {
            Path path = Paths.get("D:\\helloConsole.html"); //절대 경로 HTML 파일 읽기
            byte[] data = Files.readAllBytes(path);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, data.length);
            OutputStream os = exchange.getResponseBody();
            os.write(data);
            os.close();
        });

        server.start();
        System.out.println("🌍 HTML 서버 실행 중: http://localhost:8080");
	}
}

// html 내용이 다~~ 길게 나오는 버전!!
// 02로 넘어가기. 파일 경로 설정시 폴더 명 유의
