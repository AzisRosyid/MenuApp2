package com.example.menuapp.cart

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.menuapp.DB.CartDao
import com.example.menuapp.DB.Cart

@Database(entities = [Cart::class], version = 1)
abstract class CartDB: RoomDatabase() {

    abstract fun cartDao(): CartDao

    companion object {

        @Volatile private var instance: CartDB? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance?: synchronized(lock){
            instance?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, CartDB::class.java, "Cart.db"
        ).build()

    }

}