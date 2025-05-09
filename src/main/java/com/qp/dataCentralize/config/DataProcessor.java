package com.qp.dataCentralize.config;

import com.qp.dataCentralize.entity.Datas;
import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DataProcessor implements ItemProcessor<Datas, Datas> {

	private String category;
	private String enteredBy;

	public void setCategory(String category, String enteredBy) {
		this.category = category;
		this.enteredBy = enteredBy;
	}

	@Override
	public Datas process(Datas customer) throws Exception {
		customer.setCategory(category);
		customer.setEnteredBy(enteredBy);
		Instant now = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("UTC"));
		customer.setEntryDate(formatter.format(now));
		return customer;
	}
}
