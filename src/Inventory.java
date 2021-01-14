import java.util.ArrayList;

public class Inventory {
    private ArrayList<Item> items;
public Inventory(){
    items = new ArrayList<Item>();
}
    public boolean addItem(Item item){
        return items.add(item);
    }

    public Item contains(String name){
        for (int i = 0; i < items.size(); i++){
            if(name.equals(items.get(i).getName())) {
                return items.get(i);
            }
          }
          return null;
    }
    // Returns the item based on the name given
    // If the item is not in the inventory return null
    public Item removeItem(String name) {
        for (int i = 0; i < items.size(); i++){
            if(name.equals(items.get(i).getName())) {
                return items.remove(i);
          }
          
        }
        return null;
    }
    public String toString(){

        if(items.size() == 0){
            return "No items.";
        }
        String msg = "";

        for(Item i : items) {
            msg += i.getName() + "\n";
        }

        return msg;
    }
    //checks if user has the item 
    public boolean hasItem(String itemName){
        for(Item i : items){
            if(i.getName().equals(itemName)){
                return true;
            }
        }
            return false;
        
    }
}
