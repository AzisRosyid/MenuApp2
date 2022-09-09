package com.example.menuapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.example.menuapp.Api.ApiRetrofit
import com.example.menuapp.Model.UserModel
import com.example.menuapp.databinding.ActivityLoginBinding
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val api by lazy { ApiRetrofit().apiEndPoint }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Login Menu"
        setupListener()

        binding.emailUser.setText("admin@gmail.com")
        binding.passwordUser.setText("12345678")
    }

    private fun setupListener(){
        binding.btnLogin.setOnClickListener{
            if(binding.emailUser.text.isNullOrEmpty() || binding.passwordUser.text.isNullOrEmpty()){
                Helper.message("All field must be filled", this, false)
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailUser.text).matches()){
                Helper.message("Email format must be email", this, false)
            } else {
                api.login(
                    binding.emailUser.text.toString(),
                    binding.passwordUser.text.toString()
                ).enqueue(object: Callback<UserModel> {
                    override fun onResponse(call: Call<UserModel>, response: Response<UserModel>) {
                        if(!response.isSuccessful){
                            val errors = JSONObject(response.errorBody()!!.string())
                            Helper.message(errors.getString("errors"), this@LoginActivity, false)
                        } else {
                            Helper.id = response.body()!!.users.id
                            Helper.name = response.body()!!.users.name
                            Helper.email = response.body()!!.users.email
                            Helper.password = binding.passwordUser.text.toString()
                            Helper.level = response.body()!!.users.level
                            if(Helper.level == "admin"){
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            }else if (Helper.level == "user"){
                                startActivity(Intent(applicationContext, UserActivity::class.java))
                                finish()
                            }
                        }
                    }

                    override fun onFailure(call: Call<UserModel>, t: Throwable) {
                        Log.e("onFailure", t.message.toString())
                    }
                })
            }
        }
    }
}