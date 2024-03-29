package com.example.googleloginexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private SignInButton btn_google; //구글 로그인 버튼
    private FirebaseAuth auth; //파이어베이스 인증 객체
    private GoogleApiClient googleApiClient; //구글 API 클라이언트 객체
    private static final int REQ_SIGN_GOOGLE=100; //구글 로그인 결과 코드
    private static final int resultCode=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //앱이 실행될 때 처음 수행되는 곳
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();

        googleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions).build();

        auth=FirebaseAuth.getInstance(); //파이어베이스 인증 객체 초기화

        btn_google=findViewById(R.id.btn_google);
        btn_google.setOnClickListener(new View.OnClickListener() { //구글 로그인 버튼을 클릭했을 때 이곳을 수행
            @Override
            public void onClick(View v) {
                Intent intent=Auth.GoogleSignInApi.getSignInIntent(googleApiClient); //구글 sign 인증을 받고 돌아옴
                startActivityForResult(intent,REQ_SIGN_GOOGLE); //그 결과를 돌려받아


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //구글 로그인 인증을 요청했을 때 결과 값을 되돌려 받는 곳
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQ_SIGN_GOOGLE){
            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()) { //인증 결과가 성공적이면
                GoogleSignInAccount account = result.getSignInAccount(); //account라는 데이터는 구글 로그인 정보를 담고 있습니다. (닉네임, 프로필 사진, 이메일 주소... 등)
                String acnt=account.getEmail().substring(account.getEmail().length()-11);
                if(acnt.equals("@g.skku.edu"))
                {
                    resultLogin(account); //로그인 결과 값 출력하라는 메소드

                }else {

                    Toast.makeText(MainActivity.this,"성균관대학교 구글 아이디가 아니므로 로그인 실패하였으며, 해당 구글 메일은 저장되지 않습니다.",Toast.LENGTH_LONG).show();
                    Auth.GoogleSignInApi.signOut(googleApiClient); //현재 로그인되어 있는 구글 아이디 로그아웃한 뒤 다른 구글 메일로 재로그인할 수 있도록 함!

                }

            }
        }
    }


    private void resultLogin(GoogleSignInAccount account) {
        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) { //로그인에 실제 성공했는지
                        if(task.isSuccessful()){//로그인이 성공했으면..
                            Toast.makeText(MainActivity.this,"로그인 성공",Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(getApplicationContext(), HomeActivity.class);
                            intent.putExtra("nickname",account.getDisplayName()); //다음 액티비티에서 받을 내용
                            intent.putExtra("photoUrl",String.valueOf(account.getPhotoUrl())); //String.valueOf 특정 자료형을 String 형태로 변환

                            startActivity(intent);//액티비티 이동
                        } else{ //로그인 실패?
                            Toast.makeText(MainActivity.this,"로그인 실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}