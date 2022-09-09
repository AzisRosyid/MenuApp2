package com.example.menuapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.example.menuapp.Api.ApiRetrofit
import com.example.menuapp.Model.Menu
import com.example.menuapp.DB.Cart
import com.example.menuapp.cart.CartDB
import com.example.menuapp.databinding.ActivityDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.*

class DetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailBinding
    private val menu by lazy { intent.getSerializableExtra("menu") as Menu }
    private val db by lazy { CartDB(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Detail Menu"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                Helper.cart.clear()
                Helper.cart.addAll(db.cartDao().getCart())
            }
        }
        binding.nameMenu.setText(menu.name)
        binding.priceMenu.setText(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(menu.price))
        binding.carboMenu.setText(menu.carbo.toString())
        binding.proteinMenu.setText(menu.protein.toString())
        Glide.with(this)
            .load(Helper.BASE_IMAGE + menu.photo)
            .centerCrop()
            .error(R.drawable.ic_baseline_restaurant_menu_24)
            .into(binding.photoMenu)
    }

    private fun valid(): Boolean{
        for(i in Helper.cart){
            if(i.menu_id == menu.id){
                CoroutineScope(Dispatchers.IO).launch {
                    db.cartDao().updateCart(
                        Cart(
                        i.menu_id,
                        i.name,
                        i.price,
                        i.carbo,
                        i.protein,
                        i.qty + 1,
                        i.photo,
                        i.user_id,
                        i.id
                    )
                    )
                }
                return false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.cart -> {
                startActivity(Intent(applicationContext, CartActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu!!.findItem(R.id.search)
        searchItem.isVisible = false
        val logoutItem = menu!!.findItem(R.id.logout)
        logoutItem.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupListener(){
        binding.btnAdd.setOnClickListener {
            if(valid()){
                CoroutineScope(Dispatchers.IO).launch {
                    db.cartDao().createCart(
                        Cart(
                        menu.id,
                        menu.name,
                        menu.price,
                        menu.carbo,
                        menu.protein,
                        1,
                        menu.photo,
                        Helper.id,
                    )
                    )
                }
            }
            Helper.message("Menu successfully added to cart", this, true)
        }
    }
}