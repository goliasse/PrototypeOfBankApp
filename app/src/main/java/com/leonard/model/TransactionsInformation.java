package com.leonard.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

public class TransactionsInformation {
	//Account glossary
	public HashMap<String, String> mAccountGlossary = null;

	//details of every transaction, including completed and pending transactions 
	public TreeMap<Date, ArrayList<HashMap<String, String>>> mTransactions = null;

	//information of the atms
	public HashMap<String, HashMap<String, String>> mATMS = null;
}
