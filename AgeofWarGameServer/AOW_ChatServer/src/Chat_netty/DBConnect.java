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
	
	// �������� �ٷ� ���� emsg sql 
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
                    rs.close(); // ���� ����
                }
                
                if(pstmt != null) {
                    pstmt.close(); // ���û��������� ȣ�� ��õ
                }
            
                if(con != null) {
                    con.close(); // �ʼ� ����
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
		
	}
}
