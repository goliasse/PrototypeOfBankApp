package com.leonard.exercise;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.leonard.json.Tags;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ListViewAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private TreeMap<Date,ArrayList<HashMap<String, String>>> mData;
	private HashMap<String, HashMap<String, String>> mAtms;
	
	private ArrayList<Date> mDateList;
	private Date mToday = new Date();
	
	private NumberFormat mCurrencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "au"));
	SimpleDateFormat mDateFormat = new SimpleDateFormat("dd MMM yyyy");
	
	ListViewAdapter(Context context, TreeMap<Date,ArrayList<HashMap<String, String>>> transactionData, HashMap<String, HashMap<String, String>> atmsHashMap){
		mContext = context;
		mData    = transactionData;
		mAtms    = atmsHashMap;
		
		mDateList = new ArrayList<Date>();
		mDateList.addAll(mData.keySet());
	}
	
	public HashMap<String, String> getAtmInfo(String AtmID){
		if(mAtms == null) {
			return null;
		}
		
		HashMap<String, String> AtmInfo = mAtms.get(AtmID);
		
		return AtmInfo;
	}
	
	@Override
	public int getGroupCount() {
		int count = mData.size();
		return count;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		Date key = mDateList.get(groupPosition);
		int count = mData.get(key).size();
		return count;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mDateList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		Date key = mDateList.get(groupPosition);
		return mData.get(key).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition * 10000 + childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
	
	private static class GroupViewHolder {
		TextView mDate;
		TextView mDaysAgo;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Date date = (Date)getGroup(groupPosition);
		
		GroupViewHolder holder;

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_group, null);
			
			holder = new GroupViewHolder();
			holder.mDate = (TextView) convertView.findViewById(R.id.textDate);
			holder.mDaysAgo = (TextView) convertView.findViewById(R.id.textDaysAgo);
			
			convertView.setTag(holder);
		}else {
			holder = (GroupViewHolder)convertView.getTag();
		}
		
		String dateString = mDateFormat.format(date).toUpperCase();
		holder.mDate.setText(dateString);
		
		long diff = getDifferenceDays(date, mToday);
		String daysAgo = diff + " " + mContext.getString(R.string.days_ago);
		holder.mDaysAgo.setText(daysAgo);
		
		return convertView;
	}
	
	public static long getDifferenceDays(Date d1, Date d2) {
	    long diff = d2.getTime() - d1.getTime();
	    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	private static class ChildViewHolder {
		TextView mDescription;
		TextView mAmount;
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		HashMap<String, String> transaction = (HashMap<String, String>)getChild(groupPosition, childPosition);
		
		ChildViewHolder holder;

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_child, null);
			
			holder = new ChildViewHolder();
			holder.mDescription = (TextView) convertView.findViewById(R.id.textDescription);
			holder.mAmount = (TextView) convertView.findViewById(R.id.textAmount);
			
			convertView.setTag(holder);
		}else {
			holder = (ChildViewHolder)convertView.getTag();
		}
		
		String description = transaction.get(Tags.TRANSACTION.DESC);
		
		String type = transaction.get(Tags.TRANSACTION.TYPE);
		if(type.equals(Tags.TYPE.PENDING)){
			description = "<b>" + mContext.getString(R.string.pending) + ": </b>" + description;
		}
		
		holder.mDescription.setText(Html.fromHtml(description));
		
		double amountValue = Double.valueOf(transaction.get(Tags.TRANSACTION.AMOUNT));
		String amount = mCurrencyFormatter.format(amountValue);
		
		holder.mAmount.setText(amount);
		
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
