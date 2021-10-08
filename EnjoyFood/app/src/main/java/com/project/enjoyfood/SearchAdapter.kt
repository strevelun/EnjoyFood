package com.project.enjoyfood

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.project.enjoyfood.databinding.ItemSearchResultBinding
import com.project.enjoyfood.model.SearchResultEntity
import okhttp3.internal.notify

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.MyViewHolder>() {

    private var searchResultList: ArrayList<SearchResultEntity> = arrayListOf()
    private lateinit var searchResultClickListener: (SearchResultEntity) -> Unit

    //     buildFeatures {
    //        viewBinding true
    //    }
    class MyViewHolder(val binding: ItemSearchResultBinding, val searchResultClickListener: (SearchResultEntity) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(data: SearchResultEntity) = with(binding) {

            tvTitle.text = data.name
            tvAddress.text = data.addrDong
            tvHeartNum.text = data.numOfHearts.toString()
            tvReviewNum.text = data.numOfReviews.toString()
            tvDesc.text = data.desc

            if(data.imageUri.isNotEmpty()){
                
                // 이미지 로딩 빨리되게 하고 싶으면 원본 이미지 크기를 줄이자
                Glide.with(binding.root)
                    .load(data.imageUri)
                    .override(100, 100)
                    .fitCenter()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(ivMenuItemPic)
            } else {
                ivMenuItemPic.setImageResource(R.drawable.food_splash)
            }

        }

        fun bindViews(data: SearchResultEntity){
            binding.root.setOnClickListener {
                searchResultClickListener(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder{
        val view = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view, searchResultClickListener) // 어댑터 생성하면서 동시에 레이아웃에 정보 세팅
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(searchResultList[position])
        holder.bindViews(searchResultList[position])
    }

    fun sortByLike() {

        searchResultList.sortByDescending { it.numOfHearts }

        notifyDataSetChanged()
    }

    fun sortByReview() {

        searchResultList.sortByDescending { it.numOfReviews }

        notifyDataSetChanged()
    }

    fun clear() = searchResultList.clear()

    override fun getItemCount(): Int = searchResultList.size

    fun setSearchResultList(searchResultList: ArrayList<SearchResultEntity>, searchResultClickListener: (SearchResultEntity) -> Unit) {
        this.searchResultList = searchResultList
        this.searchResultClickListener = searchResultClickListener
        notifyDataSetChanged()
    }

    fun getList() : ArrayList<SearchResultEntity> {
        return searchResultList
    }

    fun addItem(item : SearchResultEntity) = searchResultList.add(item)
}