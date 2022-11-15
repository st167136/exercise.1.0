package de.example.iata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IataExchangeRateApplication {
	List<List<String>> exchangeRates = new ArrayList<>();
	Map<String, String> countries = new TreeMap<>();
	DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	WriteToCSVService writeToCSVService = new WriteToCSVService();
	static final String csvFilePath = "src/de/example/iata/KursExportNew.csv";
	
	public void run() throws Exception {
		readIataExchangeRates();
		
		displayMenu();
		
		boolean exitRequested = false;
		
		while(!exitRequested) {
			String userInput = getUserInput();
			
			exitRequested = processUserInputAndCheckForExitRequest(userInput);
		}
		
		System.out.println("Auf Wiedersehen!");
	}
	
	private void readIataExchangeRates() {
		try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath, java.nio.charset.StandardCharsets.UTF_8))) {
    		String line;
    		while ((line = br.readLine()) != null) {
        		String[] values = line.split(";");
        		exchangeRates.add(Arrays.asList(values));
				
    		}
			for(int i=0; i<exchangeRates.size(); i++) {
				String c_id = exchangeRates.get(i).get(2);
				String c_name = exchangeRates.get(i).get(0);
				if(!countries.containsKey(c_name)) {
					countries.put(c_name, c_id);
				}
			}
			
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println(countries);
	}
	
	private void displayMenu() {
		System.out.println("IATA Währungskurs-Beispiel");
		System.out.println();
		System.out.println("Wählen Sie eine Funktion durch Auswahl der Zifferntaste und Drücken von 'Return'");
		System.out.println("[1] Währungskurs anzeigen");
		System.out.println("[2] Neuen Währungskurs eingeben");
		System.out.println();
		System.out.println("[0] Beenden");
	}
	
	private String getUserInput() throws Exception {
		BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
		
		return consoleInput.readLine();
	}
	
	//Returns true when the user wants to exit the application 
	private boolean processUserInputAndCheckForExitRequest(String userInput) throws Exception {
		if(userInput.equals("0")) {
			return true;
		}
		
		if(userInput.equals("1")) {
			displayIataExchangeRate();
		} else if(userInput.equals("2")) {
			enterIataExchangeRate();
		} else {
			System.out.println("Falsche Eingabe. Versuchen Sie es bitte erneut.");
		}
		
		return false;
	}
	
	private void displayIataExchangeRate() throws Exception {
		String currencyIsoCode = getUserInputForStringField("Währung");
		Date date = getUserInputForDateField("Datum");
		boolean matchFound = false;
		for(int i=0; i<exchangeRates.size();i++) {
			if(exchangeRates.get(i).get(2).toString().equals(currencyIsoCode.toUpperCase())) {
				if(date.before(java.text.DateFormat.getDateInstance().parse(exchangeRates.get(i).get(4))) 
							&& date.after(java.text.DateFormat.getDateInstance().parse(exchangeRates.get(i).get(3)))
							|| date.equals(java.text.DateFormat.getDateInstance().parse(exchangeRates.get(i).get(3)))
							|| date.equals(java.text.DateFormat.getDateInstance().parse(exchangeRates.get(i).get(4)))) {
					System.out.println("1 Euro entspricht " + exchangeRates.get(i).get(1) + " " + currencyIsoCode.toUpperCase()+".");
					matchFound = true;
					return;
				}
			}
		}
		if(!matchFound) {
			System.out.println("Für diesen Zeitraum ist kein Wechselkurs für die gewünschte Währung vorhanden.");
		}
	}
	
	private void enterIataExchangeRate() throws Exception {
		exchangeRates = this.writeToCSVService.enterNewExchangeRate(exchangeRates, countries, dateFormat);
	}
	
	protected String getUserInputForStringField(String fieldName) throws Exception {
		System.out.print(fieldName + ": ");
		return getUserInput();
	}
	
	protected Date getUserInputForDateField(String fieldName) throws Exception {
		System.out.print(fieldName + " (tt.mm.jjjj): ");
		String dateString = getUserInput();
		long count = dateString.chars().filter(ch -> ch == '.').count();
		if(count != 2) {
			System.out.println("Bitte geben Sie ein valides Datum ein");
			return null;
		}
		Date res =  this.dateFormat.parse(dateString);
		return res;
	}
	
	protected Double getUserInputForDoubleField(String fieldName) throws Exception {
		String doubleString = getUserInputForStringField(fieldName);
		if(doubleString.indexOf(',') != -1) {
			doubleString = doubleString.replace(',', '.');
		 }
		return Double.valueOf(doubleString);
	}
}
