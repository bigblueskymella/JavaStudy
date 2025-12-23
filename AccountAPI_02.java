package old_http; // ★ 패키지는 항상 맨 윗줄

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.Map; // ★ Map/HashMap import
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

public class AccountAPI_02 {
	// ✅조회 + ✅추가(INSERT) + ✅수정 + ✅단건삭제
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", exchange -> {
			try { //accounts 먼저 accounts_v2 이후에 css 추가
				Path path = Paths.get("D:/JAVA/JavaStudy/src/old_http/accounts.html"); // ← HTML 경로
				byte[] bytes = Files.readAllBytes(path);
				exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
				exchange.sendResponseHeaders(200, bytes.length);
				OutputStream os = exchange.getResponseBody();
				os.write(bytes);
				os.close();
			} catch (IOException e) {
				exchange.sendResponseHeaders(404, 0);
			}
		});

		// (2) RESTful API:웹 서비스에서 두 시스템 간의 통신을 위해 사용되는
		server.createContext("/api/accounts", new AccountListHandler()); // 조회
		server.createContext("/api/add", new AddHandler()); // ★ 추가(등록)
		server.createContext("/api/update", new UpdateHandler()); // 수정
		server.createContext("/api/delete", new DeleteHandler()); // 삭제
		server.setExecutor(null);
		server.start();
		System.out.println("🚀 서버 실행: http://localhost:8080");
	}

	// ====== 🛑 공통 DB 연결 정보 🛑 ======
	private static final String URL = "jdbc:mysql://localhost:3306/hansei_bank?"; //serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&useSSL=false&characterEncoding=utf8";
	private static final String USER = "root";
	private static final String PASS = "1234";

	// ====== 조회 (GET /api/accounts) ======
	static class AccountListHandler implements HttpHandler {
		public void handle(HttpExchange ex) throws IOException {
			JSONArray list = new JSONArray();
			try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
					Statement stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery("SELECT * FROM account ORDER BY id")) {
				while (rs.next()) {
					JSONObject obj = new JSONObject();
					obj.put("id", rs.getInt("id"));
					obj.put("owner", rs.getString("owner"));
					obj.put("balance", rs.getInt("balance"));
					list.put(obj);
				}
				sendJson(ex, 200, list.toString(2));
			} catch (SQLException e) {
				sendText(ex, 500, "❌ DB 오류: " + e.getMessage());
			}
		}
	}

	// ====== 추가 (POST /api/add?owner=OOO&balance=1000) ======
	static class AddHandler implements HttpHandler {
		public void handle(HttpExchange ex) throws IOException {
			if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
				sendText(ex, 405, "❌ POST 요청만 허용됩니다");
				return;
			}
			// ✅ body 읽기
	        InputStream is = ex.getRequestBody();
	        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
	        // 예: owner=홍길동&balance2=1000
	        
			Map<String, String> map = parseQuery(body);
			String owner = map.get("owner");
			String balStr = map.get("balance2");

			if (owner == null || balStr == null) {
				sendText(ex, 400, "owner, balance 파라미터가 필요합니다.");
				return;
			}
			int balance = Integer.parseInt(balStr);
			
			try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
					PreparedStatement ps = conn
							.prepareStatement("INSERT INTO account (owner, balance) VALUES (?, ?)")) {
				ps.setString(1, owner);
				ps.setInt(2, balance);
				ps.executeUpdate();

				sendText(ex, 200, "✅ 계좌 추가 완료");
				
			} catch (SQLException e) {
				sendText(ex, 500, "❌ DB 오류: " + e.getMessage());
			}
		}
	}

	// ====== 수정 (POST /api/update?id=1&balance=9999) ======
	static class UpdateHandler implements HttpHandler {
		public void handle(HttpExchange ex) throws IOException {
			if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
				sendText(ex, 405, "❌ POST 요청만 허용됩니다");
				return;
			}
			try { //getOrDefault: 찾는 키가 있으면 그 값을 반환, 없으면 정한 기본값(Default) 반환
				String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
				Map<String, String> q = parseQuery(body);
				int id = Integer.parseInt(q.getOrDefault("id", "-1"));
				int balance = Integer.parseInt(q.getOrDefault("balance", "0"));
				try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
						PreparedStatement ps = conn.prepareStatement("UPDATE account SET balance=? WHERE id=?")) {
					ps.setInt(1, balance);
					ps.setInt(2, id);
					int rows = ps.executeUpdate();
					sendText(ex, 200, rows > 0 ? "✅ 수정 완료" : "❌ 해당 ID 없음");
				}
			} catch (Exception e) {
				sendText(ex, 500, "❌ 오류: " + e.getMessage());
			}
		}
	}

	// ====== 삭제(단건) (GET /api/delete?id=3) ======
	static class DeleteHandler implements HttpHandler {
		public void handle(HttpExchange ex) throws IOException {
			if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
				sendText(ex, 405, "Only GET allowed");
				return;
			}
			Map<String, String> q = parseQuery(ex.getRequestURI().getQuery());
			int id;
			try {
				id = Integer.parseInt(q.getOrDefault("id", "-1"));
			} catch (NumberFormatException nfe) {
				sendText(ex, 400, "id는 숫자여야 합니다.");
				return;
			}

			try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
					PreparedStatement ps = conn.prepareStatement("DELETE FROM account WHERE id=?")) {
				ps.setInt(1, id);
				int rows = ps.executeUpdate();
				sendText(ex, 200, rows > 0 ? "🗑️ 삭제 완료" : "❌ 해당 ID 없음");
			} catch (SQLException e) {
				sendText(ex, 500, "❌ DB 오류: " + e.getMessage());
			}
		}
	}

	// ====== 공용 유틸 ======
	private static Map<String, String> parseQuery(String query) {
		Map<String, String> map = new HashMap<>();
		if (query == null || query.isBlank())
			return map;
		for (String pair : query.split("&")) {
			String[] kv = pair.split("=", 2);
			if (kv.length == 2)
				map.put(kv[0], urlDecode(kv[1]));
		}
		return map;
	}

	private static String urlDecode(String s) {
		try {
			return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
		} catch (Exception e) {
			return s;
		}
	}

	private static void sendText(HttpExchange ex, int code, String msg) throws IOException {
		byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
		// 필요 시 CORS 허용 (프론트가 다른 포트에서 열릴 때)
		ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
		ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		ex.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
		ex.sendResponseHeaders(code, bytes.length);
		try (OutputStream os = ex.getResponseBody()) {
			os.write(bytes);
		}
		System.out.println("📤 [sendText] (" + code + ") → " + msg);

	}

	private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
		ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
		ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		ex.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
		ex.sendResponseHeaders(code, bytes.length);
		try (OutputStream os = ex.getResponseBody()) {
			os.write(bytes);
		}
		System.out.println("📤 [sendJson] (" + code + ") → " + json);
	}
}
