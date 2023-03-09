//Ryan Feroz CS388 BitFit Part 1 Project 5
package com.example.bitfit_pt1

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    private lateinit var foodListAdapter: FoodListAdapter
    private lateinit var foodListRecyclerView: RecyclerView
    private lateinit var addFoodButton: Button
    private lateinit var noFoodItemsTextView: TextView
    private lateinit var averageCaloriesTextView: TextView

    private var foodItemList: MutableList<FoodItem> = mutableListOf()


    companion object {
        private const val ADD_FOOD_ITEM_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        foodListRecyclerView = findViewById(R.id.food_list_recycler_view)
        noFoodItemsTextView = findViewById(R.id.no_food_items_text_view)
        addFoodButton = findViewById(R.id.add_button)
        averageCaloriesTextView = findViewById(R.id.average_calories_text_view)

        foodItemList = loadFoodItems()
        foodListAdapter = FoodListAdapter(foodItemList)

        val layoutManager = LinearLayoutManager(this)
        foodListRecyclerView.layoutManager = layoutManager
        foodListRecyclerView.adapter = foodListAdapter

        addFoodButton.setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            startActivityForResult(intent, ADD_FOOD_ITEM_REQUEST_CODE)
        }

        updateFoodListVisibility()
        updateAverageCalories()
    }
    private fun updateFoodListVisibility() {
        if (foodItemList.isEmpty()) {
            noFoodItemsTextView.isVisible = true
            foodListRecyclerView.isVisible = false
        } else {
            noFoodItemsTextView.isVisible = false
            foodListRecyclerView.isVisible = true
        }
    }

    private fun updateAverageCalories() {
        val totalCalories = foodItemList.sumBy { it.calories }
        val averageCalories = if (foodItemList.isNotEmpty()) {
            totalCalories / foodItemList.size
        } else {
            0
        }
        averageCaloriesTextView.text = "Average Calories: $averageCalories"
    }


    private fun onFoodItemAdded(foodItem: FoodItem) {
        foodItemList.add(foodItem)
        foodListAdapter.notifyItemInserted(foodItemList.size - 1)
        updateFoodListVisibility()
        updateAverageCalories()
        saveFoodItems()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_FOOD_ITEM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.getParcelableExtra<FoodItem>(AddFoodActivity.EXTRA_FOOD_ITEM)?.let {
                onFoodItemAdded(it)
            }
        }
    }

    private fun loadFoodItems(): MutableList<FoodItem> {
        val sharedPreferences = getSharedPreferences("food_items", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("food_items", null)
        val type = object : TypeToken<MutableList<FoodItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun saveFoodItems() {
        val sharedPreferences = getSharedPreferences("food_items", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(foodListAdapter.getFoodItems())
        editor.putString("food_items", json)
        editor.apply()
    }

    inner class FoodListAdapter(private val foodItems: MutableList<FoodItem>) :
        RecyclerView.Adapter<FoodListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.food_item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(foodItems[position]) {
                holder.foodNameTextView.text = name
                holder.foodCaloriesTextView.text = calories.toString()
            }
        }

        override fun getItemCount(): Int {
            return foodItems.size
        }

        fun getFoodItems(): MutableList<FoodItem> {
            return foodItems
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val foodNameTextView: TextView = view.findViewById(R.id.food_name_text_view)
            val foodCaloriesTextView: TextView = view.findViewById(R.id.food_calories_text_view)
        }
    }


}
