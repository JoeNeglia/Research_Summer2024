package com.studypartner;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
	@Test
	public void useAppContext() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
		assertEquals("com.studypartner", appContext.getPackageName());
	}
}

package com.studypartner;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() {
		assertEquals(4, 2 + 2);
	}
}

package com.studypartner.fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.ReminderAdapter;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.ReminderAlertReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class ReminderFragment extends Fragment implements ReminderAdapter.ReminderItemClickListener {
	
	private LinearLayout mEmptyLayout;
	private FloatingActionButton mfab;
	private RecyclerView mRecyclerView;
	private ArrayList<ReminderItem> mReminderList;
	private ReminderAdapter reminderAdapter;
	
	public ReminderFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
		
		final MainActivity activity = (MainActivity) requireActivity();
		mfab = rootView.findViewById(R.id.reminderFab);
		activity.fab.hide();
		activity.mBottomAppBar.performHide();
		activity.mBottomAppBar.setVisibility(View.GONE);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				activity.mNavController.navigate(R.id.action_nav_reminder_to_nav_home);
			}
		});
		
		mEmptyLayout = rootView.findViewById(R.id.reminderEmptyLayout);
		mRecyclerView = rootView.findViewById(R.id.recyclerview);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		
		mfab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.mNavController.navigate(R.id.reminderDialogFragment);
				mfab.setVisibility(View.GONE);
			}
		});
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Calendar calendar = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		
		for (int position = 0; position < mReminderList.size(); position++) {
			
			ReminderItem item = mReminderList.get(position);
			
			int year = Integer.parseInt(item.getDate().substring(6));
			int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
			int day = Integer.parseInt(item.getDate().substring(0, 2));
			int hour = Integer.parseInt(item.getTime().substring(0, 2));
			int minute = Integer.parseInt(item.getTime().substring(3, 5)) - 1;
			String amOrPm = item.getTime().substring(6);
			if (amOrPm.equals("PM") && hour != 12)
				hour = hour + 12;
			calendar.set(year, month, day, hour, minute);
			if (calendar.compareTo(today) < 0) {
				mReminderList.get(position).setActive(false);
				reminderAdapter.notifyItemChanged(position);
			}
		}
	}
	
	private void populateDataAndSetAdapter() {
		
		SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		Gson gson = new Gson();
		SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();
		
		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {
			}.getType();
			mReminderList = gson.fromJson(json, type);
		} else {
			mReminderList = new ArrayList<>();
		}
		
		Calendar calendar = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		
		for (int position = 0; position < mReminderList.size(); position++) {
			
			ReminderItem item = mReminderList.get(position);
			
			int year = Integer.parseInt(item.getDate().substring(6));
			int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
			int day = Integer.parseInt(item.getDate().substring(0, 2));
			int hour = Integer.parseInt(item.getTime().substring(0, 2));
			int minute = Integer.parseInt(item.getTime().substring(3, 5)) - 1;
			String amOrPm = item.getTime().substring(6);
			if (amOrPm.equals("PM") && hour != 12)
				hour = hour + 12;
			calendar.set(year, month, day, hour, minute);
			if (calendar.compareTo(today) < 0) {
				mReminderList.get(position).setActive(false);
			}
		}
		
		if (mReminderList.size() == 0) {
			reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", false);
			mEmptyLayout.setVisibility(View.VISIBLE);
		} else {
			mEmptyLayout.setVisibility(View.GONE);
		}
		
		String json = gson.toJson(mReminderList);
		
		reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
		reminderPreferenceEditor.apply();
		
		reminderAdapter = new ReminderAdapter(getContext(), mReminderList, this);
		
		mRecyclerView.setAdapter(reminderAdapter);
	}
	
	private void deleteReminder(final int position) {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				getContext());
		View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

		Button yesButton=view.findViewById(R.id.yes_button);
		Button noButton=view.findViewById(R.id.no_button);
		TextView title = view.findViewById(R.id.title_dialog);
		TextView detail = view.findViewById(R.id.detail_dialog);
		title.setText("Delete Reminder");
		detail.setText("Are you sure you want to remove the reminder");

		builder.setView(view);

		final AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(true);

		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent(requireContext(), ReminderAlertReceiver.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("BUNDLE_REMINDER_ITEM", mReminderList.get(position));
				intent.putExtra("EXTRA_REMINDER_ITEM", bundle);
				
				PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				alarmManager.cancel(pendingIntent);
				mReminderList.remove(position);
				reminderAdapter.notifyItemRemoved(position);
				
				SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
				SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();
				Gson gson = new Gson();
				
				String json = gson.toJson(mReminderList);
				
				if (mReminderList.size() == 0) {
					reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", false);
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				
				reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
				reminderPreferenceEditor.apply();

				dialog.dismiss();
			}
		});
		
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	private void editReminder(int position) {
		
		Bundle bundle = new Bundle();
		bundle.putString("REMINDER_POSITION", String.valueOf(position));
		((MainActivity) requireActivity()).mNavController.navigate(R.id.reminderDialogFragment, bundle);
		mfab.setVisibility(View.GONE);
		
	}
	
	@Override
	public void onClick(int position) {
		editReminder(position);
	}
	
	@Override
	public void deleteView(int adapterPosition) {
		deleteReminder(adapterPosition);
	}
}

package com.studypartner.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class NotesFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "NotesFragment";
	
	private final String SORT_BY_NAME = "By Name";
	private final String SORT_BY_SIZE = "By Size";
	private final String SORT_BY_CREATION_TIME = "By Creation Time";
	private final String SORT_BY_MODIFIED_TIME = "By Modification Time";
	
	private final String ASCENDING_ORDER = "Ascending Order";
	private final String DESCENDING_ORDER = "Descending Order";
	
	private String sortBy;
	private String sortOrder;
	
	private LinearLayout mEmptyLayout;
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mNotesAdapter;
	
	private MainActivity activity;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	
	private InterstitialAd mInterstitialAd;
	
	public NotesFragment() {
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				populateDataAndSetAdapter();
				addFolder();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
				builder.setTitle("Read and Write Permissions");
				builder.setMessage("Read and write permissions are required to store notes in the app");
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "onClick: closing app");
					}
				});
				builder.show();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		SharedPreferences.Editor editor = sortPreferences.edit();
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		} else {
			editor.putBoolean("SORTING_ORDER_EXISTS", true);
			editor.putString("SORTING_BY", SORT_BY_NAME);
			editor.putString("SORTING_ORDER", ASCENDING_ORDER);
			editor.apply();
			sortBy = SORT_BY_NAME;
			sortOrder = ASCENDING_ORDER;
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_notes, container, false);
		
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		activity = (MainActivity) requireActivity();
		
		if (firebaseUser != null) {
			File studyPartnerFolder = new File(String.valueOf(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(requireContext().getExternalFilesDir(null)).getParentFile()).getParentFile()).getParentFile()).getParentFile()), "StudyPartner");
			if (!studyPartnerFolder.exists()) {
				if (studyPartnerFolder.mkdirs()) {
					noteFolder = new File(studyPartnerFolder, firebaseUser.getUid());
				} else {
					noteFolder = new File(requireContext().getExternalFilesDir(null), firebaseUser.getUid());
				}
			} else {
				noteFolder = new File(studyPartnerFolder, firebaseUser.getUid());
			}
		} else {
			FirebaseAuth.getInstance().signOut();
			
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id))
					.requestEmail()
					.build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
			googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						activity.mNavController.navigate(R.id.nav_logout);
						activity.finishAffinity();
						activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					} else {
						activity.finishAffinity();
					}
				}
			});
		}
		
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				fab.setOnClickListener(null);
				activity.mNavController.navigate(R.id.action_nav_notes_to_nav_home);
			}
		});
		
		fab = activity.fab;
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: fab onclick called");
				addFolder();
			}
		});
		
		mEmptyLayout = rootView.findViewById(R.id.notesEmptyLayout);
		recyclerView = rootView.findViewById(R.id.notesRecyclerView);
		mLinearLayout = rootView.findViewById(R.id.notesLinearLayout);
		sortText = rootView.findViewById(R.id.notesSortText);
		sortOrderButton = rootView.findViewById(R.id.notesSortOrder);
		sortByButton = rootView.findViewById(R.id.notesSortButton);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		sortOrderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sortOrder.equals(ASCENDING_ORDER)) {
					sortOrder = DESCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
				} else {
					sortOrder = ASCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
				}
				editor.putString("SORTING_ORDER", sortOrder).apply();
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			}
		});
		
		sortByButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
				
				View dialogView = getLayoutInflater().inflate(R.layout.notes_sort_layout, null);
				
				Button okButton = dialogView.findViewById(R.id.sortByOkButton);
				Button cancelButton = dialogView.findViewById(R.id.sortByCancelButton);
				final RadioGroup radioGroup = dialogView.findViewById(R.id.sortByRadioGroup);
				radioGroup.clearCheck();
				
				switch (sortBy) {
					case SORT_BY_SIZE:
						radioGroup.check(R.id.sortBySizeRB);
						break;
					case SORT_BY_CREATION_TIME:
						radioGroup.check(R.id.sortByCreationTimeRB);
						break;
					case SORT_BY_MODIFIED_TIME:
						radioGroup.check(R.id.sortByModifiedTimeRB);
						break;
					default:
						radioGroup.check(R.id.sortByNameRB);
						break;
				}
				
				cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "onClick: cancel pressed while changing subject");
						builder.dismiss();
					}
				});
				
				okButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (radioGroup.getCheckedRadioButtonId()) {
							case R.id.sortBySizeRB:
								sortBy = SORT_BY_SIZE;
								break;
							case R.id.sortByCreationTimeRB:
								sortBy = SORT_BY_CREATION_TIME;
								break;
							case R.id.sortByModifiedTimeRB:
								sortBy = SORT_BY_MODIFIED_TIME;
								break;
							default:
								sortBy = SORT_BY_NAME;
								break;
						}
						sortText.setText(sortBy);
						editor.putString("SORTING_BY", sortBy).apply();
						sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
						builder.dismiss();
					}
				});
				
				builder.setView(dialogView);
				builder.show();
			}
		});
		
		mInterstitialAd = new InterstitialAd(requireContext());
		mInterstitialAd.setAdUnitId(getString(R.string.notes_interstitial_ad));
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				mInterstitialAd.loadAd(new AdRequest.Builder().build());
			}
		});
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE);
		
		if (sharedPreferences.getBoolean("NotesSearchExists", false)) {
			File searchedFile = new File(sharedPreferences.getString("NotesSearch", null));
			FileItem fileDesc = new FileItem(searchedFile.getPath());
			if (searchedFile.isDirectory()) {
				Bundle bundle = new Bundle();
				bundle.putString("FilePath", fileDesc.getPath());
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_fileFragment, bundle);
			}
		}
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		}
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		setHasOptionsMenu(true);
		activity.mBottomAppBar.performShow();
		activity.mBottomAppBar.setVisibility(View.VISIBLE);
		activity.mBottomAppBar.bringToFront();
		activity.fab.show();
		activity.fab.bringToFront();
	}
	
	@Override
	public void onPause() {
		if (actionMode != null) {
			actionMode.finish();
		}
		fab.setEnabled(true);
		Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
		setHasOptionsMenu(false);
		super.onPause();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notes_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: starts");
		switch (item.getItemId()) {
			case R.id.notes_menu_refresh:
				populateDataAndSetAdapter();
				return true;
			case R.id.notes_menu_search:
				Bundle bundle = new Bundle();
				FileItem[] files = new FileItem[notes.size()];
				files = notes.toArray(files);
				bundle.putParcelableArray("NotesArray", files);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_notesSearchFragment, bundle);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_notes_to_fileFragment, bundle);
		}
	}
	
	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}
	
	@Override
	public void onOptionsClick(View view, final int position) {

		PopupMenu popup = new PopupMenu(getContext(), view);
		if (starredIndex(position) != -1) {
			popup.inflate(R.menu.notes_item_menu_unstar);
		} else {
			popup.inflate(R.menu.notes_item_menu_star);
		}
		
		popup.getMenu().removeItem(R.id.notes_item_share);
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					case R.id.notes_item_rename:

						final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						final View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
						alertDialog.setView(dialogView);

						Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
						Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
						final TextView titleDialog = dialogView.findViewById(R.id.notesAddLinkTitle);
						titleDialog.setText("New Name");

						final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);

						final FileItem fileItem = notes.get(position);

						if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
							nameTextInput.getEditText().setText(fileItem.getName());
						}

						okButton.setOnClickListener( new View.OnClickListener() {
							public void onClick(View view) {

								if (mInterstitialAd.isLoaded()) {
									mInterstitialAd.show();
								}

								String newName = nameTextInput.getEditText().getText().toString().trim();
								File oldFile = new File(fileItem.getPath());
								File newFile = new File(noteFolder, newName);
								if (newName.equals(fileItem.getName()) || newName.equals("")) {
									Log.d(TAG, "onClick: filename not changed");
								} else if (newFile.exists()) {
									StyleableToast.makeText(getContext(), "Folder with this name already exists", Toast.LENGTH_SHORT, R.style.designedToast).show();
								} else if (newName.contains("/")) {
									StyleableToast.makeText(getContext(), "Folder name is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
								} else {
									if (oldFile.renameTo(newFile)) {
										StyleableToast.makeText(getContext(), "Folder renamed successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
										notes.get(position).setName(newName);
										notes.get(position).setPath(newFile.getPath());

										int starredIndex = starredIndex(position);
										if (starredIndex != -1) {
											SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
											SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

											starred.get(starredIndex).setName(newName);

											Gson gson = new Gson();
											starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
											starredPreferenceEditor.apply();
										}

										for (FileItem starItem : starred) {
											if (starItem.getPath().contains(oldFile.getPath())) {
												starItem.setPath(starItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
											}
										}
										for (FileItem linkItem : links) {
											if (linkItem.getPath().contains(oldFile.getPath())) {
												linkItem.setPath(linkItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
											}
										}

										SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
										SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

										Gson gson = new Gson();
										linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
										linkPreferenceEditor.apply();

										SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
										SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

										starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
										starredPreferenceEditor.apply();

										mNotesAdapter.notifyItemChanged(position);
										sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
									} else {
										StyleableToast.makeText(getContext(), "Folder could not be renamed", Toast.LENGTH_SHORT, R.style.designedToast).show();
									}
								}
								alertDialog.dismiss();
							}
						});

						cancelButton.setOnClickListener( new View.OnClickListener() {
							public void onClick(View view) {
								alertDialog.cancel();
							}
						});

						alertDialog.show();
						return true;

					case R.id.notes_item_star:

						addToStarred( position );
						return true;

					case R.id.notes_item_unstar:

						int unstarredIndex = starredIndex(position);
						if (unstarredIndex != -1) {
							SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
							SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

							starred.remove(unstarredIndex);

							notes.get(position).setStarred(false);
							if (starred.size() == 0) {
								starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
							}
							Gson gson = new Gson();
							starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
							starredPreferenceEditor.apply();
							mNotesAdapter.notifyItemChanged(position);
						} else {
							StyleableToast.makeText(activity, "You are some sort of wizard aren't you", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}

						return true;

					case R.id.notes_item_delete:

						AlertDialog.Builder builder = new AlertDialog.Builder(
								getContext());
						View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

						Button yesButton=view.findViewById(R.id.yes_button);
						Button noButton=view.findViewById(R.id.no_button);
						TextView title = view.findViewById(R.id.title_dialog);
						TextView detail = view.findViewById(R.id.detail_dialog);
						title.setText("Delete Folder");
						detail.setText("Are you sure you want to delete the folder?");

						builder.setView(view);

						final AlertDialog dialog= builder.create();
						dialog.setCanceledOnTouchOutside(true);

						yesButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								dialog.dismiss();

								// Hiding the folder to be deleted from RecyclerView
								recyclerView.findViewHolderForAdapterPosition(position).itemView.
										findViewById(R.id.noteItemLayout).setVisibility(View.GONE);

								// Displaying a Snackbar to allow user to UNDO the delete folder action
								Snackbar undoSnackbar = Snackbar.make(recyclerView, R.string.notes_action_deleted, Snackbar.LENGTH_LONG);
								undoSnackbar.setAction(R.string.notes_action_undo, new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										// Making folder visible to user again
										recyclerView.findViewHolderForAdapterPosition(position).itemView.
												findViewById(R.id.noteItemLayout).setVisibility(View.VISIBLE);
									}

								}).setDuration(3000).setActionTextColor(getResources().getColor(R.color.colorPrimary));

								undoSnackbar.addCallback(new Snackbar.Callback() {

									// Called when the Snackbar is dismissed by an event other than
									// clicking of UNDO.
									@Override
									public void onDismissed(Snackbar snackbar, int event) {
										if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
											event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
											event == Snackbar.Callback.DISMISS_EVENT_MANUAL) {

											if (mInterstitialAd.isLoaded()) {
												mInterstitialAd.show();
											}

											File file = new File(notes.get(position).getPath());
											deleteRecursive(file);

											if (notes.get(position).getType() == FileType.FILE_TYPE_FOLDER) {
												ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
												for (FileItem linkItem : links) {
													if (linkItem.getPath().contains(file.getPath())) {
														linksToBeRemoved.add(linkItem);
													}
												}
												links.removeAll(linksToBeRemoved);

												SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
												SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

												if (links.size() == 0) {
													linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
												}
												Gson gson = new Gson();
												linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
												linkPreferenceEditor.apply();

												ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
												for (FileItem starItem : starred) {
													if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
														starredToBeRemoved.add(starItem);
													}
												}
												starred.removeAll(starredToBeRemoved);

												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

												if (starred.size() == 0) {
													starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
												}
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}

											int starPosition = starredIndex(position);
											if (starPosition != -1) {

												starred.remove(starPosition);

												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

												if (starred.size() == 0) {
													starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
												}
												Gson gson = new Gson();
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											activity.mBottomAppBar.performShow();
											mNotesAdapter.notifyItemRemoved(position);
											notes.remove(position);

											if (notes.isEmpty()) {
												mEmptyLayout.setVisibility(View.VISIBLE);
											}
										}
									}
								});
								undoSnackbar.show(); // Snackbar will appear for 3 seconds
							}
						});
						noButton.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								dialog.dismiss();
							}
						});

						dialog.show();
						return true;
					
					default:
						return false;
				}
			}
		});
		popup.show();
	}
	
	private void populateDataAndSetAdapter() {
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files) {
				FileItem item = new FileItem(f.getPath());
				if (isStarred(item)) {
					item.setStarred(true);
				}
				notes.add(item);
			}
		}
		
		if (notes.isEmpty()) {
			mEmptyLayout.setVisibility(View.VISIBLE);
		} else {
			mEmptyLayout.setVisibility(View.INVISIBLE);
		}
		
		mNotesAdapter = new NotesAdapter(requireActivity(), notes, this, true);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	private int starredIndex(int position) {
		int index = -1;
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(notes.get(position).getPath())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	private boolean isStarred(FileItem item) {
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(item.getPath())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isExternalStorageReadableWritable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	private boolean writeReadPermission() {
		if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
			return false;
		} else {
			return true;
		}
	}
	
	private void addFolder() {
		if (isExternalStorageReadableWritable()) {
			if (writeReadPermission()) {
				final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
				final View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
				alertDialog.setView(dialogView);

				Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
				Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
				final TextView title = dialogView.findViewById(R.id.notesAddLinkTitle);
				title.setText("Name of the folder");

				final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);
				
				okButton.setOnClickListener( new View.OnClickListener() {
					public void onClick(View view) {
						String newName = nameTextInput.getEditText().getText().toString().trim();
						File newFolder = new File(noteFolder, newName);
						if (newName.isEmpty()) {
							StyleableToast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT, R.style.designedToast).show();
						} else if (newFolder.exists()) {
							StyleableToast.makeText(getContext(), "Folder with this name already exists", Toast.LENGTH_SHORT, R.style.designedToast).show();
						} else if (newName.contains("/")) {
							StyleableToast.makeText(getContext(), "Folder name is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
						} else {
							if (newFolder.mkdirs()) {
								notes.add(new FileItem(newFolder.getPath()));
								
								if (mEmptyLayout.getVisibility() == View.VISIBLE) {
									mEmptyLayout.setVisibility(View.GONE);
								}
								
								mNotesAdapter.notifyItemInserted(notes.size());
								
								sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
							} else {
								StyleableToast.makeText(activity, "Cannot create new folder", Toast.LENGTH_SHORT, R.style.designedToast).show();
							}
						}
						alertDialog.dismiss();
					}
					
				});
				
				cancelButton.setOnClickListener( new View.OnClickListener() {
					public void onClick(View view) {
						alertDialog.cancel();
					}
				});
				
				alertDialog.show();
			}
		} else {
			StyleableToast.makeText(activity, "Cannot create new folder", Toast.LENGTH_SHORT, R.style.designedToast).show();
		}
	}
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			File[] files = fileOrDirectory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					deleteRecursive(file);
				}
			}
		}
		
		Log.d(TAG, "deleteRecursive: " + fileOrDirectory.delete());
	}
	
	private void enableActionMode(int position) {
		if (actionMode == null) {
			
			actionMode = requireActivity().startActionMode(new android.view.ActionMode.Callback2() {
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.menu_notes_action_mode, menu);
					menu.removeItem(R.id.notes_action_share);
					MenuItem unStarred = menu.findItem(R.id.notes_action_unstar);
					unStarred.setIcon(R.drawable.starred_icon);
					actionModeOn = true;
					fab.setEnabled(false);
					mLinearLayout.setVisibility(View.GONE);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
					return true;
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.notes_action_delete:
							deleteRows();
							mode.finish();
							return true;
						case R.id.notes_action_select_all:
							selectAll();
							return true;
						case R.id.notes_action_unstar:
							addToStarred( mNotesAdapter.getSelectedItems() );
							mode.finish();
							return true;
						default:
							return false;
					}
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mNotesAdapter.clearSelections();
					actionModeOn = false;
					actionMode = null;
					fab.setEnabled(true);
					activity.mBottomAppBar.performShow();
					mLinearLayout.setVisibility(View.VISIBLE);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
				}
			});
		}
		toggleSelection(position);
	}
	
	private void toggleSelection(int position) {
		mNotesAdapter.toggleSelection(position);
		int count = mNotesAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
			actionMode = null;
		} else {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
	}
	
	private void selectAll() {
		mNotesAdapter.selectAll();
		int count = mNotesAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
		} else if (actionMode != null) {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
		
		actionMode.invalidate();
	}
	
	private void deleteRows() {
		final ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();


		AlertDialog.Builder builder = new AlertDialog.Builder(
				getContext());
		View view = getLayoutInflater().inflate(R.layout.alert_dialog_box, null);

		Button yesButton = view.findViewById(R.id.yes_button);
		Button noButton = view.findViewById(R.id.no_button);
		TextView title = view.findViewById(R.id.title_dialog);
		TextView detail = view.findViewById(R.id.detail_dialog);
		title.setText("Delete Folders");
		detail.setText("Are you sure you want to delete " + selectedItemPositions.size() + " folders?");

		builder.setView(view);

		final AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);

		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dialog.dismiss();

				// Hiding selected folders to be deleted from RecyclerView
				for (Integer folderPosition : selectedItemPositions) {
					recyclerView.findViewHolderForAdapterPosition(folderPosition).itemView.
							findViewById(R.id.noteItemLayout).setVisibility(View.GONE);
				}

				// Displaying a Snackbar to allow user to UNDO the delete folder action
				Snackbar undoSnackbar = Snackbar.make(recyclerView, R.string.multiple_folders_deleted, Snackbar.LENGTH_LONG);
				undoSnackbar.setAction(R.string.notes_action_undo, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// Making selected folders visible to user again
						for (Integer folderPosition : selectedItemPositions) {
							recyclerView.findViewHolderForAdapterPosition(folderPosition).itemView.
									findViewById(R.id.noteItemLayout).setVisibility(View.VISIBLE);
						}
					}

				}).setDuration(3000).setActionTextColor(getResources().getColor(R.color.colorPrimary));

				undoSnackbar.addCallback(new Snackbar.Callback() {

					// Called when the Snackbar is dismissed by an event other than
					// clicking of UNDO.
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
								event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
								event == Snackbar.Callback.DISMISS_EVENT_MANUAL) {

							if (mInterstitialAd.isLoaded()) {
								mInterstitialAd.show();
							}

							for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
								File file = new File(notes.get(selectedItemPositions.get(i)).getPath());
								deleteRecursive(file);

								if (notes.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_FOLDER) {
									ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
									for (FileItem linkItem : links) {
										if (linkItem.getPath().contains(file.getPath())) {
											linksToBeRemoved.add(linkItem);
										}
									}
									links.removeAll(linksToBeRemoved);

									SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
									SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

									if (links.size() == 0) {
										linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
									}
									Gson gson = new Gson();
									linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
									linkPreferenceEditor.apply();

									ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
									for (FileItem starItem : starred) {
										if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
											starredToBeRemoved.add(starItem);
										}
									}
									starred.removeAll(starredToBeRemoved);

									SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
									SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

									if (starred.size() == 0) {
										starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
									}
									starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
									starredPreferenceEditor.apply();
								}

								int starPosition = starredIndex(selectedItemPositions.get(i));
								if (starPosition != -1) {
									starred.remove(starPosition);
									SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
									SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

									if (starred.size() == 0) {
										starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
									}
									Gson gson = new Gson();
									starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
									starredPreferenceEditor.apply();
								}

								mNotesAdapter.notifyItemRemoved(selectedItemPositions.get(i));
								notes.remove(selectedItemPositions.get(i).intValue());
							}

							if (notes.isEmpty()) {
								mEmptyLayout.setVisibility(View.VISIBLE);
							}

							activity.mBottomAppBar.performShow();
						}
					}
				});
				undoSnackbar.show(); // Snackbar will appear for 3 seconds
			}
		});
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
		actionMode = null;
	}
	
	private void sort(String text, boolean ascending) {
		switch (text) {
			case SORT_BY_SIZE:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o1.getSize(), o2.getSize());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o2.getSize(), o1.getSize());
						}
					});
				}
				
				break;
			case SORT_BY_CREATION_TIME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateCreated().compareTo(o2.getDateCreated());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateCreated().compareTo(o1.getDateCreated());
						}
					});
				}
				
				break;
			case SORT_BY_MODIFIED_TIME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateModified().compareTo(o2.getDateModified());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateModified().compareTo(o1.getDateModified());
						}
					});
				}
				
				break;
			case SORT_BY_NAME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getName().compareTo(o1.getName());
						}
					});
				}
				break;
		}
		
		mNotesAdapter.notifyDataSetChanged();
	}

	private void addToStarred(ArrayList<Integer> positions){
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

		if (!starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", true);
			starred = new ArrayList<>();
		}

		final ArrayList<Integer> starredIndexes = new ArrayList<>( positions );
		for(Integer position : positions) {
			if (starredIndex( position ) == -1) {
				Log.d(TAG, "onMenuItemClick: starring position " + position);

				notes.get(position).setStarred(true);
				starred.add(notes.get(position));
			}

		}
		Gson gson = new Gson();
		starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
		starredPreferenceEditor.apply();
		mNotesAdapter.notifyDataSetChanged();
	}

	private void addToStarred(int position) {
		ArrayList<Integer> positions = new ArrayList<>(1);
		positions.add( position );
		addToStarred( positions );
	}
}

package com.studypartner.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muddzdev.styleabletoast.StyleableToast;
import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.User;
import com.studypartner.utils.Connection;

import java.io.IOException;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
	private static final String TAG = "ProfileFragment";
	
	private final int PICK_IMAGE_REQUEST = 111;
	
	private final String SESSIONS = "SESSIONS";
	
	private final String REMEMBER_ME_ENABLED = "rememberMeEnabled";
	private final String REMEMBER_ME_EMAIL = "rememberMeEmail";
	private final String REMEMBER_ME_PASSWORD = "rememberMePassword";
	
	private FirebaseUser currentUser;
	private DatabaseReference mDatabaseReference;
	private User user;
	
	private Uri filePath;
	
	private Button updateProfile, updateEmail, updatePassword, deleteAccount;
	private TextInputLayout fullNameTextInput, usernameTextInput, emailTextInput, passwordTextInput, oldPasswordTextInput,
			newPasswordTextInput, confirmPasswordTextInput, deleteAccountPasswordTextInput;
	private ImageView profileImageView;
	private ProgressBar progressBar;
	private ImageButton cameraButton;
	
	private boolean signedInWithGoogle = false;
	
	private String fullName, username, email, password, oldPassword, newPassword, confirmPassword, deleteAccountPassword;
	
	public ProfileFragment() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		oldPasswordTextInput.setEnabled(true);
		
		if (!signedInWithGoogle) {
			emailTextInput.setEnabled(true);
		}
		
		passwordTextInput.getEditText().setText("");
		oldPasswordTextInput.getEditText().setText("");
		newPasswordTextInput.getEditText().setText("");
		confirmPasswordTextInput.getEditText().setText("");
		deleteAccountPasswordTextInput.getEditText().setText("");
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		Log.d(TAG, "onActivityResult: starts");
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			Log.d(TAG, "onActivityResult: image received");
			filePath = data.getData();
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
				Log.d(TAG, "onActivityResult: setting image");
				profileImageView.setImageBitmap(bitmap);
				uploadImage();
			} catch (IOException ignored) {
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
		
		Log.d(TAG, "onCreateView: checking connection");
		
		Connection.checkConnection(this);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				MainActivity activity = (MainActivity) requireActivity();
				activity.mNavController.navigate(R.id.action_nav_profile_to_nav_home);
			}
		});
		
		//Setting hooks
		
		mDatabaseReference = FirebaseDatabase.getInstance().getReference();
		
		updateProfile = rootView.findViewById(R.id.profileScreenUpdateProfileButton);
		updateEmail = rootView.findViewById(R.id.profileScreenUpdateEmailButton);
		updatePassword = rootView.findViewById(R.id.profileScreenUpdatePasswordButton);
		deleteAccount = rootView.findViewById(R.id.profileScreenDeleteAccountButton);
		
		fullNameTextInput = rootView.findViewById(R.id.profileScreenFullNameTextInput);
		fullNameTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				fullNameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		usernameTextInput = rootView.findViewById(R.id.profileScreenUsernameTextInput);
		usernameTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				usernameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		emailTextInput = rootView.findViewById(R.id.profileScreenEmailTextInput);
		emailTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				emailTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		passwordTextInput = rootView.findViewById(R.id.profileScreenPasswordTextInput);
		passwordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		oldPasswordTextInput = rootView.findViewById(R.id.profileScreenOldPasswordTextInput);
		oldPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				oldPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		newPasswordTextInput = rootView.findViewById(R.id.profileScreenNewPasswordTextInput);
		confirmPasswordTextInput = rootView.findViewById(R.id.profileScreenConfirmPasswordTextInput);
		
		newPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				newPasswordTextInput.setError(null);
				confirmPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		confirmPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				confirmPasswordTextInput.setError(null);
				newPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		deleteAccountPasswordTextInput = rootView.findViewById(R.id.profileScreenDeleteAccountPasswordTextInput);
		deleteAccountPasswordTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				deleteAccountPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		profileImageView = rootView.findViewById(R.id.profileScreenImageView);
		cameraButton = rootView.findViewById(R.id.profileScreenImageButton);
		
		progressBar = rootView.findViewById(R.id.profileScreenProgressBar);
		
		currentUser = FirebaseAuth.getInstance().getCurrentUser();
		
		disableViews();
		
		mDatabaseReference.child("usernames").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Log.d(TAG, "onDataChange: setting values of edit texts");
				
				user = new User(currentUser.getDisplayName(),
						snapshot.getValue(String.class),
						currentUser.getEmail(), currentUser.isEmailVerified());
				
				usernameTextInput.getEditText().setText(user.getUsername());
				fullNameTextInput.getEditText().setText(user.getFullName());
				emailTextInput.getEditText().setText(user.getEmail());
				
				enableViews();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
			
			}
		});
		
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: camera button clicked");
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
			}
		});
		
		for (UserInfo userInfo : currentUser.getProviderData()) {
			if (userInfo.getProviderId().equals("google.com")) {
				Log.d(TAG, "onCreate: logged in with google");
				signedInWithGoogle = true;
			}
		}
		
		Log.d(TAG, "onCreate: loading image in profile photo");
		if (currentUser.getPhotoUrl() != null) {
			Picasso.get().load(currentUser.getPhotoUrl())
					.error(R.drawable.image_error_icon)
					.placeholder(R.drawable.user_icon)
					.into(profileImageView);
		}
		
		updateProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onCreate: update profile button clicked");
				
				Connection.checkConnection(ProfileFragment.this);
				
				disableViews();
				
				username = usernameTextInput.getEditText().getText().toString().trim();
				
				if (user.validateUsername(username) == null && !username.matches(user.getUsername())) {
					final boolean[] usernameTaken = {false};
					FirebaseDatabase.getInstance().getReference().child("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
						@Override
						public void onDataChange(@NonNull DataSnapshot snapshot) {
							if (snapshot.exists() && snapshot.hasChildren()) {
								for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
									if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
										if (username.matches((String) dataSnapshot.getValue())) {
											usernameTextInput.setError("Username is already taken by another user");
											enableViews();
											usernameTaken[0] = true;
										}
									}
								}
							}
							if (!usernameTaken[0]) {
								updateProfile();
							}
						}
						
						@Override
						public void onCancelled(@NonNull DatabaseError error) {
							enableViews();
						}
					});
				} else {
					updateProfile();
				}
			}
		});
		
		updateEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onCreate: update email button clicked");
				if (!signedInWithGoogle) {
					updateEmail();
				} else {
					StyleableToast.makeText(getContext(), "Signed in with google, cannot update email", Toast.LENGTH_SHORT, R.style.designedToast).show();
				}
			}
		});
		
		updatePassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onCreate: update password button clicked");
				if (!signedInWithGoogle) {
					updatePassword();
				} else {
					StyleableToast.makeText(getContext(), "Signed in with google, cannot update password", Toast.LENGTH_SHORT, R.style.designedToast).show();
				}
			}
		});
		
		deleteAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: delete account button pressed");
				deleteAccount();
			}
		});
		
		if (signedInWithGoogle) {
			emailTextInput.setEnabled(false);
		}
		
		Log.d(TAG, "onCreate: ends");
		
		return rootView;
	}
	
	private void deleteAccount() {
		Log.d(TAG, "deleteAccount: checking internet connection");
		
		disableViews();
		
		deleteAccountPassword = deleteAccountPasswordTextInput.getEditText().getText().toString().trim();
		
		if (!signedInWithGoogle && deleteAccountPasswordTextInput.getVisibility() == View.GONE) {
			Log.d(TAG, "deleteAccount: showing delete account password edit text");
			deleteAccountPasswordTextInput.setVisibility(View.VISIBLE);
			StyleableToast.makeText(getContext(), "Enter the current password to delete the account", Toast.LENGTH_SHORT, R.style.designedToast).show();
			enableViews();
		} else {
			Log.d(TAG, "deleteAccount: re authenticating the user");
			
			AuthCredential authCredential = null;
			
			if (signedInWithGoogle) {
				GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
				if (googleSignInAccount != null) {
					authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
				}
			} else {
				authCredential = EmailAuthProvider.getCredential(user.getEmail(), deleteAccountPassword);
			}
			
			currentUser.reauthenticate(authCredential)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: re authenticating successful");
								enableViews();

								AlertDialog.Builder builder = new AlertDialog.Builder(
										getContext());
								View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

								Button yesButton=view.findViewById(R.id.yes_button);
								Button noButton=view.findViewById(R.id.no_button);
								TextView title = view.findViewById(R.id.title_dialog);
								TextView detail = view.findViewById(R.id.detail_dialog);
								yesButton.setText("Delete");
								noButton.setText("Cancel");
								title.setText("Deleting your account");
								detail.setText("Are you sure you want to delete the account? All the data will be lost and cannot be retrieved later.");

								builder.setView(view);

								final AlertDialog dialog= builder.create();
								dialog.setCancelable(false);

								yesButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										Log.d(TAG, "onClick: deleting account");
										
										mDatabaseReference.child("users").child(currentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
											@Override
											public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
												if (error == null) {
													mDatabaseReference.child("usernames").child(currentUser.getUid()).removeValue(new DatabaseReference.CompletionListener() {
														@Override
														public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
															if (error == null) {
																
																if (!signedInWithGoogle && currentUser.getPhotoUrl() != null) {
																	FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(currentUser.getPhotoUrl())).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
																		@Override
																		public void onSuccess(Void aVoid) {
																			Log.d(TAG, "onSuccess: photo deleted successfully");
																		}
																	});
																}
																
																FirebaseAuth.getInstance().getCurrentUser().delete()
																		.addOnCompleteListener(new OnCompleteListener<Void>() {
																			@Override
																			public void onComplete(@NonNull Task<Void> task) {
																				if (task.isSuccessful()) {
																					StyleableToast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
																					
																					SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SESSIONS, MODE_PRIVATE);
																					SharedPreferences.Editor editor = sharedPreferences.edit();
																					
																					editor.putBoolean(REMEMBER_ME_ENABLED, false);
																					editor.putString(REMEMBER_ME_EMAIL, "");
																					editor.putString(REMEMBER_ME_PASSWORD, "");
																					editor.apply();
																					
																					FirebaseAuth.getInstance().signOut();
																					
																					GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
																							.requestIdToken(getString(R.string.default_web_client_id))
																							.requestEmail()
																							.build();
																					
																					GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
																					googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
																						@Override
																						public void onComplete(@NonNull Task<Void> task) {
																							if (task.isSuccessful()) {
																								MainActivity activity = (MainActivity) requireActivity();
																								activity.mNavController.navigate(R.id.nav_logout);
																								activity.finishAffinity();
																								activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
																								enableViews();
																							} else {
																								StyleableToast.makeText(requireContext(), "Could not sign out. Please try again", Toast.LENGTH_SHORT, R.style.designedToast).show();
																								enableViews();
																							}
																						}
																					});
																					
																				} else {
																					Log.d(TAG, "onComplete: could not delete account");
																					enableViews();
																				}
																			}
																		});
															} else {
																Log.d(TAG, "onComplete: could not delete data");
																enableViews();
															}
														}
													});
												} else {
													Log.d(TAG, "onComplete: could not delete data");
													enableViews();
												}
											}
										});
										dialog.dismiss();
									}
								});
								noButton.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										dialog.dismiss();
										deleteAccountPasswordTextInput.getEditText().setText("");
										deleteAccountPasswordTextInput.setVisibility(View.GONE);
										enableViews();
									}
								});
								
								dialog.show();
							} else if (task.getException().getMessage().contains("The supplied auth credential is malformed or has expired.")){
								StyleableToast.makeText(requireContext(), "We are facing some errors! Please login again and then try deleting your account", Toast.LENGTH_SHORT, R.style.designedToast).show();
								enableViews();
							} else if (task.getException().getMessage().contains("The password is invalid or the user does not have a password.")){
								StyleableToast.makeText(requireContext(), "The given password is incorrect.", Toast.LENGTH_SHORT, R.style.designedToast).show();
								enableViews();
							} else {
								StyleableToast.makeText(requireContext(), "We have blocked all requests from this device due to unusual activity. Try again later.", Toast.LENGTH_SHORT, R.style.designedToast).show();
								enableViews();
							}
						}
					});
		}
		
	}
	
	private void updateProfile() {
		Log.d(TAG, "updateProfile: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		fullName = fullNameTextInput.getEditText().getText().toString().trim();
		username = usernameTextInput.getEditText().getText().toString().trim();
		
		if (user.validateName(fullName) != null || user.validateUsername(username) != null) {
			Log.d(TAG, "updateProfile: username and full name invalid");
			fullNameTextInput.setError(user.validateName(fullName));
			usernameTextInput.setError(user.validateUsername(username));
			enableViews();
		} else if (!fullName.matches(user.getFullName()) && !username.matches(user.getUsername())) {
			Log.d(TAG, "updateProfile: updating username and full name");
			
			user.setFullName(fullName);
			user.setUsername(username);
			
			UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
			profileUpdates.setDisplayName(fullName);
			
			FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates.build())
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
							} else {
								Log.d(TAG, "onComplete: Could not update display name");
							}
						}
					});
			
			Log.d(TAG, "updateProfile: updating profile");
			UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
					.setDisplayName(fullName)
					.build();
			currentUser.updateProfile(profileChangeRequest)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: display name changed successfully");
							
							mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user)
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											Log.d(TAG, "onSuccess: users database updated successfully");
											
											mDatabaseReference.child("usernames").child(currentUser.getUid()).setValue(user.getUsername())
													.addOnSuccessListener(new OnSuccessListener<Void>() {
														@Override
														public void onSuccess(Void aVoid) {
															Log.d(TAG, "onSuccess: usernames database updated successfully");
															
															StyleableToast.makeText(getContext(), "Display name and username updated successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
															
															Log.d(TAG, "onSuccess: setting full name in nav header");
															NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
															TextView profileFullName = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
															if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
																profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
															}
															enableViews();
														}
													})
													.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															Log.d(TAG, "onFailure: usernames database could not be updated");
															StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
															enableViews();
														}
													});
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Log.d(TAG, "onFailure: users database could not be updated");
											StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
											enableViews();
										}
									});
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d(TAG, "onFailure: display name changing failed");
							StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
							enableViews();
						}
					});
			
		} else if (!fullName.matches(user.getFullName())) {
			Log.d(TAG, "updateProfile: updating full name");
			
			user.setFullName(fullName);
			
			Log.d(TAG, "updateProfile: updating profile");
			UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
					.setDisplayName(fullName)
					.build();
			currentUser.updateProfile(profileChangeRequest)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: display name changed successfully");
							
							mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user)
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											Log.d(TAG, "onSuccess: users database updated successfully");
											
											StyleableToast.makeText(getContext(), "Display name updated successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
											
											Log.d(TAG, "onSuccess: setting full name in nav header");
											NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
											TextView profileFullName = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
											
											if (null != FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) {
												profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
											}
											enableViews();
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Log.d(TAG, "onFailure: users database could not be updated");
											StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
											enableViews();
										}
									});
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d(TAG, "onFailure: display name changing failed");
							StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
							enableViews();
						}
					});
			
		} else if (!username.matches(user.getUsername())) {
			Log.d(TAG, "updateProfile: updating username");
			
			user.setUsername(username);
			
			mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user)
					.addOnSuccessListener(new OnSuccessListener<Void>() {
						@Override
						public void onSuccess(Void aVoid) {
							Log.d(TAG, "onSuccess: users database updated successfully");
							
							mDatabaseReference.child("usernames").child(currentUser.getUid()).setValue(user.getUsername())
									.addOnSuccessListener(new OnSuccessListener<Void>() {
										@Override
										public void onSuccess(Void aVoid) {
											Log.d(TAG, "onSuccess: usernames database updated successfully");
											
											StyleableToast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
											enableViews();
										}
									})
									.addOnFailureListener(new OnFailureListener() {
										@Override
										public void onFailure(@NonNull Exception e) {
											Log.d(TAG, "onFailure: usernames database could not be updated");
											StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
											enableViews();
										}
									});
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							Log.d(TAG, "onFailure: users database could not be updated");
							StyleableToast.makeText(getContext(), "Details could not be updated " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
							enableViews();
						}
					});
		} else {
			StyleableToast.makeText(requireContext(), "Name and Username are same as before", Toast.LENGTH_SHORT, R.style.designedToast).show();
			enableViews();
		}
		
	}
	
	private void updateEmail() {
		Log.d(TAG, "updateEmail: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		email = emailTextInput.getEditText().getText().toString().trim();
		password = passwordTextInput.getEditText().getText().toString().trim();
		
		if (email.matches(user.getEmail())) {
			StyleableToast.makeText(getContext(), "Entered email is same as current email", Toast.LENGTH_SHORT, R.style.designedToast).show();
			enableViews();
			return;
		}
		
		if (user.validateEmail(email) != null) {
			Log.d(TAG, "updateEmail: email invalid");
			emailTextInput.setError(user.validateEmail(email));
			enableViews();
			return;
		}
		
		if (passwordTextInput.getVisibility() == View.GONE) {
			Log.d(TAG, "updateEmail: showing password edit text");
			passwordTextInput.setVisibility(View.VISIBLE);
			emailTextInput.setEnabled(false);
			StyleableToast.makeText(getContext(), "Enter the current password to change the email", Toast.LENGTH_SHORT, R.style.designedToast).show();
			enableViews();
		} else {
			
			Log.d(TAG, "updateEmail: re authenticating the user");
			
			AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);
			
			currentUser.reauthenticate(authCredential)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								
								currentUser.updateEmail(email)
										.addOnCompleteListener(new OnCompleteListener<Void>() {
											@Override
											public void onComplete(@NonNull Task<Void> task) {
												if (task.isSuccessful()) {
													Log.d(TAG, "updateEmail: email change successful: " + currentUser.getEmail());
													
													user.setEmail(email);
													user.setEmailVerified(false);
													
													mDatabaseReference.child("users").child(currentUser.getUid()).setValue(user);
													
													currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
														@Override
														public void onComplete(@NonNull Task<Void> task) {
															if (task.isSuccessful()) {
																Log.d(TAG, "onComplete: verification email sent successfully");
																StyleableToast.makeText(getContext(), "Email changed successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
																NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
																ImageView verifiedImage = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerVerifiedImage);
																
																if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
																	verifiedImage.setImageResource(R.drawable.verified_icon);
																} else {
																	verifiedImage.setImageResource(R.drawable.not_verified_icon);
																}
																
																SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SESSIONS, MODE_PRIVATE);
																SharedPreferences.Editor editor = sharedPreferences.edit();
																
																if (sharedPreferences.getBoolean(REMEMBER_ME_ENABLED, false)) {
																	editor.putString(REMEMBER_ME_EMAIL, email);
																	editor.apply();
																}
																
																emailTextInput.setEnabled(true);
																
																passwordTextInput.getEditText().setText("");
																passwordTextInput.setVisibility(View.GONE);
																enableViews();
															} else {
																Log.d(TAG, "onComplete: verification email could not be sent: " + task.getException().getMessage());
																enableViews();
															}
														}
													});
												} else {
													StyleableToast.makeText(getContext(), "Could not update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
													enableViews();
												}
											}
										});
								
							} else {
								StyleableToast.makeText(getContext(), "Could not update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
								enableViews();
							}
						}
					});
		}
		
	}
	
	private void updatePassword() {
		Log.d(TAG, "updatePassword: checking internet connection");
		Connection.checkConnection(this);
		
		disableViews();
		
		oldPassword = oldPasswordTextInput.getEditText().getText().toString().trim();
		newPassword = newPasswordTextInput.getEditText().getText().toString().trim();
		confirmPassword = confirmPasswordTextInput.getEditText().getText().toString().trim();
		
		if (oldPasswordTextInput.getVisibility() == View.GONE) {
			Log.d(TAG, "updatePassword: showing old password edit text");
			oldPasswordTextInput.setVisibility(View.VISIBLE);
			StyleableToast.makeText(getContext(), "Enter the current password to change it", Toast.LENGTH_SHORT, R.style.designedToast).show();
			enableViews();
		} else {
			
			Log.d(TAG, "updatePassword: re authenticating the user");
			
			AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(currentUser.getEmail()), oldPassword);
			
			currentUser.reauthenticate(authCredential)
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: re authentication successful");
								
								if (newPasswordTextInput.getVisibility() == View.GONE && confirmPasswordTextInput.getVisibility() == View.GONE) {
									Log.d(TAG, "updatePassword: showing password edit text");
									newPasswordTextInput.setVisibility(View.VISIBLE);
									confirmPasswordTextInput.setVisibility(View.VISIBLE);
									StyleableToast.makeText(getContext(), "Enter the new password", Toast.LENGTH_SHORT, R.style.designedToast).show();
									oldPasswordTextInput.setEnabled(false);
									enableViews();
									return;
								} else if (user.validatePassword(newPassword, confirmPassword) != null || user.validateConfirmPassword(confirmPassword, newPassword) != null) {
									
									newPasswordTextInput.setError(user.validatePassword(newPassword, confirmPassword));
									confirmPasswordTextInput.setError(user.validateConfirmPassword(confirmPassword, newPassword));
									enableViews();
									return;
								}
								
								Log.d(TAG, "onComplete: changing password");
								
								currentUser.updatePassword(newPassword)
										.addOnCompleteListener(new OnCompleteListener<Void>() {
											@Override
											public void onComplete(@NonNull Task<Void> task) {
												if (task.isSuccessful()) {
													StyleableToast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
													
													SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SESSIONS, MODE_PRIVATE);
													SharedPreferences.Editor editor = sharedPreferences.edit();
													
													if (sharedPreferences.getBoolean(REMEMBER_ME_ENABLED, false)) {
														editor.putString(REMEMBER_ME_PASSWORD, newPassword);
														editor.apply();
													}
													
													oldPasswordTextInput.setEnabled(true);
													
													oldPasswordTextInput.getEditText().setText("");
													newPasswordTextInput.getEditText().setText("");
													confirmPasswordTextInput.getEditText().setText("");
													
													oldPasswordTextInput.setVisibility(View.GONE);
													newPasswordTextInput.setVisibility(View.GONE);
													confirmPasswordTextInput.setVisibility(View.GONE);
													enableViews();
												} else {
													StyleableToast.makeText(getContext(), "Could not update password " + task.getException().getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
													enableViews();
												}
											}
										});
								
							} else {
								StyleableToast.makeText(getContext(), "Could not update password. Please reenter the correct password", Toast.LENGTH_SHORT, R.style.designedToast).show();
								enableViews();
							}
						}
					});
			
		}
		
	}
	
	private void uploadImage() {
		Log.d(TAG, "uploadImage: checking internet connection");
		Connection.checkConnection(this);
		
		Log.d(TAG, "uploadImage: uploading image");
		
		if (filePath != null) {
			final ProgressDialog progressDialog = new ProgressDialog(getContext());
			progressDialog.setTitle("Uploading image...");
			progressDialog.show();
			
			final StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + currentUser.getUid() + "_pp.jpg");
			
			UploadTask uploadTask = ref.putFile(filePath);
			
			Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
				@Override
				public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
					if (!task.isSuccessful()) {
						throw task.getException();
					}
					
					return ref.getDownloadUrl();
				}
			}).addOnSuccessListener(getActivity(), new OnSuccessListener<Uri>() {
				@Override
				public void onSuccess(Uri uri) {
					Log.d(TAG, "onSuccess: photo upload successful");
					if (uri != null) {
						Log.d(TAG, "onSuccess: image download uri is " + uri);
						UserProfileChangeRequest userProfileUpdate = new UserProfileChangeRequest.Builder()
								.setPhotoUri(uri)
								.build();
						currentUser.updateProfile(userProfileUpdate)
								.addOnSuccessListener(new OnSuccessListener<Void>() {
									@Override
									public void onSuccess(Void aVoid) {
										Log.d(TAG, "onSuccess: image saved successfully");
										
										NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
										ImageView profileImage = navigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
										if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
											Log.d(TAG, "onCreate: Downloading profile image");
											Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
													.error(Objects.requireNonNull(requireActivity().getDrawable(R.drawable.image_error_icon)))
													.placeholder(Objects.requireNonNull(requireActivity().getDrawable(R.drawable.image_loading_icon)))
													.into(profileImage);
										} else {
											Log.d(TAG, "onCreate: Image url does not exist for user");
											profileImage.setImageDrawable(requireActivity().getDrawable(R.drawable.image_error_icon));
										}
									}
								})
								.addOnFailureListener(new OnFailureListener() {
									@Override
									public void onFailure(@NonNull Exception e) {
										Log.d(TAG, "onFailure: image could not be uploaded");
									}
								});
						progressDialog.dismiss();
					}
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Log.d(TAG, "onFailure: photo upload failed");
					progressDialog.dismiss();
					StyleableToast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
				}
			});
		}
	}
	
	private void disableViews() {
		progressBar.setVisibility(View.VISIBLE);
		
		updateProfile.setClickable(false);
		updateEmail.setClickable(false);
		updatePassword.setClickable(false);
		cameraButton.setClickable(false);
		deleteAccount.setClickable(false);
		
		fullNameTextInput.setEnabled(false);
		usernameTextInput.setEnabled(false);
		passwordTextInput.setEnabled(false);
		newPasswordTextInput.setEnabled(false);
		confirmPasswordTextInput.setEnabled(false);
		deleteAccountPasswordTextInput.setEnabled(false);
	}
	
	private void enableViews() {
		progressBar.setVisibility(View.INVISIBLE);
		
		updateProfile.setClickable(true);
		updateEmail.setClickable(true);
		updatePassword.setClickable(true);
		cameraButton.setClickable(true);
		deleteAccount.setClickable(true);
		
		fullNameTextInput.setEnabled(true);
		usernameTextInput.setEnabled(true);
		passwordTextInput.setEnabled(true);
		newPasswordTextInput.setEnabled(true);
		confirmPasswordTextInput.setEnabled(true);
		deleteAccountPasswordTextInput.setEnabled(true);
	}
}

package com.studypartner.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.AttendanceAdapter;
import com.studypartner.models.AttendanceItem;
import com.studypartner.utils.Connection;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class AttendanceFragment extends Fragment {
	
	private final String REQUIRED_ATTENDANCE_CHOSEN = "requiredAttendanceChosen";
	private final String REQUIRED_ATTENDANCE = "requiredAttendance";
	
	private ConstraintLayout mainLayout, requiredAttendanceLayout;
	
	private RecyclerView mRecyclerView;
	private FloatingActionButton attendanceRequiredPercentageFabNext;
	private NavController mNavController;
	private LinearLayout mEmptyLayout;
	private TextView dateText, dayText, percentageAttended, attendanceComment, attendanceName, attendanceRequiredPercentageSetter;
	private CircularProgressBar attendedProgressBar, requiredProgressBar, attendanceRequiredPercentageProgressBarSetter;
	private SeekBar attendanceRequiredPercentageSeekBarSetter;
	
	private ArrayList<AttendanceItem> mAttendanceItemArrayList = new ArrayList<>();
	private AttendanceAdapter attendanceAdapter;
	private SharedPreferences sharedPreferences;
	
	private double requiredPercentage;
	
	public AttendanceFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_attendance, container, false);
		
		requiredAttendanceLayout = rootView.findViewById(R.id.attendanceRequiredAttendanceConstraintLayout);
		
		attendanceName = rootView.findViewById(R.id.attendanceName);
		attendanceRequiredPercentageSetter = rootView.findViewById(R.id.attendanceRequiredPercentageSetter);
		attendanceRequiredPercentageProgressBarSetter = rootView.findViewById(R.id.attendanceRequiredProgressBarSetter);
		attendanceRequiredPercentageSeekBarSetter = rootView.findViewById(R.id.attendanceRequiredSeekBarSetter);
		attendanceRequiredPercentageFabNext = rootView.findViewById(R.id.attendanceRequiredFab);
		
		mainLayout = rootView.findViewById(R.id.attendanceMainConstraintLayout);
		
		mRecyclerView = rootView.findViewById(R.id.attendanceRecyclerView);
		mEmptyLayout = rootView.findViewById(R.id.attendanceItemEmptyAttendance);
		Button addButton = rootView.findViewById(R.id.attendanceAddButton);
		Button updateButton = rootView.findViewById(R.id.attendanceUpdateButton);
		dateText = rootView.findViewById(R.id.attendanceDate);
		dayText = rootView.findViewById(R.id.attendanceDay);
		attendanceComment = rootView.findViewById(R.id.attendanceText);
		percentageAttended = rootView.findViewById(R.id.attendancePercentageAttended);
		attendedProgressBar = rootView.findViewById(R.id.attendanceAttendedTotalProgressBar);
		requiredProgressBar = rootView.findViewById(R.id.attendanceRequiredProgressBar);
		
		sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "RequiredPercentageSelected", Context.MODE_PRIVATE);
		
		MainActivity activity = (MainActivity) requireActivity();
		mNavController = NavHostFragment.findNavController(this);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				mNavController.navigate(R.id.action_nav_attendance_to_nav_home);
			}
		});
		
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addSubject();
			}
		});
		
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
		mRecyclerView.setAdapter(attendanceAdapter);
		
		attendanceRequiredPercentageFabNext.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				requiredPercentage = attendanceRequiredPercentageSeekBarSetter.getProgress();
				sharedPreferences.edit().putBoolean(REQUIRED_ATTENDANCE_CHOSEN, true).apply();
				sharedPreferences.edit().putFloat(REQUIRED_ATTENDANCE, (float) requiredPercentage).apply();
				changeLayout();
				setTotalPercentages();
				for (AttendanceItem item : mAttendanceItemArrayList) {
					item.setRequiredPercentage(requiredPercentage);
				}
				
				attendanceAdapter.notifyDataSetChanged();
				
				SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
				SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
				Gson gson = new Gson();
				
				String json = gson.toJson(mAttendanceItemArrayList);
				
				if (!attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false) && mAttendanceItemArrayList.size() > 0) {
					attendancePreferenceEditor.putBoolean("ATTENDANCE_ITEMS_EXISTS", true);
				}
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
		});
		
		if (sharedPreferences.getBoolean(REQUIRED_ATTENDANCE_CHOSEN, false)) {
			requiredPercentage = sharedPreferences.getFloat(REQUIRED_ATTENDANCE, 75);
		} else {
			changeLayout();
		}
		
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeLayout();
				double progress = sharedPreferences.getFloat(REQUIRED_ATTENDANCE, 75);
				attendanceRequiredPercentageProgressBarSetter.setProgress((float) progress);
				DecimalFormat decimalFormat = new DecimalFormat("##.#");
				attendanceRequiredPercentageSetter.setText(requireContext().getString(R.string.attendance_percentage, decimalFormat.format(progress)));
				attendanceRequiredPercentageSeekBarSetter.setProgress((int) progress);
			}
		});
		
		attendanceRequiredPercentageSeekBarSetter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				attendanceRequiredPercentageProgressBarSetter.setProgress(progress);
				DecimalFormat decimalFormat = new DecimalFormat("##.#");
				attendanceRequiredPercentageSetter.setText(requireContext().getString(R.string.attendance_percentage, decimalFormat.format(progress)));
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			
			}
		});
		
		populateDataAndSetAdapter();
		
		initializeViews();
		
		return rootView;
	}
	
	private void changeLayout() {
		
		if (mainLayout.getVisibility() == View.VISIBLE) {
			Connection.checkConnection(this);
			mainLayout.animate()
					.alpha(0.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainLayout.setVisibility(View.GONE);
						}
					});
			
			requiredAttendanceLayout.animate()
					.alpha(1.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							requiredAttendanceLayout.setVisibility(View.VISIBLE);
						}
					});
			attendanceName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
		} else {
			requiredAttendanceLayout.animate()
					.alpha(0.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							requiredAttendanceLayout.setVisibility(View.GONE);
						}
					});
			
			mainLayout.animate()
					.alpha(1.0f)
					.setDuration(300)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mainLayout.setVisibility(View.VISIBLE);
						}
					});
		}
	}
	
	private void populateDataAndSetAdapter() {
		
		final SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
		final SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
		final Gson gson = new Gson();
		
		if (attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
			String json = attendancePreference.getString("ATTENDANCE_ITEMS", "");
			Type type = new TypeToken<List<AttendanceItem>>() {
			}.getType();
			mEmptyLayout.setVisibility(View.INVISIBLE);
			mAttendanceItemArrayList = gson.fromJson(json, type);
		} else {
			mAttendanceItemArrayList = new ArrayList<>();
			mEmptyLayout.setVisibility(View.VISIBLE);
		}
		
		attendanceAdapter = new AttendanceAdapter(getContext(), mAttendanceItemArrayList, new AttendanceAdapter.AttendanceItemClickListener() {
			@Override
			public void onAttendedPlusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).increaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void onAttendedMinusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).decreaseAttendedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void onMissedPlusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).increaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void onMissedMinusButtonClicked(final int position) {
				mAttendanceItemArrayList.get(position).decreaseMissedClasses();
				attendanceAdapter.notifyItemChanged(position);
				setTotalPercentages();
				String json = gson.toJson(mAttendanceItemArrayList);
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();
			}
			
			@Override
			public void editButtonClicked(int position) {
				editSubjectName(position);
			}
			
			@Override
			public void deleteButtonClicked(int position) {
				deleteSubject(position);
			}
		});
		
		mRecyclerView.setAdapter(attendanceAdapter);
	}
	
	private void initializeViews() {
		
		Date date = new Date();
		
		SimpleDateFormat dateFormat;
		
		int day = Calendar.getInstance().get(Calendar.DATE);
		
		if (day >= 11 && day <= 13) {
			dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
		} else {
			switch (day % 10) {
				case 1:
					dateFormat = new SimpleDateFormat("dd'st' MMMM, yyyy", Locale.getDefault());
					break;
				case 2:
					dateFormat = new SimpleDateFormat("dd'nd' MMMM, yyyy", Locale.getDefault());
					break;
				case 3:
					dateFormat = new SimpleDateFormat("dd'rd' MMMM, yyyy", Locale.getDefault());
					break;
				default:
					dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
					break;
			}
		}
		
		SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
		dayText.setText(dayFormat.format(date));
		dateText.setText(dateFormat.format(date));
		setTotalPercentages();
		
	}
	
	private void setTotalPercentages() {
		double totalPercentageAttended = 0;
		
		int totalClasses = 0, attendedClasses = 0;
		
		for (AttendanceItem item : mAttendanceItemArrayList) {
			attendedClasses += item.getAttendedClasses();
			totalClasses += item.getTotalClasses();
		}
		
		if (totalClasses > 0) {
			totalPercentageAttended = (double) attendedClasses * 100 / totalClasses;
			DecimalFormat decimalFormat = new DecimalFormat("##.#");
			percentageAttended.setText(requireContext().getString(R.string.attendance_percentage, decimalFormat.format(totalPercentageAttended)));
		} else {
			percentageAttended.setText(getString(R.string.attendance_item_empty_percentage));
		}
		
		requiredProgressBar.setProgress((float) requiredPercentage);
		attendedProgressBar.setProgress((float) totalPercentageAttended);
		
		if (totalPercentageAttended < requiredPercentage) {
			percentageAttended.setTextColor(getResources().getColor(R.color.lowAttendanceColor, getActivity().getTheme()));
			attendedProgressBar.setProgressBarColor(getResources().getColor(R.color.lowAttendanceColor, getActivity().getTheme()));
			if (totalClasses == 0) {
				attendanceComment.setText(R.string.attendance_no_classes);
			} else {
				attendanceComment.setText(R.string.attendance_not_on_track);
			}
		} else {
			percentageAttended.setTextColor(getResources().getColor(R.color.highAttendanceColor, getActivity().getTheme()));
			attendedProgressBar.setProgressBarColor(getResources().getColor(R.color.highAttendanceColor, getActivity().getTheme()));
			attendanceComment.setText(R.string.attendance_on_track);
		}
		
		if (attendedClasses == 0) {
			percentageAttended.setTextColor(getResources().getColor(R.color.requiredAttendanceColor, getActivity().getTheme()));
		}
	}
	
	private void addSubject() {
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String subjectName = subjectNameTextInput.getEditText().getText().toString().trim();
				if (subjectName.length() > 0) {
					
					AttendanceItem newItem = new AttendanceItem(subjectName, requiredPercentage, 0, 0);
					mEmptyLayout.setVisibility(View.INVISIBLE);
					mAttendanceItemArrayList.add(newItem);
					attendanceAdapter.notifyItemInserted(mAttendanceItemArrayList.size());
					setTotalPercentages();
					
					SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
					SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
					Gson gson = new Gson();
					
					String json = gson.toJson(mAttendanceItemArrayList);
					
					if (!attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
						attendancePreferenceEditor.putBoolean("ATTENDANCE_ITEMS_EXISTS", true);
					}
					
					attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
					attendancePreferenceEditor.apply();
				} else {
					StyleableToast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT, R.style.designedToast).show();
				}
				builder.dismiss();
			}
		});
		
		builder.setView(dialogView);
		builder.show();
	}
	
	private void editSubjectName(final int position) {
		
		final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.attendance_item_subject_name_dialog, null);
		
		Button okButton = dialogView.findViewById(R.id.attendanceItemSubjectOkButton);
		Button cancelButton = dialogView.findViewById(R.id.attendanceItemSubjectCancelButton);
		final TextInputLayout subjectNameTextInput = dialogView.findViewById(R.id.attendanceItemSubjectNameTextInput);
		subjectNameTextInput.getEditText().setText(mAttendanceItemArrayList.get(position).getSubjectName(), TextView.BufferType.EDITABLE);
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				builder.dismiss();
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String subjectName = subjectNameTextInput.getEditText().getText().toString().trim();
				
				if (subjectName.length() > 0) {
					mAttendanceItemArrayList.get(position).setSubjectName(subjectNameTextInput.getEditText().getText().toString());
					builder.dismiss();
					attendanceAdapter.notifyItemChanged(position);
					
					SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
					SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
					Gson gson = new Gson();
					
					String json = gson.toJson(mAttendanceItemArrayList);
					
					attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
					attendancePreferenceEditor.apply();
					
				} else {
					StyleableToast.makeText(getContext(), "Subject name cannot be empty", Toast.LENGTH_SHORT, R.style.designedToast).show();
				}
			}
		});
		
		builder.setView(dialogView);
		builder.show();
	}
	
	private void deleteSubject(final int position) {


		AlertDialog.Builder builder = new AlertDialog.Builder(
				getContext());
		View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

		Button yesButton=view.findViewById(R.id.yes_button);
		Button noButton=view.findViewById(R.id.no_button);
		TextView title = view.findViewById(R.id.title_dialog);
		TextView detail = view.findViewById(R.id.detail_dialog);
		title.setText("Delete Subject");
		detail.setText("Are you sure you want to delete attendance record for this subject?");

		builder.setView(view);

		final AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(true);

		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAttendanceItemArrayList.remove(position);
				attendanceAdapter.notifyItemRemoved(position);
				
				setTotalPercentages();
				
				SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
				SharedPreferences.Editor attendancePreferenceEditor = attendancePreference.edit();
				Gson gson = new Gson();
				
				String json = gson.toJson(mAttendanceItemArrayList);
				
				if (mAttendanceItemArrayList.size() == 0) {
					attendancePreferenceEditor.putBoolean("ATTENDANCE_ITEMS_EXISTS", false);
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				
				attendancePreferenceEditor.putString("ATTENDANCE_ITEMS", json);
				attendancePreferenceEditor.apply();

				dialog.dismiss();
			}
		});
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				attendanceAdapter.notifyItemChanged(position);

				dialog.dismiss();
			}
		});
		dialog.show();
	}
}

package com.studypartner.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.utils.FileUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutUsFragment extends Fragment {
	
	public AboutUsFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				MainActivity activity = (MainActivity) requireActivity();
				activity.mNavController.navigate(R.id.action_nav_about_us_to_nav_home);
			}
		});
		
		return inflater.inflate(R.layout.fragment_about_us, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		view.findViewById(R.id.teamCard1Github).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://github.com/Kayvee08");
			}
		});
		
		view.findViewById(R.id.teamCard1LinkedIn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://www.linkedin.com/in/karanveer-singh-102153174");
			}
		});
		
		view.findViewById(R.id.teamCard2Github).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://github.com/Saket5");
			}
		});
		
		view.findViewById(R.id.teamCard2LinkedIn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://www.linkedin.com/in/saketagar");
			}
		});
		
		view.findViewById(R.id.teamCard3Github).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://github.com/krayong");
			}
		});
		
		view.findViewById(R.id.teamCard3LinkedIn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUtils.openLink(requireContext(), "https://www.linkedin.com/in/karangourisaria");
			}
		});
		
		view.findViewById(R.id.aboutUsContactUs).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
				feedbackIntent.setData(Uri.parse("mailto:"));
				feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"studypartnerapp@gmail.com"});
				requireActivity().startActivity(Intent.createChooser(feedbackIntent, "Choose your email client"));
			}
		});
	}
}

package com.studypartner.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.HomeAttendanceAdapter;
import com.studypartner.adapters.HomeMediaAdapter;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.AttendanceItem;
import com.studypartner.models.FileItem;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "HomeFragment";
	private final ArrayList<FileItem> notes = new ArrayList<>();
	private final ArrayList<FileItem> docsList = new ArrayList<>();
	private final ArrayList<FileItem> imagesList = new ArrayList<>();
	private final ArrayList<FileItem> videosList = new ArrayList<>();
	private File noteFolder;
	private MainActivity activity;
	private ArrayList<ReminderItem> reminders = new ArrayList<>();
	private CardView reminderCard, emptyReminderCard;
	private ReminderItem reminderItemToBeDisplayed;
	
	private ArrayList<AttendanceItem> attendances = new ArrayList<>();
	
	public HomeFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: starts");
		
		return inflater.inflate(R.layout.fragment_home, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		activity = (MainActivity) requireActivity();
		
		if (firebaseUser != null) {
			File studyPartnerFolder = new File(String.valueOf(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(requireContext().getExternalFilesDir(null)).getParentFile()).getParentFile()).getParentFile()).getParentFile()), "StudyPartner");
			if (!studyPartnerFolder.exists()) {
				if (studyPartnerFolder.mkdirs()) {
					noteFolder = new File(studyPartnerFolder, firebaseUser.getUid());
					if (!noteFolder.exists())
						Log.d(TAG, "onViewCreated: making note folder returned " + noteFolder.mkdirs());
				} else {
					noteFolder = new File(requireContext().getExternalFilesDir(null), firebaseUser.getUid());
					if (!noteFolder.exists())
						Log.d(TAG, "onViewCreated: making note folder returned " + noteFolder.mkdirs());
				}
			} else {
				noteFolder = new File(studyPartnerFolder, firebaseUser.getUid());
				if (!noteFolder.exists())
					Log.d(TAG, "onViewCreated: making note folder returned " + noteFolder.mkdirs());
			}
		} else {
			FirebaseAuth.getInstance().signOut();
			
			GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
					.requestIdToken(getString(R.string.default_web_client_id))
					.requestEmail()
					.build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
			googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						activity.mNavController.navigate(R.id.nav_logout);
						activity.finishAffinity();
						activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
					} else {
						activity.finishAffinity();
					}
				}
			});
		}
		
		activity.fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.mNavController.navigate(R.id.action_nav_home_to_nav_notes);
			}
		});
		
		initializeReminder(view);
		
		populateDataAndSetAdapter(view);
	}
	
	private void initializeReminder(View view) {
		
		reminderCard = view.findViewById(R.id.homeCarouselReminderCard);
		emptyReminderCard = view.findViewById(R.id.homeCarouselEmptyReminderCard);
		
		emptyReminderCard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.mNavController.navigate(R.id.nav_reminder, null, activity.leftToRightBuilder.build());
			}
		});
		
		TextView reminderTitle = view.findViewById(R.id.homeCarouselReminderTitle);
		TextView reminderTime = view.findViewById(R.id.homeCarouselReminderTime);
		TextView reminderDate = view.findViewById(R.id.homeCarouselReminderDate);
		TextView reminderDay = view.findViewById(R.id.homeCarouselReminderDay);
		
		SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		Gson gson = new Gson();
		
		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {
			}.getType();
			reminders = gson.fromJson(json, type);
		} else {
			reminders = new ArrayList<>();
		}
		
		Calendar calendar = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		
		ArrayList<ReminderItem> remindersToBeRemoved = new ArrayList<>();
		
		for (int position = 0; position < reminders.size(); position++) {
			
			ReminderItem item = reminders.get(position);
			
			int year = Integer.parseInt(item.getDate().substring(6));
			int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
			int day = Integer.parseInt(item.getDate().substring(0, 2));
			int hour = Integer.parseInt(item.getTime().substring(0, 2));
			int minute = Integer.parseInt(item.getTime().substring(3, 5));
			String amOrPm = item.getTime().substring(6);
			if (amOrPm.equals("PM") && hour != 12)
				hour = hour + 12;
			calendar.set(year, month, day, hour, minute);
			if (calendar.compareTo(today) <= 0) {
				remindersToBeRemoved.add(item);
			}
		}
		
		reminders.removeAll(remindersToBeRemoved);
		
		Collections.sort(reminders, new Comparator<ReminderItem>() {
			@Override
			public int compare(ReminderItem o1, ReminderItem o2) {
				int dateCompare = o1.getDate().compareTo(o2.getDate());
				if (dateCompare == 0) {
					return o1.getTime().compareTo(o2.getTime());
				} else {
					return dateCompare;
				}
			}
		});
		
		if (reminders.size() > 0) {
			reminderItemToBeDisplayed = reminders.get(0);
		}
		
		if (reminders.size() == 0 || reminderItemToBeDisplayed == null) {
			
			emptyReminderCard.setVisibility(View.VISIBLE);
			reminderCard.setVisibility(View.INVISIBLE);
			
		} else {
			
			emptyReminderCard.setVisibility(View.INVISIBLE);
			reminderCard.setVisibility(View.VISIBLE);
			
			reminderTitle.setText(reminderItemToBeDisplayed.getTitle());
			reminderTime.setText(reminderItemToBeDisplayed.getTime());
			
			String stringDate = reminderItemToBeDisplayed.getDate();
			
			int year = Integer.parseInt(stringDate.substring(6));
			int month = Integer.parseInt(stringDate.substring(3, 5)) - 1;
			int day = Integer.parseInt(stringDate.substring(0, 2));
			
			calendar.set(year, month, day);
			
			SimpleDateFormat dateFormat;
			
			if (day >= 11 && day <= 13) {
				dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
			} else {
				switch (day % 10) {
					case 1:
						dateFormat = new SimpleDateFormat("dd'st' MMMM, yyyy", Locale.getDefault());
						break;
					case 2:
						dateFormat = new SimpleDateFormat("dd'nd' MMMM, yyyy", Locale.getDefault());
						break;
					case 3:
						dateFormat = new SimpleDateFormat("dd'rd' MMMM, yyyy", Locale.getDefault());
						break;
					default:
						dateFormat = new SimpleDateFormat("dd'th' MMMM, yyyy", Locale.getDefault());
						break;
				}
			}
			
			Date date = calendar.getTime();
			
			SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
			
			reminderDate.setText(dateFormat.format(date));
			reminderDay.setText(dayFormat.format(date));
			
		}
		
		initializeAttendance(view);
		
	}
	
	private void initializeAttendance(View view) {
		
		RecyclerView attendanceRecyclerView = view.findViewById(R.id.homeCarouselAttendanceRecyclerView);
		CardView highAttendanceCard = view.findViewById(R.id.homeCarouselHighAttendanceCard);
		CardView emptyAttendanceCard = view.findViewById(R.id.homeCarouselEmptyAttendanceCard);
		
		attendanceRecyclerView.setVisibility(View.INVISIBLE);
		highAttendanceCard.setVisibility(View.INVISIBLE);
		emptyAttendanceCard.setVisibility(View.INVISIBLE);
		
		CircularProgressBar totalAttendedProgressBar = view.findViewById(R.id.homeCarouselAttendanceTotalAttendedProgressBar);
		CircularProgressBar totalRequiredProgressBar = view.findViewById(R.id.homeCarouselAttendanceTotalRequiredProgressBar);
		TextView percentageAttended = view.findViewById(R.id.homeCarouselAttendanceTotalPercentageAttended);
		
		final SharedPreferences attendancePreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "ATTENDANCE", MODE_PRIVATE);
		final Gson gson = new Gson();
		
		if (attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
			String json = attendancePreference.getString("ATTENDANCE_ITEMS", "");
			Type type = new TypeToken<List<AttendanceItem>>() {
			}.getType();
			attendances = gson.fromJson(json, type);
		}
		
		if (attendances == null) {
			attendances = new ArrayList<>();
		}
		
		if (attendances.size() > 0) { // Attendance exists atleast 1
			
			double requiredPercentage = attendances.get(0).getRequiredPercentage();
			
			ArrayList<AttendanceItem> attendancesToBeRemoved = new ArrayList<>();
			
			for (AttendanceItem item : attendances) {
				if (item.getTotalClasses() == 0 || item.getAttendedPercentage() >= item.getRequiredPercentage()) { // Has not attended classes or has high attendance
					attendancesToBeRemoved.add(item);
				}
			}
			
			attendances.removeAll(attendancesToBeRemoved);
			
			if (attendances.size() == 0) { // after removing size 0 so all high attendance
				
				attendanceRecyclerView.setVisibility(View.INVISIBLE);
				highAttendanceCard.setVisibility(View.VISIBLE);
				emptyAttendanceCard.setVisibility(View.INVISIBLE);
				
				if (attendancePreference.getBoolean("ATTENDANCE_ITEMS_EXISTS", false)) {
					String json = attendancePreference.getString("ATTENDANCE_ITEMS", "");
					Type type = new TypeToken<List<AttendanceItem>>() {
					}.getType();
					attendances = gson.fromJson(json, type);
				} else {
					attendances = new ArrayList<>();
				}
				
				if (attendances == null) {
					attendances = new ArrayList<>();
				}
				
				double totalPercentageAttended;
				
				int totalClasses = 0, attendedClasses = 0;
				
				for (AttendanceItem item : attendances) {
					attendedClasses += item.getAttendedClasses();
					totalClasses += item.getTotalClasses();
				}
				
				if (totalClasses > 0) {
					totalPercentageAttended = (double) attendedClasses * 100 / totalClasses;
					DecimalFormat decimalFormat = new DecimalFormat("##.#");
					percentageAttended.setText(requireContext().getString(R.string.attendance_percentage, decimalFormat.format(totalPercentageAttended)));
					totalAttendedProgressBar.setProgress((float) totalPercentageAttended);
				} else {
					TextView highAttendanceTitle = view.findViewById(R.id.homeCarouselAttendanceTotalTitle);
					TextView highAttendanceSubTitle = view.findViewById(R.id.homeCarouselAttendanceTotalSubTitle);
					
					highAttendanceTitle.setText(R.string.home_carousel_attend_classes);
					highAttendanceSubTitle.setText(R.string.home_carousel_no_classes_attended);
					totalAttendedProgressBar.setProgress((float) 0);
				}
				
				totalRequiredProgressBar.setProgress((float) requiredPercentage);
				
			} else { // low attendance item exists
				
				attendanceRecyclerView.setVisibility(View.VISIBLE);
				highAttendanceCard.setVisibility(View.INVISIBLE);
				emptyAttendanceCard.setVisibility(View.INVISIBLE);
				
				if ((reminders.size() == 0 || reminderItemToBeDisplayed == null) && attendances.size() > 1) { // has attendance, so if no reminder then remove empty reminder layout
					emptyReminderCard.setVisibility(View.GONE);
					reminderCard.setVisibility(View.GONE);
				} // else show both reminder and attendance
				
				HomeAttendanceAdapter attendanceAdapter = new HomeAttendanceAdapter(requireContext(), attendances);
				attendanceRecyclerView.setAdapter(attendanceAdapter);
				LinearLayoutManager manager = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
				attendanceRecyclerView.setLayoutManager(manager);
				attendanceRecyclerView.setItemAnimator(new DefaultItemAnimator());
			}
			
		} else { // no attendance item exists
			
			attendanceRecyclerView.setVisibility(View.INVISIBLE);
			highAttendanceCard.setVisibility(View.INVISIBLE);
			emptyAttendanceCard.setVisibility(View.VISIBLE);
			
		}
		
	}
	
	private void populateDataAndSetAdapter(View view) {
		
		RecyclerView imageRecyclerView = view.findViewById(R.id.homeImageRecyclerView);
		RecyclerView videoRecyclerView = view.findViewById(R.id.homeVideoRecyclerView);
		RecyclerView docsRecyclerView = view.findViewById(R.id.homeDocsRecyclerView);
		
		ConstraintLayout imageLayout = view.findViewById(R.id.homeImageLayout);
		ConstraintLayout docsLayout = view.findViewById(R.id.homeDocsLayout);
		ConstraintLayout videoLayout = view.findViewById(R.id.homeVideosLayout);
		
		LinearLayout emptyLayout = view.findViewById(R.id.homeEmptyLayout);
		
		View imageDocsDivider = view.findViewById(R.id.homeImageDocsDivider);
		View docsVideoDivider = view.findViewById(R.id.homeDocsVideoDivider);
		
		addRecursively(noteFolder);
		
		Collections.sort(notes, new Comparator<FileItem>() {
			@Override
			public int compare(FileItem o1, FileItem o2) {
				return o2.getDateModified().compareTo(o1.getDateModified());
			}
		});
		
		for (FileItem fileItem : notes) {
			
			if (fileItem.getType() == FileType.FILE_TYPE_IMAGE && imagesList.size() < 9) {
				
				imagesList.add(fileItem);
				
			} else if (fileItem.getType() == FileType.FILE_TYPE_VIDEO && videosList.size() < 9) {
				
				videosList.add(fileItem);
				
			} else if ((fileItem.getType() == FileType.FILE_TYPE_APPLICATION || fileItem.getType() == FileType.FILE_TYPE_TEXT || fileItem.getType() == FileType.FILE_TYPE_OTHER) && docsList.size() < 9) {
				
				docsList.add(fileItem);
				
			}
		}
		
		GridLayoutManager imageManager = new GridLayoutManager(getContext(), 3);
		GridLayoutManager videoManager = new GridLayoutManager(getContext(), 3);
		
		imageRecyclerView.setLayoutManager(imageManager);
		HomeMediaAdapter imageAdapter = new HomeMediaAdapter(getActivity(), imagesList, new HomeMediaAdapter.HomeMediaClickListener() {
			@Override
			public void onClick(int position) {
				Bundle bundle = new Bundle();
				bundle.putString("State", "Home");
				bundle.putParcelableArrayList("HomeMedia", imagesList);
				bundle.putInt("Position", position);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_home_to_mediaActivity, bundle);
			}
		});
		imageRecyclerView.setAdapter(imageAdapter);
		
		videoRecyclerView.setLayoutManager(videoManager);
		HomeMediaAdapter videoAdapter = new HomeMediaAdapter(getActivity(), videosList, new HomeMediaAdapter.HomeMediaClickListener() {
			@Override
			public void onClick(int position) {
				Bundle bundle = new Bundle();
				bundle.putString("State", "Home");
				bundle.putParcelableArrayList("HomeMedia", videosList);
				bundle.putInt("Position", position);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_home_to_mediaActivity, bundle);
			}
		});
		videoRecyclerView.setAdapter(videoAdapter);
		
		docsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		NotesAdapter docsAdapter = new NotesAdapter(getActivity(), docsList, this, false);
		docsRecyclerView.setAdapter(docsAdapter);
		
		if (imagesList.isEmpty()) {
			imageLayout.setVisibility(View.GONE);
			imageDocsDivider.setVisibility(View.GONE);
		}
		if (docsList.isEmpty()) {
			docsLayout.setVisibility(View.GONE);
			imageDocsDivider.setVisibility(View.GONE);
			if (imagesList.isEmpty()) {
				docsVideoDivider.setVisibility(View.GONE);
			}
		}
		if (videosList.isEmpty()) {
			docsVideoDivider.setVisibility(View.GONE);
			videoLayout.setVisibility(View.GONE);
		}
		
		if (imagesList.isEmpty() && docsList.isEmpty() && videosList.isEmpty()) {
			view.findViewById(R.id.recentTextView).setVisibility(View.GONE);
			imageLayout.setVisibility(View.GONE);
			imageDocsDivider.setVisibility(View.GONE);
			docsLayout.setVisibility(View.GONE);
			docsVideoDivider.setVisibility(View.GONE);
			videoLayout.setVisibility(View.GONE);
			
			emptyLayout.setVisibility(View.VISIBLE);
		} else {
			emptyLayout.setVisibility(View.INVISIBLE);
		}
		
	}
	
	private void addRecursively(File folder) {
		FileItem item = new FileItem(folder.getPath());
		if (folder.exists()) {
			if (folder.isDirectory()) {
				File[] files = folder.listFiles();
				if (files != null && files.length > 0) {
					for (File file : files) {
						addRecursively(file);
					}
				}
			} else {
				notes.add(item);
			}
		}
	}
	
	@Override
	public void onClick(int position) {
		FileUtils.openFile(requireContext(), docsList.get(position));
	}
	
	@Override
	public void onLongClick(int position) {
	
	}
	
	@Override
	public void onOptionsClick(View view, int position) {
	
	}
}

package com.studypartner.fragments;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


public class MediaFragment extends Fragment {
	
	private String mediaPath;
	private SimpleExoPlayer player;
	private PlayerView videoPlayerView;
	private PlayerView audioPlayerView;
	private FileItem mediaFileItem;
	private PhotoView photoView;
	
	public MediaFragment() {
	}
	
	public static MediaFragment newInstance(String path) {
		
		Bundle bundle = new Bundle();
		
		MediaFragment fragment = new MediaFragment();
		bundle.putString("MediaPath", path);
		fragment.setArguments(bundle);
		
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_media, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		videoPlayerView = view.findViewById(R.id.video_view);
		audioPlayerView = view.findViewById(R.id.audio_view);
		mediaPath = getArguments().getString("MediaPath");
		mediaFileItem = new FileItem(mediaPath);
		photoView = view.findViewById(R.id.photo_view);
		
		if (mediaFileItem.getType().equals(FileType.FILE_TYPE_IMAGE)) {
			videoPlayerView.setVisibility(View.GONE);
			audioPlayerView.setVisibility(View.GONE);
			photoView.setVisibility(View.VISIBLE);
			Glide.with(requireContext())
					.load(mediaPath)
					.into(photoView);
		} else {
			photoView.setVisibility(View.GONE);
			initializePlayer();
		}
	}
	
	private void initializePlayer() {
		
		MediaSource mediaSource;
		
		player = new SimpleExoPlayer.Builder(getContext()).build();
		
		DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), "Media");
		
		mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediaPath));
		
		player.setPlayWhenReady(false);
		player.prepare(mediaSource);
		
		if (mediaFileItem.getType().equals(FileType.FILE_TYPE_VIDEO)) {
			audioPlayerView.setVisibility(View.GONE);
			videoPlayerView.setVisibility(View.VISIBLE);
			if (videoPlayerView != null)
				videoPlayerView.setPlayer(player);
		} else {
			videoPlayerView.setVisibility(View.GONE);
			audioPlayerView.setVisibility(View.VISIBLE);
			
			GradientDrawable gradientDrawable = new GradientDrawable(
					GradientDrawable.Orientation.TOP_BOTTOM,
					new int[]{ContextCompat.getColor(requireContext(), R.color.audioColor1),
							ContextCompat.getColor(requireContext(), R.color.audioColor2),
							ContextCompat.getColor(requireContext(), R.color.audioColor3),
							ContextCompat.getColor(requireContext(), R.color.audioColor4),
							ContextCompat.getColor(requireContext(), R.color.audioColor5)});
			audioPlayerView.findViewById(R.id.audio_controller_bg).setBackground(gradientDrawable);
			
			if (audioPlayerView != null)
				audioPlayerView.setPlayer(player);
			
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (player != null)
			player.stop();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (!mediaFileItem.getType().equals(FileType.FILE_TYPE_IMAGE))
			initializePlayer();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		if (player != null)
			player.release();
	}
}

package com.studypartner.fragments;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SEARCH_SERVICE;

public class NotesSearchFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "NotesSearchFragment";
	
	public SearchView mSearchView;
	
	private ArrayList<FileItem> notes, notesCopy;
	
	private NotesAdapter mNotesAdapter;
	
	public NotesSearchFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_notes_search, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
		
		if (getArguments() != null) {
			FileItem[] files = (FileItem[]) getArguments().getParcelableArray("NotesArray");
			assert files != null;
			notes = new ArrayList<>(Arrays.asList(files));
		}
		
		notesCopy = new ArrayList<>(notes);
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				NavHostFragment.findNavController(NotesSearchFragment.this).navigateUp();
			}
		});
		
		requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		((MainActivity) requireActivity()).mBottomAppBar.performHide();
		((MainActivity) requireActivity()).mBottomAppBar.setVisibility(View.GONE);
		((MainActivity) requireActivity()).fab.hide();
		
		RecyclerView recyclerView = view.findViewById(R.id.notesSearchRecyclerView);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		mNotesAdapter = new NotesAdapter(requireActivity(), notesCopy, this, false);
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onPause() {
		mSearchView.clearFocus();
		setHasOptionsMenu(false);
		super.onPause();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notes_menu_search, menu);
		
		SearchManager searchManager = (SearchManager) requireActivity().getSystemService(SEARCH_SERVICE);
		
		mSearchView = (SearchView) menu.findItem(R.id.notes_menu_search_frag).getActionView();
		SearchableInfo searchableInfo = searchManager.getSearchableInfo(requireActivity().getComponentName());
		mSearchView.setSearchableInfo(searchableInfo);
		mSearchView.setQueryHint("Enter the name of the file");
		mSearchView.setIconified(false);
		
		int searchPlateId = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		EditText searchPlate = mSearchView.findViewById(searchPlateId);
		searchPlate.setTextColor(getResources().getColor(R.color.colorAccent, requireActivity().getTheme()));
		searchPlate.setHintTextColor(getResources().getColor(R.color.colorAccent, requireActivity().getTheme()));
		
		mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				mNotesAdapter.filter(newText);
				return true;
			}
		});
		
		mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				Log.d(TAG, "onClose: called");
				mSearchView.clearFocus();
				return false;
			}
		});
	}
	
	@Override
	public void onClick(int position) {
		
		FileItem fileDesc = notesCopy.get(position);
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE);
		sharedPreferences.edit().putBoolean("NotesSearchExists", true).apply();
		sharedPreferences.edit().putString("NotesSearch", fileDesc.getPath()).apply();
		mSearchView.clearFocus();
		((MainActivity) requireActivity()).mNavController.navigateUp();
		
	}
	
	@Override
	public void onLongClick(int position) {
	
	}
	
	@Override
	public void onOptionsClick(View view, int position) {
	
	}
}

package com.studypartner.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.BuildConfig;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.content.Context.MODE_PRIVATE;

public class FileFragment extends Fragment implements NotesAdapter.NotesClickListener {
	private static final String TAG = "FileFragment";
	
	private final String SORT_BY_NAME = "By Name";
	private final String SORT_BY_SIZE = "By Size";
	private final String SORT_BY_CREATION_TIME = "By Creation Time";
	private final String SORT_BY_MODIFIED_TIME = "By Modification Time";
	
	private final String ASCENDING_ORDER = "Ascending Order";
	private final String DESCENDING_ORDER = "Descending Order";
	
	private final int RECORD_PERMISSION_REQUEST_CODE = 10;
	private final int CAMERA_PERMISSION_REQUEST_CODE = 30;
	
	private final int RECORD_REQUEST_CODE = 11;
	private final int IMAGE_REQUEST_CODE = 22;
	private final int CAMERA_IMAGE_REQUEST_CODE = 33;
	private final int DOC_REQUEST_CODE = 44;
	private final int VIDEO_REQUEST_CODE = 55;
	private final int AUDIO_REQUEST_CODE = 66;
	
	private File audioFile;
	
	private String sortBy;
	private String sortOrder;
	
	private LinearLayout mEmptyLayout;
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private File noteFolder;
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mNotesAdapter;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> notes = new ArrayList<>();
	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	
	private InterstitialAd mInterstitialAd;
	
	public FileFragment() {
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		
		if (requestCode == RECORD_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				recordAudio();
			}
		} else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				openCameraForImage();
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if (sharedPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sharedPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sharedPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		} else {
			editor.putBoolean("SORTING_ORDER_EXISTS", true);
			editor.putString("SORTING_BY", SORT_BY_NAME);
			editor.putString("SORTING_ORDER", ASCENDING_ORDER);
			editor.apply();
			sortBy = SORT_BY_NAME;
			sortOrder = ASCENDING_ORDER;
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView: starts");
		
		final View rootView = inflater.inflate(R.layout.fragment_file, container, false);
		
		if (getArguments() != null) {
			String filePath = getArguments().getString("FilePath");
			if (filePath != null) {
				noteFolder = new File(filePath);
			}
		}
		
		final MainActivity activity = (MainActivity) requireActivity();
		activity.fab.hide();
		activity.mBottomAppBar.performHide();
		activity.mBottomAppBar.setVisibility(View.GONE);
		
		Toolbar toolbar = activity.mToolbar;
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				Log.d(TAG, "handleOnBackPressed: starts");
				activity.mNavController.navigateUp();
			}
		});
		
		fab = rootView.findViewById(R.id.fileFab);
		
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: fab onclick called");
				final BottomSheetDialog bottomSheet = new BottomSheetDialog(requireContext());
				bottomSheet.setDismissWithAnimation(true);
				bottomSheet.setContentView(R.layout.bottom_sheet_notes);
				LinearLayout addFolder = bottomSheet.findViewById(R.id.addFolder);
				LinearLayout addFile = bottomSheet.findViewById(R.id.addFile);
				LinearLayout addImage = bottomSheet.findViewById(R.id.addImage);
				LinearLayout addVideo = bottomSheet.findViewById(R.id.addVideo);
				LinearLayout addCamera = bottomSheet.findViewById(R.id.addCamera);
				LinearLayout addNote = bottomSheet.findViewById(R.id.addNote);
				final LinearLayout addLink = bottomSheet.findViewById(R.id.addLink);
				final LinearLayout addAudio = bottomSheet.findViewById(R.id.addAudio);
				LinearLayout addVoice = bottomSheet.findViewById(R.id.addVoice);
				
				assert addFolder != null;
				addFolder.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addFolder();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addFile != null;
				addFile.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getDocument();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addImage != null;
				addImage.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getImage();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addVideo != null;
				addVideo.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getVideo();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addCamera != null;
				addCamera.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
						} else {
							
							openCameraForImage();
							
						}
						
						bottomSheet.dismiss();
					}
				});
				
				assert addNote != null;
				addNote.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addNote();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addLink != null;
				addLink.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						addLink();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addAudio != null;
				addAudio.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						getAudio();
						
						bottomSheet.dismiss();
					}
				});
				
				assert addVoice != null;
				addVoice.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_PERMISSION_REQUEST_CODE);
						} else {
							
							recordAudio();
							
						}
						
						bottomSheet.dismiss();
					}
				});
				
				bottomSheet.show();
			}
		});
		
		mEmptyLayout = rootView.findViewById(R.id.fileEmptyLayout);
		recyclerView = rootView.findViewById(R.id.fileRecyclerView);
		mLinearLayout = rootView.findViewById(R.id.fileLinearLayout);
		sortText = rootView.findViewById(R.id.fileSortText);
		sortOrderButton = rootView.findViewById(R.id.fileSortOrder);
		sortByButton = rootView.findViewById(R.id.fileSortButton);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setItemViewCacheSize(20);
		recyclerView.setDrawingCacheEnabled(true);
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		sortOrderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (sortOrder.equals(ASCENDING_ORDER)) {
					sortOrder = DESCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
				} else {
					sortOrder = ASCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
				}
				editor.putString("SORTING_ORDER", sortOrder).apply();
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			}
		});
		
		sortByButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
				
				View dialogView = getLayoutInflater().inflate(R.layout.notes_sort_layout, null);
				
				Button okButton = dialogView.findViewById(R.id.sortByOkButton);
				Button cancelButton = dialogView.findViewById(R.id.sortByCancelButton);
				final RadioGroup radioGroup = dialogView.findViewById(R.id.sortByRadioGroup);
				radioGroup.clearCheck();
				
				switch (sortBy) {
					case SORT_BY_SIZE:
						radioGroup.check(R.id.sortBySizeRB);
						break;
					case SORT_BY_CREATION_TIME:
						radioGroup.check(R.id.sortByCreationTimeRB);
						break;
					case SORT_BY_MODIFIED_TIME:
						radioGroup.check(R.id.sortByModifiedTimeRB);
						break;
					default:
						radioGroup.check(R.id.sortByNameRB);
						break;
				}
				
				cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d(TAG, "onClick: cancel pressed while changing subject");
						builder.dismiss();
					}
				});
				
				okButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (radioGroup.getCheckedRadioButtonId()) {
							case R.id.sortBySizeRB:
								sortBy = SORT_BY_SIZE;
								break;
							case R.id.sortByCreationTimeRB:
								sortBy = SORT_BY_CREATION_TIME;
								break;
							case R.id.sortByModifiedTimeRB:
								sortBy = SORT_BY_MODIFIED_TIME;
								break;
							default:
								sortBy = SORT_BY_NAME;
								break;
						}
						sortText.setText(sortBy);
						editor.putString("SORTING_BY", sortBy).apply();
						sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
						builder.dismiss();
					}
				});
				
				builder.setView(dialogView);
				builder.show();
			}
		});
		
		toolbar.setTitle(getTitle());
		
		mInterstitialAd = new InterstitialAd(requireContext());
		mInterstitialAd.setAdUnitId(getString(R.string.notes_interstitial_ad));
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				mInterstitialAd.loadAd(new AdRequest.Builder().build());
			}
		});
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE);
		
		if (sharedPreferences.getBoolean("NotesSearchExists", false)) {
			File searchedFile = new File(sharedPreferences.getString("NotesSearch", null));
			FileItem fileDesc = new FileItem(searchedFile.getPath());
			if (fileDesc.getType() == FileType.FILE_TYPE_FOLDER) {
				Bundle bundle = new Bundle();
				bundle.putString("FilePath", fileDesc.getPath());
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_self, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_VIDEO || fileDesc.getType() == FileType.FILE_TYPE_IMAGE || fileDesc.getType() == FileType.FILE_TYPE_AUDIO) {
				Bundle bundle = new Bundle();
				bundle.putString("State", "Files");
				bundle.putString("Media", fileDesc.getPath());
				bundle.putBoolean("InStarred", false);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_to_mediaActivity, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_LINK) {
				FileUtils.openLink(requireContext(), fileDesc);
			} else {
				FileUtils.openFile(requireContext(), fileDesc);
			}
		}
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		}
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		setHasOptionsMenu(true);
		
		fab.show();
	}
	
	@Override
	public void onPause() {
		if (actionMode != null) {
			actionMode.finish();
		}
		Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
		
		setHasOptionsMenu(false);
		
		super.onPause();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notes_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: starts");
		switch (item.getItemId()) {
			case R.id.notes_menu_refresh:
				populateDataAndSetAdapter();
				return true;
			case R.id.notes_menu_search:
				Bundle bundle = new Bundle();
				FileItem[] files = new FileItem[notes.size()];
				files = notes.toArray(files);
				bundle.putParcelableArray("NotesArray", files);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_to_notesSearchFragment, bundle);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			enableActionMode(position);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			FileItem fileDesc = notes.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_self, bundle);
		} else if (notes.get(position).getType().equals(FileType.FILE_TYPE_VIDEO) || notes.get(position).getType().equals(FileType.FILE_TYPE_AUDIO) || notes.get(position).getType() == FileType.FILE_TYPE_IMAGE) {
			Bundle bundle = new Bundle();
			bundle.putString("State", "Files");
			bundle.putString("Media", notes.get(position).getPath());
			
			bundle.putBoolean("InStarred", false);
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_fileFragment_to_mediaActivity, bundle);
		} else if (notes.get(position).getType() == FileType.FILE_TYPE_LINK) {
			FileUtils.openLink(requireContext(), notes.get(position));
		} else {
			FileUtils.openFile(requireContext(), notes.get(position));
		}
	}
	
	@Override
	public void onLongClick(int position) {
		enableActionMode(position);
	}
	
	@Override
	public void onOptionsClick(View view, final int position) {
		PopupMenu popup = new PopupMenu(getContext(), view);
		if (starredIndex(position) != -1) {
			popup.inflate(R.menu.notes_item_menu_unstar);
		} else {
			popup.inflate(R.menu.notes_item_menu_star);
		}
		
		if (notes.get(position).getType() == FileType.FILE_TYPE_FOLDER) {
			popup.getMenu().removeItem(R.id.notes_item_share);
		}
		
		if (notes.get(position).getType() == FileType.FILE_TYPE_LINK) {
			popup.getMenu().getItem(0).setTitle("Edit Link");
		}
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
					
					case R.id.notes_item_rename:
						
						final FileItem fileItem = notes.get(position);

						final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						final View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
						alertDialog.setView(dialogView);

						Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
						Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
						final TextView titleDialog = dialogView.findViewById(R.id.notesAddLinkTitle);

						final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);

						if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
							titleDialog.setText("Edit this link");
						} else {
							titleDialog.setText("Enter a new name");
						}
						
						String extension = "";
						if (fileItem.getType() == FileType.FILE_TYPE_FOLDER || fileItem.getType() == FileType.FILE_TYPE_LINK) {
							nameTextInput.getEditText().setText(fileItem.getName());
						} else {
							String name = fileItem.getName();
							if (name.indexOf(".") > 0) {
								extension = name.substring(name.lastIndexOf("."));
								name = name.substring(0, name.lastIndexOf("."));
							}
							nameTextInput.getEditText().setText(name);
						}

						final String finalExtension = extension;
						okButton.setOnClickListener(new View.OnClickListener() {
							public void onClick(View view) {
								if (mInterstitialAd.isLoaded()) {
									
									mInterstitialAd.show();
								}
								
								String newName = nameTextInput.getEditText().getText().toString().trim();
								File oldFile = new File(fileItem.getPath());
								File newFile = new File(noteFolder, newName + finalExtension);
								if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
									if (newName.equals(fileItem.getName()) || newName.equals("")) {
										Log.d(TAG, "onClick: link not changed");
									} else if (!FileUtils.isValidUrl(newName)) {
										StyleableToast.makeText(getContext(), "Link is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
									} else {
										
										int linkIndex = linkIndex(position);
										if (linkIndex != -1) {
											links.get(linkIndex).setName(newName);
											
											SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
											SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
											
											Gson gson = new Gson();
											linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
											linkPreferenceEditor.apply();
											
											int starredIndex = starredIndex(position);
											if (starredIndex != -1) {
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
												
												starred.get(starredIndex).setName(newName);
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											notes.get(position).setName(newName);
											
											mNotesAdapter.notifyItemChanged(position);
											sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
										}
										
									}
								} else {
									if (newFile.getName().equals(fileItem.getName()) || newName.equals("")) {
										Log.d(TAG, "onClick: filename not changed");
									} else if (newFile.exists()) {
										StyleableToast.makeText(getContext(), "File with this name already exists", Toast.LENGTH_SHORT, R.style.designedToast).show();
									} else if (newName.contains("/")) {
										StyleableToast.makeText(getContext(), "File name is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
									} else {
										if (oldFile.renameTo(newFile)) {
											StyleableToast.makeText(getContext(), "File renamed successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
											
											int starredIndex = starredIndex(position);
											if (starredIndex != -1) {
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
												
												starred.get(starredIndex).setName(newFile.getName());
												starred.get(starredIndex).setPath(newFile.getPath());
												Gson gson = new Gson();
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											
											if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
												for (FileItem starItem : starred) {
													if (starItem.getPath().contains(oldFile.getPath())) {
														starItem.setPath(starItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
													}
												}
												for (FileItem linkItem : links) {
													if (linkItem.getPath().contains(oldFile.getPath())) {
														linkItem.setPath(linkItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
													}
												}
												
												SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
												SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
												
												Gson gson = new Gson();
												linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
												linkPreferenceEditor.apply();
												
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
												
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											notes.get(position).setName(newFile.getName());
											notes.get(position).setPath(newFile.getPath());
											
											mNotesAdapter.notifyItemChanged(position);
											populateDataAndSetAdapter();
											sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
										} else {
											StyleableToast.makeText(getContext(), "File could not be renamed", Toast.LENGTH_SHORT, R.style.designedToast).show();
										}
									}
								}
								alertDialog.dismiss();
							}
						});
						cancelButton.setOnClickListener(new View.OnClickListener() {
							public void onClick(View view) {
								alertDialog.cancel();
							}
						});
						
						alertDialog.show();
						return true;
					
					case R.id.notes_item_star:

						addToStarred( position );
						return true;
					
					case R.id.notes_item_unstar:
						
						int unstarredIndex = starredIndex(position);
						if (unstarredIndex != -1) {
							SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
							SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
							
							for (int i = 0; i < starred.size(); i++) {
								FileItem starItem = starred.get(i);
								if (starItem.isStarred() && starItem.getPath().equals(notes.get(position).getPath())) {
									starred.remove(i);
									break;
								}
							}
							notes.get(position).setStarred(false);
							if (starred.size() == 0) {
								starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
							}
							Gson gson = new Gson();
							starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
							starredPreferenceEditor.apply();
							mNotesAdapter.notifyItemChanged(position);
						} else {
							StyleableToast.makeText(getContext(), "You are some sort of wizard aren't you", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
						
						return true;
					
					case R.id.notes_item_delete:

						AlertDialog.Builder builder = new AlertDialog.Builder(
								getContext());
						View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

						Button yesButton=view.findViewById(R.id.yes_button);
						Button noButton=view.findViewById(R.id.no_button);
						TextView title = view.findViewById(R.id.title_dialog);
						TextView detail = view.findViewById(R.id.detail_dialog);
						title.setText("Delete File");
						detail.setText("Are you sure you want to delete the file?");

						builder.setView(view);

						final AlertDialog dialog= builder.create();
						dialog.setCanceledOnTouchOutside(true);

						yesButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								dialog.dismiss();

								// Hiding the file to be deleted from RecyclerView
								recyclerView.findViewHolderForAdapterPosition(position).itemView.
										findViewById(R.id.noteItemLayout).setVisibility(View.GONE);

								// Displaying a Snackbar to allow user to UNDO the delete file action
								Snackbar undoSnackbar = Snackbar.make(recyclerView, R.string.file_action_deleted, Snackbar.LENGTH_LONG);
								undoSnackbar.setAction(R.string.notes_action_undo, new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										// Making file visible to user again
										recyclerView.findViewHolderForAdapterPosition(position).itemView.
												findViewById(R.id.noteItemLayout).setVisibility(View.VISIBLE);
									}

								}).setDuration(3000).setActionTextColor(getResources().getColor(R.color.colorPrimary));

								undoSnackbar.addCallback(new Snackbar.Callback() {

									// Called when the Snackbar is dismissed by an event other than
									// clicking of UNDO.
									@Override
									public void onDismissed(Snackbar snackbar, int event) {
										if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
												event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
												event == Snackbar.Callback.DISMISS_EVENT_MANUAL) {

											if (mInterstitialAd.isLoaded()) {
												mInterstitialAd.show();
											}

											if (notes.get(position).getType() != FileType.FILE_TYPE_LINK) {
												File file = new File(notes.get(position).getPath());
												deleteRecursive(file);

												if (notes.get(position).getType() == FileType.FILE_TYPE_FOLDER) {
													ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
													for (FileItem linkItem : links) {
														if (linkItem.getPath().contains(file.getPath())) {
															linksToBeRemoved.add(linkItem);
														}
													}
													links.removeAll(linksToBeRemoved);

													SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
													SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

													if (links.size() == 0) {
														linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
													}
													Gson gson = new Gson();
													linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
													linkPreferenceEditor.apply();

													ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
													for (FileItem starItem : starred) {
														if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
															starredToBeRemoved.add(starItem);
														}
													}
													starred.removeAll(starredToBeRemoved);

													SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
													SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

													if (starred.size() == 0) {
														starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
													}
													starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
													starredPreferenceEditor.apply();
												}

											} else {
												int linkPosition = linkIndex(position);
												if (linkPosition != -1) {
													links.remove(linkPosition);
													SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
													SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

													if (links.size() == 0) {
														linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
													}
													Gson gson = new Gson();
													linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
													linkPreferenceEditor.apply();
												}
											}
											int starPosition = starredIndex(position);
											if (starPosition != -1) {
												starred.remove(starPosition);
												SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
												SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

												if (starred.size() == 0) {
													starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
												}
												Gson gson = new Gson();
												starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
												starredPreferenceEditor.apply();
											}
											mNotesAdapter.notifyItemRemoved(position);
											notes.remove(position);

											if (notes.isEmpty()) {
												mEmptyLayout.setVisibility(View.VISIBLE);
											}
										}
									}
								});
								undoSnackbar.show(); // Snackbar will appear for 3 seconds
							}
						});
						noButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
						dialog.show();
						return true;
					
					case R.id.notes_item_share:
						
						if (notes.get(position).getType() == FileType.FILE_TYPE_LINK) {
							
							Intent intentShareFile = new Intent(Intent.ACTION_SEND);
							intentShareFile.setType("text/plain");
							intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application\n" + notes.get(position).getName());
							startActivity(Intent.createChooser(intentShareFile, "Share File"));
							
						} else if (notes.get(position).getType() != FileType.FILE_TYPE_FOLDER) {
							
							Intent intentShareFile = new Intent(Intent.ACTION_SEND);
							File shareFile = new File(notes.get(position).getPath());
							ArrayList<FileItem> fileItems = new ArrayList<>();
							fileItems.add(notes.get(position));
							if (shareFile.exists()) {
								intentShareFile.setType(FileUtils.getFileType(fileItems));
								intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(notes.get(position).getPath())));
								intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
								intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
								startActivity(Intent.createChooser(intentShareFile, "Share File"));
								
							}
						} else {
							StyleableToast.makeText(getContext(), "Folder cannot be shared", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
						return true;
					default:
						return false;
				}
			}
		});
		popup.show();
	}
	
	private void populateDataAndSetAdapter() {
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		File[] files = noteFolder.listFiles();
		
		if (files != null && files.length > 0) {
			notes = new ArrayList<>();
			for (File f : files) {
				FileItem item = new FileItem(f.getPath());
				if (isStarred(item)) {
					item.setStarred(true);
				}
				notes.add(item);
			}
		}
		
		for (FileItem link : links) {
			File linkFile = new File(link.getPath());
			if (linkFile.getParent().equals(noteFolder.getPath())) {
				if (isStarred(link)) {
					link.setStarred(true);
				}
				notes.add(link);
			}
		}
		
		if (notes.isEmpty()) {
			mEmptyLayout.setVisibility(View.VISIBLE);
		}
		
		mNotesAdapter = new NotesAdapter(requireActivity(), notes, this, true);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		recyclerView.setAdapter(mNotesAdapter);
	}
	
	private boolean isStarred(FileItem item) {
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(item.getPath())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private int starredIndex(int position) {
		int index = -1;
		if (starred != null) {
			for (int i = 0; i < starred.size(); i++) {
				FileItem starredItem = starred.get(i);
				if (starredItem.getPath().equals(notes.get(position).getPath())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	private int linkIndex(int position) {
		int index = -1;
		if (links != null) {
			for (int i = 0; i < links.size(); i++) {
				FileItem linkItem = links.get(i);
				if (linkItem.getPath().equals(notes.get(position).getPath())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			File[] files = fileOrDirectory.listFiles();
			if (files != null && files.length > 0) {
				for (File file : files) {
					deleteRecursive(file);
				}
			}
		}
		Log.d(TAG, "deleteRecursive: " + fileOrDirectory.delete());
	}
	
	private String getTitle() {
		
		String title = "Notes";
		
		FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		
		if (firebaseUser != null) {
			title = noteFolder.getPath().substring(noteFolder.getPath().indexOf(firebaseUser.getUid()) + firebaseUser.getUid().length() + 1);
		}
		
		return title.length() > 15 ? "..." + title.substring(title.length() - 12) : title;
	}
	
	private void enableActionMode(int position) {
		if (actionMode == null) {
			
			actionMode = requireActivity().startActionMode(new android.view.ActionMode.Callback2() {
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.menu_notes_action_mode, menu);
					MenuItem unStarred = menu.findItem(R.id.notes_action_unstar);
					unStarred.setIcon(R.drawable.starred_icon);
					actionModeOn = true;
					fab.hide();
					mLinearLayout.setVisibility(View.GONE);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
					return true;
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.notes_action_delete:
							deleteRows();
							mode.finish();
							return true;
						case R.id.notes_action_share:
							shareRows();
							mode.finish();
							return true;
						case R.id.notes_action_select_all:
							selectAll();
							return true;
						case R.id.notes_action_unstar:
							addToStarred(mNotesAdapter.getSelectedItems());
							mode.finish();
							return true;
						default:
							return false;
					}
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mNotesAdapter.clearSelections();
					actionModeOn = false;
					actionMode = null;
					mLinearLayout.setVisibility(View.VISIBLE);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
					fab.show();
				}
			});
		}
		toggleSelection(position);
	}
	
	private void toggleSelection(int position) {
		mNotesAdapter.toggleSelection(position);
		int count = mNotesAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
			actionMode = null;
		} else {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
	}
	
	private void selectAll() {
		mNotesAdapter.selectAll();
		int count = mNotesAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
		} else if (actionMode != null) {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
		
		actionMode.invalidate();
	}
	
	private void deleteRows() {
		final ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();

		AlertDialog.Builder builder = new AlertDialog.Builder(
				getContext());
		View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

		Button yesButton=view.findViewById(R.id.yes_button);
		Button noButton=view.findViewById(R.id.no_button);
		TextView title = view.findViewById(R.id.title_dialog);
		TextView detail = view.findViewById(R.id.detail_dialog);
		title.setText("Delete Files");
		detail.setText("Are you sure you want to delete " + selectedItemPositions.size() + " files?");

		builder.setView(view);

		final AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(true);

		yesButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				dialog.dismiss();

				// Hiding selected folders to be deleted from RecyclerView
				for (Integer folderPosition : selectedItemPositions) {
					recyclerView.findViewHolderForAdapterPosition(folderPosition).itemView.
							findViewById(R.id.noteItemLayout).setVisibility(View.GONE);
				}

				// Displaying a Snackbar to allow user to UNDO the delete folder action
				Snackbar undoSnackbar = Snackbar.make(recyclerView, R.string.multiple_files_deleted, Snackbar.LENGTH_LONG);
				undoSnackbar.setAction(R.string.notes_action_undo, new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// Making selected folders visible to user again
						for (Integer folderPosition : selectedItemPositions) {
							recyclerView.findViewHolderForAdapterPosition(folderPosition).itemView.
									findViewById(R.id.noteItemLayout).setVisibility(View.VISIBLE);
						}
					}

				}).setDuration(3000).setActionTextColor(getResources().getColor(R.color.colorPrimary));

				undoSnackbar.addCallback(new Snackbar.Callback() {

					// Called when the Snackbar is dismissed by an event other than
					// clicking of UNDO.
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
								event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
								event == Snackbar.Callback.DISMISS_EVENT_MANUAL) {

							if (mInterstitialAd.isLoaded()) {
								mInterstitialAd.show();
							}
							for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
								if (notes.get(selectedItemPositions.get(i)).getType() != FileType.FILE_TYPE_LINK) {
									File file = new File(notes.get(selectedItemPositions.get(i)).getPath());
									deleteRecursive(file);

									if (notes.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_FOLDER) {
										ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
										for (FileItem linkItem : links) {
											if (linkItem.getPath().contains(file.getPath())) {
												linksToBeRemoved.add(linkItem);
											}
										}
										links.removeAll(linksToBeRemoved);

										SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
										SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

										if (links.size() == 0) {
											linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
										}
										Gson gson = new Gson();
										linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
										linkPreferenceEditor.apply();

										ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
										for (FileItem starItem : starred) {
											if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
												starredToBeRemoved.add(starItem);
											}
										}
										starred.removeAll(starredToBeRemoved);

										SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
										SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

										if (starred.size() == 0) {
											starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
										}
										starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
										starredPreferenceEditor.apply();
									}
								} else {
									int linkPosition = linkIndex(selectedItemPositions.get(i));
									if (linkPosition != -1) {
										links.remove(linkPosition);
										SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
										SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

										if (links.size() == 0) {
											linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
										}
										Gson gson = new Gson();
										linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
										linkPreferenceEditor.apply();
									}
								}

								int starPosition = starredIndex(selectedItemPositions.get(i));
								if (starPosition != -1) {
									starred.remove(starPosition);
									SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
									SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

									if (starred.size() == 0) {
										starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
									}
									Gson gson = new Gson();
									starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
									starredPreferenceEditor.apply();
								}

								mNotesAdapter.notifyItemRemoved(selectedItemPositions.get(i));
								notes.remove(selectedItemPositions.get(i).intValue());
							}
							if (notes.isEmpty()) {
								mEmptyLayout.setVisibility(View.VISIBLE);
							}
						}
					}
				});
				undoSnackbar.show(); // Snackbar will appear for 3 seconds
			}
		});
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
		actionMode = null;
	}
	
	private void shareRows() {
		ArrayList<Integer> selectedItemPositions = mNotesAdapter.getSelectedItems();
		ArrayList<Integer> positionsToBeRemoved = new ArrayList<>();
		for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
			if (FileUtils.getFileType(new File(notes.get(selectedItemPositions.get(i)).getPath())) == FileType.FILE_TYPE_FOLDER) {
				positionsToBeRemoved.add(i);
			} else if (notes.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_LINK) {
				positionsToBeRemoved.add(i);
			}
		}
		
		selectedItemPositions.removeAll(positionsToBeRemoved);
		
		ArrayList<FileItem> fileItems = new ArrayList<>();
		ArrayList<Uri> fileItemsUri = new ArrayList<>();
		
		for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
			fileItems.add(notes.get(selectedItemPositions.get(i)));
			fileItemsUri.add(FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(notes.get(selectedItemPositions.get(i)).getPath())));
		}
		
		Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intentShareFile.setType(FileUtils.getFileType(fileItems));
		intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileItemsUri);
		intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
		startActivity(Intent.createChooser(intentShareFile, "Share File"));
	}
	
	private void sort(String text, boolean ascending) {
		switch (text) {
			case SORT_BY_SIZE:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o1.getSize(), o2.getSize());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o2.getSize(), o1.getSize());
						}
					});
				}
				
				break;
			case SORT_BY_CREATION_TIME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateCreated().compareTo(o2.getDateCreated());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateCreated().compareTo(o1.getDateCreated());
						}
					});
				}
				
				break;
			case SORT_BY_MODIFIED_TIME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateModified().compareTo(o2.getDateModified());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateModified().compareTo(o1.getDateModified());
						}
					});
				}
				
				break;
			case SORT_BY_NAME:
				
				if (ascending) {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				} else {
					Collections.sort(notes, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getName().compareTo(o1.getName());
						}
					});
				}
				break;
		}
		
		mNotesAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == RECORD_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				notes.add(new FileItem(audioFile.getPath()));
				mNotesAdapter.notifyItemInserted(notes.size() - 1);
				
				if (mEmptyLayout.getVisibility() == View.VISIBLE) {
					mEmptyLayout.setVisibility(View.GONE);
				}
				
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			} else if (resultCode != Activity.RESULT_CANCELED) {
				StyleableToast.makeText(getContext(), "Audio could not be saved", Toast.LENGTH_SHORT, R.style.designedToast).show();
			}
			
		} else if (requestCode == IMAGE_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				ArrayList<Uri> imagePaths = new ArrayList<>(Objects.requireNonNull(data.<Uri>getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)));
				if (imagePaths.size() > 2) {
					if (mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
					}
				}
				
				for (Uri uri : imagePaths) {
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
					
					if (mEmptyLayout.getVisibility() == View.VISIBLE) {
						mEmptyLayout.setVisibility(View.GONE);
					}
				}
				
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			} else if (resultCode != Activity.RESULT_CANCELED) {
				StyleableToast.makeText(getContext(), "Image(s) could not be saved", Toast.LENGTH_SHORT, R.style.designedToast).show();
			}
			
		} else if (requestCode == CAMERA_IMAGE_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				notes.add(new FileItem(ImagePicker.Companion.getFilePath(data)));
				mNotesAdapter.notifyItemInserted(notes.size() - 1);
				
				if (mEmptyLayout.getVisibility() == View.VISIBLE) {
					mEmptyLayout.setVisibility(View.GONE);
				}
				
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			} else if (resultCode == ImagePicker.RESULT_ERROR) {
				StyleableToast.makeText(getContext(), "Image could not be saved", Toast.LENGTH_SHORT, R.style.designedToast).show();
			}
			
		} else if (requestCode == DOC_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				if (null != data.getClipData()) {
					Log.d(TAG, "onActivityResult: document count = " + data.getClipData().getItemCount());
					
					if (data.getClipData().getItemCount() > 2) {
						if (mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
						}
					}
					
					for (int i = 0; i < data.getClipData().getItemCount(); i++) {
						Uri uri = data.getClipData().getItemAt(i).getUri();
						String filePath = FileUtils.getFilePath(requireContext(), uri);
						notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
						mNotesAdapter.notifyItemInserted(notes.size() - 1);
						
						if (mEmptyLayout.getVisibility() == View.VISIBLE) {
							mEmptyLayout.setVisibility(View.GONE);
						}
					}
					
					sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
				} else {
					Uri uri = data.getData();
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
					
					if (mEmptyLayout.getVisibility() == View.VISIBLE) {
						mEmptyLayout.setVisibility(View.GONE);
					}
					
					sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
				}
			}
			
		} else if (requestCode == VIDEO_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				ArrayList<Uri> videoPaths = new ArrayList<>(Objects.requireNonNull(data.<Uri>getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)));
				
				if (videoPaths.size() > 2) {
					if (mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
					}
				}
				
				for (Uri uri : videoPaths) {
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					notes.add(new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath())));
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
					
					if (mEmptyLayout.getVisibility() == View.VISIBLE) {
						mEmptyLayout.setVisibility(View.GONE);
					}
				}
				
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			} else if (resultCode != Activity.RESULT_CANCELED) {
				StyleableToast.makeText(getContext(), "Video(s) could not be saved", Toast.LENGTH_SHORT, R.style.designedToast).show();
			}
			
		} else if (requestCode == AUDIO_REQUEST_CODE) {
			
			if (resultCode == Activity.RESULT_OK) {
				assert data != null;
				if (null != data.getClipData()) {
					Log.d(TAG, "onActivityResult: audio count = " + data.getClipData().getItemCount());
					
					if (data.getClipData().getItemCount() > 2) {
						if (mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
						}
					}
					
					
					for (int i = 0; i < data.getClipData().getItemCount(); i++) {
						Uri uri = data.getClipData().getItemAt(i).getUri();
						String filePath = FileUtils.getFilePath(requireContext(), uri);
						FileItem audioFileItem = new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath()));
						audioFileItem.setName(audioFileItem.getName().replaceAll(" ", "_"));
						audioFileItem.setType(FileType.FILE_TYPE_AUDIO);
						notes.add(audioFileItem);
						mNotesAdapter.notifyItemInserted(notes.size() - 1);

						if (mEmptyLayout.getVisibility() == View.VISIBLE) {
							mEmptyLayout.setVisibility(View.GONE);
						}
					}

					sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
				} else {
					Uri uri = data.getData();
					String filePath = FileUtils.getFilePath(requireContext(), uri);
					FileItem audioFileItem = new FileItem(FileUtils.copyFile(filePath, noteFolder.getPath()));
					audioFileItem.setType(FileType.FILE_TYPE_AUDIO);
					audioFileItem.setName(audioFileItem.getName().replaceAll(" ", "_"));

					notes.add(audioFileItem);
					mNotesAdapter.notifyItemInserted(notes.size() - 1);

					if (mEmptyLayout.getVisibility() == View.VISIBLE) {
						mEmptyLayout.setVisibility(View.GONE);
					}

					sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
				}
			} else if (resultCode != Activity.RESULT_CANCELED) {
				StyleableToast.makeText(getContext(), "Audio(s) could not be saved", Toast.LENGTH_SHORT, R.style.designedToast).show();
			}
			
		}
	}
	
	private void addFolder() {
		
		File file;
		
		int count = 0;
		do {
			String newFolder = "New Folder";
			if (count > 0) {
				newFolder += " " + count;
			}
			++count;
			file = new File(noteFolder, newFolder);
		} while (file.exists());


		final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
		final View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
		alertDialog.setView(dialogView);

		Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
		Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
		final TextView title = dialogView.findViewById(R.id.notesAddLinkTitle);
		title.setText("Name of the folder");

		final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);
		
		okButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String newName = nameTextInput.getEditText().getText().toString().trim();
				File newFolder = new File(noteFolder, newName);
				if (newName.isEmpty()) {
					StyleableToast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT, R.style.designedToast).show();
				} else if (newFolder.exists()) {
					StyleableToast.makeText(getContext(), "Folder with this name already exists", Toast.LENGTH_SHORT, R.style.designedToast).show();
				} else if (newName.contains("/")) {
					StyleableToast.makeText(getContext(), "Folder name is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
				} else {
					if (newFolder.mkdirs()) {
						notes.add(new FileItem(newFolder.getPath()));
						
						if (mEmptyLayout.getVisibility() == View.VISIBLE) {
							mEmptyLayout.setVisibility(View.GONE);
						}
						
						mNotesAdapter.notifyItemInserted(notes.size());
						
						sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
					} else {
						StyleableToast.makeText(requireActivity(), "Cannot create new folder", Toast.LENGTH_SHORT, R.style.designedToast).show();
					}
				}
				alertDialog.dismiss();
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				alertDialog.cancel();
			}
		});
		
		alertDialog.show();
	}
	
	private void getDocument() {
		String[] mimeTypes =
				{"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
						"application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
						"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
						"text/plain",
						"application/pdf",
						"application/zip"};
		
		Intent getDocument = new Intent(Intent.ACTION_GET_CONTENT);
		getDocument.addCategory(Intent.CATEGORY_OPENABLE);
		getDocument.setType("*/*");
		getDocument.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
		getDocument.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(Intent.createChooser(getDocument, "ChooseFile"), DOC_REQUEST_CODE);
		
	}
	
	private void getImage() {
		FilePickerBuilder.getInstance()
				.setActivityTitle("Select images")
				.enableImagePicker(true)
				.enableCameraSupport(false)
				.enableVideoPicker(false)
				.pickPhoto(this, IMAGE_REQUEST_CODE);
	}
	
	private void openCameraForImage() {
		ImagePicker.Builder builder = new ImagePicker.Builder(this);
		builder.crop();
		builder.cameraOnly();
		builder.compress(5 * 1024);
		builder.saveDir(noteFolder);
		builder.start(CAMERA_IMAGE_REQUEST_CODE);
	}
	
	private void getVideo() {
		FilePickerBuilder.getInstance()
				.setActivityTitle("Select videos")
				.enableImagePicker(false)
				.enableCameraSupport(false)
				.enableVideoPicker(true)
				.pickPhoto(this, VIDEO_REQUEST_CODE);
	}
	
	private void addNote() {
		
		final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.notes_add_note_layout, null);
		
		alertDialog.setView(dialogView);
		
		Button okButton = dialogView.findViewById(R.id.notesAddNoteOkButton);
		Button cancelButton = dialogView.findViewById(R.id.notesAddNoteCancelButton);
		final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddNoteNameTextInputLayout);
		nameTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				nameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		final TextInputLayout contentTextInput = dialogView.findViewById(R.id.notesAddNoteContentTextInputLayout);
		contentTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				contentTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = nameTextInput.getEditText().getText().toString().trim();
				String content = contentTextInput.getEditText().getText().toString().trim();
				
				if (name.isEmpty()) {
					nameTextInput.setError("Name cannot be empty");
				} else if (name.contains(".")) {
					nameTextInput.setError("Name cannot contain \".\"");
				} else if (name.contains("/")) {
					nameTextInput.setError("Name cannot contain \"/\"");
				} else {
					
					File file;
					
					int count = 0;
					String newNote;
					do {
						newNote = name;
						if (count > 0) {
							newNote += " " + count;
						}
						newNote += ".txt";
						++count;
						file = new File(noteFolder, newNote);
					} while (file.exists());
					
					newNote = newNote.substring(0, newNote.length() - 4);
					
					if (!newNote.equals(name)) {
						StyleableToast.makeText(requireContext(), name + " is already used. Setting name to " + newNote, Toast.LENGTH_SHORT, R.style.designedToast).show();
					}
					
					try {
						if (!file.exists()) {
							Log.d(TAG, "onClick: creating file " + file.createNewFile());
						}
						
						if (mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
						}
						
						FileOutputStream fos = new FileOutputStream(file);
						fos.write(content.getBytes());
						fos.close();
						notes.add(new FileItem(file.getPath()));
						mNotesAdapter.notifyItemInserted(notes.size() - 1);
						
						if (mEmptyLayout.getVisibility() == View.VISIBLE) {
							mEmptyLayout.setVisibility(View.GONE);
						}
						
						sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
					} catch (Exception e) {
						StyleableToast.makeText(requireContext(), "Could not create the note " + e.getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
						e.printStackTrace();
					}
					
					alertDialog.dismiss();
				}
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
		
		alertDialog.show();
		
	}
	
	private void addLink() {
		
		final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
		
		View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
		
		alertDialog.setView(dialogView);
		
		Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
		Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
		final TextInputLayout linkTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);
		linkTextInput.getEditText().addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				linkTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String link = linkTextInput.getEditText().getText().toString().trim();
				
				if (link.isEmpty()) {
					linkTextInput.setError("Link cannot be empty");
				} else if (!FileUtils.isValidUrl(link)) {
					linkTextInput.setError("Link is not valid");
				} else {
					
					FileItem item = new FileItem(new File(noteFolder, "Link" + UUID.randomUUID().toString().substring(0, 5)).getPath());
					item.setName(link);
					item.setType(FileType.FILE_TYPE_LINK);
					
					Log.d(TAG, item.toString());
					
					notes.add(item);
					mNotesAdapter.notifyItemInserted(notes.size() - 1);
					
					if (mEmptyLayout.getVisibility() == View.VISIBLE) {
						mEmptyLayout.setVisibility(View.GONE);
					}
					
					sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
					
					SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
					SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();
					
					if (!linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
						linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", true);
						links = new ArrayList<>();
					}
					
					links.add(item);
					
					Gson gson = new Gson();
					linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
					linkPreferenceEditor.apply();
					
					alertDialog.dismiss();
				}
			}
		});
		
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				alertDialog.dismiss();
			}
		});
		
		alertDialog.show();
		
	}
	
	private void getAudio() {
		Intent getAudio = new Intent("android.intent.action.MULTIPLE_PICK");
		getAudio.setType("audio/*");
		getAudio.setAction(Intent.ACTION_GET_CONTENT);
		getAudio.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		startActivityForResult(getAudio, AUDIO_REQUEST_CODE);
	}
	
	private void recordAudio() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
		Date date = new Date();
		
		String file = "AUD_" + dateFormat.format(date) + UUID.randomUUID().toString().substring(0, 5) + ".wav";
		audioFile = new File(noteFolder, file);
		AndroidAudioRecorder.with(FileFragment.this)
				.setColor(getResources().getColor(R.color.colorPrimary, requireActivity().getTheme()))
				.setFilePath(audioFile.getPath())
				.setRequestCode(RECORD_REQUEST_CODE)
				.setKeepDisplayOn(false)
				.recordFromFragment();
	}

	private void addToStarred(ArrayList<Integer> positions) {
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
		final ArrayList<Integer> starredIndexes = new ArrayList<>( positions );

		if (!starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", true);
			starred = new ArrayList<>();
		}

		for(Integer position : starredIndexes) {
			if (starredIndex(position) == -1) {
				Log.d(TAG, "onMenuItemClick: starring position " + position);

				notes.get(position).setStarred(true);
				starred.add(notes.get(position));
			}
		}
		Gson gson = new Gson();
		starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
		starredPreferenceEditor.apply();
		mNotesAdapter.notifyDataSetChanged();
	}

	private void addToStarred(int position) {
		ArrayList<Integer> positions = new ArrayList<>(1);
		positions.add( position );
		addToStarred( positions );
	}

}

package com.studypartner.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.ReminderAlertReceiver;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import static android.content.Context.MODE_PRIVATE;

public class ReminderDialogFragment extends DialogFragment {
	
	private TextInputEditText mTitleEditText;
	private TextInputEditText mContentEditText;
	private TextView mDateEditText;
	private TextView mTimeEditText;
	
	private NavController mNavController;
	
	private DatePickerDialog mDatePicker;
	private TimePickerDialog mTimePicker;
	
	private FloatingActionButton okFab;
	
	private String date, time;
	
	private boolean inEditMode = false;
	
	private String currentDate, currentTime;
	private int hourSelected, minuteSelected;
	
	private ArrayList<ReminderItem> mReminderList = new ArrayList<>();
	
	public ReminderDialogFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_reminder_dialog, container, false);
		
		int positionToEdit = -1;
		
		if (getArguments() != null) {
			inEditMode = true;
			positionToEdit = Integer.parseInt(String.valueOf(getArguments().getString("REMINDER_POSITION")));
		}
		
		mTitleEditText = rootView.findViewById(R.id.titleEditText);
		mContentEditText = rootView.findViewById(R.id.descriptionEditText);
		mDateEditText = rootView.findViewById(R.id.dateTextView);
		mTimeEditText = rootView.findViewById(R.id.timeTextView);
		
		okFab = rootView.findViewById(R.id.okButton);
		
		mNavController = NavHostFragment.findNavController(this);
		
		currentDate = getCurrentDate();
		currentTime = getCurrentTime();
		
		if (inEditMode) {
			populateEditText(positionToEdit);
		} else {
			date = currentDate;
			mDateEditText.setText(currentDate);
			
			time = currentTime;
			mTimeEditText.setText(currentTime);
		}
		
		final SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		final Gson gson = new Gson();
		final SharedPreferences.Editor reminderPreferenceEditor = reminderPreference.edit();
		
		if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
			
			String json = reminderPreference.getString("REMINDER_ITEMS", "");
			Type type = new TypeToken<ArrayList<ReminderItem>>() {
			}.getType();
			mReminderList = gson.fromJson(json, type);
		}
		
		if (mReminderList == null) {
			mReminderList = new ArrayList<>();
		}
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				dismiss();
				mNavController.navigateUp();
			}
		});
		
		mDateEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				int year = Integer.parseInt(date.substring(6));
				int month = Integer.parseInt(date.substring(3, 5)) - 1;
				int day = Integer.parseInt(date.substring(0, 2));
				
				mDatePicker = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
						
						String selectedYear = String.valueOf(year);
						String selectedMonth = String.valueOf(month + 1); // starts with 0
						String selectedDay = String.valueOf(dayOfMonth);
						
						if (month < 9) { // to make 01 - 09
							selectedMonth = "0" + selectedMonth;
						}
						
						if (dayOfMonth < 10) { // to make 01 - 09
							selectedDay = "0" + selectedDay;
						}
						
						date = selectedDay + "-" + selectedMonth + "-" + selectedYear;
						
						mDateEditText.setText(date);
						
					}
				}, year, month, day);
				
				mDatePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
				
				mDatePicker.show();
			}
		});
		
		mTimeEditText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				
				int hour = Integer.parseInt(time.substring(0, 2));
				int minute = Integer.parseInt(time.substring(3, 5));
				
				String am_pm = time.substring(6);
				
				if (am_pm.equals("PM") && hour != 12)
					hour = hour + 12;
				
				mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
						
						hourSelected = hourOfDay;
						minuteSelected = minute;
						
						String am_pm = "AM";
						
						if (hourOfDay >= 12) {
							am_pm = "PM";
						}
						
						if (hourOfDay > 12) {
							hourOfDay = hourOfDay - 12;
						}
						
						String selectedHour = String.valueOf(hourOfDay);
						String selectedMinute = String.valueOf(minute);
						
						if (hourOfDay < 10)
							selectedHour = "0" + selectedHour;
						
						if (minute < 10)
							selectedMinute = "0" + selectedMinute;
						
						time = selectedHour + ":" + selectedMinute + " " + am_pm;
						
						mTimeEditText.setText(time);
						
					}
				}, hour, minute, false);
				
				mTimePicker.show();
			}
		});
		
		final int finalPositionToEdit = positionToEdit;
		
		okFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Calendar cal = Calendar.getInstance();
				
				if (date.equals(currentDate) && time.equals(currentTime)) {
					StyleableToast.makeText(requireContext(), "Cannot set reminder for now", Toast.LENGTH_SHORT, R.style.designedToast).show();
				} else if (date.equals(currentDate) && (hourSelected < cal.get(Calendar.HOUR_OF_DAY) || (hourSelected == cal.get(Calendar.HOUR_OF_DAY) && minuteSelected < cal.get(Calendar.MINUTE)))) {
					StyleableToast.makeText(requireContext(), "Cannot set reminder for previous times", Toast.LENGTH_SHORT, R.style.designedToast).show();
				} else {
					String title = mTitleEditText.getText().toString().trim();
					String content = mContentEditText.getText().toString().trim();
					if (title.isEmpty()) title = "Reminder from Study Partner";
					
					if (inEditMode) {
						
						AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
						
						Intent intent = new Intent(requireContext(), ReminderAlertReceiver.class);
						Bundle bundle = new Bundle();
						bundle.putParcelable("BUNDLE_REMINDER_ITEM", mReminderList.get(finalPositionToEdit));
						
						intent.putExtra("EXTRA_REMINDER_ITEM", bundle);
						
						PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), mReminderList.get(finalPositionToEdit).getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
						
						alarmManager.cancel(pendingIntent);
						
						mReminderList.get(finalPositionToEdit).edit(title, content, time, date);
						mReminderList.get(finalPositionToEdit).setActive(true);
						
						if (mReminderList.get(finalPositionToEdit).isActive()) {
							ReminderItem newItem = mReminderList.get(finalPositionToEdit);
							mReminderList.remove(finalPositionToEdit);
							mReminderList.add(0, newItem);
						}
						
						createNotification(mReminderList.get(0));
					} else {
						ReminderItem item = new ReminderItem(title, content, time, date);
						
						mReminderList.add(0, item);
						createNotification(item);
					}
					
					String json = gson.toJson(mReminderList);
					
					if (!reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
						reminderPreferenceEditor.putBoolean("REMINDER_ITEMS_EXISTS", true);
					}
					
					reminderPreferenceEditor.putString("REMINDER_ITEMS", json);
					reminderPreferenceEditor.apply();
					
					mNavController.navigateUp();
				}
			}
		});
		return rootView;
	}
	
	private void populateEditText(int position) {
		
		SharedPreferences reminderPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
		Gson gson = new Gson();
		
		String json = reminderPreference.getString("REMINDER_ITEMS", "");
		Type type = new TypeToken<ArrayList<ReminderItem>>() {
		}.getType();
		mReminderList = gson.fromJson(json, type);
		
		if (mReminderList != null && mReminderList.size() > position) {
			ReminderItem editItem = mReminderList.get(position);
			mTitleEditText.setText(editItem.getTitle());
			mContentEditText.setText(editItem.getDescription());
			mDateEditText.setText(editItem.getDate());
			date = editItem.getDate();
			mTimeEditText.setText(editItem.getTime());
			time = editItem.getTime();
			hourSelected = Integer.parseInt(time.substring(0, 2));
			minuteSelected = Integer.parseInt(time.substring(3, 5));
		}
		
	}
	
	private void createNotification(ReminderItem item) {
		
		Calendar calendar = Calendar.getInstance();
		
		int year = Integer.parseInt(item.getDate().substring(6));
		int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
		int day = Integer.parseInt(item.getDate().substring(0, 2));
		
		int hour = Integer.parseInt(item.getTime().substring(0, 2));
		int minute = Integer.parseInt(item.getTime().substring(3, 5));
		
		String am_pm = item.getTime().substring(6);
		
		if (am_pm.equals("PM") && hour != 12)
			hour = hour + 12;
		
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		
		AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(requireContext(), ReminderAlertReceiver.class);
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
		
		intent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		
		PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), item.getNotifyId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
		
	}
	
	private String getCurrentDate() {
		
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		
		String selectedYear = String.valueOf(year);
		String selectedMonth = String.valueOf(month + 1); // starts with 0
		String selectedDay = String.valueOf(dayOfMonth);
		
		if (month < 9) { // to make 01 - 09
			selectedMonth = "0" + selectedMonth;
		}
		
		if (dayOfMonth < 10) { // to make 01 - 09
			selectedDay = "0" + selectedDay;
		}
		
		return selectedDay + "-" + selectedMonth + "-" + selectedYear;
		
	}
	
	private String getCurrentTime() {
		
		Calendar calendar = Calendar.getInstance();
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		hourSelected = hourOfDay;
		minuteSelected = minute;
		String am_pm = "AM";
		
		if (hourOfDay >= 12) {
			am_pm = "PM";
		}
		
		if (hourOfDay > 12) {
			hourOfDay = hourOfDay - 12;
		}
		
		String selectedHour = String.valueOf(hourOfDay);
		String selectedMinute = String.valueOf(minute);
		
		if (hourOfDay < 10)
			selectedHour = "0" + selectedHour;
		
		if (minute < 10)
			selectedMinute = "0" + selectedMinute;
		
		return selectedHour + ":" + selectedMinute + " " + am_pm;
		
	}
}

package com.studypartner.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.BuildConfig;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.adapters.NotesAdapter;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

public class StarredFragment extends Fragment implements NotesAdapter.NotesClickListener {
	
	private final String SORT_BY_NAME = "By Name";
	private final String SORT_BY_SIZE = "By Size";
	private final String SORT_BY_CREATION_TIME = "By Creation Time";
	private final String SORT_BY_MODIFIED_TIME = "By Modification Time";
	
	private final String ASCENDING_ORDER = "Ascending Order";
	private final String DESCENDING_ORDER = "Descending Order";
	
	private String sortBy;
	private String sortOrder;
	
	private LinearLayout mEmptyLayout;
	private FloatingActionButton fab;
	private RecyclerView recyclerView;
	private LinearLayout mLinearLayout;
	private TextView sortText;
	private ImageButton sortOrderButton, sortByButton;
	
	private NotesAdapter mStarredAdapter;
	
	private MainActivity activity;
	
	private ActionMode actionMode;
	private boolean actionModeOn = false;
	
	private ArrayList<FileItem> starred = new ArrayList<>();
	private ArrayList<FileItem> links = new ArrayList<>();
	
	public StarredFragment() {
	}
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		SharedPreferences.Editor editor = sortPreferences.edit();
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		} else {
			editor.putBoolean("SORTING_ORDER_EXISTS", true);
			editor.putString("SORTING_BY", SORT_BY_NAME);
			editor.putString("SORTING_ORDER", ASCENDING_ORDER);
			editor.apply();
			sortBy = SORT_BY_NAME;
			sortOrder = ASCENDING_ORDER;
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_starred, container, false);
		
		activity = (MainActivity) requireActivity();
		activity.mBottomAppBar.bringToFront();
		activity.fab.bringToFront();
		
		requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				fab.setOnClickListener(null);
				activity.mNavController.navigate(R.id.action_nav_starred_to_nav_home);
			}
		});
		
		fab = activity.fab;
		fab.show();
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				activity.mNavController.navigate(R.id.action_nav_starred_to_nav_notes);
			}
		});
		
		mEmptyLayout = rootView.findViewById(R.id.starredEmptyLayout);
		recyclerView = rootView.findViewById(R.id.starredRecyclerView);
		mLinearLayout = rootView.findViewById(R.id.starredLinearLayout);
		sortText = rootView.findViewById(R.id.starredSortText);
		sortOrderButton = rootView.findViewById(R.id.starredSortOrder);
		sortByButton = rootView.findViewById(R.id.starredSortButton);
		
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		sortOrderButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (sortOrder.equals(ASCENDING_ORDER)) {
					sortOrder = DESCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
				} else {
					sortOrder = ASCENDING_ORDER;
					sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
				}
				
				editor.putString("SORTING_ORDER", sortOrder).apply();
				sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
			}
		});
		
		sortByButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				final AlertDialog builder = new AlertDialog.Builder(getContext()).create();
				
				View dialogView = getLayoutInflater().inflate(R.layout.notes_sort_layout, null);
				
				Button okButton = dialogView.findViewById(R.id.sortByOkButton);
				Button cancelButton = dialogView.findViewById(R.id.sortByCancelButton);
				final RadioGroup radioGroup = dialogView.findViewById(R.id.sortByRadioGroup);
				radioGroup.clearCheck();
				
				switch (sortBy) {
					
					case SORT_BY_SIZE:
						radioGroup.check(R.id.sortBySizeRB);
						break;
					
					case SORT_BY_CREATION_TIME:
						radioGroup.check(R.id.sortByCreationTimeRB);
						break;
					
					case SORT_BY_MODIFIED_TIME:
						radioGroup.check(R.id.sortByModifiedTimeRB);
						break;
					
					default:
						radioGroup.check(R.id.sortByNameRB);
						break;
				}
				
				cancelButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						builder.dismiss();
					}
				});
				
				okButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (radioGroup.getCheckedRadioButtonId()) {
							case R.id.sortBySizeRB:
								sortBy = SORT_BY_SIZE;
								break;
							case R.id.sortByCreationTimeRB:
								sortBy = SORT_BY_CREATION_TIME;
								break;
							case R.id.sortByModifiedTimeRB:
								sortBy = SORT_BY_MODIFIED_TIME;
								break;
							default:
								sortBy = SORT_BY_NAME;
								break;
						}
						sortText.setText(sortBy);
						editor.putString("SORTING_BY", sortBy).apply();
						sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
						builder.dismiss();
					}
				});
				
				builder.setView(dialogView);
				builder.show();
			}
		});
		
		populateDataAndSetAdapter();
		
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE);
		
		if (sharedPreferences.getBoolean("NotesSearchExists", false)) {
			File searchedFile = new File(sharedPreferences.getString("NotesSearch", null));
			FileItem fileDesc = new FileItem(searchedFile.getPath());
			if (fileDesc.getType() == FileType.FILE_TYPE_FOLDER) {
				Bundle bundle = new Bundle();
				bundle.putString("FilePath", fileDesc.getPath());
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_fileFragment, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_VIDEO || fileDesc.getType() == FileType.FILE_TYPE_IMAGE || fileDesc.getType() == FileType.FILE_TYPE_AUDIO) {
				Bundle bundle = new Bundle();
				bundle.putString("State", "Files");
				bundle.putString("Media", fileDesc.getPath());
				bundle.putBoolean("InStarred", true);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_mediaActivity, bundle);
			} else if (fileDesc.getType() == FileType.FILE_TYPE_LINK) {
				FileUtils.openLink(requireContext(), fileDesc);
			} else {
				FileUtils.openFile(requireContext(), fileDesc);
			}
		}
		
		SharedPreferences sortPreferences = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SORTING", MODE_PRIVATE);
		
		if (sortPreferences.getBoolean("SORTING_ORDER_EXISTS", false)) {
			sortBy = sortPreferences.getString("SORTING_BY", SORT_BY_NAME);
			sortOrder = sortPreferences.getString("SORTING_ORDER", ASCENDING_ORDER);
		}
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		sortText.setText(sortBy);
		
		if (sortOrder.equals(ASCENDING_ORDER)) {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.downward_arrow));
		} else {
			sortOrderButton.setImageDrawable(requireActivity().getDrawable(R.drawable.upward_arrow));
		}
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		setHasOptionsMenu(true);
		activity.mBottomAppBar.performShow();
		activity.mBottomAppBar.setVisibility(View.VISIBLE);
		activity.mBottomAppBar.bringToFront();
		activity.fab.show();
		activity.fab.bringToFront();
	}
	
	@Override
	public void onPause() {
		if (actionMode != null) {
			actionMode.finish();
		}
		fab.setEnabled(true);
		Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
		setHasOptionsMenu(false);
		super.onPause();
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.notes_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.notes_menu_refresh:
				populateDataAndSetAdapter();
				return true;
			case R.id.notes_menu_search:
				Bundle bundle = new Bundle();
				FileItem[] files = new FileItem[starred.size()];
				files = starred.toArray(files);
				bundle.putParcelableArray("NotesArray", files);
				((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_notesSearchFragment, bundle);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onClick(int position) {
		if (actionModeOn) {
			
			enableActionMode(position);
			
		} else if (starred.get(position).getType().equals(FileType.FILE_TYPE_FOLDER)) {
			
			FileItem fileDesc = starred.get(position);
			Bundle bundle = new Bundle();
			bundle.putString("FilePath", fileDesc.getPath());
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_fileFragment, bundle);
			
		} else if (starred.get(position).getType().equals(FileType.FILE_TYPE_VIDEO) || starred.get(position).getType().equals(FileType.FILE_TYPE_AUDIO) || starred.get(position).getType() == FileType.FILE_TYPE_IMAGE) {
			
			Bundle bundle = new Bundle();
			bundle.putString("State", "Files");
			bundle.putString("Media", starred.get(position).getPath());
			bundle.putBoolean("InStarred", true);
			((MainActivity) requireActivity()).mNavController.navigate(R.id.action_nav_starred_to_mediaActivity, bundle);
			
		} else if (starred.get(position).getType() == FileType.FILE_TYPE_LINK) {
			
			FileUtils.openLink(requireContext(), starred.get(position));
			
		} else {

            FileUtils.openFile(requireContext(), starred.get(position));

        }
    }

    @Override
    public void onLongClick(int position) {
        enableActionMode(position);
    }

    @Override
    public void onOptionsClick(View view, final int position) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.notes_item_menu_unstar);

        if (starred.get(position).getType() == FileType.FILE_TYPE_FOLDER || starred.get(position).getType() == FileType.FILE_TYPE_LINK) {
            popup.getMenu().removeItem(R.id.notes_item_share);
        }

        if (starred.get(position).getType() == FileType.FILE_TYPE_LINK) {
            popup.getMenu().getItem(0).setTitle("Edit Link");
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.notes_item_rename:

                        final FileItem fileItem = starred.get(position);

						final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						final View dialogView = getLayoutInflater().inflate(R.layout.notes_add_link_layout, null);
						alertDialog.setView(dialogView);

						Button okButton = dialogView.findViewById(R.id.notesAddLinkOkButton);
						Button cancelButton = dialogView.findViewById(R.id.notesAddLinkCancelButton);
						final TextView titleDialog = dialogView.findViewById(R.id.notesAddLinkTitle);

						final TextInputLayout nameTextInput = dialogView.findViewById(R.id.notesAddLinkTextInputLayout);

                        if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
                            titleDialog.setText("Edit this link");
                        } else {
                            titleDialog.setText("Enter a new name");
                        }

                        String extension = "";
                        if (fileItem.getType() == FileType.FILE_TYPE_FOLDER || fileItem.getType() == FileType.FILE_TYPE_LINK) {
                            nameTextInput.getEditText().setText(fileItem.getName());
                        } else {
                            String name = fileItem.getName();
                            if (name.indexOf(".") > 0) {
                                extension = name.substring(name.lastIndexOf("."));
                                name = name.substring(0, name.lastIndexOf("."));
                            }
                            nameTextInput.getEditText().setText(name);
                        }

                        final String finalExtension = extension;
						okButton.setOnClickListener(new View.OnClickListener() {
							public void onClick(View view){
							String newName = nameTextInput.getEditText().getText().toString().trim();
                                File oldFile = new File(fileItem.getPath());
                                File newFile = new File(oldFile.getParent(), (newName + finalExtension));
                                if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
                                    if (newName.equals(fileItem.getName()) || newName.equals("")) {
                                        Log.d(TAG, "onClick: link not changed");
                                    } else if (!FileUtils.isValidUrl(newName)) {
                                        StyleableToast.makeText(getContext(), "Link is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
                                    } else {
                                        int linkIndex = linkIndex(position);
                                        if (linkIndex != -1) {
                                            links.get(linkIndex).setName(newName);
                                            starred.get(position).setName(newName);

                                            SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
                                            SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

                                            Gson gson = new Gson();
                                            linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
                                            linkPreferenceEditor.apply();

                                            SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
                                            SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

                                            starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
                                            starredPreferenceEditor.apply();


                                            mStarredAdapter.notifyItemChanged(position);
                                            sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
                                        }

                                    }
                                } else {
                                    if (newFile.getName().equals(fileItem.getName()) || newName.equals("")) {
                                        Log.d(TAG, "onClick: filename not changed");
                                    } else if (newFile.exists()) {
                                        StyleableToast.makeText(getContext(), "File with this name already exists", Toast.LENGTH_SHORT, R.style.designedToast).show();
                                    } else if (newName.contains("/")) {
                                        StyleableToast.makeText(getContext(), "File name is not valid", Toast.LENGTH_SHORT, R.style.designedToast).show();
                                    } else {
                                        if (oldFile.renameTo(newFile)) {
                                            StyleableToast.makeText(getContext(), "File renamed successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
                                            Gson gson = new Gson();

                                            if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
                                                for (FileItem starItem : starred) {
                                                    if (starItem.getPath().contains(oldFile.getPath())) {
                                                        starItem.setPath(starItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
                                                    }
                                                }
                                                for (FileItem linkItem : links) {
                                                    if (linkItem.getPath().contains(oldFile.getPath())) {
                                                        linkItem.setPath(linkItem.getPath().replaceFirst(oldFile.getPath(), newFile.getPath()));
                                                    }
                                                }

                                                SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
                                                SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

                                                linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
                                                linkPreferenceEditor.apply();
                                            }

											starred.get(position).setName(newFile.getName());
											starred.get(position).setPath(newFile.getPath());
											Log.d("Rename", starred.get(position).getPath());

                                            SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
                                            SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

                                            starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
                                            starredPreferenceEditor.apply();

                                            mStarredAdapter.notifyItemChanged(position);
                                            sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
                                        } else {
                                            StyleableToast.makeText(getContext(), "File could not be renamed", Toast.LENGTH_SHORT, R.style.designedToast).show();
                                        }
                                    }
                                }
								alertDialog.dismiss();
                            }
                        });
						cancelButton.setOnClickListener(new View.OnClickListener() {
															public void onClick(View view) {
																alertDialog.cancel();
															}
														});

                        alertDialog.show();
                        return true;

                    case R.id.notes_item_unstar:

                        SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
                        SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

                        mStarredAdapter.notifyItemRemoved(position);
                        starred.remove(position);
                        activity.mBottomAppBar.performShow();
                        if (starred.size() == 0) {
                            starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
                            mEmptyLayout.setVisibility(View.VISIBLE);
                        }
                        Gson gson = new Gson();
                        starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
                        starredPreferenceEditor.apply();

                        return true;

					case R.id.notes_item_delete:

						AlertDialog.Builder builder = new AlertDialog.Builder(
								getContext());
						View view = getLayoutInflater().inflate(R.layout.alert_dialog_box, null);

						Button yesButton = view.findViewById(R.id.yes_button);
						Button noButton = view.findViewById(R.id.no_button);
						TextView title = view.findViewById(R.id.title_dialog);
						TextView detail = view.findViewById(R.id.detail_dialog);
						title.setText("Delete File");
						detail.setText("Are you sure you want to delete the file?");

						builder.setView(view);

						final AlertDialog dialog = builder.create();
						dialog.setCanceledOnTouchOutside(true);

						yesButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {

								dialog.dismiss();

								// Hiding the file to be deleted from RecyclerView
								recyclerView.findViewHolderForAdapterPosition(position).itemView.
										findViewById(R.id.noteItemLayout).setVisibility(View.GONE);

								// Displaying a Snackbar to allow user to UNDO the delete file action
								Snackbar undoSnackbar = Snackbar.make(recyclerView, R.string.starred_file_deleted, Snackbar.LENGTH_LONG);
								undoSnackbar.setAction(R.string.notes_action_undo, new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										// Making folder visible to user again
										recyclerView.findViewHolderForAdapterPosition(position).itemView.
												findViewById(R.id.noteItemLayout).setVisibility(View.VISIBLE);
									}

								}).setDuration(3000).setActionTextColor(getResources().getColor(R.color.colorPrimary));

								undoSnackbar.addCallback(new Snackbar.Callback() {
									// Called when the Snackbar is dismissed by an event other than
									// clicking of UNDO.
									@Override
									public void onDismissed(Snackbar snackbar, int event) {
										if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
												event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
												event == Snackbar.Callback.DISMISS_EVENT_MANUAL) {

											if (starred.get(position).getType() != FileType.FILE_TYPE_LINK) {
												File file = new File(starred.get(position).getPath());
												deleteRecursive(file);

												if (starred.get(position).getType() == FileType.FILE_TYPE_FOLDER) {
													ArrayList<FileItem> linksToBeRemoved = new ArrayList<>();
													for (FileItem linkItem : links) {
														if (linkItem.getPath().contains(file.getPath())) {
															linksToBeRemoved.add(linkItem);
														}
													}
													links.removeAll(linksToBeRemoved);

													SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
													SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

													if (links.size() == 0) {
														linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
													}
													Gson gson = new Gson();
													linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
													linkPreferenceEditor.apply();

													ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
													for (FileItem starItem : starred) {
														if (starItem.getPath().contains(file.getPath()) && !starItem.getPath().equals(file.getPath())) {
															starredToBeRemoved.add(starItem);
														}
													}
													starredToBeRemoved.add(starred.get(position));
													starred.removeAll(starredToBeRemoved);

													SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
													SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();

													if (starred.size() == 0) {
														starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
														mEmptyLayout.setVisibility(View.VISIBLE);
													}
													starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
													starredPreferenceEditor.apply();
												}

											} else {
												int linkPosition = linkIndex(position);
												if (linkPosition != -1) {
													links.remove(linkPosition);
													SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
													SharedPreferences.Editor linkPreferenceEditor = linkPreference.edit();

													if (links.size() == 0) {
														linkPreferenceEditor.putBoolean("LINK_ITEMS_EXISTS", false);
													}
													Gson gson = new Gson();
													linkPreferenceEditor.putString("LINK_ITEMS", gson.toJson(links));
													linkPreferenceEditor.apply();
												}
											}
											mStarredAdapter.notifyDataSetChanged();
										}
									}
								});
								undoSnackbar.show(); // Snackbar will appear for 3 seconds
							}
						});
						noButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
							}
						});
						dialog.show();
						return true;

                    case R.id.notes_item_share:
                        if (starred.get(position).getType() != FileType.FILE_TYPE_FOLDER) {
                            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                            File shareFile = new File(starred.get(position).getPath());
                            ArrayList<FileItem> fileItems = new ArrayList<>();
                            fileItems.add(starred.get(position));
                            if (shareFile.exists()) {
                                intentShareFile.setType(FileUtils.getFileType(fileItems));
                                intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(starred.get(position).getPath())));
                                intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
                                startActivity(Intent.createChooser(intentShareFile, "Share File"));
                            }
                        } else {
                            StyleableToast.makeText(getContext(), "Folder cannot be shared", Toast.LENGTH_SHORT, R.style.designedToast).show();
                        }
                        return true;

                    default:
                        return false;
                }
            }
        });
        popup.show();
    }
	
	private void populateDataAndSetAdapter() {
		SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
		
		if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = starredPreference.getString("STARRED_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			starred = gson.fromJson(json, type);
			
			if (starred == null) starred = new ArrayList<>();
		}
		
		ArrayList<FileItem> starredToBeRemoved = new ArrayList<>();
		for (FileItem starItem : starred) {
			File starFile = new File(starItem.getPath());
			if (starItem.getType() == FileType.FILE_TYPE_LINK) {
				if (starFile.getParentFile() == null || !starFile.getParentFile().exists()) {
					starredToBeRemoved.add(starItem);
				}
			} else {
				if (!starFile.exists()) {
					starredToBeRemoved.add(starItem);
				}
			}
		}
		starred.removeAll(starredToBeRemoved);
		
		SharedPreferences linkPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "LINK", MODE_PRIVATE);
		
		if (linkPreference.getBoolean("LINK_ITEMS_EXISTS", false)) {
			Gson gson = new Gson();
			String json = linkPreference.getString("LINK_ITEMS", "");
			Type type = new TypeToken<List<FileItem>>() {
			}.getType();
			links = gson.fromJson(json, type);
			
			if (links == null) links = new ArrayList<>();
		}
		
		ArrayList<FileItem> linkToBeRemoved = new ArrayList<>();
		for (FileItem linkItem : links) {
			File linkParent = new File(linkItem.getPath()).getParentFile();
			assert linkParent != null;
			if (!linkParent.exists()) {
				linkToBeRemoved.add(linkItem);
			}
		}
		links.removeAll(linkToBeRemoved);
		
		if (starred.isEmpty()) {
			mEmptyLayout.setVisibility(View.VISIBLE);
		}
		
		mStarredAdapter = new NotesAdapter(requireActivity(), starred, this, true);
		
		sort(sortBy, sortOrder.equals(ASCENDING_ORDER));
		
		recyclerView.setAdapter(mStarredAdapter);
	}
	
	private int linkIndex(int position) {
		int index = -1;
		if (links != null) {
			for (int i = 0; i < links.size(); i++) {
				FileItem linkItem = links.get(i);
				if (linkItem.getPath().equals(starred.get(position).getPath())) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory()) {
			for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
				deleteRecursive(child);
			}
		}
		if (!fileOrDirectory.delete()) {
			StyleableToast.makeText(activity, "Cannot delete some files", Toast.LENGTH_SHORT, R.style.designedToast).show();
		}
	}
	
	private void enableActionMode(int position) {
		if (actionMode == null) {
			
			actionMode = requireActivity().startActionMode(new android.view.ActionMode.Callback2() {
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					mode.getMenuInflater().inflate(R.menu.menu_notes_action_mode, menu);
					menu.removeItem(R.id.notes_action_delete);
					actionModeOn = true;
					fab.setEnabled(false);
					mLinearLayout.setVisibility(View.GONE);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
					return true;
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch (item.getItemId()) {
						case R.id.notes_action_unstar:
							unstarRows();
							mode.finish();
							return true;
						case R.id.notes_action_share:
							shareRows();
							mode.finish();
							return true;
						case R.id.notes_action_select_all:
							selectAll();
							return true;
						default:
							return false;
					}
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					mStarredAdapter.clearSelections();
					actionModeOn = false;
					actionMode = null;
					fab.setEnabled(true);
					activity.mBottomAppBar.performShow();
					mLinearLayout.setVisibility(View.VISIBLE);
					Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
				}
			});
		}
		toggleSelection(position);
	}
	
	private void toggleSelection(int position) {
		mStarredAdapter.toggleSelection(position);
		int count = mStarredAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
			actionMode = null;
		} else {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
	}
	
	private void selectAll() {
		mStarredAdapter.selectAll();
		int count = mStarredAdapter.getSelectedItemCount();
		
		if (count == 0) {
			actionMode.finish();
		} else if (actionMode != null) {
			actionMode.setTitle(String.valueOf(count));
			actionMode.invalidate();
		}
		
		actionMode = null;
	}
	
	private void unstarRows() {
		final ArrayList<Integer> selectedItemPositions = mStarredAdapter.getSelectedItems();

		AlertDialog.Builder builder = new AlertDialog.Builder(
				getContext());
		View view=getLayoutInflater().inflate(R.layout.alert_dialog_box,null);

		Button yesButton=view.findViewById(R.id.yes_button);
		Button noButton=view.findViewById(R.id.no_button);
		TextView title = view.findViewById(R.id.title_dialog);
		TextView detail = view.findViewById(R.id.detail_dialog);
		title.setText("Unstar Files");
		detail.setText("Are you sure you want to unstar " + selectedItemPositions.size() + " files?");

		builder.setView(view);

		final AlertDialog dialog= builder.create();
		dialog.setCanceledOnTouchOutside(true);

		yesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
					starred.remove(selectedItemPositions.get(i).intValue());
					mStarredAdapter.notifyItemRemoved(selectedItemPositions.get(i));
				}
				
				SharedPreferences starredPreference = requireActivity().getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
				SharedPreferences.Editor starredPreferenceEditor = starredPreference.edit();
				
				if (starred.size() == 0) {
					starredPreferenceEditor.putBoolean("STARRED_ITEMS_EXISTS", false);
					mEmptyLayout.setVisibility(View.VISIBLE);
				}
				Gson gson = new Gson();
				starredPreferenceEditor.putString("STARRED_ITEMS", gson.toJson(starred));
				starredPreferenceEditor.apply();
				activity.mBottomAppBar.performShow();
				dialog.dismiss();
			}
		});
		noButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		
		actionMode = null;
	}
	
	private void shareRows() {
		ArrayList<Integer> selectedItemPositions = mStarredAdapter.getSelectedItems();
		ArrayList<Integer> positionsToBeRemoved = new ArrayList<>();
		for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
			if (FileUtils.getFileType(new File(starred.get(selectedItemPositions.get(i)).getPath())) == FileType.FILE_TYPE_FOLDER) {
				positionsToBeRemoved.add(i);
			} else if (starred.get(selectedItemPositions.get(i)).getType() == FileType.FILE_TYPE_LINK) {
				positionsToBeRemoved.add(i);
			}
		}
		
		selectedItemPositions.removeAll(positionsToBeRemoved);
		
		ArrayList<FileItem> fileItems = new ArrayList<>();
		ArrayList<Uri> fileItemsUri = new ArrayList<>();
		
		for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
			fileItems.add(starred.get(selectedItemPositions.get(i)));
			fileItemsUri.add(FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", new File(starred.get(selectedItemPositions.get(i)).getPath())));
		}
		
		Intent intentShareFile = new Intent(Intent.ACTION_SEND_MULTIPLE);
		intentShareFile.setType(FileUtils.getFileType(fileItems));
		intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileItemsUri);
		intentShareFile.putExtra(Intent.EXTRA_TEXT, "Shared using Study Partner application");
		startActivity(Intent.createChooser(intentShareFile, "Share File"));
	}
	
	private void sort(String text, boolean ascending) {
		switch (text) {
			case SORT_BY_SIZE:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o1.getSize(), o2.getSize());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return Long.compare(o2.getSize(), o1.getSize());
						}
					});
				}
				
				break;
			case SORT_BY_CREATION_TIME:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateCreated().compareTo(o2.getDateCreated());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateCreated().compareTo(o1.getDateCreated());
						}
					});
				}
				
				break;
			case SORT_BY_MODIFIED_TIME:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getDateModified().compareTo(o2.getDateModified());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getDateModified().compareTo(o1.getDateModified());
						}
					});
				}
				
				break;
			case SORT_BY_NAME:
				
				if (ascending) {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o1.getName().compareTo(o2.getName());
						}
					});
				} else {
					Collections.sort(starred, new Comparator<FileItem>() {
						@Override
						public int compare(FileItem o1, FileItem o2) {
							return o2.getName().compareTo(o1.getName());
						}
					});
				}
				break;
		}
		
		mStarredAdapter.notifyDataSetChanged();
	}
}

package com.studypartner.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;

import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;

public class ReminderAlertReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Bundle bundle = intent.getBundleExtra("EXTRA_REMINDER_ITEM");
		assert bundle != null;
		ReminderItem item = bundle.getParcelable("BUNDLE_REMINDER_ITEM");
		assert item != null;
		boolean cancel = intent.getBooleanExtra("CANCEL", false);
		
		NotificationHelper notificationHelper = new NotificationHelper(context);
		
		Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		if (notificationUri == null) {
			notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		}
		Ringtone ringtone = RingtoneManager.getRingtone(context, notificationUri);
		ringtone.play();
		
		if (cancel) {
			notificationHelper.getManager().cancel(item.getNotifyId());
		} else {
			NotificationCompat.Builder builder = notificationHelper.getChannelNotification(context, item);
			notificationHelper.getManager().notify(item.getNotifyId(), builder.build());
		}
		
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock, wakeLock_cpu;
		boolean isScreenOn = powerManager.isInteractive();
		if (!isScreenOn) {
			wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "StudyPartner:WakeLockTag");
			wakeLock.acquire(2000);
			wakeLock_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StudyPartner:CPUWakeLockTag");
			wakeLock_cpu.acquire(2000);
		}
	}
}


package com.studypartner.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Patterns;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.BuildConfig;
import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import okhttp3.HttpUrl;


public class FileUtils {
	public static final String DOCUMENTS_DIR = "documents";
	public static final String AUTHORITY =  "${applicationId}.provider";
	
	final static HashMap<String, FileType> types = new HashMap<>();
	
	static void createMap() {
		types.put("image", FileType.FILE_TYPE_IMAGE);
		types.put("applicaton", FileType.FILE_TYPE_APPLICATION);
		types.put("text", FileType.FILE_TYPE_TEXT);
		types.put("audio", FileType.FILE_TYPE_AUDIO);
		types.put("video", FileType.FILE_TYPE_VIDEO);
	}
	
	public static FileType getFileType(File file) {
		FileType ft = FileType.FILE_TYPE_OTHER;
		if (file.isDirectory())
			ft = FileType.FILE_TYPE_FOLDER;
		else {
			String extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.getPath()));
			if (extension != null) {
				extension = extension.substring(0, extension.indexOf('/'));
				createMap();
				if (types.containsKey(extension))
					ft = types.get(extension);
			}
		}
		return ft;
	}
	
	public static boolean isValidUrl (String link) {
	
		if (link.trim().isEmpty()) 	return false;
		else if (!URLUtil.isNetworkUrl(link)) return false;
		else if (!Patterns.WEB_URL.matcher(link).matches()) return false;
		else return HttpUrl.parse(link) != null;
		
	}
	
	public static void openLink(final Context context, final FileItem link) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Open Link");
		builder.setMessage("Do you want to open the link?");
		builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (isValidUrl(link.getName())) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.getName()));
					context.startActivity(Intent.createChooser(browserIntent, "Select the app to open the link"));
				} else {
					StyleableToast.makeText(context, "Link is invalid", Toast.LENGTH_SHORT, R.style.designedToast).show();
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
			}
		});
		builder.show();
		
	}
	
	public static void openLink(final Context context, final String link) {
		
		if (isValidUrl(link)) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			context.startActivity(Intent.createChooser(browserIntent, "Select the app to open the link"));
		} else {
			StyleableToast.makeText(context, "Link is invalid", Toast.LENGTH_SHORT, R.style.designedToast).show();
		}
		
	}
	
	public static void openFile(Context context, FileItem file) {
		
		Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(file.getPath()));
		
		Intent target = new Intent(Intent.ACTION_VIEW);
		if (uri.toString().contains(".doc") || uri.toString().contains(".docx")) {
			target.setDataAndType(uri,"application/msword");
		} else if(uri.toString().contains(".pdf")) {
			target.setDataAndType(uri,"application/pdf");
		} else if(uri.toString().contains(".ppt") || uri.toString().contains(".pptx")) {
			target.setDataAndType(uri,"application/vnd.ms-powerpoint");
		} else if(uri.toString().contains(".xls") || uri.toString().contains(".xlsx")) {
			target.setDataAndType(uri,"application/vnd.ms-excel");
		} else if(uri.toString().contains(".zip") || uri.toString().contains(".rar")) {
			target.setDataAndType(uri,"application/zip");
		} else if(uri.toString().contains(".rtf")) {
			target.setDataAndType(uri,"application/rtf");
		}  else if(uri.toString().contains(".txt")) {
			target.setDataAndType(uri,"text/plain");
		} else {
			target.setDataAndType(uri, "*/*");
		}
		
		target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		target.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
		
		Intent intent = Intent.createChooser(target, "Open file");
		
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			StyleableToast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT, R.style.designedToast).show();
		}
	}
	
	public static String copyFile (String inputFilePath, String outputDirectoryPath) {

		String fileName = new File(inputFilePath).getName();
		//if(getFileType(new File(inputFilePath))==FileType.FILE_TYPE_AUDIO)
		fileName = fileName.replaceAll(" ", "_");
		String outputFilePath = new File(outputDirectoryPath, fileName).getPath();


		try (InputStream in = new FileInputStream(inputFilePath)) {

			OutputStream out = new FileOutputStream(outputFilePath);

			byte[] buffer = new byte[1024];

			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputFilePath;
	}
	
	public static String getFileType(ArrayList<FileItem> fileItems) {
		String fileType = "*/*";
		boolean text = false, app = false, image = false, video = false, audio = false, other = false;
		
		for (FileItem item : fileItems) {
			if (item.getType() == FileType.FILE_TYPE_FOLDER) {
				return null;
			} else if (item.getType() == FileType.FILE_TYPE_IMAGE) {
				image = true;
			} else if (item.getType() == FileType.FILE_TYPE_APPLICATION) {
				app = true;
			} else if (item.getType() == FileType.FILE_TYPE_TEXT) {
				text = true;
			} else if (item.getType() == FileType.FILE_TYPE_AUDIO) {
				audio = true;
			} else if (item.getType() == FileType.FILE_TYPE_VIDEO) {
				video = true;
			} else {
				other = true;
			}
		}
		
		if (text && !(app && image && video && audio && other)) {
			fileType = "text/*";
		} else if (app && !(text && image && video && audio && other)) {
			fileType = "application/*";
		} else if (image && !(text && app && video && audio && other)) {
			fileType = "image/*";
		} else if (video && !(text && app && image && audio && other)) {
			fileType = "video/*";
		} else if (audio && !(text && app && image && video && other)) {
			fileType = "audio/*";
		}
		
		return fileType;
	}
	
	/**
	 * @return Whether the URI is a local one.
	 */
	public static boolean isLocal(String url) {
		return url != null && !url.startsWith("http://") && !url.startsWith("https://");
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is local.
	 */
	public static boolean isLocalStorageDocument(Uri uri) {
		return AUTHORITY.equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Drive.
	 */
	public static boolean isGoogleDriveUri(Uri uri) {
		return "com.google.android.apps.docs.storage".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
	}
	
	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context       The context.
	 * @param uri           The Uri to query.
	 * @param selection     (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	                                   String[] selectionArgs) {
		
		Cursor cursor = null;
		final String column = MediaStore.Files.FileColumns.DATA;
		final String[] projection = {
				column
		};
		
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		}  catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
	
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 * Callers should check whether the path is local before assuming it
	 * represents a local file.
	 *
	 * @param context The context.
	 * @param uri     The Uri to query.
	 * @see #isLocal(String)
	 */
	public static String getFilePath(final Context context, final Uri uri) {
		String absolutePath = getLocalPath(context, uri);
		return absolutePath != null ? absolutePath : uri.toString();
	}
	
	private static String getLocalPath(final Context context, final Uri uri) {
		
		// DocumentProvider
		if (DocumentsContract.isDocumentUri(context, uri)) {
			// LocalStorageProvider
			if (isLocalStorageDocument(uri)) {
				// The path is the id
				return DocumentsContract.getDocumentId(uri);
			}
			// ExternalStorageProvider
			else if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				} else if ("home".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/documents/" + split[1];
				} else {
					return "/storage/" + type + "/" + split[1];
				}
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				
				String id = DocumentsContract.getDocumentId(uri);
				
				if (id != null && id.startsWith("raw:")) {
					id = id.substring(4);
				} else if (id != null && id.startsWith("msf:")) {
					id = id.substring(4);
				}
				
				String[] contentUriPrefixesToTry = new String[]{
						"content://downloads/public_downloads",
						"content://downloads/my_downloads"
				};
				
				for (String contentUriPrefix : contentUriPrefixesToTry) {
					Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.parseLong(id));
					try {
						String path = getDataColumn(context, contentUri, null, null);
						if (path != null) {
							return path;
						}
					} catch (Exception ignored) {}
				}
				
				// path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
				String fileName = getFileName(context, uri);
				File cacheDir = getDocumentCacheDir(context);
				File file = generateFileName(fileName, cacheDir);
				String destinationPath = null;
				if (file != null) {
					destinationPath = file.getAbsolutePath();
					saveFileFromUri(context, uri, destinationPath);
				}
				
				return destinationPath;
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				
				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{
						split[1]
				};
				
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
			//GoogleDrive
			else if (isGoogleDriveUri(uri)) {
				Log.d("TAG", "getLocalPath: uri is " + uri + " authority is " + uri.getAuthority());
				
				Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
				int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				returnCursor.moveToFirst();
				String name = (returnCursor.getString(nameIndex));
				File file = new File(context.getCacheDir(), name);
				try {
					InputStream inputStream = context.getContentResolver().openInputStream(uri);
					FileOutputStream outputStream = new FileOutputStream(file);
					int read;
					int maxBufferSize = 1024 * 1024;
					int bytesAvailable = inputStream.available();
					
					int bufferSize = Math.min(bytesAvailable, maxBufferSize);
					
					final byte[] buffers = new byte[bufferSize];
					while ((read = inputStream.read(buffers)) != -1) {
						outputStream.write(buffers, 0, read);
					}
					inputStream.close();
					outputStream.close();
				} catch (Exception ignored) {}
				return file.getPath();
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}
			
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		
		return null;
	}
	
	public static File getDocumentCacheDir(@NonNull Context context) {
		File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	@Nullable
	public static File generateFileName(@Nullable String name, File directory) {
		if (name == null) {
			return null;
		}
		
		File file = new File(directory, name);
		
		if (file.exists()) {
			String fileName = name;
			String extension = "";
			int dotIndex = name.lastIndexOf('.');
			if (dotIndex > 0) {
				fileName = name.substring(0, dotIndex);
				extension = name.substring(dotIndex);
			}
			
			int index = 0;
			
			while (file.exists()) {
				index++;
				name = fileName + '(' + index + ')' + extension;
				file = new File(directory, name);
			}
		}
		
		try {
			if (!file.createNewFile()) {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
		
		return file;
	}
	
	private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
		InputStream is = null;
		BufferedOutputStream bos = null;
		try {
			is = context.getContentResolver().openInputStream(uri);
			bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
			byte[] buf = new byte[1024];
			is.read(buf);
			do {
				bos.write(buf);
			} while (is.read(buf) != -1);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) is.close();
				if (bos != null) bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getFileName(@NonNull Context context, Uri uri) {
		String mimeType = context.getContentResolver().getType(uri);
		String filename = null;
		
		if (mimeType == null) {
			String path = getFilePath(context, uri);
			File file = new File(path);
			filename = file.getName();
		} else {
			Cursor returnCursor = context.getContentResolver().query(uri, null,
					null, null, null);
			if (returnCursor != null) {
				int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				returnCursor.moveToFirst();
				filename = returnCursor.getString(nameIndex);
				returnCursor.close();
			}
		}
		
		return filename;
	}
}

package com.studypartner.utils;

public enum FileType {
	FILE_TYPE_FOLDER,
	FILE_TYPE_IMAGE,
	FILE_TYPE_VIDEO,
	FILE_TYPE_TEXT,
	FILE_TYPE_APPLICATION,
	FILE_TYPE_AUDIO,
	FILE_TYPE_LINK,
	FILE_TYPE_OTHER
}

package com.studypartner.utils;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;

import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.models.ReminderItem;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper extends ContextWrapper {
	public static final String channelID = "StudyPartnerChannelId";
	public static final String channelName = "Reminders";
	private NotificationManager mManager;
	
	public NotificationHelper(Context base) {
		super(base);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.O)
	private void createChannel() {
		NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
		channel.setLightColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
		channel.enableLights(true);
		AudioAttributes audioAttributes = new AudioAttributes.Builder()
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
				.build();
		channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes);
		getManager().createNotificationChannel(channel);
	}
	
	public NotificationManager getManager() {
		if (mManager == null) {
			mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mManager;
	}
	
	public NotificationCompat.Builder getChannelNotification(Context context, ReminderItem item) {
		
		Bundle bundle = new Bundle();
		bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
		
//		PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
//				.setComponentName(MainActivity.class)
//				.setGraph(R.navigation.main_nav_graph)
//				.setDestination(R.id.nav_reminder)
//				.createPendingIntent();
		
		Intent openIntent = new Intent(context, MainActivity.class);
		openIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent openPendingIntent = PendingIntent.getActivity(context,1,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent dismissIntent = new Intent(context, ReminderAlertReceiver.class);
		dismissIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
		dismissIntent.putExtra("CANCEL", true);
		PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		return new NotificationCompat.Builder(context, channelID)
				.setAutoCancel(true)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setContentTitle(item.getTitle())
				.setContentText(item.getDescription())
				.setContentIntent(openPendingIntent)
				.addAction(android.R.drawable.ic_menu_view, "OPEN IN APP", openPendingIntent)
				.addAction(android.R.drawable.ic_delete, "DISMISS", dismissPendingIntent)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_logo_round))
				.setSmallIcon(R.drawable.app_logo_transparent)
				.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
				.setLights(ContextCompat.getColor(this, R.color.colorPrimaryDark), 1000, 1000)
				.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
	}
}


package com.studypartner.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.R;

import androidx.fragment.app.Fragment;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Connection {
	
	public static void checkConnection(final Fragment fragment) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) fragment.requireActivity().getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragment.requireActivity())
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							fragment.requireActivity().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							StyleableToast.makeText(fragment.requireActivity(), "Some functions might not work properly without internet", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
					})
					.setNeutralButton("Reload", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkConnection(fragment.requireActivity());
						}
					});
			alertDialog.show();
		}
	}
	
	public static void checkConnection(final Activity activity) {
		
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity)
					.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							StyleableToast.makeText(activity, "Some functions might not work properly without internet", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
					})
					.setNeutralButton("Reload", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkConnection(activity);
						}
					});
			alertDialog.show();
		}
	}
	
	public static void feedback(final Activity activity) {
		
		Connection.checkConnection(activity);
		Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
		feedbackIntent.setData(Uri.parse("mailto:"));
		feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"studypartnerapp@gmail.com"});
		feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Study Partner");
		activity.startActivity(Intent.createChooser(feedbackIntent, "Choose your email client"));
		
	}
	
	public static void reportBug(final Activity activity) {
		
		Connection.checkConnection(activity);
		TelephonyManager telephonyManager = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		StatFs internalStatFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
		long internalMemSizeInMB = (internalStatFs.getAvailableBlocksLong() * internalStatFs.getBlockCountLong()) / (1024 * 1024);
		ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);
		long ramSizeInMB = memoryInfo.totalMem / (1024 * 1024);

//		String networkType;
//
//		switch (telephonyManager.getNetworkType()) {
//			case TelephonyManager.NETWORK_TYPE_CDMA:
//				networkType = "CDMA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_EDGE:
//				networkType = "EDGE";
//				break;
//			case TelephonyManager.NETWORK_TYPE_GPRS:
//				networkType = "GPRS";
//				break;
//			case TelephonyManager.NETWORK_TYPE_GSM:
//				networkType = "GSM";
//				break;
//			case TelephonyManager.NETWORK_TYPE_IWLAN:
//				networkType = "IWLAN";
//				break;
//			case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
//				networkType = "TD SCDMA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_LTE:
//				networkType = "LTE";
//				break;
//			case TelephonyManager.NETWORK_TYPE_UMTS:
//				networkType = "UMTS";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSDPA:
//				networkType = "HSDPA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSPA:
//				networkType = "HSPA";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSPAP:
//				networkType = "HSPAP";
//				break;
//			case TelephonyManager.NETWORK_TYPE_HSUPA:
//				networkType = "HSUPA";
//				break;
//			default:
//				networkType = "UNKOWN";
//				break;
//		}
//
//		String phoneType;
//		switch (telephonyManager.getPhoneType()) {
//			case TelephonyManager.PHONE_TYPE_CDMA:
//				phoneType = "CDMA";
//				break;
//			case TelephonyManager.PHONE_TYPE_GSM:
//				phoneType = "GSM";
//				break;
//			case TelephonyManager.PHONE_TYPE_SIP:
//				phoneType = "SIP";
//				break;
//			default:
//				phoneType = "NONE";
//				break;
//		}
		
		StringBuilder builder = new StringBuilder();
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			builder.append("UID: ").append(FirebaseAuth.getInstance().getCurrentUser().getUid()).append("\n");
			builder.append("EMAIL ADDRESS: ").append(FirebaseAuth.getInstance().getCurrentUser().getEmail()).append("\n");
			builder.append("EMAIL VERIFIED: ").append(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()).append("\n");
		}
//		builder.append("SERIAL: ").append(Build.SERIAL).append("\n");
//		builder.append("APP: ").append(BuildConfig.APPLICATION_ID).append("\n");
		builder.append("MODEL: ").append(Build.MODEL).append("\n");
		builder.append("ID: ").append(Build.ID).append("\n");
		builder.append("MANUFACTURER: ").append(Build.MANUFACTURER).append("\n");
		builder.append("BRAND: ").append(Build.BRAND).append("\n");
		builder.append("SDK  ").append(Build.VERSION.SDK_INT).append("\n");
		builder.append("RELEASE: ").append(Build.VERSION.RELEASE).append("\n");
//		builder.append("NETWORK INFO: ").append(connectivityManager.getActiveNetwork()).append("\n");
		builder.append("CARRIER: ").append(telephonyManager.getNetworkOperatorName()).append("\n");
//		builder.append("PHONE TYPE: ").append(phoneType).append("\n");
//		builder.append("NETWORK TYPE: ").append(networkType).append("\n");
		builder.append("TOTAL RAM: ").append(ramSizeInMB).append(" MB").append("\n");
		builder.append("INTERNAL MEMORY AVAILABLE: ").append(internalMemSizeInMB).append(" MB").append("\n");
//		builder.append("INCREMENTAL ").append(Build.VERSION.INCREMENTAL).append("\n");
//		builder.append("BOARD: ").append(Build.BOARD).append("\n");
//		builder.append("HOST ").append(Build.HOST).append("\n");
//		builder.append("FINGERPRINT: ").append(Build.FINGERPRINT).append("\n");
//		builder.append("BOOTLOADER: ").append(Build.BOOTLOADER).append("\n");
//		builder.append("DEVICE: ").append(Build.DEVICE).append("\n");
//		builder.append("DISPLAY: ").append(Build.DISPLAY).append("\n");
//		builder.append("HARDWARE: ").append(Build.HARDWARE).append("\n");
//		builder.append("PRODUCT: ").append(Build.PRODUCT).append("\n");
		builder.append("PERMISSIONS GRANTED: ").append("\n\n");
		try {
			PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
			for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
				if ((packageInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == PackageInfo.REQUESTED_PERMISSION_GRANTED) {
					builder.append(packageInfo.requestedPermissions[i]).append("\n");
				}
			}
		} catch (Exception ignored) {
		}
		builder.append("\n\n\n\n");
		
		Intent reportBugIntent = new Intent(Intent.ACTION_SENDTO);
		reportBugIntent.setData(Uri.parse("mailto:"));
		reportBugIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"studypartnerapp@gmail.com"});
		reportBugIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report for Study Partner");
		reportBugIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
		activity.startActivity(Intent.createChooser(reportBugIntent, "Choose your email client"));
		
	}
}

package com.studypartner.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.models.ReminderItem;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class ReminderAlertBootReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getAction() != null && (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") || intent.getAction().equals("android.intent.action.TIME_SET") || intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON")) && FirebaseAuth.getInstance().getCurrentUser() != null) {
			Log.d("TAG", "onReceive: " + intent.getAction());
			
			AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			
			ArrayList<ReminderItem> mReminderList = null;
			
			SharedPreferences reminderPreference = context.getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "REMINDER", MODE_PRIVATE);
			Gson gson = new Gson();
			
			if (reminderPreference.getBoolean("REMINDER_ITEMS_EXISTS", false)) {
				String json = reminderPreference.getString("REMINDER_ITEMS", "");
				Type type = new TypeToken<ArrayList<ReminderItem>>() {
				}.getType();
				mReminderList = gson.fromJson(json, type);
			}
			
			if (mReminderList == null) {
				mReminderList = new ArrayList<>();
			}
			
			for (ReminderItem item: mReminderList) {
				if (item.isActive()) {
					Calendar calendar = Calendar.getInstance();
					
					int year = Integer.parseInt(item.getDate().substring(6));
					int month = Integer.parseInt(item.getDate().substring(3, 5)) - 1;
					int day = Integer.parseInt(item.getDate().substring(0, 2));
					
					int hour = Integer.parseInt(item.getTime().substring(0, 2));
					int minute = Integer.parseInt(item.getTime().substring(3, 5));
					
					String am_pm = item.getTime().substring(6);
					
					if (am_pm.equals("PM") && hour != 12)
						hour = hour + 12;
					
					calendar.set(Calendar.YEAR, year);
					calendar.set(Calendar.MONTH, month);
					calendar.set(Calendar.DAY_OF_MONTH, day);
					calendar.set(Calendar.HOUR_OF_DAY, hour);
					calendar.set(Calendar.MINUTE, minute);
					calendar.set(Calendar.SECOND, 0);
					
					Intent reminderIntent = new Intent(context, ReminderAlertReceiver.class);
					
					Bundle bundle = new Bundle();
					bundle.putParcelable("BUNDLE_REMINDER_ITEM", item);
					
					reminderIntent.putExtra("EXTRA_REMINDER_ITEM", bundle);
					
					PendingIntent pendingIntent = PendingIntent.getBroadcast(context, item.getNotifyId(), reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
				}
			}
		}
		
	}
}


package com.studypartner.models;


import android.util.Patterns;

import java.util.UUID;

public class User {
	private String fullName, username, email;
	private Boolean isEmailVerified;
	
	public User() {
	}
	
	public User(String fullName, String email, Boolean isEmailVerified) {
		this.fullName = fullName;
		this.email = email;
		this.isEmailVerified = isEmailVerified;
		generateUsername();
	}
	
	public User(String fullName, String username, String email, Boolean isEmailVerified) {
		this.fullName = fullName;
		this.username = username;
		this.email = email;
		this.isEmailVerified = isEmailVerified;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setEmailVerified(Boolean isEmailVerified) {
		this.isEmailVerified = isEmailVerified;
	}
	
	public void generateUsername() {
		String newEmail = email.substring(0, email.indexOf("@")) + UUID.randomUUID().toString().substring(0, 5);
		
		if (newEmail.length() > 25) {
			this.username = newEmail.substring(0, 25);
		} else {
			this.username = newEmail;
		}
	}
	
	public String validateName(String name) {
		if (name.trim().length() == 0) {
			return "Name cannot be empty";
		} else if (name.trim().matches("^[0-9]+$")) {
			return "Name cannot have numbers in it";
		} else if (!name.trim().matches("^[a-zA-Z][a-zA-Z ]++$")) {
			return "Invalid Name";
		}
		return null;
	}
	
	public String validateUsername(String username) {
		if (username.trim().length() == 0) {
			return "Username cannot be empty";
		} else if (username.trim().length() < 5) {
			return "Username too small. Minimum length is 5";
		} else if (username.trim().length() > 25) {
			return "Username too long. Maximum length is 25";
		} else if (!username.trim().matches("^[a-zA-Z][a-zA-Z0-9]+$")) {
			return "Username can only contain letters and numbers";
		}
		return null;
	}
	
	public String validateEmail(String email) {
		if (email.trim().length() == 0) {
			return "Email cannot be empty";
		} else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
			return "Invalid Email";
		}
		return null;
	}
	
	public String validatePassword(String password, String confirmPassword) {
		if (password.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (confirmPassword.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (!confirmPassword.trim().matches(password.trim())) {
			return "Passwords do not match";
		} else if (password.trim().length() < 8) {
			return "Password too small. Minimum length is 8";
		} else if (password.trim().length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.trim().contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.trim().contains("@") || password.trim().contains("#")) || password.trim().contains("$") || password.trim().contains("%") || password.trim().contains("*") || password.trim().contains(".") || password.trim().matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
	
	public String validateConfirmPassword(String confirmPassword, String password) {
		if (confirmPassword.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (password.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (!confirmPassword.trim().matches(password.trim())) {
			return "Passwords do not match";
		} else if (password.trim().length() < 8) {
			return "Password too small. Minimum length is 8";
		} else if (password.trim().length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.trim().contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.trim().contains("@") || password.trim().contains("#")) || password.trim().contains("$") || password.trim().contains("%") || password.trim().contains("*") || password.trim().contains(".") || password.trim().matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
}

package com.studypartner.models;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AttendanceItem {
	private String id;
	private int attendedClasses, totalClasses, missedClasses, classesNeededToAttend;
	private double attendedPercentage, requiredPercentage;
	private String subjectName;
	
	public AttendanceItem() {
	}
	
	public AttendanceItem(String subjectName, double requiredPercentage, int attendedClasses, int missedClasses) {
		this.id = String.valueOf(UUID.randomUUID());
		this.subjectName = subjectName;
		this.requiredPercentage = requiredPercentage;
		this.attendedClasses = attendedClasses;
		this.missedClasses = missedClasses;
		this.totalClasses = attendedClasses + missedClasses;
		this.classesNeededToAttend = classesNeededToAttend();
		this.attendedPercentage = attendedPercentage();
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public int getAttendedClasses() {
		return attendedClasses;
	}
	
	public int getTotalClasses() {
		return totalClasses;
	}
	
	public int getMissedClasses() {
		return totalClasses - attendedClasses;
	}
	
	public int getClassesNeededToAttend() {
		return classesNeededToAttend;
	}
	
	public double getAttendedPercentage() {
		return attendedPercentage;
	}
	
	public double getRequiredPercentage() {
		return requiredPercentage;
	}
	
	public void setRequiredPercentage(double requiredPercentage) {
		this.requiredPercentage = requiredPercentage;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
	
	public void increaseAttendedClasses() {
		this.attendedClasses++;
		this.totalClasses++;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public void increaseMissedClasses() {
		this.missedClasses++;
		this.totalClasses++;
		this.attendedPercentage = attendedPercentage();
		this.classesNeededToAttend = classesNeededToAttend();
	}
	
	public void decreaseAttendedClasses() {
		if (this.totalClasses > 0 && this.attendedClasses > 0) {
			this.totalClasses--;
			this.attendedClasses--;
			this.attendedPercentage = attendedPercentage();
			this.classesNeededToAttend = classesNeededToAttend();
		}
	}
	
	public void decreaseMissedClasses() {
		if (this.totalClasses > 0 && this.missedClasses > 0) {
			this.totalClasses--;
			this.missedClasses--;
			this.attendedPercentage = attendedPercentage();
			this.classesNeededToAttend = classesNeededToAttend();
		}
	}
	
	private double attendedPercentage() {
		return (totalClasses == 0) ? 0 : (double) (attendedClasses * 100) / totalClasses;
	}
	
	private int classesNeededToAttend() {
		int classesNeeded;
		
		if (totalClasses == 0) {
			classesNeeded = 0;
		} else if (attendedPercentage < requiredPercentage) {
			classesNeeded = (int) Math.ceil((((requiredPercentage / 100) * totalClasses) - attendedClasses) / (1 - (requiredPercentage / 100)));
		} else {
			classesNeeded = (int) ((((requiredPercentage / 100) * totalClasses) - attendedClasses) / (requiredPercentage / 100));
		}
		return classesNeeded;
	}
	
	@NotNull
	@Override
	public String toString() {
		return "AttendanceItem{" +
				"id='" + id + '\'' +
				", attendedClasses=" + attendedClasses +
				", totalClasses=" + totalClasses +
				", missedClasses=" + missedClasses +
				", classesNeededToAttend=" + classesNeededToAttend +
				", attendedPercentage=" + attendedPercentage +
				", requiredPercentage=" + requiredPercentage +
				", subjectName='" + subjectName + '\'' +
				'}';
	}
}

package com.studypartner.models;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.studypartner.utils.FileType;
import com.studypartner.utils.FileUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

public class FileItem implements Parcelable {
	
	public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
		@Override
		public FileItem createFromParcel(Parcel in) {
			return new FileItem(in);
		}
		
		@Override
		public FileItem[] newArray(int size) {
			return new FileItem[size];
		}
	};
	private String path;
	private String name;
	private FileType type;
	private String dateCreated, dateModified;
	private boolean isStarred;
	private long size;
	
	public FileItem() {
	}
	
	public FileItem(String path) {
		File file = new File(path);
		this.path = path;
		this.name = file.getName();
		this.type = FileUtils.getFileType(file);
		this.dateModified = String.valueOf(file.lastModified());
		this.isStarred = false;
		this.size = getFolderSize(file);
		try {
			setCreationTime(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected FileItem(Parcel in) {
		path = in.readString();
		name = in.readString();
		type = FileType.valueOf(in.readString());
		size = in.readLong();
		dateModified = in.readString();
		dateCreated = in.readString();
		isStarred = Boolean.parseBoolean(in.readString());
	}
	
	public static long getFolderSize(File file) {
		long size = 0;
		if (file.isDirectory()) {
			try {
				File[] files = file.listFiles();
				if (files != null && files.length > 0) {
					for (File f : files) {
						size += getFolderSize(f);
					}
				}
			} catch (NullPointerException ignored) {
			}
		} else {
			size = file.length();
		}
		return size;
	}
	
	private void setCreationTime(File file) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			this.dateCreated = Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toString();
		} else {
			this.dateCreated = String.valueOf(file.lastModified());
		}
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
		File file = new File(path);
		this.dateModified = String.valueOf(file.lastModified());
		this.size = getFolderSize(file);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		File file = new File(path);
		this.dateModified = String.valueOf(file.lastModified());
		this.size = getFolderSize(file);
	}
	
	public FileType getType() {
		return type;
	}
	
	public void setType(FileType type) {
		this.type = type;
		File file = new File(path);
		this.dateModified = String.valueOf(file.lastModified());
		this.size = getFolderSize(file);
	}
	
	public long getSize() {
		return size;
	}
	
	public String getDateCreated() {
		return dateCreated;
	}
	
	public String getDateModified() {
		return dateModified;
	}
	
	public boolean isStarred() {
		return isStarred;
	}
	
	public void setStarred(boolean starred) {
		this.isStarred = starred;
		File file = new File(path);
		this.dateModified = String.valueOf(file.lastModified());
		this.size = getFolderSize(file);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(path);
		parcel.writeString(name);
		parcel.writeString(type.name());
		parcel.writeLong(size);
		parcel.writeString(dateModified);
		parcel.writeString(dateCreated);
		parcel.writeString(String.valueOf(isStarred));
	}
	
	@NotNull
	@Override
	public String toString() {
		return "FileItem{" +
				"path='" + path + '\'' +
				", name='" + name + '\'' +
				", type=" + type +
				", size=" + size + '\'' +
				", dateCreated='" + dateCreated + '\'' +
				", dateModified='" + dateModified + '\'' +
				", isStarred=" + isStarred +
				'}';
	}
}

package com.studypartner.models;

public class OnBoardingItem {
	
	private final String title;
	private final String description;
	private final int image;
	
	public OnBoardingItem(String title, String description, int image) {
		this.title = title;
		this.description = description;
		this.image = image;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getImage() {
		return image;
	}
	
}

package com.studypartner.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ReminderItem implements Parcelable {
	private String time;
	private String date;
	private String title;
	private String description;
	private int notifyId;
	private boolean active;
	
	public ReminderItem() {}
	
	public ReminderItem(String title, String description, String time, String date) {
		this.title = title;
		this.description = description;
		this.time = time;
		this.date = date;
		this.active = true;
		createNotifyId();
	}
	
	protected ReminderItem(Parcel in) {
		title = in.readString();
		description = in.readString();
		date = in.readString();
		time = in.readString();
		notifyId = in.readInt();
		active = in.readInt() == 1;
	}
	
	public static final Creator<ReminderItem> CREATOR = new Creator<ReminderItem>() {
		@Override
		public ReminderItem createFromParcel(Parcel in) {
			return new ReminderItem(in);
		}
		
		@Override
		public ReminderItem[] newArray(int size) {
			return new ReminderItem[size];
		}
	};
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setDate(String setdate) {
		date = setdate;
	}
	
	public void setDescription(String setDes) {
		description = setDes;
	}
	
	public void setTime(String setTime) {
		time = setTime;
	}
	
	public void edit (String editTitle, String editDescription, String editTime, String editDate) {
		this.title = editTitle;
		this.description = editDescription;
		this.date = editDate;
		this.time = editTime;
		createNotifyId();
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getNotifyId() {
		return notifyId;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void createNotifyId() {
		Random random = new Random();
		notifyId = random.nextInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(title);
		parcel.writeString(description);
		parcel.writeString(date);
		parcel.writeString(time);
		parcel.writeInt(notifyId);
		parcel.writeInt(active ? 1 : 0);
	}
	
	@NotNull
	@Override
	public String toString() {
		return "ReminderItem{" +
				"title='" + title + '\'' +
				", description='" + description + '\'' +
				", date='" + date + '\'' +
				", time='" + time + '\'' +
				", notifyId=" + notifyId +
				", active=" + active +
				'}';
	}
}


package com.studypartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.models.AttendanceItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {
	
	private final Context mContext;
	private final AttendanceItemClickListener mAttendanceItemClickListener;
	private final ArrayList<AttendanceItem> mAttendanceItemArrayList;
	
	public AttendanceAdapter(Context context, ArrayList<AttendanceItem> attendanceItemArrayList, AttendanceItemClickListener attendanceItemClickListener) {
		mContext = context;
		mAttendanceItemArrayList = attendanceItemArrayList;
		mAttendanceItemClickListener = attendanceItemClickListener;
	}
	
	@NonNull
	@Override
	public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.attendance_item, parent, false);
		return new AttendanceViewHolder(itemView, mAttendanceItemClickListener);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final AttendanceViewHolder holder, final int position) {
		
		final AttendanceItem item = mAttendanceItemArrayList.get(position);
		
		DecimalFormat decimalFormat = new DecimalFormat("##.#");
		
		holder.deleteButton.setClickable(true);
		
		holder.subjectName.setText(item.getSubjectName());
		holder.classesAttended.setText(mContext.getString(R.string.attendance_item_attended, item.getAttendedClasses()));
		holder.classesMissed.setText(mContext.getString(R.string.attendance_item_missed, item.getMissedClasses()));
		holder.attendedProgressBar.setProgress((float) item.getAttendedPercentage());
		
		if (item.getTotalClasses() > 0) {
			holder.percentageAttended.setText(mContext.getString(R.string.attendance_percentage, decimalFormat.format(item.getAttendedPercentage())));
			
			if (item.getAttendedPercentage() < item.getRequiredPercentage()) {
				holder.attendedProgressBar.setProgressBarColor(mContext.getColor(R.color.lowAttendanceColor));
				holder.percentageAttended.setTextColor(mContext.getColor(R.color.lowAttendanceColor));
				
				if (item.getClassesNeededToAttend() > 1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), "es"));
				} else if (item.getClassesNeededToAttend() == 1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), ""));
				} else {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_cannot_miss));
				}
				
			} else {
				holder.attendedProgressBar.setProgressBarColor(mContext.getColor(R.color.highAttendanceColor));
				holder.percentageAttended.setTextColor(mContext.getColor(R.color.highAttendanceColor));
				
				if (item.getClassesNeededToAttend() < -1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_you_can_miss, item.getClassesNeededToAttend() * (-1), "es"));
				} else if (item.getClassesNeededToAttend() == -1) {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_you_can_miss, item.getClassesNeededToAttend() * (-1), ""));
				} else {
					holder.classesText.setText(mContext.getString(R.string.attendance_item_cannot_miss));
				}
			}
			
		} else {
			holder.percentageAttended.setText(mContext.getString(R.string.attendance_item_empty_percentage));
			holder.classesText.setText("");
		}
	}
	
	@Override
	public int getItemCount() {
		return mAttendanceItemArrayList.size();
	}
	
	public interface AttendanceItemClickListener {
		
		void onAttendedPlusButtonClicked(int position);
		
		void onAttendedMinusButtonClicked(int position);
		
		void onMissedPlusButtonClicked(int position);
		
		void onMissedMinusButtonClicked(int position);
		
		void editButtonClicked(int position);
		
		void deleteButtonClicked(int position);
	}
	
	static class AttendanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		private final TextView subjectName;
		private final TextView percentageAttended;
		private final TextView classesAttended;
		private final TextView classesMissed;
		private final TextView classesText;
		private final Button attendedPlusButton;
		private final Button attendedMinusButton;
		private final Button missedPlusButton;
		private final Button missedMinusButton;
		private final ImageButton editButton;
		private final ImageButton deleteButton;
		private final CircularProgressBar attendedProgressBar;
		
		private final AttendanceItemClickListener mClickListener;
		
		public AttendanceViewHolder(@NonNull View itemView, AttendanceItemClickListener listener) {
			super(itemView);
			
			mClickListener = listener;
			
			subjectName = itemView.findViewById(R.id.attendanceItemSubjectName);
			percentageAttended = itemView.findViewById(R.id.attendanceItemPercentageAttended);
			classesText = itemView.findViewById(R.id.attendanceItemClassesText);
			classesAttended = itemView.findViewById(R.id.attendanceItemAttended);
			classesMissed = itemView.findViewById(R.id.attendanceItemMissed);
			
			attendedPlusButton = itemView.findViewById(R.id.attendanceItemAttendedPlusButton);
			attendedPlusButton.setOnClickListener(this);
			attendedMinusButton = itemView.findViewById(R.id.attendanceItemAttendedMinusButton);
			attendedMinusButton.setOnClickListener(this);
			missedPlusButton = itemView.findViewById(R.id.attendanceItemMissedPlusButton);
			missedPlusButton.setOnClickListener(this);
			missedMinusButton = itemView.findViewById(R.id.attendanceItemMissedMinusButton);
			missedMinusButton.setOnClickListener(this);
			editButton = itemView.findViewById(R.id.attendanceItemEditButton);
			editButton.setOnClickListener(this);
			deleteButton = itemView.findViewById(R.id.attendanceItemDeleteButton);
			deleteButton.setOnClickListener(this);
			
			attendedProgressBar = itemView.findViewById(R.id.attendanceItemAttendedPercentageProgressBar);
		}
		
		@Override
		public void onClick(View v) {
			
			if (v.getId() == attendedPlusButton.getId()) {
				
				mClickListener.onAttendedPlusButtonClicked(getAdapterPosition());
				
			} else if (v.getId() == attendedMinusButton.getId()) {
				
				mClickListener.onAttendedMinusButtonClicked(getAdapterPosition());
				
			} else if (v.getId() == missedPlusButton.getId()) {
				
				mClickListener.onMissedPlusButtonClicked(getAdapterPosition());
				
			} else if (v.getId() == missedMinusButton.getId()) {
				
				mClickListener.onMissedMinusButtonClicked(getAdapterPosition());
				
			} else if (v.getId() == editButton.getId()) {
				
				mClickListener.editButtonClicked(getAdapterPosition());
				
			} else if (v.getId() == deleteButton.getId()) {
				
				mClickListener.deleteButtonClicked(getAdapterPosition());
				deleteButton.setClickable(false);
			}
		}
	}
}


package com.studypartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.studypartner.R;
import com.studypartner.models.AttendanceItem;

import java.text.DecimalFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeAttendanceAdapter extends RecyclerView.Adapter<HomeAttendanceAdapter.HomeAttendanceViewHolder> {
	
	private final Context mContext;
	private final ArrayList<AttendanceItem> mAttendanceItems;
	
	public HomeAttendanceAdapter(Context context, ArrayList<AttendanceItem> attendanceItems) {
		mContext = context;
		mAttendanceItems = attendanceItems;
	}
	
	@NonNull
	@Override
	public HomeAttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.home_carousel_attendance_item, parent, false);
		return new HomeAttendanceViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull HomeAttendanceViewHolder holder, int position) {
		
		final AttendanceItem item = mAttendanceItems.get(position);
		
		DecimalFormat decimalFormat = new DecimalFormat("##.#");
		
		holder.subjectName.setText(item.getSubjectName());
		holder.percentageAttended.setText(mContext.getString(R.string.attendance_percentage, decimalFormat.format(item.getAttendedPercentage())));
		
		if (item.getClassesNeededToAttend() > 1) {
			holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), "es"));
		} else if (item.getClassesNeededToAttend() == 1) {
			holder.classesText.setText(mContext.getString(R.string.attendance_item_need_to_attend, item.getClassesNeededToAttend(), ""));
		}
		
		holder.attendedProgressBar.setProgress((float) item.getAttendedPercentage());
		holder.requiredProgressBar.setProgress((float) item.getRequiredPercentage());
		
	}
	
	@Override
	public int getItemCount() {
		return mAttendanceItems.size();
	}
	
	public static class HomeAttendanceViewHolder extends RecyclerView.ViewHolder {
		
		private final TextView subjectName;
		private final TextView classesText;
		private final TextView percentageAttended;
		private final CircularProgressBar attendedProgressBar;
		private final CircularProgressBar requiredProgressBar;
		
		public HomeAttendanceViewHolder(@NonNull View itemView) {
			super(itemView);
			
			subjectName = itemView.findViewById(R.id.homeCarouselAttendanceSubjectName);
			classesText = itemView.findViewById(R.id.homeCarouselAttendanceClassesNeededToAttend);
			percentageAttended = itemView.findViewById(R.id.homeCarouselAttendancePercentageAttended);
			
			attendedProgressBar = itemView.findViewById(R.id.homeCarouselAttendanceAttendedProgressBar);
			requiredProgressBar = itemView.findViewById(R.id.homeCarouselAttendanceRequiredProgressBar);
		}
	}
}


package com.studypartner.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
	
	private final Activity mActivity;
	private final ArrayList<FileItem> mFileItems;
	private final ArrayList<FileItem> mFileItemsCopy;
	private final SparseBooleanArray selectedItems;
	private final NotesClickListener listener;
	private final boolean isOptionsVisible;
	
	public NotesAdapter(Activity activity, ArrayList<FileItem> fileItems, NotesClickListener listener, boolean isOptionVisible) {
		this.mActivity = activity;
		this.mFileItems = fileItems;
		this.mFileItemsCopy = new ArrayList<>();
		mFileItemsCopy.addAll(mFileItems);
		this.listener = listener;
		selectedItems = new SparseBooleanArray();
		this.isOptionsVisible = isOptionVisible;
	}
	
	@NonNull
	@Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.notes_item, parent, false);
		return new NotesAdapter.NotesViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final NotesViewHolder holder, final int position) {
		
		final FileItem fileItem = mFileItems.get(position);
		
		holder.fileName.setText(fileItem.getName());
		
		if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
			
			if (fileItem.isStarred()) {
				holder.fileImage.setImageResource(R.drawable.folder_starred_icon);
			} else {
				holder.fileImage.setImageResource(R.drawable.folder_icon);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_IMAGE) {
			
			File image = new File(fileItem.getPath());
			if (image.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(image)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.error(R.drawable.image_add_icon_bs)
						.into(holder.fileImage);
				
			} else {
				holder.fileImage.setImageResource(R.drawable.image_add_icon_bs);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_VIDEO) {
			
			File video = new File(fileItem.getPath());
			if (video.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(video)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.error(R.drawable.video_add_icon_bs)
						.into(holder.fileImage);
				
			} else {
				holder.fileImage.setImageResource(R.drawable.video_add_icon_bs);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_AUDIO) {
			
			holder.fileImage.setImageResource(R.drawable.headphone_icon);
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
			
			holder.fileImage.setImageResource(R.drawable.link_icon);
			
		} else {
			if (fileItem.getName().contains(".doc") || fileItem.getName().contains(".docx")) {
				holder.fileImage.setImageResource(R.drawable.doc_icon);
			} else if (fileItem.getName().contains(".pdf")) {
				holder.fileImage.setImageResource(R.drawable.pdf_icon);
			} else if (fileItem.getName().contains(".ppt") || fileItem.getName().contains(".pptx")) {
				holder.fileImage.setImageResource(R.drawable.ppt_icon);
			} else if (fileItem.getName().contains(".xls") || fileItem.getName().contains(".xlsx")) {
				holder.fileImage.setImageResource(R.drawable.excel_icon);
			} else if (fileItem.getName().contains(".zip") || fileItem.getName().contains(".rar")) {
				holder.fileImage.setImageResource(R.drawable.zip_icon);
			} else if (fileItem.getName().contains(".txt")) {
				holder.fileImage.setImageResource(R.drawable.txt_icon);
			} else {
				holder.fileImage.setImageResource(R.drawable.file_icon);
			}
		}
		
		holder.fileLayout.setBackground(ContextCompat.getDrawable(mActivity,R.drawable.notes_item_background_odd));
		
		if (selectedItems.get(position, false)) {
			holder.itemView.setActivated(true);
		} else {
			holder.itemView.setActivated(false);
			if (position % 2 == 0) {
				holder.fileLayout.setBackground(ContextCompat.getDrawable(mActivity,R.drawable.notes_item_background_even));
			}
		}
		
		if (selectedItems.size() > 0 || !isOptionsVisible) {
			holder.fileOptions.setVisibility(View.GONE);
		} else {
			holder.fileOptions.setVisibility(View.VISIBLE);
		}
		
		applyClickEvents(holder);
		
	}
	
	private void applyClickEvents(final NotesViewHolder holder) {
		holder.fileOptions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onOptionsClick(v, holder.getAdapterPosition());
			}
		});
		
		holder.fileLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listener.onClick(holder.getAdapterPosition());
			}
		});
		
		holder.fileLayout.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				listener.onLongClick(holder.getAdapterPosition());
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
		});
	}
	
	public void filter(String query) {
		mFileItems.clear();
		
		if (!query.isEmpty()) {
			for (FileItem item : mFileItemsCopy) {
				if (item.getName().toLowerCase().contains(query.toLowerCase())) {
					mFileItems.add(item);
				}
			}
			
		} else {
			mFileItems.addAll(mFileItemsCopy);
		}
		notifyDataSetChanged();
	}
	
	public void toggleSelection(int pos) {
		int oldSelectedItemSize = selectedItems.size();
		
		if (selectedItems.get(pos, false)) {
			selectedItems.delete(pos);
		} else {
			selectedItems.put(pos, true);
		}
		
		if (selectedItems.size() == 1 && selectedItems.size() > oldSelectedItemSize) {
			notifyDataSetChanged();
		} else {
			notifyItemChanged(pos);
		}
	}
	
	public void selectAll() {
		for (int i = 0; i < getItemCount(); i++) {
			if (!selectedItems.get(i, false)) {
				selectedItems.put(i, true);
			}
		}
		notifyDataSetChanged();
	}
	
	public void clearSelections() {
		selectedItems.clear();
		notifyDataSetChanged();
	}
	
	public int getSelectedItemCount() {
		return selectedItems.size();
	}
	
	public ArrayList<Integer> getSelectedItems() {
		ArrayList<Integer> items = new ArrayList<>(selectedItems.size());
		for (int i = 0; i < selectedItems.size(); i++) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}
	
	@Override
	public int getItemCount() {
		return mFileItems.size();
	}
	
	public interface NotesClickListener {
		void onClick(int position);
		
		void onLongClick(int position);
		
		void onOptionsClick(View view, int position);
	}
	
	public class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		
		final TextView fileName;
		final ImageView fileImage;
		final ImageButton fileOptions;
		final CardView fileLayout;
		
		public NotesViewHolder(View view) {
			super(view);
			
			fileName = view.findViewById(R.id.fileName);
			fileImage = view.findViewById(R.id.fileImage);
			fileLayout = view.findViewById(R.id.fileLayout);
			fileOptions = view.findViewById(R.id.fileOptions);
			
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
		}
		
		@Override
		public void onClick(View v) {
			listener.onClick(getAdapterPosition());
		}
		
		@Override
		public boolean onLongClick(View v) {
			listener.onLongClick(getAdapterPosition());
			v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			return true;
		}
	}
}

package com.studypartner.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.zerobranch.layout.SwipeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
	
	private final Context context;
	private final ReminderItemClickListener mReminderItemClickListener;
	private final ArrayList<ReminderItem> mReminderList;
	
	public ReminderAdapter(Context context, ArrayList<ReminderItem> mReminderList, ReminderItemClickListener mReminderItemClickListener) {
		this.context = context;
		this.mReminderItemClickListener = mReminderItemClickListener;
		this.mReminderList = mReminderList;
	}
	
	@NotNull
	@Override
	public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.reminder_item, parent, false);
		return new ReminderViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ReminderAdapter.ReminderViewHolder holder, int position) {
		final ReminderItem item = mReminderList.get(position);
		holder.title.setText(item.getTitle());
		holder.date.setText(item.getDate());
		holder.time.setText(item.getTime());
		if (item.isActive()) {
			
			holder.activeIcon.setImageResource(R.drawable.alarm_icon);
			holder.activeIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.reminderCalendar.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.reminderClock.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.title.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.date.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			holder.time.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary, context.getTheme())));
			
		} else {
			
			holder.activeIcon.setImageResource(R.drawable.alarm_off_icon);
			holder.activeIcon.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.reminderCalendar.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.reminderClock.setImageTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.title.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.date.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			holder.time.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.bottomSheetBackground, context.getTheme())));
			
		}
		
		applyClickEvents(holder);
	}
	
	private void applyClickEvents(final ReminderViewHolder holder) {
		holder.reminderLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mReminderItemClickListener.onClick(holder.getAdapterPosition());
			}
		});
		holder.delete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				holder.swipeLayout.close();
				mReminderItemClickListener.deleteView(holder.getAdapterPosition());
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return mReminderList.size();
	}
	
	public interface ReminderItemClickListener {
		void onClick(int position);
		
		void deleteView(int position);
	}
	
	public class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		private final TextView title;
		private final TextView date;
		private final TextView time;
		private final CardView reminderLayout;
		private final SwipeLayout swipeLayout;
		private final ImageView activeIcon;
		private final ImageView reminderClock;
		private final ImageView reminderCalendar;
		private final ImageView delete;
		
		public ReminderViewHolder(@NonNull View itemView) {
			super(itemView);
			
			title = itemView.findViewById(R.id.reminderItemTitle);
			date = itemView.findViewById(R.id.reminderItemDate);
			time = itemView.findViewById(R.id.reminderItemTime);
			swipeLayout = itemView.findViewById(R.id.swipeLayout);
			activeIcon = itemView.findViewById(R.id.reminderActiveIcon);
			reminderCalendar = itemView.findViewById(R.id.reminderCalendar);
			reminderClock = itemView.findViewById(R.id.reminderClock);
			reminderLayout = itemView.findViewById(R.id.reminderItemCard);
			delete = itemView.findViewById(R.id.reminderItemDeleteIcon);
			
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view) {
			mReminderItemClickListener.onClick(getAdapterPosition());
		}
		
	}
}


package com.studypartner.adapters;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MediaAdapter extends FragmentStateAdapter {
	private final ArrayList<Fragment> list = new ArrayList<>();
	
	public MediaAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
		super(fm, lifecycle);
	}
	
	public void addFragment(Fragment frag) {
		list.add(frag);
	}
	
	@NonNull
	@Override
	public Fragment createFragment(int position) {
		return list.get(position);
	}
	
	@Override
	public int getItemCount() {
		return list.size();
	}
}

package com.studypartner.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeMediaAdapter extends RecyclerView.Adapter<HomeMediaAdapter.HomeMediaViewHolder> {
	
	private final Activity mActivity;
	private final ArrayList<FileItem> mFileItems;
	private final HomeMediaClickListener listener;
	
	public HomeMediaAdapter(Activity mActivity, ArrayList<FileItem> mFileItems, HomeMediaClickListener listener) {
		this.mActivity = mActivity;
		this.mFileItems = mFileItems;
		this.listener = listener;
	}
	
	@NonNull
	@Override
	public HomeMediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.home_media_item, parent, false);
		return new HomeMediaAdapter.HomeMediaViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull HomeMediaViewHolder holder, int position) {
		final FileItem fileItem = mFileItems.get(position);
		
		if (fileItem.getType() == FileType.FILE_TYPE_IMAGE) {
			
			File image = new File(fileItem.getPath());
			if (image.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(image)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.into(holder.homeMediaImage);
				
			} else {
				holder.homeMediaImage.setImageResource(R.drawable.image_add_icon_bs);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_VIDEO) {
			
			File video = new File(fileItem.getPath());
			if (video.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(video)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.into(holder.homeMediaImage);
				
			} else {
				holder.homeMediaImage.setImageResource(R.drawable.video_add_icon_bs);
			}
			
		}
		
		applyClickEvents(holder);
		
	}
	
	private void applyClickEvents(final HomeMediaViewHolder holder) {
		holder.homeMediaImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listener.onClick(holder.getAdapterPosition());
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return mFileItems.size();
	}
	
	public interface HomeMediaClickListener {
		void onClick(int position);
	}
	
	public class HomeMediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		private final ImageView homeMediaImage;
		
		public HomeMediaViewHolder(@NonNull View itemView) {
			super(itemView);
			homeMediaImage = itemView.findViewById(R.id.homeMediaImage);
			itemView.setOnClickListener(this);
		}
		
		@Override
		public void onClick(View view) {
			listener.onClick(getAdapterPosition());
		}
	}
}


package com.studypartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.models.OnBoardingItem;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class OnBoardingViewPagerAdapter extends PagerAdapter {
	
	final Context mContext;
	final List<OnBoardingItem> mListScreen;
	
	public OnBoardingViewPagerAdapter(Context mContext, List<OnBoardingItem> mListScreen) {
		this.mContext = mContext;
		this.mListScreen = mListScreen;
	}
	
	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layoutScreen = inflater.inflate(R.layout.on_boarding_screen_page_layout, container, false);
		
		ImageView image = layoutScreen.findViewById(R.id.onBoardingScreenItemImage);
		TextView title = layoutScreen.findViewById(R.id.onBoardingScreenItemTitle);
		TextView description = layoutScreen.findViewById(R.id.onBoardingScreenItemDescription);
		
		title.setText(mListScreen.get(position).getTitle());
		description.setText(mListScreen.get(position).getDescription());
		image.setImageResource(mListScreen.get(position).getImage());
		
		container.addView(layoutScreen);
		
		return layoutScreen;
	}
	
	@Override
	public int getCount() {
		return mListScreen.size();
	}
	
	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}
	
	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		container.removeView((View) object);
	}
}

package com.studypartner.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.R;
import com.studypartner.models.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {
	private static final String TAG = "CreateAccountActivity";
	
	private TextInputLayout nameTextInput, usernameTextInput, emailTextInput, passwordTextInput, confirmPasswordTextInput;
	private TextInputEditText nameEditText, usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
	
	private String fullName, username, email;
	
	@Override
	public void onBackPressed() {
		Log.d(TAG, "onClick: creating shared animation on back pressed");
		
		finish();
		
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_create_account);
		
		//Setting hooks
		
		nameTextInput = findViewById(R.id.createAccountScreenFullNameTextInput);
		nameEditText = findViewById(R.id.createAccountScreenFullNameEditText);
		
		nameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				nameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		usernameTextInput = findViewById(R.id.createAccountScreenUsernameTextInput);
		usernameEditText = findViewById(R.id.createAccountScreenUsernameEditText);
		
		usernameEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				usernameTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		emailTextInput = findViewById(R.id.createAccountScreenEmailTextInput);
		emailEditText = findViewById(R.id.createAccountScreenEmailEditText);
		
		emailEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				emailTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		passwordTextInput = findViewById(R.id.createAccountScreenPasswordTextInput);
		passwordEditText = findViewById(R.id.createAccountScreenPasswordEditText);
		
		passwordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		confirmPasswordTextInput = findViewById(R.id.createAccountScreenConfirmPasswordTextInput);
		confirmPasswordEditText = findViewById(R.id.createAccountScreenConfirmPasswordEditText);
		
		confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				confirmPasswordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		Button createAccountButton = findViewById(R.id.createAccountScreenCreateAccountButton);
		Button termsAndConditionsButton = findViewById(R.id.createAccountScreenTCButton);
		
		//Setting on click listeners
		
		findViewById(R.id.createAccountBackArrow).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: back arrow is pressed");
				onBackPressed();
			}
		});
		
		findViewById(R.id.createAccountScreenLoginButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: login button pressed");
				onBackPressed();
			}
		});
		
		createAccountButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: create account button pressed");
				
				if (validateFields()) {
					checkUsernameValidity();
				}
			}
		});
		
		termsAndConditionsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: terms and conditions pressed");
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1MKWGuegWbqugvAPzyNpF0oVYQpDsIZON1DJr8Ap9CWc/edit?usp=sharing"));
				startActivity(Intent.createChooser(browserIntent, "Select the app to open the link"));
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private void checkUsernameValidity() {
		
		findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.VISIBLE);
		
		FirebaseDatabase.getInstance().getReference().child("usernames").addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				if (snapshot.exists() && snapshot.hasChildren()) {
					for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
						if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
							if (username.trim().matches((String) dataSnapshot.getValue())) {
								usernameTextInput.setError("Username is already taken by another user");
								findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.INVISIBLE);
								return;
							}
						}
					}
				}
				createAccount();
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError error) {
				findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.INVISIBLE);
			}
		});
		
	}
	
	private void createAccount() {
		Log.d(TAG, "createAccount: starts");
		
		findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.VISIBLE);
		
		FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: account created");
							storeUserDetails();
						} else {
							findViewById(R.id.createAccountScreenProgressBar).setVisibility(View.INVISIBLE);
							StyleableToast.makeText(CreateAccountActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
					}
				});
	}
	
	private void storeUserDetails() {
		Log.d(TAG, "storeUserDetails: starts");
		final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
		
		final User user = new User(fullName, username, email, FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());
		
		UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
		profileUpdates.setDisplayName(fullName);
		
		FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates.build())
				.addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
							
							//Make users database
							FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user);
							
							//Make usernames database
							FirebaseDatabase.getInstance().getReference().child("usernames").child(uid).setValue(username);
							
							Log.d(TAG, "onComplete: starting main activity");
							startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
							
						} else {
							Log.d(TAG, "onComplete: Could not update display name");
						}
					}
				});
		
		FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful()) {
					Log.d(TAG, "onComplete: Verification mail sent successfully");
				} else {
					Log.d(TAG, "onComplete: Verification mail could not be sent");
				}
			}
		});
		
		Log.d(TAG, "storeUserDetails: ends");
	}
	
	private boolean validateFields() {
		fullName = nameEditText.getText().toString();
		String nameValidation = validateName(fullName);
		
		username = usernameEditText.getText().toString();
		String usernameValidation = validateUsername(username);
		
		email = emailEditText.getText().toString();
		String emailValidation = validateEmail(email);
		
		String passwordValidation = validatePassword(passwordEditText.getText().toString(), confirmPasswordEditText.getText().toString());
		
		String confirmPasswordValidation = validateConfirmPassword(confirmPasswordEditText.getText().toString(), passwordEditText.getText().toString());
		
		if (nameValidation == null && usernameValidation == null && emailValidation == null && passwordValidation == null && confirmPasswordValidation == null) {
			Log.d(TAG, "validateFields: all fields valid");
			return true;
		}
		
		if (nameValidation != null) {
			nameTextInput.setError(nameValidation);
		}
		
		if (usernameValidation != null) {
			usernameTextInput.setError(usernameValidation);
		}
		
		if (emailValidation != null) {
			emailTextInput.setError(emailValidation);
		}
		
		if (passwordValidation != null) {
			passwordTextInput.setError(passwordValidation);
		}
		
		if (confirmPasswordValidation != null) {
			confirmPasswordTextInput.setError(confirmPasswordValidation);
		}
		
		return false;
	}
	
	private String validateName(String name) {
		if (name.trim().length() == 0) {
			return "Name cannot be empty";
		} else if (name.trim().matches("^[0-9]+$")) {
			return "Name cannot have numbers in it";
		} else if (!name.trim().matches("^[a-zA-Z][a-zA-Z ]++$")) {
			return "Invalid Name";
		}
		return null;
	}
	
	private String validateUsername(final String username) {
		if (username.trim().length() == 0) {
			return "Username cannot be empty";
		} else if (username.trim().length() < 5) {
			return "Username too small. Minimum length is 5";
		} else if (username.trim().length() > 25) {
			return "Username too long. Maximum length is 25";
		} else if (!username.trim().matches("^[a-zA-Z][a-zA-Z0-9]+$")) {
			return "Username can only contain letters and numbers";
		}
		return null;
	}
	
	private String validateEmail(String email) {
		if (email.trim().length() == 0) {
			return "Email cannot be empty";
		} else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
			return "Invalid Email";
		}
		return null;
	}
	
	private String validatePassword(String password, String confirmPassword) {
		if (password.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (confirmPassword.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (!confirmPassword.trim().matches(password.trim())) {
			return "Passwords do not match";
		} else if (password.trim().length() < 8) {
			return "Password too small. Minimum length is 8";
		} else if (password.trim().length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.trim().contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.trim().contains("@") || password.trim().contains("#")) || password.trim().contains("$") || password.trim().contains("%") || password.trim().contains("*") || password.trim().contains(".") || password.trim().matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
	
	private String validateConfirmPassword(String confirmPassword, String password) {
		if (confirmPassword.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (password.trim().length() == 0) {
			return "Passwords cannot be empty";
		} else if (!confirmPassword.trim().matches(password.trim())) {
			return "Passwords do not match";
		} else if (password.trim().length() < 8) {
			return "Password too small. Minimum length is 8";
		} else if (password.trim().length() > 15) {
			return "Password too long. Maximum length is 15";
		} else if (password.trim().contains(" ")) {
			return "Password cannot contain spaces";
		} else if (!(password.trim().contains("@") || password.trim().contains("#")) || password.trim().contains("$") || password.trim().contains("%") || password.trim().contains("*") || password.trim().contains(".") || password.trim().matches("(((?=.*[a-z])(?=.*[A-Z]))|((?=.*[a-z])(?=.*[0-9]))|((?=.*[A-Z])(?=.*[0-9])))")) {
			return "Password should contain atleast one uppercase character or one number or any of the special characters from (@, #, $, %, *, .)";
		}
		return null;
	}
}

package com.studypartner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;
import com.studypartner.R;
import com.studypartner.adapters.OnBoardingViewPagerAdapter;
import com.studypartner.models.OnBoardingItem;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class OnBoardingActivity extends AppCompatActivity {
	private static final String TAG = "OnBoardingActivity";
	
	private ViewPager screenPager;
	private OnBoardingViewPagerAdapter onBoardingViewPagerAdapter;
	private TabLayout tabIndicator;
	private Button nextButton, getStartedButton, skipButton, backButton;
	private Animation buttonAnimation;
	private int currentPagePosition = 0;
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_on_boarding);
		
		nextButton = findViewById(R.id.onBoardingScreenNextButton);
		getStartedButton = findViewById(R.id.onBoardingScreenGetStartedButton);
		skipButton = findViewById(R.id.onBoardingScreenSkipButton);
		backButton = findViewById(R.id.onBoardingScreenBackButton);
		tabIndicator = findViewById(R.id.onBoardingScreenTabLayout);
		buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.on_boarding_get_started_animation);
		
		Log.d(TAG, "onCreate: Initialising screens for onBoarding");
		
		final List<OnBoardingItem> screenList = new ArrayList<>();
		screenList.add(new OnBoardingItem("NOTES KEEPER", "One step to keep and arrange all your notes!", R.drawable.on_boarding_notebook_image));
		screenList.add(new OnBoardingItem("ALL TYPES OF NOTES", "Store Images, Documents, Videos and much more", R.drawable.on_boarding_screen_types_of_notes_image));
		screenList.add(new OnBoardingItem("ATTENDANCE MANAGER", "We manage your attendance too", R.drawable.on_boarding_screen_attendance_image));
		
		screenPager = findViewById(R.id.onBoardingScreenViewPager);
		onBoardingViewPagerAdapter = new OnBoardingViewPagerAdapter(this, screenList);
		screenPager.setAdapter(onBoardingViewPagerAdapter);
		tabIndicator.setupWithViewPager(screenPager, true);
		
		if (screenList.size() == 1) {
			Log.d(TAG, "onCreate: onBoarding has only one screen");
			loadLastScreen();
		}
		
		nextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: nextButton pressed");
				backButton.setVisibility(View.VISIBLE);
				
				currentPagePosition = screenPager.getCurrentItem();
				
				if (currentPagePosition < screenList.size()) {
					currentPagePosition++;
					screenPager.setCurrentItem(currentPagePosition);
				}
				
				if (currentPagePosition == screenList.size() - 1) {
					Log.d(TAG, "onClick: Loading last screen for next button");
					loadLastScreen();
				}
				
			}
		});
		
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: backButton pressed");
				currentPagePosition = screenPager.getCurrentItem();
				
				nextButton.setVisibility(View.VISIBLE);
				skipButton.setVisibility(View.VISIBLE);
				tabIndicator.setVisibility(View.VISIBLE);
				getStartedButton.setVisibility(View.INVISIBLE);
				
				if (currentPagePosition > 0) {
					currentPagePosition--;
					screenPager.setCurrentItem(currentPagePosition);
				}
				
				if (currentPagePosition == 0) {
					Log.d(TAG, "onClick: loading first page for back button");
					loadFirstScreen();
				}
			}
		});
		
		skipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: loading LoginActivity for skip button");
				getSharedPreferences("OnBoarding", MODE_PRIVATE).edit().putBoolean("ON_BOARDING_SCREEN_VIEWED", true).apply();
				startActivity(new Intent(OnBoardingActivity.this, LoginActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		});
		
		getStartedButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: loading LoginActivity for get started button");
				getSharedPreferences("OnBoarding", MODE_PRIVATE).edit().putBoolean("ON_BOARDING_SCREEN_VIEWED", true).apply();
				startActivity(new Intent(OnBoardingActivity.this, LoginActivity.class));
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		});
		
		tabIndicator.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if (tab.getPosition() == screenList.size() - 1) {
					Log.d(TAG, "onTabSelected: loading last screen for tab indicator");
					loadLastScreen();
				} else if (tab.getPosition() == 0) {
					Log.d(TAG, "onTabSelected: loading first screen for tab indicator");
					loadFirstScreen();
				} else {
					nextButton.setVisibility(View.VISIBLE);
					skipButton.setVisibility(View.VISIBLE);
					tabIndicator.setVisibility(View.VISIBLE);
					getStartedButton.setVisibility(View.INVISIBLE);
					backButton.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			
			}
			
			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private void loadFirstScreen() {
		Log.d(TAG, "loadFirstScreen: starts");
		nextButton.setVisibility(View.VISIBLE);
		skipButton.setVisibility(View.VISIBLE);
		tabIndicator.setVisibility(View.VISIBLE);
		getStartedButton.setVisibility(View.INVISIBLE);
		backButton.setVisibility(View.INVISIBLE);
		Log.d(TAG, "loadFirstScreen: ends");
	}
	
	private void loadLastScreen() {
		Log.d(TAG, "loadLastScreen: starts");
		nextButton.setVisibility(View.INVISIBLE);
		skipButton.setVisibility(View.INVISIBLE);
		tabIndicator.setVisibility(View.INVISIBLE);
		getStartedButton.setVisibility(View.VISIBLE);
		backButton.setVisibility(View.VISIBLE);
		
		getStartedButton.setAnimation(buttonAnimation);
		Log.d(TAG, "loadLastScreen: ends");
	}
}

package com.studypartner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;
import com.studypartner.R;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {
	private static final String TAG = "SplashScreen";
	final String ON_BOARDING_SCREEN_VIEWED = "ON_BOARDING_SCREEN_VIEWED";
	Animation mAnimation;
	ImageView splashScreenAppLogo;
	TextView splashScreenAppName, splashScreenMadeInIndia;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.WHITE);
		
		overridePendingTransition(0, R.anim.slide_out_left);
		
		mAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		
		splashScreenAppLogo = findViewById(R.id.splashScreenAppLogo);
		splashScreenAppName = findViewById(R.id.splashScreenAppName);
		splashScreenMadeInIndia = findViewById(R.id.splashScreenMadeInIndia);
		
		TextPaint paint = splashScreenMadeInIndia.getPaint();
		float width = paint.measureText("Made in India");
		Shader textShader = new LinearGradient(0, 0, width, splashScreenMadeInIndia.getTextSize(),
				new int[]{
						Color.parseColor("#FF8000"),
						Color.parseColor("#BBBBBB"),
						Color.parseColor("#008000"),
				}, null, Shader.TileMode.CLAMP);
		splashScreenMadeInIndia.getPaint().setShader(textShader);
		
		splashScreenAppLogo.setAnimation(mAnimation);
		splashScreenAppName.setAnimation(mAnimation);
		splashScreenMadeInIndia.setAnimation(mAnimation);
		
		MobileAds.initialize(this);
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "run: Splash Screen Finished");
				SharedPreferences sharedPreferences = getSharedPreferences("OnBoarding", MODE_PRIVATE);
				if (sharedPreferences.getBoolean(ON_BOARDING_SCREEN_VIEWED, false)) {
					Log.d(TAG, "run: starting Login Activity");
					startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
				} else {
					Log.d(TAG, "run: starting On Boarding Activity");
					startActivity(new Intent(SplashScreenActivity.this, OnBoardingActivity.class));
				}
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
				finish();
			}
		}, 1500);
		
		Log.d(TAG, "onCreate: ends");
	}
}

package com.studypartner.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.muddzdev.styleabletoast.StyleableToast;
import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.models.ReminderItem;
import com.studypartner.utils.Connection;
import com.studypartner.utils.NotificationHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener, NavController.OnDestinationChangedListener {
	private static final String TAG = "MainActivity";
	public NavController mNavController;
	public BottomAppBar mBottomAppBar;
	public DrawerLayout mDrawerLayout;
	public FloatingActionButton fab;
	public Toolbar mToolbar;
	public NavOptions.Builder leftToRightBuilder, rightToLeftBuilder;
	private AppBarConfiguration mAppBarConfiguration;
	private BottomNavigationView mBottomNavigationView;
	private NavigationView mNavigationView;

	@Override
	public void onBackPressed() {
		Log.d(TAG, "onBackPressed: back pressed");
		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			Log.d(TAG, "onBackPressed: closing drawer");
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else if (mNavController.getCurrentDestination().getId() == R.id.nav_home) {
			Log.d(TAG, "onBackPressed: closing app");
			finishAffinity();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		} else {
			Log.d(TAG, "onBackPressed: navigating to home");
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		ImageView profileImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileImage);
		ImageView verifiedImage = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerVerifiedImage);
		TextView profileFullName = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerProfileName);
		TextView profileEmail = mNavigationView.getHeaderView(0).findViewById(R.id.navigationDrawerEmail);

		if (FirebaseAuth.getInstance().getCurrentUser() != null) {

			if (FirebaseAuth.getInstance().getCurrentUser().getDisplayName() != null) {
				profileFullName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
			}

			if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null) {
				Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
						.error(R.drawable.image_error_icon)
						.placeholder(R.drawable.user_icon)
						.into(profileImage);
			} else {
				profileImage.setImageResource(R.drawable.user_icon);
			}

			profileEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

			if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
				verifiedImage.setImageResource(R.drawable.verified_icon);
			} else {
				verifiedImage.setImageResource(R.drawable.not_verified_icon);
			}

			profileEmail.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
						FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
							@Override
							public void onSuccess(Void aVoid) {
								StyleableToast.makeText(MainActivity.this, "Verification email sent successfully", Toast.LENGTH_SHORT, R.style.designedToast).show();
							}
						});
					}
				}
			});

		} else {
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finishAffinity();
			overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//setting hooks

		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

		mDrawerLayout = findViewById(R.id.drawer_layout);
		mBottomNavigationView = findViewById(R.id.bottom_nav_view);
		mBottomAppBar = findViewById(R.id.bottom_app_bar);
		mNavigationView = findViewById(R.id.nav_view);
		fab = findViewById(R.id.fab);

		//set up navigation
		mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
		mNavController.addOnDestinationChangedListener(this);

		mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_attendance, R.id.nav_starred, R.id.nav_notes, R.id.nav_reminder, R.id.nav_profile, R.id.nav_about_us, R.id.nav_logout)
				.setDrawerLayout(mDrawerLayout)
				.build();

		NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(mNavigationView, mNavController);
		NavigationUI.setupWithNavController(mBottomNavigationView, mNavController);

		mNavigationView.setCheckedItem(R.id.nav_home);
		mBottomNavigationView.setSelectedItemId(R.id.nav_home);

		mNavigationView.setNavigationItemSelectedListener(this);
		mBottomNavigationView.setOnNavigationItemSelectedListener(this);

		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
					Log.d(TAG, "onClick: closing drawer");
					mDrawerLayout.closeDrawer(GravityCompat.START);
				} else if (mNavController.getCurrentDestination().getId() == R.id.fileFragment) {
					mNavController.navigateUp();
				} else if (mNavController.getCurrentDestination().getId() == R.id.notesSearchFragment) {
					mNavController.navigateUp();
				} else if (mNavController.getCurrentDestination().getId() == R.id.reminderDialogFragment) {
					mNavController.navigateUp();
				} else {
					Log.d(TAG, "onClick: opening drawer");
					mDrawerLayout.openDrawer(GravityCompat.START);
				}
			}
		});

		Log.d(TAG, "onNavigationItemSelected: animations for opening fragment to right of current one");
		leftToRightBuilder = new NavOptions.Builder();
		leftToRightBuilder.setEnterAnim(R.anim.slide_in_right);
		leftToRightBuilder.setExitAnim(R.anim.slide_out_left);
		leftToRightBuilder.setPopEnterAnim(R.anim.slide_in_left);
		leftToRightBuilder.setPopExitAnim(R.anim.slide_out_right);
		leftToRightBuilder.setLaunchSingleTop(true);

		Log.d(TAG, "onNavigationItemSelected: animations for opening fragment to left of current one");
		rightToLeftBuilder = new NavOptions.Builder();
		rightToLeftBuilder.setEnterAnim(R.anim.slide_in_left);
		rightToLeftBuilder.setExitAnim(R.anim.slide_out_right);
		rightToLeftBuilder.setPopEnterAnim(R.anim.slide_in_right);
		rightToLeftBuilder.setPopExitAnim(R.anim.slide_out_left);
		rightToLeftBuilder.setLaunchSingleTop(true);

		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("EXTRA_REMINDER_ITEM");
		if (bundle != null) {
			Log.d(TAG, "onCreate: starting reminder");
			ReminderItem item = bundle.getParcelable("BUNDLE_REMINDER_ITEM");

			NotificationHelper notificationHelper = new NotificationHelper(this);
			if (item != null) {
				notificationHelper.getManager().cancel(item.getNotifyId());
			}
			if (FirebaseAuth.getInstance().getCurrentUser() != null) {
				mNavController.navigate(R.id.nav_reminder, null, leftToRightBuilder.build());
			} else {
				FirebaseAuth.getInstance().signOut();

				GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
						.requestIdToken(getString(R.string.default_web_client_id))
						.requestEmail()
						.build();
				GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
				googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
					@Override
					public void onComplete(@NonNull Task<Void> task) {
						if (task.isSuccessful()) {
							mNavController.navigate(R.id.nav_logout);
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
						} else {
							StyleableToast.makeText(MainActivity.this, "Could not sign out. Please try again", Toast.LENGTH_SHORT, R.style.designedToast).show();
						}
					}
				});
			}
		}
	}

	@Override
	public boolean onSupportNavigateUp() {
		Log.d(TAG, "onSupportNavigateUp: starts");
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}

	@Override
	public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
		Log.d(TAG, "onDestinationChanged: starts");
		fab.setOnClickListener(null);
		switch (destination.getId()) {
			case R.id.nav_home:
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				mBottomAppBar.setVisibility(View.VISIBLE);
				mBottomAppBar.performShow();
				mBottomAppBar.bringToFront();
				if (mBottomNavigationView.getMenu().size() != 5) {
					mBottomNavigationView.getMenu().add(Menu.NONE, R.id.nav_fab, 3, "");
				}
				fab.show();
				fab.setImageResource(R.drawable.plus_icon);
				fab.setVisibility(View.VISIBLE);
				break;
			case R.id.nav_attendance:
				fab.hide();
				mBottomAppBar.performShow();
				mBottomAppBar.setVisibility(View.VISIBLE);
				mBottomNavigationView.getMenu().removeItem(R.id.nav_fab);
				mBottomAppBar.bringToFront();
				break;
			case R.id.nav_starred:
				fab.show();
				fab.setVisibility(View.VISIBLE);
				fab.setImageResource(R.drawable.plus_icon);
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				mBottomAppBar.setVisibility(View.VISIBLE);
				if (mBottomNavigationView.getMenu().size() != 5) {
					mBottomNavigationView.getMenu().add(Menu.NONE, R.id.nav_fab, 3, "");
				}
				mBottomAppBar.performShow();
				mBottomAppBar.bringToFront();
				break;
			case R.id.nav_notes:
				fab.show();
				fab.setVisibility(View.VISIBLE);
				fab.setImageResource(R.drawable.folder_add_icon);
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
				mBottomAppBar.performShow();
				if (mBottomNavigationView.getMenu().size() != 5) {
					mBottomNavigationView.getMenu().add(Menu.NONE, R.id.nav_fab, 3, "");
				}
				mBottomAppBar.setVisibility(View.VISIBLE);
				mBottomAppBar.bringToFront();
				break;
			case R.id.nav_reminder:
				mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
				mBottomAppBar.setVisibility(View.GONE);
				fab.show();
				fab.setVisibility(View.VISIBLE);
				fab.setImageResource(R.drawable.reminder_add_icon);
				break;
			case R.id.nav_profile:
			case R.id.nav_about_us:
				mBottomAppBar.setVisibility(View.GONE);
				fab.setVisibility(View.GONE);
				fab.hide();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		Log.d(TAG, "onNavigationItemSelected: starts");
		int itemId = item.getItemId();

		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			Log.d(TAG, "onNavigationItemSelected: closing drawer");
			mDrawerLayout.closeDrawer(GravityCompat.START);
		}

		switch (itemId) {
			case R.id.nav_home:
				Log.d(TAG, "onNavigationItemSelected: home selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_home) {
					Log.d(TAG, "onNavigationItemSelected: opening home fragment");
					mNavController.navigate(R.id.nav_home, null, rightToLeftBuilder.build());
				}
				return true;

			case R.id.nav_attendance:
				Log.d(TAG, "onNavigationItemSelected: attendance selected");
				if (mNavController.getCurrentDestination().getId() == R.id.nav_home) {
					Log.d(TAG, "onNavigationItemSelected: opening attendance fragment");
					mNavController.navigate(R.id.nav_attendance, null, leftToRightBuilder.build());
				} else if (mNavController.getCurrentDestination().getId() != R.id.nav_attendance) {
					Log.d(TAG, "onNavigationItemSelected: opening attendance fragment");
					mNavController.navigate(R.id.nav_attendance, null, rightToLeftBuilder.build());
				}
				return true;

			case R.id.nav_starred:
				Log.d(TAG, "onNavigationItemSelected: starred selected");
				if (mNavController.getCurrentDestination().getId() == R.id.nav_notes) {
					Log.d(TAG, "onNavigationItemSelected: opening starred fragment");
					mNavController.navigate(R.id.nav_starred, null, rightToLeftBuilder.build());
				} else if (mNavController.getCurrentDestination().getId() != R.id.nav_starred) {
					Log.d(TAG, "onNavigationItemSelected: opening starred fragment");
					mNavController.navigate(R.id.nav_starred, null, leftToRightBuilder.build());
				}
				return true;

			case R.id.nav_notes:
				Log.d(TAG, "onNavigationItemSelected: notes selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_notes) {
					Log.d(TAG, "onNavigationItemSelected: opening notes fragment");
					mNavController.navigate(R.id.nav_notes, null, leftToRightBuilder.build());
				}
				return true;

			case R.id.nav_fab:
				Log.d(TAG, "onNavigationItemSelected: fab selected");
				return true;

			case R.id.nav_reminder:
				Log.d(TAG, "onNavigationItemSelected: reminder selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_reminder) {
					Log.d(TAG, "onNavigationItemSelected: opening reminder fragment");
					mNavController.navigate(R.id.nav_reminder, null, leftToRightBuilder.build());
				}
				return true;

			case R.id.nav_profile:
				Log.d(TAG, "onNavigationItemSelected: profile selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_profile) {
					Log.d(TAG, "onNavigationItemSelected: opening profile fragment");
					mNavController.navigate(R.id.nav_profile, null, leftToRightBuilder.build());
				}
				return true;

			case R.id.nav_logout:
				Log.d(TAG, "onNavigationItemSelected: logging out");


				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				View view=getLayoutInflater().inflate(R.layout.logout_dialog_box,null);

				Button logoutYesButton=view.findViewById(R.id.logout_yes);
				Button logoutNoButton=view.findViewById(R.id.logout_no);

				builder.setView(view);

				final AlertDialog alertDialog= builder.create();
				alertDialog.setCanceledOnTouchOutside(true);

				logoutYesButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {

						FirebaseAuth.getInstance().signOut();

						GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
								.requestIdToken(getString(R.string.default_web_client_id))
								.requestEmail()
								.build();
						GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
						googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
							@Override
							public void onComplete(@NonNull Task<Void> task) {
								if (task.isSuccessful()) {
									mNavController.navigate(R.id.nav_logout);
									finishAffinity();
									overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
								} else {
									StyleableToast.makeText(MainActivity.this, "Could not sign out. Please try again", Toast.LENGTH_SHORT, R.style.designedToast).show();
								}
							}
						});

						alertDialog.dismiss();
					}
				});

				logoutNoButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						alertDialog.dismiss();
					}
				});

				alertDialog.show();

				return true;

			case R.id.nav_feedback:

				Connection.feedback(this);
				return true;

			case R.id.nav_report_bug:
				Connection.reportBug(this);
				return true;

			case R.id.nav_about_us:
				Log.d(TAG, "onNavigationItemSelected: about us selected");
				if (mNavController.getCurrentDestination().getId() != R.id.nav_about_us) {
					Log.d(TAG, "onNavigationItemSelected: opening about us fragment");
					mNavController.navigate(R.id.nav_about_us, null, leftToRightBuilder.build());
				}
				return true;

			default:
				StyleableToast.makeText(this, "This feature is not yet available", Toast.LENGTH_SHORT, R.style.designedToast).show();
				return false;
		}
	}
}

package com.studypartner.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muddzdev.styleabletoast.StyleableToast;
import com.studypartner.R;
import com.studypartner.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
	private static final String TAG = "LoginActivity";
	
	private final int RC_SIGN_IN = 123;
	
	private final String SESSIONS = "SESSIONS";
	
	private final String REMEMBER_ME_ENABLED = "rememberMeEnabled";
	private final String REMEMBER_ME_EMAIL = "rememberMeEmail";
	private final String REMEMBER_ME_PASSWORD = "rememberMePassword";
	
	private SharedPreferences.Editor mEditor;
	
	private TextInputLayout emailTextInput, passwordTextInput;
	private TextInputEditText emailEditText, passwordEditText;
	private CheckBox rememberMe;
	
	private GoogleSignInClient mGoogleSignInClient;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			findViewById(R.id.loginScreenProgressBar).setVisibility(View.VISIBLE);
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			try {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
				firebaseAuthWithGoogle(account.getIdToken());
			} catch (ApiException e) {
				e.printStackTrace();
				findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
				// In case of Login failed because of any reason
				StyleableToast.makeText(this,"Login unsuccessful, Please try again",
						Toast.LENGTH_LONG,R.style.designedToast)
						.show();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onCreate: Checking internet connection");
		checkConnection(LoginActivity.this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate: starts");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		
		// Configure Google Sign In
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();
		
		mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
		
		Log.d(TAG, "onCreate: Checking internet connection");
		checkConnection(this);
		if (FirebaseAuth.getInstance().getCurrentUser() != null) {
			Log.d(TAG, "onCreate: User already logged in");
			
			findViewById(R.id.loginScreenProgressBar).setVisibility(View.VISIBLE);
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finishAffinity();
			overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		}
		
		SharedPreferences sharedPreferences = getSharedPreferences(SESSIONS, MODE_PRIVATE);
		
		mEditor = sharedPreferences.edit();
		
		//Setting hooks
		
		emailTextInput = findViewById(R.id.loginScreenEmailTextInput);
		emailEditText = findViewById(R.id.loginScreenEmailEditText);
		
		emailEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				emailTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		passwordTextInput = findViewById(R.id.loginScreenPasswordTextInput);
		passwordEditText = findViewById(R.id.loginScreenPasswordEditText);
		
		passwordEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				passwordTextInput.setError(null);
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});
		
		Button loginButton = findViewById(R.id.loginScreenLoginButton);
		Button googleLoginButton = findViewById(R.id.loginScreenGoogleLoginButton);
		Button forgotPasswordButton = findViewById(R.id.loginScreenForgotPasswordButton);
		
		rememberMe = findViewById(R.id.loginScreenRememberMeCheckBox);
		
		if (sharedPreferences.getBoolean(REMEMBER_ME_ENABLED, false)) {
			emailEditText.setText(sharedPreferences.getString(REMEMBER_ME_EMAIL, ""));
			passwordEditText.setText(sharedPreferences.getString(REMEMBER_ME_PASSWORD, ""));
			rememberMe.setChecked(true);
		}
		
		findViewById(R.id.loginScreenCreateAccountButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: creating shared animation on create account button clicked");
				Pair[] pairs = new Pair[6];
				pairs[0] = new Pair<>(findViewById(R.id.loginScreenWelcomeBack), "welcome_transition");
				pairs[1] = new Pair<>(findViewById(R.id.loginScreenCreateAccountButton), "create_account_transition");
				pairs[2] = new Pair<>(findViewById(R.id.loginScreenLoginButton), "login_transition");
				pairs[3] = new Pair<>(findViewById(R.id.loginScreenEmailTextInput), "email_transition");
				pairs[4] = new Pair<>(findViewById(R.id.loginScreenPasswordTextInput), "password_transition");
				pairs[5] = new Pair<>(findViewById(R.id.loginScreenBackgroundRectangle), "bgrect_transition");
				
				ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
				
				startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class), activityOptions.toBundle());
				overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: login button clicked");
				
				if (validateFields()) {
					login();
				}
			}
		});
		
		googleLoginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: google login button clicked");
				
				googleLogin();
			}
		});
		
		forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "onClick: forget password clicked");
				if (validateEmail(emailEditText.getText().toString()) != null) {
					Log.d(TAG, "onClick: invalid email address entered");
					StyleableToast.makeText(LoginActivity.this, "Put a valid email associated with an account to change the password", Toast.LENGTH_SHORT, R.style.designedToast).show();
				} else {
					FirebaseAuth.getInstance().sendPasswordResetEmail(emailEditText.getText().toString())
							.addOnCompleteListener(new OnCompleteListener<Void>() {
								@Override
								public void onComplete(@NonNull Task<Void> task) {
									if (task.isSuccessful()) {
										Log.d(TAG, "onComplete: email sent to user for password reset");
										StyleableToast.makeText(LoginActivity.this, "A password reset email has been sent to your email address", Toast.LENGTH_SHORT, R.style.designedToast).show();
									} else {
										Log.d(TAG, "onComplete: email for password reset cannot be sent");
										StyleableToast.makeText(LoginActivity.this, "Couldn't send a password reset email", Toast.LENGTH_SHORT, R.style.designedToast).show();
										task.getException().printStackTrace();
									}
								}
							});
				}
			}
		});
		
		Log.d(TAG, "onCreate: ends");
	}
	
	private void checkConnection(Activity activity) {
		Log.d(TAG, "isConnected: internet check");
		
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
		
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
			Log.d(TAG, "onCreate: Internet not connected");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Please connect to the internet to proceed further")
					.setCancelable(false)
					.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: opening settings for internet");
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "onClick: closing app");
							finishAffinity();
						}
					});
			builder.show();
		} else {
			Log.d(TAG, "isConnected: internet connected");
		}
	}
	
	private void googleLogin() {
		Log.d(TAG, "googleLogin: starts");
		
		Intent signInIntent = mGoogleSignInClient.getSignInIntent();
		startActivityForResult(signInIntent, RC_SIGN_IN);
		
		Log.d(TAG, "googleLogin: ends");
	}
	
	private void firebaseAuthWithGoogle(String idToken) {
		Log.d(TAG, "firebaseAuthWithGoogle: starts");
		
		AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
		FirebaseAuth.getInstance().signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "signInWithCredential: success");
							storeUserDetails();
						} else {
							findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
							StyleableToast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
							task.getException().printStackTrace();
						}
					}
				});
	}
	
	private void storeUserDetails() {
		Log.d(TAG, "storeUserDetails: starts");
		
		final GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
		
		if (signInAccount != null) {
			
			final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
			
			final User user = new User(signInAccount.getDisplayName(), signInAccount.getEmail(), false);
			
			UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
			profileUpdates.setDisplayName(signInAccount.getDisplayName());
			
			FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates.build())
					.addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {
							if (task.isSuccessful()) {
								Log.d(TAG, "onComplete: " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
							} else {
								Log.d(TAG, "onComplete: Could not update display name");
							}
						}
					});
			
			FirebaseDatabase.getInstance().getReference().child("usernames").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot snapshot) {
					if (snapshot.exists()) {
						Log.d(TAG, "onDataChange: account already exists");
						if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
							FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
						}
						FirebaseDatabase.getInstance().getReference().child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot snapshot) {
								User tempUser = snapshot.getValue(User.class);
								if (tempUser != null) {
									tempUser.setEmail(signInAccount.getEmail());
									tempUser.setFullName(signInAccount.getDisplayName());
									tempUser.setEmailVerified(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified());
									FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(tempUser);
								}
							}
							
							@Override
							public void onCancelled(@NonNull DatabaseError error) {
							
							}
						});
						startActivity(new Intent(LoginActivity.this, MainActivity.class));
						finishAffinity();
						overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
					} else {
						Log.d(TAG, "onDataChange: account does not exist");
						FirebaseAuth.getInstance().getCurrentUser().updateEmail(signInAccount.getEmail())
								.addOnCompleteListener(new OnCompleteListener<Void>() {
									@Override
									public void onComplete(@NonNull Task<Void> task) {
										if (task.isSuccessful()) {
											Log.d(TAG, "onComplete: email " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
											
											if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
												FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
											}
											
											//Make users database
											FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user);
											
											//Make usernames database
											FirebaseDatabase.getInstance().getReference().child("usernames").child(uid).setValue(user.getUsername());
											
											startActivity(new Intent(LoginActivity.this, MainActivity.class));
											finishAffinity();
											overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
										} else if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
											Log.d(TAG, "onComplete: email already in use");
											
											findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
											
											StyleableToast.makeText(LoginActivity.this, "Email already in use by other account. Cannot sign in", Toast.LENGTH_LONG, R.style.designedToast).show();
											
											GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
													.requestIdToken(getString(R.string.default_web_client_id))
													.requestEmail()
													.build();
											
											GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);
											googleSignInClient.signOut();
											
											FirebaseAuth.getInstance().getCurrentUser().delete()
													.addOnSuccessListener(new OnSuccessListener<Void>() {
														@Override
														public void onSuccess(Void aVoid) {
															Log.d(TAG, "onSuccess: account deleted successfully");
														}
													})
													.addOnFailureListener(new OnFailureListener() {
														@Override
														public void onFailure(@NonNull Exception e) {
															Log.d(TAG, "onFailure: account could not be deleted");
														}
													});
										} else {
											findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
											task.getException().printStackTrace();
										}
									}
								});
						
					}
				}
				
				@Override
				public void onCancelled(@NonNull DatabaseError error) {
				
				}
			});
			
		}
		Log.d(TAG, "storeUserDetails: ends");
	}
	
	private void login() {
		Log.d(TAG, "login: starts");
		findViewById(R.id.loginScreenProgressBar).setVisibility(View.VISIBLE);
		
		FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
				.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "onComplete: login successful");
							if (rememberMe.isChecked()) {
								mEditor.putBoolean(REMEMBER_ME_ENABLED, true);
								mEditor.putString(REMEMBER_ME_EMAIL, emailEditText.getText().toString());
								mEditor.putString(REMEMBER_ME_PASSWORD, passwordEditText.getText().toString());
								mEditor.apply();
							} else {
								mEditor.putBoolean(REMEMBER_ME_ENABLED, false);
								mEditor.apply();
							}
							updateDetails();
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finishAffinity();
							overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
						} else {
							findViewById(R.id.loginScreenProgressBar).setVisibility(View.INVISIBLE);
							StyleableToast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT, R.style.designedToast).show();
							task.getException().printStackTrace();
						}
					}
				});
	}
	
	private void updateDetails() {
		Log.d(TAG, "updateDetails: starts");
		
		Log.d(TAG, "updateDetails: updating email");
		FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("email").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
		
		if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
			FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
		}
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause: starts");
		super.onPause();
		
		if (emailTextInput.isErrorEnabled()) {
			emailTextInput.setError(null);
		}
		
		if (passwordTextInput.isErrorEnabled()) {
			passwordTextInput.setError(null);
		}
	}
	
	private boolean validateFields() {
		Log.d(TAG, "validateFields: starts");
		String emailValidation = validateEmail(emailEditText.getText().toString());
		String passwordValidation = validatePassword(passwordEditText.getText().toString());
		
		if (emailValidation == null && passwordValidation == null) {
			Log.d(TAG, "validateFields() returned: " + true);
			return true;
		}
		
		if (emailValidation != null) {
			emailTextInput.setError(emailValidation);
		}
		
		if (passwordValidation != null) {
			passwordTextInput.setError(passwordValidation);
		}
		
		Log.d(TAG, "validateFields() returned: " + false);
		return false;
	}
	
	private String validateEmail(String email) {
		if (email.trim().length() == 0) {
			return "Email cannot be empty";
		}
		return null;
	}
	
	private String validatePassword(String password) {
		if (password.trim().length() == 0) {
			return "Password cannot be empty";
		}
		return null;
	}
}

package com.studypartner.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.studypartner.R;
import com.studypartner.adapters.MediaAdapter;
import com.studypartner.fragments.MediaFragment;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MediaActivity extends AppCompatActivity {
	private static final String TAG = "Media ";
	
	private boolean inStarred = false;
	private ViewPager2 viewPager;
	private MediaAdapter mediaAdapter;
	
	private InterstitialAd mInterstitialAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "Media Activity");
		setContentView(R.layout.activity_media);
		
		getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "NOTES_SEARCH", MODE_PRIVATE).edit().putBoolean("NotesSearchExists", false).apply();
		
		viewPager = findViewById(R.id.viewPager2);
		Intent intent = getIntent();
		String state = intent.getStringExtra("State");
		
		if (state.equals("Files")) {
			
			String path = intent.getStringExtra("Media");
			inStarred = intent.getBooleanExtra("InStarred", false);
			File file = new File(path);
			File parentFile = file.getParentFile();
			mediaData(parentFile, file);
			
		} else if (state.equals("Home")) {
			
			ArrayList<FileItem> homeMedia = intent.getParcelableArrayListExtra("HomeMedia");
			int position = intent.getIntExtra("Position", 0);
			homeMediaDisplay(homeMedia, position);
			
		}
		
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId(getString(R.string.notes_interstitial_ad));
		mInterstitialAd.loadAd(new AdRequest.Builder().build());
	}
	
	private void homeMediaDisplay(ArrayList<FileItem> homeMedia, int value) {
		
		ArrayList<String> mediaFiles = new ArrayList<>();
		for (FileItem fileItem : homeMedia) {
			mediaFiles.add(fileItem.getPath());
		}
		mediaAdapter = new MediaAdapter(getSupportFragmentManager(), getLifecycle());
		for (String s : mediaFiles) {
			mediaAdapter.addFragment(MediaFragment.newInstance(s));
		}
		
		viewPager.setAdapter(mediaAdapter);
		viewPager.setCurrentItem(value, false);
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
			
			@Override
			public void onPageSelected(final int position) {
				super.onPageSelected(position);
				try {
					(mediaAdapter.createFragment(position - 1)).isHidden();
					if (position != 0 && position % 3 == 0 && mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
						mInterstitialAd.setAdListener(new AdListener() {
							@Override
							public void onAdClosed() {
								(mediaAdapter.createFragment(position)).isVisible();
								mInterstitialAd.loadAd(new AdRequest.Builder().build());
							}
						});
					} else {
						(mediaAdapter.createFragment(position)).isVisible();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				super.onPageScrollStateChanged(state);
			}
		});
		
	}
	
	public void mediaData(File parent, File child) {
		ArrayList<String> mediafiles = new ArrayList<>();
		
		int value = 0;
		if (!inStarred) {
			File[] files = parent.listFiles();
			assert files != null;
			for (File f : files) {
				FileItem newFile = new FileItem(f.getPath());
				
				if (newFile.getType() == FileType.FILE_TYPE_VIDEO || newFile.getType() == FileType.FILE_TYPE_AUDIO || newFile.getType() == FileType.FILE_TYPE_IMAGE) {
					mediafiles.add(newFile.getPath());
					if (f.getName().equals(child.getName()))
						value = mediafiles.size() - 1;
				}
			}
		} else {
			ArrayList<FileItem> starredFiles;
			
			SharedPreferences starredPreference = getSharedPreferences(FirebaseAuth.getInstance().getCurrentUser().getUid() + "STARRED", MODE_PRIVATE);
			
			if (starredPreference.getBoolean("STARRED_ITEMS_EXISTS", false)) {
				Gson gson = new Gson();
				String json = starredPreference.getString("STARRED_ITEMS", "");
				Type type = new TypeToken<List<FileItem>>() {
				}.getType();
				starredFiles = gson.fromJson(json, type);
			} else {
				starredFiles = new ArrayList<>();
			}
			
			assert starredFiles != null;
			for (FileItem newFile : starredFiles) {
				if (newFile.getType() == FileType.FILE_TYPE_VIDEO || newFile.getType() == FileType.FILE_TYPE_AUDIO || newFile.getType() == FileType.FILE_TYPE_IMAGE) {
					mediafiles.add(newFile.getPath());
					if (newFile.getName().equals(child.getName()))
						value = mediafiles.size() - 1;
				}
			}
		}
		
		mediaAdapter = new MediaAdapter(getSupportFragmentManager(), getLifecycle());
		for (String s : mediafiles) {
			mediaAdapter.addFragment(MediaFragment.newInstance(s));
		}
		
		viewPager.setAdapter(mediaAdapter);
		viewPager.setCurrentItem(value, false);
		viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
			
			@Override
			public void onPageSelected(final int position) {
				super.onPageSelected(position);
				try {
					(mediaAdapter.createFragment(position - 1)).isHidden();
					if (position != 0 && position % 3 == 0 && mInterstitialAd.isLoaded()) {
						mInterstitialAd.show();
						mInterstitialAd.setAdListener(new AdListener() {
							@Override
							public void onAdClosed() {
								(mediaAdapter.createFragment(position)).isVisible();
								mInterstitialAd.loadAd(new AdRequest.Builder().build());
							}
						});
					} else {
						(mediaAdapter.createFragment(position)).isVisible();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				super.onPageScrollStateChanged(state);
			}
		});
		
	}
}

