package de.example.iata;

import java.util.Date;

public class ExcangeRate {

    String isoCode;
    double exchangeRate;
    Date dateFrom;
    Date dateTo;
    
    public ExcangeRate(String isoCode, double exchangeRate, Date dateFrom, Date dateTo) {
        this.isoCode = isoCode;
        this.exchangeRate = exchangeRate;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }


    //ToDo: Generate Getters and Setters
}
