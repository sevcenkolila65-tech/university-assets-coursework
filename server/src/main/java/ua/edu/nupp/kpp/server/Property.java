package ua.edu.nupp.kpp.server;
public class Property {
 private long id; private String name, inventoryNumber, conditionState; private int totalUnits, availableUnits, criticality;
 public Property() {} public Property(long id,String name,String inventoryNumber,String conditionState,int totalUnits,int availableUnits,int criticality){this.id=id;this.name=name;this.inventoryNumber=inventoryNumber;this.conditionState=conditionState;this.totalUnits=totalUnits;this.availableUnits=availableUnits;this.criticality=criticality;}
 public long getId(){return id;} public void setId(long v){id=v;} public String getName(){return name;} public void setName(String v){name=v;}
 public String getInventoryNumber(){return inventoryNumber;} public void setInventoryNumber(String v){inventoryNumber=v;} public String getConditionState(){return conditionState;} public void setConditionState(String v){conditionState=v;}
 public int getTotalUnits(){return totalUnits;} public void setTotalUnits(int v){totalUnits=v;} public int getAvailableUnits(){return availableUnits;} public void setAvailableUnits(int v){availableUnits=v;}
 public int getCriticality(){return criticality;} public void setCriticality(int v){criticality=v;}
}
