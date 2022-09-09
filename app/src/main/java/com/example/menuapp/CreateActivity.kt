package com.example.menuapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.isDigitsOnly
import com.example.menuapp.Api.ApiRetrofit
import com.example.menuapp.Model.ResponseModel
import com.example.menuapp.databinding.ActivityCreateBinding
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class CreateActivity : AppCompatActivity() {
    lateinit var binding: ActivityCreateBinding
    private val api by lazy { ApiRetrofit().apiEndPoint }
    private var selectedImage: Uri? = null
    private var body: MultipartBody.Part? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Create Menu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun setupListener(){
        binding.btnCreate.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if(binding.nameMenu.text.isEmpty() || binding.priceMenu.text.isEmpty() || binding.carboMenu.text.isEmpty() || binding.proteinMenu.text.isEmpty()) {
                Helper.message("All field must be filled", this, false)
            }
            else if (!binding.priceMenu.text.isDigitsOnly() || !binding.carboMenu.text.isDigitsOnly() || !binding.proteinMenu.text.isDigitsOnly()) {
                Helper.message("Price, Carbo, and Protein format must be number", this, false)
            }
            else {
                uploadImage()
            }
            binding.progressBar.visibility = View.GONE
        }
        binding.uploadImage.setOnClickListener {
            openImageLauncher.launch("image/*")
        }
    }

    private fun uploadImage() {
        if (selectedImage != null) {
            val file = File(cacheDir, contentResolver.getFileName(selectedImage!!))
            body = MultipartBody.Part.createFormData("photo", file.name, file.asRequestBody("multipart/form-data".toMediaTypeOrNull()))
        }
        binding.progressImage.visibility = View.VISIBLE
        api.createMenu(
            binding.nameMenu.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
            binding.priceMenu.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
            binding.carboMenu.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
            binding.proteinMenu.text.toString().toRequestBody("multipart/form-data".toMediaTypeOrNull()),
            body
        ).enqueue(object :
            Callback<ResponseModel> {
            override fun onResponse(
                call: Call<ResponseModel>,
                response: Response<ResponseModel>
            ) {
                if(!response.isSuccessful){
                    val errors = JSONObject(response.errorBody()!!.string())
                    Toast.makeText(baseContext, errors.getString("errors"), Toast.LENGTH_SHORT).show()
                } else {
                    Helper.message(response.body()!!.message, this@CreateActivity, true)
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Log.e("onFailure", t.toString())
            }
        })
        binding.progressImage.visibility = View.GONE
    }

    private val openImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ result ->
        selectedImage = result
        binding.imageMenu.setImageURI(selectedImage)
    }
}