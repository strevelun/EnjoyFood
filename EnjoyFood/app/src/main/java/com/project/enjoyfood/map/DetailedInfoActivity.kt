package com.project.enjoyfood.map

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.project.enjoyfood.R
import com.project.enjoyfood.databinding.ActivityDetailedInfoBinding
import com.project.enjoyfood.model.SearchResultEntity

class DetailedInfoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailedInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("DetailedInfoActivity", "onCreate: ")


        val entity = intent.getParcelableExtra<SearchResultEntity>("SearchResultEntity")!!
        Toast.makeText(applicationContext, entity?.name, Toast.LENGTH_SHORT).show()

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        toolbar.title = entity?.name
        setSupportActionBar(toolbar)

        if(entity.imageUri.isNotEmpty()){

            // 이미지 로딩 빨리되게 하고 싶으면 원본 이미지 크기를 줄이자
            Glide.with(binding.root)
                .load(entity.imageUri)
                .override(300, 300)
                .fitCenter()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ivRestaurantImage)
        } else {
            binding.ivRestaurantImage.setImageResource(R.drawable.food_splash)
        }
    }
}