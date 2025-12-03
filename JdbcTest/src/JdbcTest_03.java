import java.sql.*; // JDBC 관련 패키지 임포트
import java.util.Scanner;

public class JdbcTest_03 {

	public static void main(String[] args) {
// 🛑 페이지는 id포함해서 조회 퀴즈!! 01을 바꿔서 표현해도 된다.
		//삭제 추가
		
		String url = "jdbc:mysql://localhost:3306/hansei_bank?serverTimezone=Asia/Seoul&useSSL=false&characterEncoding=utf8";
		String user = "root"; // 또는 student 계정
		String pass = "1234"; // ✅ 설치 시 입력했던 비번

		try (Connection conn = DriverManager.getConnection(url, user, pass)) {
			System.out.print("✅ MySQL 연결 성공!");

			// ✅ 사용자 입력 받기
			Scanner sc = new Scanner(System.in);
//			System.out.println("예금주 이름 입력: ");
//			String owner = sc.nextLine();
//			System.out.println("초기 잔액 입력: ");
//			int balance = sc.nextInt();
//			sc.close();

			// ✅ INSERT SQL 실행
//			String sql = "INSERT INTO account (owner, balance) VALUES (?, ?)";
//			PreparedStatement ps = conn.prepareStatement(sql);
//			ps.setString(1, owner);
//			ps.setInt(2, balance);

//			int rows = ps.executeUpdate(); // 실행 후 영향받은 행 수 리턴
//			if (rows > 0) {
//				System.out.println("🎉 계좌 정보가 성공적으로 저장되었습니다!");
//			}

			// ✅ 확인용 SELECT (선택: 데이터 꺼내서 확인할 때)
//			System.out.println("\n📄 현재 DB 상태:");
//			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM account");
//			while (rs.next()) {
//				System.out.println(rs.getString("owner") + " / 잔액: " + rs.getInt("balance"));
//			}

			// ✅ id까지 포함해서 조회
			String sql  = "SELECT id, owner, balance FROM account";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			System.out.println("\n📄 계좌 목록 (ID / 예금주 / 잔액)");
			System.out.println("----------------------------------");
			while (rs.next()) {
				int id = rs.getInt("id");
				String owner = rs.getString("owner");
				int balance = rs.getInt("balance");
				System.out.println(id + "번 / " + owner + " / 잔액: " + balance + "원");
			}
			
			// ✅ 삭제 기능
			System.out.println("\n삭제할 계좌 ID 입력: ");
			int delId = sc.nextInt();

			String delSql = "DELETE FROM account WHERE id = ?";
			PreparedStatement delPs = conn.prepareStatement(delSql);
			delPs.setInt(1, delId); //첫 번째 ?(물음표) 위치. 
			//미리 만들어진 SQL 틀을 사용
			//SQL은 빈칸(?) 을 하나 만들어 놓고, 나중에 자바 코드에서 값을 채워넣는 구조

			int deleted = delPs.executeUpdate();

			if (deleted > 0) {
			    System.out.println("🗑 계좌가 성공적으로 삭제되었습니다!");
			} else {
			    System.out.println("⚠ 해당 ID의 계좌를 찾을 수 없습니다.");
			}

			// 4️ 연결 자동 종료
		} catch (SQLException e) {
			System.out.println("❌ 연결 실패: " + e.getMessage());
		}
	}

}
