/** Starter code for LP3
 *  @author
 */

// Change to your net id
package lab160730;

// If you want to create additional classes, place them in this file as subclasses of MDS

import java.util.*;

public class MDS {
    // Add fields of MDS here
    HashMap<Integer,Item> map;
    HashMap<Integer, TreeSet<Item>> nameSets;
    // Constructors
    public MDS() {
        map = new HashMap<>();
        nameSets = new HashMap<>();
    }

    /* Public methods of MDS. Do not change their signatures.
       __________________________________________________________________
       a. Insert(id,price,list): insert a new item whose description is given
       in the list.  If an entry with the same id already exists, then its
       description and price are replaced by the new values, unless list
       is null or empty, in which case, just the price is updated.
       Returns 1 if the item is new, and 0 otherwise.
    */
    public int insert(int id, int price, java.util.List<Integer> list) {
        Item item = map.get(id);

        //Item exists?
        if(item != null) {

            //Remove item from the nameSets
            for(Integer name : item.getNames()){
                //Grab corresponding treeSet based on the name
                TreeSet<Item> treeSet = nameSets.get(name);

                //Remove the item, no need to check for null since it should exist if its in the item's names
                treeSet.remove(item);

                //Remove empty TreeSets from nameSet
                if(treeSet.isEmpty())
                    nameSets.remove(name);
            }

            //Change the price of the item
            item.setPrice(price);

            //Update Item's names?
            if(!list.isEmpty()) {
                //replace the names of the item with list
                item.replaceNames(list);

                //Add the item to the nameSets using lists
                for (Integer name : list) {
                    if (!nameSets.containsKey(name)) {
                        nameSets.put(name, new TreeSet<>());
                    }

                    nameSets.get(name).add(item);
                }
            }
            //Re-add updated items to sort them back into TreeSets
            for(Integer name : item.getNames()){
                if(!nameSets.containsKey(name)){
                    nameSets.put(name, new TreeSet<>());
                }

                nameSets.get(name).add(item);
            }

            return 0;
        }
        else { //item doesn't exist, add new item
            Item newItem = new Item(id,price,list);
            //Place new item in the map
            map.put(id, newItem);

            //Add item to the TreeSet of each name
            for(Integer name : list){
                //If the TreeSet does not exist, create a new one and add it to nameSets
                if(!nameSets.containsKey(name)) {
                    nameSets.put(name,new TreeSet<>());
                }

                //Add item to corresponding TreeSet
                nameSets.get(name).add(newItem);
            }
            return 1;
        }
    }



    // b. Find(id): return price of item with given id (or 0, if not found).
    public int find(int id) {
        Item item = map.get(id);
        return (item!=null)?item.getPrice():0;
    }

    /*
       c. Delete(id): delete item from storage.  Returns the sum of the
       ints that are in the description of the item deleted,
       or 0, if such an id did not exist.
    */
    public int delete(int id) {
        //Retrieve item from map
        Item item = map.get(id);

        //if the item doesnt exist return 0
        if(item == null) return 0;

        //Pre: item is in every TreeSet of each name its associated with
        //Post: item is removed from every TreeSet of each name its associated with
        for(Integer names : item.getNames()) {
            TreeSet<Item> set = nameSets.get(names);
            set.remove(item);
            if(set.isEmpty())
                nameSets.remove(names);
        }

        //Grab the sum of the names
        int sum = item.getSum();

        //Remove the item from the map
        map.remove(id);


        //item is the only reference to the object and should be garbage collected when it loses scope
        return sum;
    }

    /*
       d. FindMinPrice(n): given an integer, find items whose description
       contains that number (exact match with one of the ints in the
       item's description), and return lowest price of those items.
       Return 0 if there is no such item.
    */
    public int findMinPrice(int n) {
        return (nameSets.get(n)!=null)?nameSets.get(n).first().getPrice():0;
    }

    /*
       e. FindMaxPrice(n): given an integer, find items whose description
       contains that number, and return highest price of those items.
       Return 0 if there is no such item.
    */
    public int findMaxPrice(int n) {
        return (nameSets.get(n) != null)?nameSets.get(n).last().getPrice():0;
    }

    /*
       f. FindPriceRange(n,low,high): given int n, find the number
       of items whose description contains n, and in addition,
       their prices fall within the given range, [low, high].
    */
    public int findPriceRange(int n, int low, int high) {
        //Retrieve the corresponding TreeSet for the name n
        TreeSet<Item> treeSet = nameSets.get(n);

        //return 0 if there is no TreeSet associated with n
        if(treeSet == null) return 0;

        //Count to keep track of the number of values that contain n
        int count = 0;

        //For each item in treeSet check if the item price is between high and low
        //if it is add it to the count
        for(Item i : treeSet){
            if(i.getPrice() > high)
                break;
            else if(i.getPrice() >= low)
                count++;
        }

        //return the number of items in range
        return count;
    }

    /*
      g. RemoveNames(id, list): Remove elements of list from the description of id.
      It is possible that some of the items in the list are not in the
      id's description.  Return the sum of the numbers that are actually
      deleted from the description of id.  Return 0 if there is no such id.
    */
    public int removeNames(int id, java.util.List<Integer> list) {
        //Retrieve item
        Item item = map.get(id);

        //If the item doesnt exist return 0
        if(item == null) return 0;

        //Get the set of itemNames
        HashSet<Integer> itemNames = item.getNames();

        //To record the sum of removed names
        int result = 0;
        for(Integer name : list){

            //if the item has a matching name, remove it from TreeSet and the item's HashSet
            if(itemNames.contains(name)){
                TreeSet<Item> treeSet = nameSets.get(name);
                treeSet.remove(item);

                //Remove the TreeSet from NameSets if its empty
                if(treeSet.isEmpty())
                    nameSets.remove(name);

                //remove name from the item's names
                itemNames.remove(name);

                //Update the sum of the names of the item
                item.setSum(item.getSum()-name);

                //Add name to the return result
                result += name;
            }
        }
        return result;
    }

    public class Item implements Comparable<Item> {
        private int id;
        private int price;
        private int sum;
        private HashSet<Integer> names;

        public Item(int i, int p, List<Integer> n){
            id = i;
            price = p;
            sum = 0;
            replaceNames(n);
        }

        public int getID(){return id;}

        public int getSum(){return sum;}

        public void setSum(int s){sum = s;}

        public int getPrice(){return price;}

        public void setPrice(int p){price = p;}

        public HashSet<Integer> getNames(){return names;}

        //Replace the elements of names with the elements of n
        public void replaceNames(List<Integer> n){
            names = new HashSet<>();
            sum = 0;

            for(Integer j : n) {
                //Only add to the sum only if j isnt already in the set
                if(names.add(j))
                    sum+=j;
            }
        }

        //compareTo compares prices of Items, if the prices are equal, it compares the IDs of the items
        @Override
        public int compareTo(Item other){
            int difference = price - other.getPrice();

            if(difference != 0)
                return difference;

            return id - other.id;
        }

        //If this object and other have the same ID then they are the same Item
        @Override
        public boolean equals(Object other) {
            if(other instanceof Item)
                return (id == ((Item)other).getID());
            else
                return false;
        }
    }
}
