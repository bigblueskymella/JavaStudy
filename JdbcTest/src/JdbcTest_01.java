import java.sql.*;  // JDBC 관련 패키지 가져오기 

public class JdbcTest_01 {

	public static void main(String[] args) {

		// 1️ MySQL 연결 정보 : 01 조회만!!
		// ✅ “JDBC 주소는 MySQL 집 주소야.
		//		localhost는 ‘내 집’,
		//		hansei_bank는 ‘내 방 이름’,
		//		비밀번호는 ‘내 방 열쇠’”
		//	jdbc:mysql://주소:포트/DB명 형식
        String url = "jdbc:mysql://localhost:3306/hansei_bank?serverTimezone=Asia/Seoul&useSSL=false&characterEncoding=utf8";
        String user = "root";  // 또는 student 계정
        String pass = "1234";  // ✅ 설치 시 입력했던 비번

        // 2️ 연결 시도 (22~31 주석: 연결 성공!)
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✅ MySQL 연결 성공!");
          
            // sql 명령 준비해서 실행 (조회) ❤🛑ORDER BY id로 가져옴
            String sql = "SELECT owner, balance FROM account ORDER BY id";
            PreparedStatement ps = conn.prepareStatement(sql); //sql을 db로 전달하는 역할 
            ResultSet rs = ps.executeQuery(); //조회 결과를 ResultSet으로 받음(한 줄씩 읽는 객체) 

            // 3️ DB에서 꺼낸 데이터 한 줄씩 출력
            while (rs.next()) { //결과 한 줄씩 읽기 커서를 다음 행으로 이동
                String owner = rs.getString("owner"); //데이터 꺼내기  
                int balance = rs.getInt("balance");
                System.out.println(owner + "님의 잔액: " + balance + "원");
            }
            
            // 4️ 연결 자동 종료
        } catch (SQLException e) {
            System.out.println("❌ 연결 실패: " + e.getMessage());
        }
	}
}
