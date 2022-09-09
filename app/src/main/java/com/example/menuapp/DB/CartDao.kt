package com.example.menuapp.DB
import androidx.room.*
import com.example.menuapp.Helper

@Dao
interface CartDao {

    @Insert
    suspend fun createCart(cart: Cart)

    @Update
    suspend fun updateCart(cart: Cart)

    @Delete
    suspend fun deleteCart(cart: Cart)

    @Query("SELECT * FROM cart WHERE user_id == :userid ORDER BY id DESC")
    suspend fun getCart(userid: Int = Helper.id): List<Cart>

}