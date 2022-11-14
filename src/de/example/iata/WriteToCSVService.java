package de.example.iata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteToCSVService {

	protected List<List<String>> enterNewExchangeRate(List<List<String>> exchangeRates, Map<String, String> countries, DateFormat dateFormat) throws Exception {
		IataExchangeRateApplication iataExchangeRateApplication = new IataExchangeRateApplication();
		boolean validInput = false;
		while(!validInput) {
			String currencyIsoCode = iataExchangeRateApplication.getUserInputForStringField("Währung");
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
			Date from = iataExchangeRateApplication.getUserInputForDateField("Von");
			if(from == null) {
				continue;
			} else if(from.after(new Date())) {
				System.out.println("Dieses Datum liegt in der Zukunft. Bitte geben Sie ein valides Datum ein");
				continue;
			}
			Date to = iataExchangeRateApplication.getUserInputForDateField("Bis");
			if(to == null) {
				continue;
			} else if(to.before(from)) {
				System.out.println("Dieses Datum liegt vor dem Anfangsdatum. Bitte geben Sie ein Datum ein, welches nach dem Anfangsdatum liegt.");
				continue;
			}
			Double exchangeRate = iataExchangeRateApplication.getUserInputForDoubleField("Euro-Kurs für 1 " + currencyIsoCode);
			for(int i=0;i<allCountriesWithCurrency.size();i++) {
				List<String> rate = new ArrayList<>();
				rate.add(allCountriesWithCurrency.get(i));
				rate.add(exchangeRate.toString().replace('.', ','));
				rate.add(currencyIsoCode.toUpperCase());
				rate.add(dateFormat.format(from));
				rate.add(dateFormat.format(to));
				rate.add("");
				rate.add("");

				exchangeRates.add(rate);
				writeToCSV(rate);

				System.out.println("Neuen Währungskurs hinzugefügt: "+rate);
			}
			validInput = true;
		}
		return exchangeRates;
	}

    public void writeToCSV(List<String> rate) throws IOException {
		String[] data = rate.toArray(new String[0]);
		List<String[]> newRate = new ArrayList<>();
		newRate.add(data);
		File csvOutputFile = new File("src/de/example/iata/KursExport.csv");
    	try (PrintWriter pw = new PrintWriter(new FileOutputStream(csvOutputFile, true), true, java.nio.charset.StandardCharsets.UTF_8)) {
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
}
