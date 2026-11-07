package ua.edu.nupp.kpp.server;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;

public class ApiServlet extends HttpServlet {
 private static final Logger LOG=Logger.getLogger(ApiServlet.class.getName());
 private final ObjectMapper mapper=new ObjectMapper();
 private final AssetService service=new AssetService();
 @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  try{String[]p=parts(req);send(resp,200,service.list(p[0]));}catch(Exception e){fail(resp,e);}
 }
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  try{String[]p=parts(req);Map<String,Object>d=mapper.readValue(req.getInputStream(),Map.class);Object result;
   if(p.length==3&&"requests".equals(p[0])&&"transition".equals(p[2]))result=service.transition(Long.parseLong(p[1]),RequestState.valueOf(Objects.toString(d.get("to"))));
   else result=service.create(p[0],d);send(resp,201,result);
  }catch(Exception e){fail(resp,e);}
 }
 @Override protected void doPut(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  try{String[]p=parts(req);if(p.length!=2)throw new IllegalArgumentException("Потрібен ID запису");Map<String,Object>d=mapper.readValue(req.getInputStream(),Map.class);send(resp,200,service.update(p[0],Long.parseLong(p[1]),d));}catch(Exception e){fail(resp,e);}
 }
 @Override protected void doDelete(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  try{String[]p=parts(req);if(p.length!=2)throw new IllegalArgumentException("Потрібен ID запису");service.delete(p[0],Long.parseLong(p[1]));send(resp,200,Map.of("message","Запис видалено"));}catch(Exception e){fail(resp,e);}
 }
 private String[]parts(HttpServletRequest req){String x=Objects.toString(req.getPathInfo(),"").replaceAll("^/|/$","");if(x.isBlank())throw new IllegalArgumentException("Не вказано ресурс API");return x.split("/");}
 private void send(HttpServletResponse resp,int status,Object data)throws IOException{resp.setStatus(status);resp.setCharacterEncoding("UTF-8");resp.setContentType("application/json");mapper.writeValue(resp.getOutputStream(),data);}
 private void fail(HttpServletResponse resp,Exception e)throws IOException{LOG.log(Level.WARNING,"Помилка API",e);int status=(e instanceof IllegalArgumentException||e instanceof NumberFormatException)?400:500;send(resp,status,Map.of("error",Objects.toString(e.getMessage(),"Внутрішня помилка сервера")));}
}
