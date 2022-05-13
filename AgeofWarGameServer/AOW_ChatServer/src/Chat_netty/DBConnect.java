package Chat_netty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnect {
	
	static {
		
		 try {
	            Class.forName("org.mariadb.jdbc.Driver");
		 }catch(Exception e) {}
		
	}
	
	// 연결이후 바로 저장 emsg sql 
	public void dbConEmsg(String msg) {
		
		Connection con = null;
        PreparedStatement pstmt = null;   
        ResultSet rs = null;
        try {
           
            con = DriverManager.getConnection(
                "jdbc:mariadb://52.79.216.45:33333/AOW?user=root&password=wltjd741953");
                        
            pstmt = con.prepareStatement("SELECT * FROM `Account`");
            
            rs = pstmt.executeQuery();
            
            while(rs.next()) {
            	System.out.println("->"+ rs.getString("NickName"));
              
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(rs != null) {
                    rs.close(); // 선택 사항
                }
                
                if(pstmt != null) {
                    pstmt.close(); // 선택사항이지만 호출 추천
                }
            
                if(con != null) {
                    con.close(); // 필수 사항
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
		
	}
}
