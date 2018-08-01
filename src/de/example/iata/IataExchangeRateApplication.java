/*************************************
* 
*    Modified by: Obed Tetteh
*	 Submitted to: AEB
*		
**************************************/

package de.example.iata;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Collections.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class IataExchangeRateApplication{
	//list of arraylist to hold csv file data
	private static List<ArrayList> csvData = new CopyOnWriteArrayList<ArrayList>();

	//private SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

	public void run() throws IOException {
		readIataExchangeRates();
		//System.out.println(csvData);
		
		displayMenu();
		
		boolean exitRequested = false;
		
		while(!exitRequested) {
			String userInput = getUserInput();
			
			exitRequested = processUserInputAndCheckForExitRequest(userInput);
		}
		
		System.out.println("Auf Wiedersehen!");
	}
	
	private void readIataExchangeRates() throws IOException{
		//The IATA currency rates must be imported from the file.

		String path = "/home/obed/Documents/aiti_docs/java/JavaExam/test_codes_2/de/example/iata/KursExport.csv";
		
		File source = new File(path);
		String[] splitRow;

		try(BufferedReader reader = new BufferedReader(new FileReader(source));
		){
			String currentRow;
			while((currentRow = reader.readLine()) != null){
				splitRow =  currentRow.split(";");
				ArrayList<String> rowList = new ArrayList(Arrays.asList(splitRow));
				
				ArrayList<Object> cleanList = cleaner(rowList);
				//duplicationChecker(cleanList);
				csvData.add(cleanList);
			}
		}
	}

	//method to remove space and other unwanted charaters around the String in the list
	private ArrayList<Object> cleaner(ArrayList<String> rowList ){
		ArrayList<Object> cleanList = new ArrayList<>();

		//create variables to hold data from each csv row
		String country, ISOcode;
		Double rate;
		Date startDate, endDate;

		country = rowList.get(0);
		rate = Double.valueOf(((rowList.get(1)).trim()).replace(",","."));
		ISOcode = (rowList.get(2)).trim();
		startDate = dateConverter((rowList.get(3)).trim());
		endDate = dateConverter((rowList.get(4)).trim());

		cleanList.add(ISOcode);
		cleanList.add(rate);
		cleanList.add(startDate);
		cleanList.add(endDate);

		return cleanList;
	}
	
	private void displayMenu() {
		System.out.println("IATA exchange rate example");
		System.out.println();
		
		System.out.println("Select a function by selecting the number key and pressing'Enter': ");
		System.out.println("[1] Display exchange rate");
		System.out.println("[2] Enter new exchange rate");
		System.out.println("[0] Beenden or Exit" + "\n");
		System.out.print("Enter number here: ");
	}
	
	//Returns true when the user wants to exit the application 
	private boolean processUserInputAndCheckForExitRequest(String userInput) throws IOException{
		if(userInput.equals("0")) {
			return true;
		}
		
		if(userInput.equals("1")) {
			displayIataExchangeRate();
			
		} else if(userInput.equals("2")) {
			enterIataExchangeRate();
			
		} else {
			System.out.println(" Wrong input. \n Please try again.");
			displayMenu();
		}
		
		return false;
	}
	
	private void displayIataExchangeRate() throws IOException{
		String userISOCode = getUserInputForStringField("Enter Currency code");
		Date userDate = getUserInputForDateField("Enter Date");
		System.out.println();

		//TODO: Mit currencyIsoCode und date sollte hier der Kurs ermittelt und ausgegeben werden. 
		//TODO: With currencyIsoCode and date the exchange rate should be determined and output. 
		String storedISOCode;
		Date storedStartDate, storedEndDate;
		Double storedRate;
		int count = 0;
		hashSet.addAll(csvData); //hashset is used here to avoid duplication

		for(ArrayList currentList: hashSet){
			//hold the stored values in varibales 
			
			storedISOCode = String.valueOf(currentList.get(0));
			storedRate = (Double)currentList.get(1);
			storedStartDate = (Date)currentList.get(2);
			storedEndDate = (Date)currentList.get(3);
			if (userISOCode.equalsIgnoreCase(storedISOCode) && (isWithinRange(userDate, storedStartDate, storedEndDate))) {
				System.out.println("The exchange range: " + storedRate + " " + storedISOCode + " per Euro \n");
				count++;
				System.exit(0);
			}else{
				continue;
			}
		} 
		if (count == 0) {
			System.out.println("Sorry, we found no exchange rate records for the entry. Auf Wiedersehen! \n");
			System.exit(0);
		}
	}
	
	//Use Collection Set to temporarily hold rows from the CSV data that have same IOS code and to avoid duplication
	List<ArrayList> partData = new CopyOnWriteArrayList<ArrayList>();
	Set<ArrayList> hashSet = new HashSet<ArrayList>();
	ArrayList matchRowList = new ArrayList();

	private void enterIataExchangeRate() throws IOException{
		String userISOCode = getUserInputForStringField("Enter Currency code");
		Date userStartDate = getUserInputForDateField("Enter date From");
		Date userEndDate = getUserInputForDateField("Enter date To");
		Double userRate = getUserInputForDoubleField("Euro rate for 1 " + userISOCode);
		String storedISOCode;
		//TODO: Aus den Variablen muss jetzt ein Kurs zusammengesetzt und in die eingelesenen Kurse eingefuegt werden. 
		//TODO: A course must now be composed of the variables and inserted into the read-in courses.

		int insertCount = 0;
		int codeMatchCount = 0;

		for(ArrayList currentList: csvData){
			//hold the stored values in varibales 
			storedISOCode = String.valueOf(currentList.get(0));

			//1st check: ISO codes match, export only the exchange rows to a new arraylist for faster processing
			if (userISOCode.equalsIgnoreCase(storedISOCode)) {
				codeMatchCount++;
				boolean state = false;
				state = partData.add(currentList);// this inserts into a new set before the list is deleted from the main dataset
				if (state) {
					insertCount++;
				}
			}
		}

		removeDuplicateRowsFromPartData(userStartDate, userEndDate, userRate);
		removeDateOverlaps();

		//write data into original data list
		boolean state = csvData.addAll(hashSet);
		if (state) {
			System.out.println("Your new Exchnage rate has been added successfully!");
			System.exit(0);
		}else{
			partData.clear();
			System.out.println("Sorry, your new Exchnage rate could not be added. Please try again");
			displayMenu();
		}
	}

	//Write data into a HashSet to naturally remove duplicate entries
	private void removeDuplicateRowsFromPartData(Date userStartDate, Date userEndDate, Double userRate){
		String partISOCode;
		Date partStartDate, partEndDate;
		Double partRate;

		if (!partData.isEmpty()) {
			for (ArrayList partList : partData) {
				//hold the stored values in varibales 
				partISOCode = String.valueOf(partList.get(0));
				partRate = (Double)partList.get(1);
				partStartDate = (Date)partList.get(2);
				partEndDate = (Date)partList.get(3);

				if ( userStartDate.before(partStartDate) && userEndDate.after(partEndDate)) {
					partList.set(2, userStartDate);
					partList.set(3, userEndDate);
					partList.set(1, userRate);	

					matchRowList = partList;
				} 
				hashSet.add(partList); //hashset is used here to avoid duplication
			}	
		}
	}

	//update the dates to prevent overlap
	private void removeDateOverlaps(){
		
		for (ArrayList list : hashSet) {
			//hold the stored values in varibales 
			Date setListStart = (Date)list.get(2);
			Date setListEnd = (Date)list.get(3);

			Date matchListStart = (Date)matchRowList.get(2);
			Date matchListEnd = (Date)matchRowList.get(3);

			if (!(isWithinRange(setListStart, matchListStart, matchListEnd)) && (isWithinRange(setListEnd, matchListStart, matchListEnd))) {
				//set the end date of the set list to the previous day's date of the start date of matchlist
				list.set(3, getPreviousDayDate(matchListStart));
			}
			if ( (isWithinRange(setListStart, matchListStart, matchListEnd)) && !(isWithinRange(setListEnd, matchListStart, matchListEnd))) {
				//set the start date of the of the set list to the next day's date after the match rows date
				list.set(2, getNextDayDate(matchListEnd));
			}
		}
		//------------------------Uncomment this loop to see the result of the updates -------------------------- 
		// for (ArrayList list : hashSet) {
		// 	System.out.println(list);
		// }
	}
	
	private String getUserInputForStringField(String fieldName) throws IOException{
		System.out.print(fieldName + ": ");
		return getUserInput();
	}

	private String getUserInput() throws IOException{
		BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
		return (consoleInput.readLine()).trim();
	}
	
	private Date getUserInputForDateField(String fieldName) throws IOException{
		System.out.print(fieldName + " (tt.mm.jjjj): ");
		String dateString = getUserInput();
		return dateConverter(dateString);
	}
	
	private Double getUserInputForDoubleField(String fieldName) throws IOException{
		String doubleString = getUserInputForStringField(fieldName);
		return Double.valueOf(doubleString);
	}

	//method to convert String to date from csv file
	private Date dateConverter(String dateString){
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
        	System.out.println("\nWrong data format, please try again");
        	System.out.println("\n\n-------------New Attempt ----------------");
        	try{
        		run();
        	}catch(IOException ioe){
        		System.out.println(ioe.getMessage());
        	}
            //e.printStackTrace();
        }
        return date;
	}

	private Date getPreviousDayDate(Date date) {

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.DAY_OF_MONTH, -1);
	    return cal.getTime();
	}

	private Date getNextDayDate(Date date) {

	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(Calendar.DAY_OF_MONTH, 1);
	    return cal.getTime();
	}

	boolean isWithinRange(Date testDate, Date startDate, Date endDate) {
	   return !(testDate.before(startDate) || testDate.after(endDate));
	}

}
