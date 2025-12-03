import java.sql.*;  // JDBC 관련 패키지 임포트 // 입력받아서 sql에 저장 
import java.util.Scanner;

// 입력받아 삽입, 조회
//		 ❤🖤🛑SELECT 실행할 때 사용하는 메서드: executeQuery()
//		 ❤🖤🛑INSERT/UPDATE/DELETE 실행 시 사용하는 메서드: executeUpdate()
public class JdbcTest_02 {

	public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/hansei_bank?serverTimezone=Asia/Seoul&useSSL=false&characterEncoding=utf8";
        String user = "root";  // 또는 student 계정
        String pass = "1234";  // ✅ 설치 시 입력했던 비번

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✅ MySQL 연결 성공!");

            // ✅ 사용자 입력 받기
            Scanner sc = new Scanner(System.in);
            System.out.println("예금주 이름 입력: ");
            String owner = sc.nextLine();
            System.out.println("초기 잔액 입력: ");
            int balance = sc.nextInt();     

            // ✅ INSERT SQL 실행 [인 투 [테이블명] ([컬럼1][컬럼2]) 밸류([값1], [값2])를 넣어라
            String sql = "INSERT INTO account (owner, balance) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql); //sql 명령 준비 
            ps.setString(1, owner);
            ps.setInt(2, balance);

            int rows = ps.executeUpdate();  // 실행 후 영향받은 행 수 리턴
            if (rows > 0) { //DB에 한 줄 이상 저장
                System.out.println("🎉 계좌 정보가 성공적으로 저장되었습니다!");
            }
            
            // ✅ 확인용 SELECT (선택)
            System.out.println("\n📄 현재 DB 상태:");
            //❤🛑ORDER BY id 아닌 account 통째로 가져옴 
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM account");
            while (rs.next()) {
            	System.out.println(rs.getString("owner") + " / 잔액: " + rs.getInt("balance"));
            }
            
            // 4️ 연결 자동 종료 ("close 가능한" 객체들)
        } catch (SQLException e) {
            System.out.println("❌ 연결 실패: " + e.getMessage());
        }
	}
}
