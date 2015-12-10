package com.leonard.json;

public class Tags {
	// account glossary
	public static class ACCOUNT {
		public final static String JSON_OBJECT_NAME = "account";
		public final static String NAME = "accountName";
		public final static String NUMBER = "accountNumber";
		public final static String AVAILABLE = "available";
		public final static String BALANCE = "balance";
	}

	// transactions
	public static class TRANSACTION {
		public final static String JSON_ARRAY_NAME = "transactions";
		public final static String TYPE = "type";
		public final static String ID = "id";
		public final static String DATE = "effectiveDate";
		public final static String DESC = "description";
		public final static String AMOUNT = "amount";
		public final static String ATMID = "atmId";
	}
	
	// pending transactions array
	public static class PENDING {
		public final static String JSON_ARRAY_NAME = "pending";
	}

	// transaction type
	public static class TYPE {
		public final static String TRANSACTION = "TRANSACTION";
		public final static String PENDING = "PENDING";
	}

	// ATM information
	public static class ATM {
		public final static String JSON_ARRAY_NAME = "atms";
		public final static String ID = "id";
		public final static String NAME = "name";
		public final static String ADDRESS = "address";
		public final static String LOCATION = "location";
		public final static String LATITUDE = "lat";
		public final static String LONGITUDE = "lng";
	}
}
