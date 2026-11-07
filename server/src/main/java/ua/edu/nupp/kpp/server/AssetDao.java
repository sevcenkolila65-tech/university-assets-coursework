package ua.edu.nupp.kpp.server;
import java.sql.*; import java.util.*;
public class AssetDao {
 public List<Map<String,Object>> findAll() throws SQLException {
  String sql="SELECT * FROM property ORDER BY id"; List<Map<String,Object>> out=new ArrayList<>();
  try(Connection c=DBUtil.getConnection(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()){
   while(r.next()){Map<String,Object> m=new LinkedHashMap<>(); ResultSetMetaData md=r.getMetaData(); for(int i=1;i<=md.getColumnCount();i++)m.put(md.getColumnLabel(i),r.getObject(i));out.add(m);}
  } return out;
 }
 public void add(String no,String name,String condition,int total,int available,int criticality)throws SQLException{
  String sql="INSERT INTO property(name,inventory_number,condition_state,total_units,available_units,criticality) VALUES(?,?,?,?,?,?)";
  try(Connection c=DBUtil.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setString(1,name);p.setString(2,no);p.setString(3,condition);p.setInt(4,total);p.setInt(5,available);p.setInt(6,criticality);p.executeUpdate();}
 }
}
