package com.project.enjoyfood.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.project.enjoyfood.R
import com.project.enjoyfood.databinding.ActivityMapBinding
import com.project.enjoyfood.map.utility.RetrofitUtil
import com.project.enjoyfood.model.LocationLatLngEntity
import com.project.enjoyfood.model.SearchResultEntity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MapActivity : AppCompatActivity() , OnMapReadyCallback, CoroutineScope {

    companion object {
        const val SEARCH_RESULT_EXTRA_KEY = "SEARCH_RESULT_EXTRA_KEY"
        const val CAMERA_ZOOM_LEVEL = 17f
        const val PERMISSION_REQUEST_CODE = 101
    }

    private lateinit var markerList : ArrayList<SearchResultEntity>

    private lateinit var job: Job // Job : 코루틴의 상태확인 및 제어

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var map : GoogleMap
    private var currentSelectMarker: Marker? = null

    private lateinit var searchResult: SearchResultEntity

    private lateinit var binding : ActivityMapBinding
    private lateinit var progressBar : ProgressBar

    private lateinit var curLocationDong : String
    private lateinit var curLatLngEntity : LocationLatLngEntity

    private lateinit var locationManager: LocationManager
    private lateinit var myLocationListener: MyLocationListener
    private var curPos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBarLoadingMap

        job = Job()

        if(::searchResult.isInitialized.not()) {
            intent?.let {
                curLocationDong = it.getStringExtra(SEARCH_RESULT_EXTRA_KEY)!!
                curLatLngEntity = it.getParcelableExtra("curLatLng")!!
                curPos = it.getIntExtra("curPos", 0)

                Toast.makeText(this, "현재 설정된 위치 : " + curLocationDong, Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "현재 좌표 : " + curLatLngEntity.latitude + "\n" + curLatLngEntity.longitude, Toast.LENGTH_SHORT).show()

                markerList = it.getSerializableExtra("Marker List") as ArrayList<SearchResultEntity>
                setupGoogleMap()
            }
        }
        getMyLocation()

        // 현재 리사이클러뷰에 띄운 식당 전부 마커로 찝기

    }

    // TODO 함수 수정 및 이름 바꿀것
    private fun getMyLocation() {
        if(::locationManager.isInitialized.not()){
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if(isGPSEnabled) {
            if(ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                // curPos가 0일때만
                setMyLocationListener()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMyLocationListener() {
        val minTime = 1500L // 받아오는데 걸리는 최소 시간 1.5초
        val minDistance = 100f

        if(::myLocationListener.isInitialized.not())
        {
            myLocationListener = MyLocationListener()
        }

        with(locationManager) {
            requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime, minDistance, myLocationListener
            )
            requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime, minDistance, myLocationListener
            )
        }
    }
    // 위치가 바뀌면 사용자 위치정보를 계속 요청하는 리스너 삭제
    private fun removeLocationListener() {
        if(::locationManager.isInitialized && ::myLocationListener.isInitialized){
            locationManager.removeUpdates(myLocationListener)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                setMyLocationListener()
            } else {
                Toast.makeText(this, "권한 못 받음", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.map = map
    }

    // import com.google.android.gms.location.LocationListener 하지 말것
    inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {

            progressBar.visibility = View.INVISIBLE

            if(curPos != 0) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(curLatLngEntity.latitude.toDouble(), curLatLngEntity.longitude.toDouble()), CAMERA_ZOOM_LEVEL))

                for(i in 0..markerList.size-1){
                    map.addMarker(MarkerOptions().apply {
                        position(LatLng(markerList[i].latLngEntity.latitude.toDouble(), markerList[i].latLngEntity.longitude.toDouble()))
                        title(markerList[i].name)
                    })
                }

                (map.addMarker(MarkerOptions().apply {
                    position(LatLng(curLatLngEntity.latitude.toDouble(), curLatLngEntity.longitude.toDouble()))
                    title(curLocationDong)
                })).showInfoWindow()

                return
            }

            val locationLatLngEntity = LocationLatLngEntity(
                location.latitude.toFloat(),
                location.longitude.toFloat()
            )
                onCurrentLocationChanged(locationLatLngEntity)
        }
    }

    // 오래걸림
    private fun onCurrentLocationChanged(locationLngLatEntity: LocationLatLngEntity){
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    curLatLngEntity.latitude.toDouble(),
                    curLatLngEntity.longitude.toDouble(),
                ), CAMERA_ZOOM_LEVEL
            ))
        loadReverseGeoInfo(locationLngLatEntity)
        removeLocationListener()
    }


    private fun loadReverseGeoInfo(locationLngLatEntity: LocationLatLngEntity){

        launch(coroutineContext){
            try {
                withContext(Dispatchers.IO) {
                    val response = RetrofitUtil.apiService.getReverseGeoCode(
                        lat = locationLngLatEntity.latitude.toDouble(),
                        lon = locationLngLatEntity.longitude.toDouble()
                    )

                    if(response.isSuccessful) {
                        val body = response.body()

                        withContext(Dispatchers.Main) {

                            body?.let {
                                currentSelectMarker = setupMarker(
                                    locationLngLatEntity.latitude.toDouble(), locationLngLatEntity.longitude.toDouble()
                                )
                                currentSelectMarker?.showInfoWindow()
                            }


                            Log.e("list", body?.addressInfo?.middleBizName.toString())
                            Log.e("list", body?.addressInfo?.roadCode.toString())
                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MapActivity, "검색하는 과정에서 에러가 발생했습니다. : ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun setupMarker(lat : Double, lng : Double): Marker {

        val positionLatLng = LatLng (
            lat, lng
        )

            val markerOptions = MarkerOptions().apply {
                position(positionLatLng)
                title("내 위치")
            }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(positionLatLng, CAMERA_ZOOM_LEVEL))

        for(i in 0..markerList.size-1){
            map.addMarker(MarkerOptions().apply {
                position(LatLng(markerList[i].latLngEntity.latitude.toDouble(), markerList[i].latLngEntity.longitude.toDouble()))
                title(markerList[i].name)
            })
        }

        // 사용자 위치 마커만 리턴
        return map.addMarker(markerOptions)
    }
}