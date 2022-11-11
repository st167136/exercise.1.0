package de.example.iata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IataExchangeRateApplication {
	List<List<String>> exchangeRates = new ArrayList<>();
	Map<String, String> countries = new TreeMap<>();
	DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	
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
		//TODO: Hier muss das Einlesen der IATA-Währungskurse aus der Datei geschehen.	--Done
		try (BufferedReader br = new BufferedReader(new FileReader("src/de/example/iata/KursExport.csv"))) {
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
					System.out.println("1 " + currencyIsoCode.toUpperCase() + " entspricht " + exchangeRates.get(i).get(1) + " Euro.");
					matchFound = true;
					return;
				}
			}
		}
		if(!matchFound) {
			System.out.println("Für diesen Zeitraum ist kein Wechselkurs für die gewünschte Währung vorhanden.");
		}

		//TODO: Mit currencyIsoCode und date sollte hier der Kurs ermittelt und ausgegeben werden.  -- Done 
	}
	
	private void enterIataExchangeRate() throws Exception {
		boolean validInput = false;
		while(!validInput) {
			String currencyIsoCode = getUserInputForStringField("Währung");
			if(!countries.containsValue(currencyIsoCode.toUpperCase())) {
				System.out.println("Diese Währung ist im System nicht hinterlegt.");
				continue;
			}
			List<String> allCountriesWithCurrency = new ArrayList<>();
			for(var entry : countries.entrySet()) {
				if(entry.getValue().equals(currencyIsoCode.toUpperCase())) {
					allCountriesWithCurrency.add(entry.getKey());
				}
			}
			Date from = getUserInputForDateField("Von");
			if(from == null) {
				continue;
			} else if(from.after(new Date())) {
				System.out.println("Dieses Datum liegt in der Zukunft. Bitte geben Sie ein valides Datum ein");
				continue;
			}
			Date to = getUserInputForDateField("Bis");
			if(to == null) {
				continue;
			} else if(to.before(from)) {
				System.out.println("Dieses Datum liegt vor dem Anfangsdatum. Bitte geben Sie ein Datum ein, welches nach dem Anfangsdatum liegt.");
				continue;
			}
			Double exchangeRate = getUserInputForDoubleField("Euro-Kurs für 1 " + currencyIsoCode);
			for(int i=0;i<allCountriesWithCurrency.size();i++) {
				List<String> rate = new ArrayList<>();
				rate.add(allCountriesWithCurrency.get(i));
				rate.add(exchangeRate.toString().replace('.', ','));
				rate.add(currencyIsoCode.toUpperCase());
				rate.add(this.dateFormat.format(from));
				rate.add(this.dateFormat.format(to));
				rate.add("");
				rate.add("");

				exchangeRates.add(rate);
				writeToCSV(rate);

				System.out.println("Neuen Währungskurs hinzugefügt: "+rate);
			}
			validInput = true;
		}
		
		
		
		//TODO: Aus den Variablen muss jetzt ein Kurs zusammengesetzt und in die eingelesenen Kurse eingefügt werden. 	--Done
	}

	private void writeToCSV(List<String> rate) throws IOException {
		String[] data = rate.toArray(new String[0]);
		List<String[]> newRate = new ArrayList<>();
		newRate.add(data);
		File csvOutputFile = new File("src/de/example/iata/KursExport.csv");
    	try (PrintWriter pw = new PrintWriter(new FileOutputStream(csvOutputFile, true))) {
        	newRate.stream()
          	.map(this::convertToCSV)
          	.forEach(pw::println);
    	}
	}

	private String convertToCSV(String[] rate) {
		return Stream.of(rate).map(this::escapeSpecialCharacters).collect(Collectors.joining(";"));
	}

	private String escapeSpecialCharacters(String data) {
		String escapedData = data.replaceAll("\\R", " ");
		if (data.contains(";") || data.contains("\"") || data.contains("'")) {
			data = data.replace("\"", "\"\"");
			escapedData = "\"" + data + "\"";
		}
		return escapedData;
	}
	
	private String getUserInputForStringField(String fieldName) throws Exception {
		System.out.print(fieldName + ": ");
		return getUserInput();
	}
	
	private Date getUserInputForDateField(String fieldName) throws Exception {
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
	
	private Double getUserInputForDoubleField(String fieldName) throws Exception {
		String doubleString = getUserInputForStringField(fieldName);
		if(doubleString.indexOf(',') != -1) {
			doubleString = doubleString.replace(',', '.');
		 }
		return Double.valueOf(doubleString);
	}
}
