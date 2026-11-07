package ua.edu.nupp.kpp.server;
import java.sql.*; import java.time.LocalDate; import java.util.*; import java.util.logging.*;

public class AssetService {
 private static final Logger LOG=Logger.getLogger(AssetService.class.getName());
 private static final Map<String,String> TABLES=Map.of("assets","property","users","app_user","requests","property_request","transfers","transfer_record");

 public List<Map<String,Object>> list(String entity)throws SQLException{
  String table=table(entity); List<Map<String,Object>> out=new ArrayList<>();
  try(Connection c=DBUtil.getConnection();PreparedStatement p=c.prepareStatement("SELECT * FROM "+table+" ORDER BY id");ResultSet r=p.executeQuery()){while(r.next())out.add(row(r));}
  return out;
 }
 public Map<String,Object> create(String entity,Map<String,Object>d)throws SQLException{
  try(Connection c=DBUtil.getConnection()){
   return switch(entity){
    case "assets" -> insert(c,"property","INSERT INTO property(name,inventory_number,condition_state,total_units,available_units,criticality) VALUES(?,?,?,?,?,?)",
      p->{p.setString(1,txt(d,"name"));p.setString(2,txt(d,"inventoryNumber"));p.setString(3,txt(d,"conditionState"));int t=num(d,"totalUnits",0,100000);p.setInt(4,t);p.setInt(5,num(d,"availableUnits",0,t));p.setInt(6,num(d,"criticality",0,100));});
    case "users" -> insert(c,"app_user","INSERT INTO app_user(full_name,email,role_name) VALUES(?,?,?)",
      p->{String email=txt(d,"email");if(!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("Некоректна електронна адреса");p.setString(1,txt(d,"fullName"));p.setString(2,email);p.setString(3,txt(d,"roleName"));});
    case "requests" -> createRequest(c,d);
    case "transfers" -> insert(c,"transfer_record","INSERT INTO transfer_record(request_id,notes) VALUES(?,?)",
      p->{p.setLong(1,lng(d,"requestId"));p.setString(2,Objects.toString(d.get("notes"),""));});
    default -> throw new IllegalArgumentException("Невідомий ресурс");
   };
  }
 }
 public Map<String,Object> update(String entity,long id,Map<String,Object>d)throws SQLException{
  String sql; try(Connection c=DBUtil.getConnection()){
   switch(entity){
    case "assets"->{int t=num(d,"totalUnits",0,100000);sql="UPDATE property SET name=?,inventory_number=?,condition_state=?,total_units=?,available_units=?,criticality=? WHERE id=?";try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,txt(d,"name"));p.setString(2,txt(d,"inventoryNumber"));p.setString(3,txt(d,"conditionState"));p.setInt(4,t);p.setInt(5,num(d,"availableUnits",0,t));p.setInt(6,num(d,"criticality",0,100));p.setLong(7,id);one(p);}}
    case "users"->{sql="UPDATE app_user SET full_name=?,email=?,role_name=? WHERE id=?";try(PreparedStatement p=c.prepareStatement(sql)){String email=txt(d,"email");if(!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))throw new IllegalArgumentException("Некоректна електронна адреса");p.setString(1,txt(d,"fullName"));p.setString(2,email);p.setString(3,txt(d,"roleName"));p.setLong(4,id);one(p);}}
    case "requests"->{sql="UPDATE property_request SET quantity=?,planned_return_date=?,timeliness_score=? WHERE id=? AND status='CREATED'";try(PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,num(d,"quantity",1,100000));p.setDate(2,java.sql.Date.valueOf(LocalDate.parse(txt(d,"plannedReturnDate"))));p.setInt(3,num(d,"timelinessScore",0,100));p.setLong(4,id);one(p);}}
    case "transfers"->{sql="UPDATE transfer_record SET notes=? WHERE id=?";try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,Objects.toString(d.get("notes"),""));p.setLong(2,id);one(p);}}
    default -> throw new IllegalArgumentException("Невідомий ресурс");
   }
   return get(c,table(entity),id);
  }
 }
 public void delete(String entity,long id)throws SQLException{
  try(Connection c=DBUtil.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM "+table(entity)+" WHERE id=?")){p.setLong(1,id);one(p);}
 }
 public Map<String,Object> transition(long id,RequestState target)throws SQLException{
  try(Connection c=DBUtil.getConnection()){c.setAutoCommit(false);try{
   Map<String,Object> req=get(c,"property_request",id);if(req==null)throw new IllegalArgumentException("Заявку не знайдено");
   RequestState from=RequestState.valueOf(req.get("status").toString());Lifecycle.transition(from,target);
   long propertyId=((Number)req.get("property_id")).longValue();int qty=((Number)req.get("quantity")).intValue();
   if(target==RequestState.ISSUED){
    try(PreparedStatement p=c.prepareStatement("UPDATE property SET available_units=available_units-? WHERE id=? AND available_units>=?")){p.setInt(1,qty);p.setLong(2,propertyId);p.setInt(3,qty);one(p);}
    try(PreparedStatement p=c.prepareStatement("INSERT INTO transfer_record(request_id,issued_at,notes) VALUES(?,CURRENT_TIMESTAMP,'Видача майна')")){p.setLong(1,id);p.executeUpdate();}
   }
   if(target==RequestState.RETURNED){
    try(PreparedStatement p=c.prepareStatement("UPDATE property SET available_units=LEAST(total_units,available_units+?) WHERE id=?")){p.setInt(1,qty);p.setLong(2,propertyId);one(p);}
    try(PreparedStatement p=c.prepareStatement("UPDATE transfer_record SET returned_at=CURRENT_TIMESTAMP WHERE request_id=? AND returned_at IS NULL")){p.setLong(1,id);p.executeUpdate();}
   }
   try(PreparedStatement p=c.prepareStatement("UPDATE property_request SET status=? WHERE id=?")){p.setString(1,target.name());p.setLong(2,id);one(p);}
   c.commit();LOG.info("Заявку "+id+" переведено у стан "+target);return get(c,"property_request",id);
  }catch(Exception ex){c.rollback();if(ex instanceof SQLException se)throw se;if(ex instanceof RuntimeException re)throw re;throw new SQLException(ex);}finally{c.setAutoCommit(true);}}
 }
 private Map<String,Object> createRequest(Connection c,Map<String,Object>d)throws SQLException{
  long uid=lng(d,"userId"),pid=lng(d,"propertyId");int qty=num(d,"quantity",1,100000),f1=num(d,"timelinessScore",0,100);
  Map<String,Object>a=get(c,"property",pid);if(a==null)throw new IllegalArgumentException("Майно не знайдено");
  int total=((Number)a.get("total_units")).intValue(),available=((Number)a.get("available_units")).intValue(),criticality=((Number)a.get("criticality")).intValue();
  if(qty>available)throw new IllegalArgumentException("Недостатньо доступного майна");
  if(get(c,"app_user",uid)==null)throw new IllegalArgumentException("Користувача не знайдено");
  double index=RiskCalculator.calculate(f1,criticality,RiskCalculator.scarcity(total,available));
  return insert(c,"property_request","INSERT INTO property_request(user_id,property_id,quantity,planned_return_date,timeliness_score,status,integral_index) VALUES(?,?,?,?,?,'CREATED',?)",
   p->{p.setLong(1,uid);p.setLong(2,pid);p.setInt(3,qty);p.setDate(4,java.sql.Date.valueOf(LocalDate.parse(txt(d,"plannedReturnDate"))));p.setInt(5,f1);p.setDouble(6,index);});
 }
 private interface Binder{void bind(PreparedStatement p)throws SQLException;}
 private Map<String,Object> insert(Connection c,String table,String sql,Binder binder)throws SQLException{
  try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){binder.bind(p);p.executeUpdate();try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("Не отримано ID створеного запису");return get(c,table,k.getLong(1));}}
 }
 private Map<String,Object> get(Connection c,String table,long id)throws SQLException{
  if(!Set.of("property","app_user","property_request","transfer_record").contains(table))throw new IllegalArgumentException("Недозволена таблиця");
  try(PreparedStatement p=c.prepareStatement("SELECT * FROM "+table+" WHERE id=?")){p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?row(r):null;}}
 }
 private Map<String,Object> row(ResultSet r)throws SQLException{Map<String,Object>m=new LinkedHashMap<>();ResultSetMetaData md=r.getMetaData();for(int i=1;i<=md.getColumnCount();i++)m.put(md.getColumnLabel(i),r.getObject(i));return m;}
 private String table(String e){String t=TABLES.get(e);if(t==null)throw new IllegalArgumentException("Невідомий ресурс");return t;}
 private String txt(Map<String,Object>d,String k){Object v=d.get(k);if(v==null||v.toString().isBlank())throw new IllegalArgumentException("Обов’язкове поле: "+k);return v.toString().trim();}
 private int num(Map<String,Object>d,String k,int min,int max){try{int v=Integer.parseInt(String.valueOf(d.get(k)));if(v<min||v>max)throw new IllegalArgumentException(k+" має бути в межах "+min+"–"+max);return v;}catch(NumberFormatException ex){throw new IllegalArgumentException("Некоректне числове поле: "+k);}}
 private long lng(Map<String,Object>d,String k){try{long v=Long.parseLong(String.valueOf(d.get(k)));if(v<=0)throw new IllegalArgumentException("ID має бути додатним");return v;}catch(NumberFormatException ex){throw new IllegalArgumentException("Некоректний ID: "+k);}}
 private void one(PreparedStatement p)throws SQLException{if(p.executeUpdate()==0)throw new IllegalArgumentException("Запис не знайдено або не може бути змінений");}
}
