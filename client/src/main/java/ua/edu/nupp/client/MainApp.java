package ua.edu.nupp.client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;

public class MainApp extends Application {
 private static final String BASE=System.getenv().getOrDefault("ASSETS_API_BASE","http://localhost:8080/university-assets/api");
 private final HttpClient http=HttpClient.newHttpClient(); private final ObjectMapper json=new ObjectMapper();
 private final TableView<Map<String,Object>> table=new TableView<>();
 private final ComboBox<String> resource=new ComboBox<>();
 private final Label status=new Label("Готово");
 private final ProgressBar progress=new ProgressBar(0);
 @Override public void start(Stage stage){
  resource.getItems().addAll("assets","users","requests","transfers");resource.setValue("assets");
  Button refresh=new Button("Оновити");refresh.setOnAction(e->load());
  Button add=new Button("Додати запис");add.setOnAction(e->addRecord());
  Button delete=new Button("Видалити вибраний");delete.setOnAction(e->deleteRecord());
  Button approve=new Button("Схвалити");approve.setOnAction(e->transition("APPROVED"));
  Button issue=new Button("Видати");issue.setOnAction(e->transition("ISSUED"));
  Button returned=new Button("Повернути");returned.setOnAction(e->transition("RETURNED"));
  Button csv=new Button("Імпорт CSV");csv.setOnAction(e->importCsv(stage));
  HBox actions=new HBox(8,resource,refresh,add,delete,approve,issue,returned,csv);
  VBox root=new VBox(10,actions,table,new HBox(10,progress,status));root.setPadding(new Insets(12));VBox.setVgrow(table,Priority.ALWAYS);
  stage.setTitle("Облік університетського майна");stage.setScene(new Scene(root,1050,620));stage.show();load();
 }
 private void load(){
  try{HttpRequest r=HttpRequest.newBuilder(URI.create(BASE+"/"+resource.getValue())).GET().build();
   var response=http.send(r,HttpResponse.BodyHandlers.ofString());if(response.statusCode()>=300)throw new IOException(response.body());
   List<Map<String,Object>> rows=json.readValue(response.body(),new TypeReference<>(){});table.getColumns().clear();table.getItems().clear();
   if(!rows.isEmpty()){for(String key:rows.get(0).keySet()){TableColumn<Map<String,Object>,String> c=new TableColumn<>(key);c.setCellValueFactory(v->new javafx.beans.property.SimpleStringProperty(Objects.toString(v.getValue().get(key),"")));table.getColumns().add(c);}table.getItems().addAll(rows);}
   status.setText("Записів: "+rows.size());
  }catch(Exception ex){status.setText("Помилка: "+ex.getMessage());}
 }
 private void addRecord(){
  Dialog<Map<String,String>> d=new Dialog<>();d.setTitle("Новий запис");ButtonType save=new ButtonType("Зберегти",ButtonBar.ButtonData.OK_DONE);d.getDialogPane().getButtonTypes().addAll(save,ButtonType.CANCEL);
  GridPane g=new GridPane();g.setHgap(8);g.setVgap(8);Map<String,TextField> fields=new LinkedHashMap<>();
  List<String> names=switch(resource.getValue()){case "assets"->List.of("name","inventoryNumber","conditionState","totalUnits","availableUnits","criticality");case "users"->List.of("fullName","email","roleName");case "requests"->List.of("userId","propertyId","quantity","plannedReturnDate","timelinessScore");default->List.of("requestId","notes");};
  for(int i=0;i<names.size();i++){TextField f=new TextField();fields.put(names.get(i),f);g.addRow(i,new Label(names.get(i)),f);}d.getDialogPane().setContent(g);
  d.setResultConverter(b->{if(b==save){Map<String,String>m=new LinkedHashMap<>();fields.forEach((k,v)->m.put(k,v.getText()));return m;}return null;});
  d.showAndWait().ifPresent(m->{try{Map<String,Object>payload=new LinkedHashMap<>(m);for(String k:List.of("totalUnits","availableUnits","criticality","userId","propertyId","quantity","timelinessScore","requestId"))if(payload.containsKey(k))payload.put(k,Integer.parseInt(m.get(k)));send("POST",resource.getValue(),payload);load();}catch(Exception ex){status.setText("Помилка: "+ex.getMessage());}});
 }
 private void deleteRecord(){Map<String,Object> row=table.getSelectionModel().getSelectedItem();if(row==null)return;try{send("DELETE",resource.getValue()+"/"+row.get("id"),null);load();}catch(Exception ex){status.setText(ex.getMessage());}}
 private void transition(String target){if(!"requests".equals(resource.getValue()))return;Map<String,Object> row=table.getSelectionModel().getSelectedItem();if(row==null)return;try{send("POST","requests/"+row.get("id")+"/transition",Map.of("to",target));load();}catch(Exception ex){status.setText(ex.getMessage());}}
 private void send(String method,String path,Object payload)throws Exception{HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(BASE+"/"+path));if(payload==null)b.method(method,HttpRequest.BodyPublishers.noBody());else b.header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)));HttpResponse<String>r=http.send(b.build(),HttpResponse.BodyHandlers.ofString());if(r.statusCode()>=300)throw new IOException(r.body());}
 private void importCsv(Stage stage){
  FileChooser fc=new FileChooser();fc.setTitle("Виберіть CSV-файл");fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV","*.csv"));File f=fc.showOpenDialog(stage);if(f==null)return;
  Task<Integer> task=new Task<>(){protected Integer call()throws Exception{int count=0;try(BufferedReader br=Files.newBufferedReader(f.toPath())){String line;boolean first=true;while((line=br.readLine())!=null){if(first){first=false;continue;}if(line.isBlank())continue;String[]p=line.split(",",-1);if(p.length<6)continue;Map<String,Object>d=Map.of("name",p[0].trim(),"inventoryNumber",p[1].trim(),"conditionState",p[2].trim(),"totalUnits",Integer.parseInt(p[3].trim()),"availableUnits",Integer.parseInt(p[4].trim()),"criticality",Integer.parseInt(p[5].trim()));send("POST","assets",d);count++;updateProgress(count,-1);} }return count;}};
  progress.progressProperty().bind(task.progressProperty());task.setOnSucceeded(e->{progress.progressProperty().unbind();progress.setProgress(1);status.setText("Імпортовано записів: "+task.getValue());load();});task.setOnFailed(e->{progress.progressProperty().unbind();status.setText("Помилка CSV: "+task.getException().getMessage());});new Thread(task,"csv-import").start();
 }
 public static void main(String[]args){launch(args);}
}
