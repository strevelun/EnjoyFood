package com.project.enjoyfood.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.enjoyfood.MainActivity
import com.project.enjoyfood.R
import com.project.enjoyfood.databinding.ActivityJoinBinding

class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding : ActivityJoinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)

        binding.joinBtn.setOnClickListener {

            var isJoin = true

            val email = binding.emailText.text.toString()
            val password = binding.passwordText1.text.toString()
            val passwordck = binding.passwordText2.text.toString()

            if(email.isEmpty()) {
                Toast.makeText(this,"이메일을 입력해주세요",Toast.LENGTH_LONG).show()
                isJoin = false
            }
            if(password.isEmpty()) {
                Toast.makeText(this,"패스워드를 입력해주세요",Toast.LENGTH_LONG).show()
                isJoin = false
            }
            if(passwordck.isEmpty()) {
                Toast.makeText(this,"패스워드 체크를 입력해주세요",Toast.LENGTH_LONG).show()
                isJoin = false
            }
            //비밀번호가 동일한지 확인
            if(!password.equals(passwordck)) {
                Toast.makeText(this,"같은 비밀번호를 입력해주세요",Toast.LENGTH_LONG).show()
                isJoin = false
            }
            //비밀번호 자리
            if(password.length < 7) {
                Toast.makeText(this,"비밀번호를 7자리 이상으로 입력해주세요",Toast.LENGTH_LONG).show()
                isJoin = false
            }
            if(isJoin)
            {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this,"회원가입에 성공하셨습니다.", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                        } else {
                            Toast.makeText(this,"실패", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}