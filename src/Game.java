import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Class Game - the main class of the "Zork" game.
 *
 * Author: Michael Kolling Version: 1.1 Date: March 2000
 * 
 * This class is the main class of the "Zork" application. Zork is a very
 * simple, text based adventure game. Users can walk around some scenery. That's
 * all. It should really be extended to make it more interesting!
 * 
 * To play this game, create an instance of this class and call the "play"
 * routine.
 * 
 * This main class creates and initialises all the others: it creates all rooms,
 * creates the parser and starts the game. It also evaluates the commands that
 * the parser returns.
 */
class Game {
	private Parser parser;
	private Room currentRoom;
	private Inventory inventory;
	// This is a MASTER object that contains all of the rooms and is easily
	// accessible.
	// The key will be the name of the room -> no spaces (Use all caps and
	// underscore -> Great Room would have a key of GREAT_ROOM
	// In a hashmap keys are case sensitive.
	// masterRoomMap.get("GREAT_ROOM") will return the Room Object that is the Great
	// Room (assuming you have one).
	private HashMap<String, Room> masterRoomMap;
	private HashMap<String, Item> masterItemMap;


	private void initItems(String fileName) throws Exception{
		Scanner itemScanner;
		masterItemMap = new HashMap<String, Item>();

		try {
			
			itemScanner = new Scanner(new File(fileName));
			while (itemScanner.hasNext()) {
				Item item = new Item();
				String itemName = itemScanner.nextLine().split(":")[1].trim();
				item.setName(itemName);
				String itemDesc = itemScanner.nextLine().split(":")[1].trim();
				item.setDescription(itemDesc);	
				Boolean openable = Boolean.valueOf(itemScanner.nextLine().split(":")[1].trim());
				item.setOpenable(openable);
				
				masterItemMap.put(itemName.toUpperCase().replaceAll(" ", "_"), item);
				
				String temp = itemScanner.nextLine();
				String itemType = temp.split(":")[0].trim();
				String name = temp.split(":")[1].trim();
				if (itemType.equals("Room"))
					masterRoomMap.get(name).getInventory().addItem(item);
				else
					masterItemMap.get(name).addItem(item);
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void initRooms(String fileName) throws Exception {
		masterRoomMap = new HashMap<String, Room>();
		Scanner roomScanner;
		try {
			HashMap<String, HashMap<String, String>> exits = new HashMap<String, HashMap<String, String>>();
			roomScanner = new Scanner(new File(fileName));
			while (roomScanner.hasNext()) {
				Room room = new Room();
				// Read the Name
				String roomName = roomScanner.nextLine();
				room.setRoomName(roomName.split(":")[1].trim());
				// Read the Description
				String roomDescription = roomScanner.nextLine();
				room.setDescription(roomDescription.split(":")[1].replaceAll("<br>", "\n").trim());
				// Read the Exits
				String roomExits = roomScanner.nextLine();
				// An array of strings in the format E-RoomName
				String[] rooms = roomExits.split(":")[1].split(",");
				HashMap<String, String> temp = new HashMap<String, String>();
				for (String s : rooms) {
					temp.put(s.split("-")[0].trim(), s.split("-")[1]);
				}

				exits.put(roomName.substring(10).trim().toUpperCase().replaceAll(" ", "_"), temp);

				// This puts the room we created (Without the exits in the masterMap)
				masterRoomMap.put(roomName.toUpperCase().substring(10).trim().replaceAll(" ", "_"), room);

				// Now we better set the exits.
			}

			for (String key : masterRoomMap.keySet()) {
				Room roomTemp = masterRoomMap.get(key);
				HashMap<String, String> tempExits = exits.get(key);
				for (String s : tempExits.keySet()) {
					// s = direction
					// value is the room.

					String roomName2 = tempExits.get(s.trim());
					Room exitRoom = masterRoomMap.get(roomName2.toUpperCase().replaceAll(" ", "_"));
					roomTemp.setExit(s.trim().charAt(0), exitRoom);

				}

			}

			roomScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the game and initialise its internal map.
	 */
	public Game() {
		try {
			initRooms("data/Rooms.dat");	// creates the map from the rooms.dat file
			// initRooms is responsible for building/ initializing the masterRoomMap (private instance variable)
			currentRoom = masterRoomMap.get("ENTRANCE_COURTYARD");	// the key for the masterRoomMap is the name of the room all in Upper Case (spaces replaced with _)
			inventory = new Inventory();
			initItems("data/items.dat");
			/*currentRoom.getInventory().addItem(new Item("Wand", "A wizards wand", false));
			Item bag = new Item("Bag", "a brown paper bag", true);
				bag.addItem(new Item("Apple", "Red Apple"));
				bag.addItem(new Item("Juice", "Red Apple Juice"));
			currentRoom.getInventory().addItem(bag); */
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser = new Parser();
	}

	

	/**
	 * Main play routine. Loops until end of play.
	 */
	public void play() {
		printWelcome();
		// Enter the main command loop.  Here we repeatedly read commands and
		// execute them until the game is over.
		boolean finished = false;
		
		
		while (!finished) {
			Command command = parser.getCommand();
			if(processCommand(command)){
				finished = true;
			}
		//if your health is less than or equal to zero, then Room.healthReturnTF returns true and the game is over
			if(Room.healthReturnTF() == true){
				System.out.println("You have lost all your health, the game is over");
				finished = true;
			}
			//finished = processCommand(command);
		}
		System.out.println("Thank you for playing.  Good bye.");
	}

	/**
	 * Print out the opening message for the player.
	 */
	private void printWelcome() {
		System.out.println();
		System.out.println("Welcome to Hogwarts!");
		System.out.println("In this game, you will need to collect items in order to defeat the villain in the end");
		System.out.println("In order to defeat the villain, you will do a free roam and collect items");
		System.out.println("To find the villain, you will have to go to the restricted floor, then to the trophy room and then find your way from there");
		System.out.println("Be careful, theres a chance that Filch's cat may scratch you");
		System.out.println("To reccover health, eat food, food will be replenished once you have eaten it");
		System.out.println("There is also a bottle of Pheonix Tears in the Headmaster's Office that will recover 100 health, but you need a broom to enter");
		System.out.println("Type 'help' if you need help.");
		System.out.println();
		System.out.println(currentRoom.longDescription());
	}

	/**
	 * Given a command, process (that is: execute) the command. If this command ends
	 * the game, true is returned, otherwise false is returned.
	 */

	 // thsee are all the boolean statmensts for the commands, once the command happens, a specific one becomes true
	private static boolean practiced = false;
	private static boolean hasAlohomora = false;
	private static boolean hasWing = false;
	private static boolean isflying = false;
	private static boolean hasRead = false;
	private static boolean lost10 = false;
	private static boolean usedKey = false;
	private static boolean wearingInvis = false;
	private static boolean snape50 = false;
	private static boolean giveRat = false;
	private static boolean haslistened = false;
	private static boolean giveSleep = false;
	private static boolean hasSnatched = false;
	private static boolean hasPlayed = false;
	private static boolean hasWonChess = false;
	private static boolean voldemort = false;
	private static boolean usedStun = false;
	private static boolean completed = false;

	private boolean processCommand(Command command) {
		if (command.isUnknown()) {
			System.out.println("I don't know what you mean...");
			return false;
		}
		String commandWord = command.getCommandWord();
		if (commandWord.equals("help"))
			printHelp();
		else if (commandWord.equals("go"))
			goRoom(command);
		else if (commandWord.equals("quit")) {
			if (command.hasSecondWord())
				System.out.println("Quit what?");
			else
				return true; // signal that we want to quit
		} else if (commandWord.equals("eat")) {
			if (!command.hasSecondWord()){
				System.out.println("Eat what?");
			}
			else{
				eat(command.getSecondWord());
			}
		} 		
		//snatches a key
		else if (commandWord.equals("snatch")) {
			if(currentRoom.getRoomName().equals("Winged Keys Room")){
				if(isflying != true){
				System.out.println("You need to be flying in order to snatch");
				}
				else{
					System.out.println("You have just caught a key, and you have used the key to open the door");
					hasSnatched = true;
				}
			}
		}
		 else if (commandWord.equals("jump")) {
			return jump();
		} 
		//allows the user to give an item
		else if (commandWord.equals("give")) {
			if (!command.hasSecondWord())
			System.out.println("give what?");
			else{
				if(command.getSecondWord().equals("Rat")){
					if(inventory.hasItem("Rat")){
						if(currentRoom.getRoomName().equals("Boys Washroom")){
						giveRat = true;
						System.out.println("You have just given Ron his rat");
						currentRoom.getInventory().addItem(new Item("InvisibilityCloak", "Harry's invisiblity cloak", false));
						System.out.println("Thanks for getting me my rat, now my rat and I are going to read resurrection methods for fun, Goodbye!");
						System.out.println("Write take InvisibilityCloak to claim your prize and type wear, if you want to become invisible");
						haslistened = true;
						completed = true;
						}
						else{
							System.out.println("You cannot give the rat in that room");
						}
					}
					else{
						System.out.println("You do not have a rat to give");
					}
				}
				else if(command.getSecondWord().equals("SleepingDraught") && currentRoom.getRoomName().equals("Trophy Room") && inventory.hasItem("SleepingDraught")){
					giveSleep = true;
					System.out.println("You have successfully used the SleepingDraught and the dogs are asleep");
					inventory.removeItem("SleepingDraught");
				}
			
				else{
					System.out.println("You cannot give that");
				}
			}
		
		} else if (commandWord.equals("sit")) {
			sit();
		} else if ("udeswn".indexOf(commandWord) > -1) {
			goRoom(command);
			
		} else if (commandWord.equals("take")) {
			if (!command.hasSecondWord())
				System.out.println("Take what?");
			else
				if(inventory.getSize() > 9){
					System.out.println("You are out of storage, your inventory can only store eight items");
				}
				else{
				takeItem(command.getSecondWord());
				}
		} else if (commandWord.equals("drop")) {
			if (!command.hasSecondWord())
				System.out.println("Drop what?");
			else
				dropItem(command.getSecondWord());
		} 
		//allows the user to put on the invisbilty cloak
		else if (commandWord.equals("wear")){
			if(inventory.hasItem("InvisibilityCloak")){
			System.out.println("You have become invisible, type the word: off, to take off the invisibilty cloak");
			wearingInvis = true;
			}
			else{
				System.out.println("You do not have an invisibilty cloak to wear");
			}

			//allows the user to take off the invisiibilty cloak
		} else if (commandWord.equals("off")){
			if(wearingInvis = true){
				System.out.println("Your invisiblity cloak is off");
				wearingInvis = false;
			}
			else{
				System.out.println("You are not wearing an invisibilty cloak, so you cannot take off anything");
			}
		} else if (commandWord.equals("i")) {
			System.out.println("You are carrying the following:" + inventory);
		} else if (commandWord.equals("open")) {
			if (!command.hasSecondWord())
				System.out.println("Open what?");
		} else if (commandWord.equals("open")) {
			if (!command.hasSecondWord())
				System.out.println("Open what?");
			else
				openItem(command.getSecondWord());
		} else if (commandWord.equals("play")){
			if(currentRoom.getRoomName().equals("Chessboard Chamber")){
				hasPlayed = true;
				System.out.println("Type go west to make your first move");
			}
			else{
				System.out.println("You cannot play in this room");
			}
		} else if (commandWord.equals("use")) {
			if (!command.hasSecondWord()){
				System.out.println("Use what?");
			}
			
			else{
				if(command.getSecondWord().equals("Alohomora")){
					if(inventory.hasItem("Alohomora")){
						System.out.println("You have used Alohamora");
						hasAlohomora = true;
					}
					else{
						System.out.println("You do not have Alohomora");
						hasAlohomora = false;
				} 
			}	
			else if(command.getSecondWord().equals("Stupefy")){
				if(inventory.hasItem("Stupefy")){
					System.out.println("You have successfuly used Stupefy, and Voldemort is stunned, leave the room to continue to the end");
					usedStun = true;
				}
				else{
					System.out.println("You do not have Stupefy");
				}
			}
			else if(command.getSecondWord().equals("Wingardium leviosa")){
						if(inventory.hasItem("Wingardium leviosa")){
							System.out.println("You have used Wingardium leviosa");
							return hasWing = true;
							}
						else{
							System.out.println("You do not have Wingardium leviosa");
							return hasWing = false;
						}
					 }
				else if(command.getSecondWord().equals("Key")){
					if(currentRoom.getRoomName().equals("Library")){
						if(inventory.hasItem("Key")){
							System.out.println("You have used your key and unlocked the restriced library");
							usedKey = true;
							inventory.removeItem("Key");
						}else{
							System.out.println("You do not have a key");
						}
					}
					else{
						System.out.println("This room is not locked, no need for key");
					}
					 }
					 else if(command.getSecondWord().equals("PhoenixTears")){
						 if(inventory.hasItem("PhoenixTears")){
						Item item = inventory.removeItem("PhoenixTears");
						System.out.println("You have gained 100 health");
						Room.healthamount(-100);
						inventory.removeItem("PhoenixTears");
						masterRoomMap.get("HEADMASTER'S_OFFICE").getInventory().addItem(item);
						 }
						 else{
							 System.out.println("You don't have PhoenixTears to use");
						 }
					 }
					
					else{
					System.out.println("you cannot use that");
					}
				}
				
			}else if (commandWord.equals("fly")){
					fly();

				}
			else if (commandWord.equals("compete")) {
					if(currentRoom.getRoomName().equals("Quidditch Match")){
						System.out.println("You have won the Quidditch Match, your prize is a broom which will allow you to fly, write 'take Broom' to claim your prise");
						System.out.println("Type fly to fly on your broom and depart to get off");
						currentRoom.getInventory().addItem(new Item("Broom", "A wizards broom", false));
					}else{
						System.out.println("You can only compete in the Quidditch Match");
					}
			}else if (commandWord.equals("practice")){
				if(currentRoom.getRoomName().equals("Broomstick Practice")){
					System.out.println("You just learn the basics of flying using one of the practice brooms");
					practiced = true;
				}
				else{
				System.out.println("You can only practice in the Broomstick Practice Room");
				
				}
			}

			else if (commandWord.equals("depart")) {
				if(isflying = true){
					isflying = false;
					System.out.println("You have departed from your broom");
				}
				else{
					System.out.println("You are not riding a broom, how can you depart?");
				}
			}
			else if (commandWord.equals("read")) {
				if(inventory.hasItem("WizardsChessBook")){
					System.out.println("You have just read Introduction to Wizard's Chess Book, the book says always use the knight");
					hasRead = true;
				}
				else{
					System.out.println("You don't have a book to read, grab WizardChessBook, its a good book, buts its in the restricted section of the library ");
				}
			}
			/*else if (commandWord.equals("play")) {
				if(hasRead = false){
					System.out.println("")
			}*/
			
		return false;
	}

	private void openItem(String itemName) {
		Item item = inventory.contains(itemName);
		
		if(item != null) {
			System.out.println(item.displayContents());
		}else {
			System.out.println("What is it that you think you have but do not.");
		}
		
	}

	private void takeItem(String itemName) {
		Inventory temp = currentRoom.getInventory();
		
		Item item = temp.removeItem(itemName);
		
		if (item != null) {
			if (inventory.addItem(item)) {
				System.out.println("You have taken the " + itemName);
			
				/*
				if (currentRoom.getRoomName().equals("Hallway") &&  itemName.equals("ball")) {
					currentRoom = masterRoomMap.get("ATTIC");
					System.out.println("You seem to be lying on the floor all confused. It seems you have been here for a while.\n");
					System.out.println(currentRoom.longDescription()); 
				}*/
			}else {
				System.out.println("You were unable to take the " + itemName);
			}
		}else {
			System.out.println("There is no " + itemName + " here.");
		}
	}
	
	private void dropItem(String itemName) {
		Item item = inventory.removeItem(itemName);
		
		if (item != null) {
			if (currentRoom.getInventory().addItem(item)) {
				System.out.println("You have dropped the " + itemName);
			}else {
				System.out.println("You were unable to drop the " + itemName);
			}
		}else {
			System.out.println("You are not carrying a " + itemName + ".");
		}
	}
	// allows you to eat/gain health
	private void eat(String secondWord) {
		if (secondWord.equals("ChocolateFrogs") && inventory.hasItem("ChocolateFrogs")){
			Item item = inventory.removeItem("ChocolateFrogs");
			System.out.println("You have just recovered 20 health");
			Room.healthamount(-20);
		//	inventory.removeItem("ChocolateFrogs");
			masterRoomMap.get("THE_GREAT_HALL").getInventory().addItem(item);
		}
		else if (secondWord.equals("EveryFlavourBeans") && inventory.hasItem("EveryFlavourBeans")){
			Item item = inventory.removeItem("ChocolateFrogs");
			System.out.println("You have just recovered 20 health");
			Room.healthamount(-20);
			inventory.removeItem("EveryFlavourBeans");
			masterRoomMap.get("THE_GREAT_HALL").getInventory().addItem(item);
				}
		else if (secondWord.equals("LupinsChocolate") && inventory.hasItem("LupinsChocolate")){
			Item item = inventory.removeItem("ChocolateFrogs");
			System.out.println("You have just recovered 20 health");
			Room.healthamount(-20);
			inventory.removeItem("LupinsChocolate");
			masterRoomMap.get("THE_GREAT_HALL").getInventory().addItem(item);
		}
		else if (secondWord.equals("SherbetLemon") && inventory.hasItem("SherbetLemon")){
			Item item = inventory.removeItem("ChocolateFrogs");
			System.out.println("You have just recovered 20 health");
			Room.healthamount(-20);
			inventory.removeItem("SherbetLemon");
			masterRoomMap.get("THE_GREAT_HALL").getInventory().addItem(item);
		}
		else if (secondWord.equals("Harry'sBirthdayCake") && inventory.hasItem("Harry'sBirthdayCake")){
			Item item = inventory.removeItem("ChocolateFrogs");
			System.out.println("You have just recovered 20 health");
			Room.healthamount(-20);
			inventory.removeItem("Harry'sBirthdayCake");
			masterRoomMap.get("THE_GREAT_HALL").getInventory().addItem(item);
			
		}
		else 
			System.out.println("You do not have the item you want to eat");
		
	}

	private void sit() {
		System.out.println("You are now sitting. You lazy excuse for a person.");
		
	}

	private boolean jump() {
		System.out.println("You jumped. Ouch you fell. You fell hard. Really hard. You are getting sleepy. Very sleepy! Yuo are dead!");
		return true;
	}
	//allows user to fly
	private boolean fly() {
		if(inventory.hasItem("Broom")){
			System.out.println("You are flying");
			isflying = true;
			return true;
			
		}
		else{
			System.out.println("You can't fly, you need a broom");
			isflying = false;
			return false;
		}
	} 
// implementations of user commands:
	/**
	 * Print out some help information. Here we print some stupid, cryptic message
	 * and a list of the command words.
	 */
	private void printHelp() {
		System.out.println("You seem to need help");
		//System.out.println("around at Monash Uni, Peninsula Campus.");
		System.out.println();
		System.out.println("Your command words are:");
		parser.showCommands();
	}

	/**
	 * Try to go to one direction. If there is an exit, enter the new room,
	 * otherwise print an error message.
	 */

	private void goRoom(Command command) {
		if (!command.hasSecondWord() && ("udeswn".indexOf(command.getCommandWord()) < 0)) {
			// if there is no second word, we don't know where to go...
			System.out.println("Go where?");
			return;
		}
		
		String direction = command.getSecondWord();
		if ("udeswn".indexOf(command.getCommandWord()) > -1) {
			direction = command.getCommandWord();
			if (direction.equals("u"))
				direction = "up";
			else if (direction.equals("d"))
				direction = "down";
			else if (direction.equals("e")){
				direction = "east";
			}
			else if (direction.equals("w"))
				direction = "west";
			else if (direction.equals("n"))
				direction = "north";
			else if (direction.equals("s"))
				direction = "south";
		}
		int filchrandom = (int)(Math.random()*10) + 1;
		if(filchrandom == 3){
			System.out.println("Filch's cat has spotted you, you lost 20 health");
			Room.healthamount(20);
		}

// Try to leave current room.
		Room nextRoom = currentRoom.nextRoom(direction);
		if (nextRoom == null)
			System.out.println("There is no door!");

		// These are conditions for every room in this game
		else if(currentRoom.getRoomName().equals("Girls Washroom") && lost10 == false){
			System.out.println("Before you leave, you just got smacked by the troll, you have lost 50 health");
			Room.healthamount(50);
			lost10 = true;
		} 
		else if(currentRoom.getRoomName().equals("Viaduct Entrance") && lost10 == true){
			System.out.println("Just a reminder before you leave the Viaduct Entrance, there is still a troll in the washroom");
			lost10 = false;
		}
		
		else if(currentRoom.getRoomName().equals("Potions Classroom") && snape50 == false){
			System.out.println("Before you leave, Snape has just poured a potion on you, You have lost 50 health");
			Room.healthamount(50);
			snape50 = true;
		}
		else if(currentRoom.getRoomName().equals("Viaduct Entrance") && haslistened == true && completed == false){
			System.out.println("Just a reminder before you leave the Viaduct Entrance, that Ron is looking for his rat");
			haslistened = false;
		}
		else if(currentRoom.getRoomName().equals("Boys Washroom") && haslistened == false){
			Scanner in = new Scanner(System.in);
			System.out.println("Wait before you leave, My name is Ron and I am looking for my Rat, do you have him?");
			System.out.println("I will give you Harry's invisibility cloak if you do, if you have it say yes if not say any word to leave");
			if(in.nextLine().equalsIgnoreCase("yes") && haslistened == false){
				System.out.println("Well then give him to me and we will trade");
				haslistened = true;
						
				
			}			
			else{
				System.out.println("I don't know what you mean, I have not studied english to well, I failed the Owl exams, I can only understand yes");
				System.out.println("I heard my Rats in the gryfindor Common Room but im too afraid to go in their because of Dean");
				haslistened = true;
			}
		}
		else if(currentRoom.getRoomName().equals("Defense Against The Dark Arts") && snape50 == true){
			System.out.println("Just a reminder before you leave the Defense Against the Dark Arts Classroom, Snape is still mad at you, so maybe don't go back in");
			snape50 = false;
		}
		
		else if(nextRoom.getRoomName().equals("Library Restricted Section") && usedKey == false){
				System.out.println("You cannot enter this room without a key, if you have a key, state 'use Key'");

					}
		
		else if(nextRoom.getRoomName().equals("Quidditch Match") && practiced == false){
			System.out.println("You cannot access this room until you have practiced");			
		}
		
		else if(currentRoom.getRoomName().equals("Middle Courtyard") && nextRoom.getRoomName().equals("Gryffindor Common Room") && isflying == true){
			System.out.println("Please depart from your broom before entering the school");
		}
		else if(currentRoom.getRoomName().equals("Middle Courtyard") && nextRoom.getRoomName().equals("Long Gallery") && isflying == true){
			System.out.println("Please depart from your broom before entering the school");
		}
		else if(currentRoom.getRoomName().equals("Middle Courtyard") && nextRoom.getRoomName().equals("Library") && isflying == true){
			System.out.println("Please depart from your broom before entering the school");
		}
		else if(nextRoom.getRoomName().equals("Headmaster's Office") && isflying != true){
			System.out.println("You need to be flying to access this room");
		}
		else if(nextRoom.getRoomName().equals("Winged Keys Room") && giveSleep == false){
			System.out.println("You need to give a sleeping potion to the dogs to make them fall asleep, you have been here too long, lose 20 points");
			Room.healthamount(20);
		}
		else if(nextRoom.getRoomName().equals("Trophy Room") && wearingInvis != true){
			System.out.println("You cannot go to the Trophy Room without an invisibilty cloak");
		}
		else if(nextRoom.getRoomName().equals("Trophy Room") && hasAlohomora != true){
			System.out.println("In order to enter, you need to use Alohomora to unlock the door");
		}
		else if(nextRoom.getRoomName().equals("Chessboard Chamber") && hasSnatched == false){
				System.out.println("You cannot enter the next room until you are on a broom and have snatched a key");
			
			
		}
		else if(nextRoom.getRoomName().equals("Villain Room") && hasRead == false && hasPlayed == false && hasWonChess == false){
			System.out.println("You cannot enter the room unless you have read the chess book and played the chess game in this room");
			System.out.println("Type play once you are ready also make sure you have undesrtood the book before playing chess");
		
	}
		else if(nextRoom.getRoomName().equals("Villain Room") && hasRead == true && hasPlayed == false && hasWonChess == false){
			System.out.println("You cannot enter the room unless you have read the chess book and played the chess game in this room");
			System.out.println("Type play once you are ready also make sure you have undesrtood the book before playing chess");
	
		}
		else if(hasRead == true && hasPlayed == true && hasWonChess == false){
			Scanner in = new Scanner(System.in);
			System.out.println("These are the final minutes of the chess game, will you sacrifice your queen or knight");
			System.out.println("If you know the answer choose between queen or knight");
			if(in.nextLine().equalsIgnoreCase("knight")){
				System.out.println("You choose correct, you can now go to the next room, the Villain Room");
				hasWonChess = true;
			}
		
			else{
				System.out.println("You chose wrong, lose 20 points and try again, read the book to find the answer");
				Room.healthamount(20);
			}
		}
		else if(currentRoom.getRoomName().equals("Villain Room") && nextRoom.getRoomName().equals("Chessboard Chamber") && voldemort == false && usedStun == false){
			System.out.println("Yes run away, you will never defeat me");
			System.out.println("You have just lost 50 health by trying to escape");
			System.out.println("In order to defeat him, you will need to use Stupefy");
			Room.healthamount(50);
			voldemort = true;
		}
		else if(currentRoom.getRoomName().equals("Villain Room") && usedStun == true){
			System.out.println("You have beaten the game and defeated the dark lord");
			System.out.println("Dumbuldore comes to see you and gives you 150 house points and your house, Gryffindor Wins");
			System.out.println("You are done the game, you can know quit by using commmand word 'quit'");
			voldemort = true;
		}
		else {
			currentRoom = nextRoom;
			System.out.println(currentRoom.longDescription());
		}
	}

}
