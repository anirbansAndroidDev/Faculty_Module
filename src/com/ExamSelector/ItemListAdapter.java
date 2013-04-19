package com.ExamSelector;

import ica.ProfileInfo.QuestionDetails;

import java.util.List;

import com.FacultyModule.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ItemListAdapter extends ArrayAdapter<QuestionDetails> {

	private LayoutInflater li;

	/**
	 * Constructor from a list of items
	 */
	public ItemListAdapter(Context context, List<QuestionDetails> items) {
		super(context, 0, items);
		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// This is how you would determine if this particular item is checked
		// when the view gets created
		// --
		// final ListView lv = (ListView) parent;
		// final boolean isChecked = lv.isItemChecked(position);

		// The item we want to get the view for
		// --
		final QuestionDetails item = getItem(position);

		// Re-use the view if possible
		// --
		View v = convertView;
		if (v == null) {
			v = li.inflate(R.layout.questionitem, null);
		}

		// Set some view properties (We should use the view holder pattern in
		// order to avoid all the findViewById and thus improve performance)
		// --
		final TextView idView = (TextView) v.findViewById(R.id.itemId);
		if (idView != null) {
			
			int idxNum=position+1;
			idView.setText(Integer.toString(idxNum)+".");
		}

		final TextView captionView = (TextView) v
				.findViewById(R.id.itemCaption);
		if (captionView != null) {
			captionView.setText(item.getText());
		}

		return v;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
}
