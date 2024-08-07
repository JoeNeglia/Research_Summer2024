/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of AdBlock.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.adBlock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Main Activity to control ad blocking Proxy.
 * 
 * @author Felix Bechstein
 */
public class AdBlock extends Activity implements OnClickListener,
		OnItemClickListener {

	/** Tag for output. */
	private static final String TAG = "AdBlock";

	/** Prefs: name for last version run. */
	private static final String PREFS_LAST_RUN = "lastrun";
	/** Preferences: import url. */
	private static final String PREFS_IMPORT_URL = "importurl";

	/** Filename for export of filter. */
	// private static final String FILENAME_EXPORT = "/sdcard/filter.txt";
	/** ItemDialog: edit. */
	private static final short ITEM_DIALOG_EDIT = 0;
	/** ItemDialog: delete. */
	private static final short ITEM_DIALOG_DELETE = 1;

	/** Dialog: about. */
	private static final int DIALOG_ABOUT = 0;
	/** Dialog: import. */
	private static final int DIALOG_IMPORT = 1;
	/** Dialog: update. */
	private static final int DIALOG_UPDATE = 2;

	/** Prefs. */
	private SharedPreferences preferences;
	/** Prefs. import URL. */
	private String importUrl = null;

	/** The filter. */
	private ArrayList<String> filter = new ArrayList<String>();
	/** The ArrayAdapter. */
	private ArrayAdapter<String> adapter = null;

	/** Editmode? */
	private int itemToEdit = -1;

	/**
	 * Import filter from URL on background.
	 * 
	 * @author Felix Bechstein
	 */
	class Importer extends AsyncTask<String, Boolean, Boolean> {
		/** Error message. */
		private String message = "";

		/**
		 * Do the work.
		 * 
		 * @param dummy
		 *            nothing here
		 * @return successful?
		 */
		@Override
		protected final Boolean doInBackground(final String... dummy) {
			try {
				HttpURLConnection c = (HttpURLConnection) (new URL(
						AdBlock.this.importUrl)).openConnection();
				int resp = c.getResponseCode();
				if (resp != 200) {
					return false;
				}
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(c.getInputStream()));
				AdBlock.this.filter.clear();
				while (true) {
					String s = reader.readLine();
					if (s == null) {
						break;
					}
					s = s.trim();
					if (s.length() > 0) {
						AdBlock.this.filter.add(s);
					}
				}
				reader.close();
				return true;
			} catch (MalformedURLException e) {
				Log.e(AdBlock.TAG, null, e);
				this.message = e.toString();
				return false;
			} catch (IOException e) {
				this.message = e.toString();
				Log.e(AdBlock.TAG, null, e);
				return false;
			}
		}

		/**
		 * Merge imported filter to the real one.
		 * 
		 * @param result
		 *            nothing here
		 */
		@Override
		protected final void onPostExecute(final Boolean result) {
			if (result.booleanValue()) {
				Toast.makeText(AdBlock.this, "imported", Toast.LENGTH_LONG)
						.show();
				AdBlock.this.adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(AdBlock.this, "failed: " + this.message,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);

		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
		// display changelog?
		String v0 = this.preferences.getString(PREFS_LAST_RUN, "");
		String v1 = this.getString(R.string.app_version);
		if (!v0.equals(v1)) {
			SharedPreferences.Editor editor = this.preferences.edit();
			editor.putString(PREFS_LAST_RUN, v1);
			editor.commit();
			this.showDialog(DIALOG_UPDATE);
		}

		((EditText) this.findViewById(R.id.port)).setText(this.preferences
				.getString(Proxy.PREFS_PORT, "8080"));
		String f = this.preferences.getString(Proxy.PREFS_FILTER, this
				.getString(R.string.default_filter));
		for (String s : f.split("\n")) {
			if (s.length() > 0) {
				this.filter.add(s);
			}
		}
		this.importUrl = this.preferences.getString(PREFS_IMPORT_URL, "");

		((Button) this.findViewById(R.id.start_service))
				.setOnClickListener(this);
		((Button) this.findViewById(R.id.stop_service))
				.setOnClickListener(this);
		((Button) this.findViewById(R.id.filter_add_)).setOnClickListener(this);
		ListView lv = (ListView) this.findViewById(R.id.filter);
		this.adapter = new ArrayAdapter<String>(this,
				R.layout.simple_list_item_1, this.filter);
		lv.setAdapter(this.adapter);
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(this);

		this.startService(new Intent(this, Proxy.class));
	}

	/** Save Preferences. */
	private void savePreferences() {
		SharedPreferences.Editor editor = this.preferences.edit();
		editor.putString(Proxy.PREFS_PORT, ((EditText) this
				.findViewById(R.id.port)).getText().toString());
		StringBuilder sb = new StringBuilder();
		for (String s : this.filter) {
			sb.append(s + "\n");
		}
		editor.putString(Proxy.PREFS_FILTER, sb.toString());
		editor.putString(PREFS_IMPORT_URL, this.importUrl);
		editor.commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onPause() {
		super.onPause();
		this.savePreferences();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.start_service:
			this.savePreferences();
			this.startService(new Intent(this, Proxy.class));
			break;
		case R.id.stop_service:
			this.stopService(new Intent(this, Proxy.class));
		case R.id.filter_add_:
			EditText et = (EditText) this.findViewById(R.id.filter_add);
			String f = et.getText().toString();
			if (f.length() > 0) {
				if (this.itemToEdit >= 0) {
					this.filter.remove(this.itemToEdit);
					this.itemToEdit = -1;
				}
				this.filter.add(f);
				et.setText("");
				this.adapter.notifyDataSetChanged();
			}
			break;
		case R.id.cancel:
			this.dismissDialog(DIALOG_IMPORT);
			break;
		case R.id.ok:
			this.dismissDialog(DIALOG_IMPORT);
			this.importUrl = ((EditText) v.getRootView().findViewById(
					R.id.import_url)).getText().toString();
			new Importer().execute((String[]) null);
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_about: // start about dialog
			this.showDialog(DIALOG_ABOUT);
			return true;
		case R.id.item_import:
			this.showDialog(DIALOG_IMPORT);
			return true;
		case R.id.item_donate:
			try {
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(this.getString(R.string.donate_url))));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "no browser", e);
			}
			return true;
		case R.id.item_more:
			try {
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pub:\"Felix Bechstein\"")));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "no market", e);
			}
			return true;
			// case R.id.item_export:
			// try {
			// // OutputStream os = this.openFileOutput(FILENAME_EXPORT,
			// // MODE_WORLD_READABLE);
			// File file = new File(FILENAME_EXPORT);
			// if (!file.createNewFile()) {
			// file.delete();
			// file.createNewFile();
			// }
			// OutputStream os = new FileOutputStream(file);
			// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
			// os));
			// for (String s : this.filter) {
			// bw.append(s + "\n");
			// }
			// }
			// bw.close();
			// os.close();
			// Toast.makeText(this, "exported to " + FILENAME_EXPORT,
			// Toast.LENGTH_LONG).show();
			// } catch (IOException e) {
			// Log.e(this.TAG, null, e);
			// Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			// }
			// return true;
		default:
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final Dialog onCreateDialog(final int id) {
		Dialog d;
		switch (id) {
		case DIALOG_ABOUT:
			d = new Dialog(this);
			d.setContentView(R.layout.about);
			d.setTitle(this.getString(R.string.about_) + " v"
					+ this.getString(R.string.app_version));
			return d;
		case DIALOG_IMPORT:
			d = new Dialog(this);
			d.setContentView(R.layout.import_url);
			d.setTitle(this.getString(R.string.import_url_));
			((Button) d.findViewById(R.id.ok)).setOnClickListener(this);
			((Button) d.findViewById(R.id.cancel)).setOnClickListener(this);
			return d;
		case DIALOG_UPDATE:
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.changelog_);
			final String[] changes = this.getResources().getStringArray(
					R.array.updates);
			final StringBuilder buf = new StringBuilder(changes[0]);
			for (int i = 1; i < changes.length; i++) {
				buf.append("\n\n");
				buf.append(changes[i]);
			}
			builder.setIcon(android.R.drawable.ic_menu_info_details);
			builder.setMessage(buf.toString());
			builder.setCancelable(true);
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int id) {
							dialog.cancel();
						}
					});
			return builder.create();
		default:
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onPrepareDialog(final int id, final Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case DIALOG_IMPORT:
			((EditText) dialog.findViewById(R.id.import_url))
					.setText(this.importUrl);
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onItemClick(final AdapterView<?> parent, final View v,
			final int position, final long id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(
				this.getResources().getStringArray(R.array.itemDialog),
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int item) {
						switch (item) {
						case ITEM_DIALOG_EDIT:
							AdBlock.this.itemToEdit = position;
							((EditText) AdBlock.this
									.findViewById(R.id.filter_add))
									.setText(AdBlock.this.adapter
											.getItem(position));
							break;
						case ITEM_DIALOG_DELETE:
							AdBlock.this.filter.remove(position);
							AdBlock.this.adapter.notifyDataSetChanged();
							break;
						default:
							break;
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}


/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of AdBlock.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.adBlock;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This ad blocking Proxy Service will work as an ordinary HTTP proxy. Set APN's
 * proxy preferences to proxy's connection parameters.
 * 
 * @author Felix Bechstein
 */
public class Proxy extends Service implements Runnable {
	/** Tag for output. */
	private static final String TAG = "AdBlock.Proxy";

	/** Method Signature: startForeground. */
	@SuppressWarnings("unchecked")
	private static final Class[] START_FOREGROUND_SIGNATURE = new Class[] {
			int.class, Notification.class };
	/** Method Signature: stopForeground. */
	@SuppressWarnings("unchecked")
	private static final Class[] STOP_FOREGROUND_SIGNATURE = // .
	new Class[] { boolean.class };

	/** {@link NotificationManager}. */
	private NotificationManager mNM;
	/** Method: startForeground. */
	private Method mStartForeground;
	/** Method: stopForeground. */
	private Method mStopForeground;
	/** Method's arguments: startForeground. */
	private Object[] mStartForegroundArgs = new Object[2];
	/** Method's arguments: stopForeground. */
	private Object[] mStopForegroundArgs = new Object[1];

	/** Preferences: Port. */
	static final String PREFS_PORT = "port";
	/** Preferences: Filter. */
	static final String PREFS_FILTER = "filter";

	/** HTTP Response: blocked. */
	private static final String HTTP_BLOCK = "HTTP/1.1 500 blocked by AdBlock";
	/** HTTP Response: error. */
	private static final String HTTP_ERROR = "HTTP/1.1 500 error by AdBlock";
	/** HTTP Response: connected. */
	private static final String HTTP_CONNECTED = "HTTP/1.1 200 connected";
	/** HTTP Response: flush. */
	private static final String HTTP_RESPONSE = "\n\n";

	/** Default Port for HTTP. */
	private static final int PORT_HTTP = 80;
	/** Default Port for HTTPS. */
	private static final int PORT_HTTPS = 443;

	/** Proxy. */
	private Thread proxy = null;
	/** Proxy's port. */
	private int port = -1;
	/** Proxy's filter. */
	ArrayList<String> filter = new ArrayList<String>();
	/** Stop proxy? */
	private boolean stop = false;

	/**
	 * Connection handles a single HTTP Connection. Run this as a Thread.
	 * 
	 * @author Felix Bechstein
	 */
	private class Connection implements Runnable {

		// cache object.refs
		// no private object.refs accessed by inner classes
		// TODO: reduce object creation

		/** Local Socket. */
		private final Socket local;
		/** Remote Socket. */
		private Socket remote;

		/** State: normal. */
		private static final short STATE_NORMAL = 0;
		/** State: closed by local side. */
		private static final short STATE_CLOSED_IN = 1;
		/** State: closed by remote side. */
		private static final short STATE_CLOSED_OUT = 2;
		/** Connections state. */
		private short state = STATE_NORMAL;

		/**
		 * CopyStream reads one stream and writes it's data into an other
		 * stream. Run this as a Thread.
		 * 
		 * @author Felix Bechstein
		 */
		private class CopyStream implements Runnable {
			/** Reader. */
			private final InputStream reader;
			/** Writer. */
			private final OutputStream writer;

			/** Size of buffer. */
			private static final int BUFFSIZE = 32768;
			/** Size of header buffer. */
			private static final int HEADERBUFFSIZE = 1024;

			/**
			 * Constructor.
			 * 
			 * @param r
			 *            reader
			 * @param w
			 *            writer
			 */
			public CopyStream(final InputStream r, final OutputStream w) {
				this.reader = new BufferedInputStream(r, BUFFSIZE);
				this.writer = w;
			}

			/**
			 * Run by Thread.start().
			 */
			@Override
			public void run() {
				byte[] buf = new byte[BUFFSIZE];
				int read = 0;
				final InputStream r = this.reader;
				final OutputStream w = this.writer;
				try {
					while (true) {
						read = r.available();
						if (read < 1 || read > BUFFSIZE) {
							read = BUFFSIZE;
						}
						read = r.read(buf, 0, read);
						if (read < 0) {
							break;
						}
						w.write(buf, 0, read);
						if (r.available() < 1) {
							w.flush();
						}
					}
					Connection.this.close(Connection.STATE_CLOSED_OUT);
					// this.writer.close();
				} catch (IOException e) {
					// FIXME: java.net.SocketException: Broken pipe
					// no idea, what causes this :/
					// Connection c = Connection.this;
					// String s = new String(buf, 0, read);
					Log.e(TAG, null, e);
				}
			}
		}

		/**
		 * Constructor.
		 * 
		 * @param socket
		 *            local Socket
		 */
		public Connection(final Socket socket) {
			this.local = socket;
		}

		/**
		 * Check if URL is blocked.
		 * 
		 * @param url
		 *            URL
		 * @return if URL is blocked?
		 */
		private boolean checkURL(final String url) {
			final ArrayList<String> f = Proxy.this.filter;
			final int s = f.size();
			for (int i = 0; i < s; i++) {
				if (url.indexOf(f.get(i)) >= 0) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Read in HTTP Header. Parse for URL to connect to.
		 * 
		 * @param reader
		 *            buffer reader from which we read the header
		 * @param buffer
		 *            buffer into which the header is written
		 * @return URL to which we should connect, port other than 80 is given
		 *         explicitly
		 * @throws IOException
		 *             inner IOException
		 */
		private URL readHeader(final BufferedInputStream reader,
				final StringBuilder buffer) throws IOException {
			URL ret = null;
			String[] strings;
			int avail;
			byte[] buf = new byte[CopyStream.HEADERBUFFSIZE];
			// read first line
			if (this.state == STATE_CLOSED_OUT) {
				return null;
			}
			avail = reader.available();
			if (avail > CopyStream.HEADERBUFFSIZE) {
				avail = CopyStream.HEADERBUFFSIZE;
			} else if (avail == 0) {
				avail = CopyStream.HEADERBUFFSIZE;
			}
			avail = reader.read(buf, 0, avail);
			if (avail < 1) {
				return null;
			}
			String line = new String(buf, 0, avail);
			String testLine = line;
			int i = line.indexOf(" http://");
			if (i > 0) {
				// remove "http://host:port" from line
				int j = line.indexOf('/', i + 9);
				if (j > i) {
					testLine = line.substring(0, i + 1) + line.substring(j);
				}
			}
			buffer.append(testLine);
			strings = line.split(" ");
			if (strings.length > 1) {
				if (strings[0].equals("CONNECT")) {
					String targetHost = strings[1];
					int targetPort = PORT_HTTPS;
					strings = targetHost.split(":");
					if (strings.length > 1) {
						targetPort = Integer.parseInt(strings[1]);
						targetHost = strings[0];
					}
					ret = new URL("https://" + targetHost + ":" + targetPort);
				} else if (strings[0].equals("GET")
						|| strings[0].equals("POST")) {
					String path = null;
					if (strings[1].startsWith("http://")) {
						ret = new URL(strings[1]);
						path = ret.getPath();
					} else {
						path = strings[1];
					}
					// read header
					String lastLine = line;
					do {
						testLine = lastLine + line;
						i = testLine.indexOf("\nHost: ");
						if (i >= 0) {
							int j = testLine.indexOf("\n", i + 6);
							if (j > 0) {
								String tHost = testLine.substring(i + 6, j)
										.trim();
								ret = new URL("http://" + tHost + path);
								break;
							} else {
								// test for "Host:" again with longer buffer
								line = lastLine + line;
							}
						}
						if (line.indexOf("\r\n\r\n") >= 0) {
							break;
						}
						lastLine = line;
						avail = reader.available();
						if (avail > 0) {
							if (avail > CopyStream.HEADERBUFFSIZE) {
								avail = CopyStream.HEADERBUFFSIZE;
							}
							avail = reader.read(buf, 0, avail);
							// FIXME: this may break
							line = new String(buf, 0, avail);
							buffer.append(line);
						}
					} while (avail > 0);
				} else {
					Log.d(TAG, "unknown method: " + strings[0]);
				}
			}
			strings = null;

			// copy rest of reader's buffer
			avail = reader.available();
			while (avail > 0) {
				if (avail > CopyStream.HEADERBUFFSIZE) {
					avail = CopyStream.HEADERBUFFSIZE;
				}
				avail = reader.read(buf, 0, avail);
				// FIXME: this may break!
				buffer.append(new String(buf, 0, avail));
				avail = reader.available();
			}
			return ret;
		}

		/**
		 * Close local and remote socket.
		 * 
		 * @param nextState
		 *            state to go to
		 * @return new state
		 * @throws IOException
		 *             IOException
		 */
		private synchronized short close(final short nextState)
				throws IOException {
			Log.d(TAG, "close(" + nextState + ")");
			short mState = this.state;
			if (mState == STATE_NORMAL || nextState == STATE_NORMAL) {
				mState = nextState;
			}
			Socket mSocket;
			if (mState != STATE_NORMAL) {
				// close remote socket
				mSocket = this.remote;
				if (mSocket != null && mSocket.isConnected()) {
					try {
						mSocket.shutdownInput();
						mSocket.shutdownOutput();
					} catch (IOException e) {
						Log.d(TAG, null, e);
					}
					mSocket.close();
				}
				this.remote = null;
			}
			if (mState == STATE_CLOSED_OUT) {
				// close local socket
				mSocket = this.local;
				if (mSocket.isConnected()) {
					try {
						mSocket.shutdownOutput();
						mSocket.shutdownInput();
					} catch (IOException e) {
						Log.d(TAG, null, e);
					}
					mSocket.close();
				}
			}
			this.state = mState;
			return mState;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			BufferedInputStream lInStream;
			OutputStream lOutStream;
			BufferedWriter lWriter;
			try {
				lInStream = new BufferedInputStream(
						this.local.getInputStream(), CopyStream.BUFFSIZE);
				lOutStream = this.local.getOutputStream();
				lWriter = new BufferedWriter(
						new OutputStreamWriter(lOutStream), // .
						CopyStream.BUFFSIZE);
			} catch (IOException e) {
				Log.e(TAG, null, e);
				return;
			}
			try {
				InputStream rInStream = null;
				OutputStream rOutStream = null;
				BufferedWriter remoteWriter = null;
				Thread rThread = null;
				StringBuilder buffer = new StringBuilder();
				boolean block = false;
				String tHost = null;
				int tPort = -1;
				URL url;
				boolean connectSSL = false;
				while (this.local.isConnected()) {
					buffer = new StringBuilder();
					url = this.readHeader(lInStream, buffer);
					if (buffer.length() == 0) {
						break;
					}
					if (this.local.isConnected() && rThread != null
							&& !rThread.isAlive()) {
						// socket should be closed allready..
						Log.d(TAG, "close dead remote");
						if (connectSSL) {
							this.local.close();
						}
						tHost = null;
						rInStream = null;
						rOutStream = null;
						rThread = null;
					}
					if (url != null) {
						block = this.checkURL(url.toString());
						Log.d(TAG, "new url: " + url.toString());
						if (!block) {
							// new connection needed?
							int p = url.getPort();
							if (p < 0) {
								p = PORT_HTTP;
							}
							if (tHost == null || !tHost.equals(url.getHost())
									|| tPort != p) {
								// create new connection
								Log.d(TAG, "shutdown old remote");
								this.close(STATE_CLOSED_IN);
								if (rThread != null) {
									rThread.join();
									rThread = null;
								}

								tHost = url.getHost();
								tPort = p;
								Log.d(TAG, "new socket: " + url.toString());
								this.state = STATE_NORMAL;
								this.remote = new Socket();
								this.remote.connect(new InetSocketAddress(
										tHost, tPort));
								rInStream = this.remote.getInputStream();
								rOutStream = this.remote.getOutputStream();
								rThread = new Thread(new CopyStream(rInStream,
										lOutStream));
								rThread.start();
								if (url.getProtocol().startsWith("https")) {
									connectSSL = true;
									lWriter.write(HTTP_CONNECTED
											+ HTTP_RESPONSE);
									lWriter.flush();
									// copy local to remote by blocks
									Thread t2 = new Thread(new CopyStream(
											lInStream, rOutStream));

									t2.start();
									remoteWriter = null;
									break; // copy in separate thread. break
									// while here
								} else {
									remoteWriter = new BufferedWriter(
											new OutputStreamWriter(rOutStream),
											CopyStream.BUFFSIZE);
								}
							}
						}
					}
					// push data to remote if not blocked
					if (block) {
						lWriter.append(HTTP_BLOCK + HTTP_RESPONSE
								+ "BLOCKED by AdBlock!");
						lWriter.flush();
					} else {
						Socket mSocket = this.remote;
						if (mSocket != null && mSocket.isConnected()
								&& remoteWriter != null) {
							try {
								remoteWriter.append(buffer);
								remoteWriter.flush();
							} catch (IOException e) {
								Log.d(TAG, buffer.toString(), e);
							}
						}
					}
				}
				if (rThread != null && rThread.isAlive()) {
					rThread.join();
				}
			} catch (InterruptedException e) {
				Log.e(TAG, null, e);
			} catch (IOException e) {
				Log.e(TAG, null, e);
				try {
					lWriter.append(HTTP_ERROR + " - " + e.toString()
							+ HTTP_RESPONSE + e.toString());
					lWriter.flush();
					lWriter.close();
					this.local.close();
				} catch (IOException e1) {
					Log.e(TAG, null, e1);
				}
			}
			Log.d(TAG, "close connection");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final IBinder onBind(final Intent intent) {
		return null;
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 * 
	 * @param id
	 *            {@link Notification} id
	 * @param notification
	 *            {@link Notification}
	 * @param foreNotification
	 *            for display of {@link Notification}
	 */
	private void startForegroundCompat(final int id,
			final Notification notification, final boolean foreNotification) {
		// If we have the new startForeground API, then use it.
		if (this.mStartForeground != null) {
			this.mStartForegroundArgs[0] = Integer.valueOf(id);
			this.mStartForegroundArgs[1] = notification;
			try {
				this.mStartForeground.invoke(this, this.mStartForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke startForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke startForeground", e);
			}
		} else {
			// Fall back on the old API.
			this.setForeground(true);
		}

		if (foreNotification) {
			this.mNM.notify(id, notification);
		}
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	private void stopForegroundCompat() {
		this.mNM.cancelAll();
		// If we have the new stopForeground API, then use it.
		if (this.mStopForeground != null) {
			this.mStopForegroundArgs[0] = Boolean.TRUE;
			try {
				this.mStopForeground.invoke(this, this.mStopForegroundArgs);
			} catch (InvocationTargetException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke stopForeground", e);
			} catch (IllegalAccessException e) {
				// Should not happen.
				Log.w(TAG, "Unable to invoke stopForeground", e);
			}
		} else {
			// Fall back on the old API. Note to cancel BEFORE changing the
			// foreground state, since we could be killed at that point.
			// this.mNM.cancel(id);
			this.setForeground(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate() {
		super.onCreate();
		this.mNM = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		try {
			this.mStartForeground = this.getClass().getMethod(
					"startForeground", START_FOREGROUND_SIGNATURE);
			this.mStopForeground = this.getClass().getMethod("stopForeground",
					STOP_FOREGROUND_SIGNATURE);
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			this.mStartForeground = null;
			this.mStopForeground = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onStart(final Intent intent, final int startId) {
		super.onStart(intent, startId);

		// Don't kill me!
		final Notification notification = new Notification(
				R.drawable.stat_notify_proxy, "", System.currentTimeMillis());
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, AdBlock.class), 0);
		notification.setLatestEventInfo(this, this
				.getString(R.string.notify_proxy), "", contentIntent);
		notification.defaults |= Notification.FLAG_NO_CLEAR;
		this.startForegroundCompat(0, notification, false);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		int p = Integer.parseInt(preferences.getString(PREFS_PORT, "8080"));
		boolean portChanged = p != this.port;
		this.port = p;

		String f = preferences.getString(PREFS_FILTER, "");
		final ArrayList<String> fl = this.filter;
		fl.clear();
		for (String s : f.split("\n")) {
			if (s.length() > 0) {
				fl.add(s);
			}
		}
		if (this.proxy == null) {
			// Toast.makeText(this, "starting proxy on port: " + this.port,
			// Toast.LENGTH_SHORT).show();
			final Thread pr = new Thread(this);
			pr.start();
			this.proxy = pr;
		} else {
			Toast.makeText(this,
					this.getString(R.string.proxy_running) + " " + this.port,
					Toast.LENGTH_SHORT).show();
			if (portChanged) {
				Thread pr = this.proxy;
				pr.interrupt();
				pr = new Thread(this);
				pr.start();
				this.proxy = pr;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onDestroy() {
		super.onDestroy();
		Toast.makeText(this, R.string.proxy_stopped, Toast.LENGTH_LONG).show();
		this.stop = true;
		this.stopForegroundCompat();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void run() {
		try {
			int p = this.port;
			ServerSocket sock = new ServerSocket(p);
			Socket client;
			while (!this.stop && p == this.port) {
				if (p != this.port) {
					break;
				}
				client = sock.accept();
				if (client != null) {
					Log.d(TAG, "new client");
					Thread t = new Thread(new Connection(client));
					t.start();
				}
			}
			sock.close();
		} catch (IOException e) {
			Log.e(TAG, null, e);
		}
	}
}


/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of AdBlock.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */

package de.ub0r.android.adBlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ProxyStarter listens to any Broadcast. It'l start the Proxy Service on
 * receive.
 * 
 * @author Felix Bechstein
 */
public class ProxyStarter extends BroadcastReceiver {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onReceive(final Context context, final Intent intent) {
		context.startService(new Intent(context, Proxy.class));
	}
}


