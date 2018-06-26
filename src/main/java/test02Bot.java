import com.mongodb.BasicDBObject;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.bson.Document;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class test02Bot extends TelegramLongPollingBot {
    int code = 0;
    String username = "";
    String input = "";
    long user_id = 0;

    public void onUpdateReceived(Update update) {
        //System.out.println(update.getMessage().getText());
        //System.out.println(update.getMessage().getFrom().getFirstName());

        String command=update.getMessage().getText();

        //var to send strings to bot
        SendMessage message = new SendMessage();

        username = update.getMessage().getChat().getUserName();
        user_id = update.getMessage().getChat().getId();

        String message_text = update.getMessage().getText();
        long chat_id = update.getMessage().getChatId();
        String[] cmdList = {"/start", "/new", "/add", "/update", "/del", "rem", "view", "/all", "/choose", "/help"};

        message.setChatId(chat_id).setText(message_text);
        HashMap<Integer, String> formatCheckRes  = new HashMap<>();
        HashMap<Integer, String> findListnameRes  = new HashMap<>();

       if (command.equals("/start") || command.equals("/new")){
            //System.out.println("ohi there, " +update.getMessage().getFrom().getUserName() + "!");
            message.setText("hello there, " +update.getMessage().getFrom().getUserName() + "!");
            System.out.println("start()");

            code = 1;
            //todo: make default responses for each command a global var to call
            message.setText("okay, type the new list as shown below. Note that only one list can be created at a time.\n\n" +
                        "listname - item 1, item 2, item 3");



            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        else if (command.equals("/update")){
            System.out.println("new()");
            code = 2;
            message.setText("Enter listname to update in the format below: \n\n" +
                        "listname - item 1, item 2, item 3");

            try {
                execute(message);

            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else if (command.equals("/del")){
            System.out.println("del()");
            code = 5;
            message.setText("Name of list to delete: ");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        else if (command.equals("/add")){
            System.out.println("addItem()");
            code = 3;
            message.setText("Enter the name of the of list and item to add. \n Note: only one item can be added using this function \n i.e.:\n listname - item ");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        else if (command.equals("/rem")){
            System.out.println("removeItem()");
            code = 4;
            message.setText("Enter the name of the of list and item to remove. \n Note: only one item can be removed using this function \n i.e.:\n listname - item ");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        else if (command.equals("/view")){
            System.out.println("view()");
            message.setText("Which list to view?");
            code = 7;
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }

        else if (command.equals("/all")){
            System.out.println("all()");

            message.setText(showAll(user_id));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        else if(command.equals("/choose")){
            System.out.println("choose()");
            code = 6;
            message.setText("Which list to choose from?");
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        else if(command.equals("/help")){
            System.out.println("help()");
            String helpText = "Hi! This is the help page. You may use either of the following commands below:\n" +
                    "/start - Start with this bot by creating a list\n" +
                    "/new - Add a new list to your collection\n" +
                    "/add - Add one item to an existing list\n" +
                    "/update - Add/remove/modify items from an existing list\n" +
                    "/del - Delete a list\n" +
                    "/rem - Remove one item from an existing list\n" +
                    "/view - Choose a list to see\n" +
                    "/all - Shows all lists in your collection\n" +
                    "/choose - Choose food choice from a list\n" +
                    "/help - Shows all commands available\n" +
                    " ";

            message.setText(helpText);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
        else if (code != 0) {
            System.out.println("Your code: " + code);


            if (update.hasMessage() && update.getMessage().hasText()){
                if (code >= 1 && code < 5 ){
                    input = update.getMessage().getText().trim();
                    formatCheckRes = checkInput(code, input);


                    if (formatCheckRes.get(0).equals("1")){
                        String listName = getListInfoFromInput(input).get(0).replaceAll("\\s+","").toLowerCase();
                        String listItems = getListInfoFromInput(input).get(1);
                        findListnameRes = ifListexist(user_id, listName);

                        // add item + lname found -> update
                        if (code == 3 && findListnameRes.get(0).equals("1")){
                            addItem(user_id, listName, listItems);
                            message.setText("Item added!");
                        }
                        else if (code == 2 && findListnameRes.get(0).equals("1")){
                            updatelist(user_id, listName, listItems);
                            message.setText("List updated!");
                        }
                        // add list + lname not found -> add
                        else if (( (code == 1 || code == 2) && findListnameRes.get(0).equals("0"))) {
                            addList(user_id, username, listName, listItems);
                            message.setText("List added!");
                        }
                        // rem item + lname  found -> remove
                        else if (code == 4 && findListnameRes.get(0).equals("1")) {
                            remItem(user_id, listName, listItems);
                            message.setText("Item removed! ");
                        }

                        // add list + lname  found -> prompt user to use /update
                        else if (code == 1 && findListnameRes.get(0).equals("1")){
                            message.setText("List name already exists. Use /update to update the list.");

                        }
                        else {
                            // rem item + lname not found ->
                            message.setText("List name does not exist. Use /new to create a new list.");
                        }



                        System.out.println();
                    }
                    else{
                        //message.setText("");
                        message.setText(formatCheckRes.get(1));
                    }

                }

                // /del list
                else {
                    input = update.getMessage().getText().trim().toLowerCase().replaceAll("\\s+","");

                    formatCheckRes = checkInput(code, input);

                    if (formatCheckRes.get(0).equals("1")){

                        findListnameRes = ifListexist(user_id, input);
                        if (findListnameRes.get(0).equals("1")) {
                            if (code == 5){
                                delList(user_id, input);
                                message.setText("List deleted.");
                            }
                            // /choose
                            else if (code == 6){
                                message.setText(choose(user_id, input));
                            }
                            // /showList
                            else if (code == 7) {
                                message.setText(showList(user_id, input));
                            }

                        }
                        else {
                            message.setText(findListnameRes.get(1));
                        }

                    }
                    else {
                        message.setText(formatCheckRes.get(1));
                    }


                }
                //set chatID to reply to
                message.setChatId(update.getMessage().getChatId());

                //send out the message
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }

        }


        //set chatID to reply to
        //message.setChatId(update.getMessage().getChatId());

        //send out the message
        //try {
        //    execute(message);
        //} catch (TelegramApiException e) {
        //    e.printStackTrace();
       // }
    }

        public String getBotUsername() {
            return "test02_bot";
        }

        public String getBotToken() {
            return "YOUR TOKEN";
        }




    public void addList(long uid, String userName, String listName, String listItems ) {
        System.out.println("addlist()");
        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");



        long found = mCollection.count(Document.parse("{uid : " + Long.toString(uid) + "}"));

        System.out.println(mCollection.find(eq("uid", uid)));

        if (found == 0) {
            System.out.println("-found-");
            Document doc = new Document("uid", uid).append("uname", userName);

            mCollection.insertOne(doc);
            Document list = new Document().append("lname", listName).append("items", getItemList(listItems));
            mCollection.updateOne(eq("uname", userName), Updates.addToSet("lists", list));
            mClient.close();

            System.out.println("New user created in database");

        } else {

            System.out.println("user exists - update list");
            updatelist(uid, listName, listItems);


        }
        System.out.println("== Operation Complete ==");

    }

    public void delList(long uid, String listName){
        System.out.println("del()");



        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");

        long c = mCollection.count(and(  eq("uid", uid), eq("lists.lname",listName)));

        if (c >= 1){
            Document filter = new Document("uid", uid);
            Document update = new Document("$pull", new Document("lists", new Document("lname", listName)));
            mCollection.updateOne(filter, update);
            System.out.println("removed!");
        }
        else {
            System.out.println("List not in collection. Please choose an existing list.");
        }

        mClient.close();
        System.out.println("== Operation Complete ==");

    }

    //todo: display list for user to copy-paste + edit
    public void updatelist(long uid, String listName, String listItems){
        System.out.println("updatelist()");



        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");



        long c = mCollection.count(and(  eq("uid", uid), eq("lists.lname",listName)));

        if (c == 1){
            System.out.println("list exist. Updating items");
            Document query = new Document("uid", uid).append("lists.lname", listName);
            Document update = new Document("$set",  new Document("lists.$.items", getItemList(listItems)));
            mCollection.updateOne(query, update);

        }
        else {
            System.out.println("list does not exist. Adding list and items");
            Document list = new Document().append("lname", listName).append("items", getItemList(listItems));
            mCollection.updateOne(eq("uid", uid), Updates.addToSet("lists", list));
        }


        mClient.close();
        System.out.println("== Operation Complete ==");





    }


    public String showAll(long uid){
        System.out.println("showAll()");


        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");

        List<Document> users =  mCollection.find().into(new ArrayList<Document>());
        String res = "";


        for (Document user : users) {
            if (user.getLong("uid").equals(uid)){
                List<Document> lists = (List<Document>) user.get("lists");
                if (lists.size() > 0){
                    for (Document list : lists) {
                        System.out.println(list.getString("lname"));
                        res = res + "======= \n" + list.getString("lname") + "\n=======\n";
                        List<String> items = (List<String>) list.get("items");
                        res = res + getItemStr(items) + "\n";

                    }
                    return res;

                }

            }

        }
        res = "No list in your collection! Use /new to create a list.";
        mClient.close();

        System.out.println("== Operation Complete ==");
        System.out.println("all() res: " + res);
        return res;
    }

    public String showList(long uid, String listName){
        System.out.println("showList()");


        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");

        List<Document> users =  mCollection.find(eq("uid",uid )).into(new ArrayList<Document>());



        for (Document user : users) {


            List<Document> lists = (List<Document>) user.get("lists");
            for (Document list : lists) {

                System.out.println(list.getString("lname"));
                if (list.getString("lname").equals(listName)) {

                    List<String> items = (List<String>) list.get("items");
                    return getItemStr(items);

                }

            }
            break;

        }
        mClient.close();
        System.out.println("== Operation Complete ==");
        return "";


    }

    public List<String> getItemList(String items){
        System.out.println("getlistitems()");

        String itemArr[] = items.trim().split("\\s*,[,\\s]*");
        List<String> itemList = Arrays.asList(itemArr);
        System.out.println("== Operation Complete ==");

        return itemList;

    }


    public String choose(long uid, String listName){
        System.out.println("choose()");


        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");

        List<Document> users =  mCollection.find(eq("uid",uid )).into(new ArrayList<Document>());
        String item = "";

        for (Document user : users) {


            List<Document> lists = (List<Document>) user.get("lists");
            for (Document list : lists) {

                //System.out.println(list.getString("lname"));
                if (list.getString("lname").equals(listName)) {

                    List<String> items = (List<String>) list.get("items");
                    Random rand = new Random();
                    int randomIndex = rand.nextInt(items.size());
                    item = items.get(randomIndex);
                    System.out.println("chosen item: " + item);
                }

            }
            break;



        }
        mClient.close();
        System.out.println("== Operation Complete ==");
        return item;

    }


    public void addItem(long uid, String listName, String newItem) {
        System.out.println("addItem()");


        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");

        long c = mCollection.count(and(  eq("uid", uid), eq("lists.lname",listName), eq("lists.items", newItem) ));

        if (c == 0){
            Document query = new Document("uid", uid).append("lists.lname", listName);
            Document update = new Document("$push", new Document("lists.$.items", newItem));
            mCollection.updateOne(query, update, (new UpdateOptions()).upsert(true));
            System.out.println("updated!");
        }
        else {
            System.out.println("item already in list. No update done.");
        }


        mClient.close();
        System.out.println("== Operation Complete ==");



    }


    public void remItem(long uid, String listName, String remItem){
        System.out.println("remItem()");


        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");


        long c = mCollection.count(and(  eq("uid", uid), eq("lists.lname",listName), eq("lists.items", remItem) ));

        if (c >= 1){
            Document query = new Document("uid", uid).append("lists.lname", listName);
            Document update = new Document("$pull", new Document("lists.$.items", remItem));
            mCollection.updateOne(query, update);
            System.out.println("item removed!");
        }
        else {
            System.out.println("item not in list. No delete.");
        }


        mClient.close();
        System.out.println("== Operation Complete ==");


    }

    public String getItemStr(List<String>  itemArr){
        System.out.println("getItemStr()");
        int arrSize = itemArr.size();
        String itemStr = "";
        int i = 0;
        while (i < arrSize){

            if (i == 0 ){
                itemStr =  itemArr.get(i);
            }
            else {
                itemStr = itemStr + "\n" + itemArr.get(i);
            }
            i++;
        }
        System.out.println("== Operation Complete ==");
        return itemStr;



    }

    public HashMap<Integer, String> checkInput(int code, String input) {
        System.out.println("checkInput()");
        System.out.println("test string: " + input);
        System.out.println("code: "+ Integer.toString(code));
        HashMap<Integer, String> results = new HashMap<>();

        switch (code) {
            case 1:
            case 2: {
                System.out.println("a");
                //if more than one item, each item must be not null or empty
                if (input.contains(" - ")) {
                    System.out.println("b");

                    //check that both left and right hand side of the "-" has non-empty strings
                    List<String> tempInput = Arrays.asList(input.split("\\s* - \\s*"));
                    //System.out.println(tempInput.size());

                    if ((tempInput.size() == 2) && (tempInput.get(0) != null && !tempInput.get(0).isEmpty()) && (tempInput.get(1) != null && !tempInput.get(1).isEmpty())) {
                        System.out.println("pass");
                        results.put(0, "1");
                        results.put(1, "correct format");

                        return results;
                    }
                    System.out.println("c");


                    results.put(0, "0");
                    results.put(1, "Oops, please use the recommended format.");
                    System.out.println("fail");
                    return results;
                }
            }
            case 5:
            case 6:
            case 7: {
                System.out.println("d");

                if (input != null && !input.isEmpty()) {
                    System.out.println("e");

                    results.put(0, "1");
                    results.put(1, "correct format");

                    return results;
                }
                System.out.println("f");
                results.put(0, "0");
                results.put(1, "Oops, please use the recommended format.");
                System.out.println("fail");
                return results;

            }
            case 3:
            case 4: {
                System.out.println("g");
                //if more than one item, each item must be not null or empty
                if (input.contains(" - ") && !input.contains(", ")) {
                    System.out.println("h");

                    //check that both left and right hand side of the "-" has non-empty strings
                    List<String> tempInput = Arrays.asList(input.split("\\s* - \\s*"));

                    if ((tempInput.size() == 2) && (tempInput.get(0) != null && !tempInput.get(0).isEmpty()) && (tempInput.get(1) != null && !tempInput.get(1).isEmpty())) {
                        System.out.println("pass");
                        System.out.println("i");

                        results.put(0, "1");
                        results.put(1, "correct format");

                        return results;
                    }


                }
                System.out.println("j");
                results.put(0, "0");
                results.put(1, "Oops, please use the recommended format.");
                System.out.println("fail");
                return results;
            }


        }
        System.out.println("k");
        results.put(0, "0");
        results.put(1, "Oops, something went wrong somewhere... please try again!");
        System.out.println("fail");
        return results;
    }

    public List<String> getListInfoFromInput(String input){
        List<String> tempInput = Arrays.asList(input.split("\\s* - \\s*"));
        return tempInput;

    }

    public HashMap<Integer, String> ifListexist(long uid, String listName){
        System.out.println("ifListexist()");

        MongoClientURI mConnStr = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mClient = new MongoClient(mConnStr);
        MongoDatabase mDb = mClient.getDatabase("test");
        MongoCollection<Document> mCollection = mDb.getCollection("user");
        HashMap<Integer, String> results = new HashMap<>();

        long c = mCollection.count(and(  eq("uid", uid), eq("lists.lname",listName) ));

        if (c == 0){

            results.put(0, "0");
            results.put(1, "list not found in collection. Please enter an existing list.");
            System.out.println("fail");
            return results;
        }
        results.put(0, "1");
        results.put(1, "list found!");

        return results;

    }



}

