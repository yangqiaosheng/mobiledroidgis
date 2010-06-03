/*
 * Copyright (C) 2010 by Mathias Menninghaus (mmenning (at) uos (dot) de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmenning.mobilegis.surface3d;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ListIterator;

import mmenning.mobilegis.Preferences;
import mmenning.mobilegis.R;
import mmenning.mobilegis.surface3d.GOCADConnector.TSFormatException;
import mmenning.mobilegis.util.Base64;
import mmenning.mobilegis.util.LoginData;
import mmenning.mobilegis.util.LoginDialog;
import mmenning.mobilegis.util.LoginDialog.LoginDialogListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity for displaying one or more TS (GOCAD) Files with OpenGL|ES. Reads
 * url or file data to display.
 * 
 * @author Mathias Menninghaus
 * @version 03.06.2010
 * @see GLSurfaceView
 * @see GOCADConnector
 * @see SurfaceRenderer
 * 
 */
public class SurfaceVisualizer extends Activity {

	private static final int LOADING_PROGRESS_DIALOG = 0;
	private static final int EXCEPTION_DIALOG = 1;
	private static final int LOADFROMURL_DIALOG = 2;
	private static final int SAVETOFILE_DIALOG = 4;
	private static final int SAVING_PROGRESS_DIALOG = 5;
	private static final int LOGIN_DIALOG = 6;

	private static final int NO_EXCEPTION_TO_SHOW = -1;
	private int EXCEPTION_DIALOG_MESSAGE = NO_EXCEPTION_TO_SHOW;

	/**
	 * Constant to seperate URLS in a String
	 */
	public static final String URL_SEPERATOR = ";";

	/*
	 * Constants to set up the menu
	 */
	private static final int CHOOSER = 1;
	private static final int SETTINGS = 2;
	private static final int FILEMANAGER = 4;
	private static final int LOADFROMURL = 5;
	private static final int SAVETOFILE = 6;

	/**
	 * Tag for Android-LogFile
	 */
	private static final String DT = "SurfaceVisualizer";

	/**
	 * GLSurfaceView on which the SurfaceRenderer will work
	 */
	private GLSurfaceView glView;
	/**
	 * View to switch between different parsedLayers and manipulate them
	 */
	private View chooserView;
	/**
	 * Listener for communication with the SurfaceRenderer
	 */
	private Listener3D l;
	/**
	 * Connection to the source-file(s)
	 */
	private GOCADConnector connect3D;
	/**
	 * the actual choosen OGLLayer (needed for chooserView)
	 */
	private OGLLayer actual;
	/**
	 * Iterator for the OGLLayer which are displayed (needed for chosserView)
	 */
	private ListIterator<OGLLayer> chooser;
	/**
	 * GOCADFileManager to read and write GOCADFiles
	 */
	private GOCADFileManager manager;

	/*
	 * views components
	 */
	private CheckBox visible;
	private CheckBox fill;
	private Button forward;
	private Button backward;
	private TextView name;
	private Controls3D switchtouchopt;

	/*
	 * fields to handle touch events
	 */
	private float xstart;
	private float ystart;

	private static Uri actualUri;

	private LoginData loginDB;

	/*
	 * create menu. only settings, chooser, filemanager, save to file and load
	 * from url
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CHOOSER, 0, R.string.choose_layer).setIcon(
				R.drawable.menu_chooser);
		menu.add(0, FILEMANAGER, 0, R.string.filemanager).setIcon(
				R.drawable.menu_filemanager);
		menu.add(0, LOADFROMURL, 0, R.string.load_url).setIcon(
				R.drawable.menu_load);
		menu.add(0, SAVETOFILE, 0, R.string.save).setIcon(R.drawable.menu_save);
		menu.add(0, SETTINGS, 0, R.string.preferences).setIcon(
				R.drawable.menu_preferences);

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CHOOSER:
			chooserEnabledDisabled();
			return true;
		case SETTINGS:
			Intent intent = new Intent(this, Preferences.class);
			this.startActivity(intent);
			return true;
		case FILEMANAGER:
			this.showFileManagerDialog();
			return true;
		case LOADFROMURL:
			this.showDialog(LOADFROMURL_DIALOG);
			return true;
		case SAVETOFILE:
			this.showDialog(SAVETOFILE_DIALOG);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * only Handle Touch events if the Controls3D Button is linked
		 */
		if (switchtouchopt == null) {
			return super.onTouchEvent(event);
		}
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			/*
			 * recognize the beginning of the action
			 */
			xstart = event.getX();
			ystart = event.getY();
			return true;
		case MotionEvent.ACTION_MOVE:
			/*
			 * change in relation to the actual position and the last position
			 * 
			 * switch between several states of Controls3D Button
			 */
			switch (switchtouchopt.getStatus()) {
			case Controls3D.MOVEXY:
				l.setXwalk(l.getXwalk()
						+ ((event.getX() - xstart) * l.getSensibility()));
				l.setYwalk(l.getYwalk()
						+ ((event.getY() - ystart) * l.getSensibility()));
				break;
			case Controls3D.ROTATE:
				l.setXrot(l.getXrot()
						+ ((event.getY() - ystart) * l.getSensibility()));
				l.setYrot(l.getYrot()
						+ ((event.getX() - xstart) * l.getSensibility()));
				break;
			case Controls3D.MOVEZ:
				l.setZwalk(l.getZwalk()
						+ ((event.getY() - ystart) * l.getSensibility()));
				break;
			}
			/*
			 * the last position is now the actual position -> for the next time
			 */
			xstart = event.getX();
			ystart = event.getY();
			return true;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * Enables/Disables the chooser view.
	 */
	private void chooserEnabledDisabled() {
		/*
		 * should not be possible to generate a chooser view if it is not linked
		 * yet
		 */
		if (chooserView != null && !connect3D.getTsObjects().isEmpty()) {
			if (chooserView.getVisibility() == View.GONE) {
				actual.setSelected(true);
				chooserView.setVisibility(View.VISIBLE);

			} else {
				actual.setSelected(false);
				chooserView.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * always called when the choosen element changes (previous or next element
	 * operation). sets the view up for the actual element.
	 */
	private void elemChanged() {
		name.setText(actual.getName());
		visible.setChecked(actual.isVisible());
		fill.setChecked(actual.isFill());
		int color = actual.getColor();
		chooserView.setBackgroundColor(Color.argb(127, Color.red(color), Color
				.green(color), Color.blue(color)));
	}

	/**
	 * Generate the ChooserView. It makes it possible to switch between the
	 * available TSObjects, to set them visible/invisible and to fill them or
	 * display them as points
	 */
	private void generateChooserView() {

		/*
		 * link to the view
		 */

		visible = (CheckBox) this
				.findViewById(R.id.surfacevisualizer_visibleButton);
		fill = (CheckBox) this.findViewById(R.id.surfacevisualizer_fillButton);
		backward = (Button) this
				.findViewById(R.id.surfacevisualizer_backwardButton);
		forward = (Button) this
				.findViewById(R.id.surfacevisualizer_forwardButton);
		name = (TextView) this
				.findViewById(R.id.surfacevisualizer_tsbject_name);
		/*
		 * get the iterator and the first element in the list
		 */
		chooser = connect3D.getTsObjects().listIterator();

		if (chooser.hasNext()) {
			actual = chooser.next();
			elemChanged();

			/* actual element visible - invisible */
			visible
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							actual.setVisible(isChecked);
						}
					});
			/* actual element filled - wired */
			fill
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							actual.setFill(isChecked);
						}

					});
			/* previous element */
			backward.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (chooser.hasPrevious()) {
						actual.setSelected(false);
						actual = chooser.previous();
						actual.setSelected(true);
						elemChanged();
					}
				}
			});

			/* next element */
			forward.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (chooser.hasNext()) {
						actual.setSelected(false);
						actual = chooser.next();
						actual.setSelected(true);
						elemChanged();
					}
				}
			});
		}
	}

	/**
	 * display the viewing control
	 */
	private void generateControls() {
		switchtouchopt = (Controls3D) this
				.findViewById(R.id.surfacevisualizer_switchtouchopt);
	}

	/**
	 * inits SurfaceRenderer with basic settings and relates it to the
	 * GLSurfaceView
	 */
	private void generateGLView() {
		/*
		 * determine the initial distance from the user to the scene. assuming
		 * that the user views with an angle of 45° degree to each side and
		 * therefore the tangens is 1. so the initial distance is the maximum
		 * extent in x, y and z direction.
		 */
		float dist = Math.max(Math.abs(connect3D.getMaxX()
				- connect3D.getMinX()), Math.abs(connect3D.getMaxY()
				- connect3D.getMinY())) / 2;

		/*
		 * set up the distance through the listener
		 */
		l.setZDistance(dist);

		/*
		 * at first the scene is in the origin (@see DatabaseConnector3D)
		 */
		l.setXwalk(0);
		l.setYwalk(0);
		l.setZwalk(0);

		/*
		 * Set up Frustum by using the initial distance. Assuming that one
		 * wouldn't zoom out more than the double initial distance because with
		 * the initial distance he has an overview over the whole scene. Also
		 * rotating the scene with initial distance will not be affected by such
		 * a far BackPlane of the Frustum.
		 */
		l.setZFar(dist * 2);
		l.setZNear(1);

		/*
		 * Set up initial values for from the user modifiable settings by using
		 * the settings Database.
		 */
		setOptions();

		SurfaceRenderer rend = new SurfaceRenderer(this, l, connect3D
				.getTsObjects());

		/*
		 * link renderer to the view
		 */
		glView.setRenderer(rend);
		/*
		 * inititalize the view
		 */
		glView.requestFocus();
	}

	private void loadFromURL(Uri uri) {

		this.actualUri = uri;

		String urlString = Uri.decode(uri.toString());
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(urlString)
					.openConnection();
			con.setUseCaches(false);
			con.connect();

			if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				con.disconnect();
				String[] login = loginDB.getLogin(actualUri.getHost());
				if (login == null) {

					this.showDialog(LOGIN_DIALOG);
					return;
				} else {
					Log.d(DT, "Atempts to reconnect");
					con = (HttpURLConnection) new URL(urlString)
							.openConnection();
					con.setUseCaches(false);
					con.setRequestProperty("Authorization",
							"basic "
									+ Base64.encodeBytes((login[LoginData.USER]
											+ ":" + login[LoginData.PW])
											.getBytes()));
					con.connect();
					if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
						con.disconnect();
						this.showDialog(LOGIN_DIALOG);
						return;

					}
				}
			}

			new Thread(new LoadingThread(new LoadingHandler(), con, null))
					.start();

		} catch (MalformedURLException ex) {
			Log.w(DT, ex);
			EXCEPTION_DIALOG_MESSAGE = R.string.mlfurlex;
			this.showDialog(EXCEPTION_DIALOG);
		} catch (IOException ex) {
			Log.w(DT, ex);
			EXCEPTION_DIALOG_MESSAGE = R.string.ioexc;
			this.showDialog(EXCEPTION_DIALOG);
		}

	}

	/**
	 * Set up the listeners options using the database connection the listener
	 * and db-connection must not be null at this moment!
	 */
	private void setOptions() {

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		l.setFullGraphics(prefs.getBoolean(this.getText(R.string.fullgraphics)
				.toString(), false));
	}

	/**
	 * Shows a File Manager Dialog. Because it should display itself again for
	 * refreshing the list, not showDialog(int) is used.
	 */
	private void showFileManagerDialog() {
		final File[] files = manager.getStoredFiles();
		final CharSequence[] items = new CharSequence[files.length];
		for (int i = 0; i < files.length; i++) {
			items[i] = files[i].getName();
		}
		final boolean[] selectedItems = new boolean[files.length];

		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(this.getString(R.string.manage_your_files));
		b.setMultiChoiceItems(items, selectedItems,
				new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						selectedItems[which] = isChecked;
					}
				});
		b.setPositiveButton(R.string.load,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						if (selectedItems.length != 0) {
							int cnt = 0;
							for (boolean b : selectedItems) {
								if (b)
									cnt++;
							}
							File[] filesToLoad = new File[cnt];
							for (int i = 0, k = 0; i < files.length; i++) {
								if (selectedItems[i]) {
									filesToLoad[k++] = files[i];
								}
							}
							new Thread(new LoadingThread(new LoadingHandler(),
									null, filesToLoad)).start();
						}
					}
				});
		b.setNeutralButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.cancel();
					}
				});
		b.setNegativeButton(R.string.delete,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						dialog.dismiss();
						if (selectedItems.length != 0) {
							for (int i = 0; i < files.length; i++) {
								if (selectedItems[i]) {
									files[i].delete();
								}
							}
						}
						SurfaceVisualizer.this.showFileManagerDialog();
					}
				});
		b.create().show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SurfaceVisualizer.this.setContentView(R.layout.surfacevisualizer_clear);
		/*
		 * generate a listener to communicate with the renderer
		 */

		l = new Listener3D();

		loginDB = new LoginData(this);
		connect3D = new GOCADConnector();
		manager = new GOCADFileManager(this);

		Intent i = this.getIntent();

		Uri uri = i.getData();
		if (i.getAction() != null
				&& (Intent.ACTION_VIEW).compareTo(i.getAction()) == 0) {

			if (uri.getScheme().compareTo("http") == 0) {
				this.loadFromURL(uri);

			} else if (uri.getScheme().compareTo("file") == 0) {
				new Thread(new LoadingThread(new LoadingHandler(), null,
						new File[] { new File(uri.getPath()) })).start();
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder b;
		switch (id) {

		case LOADING_PROGRESS_DIALOG:
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setMessage(this.getString(R.string.loading__));
			loadingDialog.setCancelable(false);
			return loadingDialog;
		case LOADFROMURL_DIALOG:
			b = new AlertDialog.Builder(this);
			final EditText et = new EditText(this);
			b.setView(et);
			et
					.setText("http://feanor.igf.uos.de:8182/projects/GeoTopo3D/Balingen3D/1.ts");
			b.setTitle(R.string.load_url);
			b.setPositiveButton(R.string.confirm,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							loadFromURL(Uri.parse(et.getText().toString()));
						}

					});
			b.setNegativeButton(R.string.cancel,
					new AlertDialog.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			return b.create();

		case EXCEPTION_DIALOG:
			b = new AlertDialog.Builder(this);
			b.setTitle(this.getString(R.string.loadingfailure));

			b
					.setMessage(
							this
									.getString(EXCEPTION_DIALOG_MESSAGE != NO_EXCEPTION_TO_SHOW ? EXCEPTION_DIALOG_MESSAGE
											: R.string.loadingfailure))
					.setCancelable(false).setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();

								}
							});
			return b.create();
		case SAVETOFILE_DIALOG:
			b = new AlertDialog.Builder(this);
			final EditText filename = new EditText(this);
			b.setView(filename);
			b.setTitle(R.string.save_to_file);
			b.setPositiveButton(R.string.confirm,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if (connect3D.getTsObjects() != null) {
								new Thread(new SavingThread(
										new SavingHandler(), filename.getText()
												.toString())).start();
							}
						}
					});
			b.setNegativeButton(R.string.cancel,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			return b.create();

		case SAVING_PROGRESS_DIALOG:
			ProgressDialog savingDialog = new ProgressDialog(this);
			savingDialog.setMessage(this.getString(R.string.saving__));
			savingDialog.setCancelable(false);
			return savingDialog;
		case LOGIN_DIALOG:
			LoginDialog loginDialog = new LoginDialog(this);
			loginDialog.setLoginDialogListener(new LoginDialogListener() {

				@Override
				public void onCancel(LoginDialog dialog) {
					dialog.dismiss();
				}

				@Override
				public void onConfirm(LoginDialog dialog, String username,
						String password, boolean savepw) {
					dialog.dismiss();

					String urlString = Uri.decode(actualUri.toString());
					try {
						HttpURLConnection con = (HttpURLConnection) new URL(
								urlString).openConnection();
						con.setUseCaches(false);
						con
								.setRequestProperty("Authorization",
										"basic "
												+ Base64.encodeBytes((username
														+ ":" + password)
														.getBytes()));
						con.connect();
						if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
							con.disconnect();
							EXCEPTION_DIALOG_MESSAGE = R.string.wronguserpw;
							SurfaceVisualizer.this.showDialog(EXCEPTION_DIALOG);
						} else {
							if (savepw) {
								loginDB.insertLogin(actualUri.getHost(),
										username, password);
							}

							new Thread(new LoadingThread(new LoadingHandler(),
									con, null)).start();
						}
					} catch (MalformedURLException ex) {
						Log.w(DT, ex);
						EXCEPTION_DIALOG_MESSAGE = R.string.mlfurlex;
						SurfaceVisualizer.this.showDialog(EXCEPTION_DIALOG);
					} catch (IOException ex) {
						Log.w(DT, ex);
						EXCEPTION_DIALOG_MESSAGE = R.string.ioexc;
						SurfaceVisualizer.this.showDialog(EXCEPTION_DIALOG);
					}
				}
			});
			return loginDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPause() {
		loginDB.close();

		if (glView != null)
			glView.onPause();
		super.onPause();

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case EXCEPTION_DIALOG:
			if (EXCEPTION_DIALOG_MESSAGE != NO_EXCEPTION_TO_SHOW) {
				((AlertDialog) dialog).setMessage(this
						.getString(this.EXCEPTION_DIALOG_MESSAGE));
			}
			break;
		case LOGIN_DIALOG:
			LoginDialog loginDialog = (LoginDialog) dialog;
			loginDialog.setTitle(this.getString(R.string.loginto) + ": "
					+ (actualUri != null ? actualUri.getHost() : ""));
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected void onResume() {
		super.onResume();

		loginDB.open();
		/*
		 * actualize the listener onResume from the Setting3D Activity
		 */
		setOptions();
		if (glView != null) {
			glView.onResume();
		}
	}

	private class LoadingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case LoadingThread.START:
				SurfaceVisualizer.this.showDialog(LOADING_PROGRESS_DIALOG);
				return;

			case LoadingThread.LOADSUCCESS:
				SurfaceVisualizer.this.dismissDialog(LOADING_PROGRESS_DIALOG);
				/*
				 * init the view
				 */
				SurfaceVisualizer.this
						.setContentView(R.layout.surfacevisualizer);
				/*
				 * generate GLSurfaceView
				 */
				glView = (GLSurfaceView) SurfaceVisualizer.this
						.findViewById(R.id.surfacevisualizer_glsurfaceview);

				/*
				 * generate chooserView
				 */
				chooserView = SurfaceVisualizer.this
						.findViewById(R.id.surfacevisualizer_chooserview);

				generateControls();
				generateGLView();
				generateChooserView();
				return;
			}
			SurfaceVisualizer.this.dismissDialog(LOADING_PROGRESS_DIALOG);
			SurfaceVisualizer.this
					.setContentView(R.layout.surfacevisualizer_clear);
			chooserView = null;
			glView = null;
			switch (msg.what) {
			case LoadingThread.NUMBERFORMATEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.numberformatexc;
				break;
			case LoadingThread.TSEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.tsexc;
				break;

			case LoadingThread.IOEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.ioexc;
				break;
			case LoadingThread.MFURLEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.mlfurlex;
				break;

			case LoadingThread.CONNECTEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.connectex;
				break;

			case LoadingThread.UNKNOWNHEX:
				EXCEPTION_DIALOG_MESSAGE = R.string.unknownhex;
				break;
			}
			SurfaceVisualizer.this.showDialog(EXCEPTION_DIALOG);
		}
	}

	/**
	 * Within a Loading Thread the GOCADConnecter will read the Data from the
	 * actual URL
	 * 
	 * @author Mathias Menninghaus
	 * 
	 */
	private class LoadingThread implements Runnable {

		public static final int START = 0;
		public static final int LOADSUCCESS = 1;
		public static final int NUMBERFORMATEX = 2;
		public static final int TSEX = 3;
		public static final int IOEX = 4;
		public static final int MFURLEX = 5;
		public static final int CONNECTEX = 6;
		public static final int UNKNOWNHEX = 7;

		private Handler handler;

		private HttpURLConnection input;
		private File[] files;

		public LoadingThread(Handler handler, HttpURLConnection input,
				File[] filesToLoad) {
			if (input != null && filesToLoad != null) {
				throw new IllegalArgumentException(
						"can only load either from strean or from file");
			}
			this.handler = handler;
			this.input = input;
			this.files = filesToLoad;
		}

		public void run() {

			handler.sendEmptyMessage(START);
			/*
			 * read data from urls
			 */
			try {
				if (input != null) {
					connect3D.requestTS(input);
				} else {
					connect3D.requestTS(files);
				}
				handler.sendEmptyMessage(LOADSUCCESS);

			} catch (ConnectException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(CONNECTEX);
			} catch (UnknownHostException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(UNKNOWNHEX);
			} catch (MalformedURLException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(MFURLEX);
			} catch (NumberFormatException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(NUMBERFORMATEX);
			} catch (TSFormatException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(TSEX);
			} catch (IOException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(IOEX);
			}
		}
	}

	private class SavingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SavingThread.START:
				SurfaceVisualizer.this.showDialog(SAVING_PROGRESS_DIALOG);
				break;
			case SavingThread.IOEX:
				SurfaceVisualizer.this.dismissDialog(SAVING_PROGRESS_DIALOG);
				EXCEPTION_DIALOG_MESSAGE = R.string.ioexc;
				SurfaceVisualizer.this.showDialog(EXCEPTION_DIALOG);
			case SavingThread.SAVESUCCESS:
				SurfaceVisualizer.this.dismissDialog(SAVING_PROGRESS_DIALOG);
			}

		}

	}

	private class SavingThread implements Runnable {

		public static final int START = 0;
		public static final int SAVESUCCESS = 1;
		public static final int IOEX = 2;

		private Handler handler;
		private String filename;

		public SavingThread(Handler handler, String filename) {
			this.filename = filename;
			this.handler = handler;
		}

		public void run() {
			handler.sendEmptyMessage(START);

			try {
				manager.storeToFile(connect3D.getTsObjects(), filename,
						GOCADFileManager.TS);
				handler.sendEmptyMessage(SAVESUCCESS);
			} catch (IOException e) {
				Log.w(DT, e);
				handler.sendEmptyMessage(IOEX);
			}
		}
	}
}