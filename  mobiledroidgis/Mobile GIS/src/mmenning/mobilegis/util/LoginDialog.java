package mmenning.mobilegis.util;

import mmenning.mobilegis.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginDialog extends Dialog {

	private static final String DT = "LoginDialog";

	private Context context;

	private LoginDialogListener loginDialogListener;

	public LoginDialog(Context context) {
		super(context);
		this.context = context;
	}

	public void setLoginDialogListener(LoginDialogListener loginDialogListener) {
		this.loginDialogListener = loginDialogListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.login);

		final EditText username = (EditText) this
				.findViewById(R.id.login_username);

		final EditText password = (EditText) this
				.findViewById(R.id.login_password);

		final CheckBox savepw = (CheckBox) this.findViewById(R.id.login_savepw);

		Button confirm = (Button) this.findViewById(R.id.login_confirm);
		confirm.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				loginDialogListener.onConfirm(LoginDialog.this, username
						.getText().toString(), password.getText().toString(),
						savepw.isChecked());
			}

		});

		Button cancel = (Button) this.findViewById(R.id.login_cancel);
		cancel.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				loginDialogListener.onCancel(LoginDialog.this);

			}

		});

	}

	public interface LoginDialogListener {

		public void onCancel(LoginDialog dialog);

		public void onConfirm(LoginDialog dialog, String username,
				String password, boolean savepw);
	}
}
