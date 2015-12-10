package com.leonard.exercise;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

import com.leonard.json.JSONParser;
import com.leonard.json.JSONParser.DateDescendingOrder;
import com.leonard.json.Tags;
import com.leonard.model.TransactionsInformation;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
	private static TaskLoadJson mJsonLoader;

	private TextView mTextStatus;
	private ProgressBar mProgressBar;

	// transaction details
	private ExpandableListView mListView;
	private ListViewAdapter mListViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		setTitle(getString(R.string.title_details));
		mTextStatus = (TextView) findViewById(R.id.textStatus);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

		mListView = (ExpandableListView) findViewById(R.id.listViewTransactions);

		mListView.setOnGroupClickListener(this);
		mListView.setOnChildClickListener(this);

		mJsonLoader = new TaskLoadJson(this);
		mJsonLoader.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mJsonLoader != null) {
			mJsonLoader.cancel(true);
			mJsonLoader = null;
		}
	}

	protected enum TaskStatus {
		LOADING(1), LOADED_FAILURE(2), LOADED_DONE(3);
		private final int value;

		private TaskStatus(final int mode) {
			this.value = mode;
		}

		public int getValue() {
			return value;
		}
	}

	public void setStatus(TaskStatus value) {
		switch (value) {
		case LOADING:
			mProgressBar.setVisibility(View.VISIBLE);
			mTextStatus.setText(R.string.loading);
			mTextStatus.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			break;
		case LOADED_FAILURE:
			mProgressBar.setVisibility(View.GONE);
			mTextStatus.setText(R.string.load_failure);
			mTextStatus.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.GONE);
			break;
		case LOADED_DONE:
			mProgressBar.setVisibility(View.GONE);
			mTextStatus.setText(R.string.load_done);
			mTextStatus.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	public void setData(TransactionsInformation data) {
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "au"));

		mListViewAdapter = new ListViewAdapter(this, data.mTransactions, data.mATMS);

		View listViewHeader = getLayoutInflater().inflate(R.layout.list_header, null);
		mListView.addHeaderView(listViewHeader);

		TextView accountName = (TextView) listViewHeader.findViewById(R.id.textAccountName);
		TextView accountNumber = (TextView) listViewHeader.findViewById(R.id.textAccountNumber);
		TextView accountAvailableValue = (TextView) listViewHeader.findViewById(R.id.textAvailableFundsValue);
		TextView accountBalanceValue = (TextView) listViewHeader.findViewById(R.id.textAccountBalanceValue);

		// account glossary
		accountName.setText(data.mAccountGlossary.get(Tags.ACCOUNT.NAME));
		accountNumber.setText(data.mAccountGlossary.get(Tags.ACCOUNT.NUMBER));

		double availableValue = Double.valueOf(data.mAccountGlossary.get(Tags.ACCOUNT.AVAILABLE));
		accountAvailableValue.setText(currencyFormatter.format(availableValue));

		double balanceValue = Double.valueOf(data.mAccountGlossary.get(Tags.ACCOUNT.BALANCE));
		accountBalanceValue.setText(currencyFormatter.format(balanceValue));
		
		mListView.setAdapter(mListViewAdapter);

		for (int i = 0; i < mListViewAdapter.getGroupCount(); i++) {
			mListView.expandGroup(i);
		}

	}

	private static class TaskLoadJson extends AsyncTask<Void, Void, TransactionsInformation> {
		WeakReference<DetailsActivity> mContextRef;

		public TaskLoadJson(DetailsActivity context) {
			mContextRef = new WeakReference<DetailsActivity>(context);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			DetailsActivity activity = mContextRef.get();
			if (activity == null) {
				return;
			}

			activity.setStatus(TaskStatus.LOADING);
		}

		public String loadJSONFromAsset() {
			String json = null;

			try {
				DetailsActivity context = mContextRef.get();

				if (context == null) {
					return null;
				}

				InputStream is = context.getAssets().open("data/exercise.json");
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				json = new String(buffer, "UTF-8");
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;
			}

			return json;
		}

		@Override
		protected TransactionsInformation doInBackground(Void... params) {
			String jsonString = null;

			jsonString = loadJSONFromAsset();

			if (jsonString == null) {
				return null;
			}
			
			JSONParser parser = new JSONParser();
			
			DateDescendingOrder order = new DateDescendingOrder();
			TransactionsInformation parsedData = parser.parse(jsonString, order);

			return parsedData;
		}

		@Override
		protected void onPostExecute(TransactionsInformation result) {
			super.onPostExecute(result);

			DetailsActivity activity = mContextRef.get();
			if (activity == null) {
				return;
			}

			if (result == null) {
				activity.setStatus(TaskStatus.LOADED_FAILURE);
				return;
			} else {
				activity.setData(result);

				activity.setStatus(TaskStatus.LOADED_DONE);
			}

		}

	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		return true;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		HashMap<String, String> transaction = (HashMap<String, String>) mListViewAdapter.getChild(groupPosition, childPosition);

		String atmID = transaction.get(Tags.TRANSACTION.ATMID);

		if (atmID == null) {
			return true;
		}

		HashMap<String, String> AtmInfo = mListViewAdapter.getAtmInfo(atmID);
		if (AtmInfo == null) {
			return true;
		}
		
		String uriString = "geo:" + AtmInfo.get(Tags.ATM.LATITUDE) + "," + AtmInfo.get(Tags.ATM.LONGITUDE) + "?q=" + AtmInfo.get(Tags.ATM.LATITUDE) + "," + AtmInfo.get(Tags.ATM.LONGITUDE) + "(ABC+ATM)";
		Uri gmmIntentUri = Uri.parse(uriString);
		Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
		mapIntent.setPackage("com.google.android.apps.maps");
		if (mapIntent.resolveActivity(getPackageManager()) != null) {
			startActivity(mapIntent);
		}

		return true;
	}

}
