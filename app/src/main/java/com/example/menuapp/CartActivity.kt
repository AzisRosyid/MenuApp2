package com.example.menuapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import com.example.menuapp.Api.ApiRetrofit
import com.example.coba_app.CartAdapter
import com.example.menuapp.DB.Cart
import com.example.menuapp.Model.ResponseModel
import com.example.menuapp.cart.CartDB
import com.example.menuapp.databinding.ActivityCartBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class CartActivity : AppCompatActivity() {
    lateinit var binding: ActivityCartBinding
    private val api by lazy { ApiRetrofit().apiEndPoint }
    private val db by lazy { CartDB(this) }
    lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Cart List"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setupList()
        setupListener()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        getCart()
    }

    private fun setupList(){
        cartAdapter = CartAdapter(arrayListOf(), object: CartAdapter.onSetupListener{
            override fun onTextChanged(cart: Cart) {
                CoroutineScope(Dispatchers.IO).launch {
                    db.cartDao().updateCart(cart)
                }
                Helper.total = Helper.cart.sumOf { it.qty * it.price }.toDouble()
                binding.totalMenu.setText(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(Helper.total))
            }

            override fun onDelete(cart: Cart) {
                alertDelete(cart)
            }
        })
        binding.recyclerView.apply {
            adapter = cartAdapter
        }
    }

    private fun setupListener(){
        binding.btnCheckout.setOnClickListener {
            if(Helper.cart.isEmpty()){
                Helper.message("Insert menu on cart first", this, false)
            } else {
                binding.progressBar.visibility = View.VISIBLE
                api.orderHeader(Helper.id).enqueue(object: Callback<ResponseModel> {
                    override fun onResponse(
                        call: Call<ResponseModel>,
                        response: Response<ResponseModel>
                    ) {
                        val headerId = response.body()!!.message.toInt()
                        for(i in Helper.cart){
                            api.orderDetail(headerId, i.menu_id, i.qty).enqueue(object:
                                Callback<ResponseModel> {
                                override fun onResponse(
                                    call: Call<ResponseModel>,
                                    response: Response<ResponseModel>
                                ) {

                                }

                                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                                    Log.e("onFailure", t.message.toString())
                                }
                            })
                            CoroutineScope(Dispatchers.IO).launch {
                                db.cartDao().deleteCart(i)
                            }
                        }
                        binding.progressBar.visibility = View.GONE
                        Helper.message("Menu successfully ordered!", this@CartActivity, false)
                        Helper.cart.clear()
                        onStart()
                    }
                    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                        Log.e("onFailure", t.message.toString())
                    }
                })
            }
        }
    }

    private fun alertDelete(cart: Cart){
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Konfirmation?")
            .setMessage("Are you sure delete this menu?")
            .setPositiveButton("Yes"){dialog, which ->
                binding.progressBar.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    db.cartDao().deleteCart(cart)
                }
                Helper.message("Menu successfully deleted", this, false)
                onStart()
                binding.progressBar.visibility = View.GONE
            }
            .setNegativeButton("No"){dialog, which->onStart()}
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val searchItem = menu!!.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                binding.progressBar.visibility = View.VISIBLE
                val search: List<Cart> = Helper.cart.filter { s ->
                    s.name.contains(p0.toString()) || s.price.toString().contains(p0.toString()) || s.qty.toString().contains(p0.toString())
                }
                cartAdapter.setData(search)
                binding.progressBar.visibility = View.GONE
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
        searchView.setOnCloseListener(object: SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                onStart()
                return false
            }
        })
        val logoutItem = menu!!.findItem(R.id.logout)
        logoutItem.isVisible = false
        val cartItem = menu!!.findItem(R.id.cart)
        cartItem.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }

    private fun getCart(){
        binding.progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                Helper.cart.clear()
                Helper.cart.addAll(db.cartDao().getCart())
                cartAdapter.setData(Helper.cart)
                Helper.total = Helper.cart.sumOf { it.price * it.qty }.toDouble()
                binding.totalMenu.setText(NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(Helper.total))
            }
        }
        binding.progressBar.visibility = View.GONE
    }
}