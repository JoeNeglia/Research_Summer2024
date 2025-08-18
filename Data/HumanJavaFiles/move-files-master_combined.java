package net.za.acraig.movefiles;

import java.io.File;

public class CountVisitor extends Visitor 
	{
	private int _count = 0;
	
	protected void processFile(File file)
		{
		_count++;
		}
	
	public final int getCount()
		{
		return _count;
		}
	}


package net.za.acraig.movefiles;

import java.io.File;

public class DeleteVisitor extends Visitor
	{
	public DeleteVisitor()
		{
		}
	
	@Override
	protected void processEmptyDir(File d)
		{
		d.delete();
		}

	@Override
	protected void processFile(File f)
		{
		f.delete();
		}

	}


package net.za.acraig.movefiles;

import java.io.File;

public abstract class Visitor 
	{
	public void visit(File root)
		{
		if (root != null && root.exists() && root.isDirectory())
			{
			File [] listing = root.listFiles();
			if (listing != null)
				{
				for (File f : listing)
					{
					if (f.isDirectory())
						{
						visit(f);
						processEmptyDir(f);
						}
					else if (f.isFile() && 0 != f.getName().compareTo("empty"))
						processFile(f);
					}
				}
			}
		}

	protected void processEmptyDir(File d)
		{
		}

	protected abstract void processFile(File f);
	}


package net.za.acraig.movefiles;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class QuickPrefsActivity extends PreferenceActivity
	{

	@Override
	protected void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
		}

	}


package net.za.acraig.movefiles;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MoveFilesActivity extends Activity 
	{
	private File _src;
	private File _dest;
	private int _srcc = 0;
	private boolean _moveMode = true;
	private boolean _clearMode = true;
	private boolean _share = false;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
		{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	
		View ruler = new View(this); 
		ruler.setBackgroundColor(0xFFFFFFFF);
		LinearLayout parent = (LinearLayout) findViewById(R.id.linearLayout1);
		parent.addView(ruler, 1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1));
		
		try
			{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			setTitle("Move Files " + pInfo.versionName);
			}
		catch (Exception e)
			{
			setTitle("Move Files");
			}

		initFromPreferences();
		updateSourceControls();
		updateDestinationControls();
		enableControls(getCanCopy());
		}


	private void initFromPreferences()
		{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		_src = directoryFromPreference("srcPref", "0");
		_dest = directoryFromPreference("destPref", "2");
		_moveMode = prefs.getBoolean("deletePref", true);
		_share = prefs.getBoolean("sharePref", false);
		}


	private File directoryFromPreference(String key, String defValue)
		{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int value = Integer.parseInt(prefs.getString(key, defValue));
		
		switch (value)
			{
			case 0:
				return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS);
			case 1:
				return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			case 2:
				{
//				File extStorage = Environment.getExternalStorageDirectory();
//				File usbHost = new File(extStorage, "usbStorage");
//				if (usbHost != null && usbHost.exists() && usbHost.isDirectory())
//					return new File(usbHost, "UsbDriveA");
//				break;
				File usbHost = new File("/storage/UsbDriveA");
				if (usbHost != null && usbHost.exists() && usbHost.isDirectory() && usbHost.canWrite())
					return usbHost;
				}
			}
		return null;
		}


	private void updateSourceControls()
		{
		CountVisitor srcv = new CountVisitor();
		srcv.visit(_src);
		_srcc = srcv.getCount();

		TextView srcDir = (TextView) findViewById(R.id.src);
		srcDir.setText("Src = " + getDirectoryDescription(_src));
		TextView srcText = (TextView) findViewById(R.id.srccount);
		srcText.setText("Files = " + String.valueOf(_srcc));
		}


	private void updateDestinationControls()
		{
		CountVisitor destv = new CountVisitor();
		destv.visit(_dest);
		int destc = destv.getCount();
		
		TextView destDev = (TextView) findViewById(R.id.dest);
		destDev.setText("Dest = " + getDirectoryDescription(_dest));
		TextView destText = (TextView) findViewById(R.id.destcount);
		destText.setText("Files = " + String.valueOf(destc));
		}


	private String getDirectoryDescription(File dir)
		{
		if (dir != null)
			{
			if (dir.exists() && dir.isDirectory())
				return dir.getAbsolutePath();
			else
				return "Directory does not exist, or is a file.";
			}
		else
			return "Directory not found (null)";
		}


	private void enableControls(boolean enabled)
		{
		Button copyButton = (Button) findViewById(R.id.copyButton);
		copyButton.setEnabled(enabled);

		CheckBox clearDestinationBox = (CheckBox) findViewById(R.id.deleteBefore);
		clearDestinationBox.setChecked(_clearMode);
		clearDestinationBox.setEnabled(enabled);

		CheckBox keepCopyBox = (CheckBox) findViewById(R.id.deleteAfter);
		keepCopyBox.setChecked(!_moveMode);
		keepCopyBox.setEnabled(enabled);
		}


	private boolean getCanCopy()
		{
		return (_src != null && _dest != null && !_src.equals(_dest) && _srcc > 0 && _src.exists() && _src.isDirectory() && _dest.exists() && _dest.isDirectory());
		}


	public void onCopyClick(View view)
		{
		CheckBox deleteBeforeBox = (CheckBox) findViewById(R.id.deleteBefore);
		_clearMode = deleteBeforeBox.isChecked();

		CheckBox keepCopyBox = (CheckBox) findViewById(R.id.deleteAfter);
		_moveMode = !keepCopyBox.isChecked();
		
		enableControls(false);
		
		CopyFilesTask task = new CopyFilesTask();
		task.execute();
		}

	public void onSwapClick(View view)
		{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String a = prefs.getString("srcPref", "0");
		String b = prefs.getString("destPref", "2");

		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("srcPref", b);
		editor.putString("destPref", a);
		editor.commit();

		initFromPreferences();
		updateSourceControls();
		updateDestinationControls();
		enableControls(getCanCopy());
		}
	
	/// Asynctask does background copying, and done as an inner class so as to easily update controls on completion. 
	private class CopyFilesTask extends AsyncTask<String, Void, String> 
		{
		@Override
		protected String doInBackground(String... urls) 
			{
			CopyVisitor copyv = new CopyVisitor(_dest);
			copyv.setMoveMode(_moveMode);
			copyv.visit(_src);
			return copyv.getResult();
			}

		@Override
		protected void onPreExecute()
			{
			if (_clearMode)
				{
				DeleteVisitor deletev = new DeleteVisitor();
				deletev.visit(_dest);
				}
			super.onPreExecute();
			}

		@Override
		protected void onPostExecute(String result) 
			{
			if (_moveMode)
				{
				// cleanup empty directories
				DeleteVisitor deletev = new DeleteVisitor();
				deletev.visit(_src);
				}

			updateSourceControls();
			updateDestinationControls();
			enableControls(getCanCopy());

			Toast toast = Toast.makeText(MoveFilesActivity.this, "Copy completed", Toast.LENGTH_SHORT);
			toast.show();

			if (_share)
				{
				Intent intent = new Intent(android.content.Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(android.content.Intent.EXTRA_TEXT, result);
				startActivity(intent);
				}
			}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
		{
		MenuItem prefItem = menu.add(Menu.NONE, 0, 0, "Preferences");
		prefItem.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
		}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
		{
		if (0 == item.getItemId())
			startActivityForResult(new Intent(this, QuickPrefsActivity.class), 1);

		return super.onOptionsItemSelected(item);
		}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
		if (1 == requestCode)
			{
			initFromPreferences();
			updateSourceControls();
			updateDestinationControls();
			enableControls(getCanCopy());
			}
		super.onActivityResult(requestCode, resultCode, data);
		}

}

package net.za.acraig.movefiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyVisitor extends Visitor
	{
	private File _destDir;
	private boolean _move = false; // whether to move files or copy them
	private String _result = "Podcasts listened to this week:\n"; // concatenation of copied filenames
	
	public CopyVisitor(File dest)
		{
		_destDir = dest;
		}
	
	public void setMoveMode(boolean move)
		{
		_move = move;
		}
	
	protected void processFile(final File file)
		{
//		Thread copyThread = new Thread()
//			{
//			public void run()
//				{
//				copyFile(file);
//				if (_move)
//					file.delete();
//				}
//			};
//		copyThread.start();
		
		boolean copyOk = copyFile(file);
		
		if (copyOk)
			_result += file.getName() + "\n";
		if (copyOk && _move)
			file.delete();
		}
	
	private boolean copyFile(File file)
		{
		try
			{
			String destname = _destDir.getAbsolutePath() + File.separator + file.getName();
			File dest = new File(destname);
			if (dest.exists())
				return false;
			
			InputStream in = new FileInputStream(file);
			OutputStream out = new FileOutputStream(dest);
			
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
				{
				out.write(buf, 0, len);
				}
			in.close();
			out.close();
			
			boolean succesfulCopy = (file.length() == dest.length());
			return succesfulCopy;
			}
		catch (FileNotFoundException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			} 
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		return false;
		}
	
	public String getResult()
		{
		return _result;
		}
	}


