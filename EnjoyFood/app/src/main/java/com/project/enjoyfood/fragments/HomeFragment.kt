package com.project.enjoyfood.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.project.enjoyfood.R
import com.project.enjoyfood.SearchAdapter
import com.project.enjoyfood.databinding.FragmentHomeBinding
import com.project.enjoyfood.databinding.FragmentSearchBinding
import com.project.enjoyfood.firebase.Ref.Companion.restaurantRef
import com.project.enjoyfood.map.DetailedInfoActivity
import com.project.enjoyfood.map.MapActivity
import com.project.enjoyfood.map.MapActivity.Companion.PERMISSION_REQUEST_CODE
import com.project.enjoyfood.map.MapActivity.Companion.SEARCH_RESULT_EXTRA_KEY
import com.project.enjoyfood.map.utility.RetrofitUtil
import com.project.enjoyfood.model.LocationLatLngEntity
import com.project.enjoyfood.model.SearchResultEntity
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var btn_search: Button
    private lateinit var btn_changeLocation: Button
    private lateinit var et_search : EditText
    private lateinit var fbtn_curLocation : FloatingActionButton

    private lateinit var btn_sortByLike : Button
    private lateinit var btn_sortByReview : Button
    private lateinit var btn_clear : Button

    private lateinit var adapter: SearchAdapter

    private lateinit var job: Job // Job : 코루틴의 상태확인 및 제어
    private lateinit var uiScope: CoroutineScope

    private lateinit var curLocationDong : String
    private lateinit var curLatLngEntity : LocationLatLngEntity

    private lateinit var locationManager: LocationManager

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var spinner: Spinner

    // 현재 다이얼로그 스피너에서 선택된 아이템 저장해서 다이얼로그 열 때마다 그 위치로 표시
    private var curPos = 0


    override fun onAttach(context: Context) {
        super.onAttach(context)

        job = Job()

        uiScope = CoroutineScope(Dispatchers.Main + job)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        adapter = SearchAdapter()

        val arr: ArrayList<SearchResultEntity> = arrayListOf<SearchResultEntity>()
        adapter.setSearchResultList(arr) {
            startActivity(Intent(context, DetailedInfoActivity::class.java).apply {
                putExtra("SearchResultEntity", it)
            })
        }
    }

    // 플레이어의 동과 같은 동을 파이어베이스에서 검색하여 처음에 전부 뿌려준다.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.searchRecyclerView.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.searchRecyclerView.adapter = adapter

        curPos = 0

        getMyLocation()

        setupLayout()

        return binding.root
    }

    private fun getMyLocation() {

        if(::locationManager.isInitialized.not()){
            locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(isGPSEnabled) {
            if(ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // 권한 받아온 후 현재 위치 받아오기
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->

                        uiScope.launch{
                            try {
                                withContext(Dispatchers.IO) {
                                    val response = RetrofitUtil.apiService.getReverseGeoCode(
                                        lat = location!!.latitude,
                                        lon = location!!.longitude
                                    )

                                    if(response.isSuccessful) {
                                        val body = response.body()

                                        withContext(Dispatchers.Main) {
                                            body?.let {
                                                Toast.makeText(requireContext(), "현재위치: " + body.addressInfo.legalDong, Toast.LENGTH_SHORT).show()
                                                curLocationDong = body.addressInfo.legalDong.toString()
                                                curLatLngEntity = LocationLatLngEntity(location!!.latitude.toFloat(), location!!.longitude.toFloat())
                                                // 프래그먼트 처음 시작시 기본적으로 전부 출력됨
                                                searchKeyword(et_search.text.toString())

                                            // TODO 삭제금지
                                                /*
                                            restaurantRef.child(curLocationDong).child("몽짬뽕 영대직영점").setValue(SearchResultEntity("", "몽짬뽕 영대직영점",
                                                curLocationDong,
                                                "영대근처에 있음", 1, 0, LocationLatLngEntity(35.84297945564072f, 128.7496319476774f)
                                            ))

                                            restaurantRef.child(curLocationDong).child("한글분식").setValue(SearchResultEntity("", "한글분식",
                                                curLocationDong,
                                                "한1글", 2, 0, LocationLatLngEntity(35.84399806408521f, 128.74876616053132f)
                                            ))

                                            restaurantRef.child(curLocationDong).child("원미분식").setValue(SearchResultEntity("", "원미분식",
                                                curLocationDong,
                                                "원미야~", 3, 0, LocationLatLngEntity(35.84435159545977f, 128.75047361803823f)
                                            ))

                                            restaurantRef.child("조영동").child("이모분식").setValue(SearchResultEntity("", "이모분식",
                                                curLocationDong,
                                                "이모~", 3, 0, LocationLatLngEntity(35.83923994725066f, 128.75560406360393f)
                                            ))

                                                 */
                                                /*
                                                restaurantRef.child("조영동").child("원남마설쌀국수").setValue(SearchResultEntity("", "원남마설쌀국수",
                                                    curLocationDong,
                                                    "원남아~", 333, 0, LocationLatLngEntity(35.841498213580024f, 128.75504444793654f)
                                                ))

                                                restaurantRef.child(curLocationDong).child("동인동김치찜").setValue(SearchResultEntity("", "동인동김치찜",
                                                    curLocationDong,
                                                    "인동인동인~", 13, 0, LocationLatLngEntity(35.84179959542258f, 128.74916902090158f)
                                                ))

                                                restaurantRef.child(curLocationDong).child("장터막창").setValue(SearchResultEntity("", "장터막창",
                                                    curLocationDong,
                                                    "장터막장~", 33, 0, LocationLatLngEntity(35.84235291250498f, 128.75044159366746f)
                                                ))

                                                restaurantRef.child(curLocationDong).child("바다포차").setValue(SearchResultEntity("https://www.google.com/maps/place/%EB%B0%94%EB%8B%A4%ED%8F%AC%EC%B0%A8/@35.841857,128.7524207,3a,75y,90t/data=!3m8!1e2!3m6!1sAF1QipNouZBN2akejSlS99A2JiW7t9M8_dWjpECej7qq!2e10!3e12!6shttps:%2F%2Flh5.googleusercontent.com%2Fp%2FAF1QipNouZBN2akejSlS99A2JiW7t9M8_dWjpECej7qq%3Dw86-h114-k-no!7i3024!8i4032!4m9!1m2!2m1!1z7J6E64u564-ZIOyLneuLuQ!3m5!1s0x35660c6c9d16976f:0xeb7e7eb336d0d08e!8m2!3d35.841857!4d128.7524207!15sChDsnoTri7nrj5kg7Iud64u5WhIiEOyehOuLueuPmSDsi53ri7mSAQpyZXN0YXVyYW50#",
                                                    "바다포차",
                                                    curLocationDong,
                                                    "바바다포차바다포바다포차바다포차차다바다포차바바다바다포차포바다포차차다포차바바다포차다바다포차포차포차바다바다포차포바바다포차다포바다포차차바다포차차~", 3, 0, LocationLatLngEntity(35.84185847278307f, 128.75242697316258f)
                                                ))

                                                restaurantRef.child(curLocationDong).child("보스찜닭").setValue(SearchResultEntity("", "보스찜닭",
                                                    curLocationDong,
                                                    "보스~", 500, 0, LocationLatLngEntity(35.84320570643505f, 128.74847672673434f)
                                                ))

                                                restaurantRef.child(curLocationDong).child("돈짬뽕").setValue(SearchResultEntity("", "돈짬뽕",
                                                    curLocationDong,
                                                    "돈없으면 오지마세요", 23, 0, LocationLatLngEntity(35.84233716838896f, 128.75064978711475f)
                                                ))

                                                 */
                                            }
                                        }
                                    }
                                }
                            } catch(e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "검색하는 과정에서 에러가 발생했습니다. : ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }

    // 검색버튼 누르는 순간, 입력했던 검색어를 검색한다.
    // 현재 위치(동) 하위에 등록된 식당이름과 하나씩 비교해가면서
    // 일치하면 그 식당의 하위 정보를 전부 가져와서 객체를 생성한 후
    // adapter.add()에 넘겨준다.
    private fun searchKeyword(strKeyword: String) {


        uiScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = restaurantRef.child(curLocationDong)

                    val postListener: ValueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            // 기존에 있던거 싹 다 날리기

                            adapter.clear()

                            var visible = true


                            // 검색기능은 사용자가 입력한 검색단어가 포한된 모든 문자열이 true
                            for (data in dataSnapshot.children) {
                                val result = data.getValue(SearchResultEntity::class.java)!!

                                //info = data.getValue(MyInfo::class.java)

                                if(strKeyword.matches(("[" + result.name + "]*").toRegex()))
                                {
                                    // 좌표까지 전달하는데 MapActivity에서 마커를 찍는 용도로 사용될 것
                                    adapter.addItem(SearchResultEntity(result.imageUri, result.name, result.addrDong, result.desc, result.numOfHearts, result.numOfReviews,
                                        LocationLatLngEntity(result.latLngEntity.latitude, result.latLngEntity.longitude)
                                    ))

                                    visible = false
                                }
                            }

                            if(!visible || strKeyword == "")
                                binding.root.tv_tung.visibility = View.INVISIBLE
                            else
                                binding.root.tv_tung.visibility = View.VISIBLE

                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.e("searchFragment", "loadPost:onCancelled", databaseError.toException());
                        }
                    }
                    response.addValueEventListener(postListener)
                }
            } catch(e: Exception) {
                e.printStackTrace()
                Toast.makeText(context,"에러발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLayout() {

        btn_search = binding.btnSearch
        btn_changeLocation = binding.btnChangeLocation
        et_search = binding.etSearch
        fbtn_curLocation = binding.fbtnCurrentLocation
        btn_sortByLike = binding.sortByLike
        btn_sortByReview = binding.sortByReview
        btn_clear = binding.btnClear

        btn_clear.setOnClickListener {
            searchKeyword("")
        }

        btn_sortByLike.setOnClickListener {
            // 어댑터에 있는 배열 순서를 바꾸고 notify
            adapter.sortByLike()
        }

        btn_sortByReview.setOnClickListener {
            adapter.sortByReview()
        }

        btn_search.setOnClickListener {
            val searchStr = et_search.text.toString()

            if(searchStr.length >= 2)
                searchKeyword(searchStr.trim())
            else
                Toast.makeText(requireContext(), "두 글자 이상 입력하세요!!!!", Toast.LENGTH_SHORT).show()
        }

        // getMyLocation()에서 curLocationDong이 초기화 되지 않으면 에러
        btn_changeLocation.setOnClickListener {

            // 위치변경
            // 각 동마다 중심좌표가 미리 설정되어 있다.
            // 이 버튼을 누르고 동을 선택하면 각 동의 중심지로 마커 카메라 이동.
            // 현재 위치 버튼을 누르면 다시 예전처럼 자기위치 받아서

            changeLocation()

        }

        // curPos가 0이 아니면 정해진 좌표에 마커를 찍는다.
        fbtn_curLocation.setOnClickListener {
            if(ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {

                startActivity(Intent(context, MapActivity::class.java).apply {
                    putExtra(SEARCH_RESULT_EXTRA_KEY, curLocationDong)
                    putParcelableArrayListExtra("Marker List", adapter.getList())
                    putExtra("curPos", curPos)
                    putExtra("curLatLng", curLatLngEntity)

                })
            }
        }
    }

    private fun changeLocation(){
        val items = resources.getStringArray(R.array.locations)
        val myAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        val dialogView = layoutInflater.inflate(R.layout.dialog_spinner_change_location, null)

        spinner = dialogView.findViewById(R.id.spinner_change_location)
        spinner.adapter = myAdapter
        spinner.setSelection(curPos)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                // '현재위치'를 선택하는 경우 실시간으로 현재 사용자 좌표를 받아온다.
                // 그렇지 않으면 미리 지정된 동 좌표를 저장

                when(position) {
                    0   ->  {
                        Toast.makeText(requireContext(), "지금부터 위치추적을 시작합니다", Toast.LENGTH_SHORT).show()
                    }
                    1   ->  {
                        curLocationDong = "임당동"
                        curLatLngEntity = LocationLatLngEntity(35.84352102309237f, 128.74580066699644f)
                    }
                    2 -> {
                        curLocationDong = "조영동"
                        curLatLngEntity = LocationLatLngEntity(35.84194924688018f, 128.7547242297819f)
                    }

                    else -> {

                    }
                }

                if(curPos != position) { // 장소를 바꿨다면
                    curPos = position

                    if(curPos == 0) {
                        getMyLocation()
                    } else {

                        Toast.makeText(
                            requireContext(),
                            "현재 " + curPos + "번 주소(동) 적용된 상태",
                            Toast.LENGTH_SHORT
                        ).show()
                        searchKeyword(et_search.text.toString())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.show()

        val btn_cancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        btn_cancel.setOnClickListener { alertDialog.dismiss() }
    }
}