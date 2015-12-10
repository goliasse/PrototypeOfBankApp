package com.leonard.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.leonard.model.TransactionType;
import com.leonard.model.TransactionsInformation;

public class JSONParser {
	public static class DateDescendingOrder implements Comparator<Date> {
		@Override
		public int compare(Date lhs, Date rhs) {
			return -lhs.compareTo(rhs);
		}
	}
	
	public TransactionsInformation parse(String jsonString, Comparator<Date> order){
		TransactionsInformation parsedData = new TransactionsInformation();
		
		try {
			JSONObject json = new JSONObject(jsonString);

			JSONObject accountObject = json.getJSONObject(Tags.ACCOUNT.JSON_OBJECT_NAME);
			parsedData.mAccountGlossary = parseAccountGlossary(accountObject);

			parsedData.mTransactions = new TreeMap<Date, ArrayList<HashMap<String, String>>>(order);
			
			JSONArray transactionsArray = json.getJSONArray(Tags.TRANSACTION.JSON_ARRAY_NAME);
			parseTransactions(parsedData.mTransactions, transactionsArray, TransactionType.TRANSACTION);

			JSONArray pendingArray = json.getJSONArray(Tags.PENDING.JSON_ARRAY_NAME);
			parseTransactions(parsedData.mTransactions, pendingArray, TransactionType.PENDING);

			JSONArray ATMArray = json.getJSONArray(Tags.ATM.JSON_ARRAY_NAME);
			parsedData.mATMS = parseAtmsArray(ATMArray);
		} catch (JSONException e) {
			e.printStackTrace();
			parsedData = null;
		}		
		
		return parsedData;
	}
	
	public HashMap<String, String> parseAccountGlossary(JSONObject accountObject) throws JSONException {
		HashMap<String, String> accountInformation = new HashMap<String, String>();

		String accountName = accountObject.getString(Tags.ACCOUNT.NAME);
		String accountNumber = accountObject.getString(Tags.ACCOUNT.NUMBER);
		String accountAvailable = accountObject.getString(Tags.ACCOUNT.AVAILABLE);
		String accountBalance = accountObject.getString(Tags.ACCOUNT.BALANCE);

		accountInformation.put(Tags.ACCOUNT.NAME, accountName);
		accountInformation.put(Tags.ACCOUNT.NUMBER, accountNumber);
		accountInformation.put(Tags.ACCOUNT.AVAILABLE, accountAvailable);
		accountInformation.put(Tags.ACCOUNT.BALANCE, accountBalance);

		return accountInformation;
	}
	
	public HashMap<String, HashMap<String, String>> parseAtmsArray(JSONArray atmsArray) throws JSONException {
		HashMap<String, HashMap<String, String>> atms = new HashMap<String, HashMap<String, String>>();

		int arrayLength = atmsArray.length();

		for (int i = 0; i < arrayLength; i++) {
			JSONObject atmObject = atmsArray.getJSONObject(i);

			insertAtmsObject(atms, atmObject);
		}

		return atms;
	}

	public void insertAtmsObject(HashMap<String, HashMap<String, String>> atmsHashMap, JSONObject atmObject) throws JSONException {
		String ID = atmObject.getString(Tags.ATM.ID);
		String name = atmObject.getString(Tags.ATM.NAME);
		String address = atmObject.getString(Tags.ATM.ADDRESS);

		JSONObject locationObj = atmObject.getJSONObject(Tags.ATM.LOCATION);
		String latitude = locationObj.getString(Tags.ATM.LATITUDE);
		String longitude = locationObj.getString(Tags.ATM.LONGITUDE);

		HashMap<String, String> atm = new HashMap<String, String>();
		atm.put(Tags.ATM.NAME, name);
		atm.put(Tags.ATM.ADDRESS, address);
		atm.put(Tags.ATM.LATITUDE, latitude);
		atm.put(Tags.ATM.LONGITUDE, longitude);

		atmsHashMap.put(ID, atm);
	}

	public void parseTransactions(TreeMap<Date, ArrayList<HashMap<String, String>>> treeMap, JSONArray transactionArray, TransactionType type) throws JSONException {
		int arrayLength = transactionArray.length();
		for (int i = 0; i < arrayLength; i++) {
			JSONObject transactionObject = transactionArray.getJSONObject(i);

			insertTransaction(treeMap, transactionObject, type);
		}
	}

	public void insertTransaction(TreeMap<Date, ArrayList<HashMap<String, String>>> treeMap, JSONObject transactionObject, TransactionType type) throws JSONException {
		HashMap<String, String> transaction = getTransaction(transactionObject);

		if (type == TransactionType.TRANSACTION) {
			transaction.put(Tags.TRANSACTION.TYPE, Tags.TYPE.TRANSACTION);
		} else if (type == TransactionType.PENDING) {
			transaction.put(Tags.TRANSACTION.TYPE, Tags.TYPE.PENDING);
		}

		String dateString = transaction.get(Tags.TRANSACTION.DATE);

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		boolean isContained = treeMap.containsKey(date);

		if (isContained) {
			ArrayList<HashMap<String, String>> dayTransactions = (ArrayList<HashMap<String, String>>) treeMap.get(date);
			dayTransactions.add(transaction);
		} else {
			ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
			list.add(transaction);

			treeMap.put(date, list);
		}
	}

	public HashMap<String, String> getTransaction(JSONObject transactionObject) throws JSONException {
		HashMap<String, String> transaction = new HashMap<String, String>();

		String id = transactionObject.getString(Tags.TRANSACTION.ID);
		String effectiveDate = transactionObject.getString(Tags.TRANSACTION.DATE);
		String description = transactionObject.getString(Tags.TRANSACTION.DESC);
		String amount = transactionObject.getString(Tags.TRANSACTION.AMOUNT);

		String atmId = null;

		try {
			atmId = transactionObject.getString(Tags.TRANSACTION.ATMID);
		} catch (JSONException e) {
		}

		transaction.put(Tags.TRANSACTION.ID, id);
		transaction.put(Tags.TRANSACTION.DATE, effectiveDate);
		transaction.put(Tags.TRANSACTION.DESC, description);
		transaction.put(Tags.TRANSACTION.AMOUNT, amount);

		if (atmId != null) {
			transaction.put(Tags.TRANSACTION.ATMID, atmId);
		}

		return transaction;
	}	
	
	
}
