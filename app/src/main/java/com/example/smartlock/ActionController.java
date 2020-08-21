package com.example.smartlock;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class ActionController extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<CredentialRequestResult>, View.OnClickListener {

    Button btnSignOut, btnSignOutDisableAuto, btnDelete;
    private GoogleApiClient mGoogleApiClient;
    CredentialsClient mCredentialsApiClient;
    CredentialRequest mCredentialRequest;
    public static final String TAG = "ActionController";
    private static final int RC_REQUEST = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        setContentViews();
        setUpGoogleApiClient();


    }
//  private methods

    private void setContentViews() {
        btnSignOut = findViewById(R.id.btnSignOut);
        btnSignOutDisableAuto = findViewById(R.id.btnSignOutDisableAutoSign);
        btnDelete = findViewById(R.id.btnDeleteAccount);
        mCredentialsApiClient = Credentials.getClient(this);
        btnDelete.setOnClickListener(this);
        btnSignOut.setOnClickListener(this);
        btnSignOutDisableAuto.setOnClickListener(this);

    }

    private void setUpGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Auth.CREDENTIALS_API)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignOut:
                signOut(false);
                break;
            case R.id.btnSignOutDisableAutoSign:
                signOut(true);
                break;
            case R.id.btnDeleteAccount:
                requestCredentials();

                break;
        }
    }

    private void requestCredentials() {
        mCredentialRequest = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        Auth.CredentialsApi.request(mGoogleApiClient, mCredentialRequest).setResultCallback(this);
    }

    private void signOut(boolean disableAutoSignIn) {

        if (disableAutoSignIn)
            Auth.CredentialsApi.disableAutoSignIn(mGoogleApiClient);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void onCredentialSuccess(Credential credential) {

        Auth.CredentialsApi.delete(mGoogleApiClient, credential).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    signOut(false);
                } else {
                    showToast("Account Deletion Failed");
                }
            }
        });


    }
    // End Private Methods


    //Google Api Client Call back methods

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull CredentialRequestResult credentialRequestResult) {
        Status status = credentialRequestResult.getStatus();
        if (status.isSuccess()) {
            onCredentialSuccess(credentialRequestResult.getCredential());
        } else {
            if (status.hasResolution()) {
                try {
                    status.startResolutionForResult(this, RC_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    Log.d(TAG, e.toString());
                }
            } else {
                showToast("Request Failed");
            }
        }

    }

    @Override
    protected void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_REQUEST) {
            if (resultCode == RESULT_OK) {
                showToast("Deleted");
                Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                onCredentialSuccess(credential);
            } else {
                Log.d(TAG, "Request failed");
            }
        }
    }

    //  End Google Api Client Call back methods

}
