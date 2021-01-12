import java.util.ArrayList;

public class Inventory {
    private ArrayList<Item> items;
public Inventory(){
    items = new ArrayList<Item>();
}
    public boolean addItem(Item item){
        return items.add(item);
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
    public String toString(){}
        String msg = "";

        for(Item i : items) {
            msg += i.getName() + "\n";
        }

        return msg;
    }
}
