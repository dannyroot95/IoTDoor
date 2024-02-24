package com.aukde.iotdoor.UI

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukde.iotdoor.BaseActivity
import com.aukde.iotdoor.Models.HistoryModel
import com.aukde.iotdoor.Providers.UserProvider
import com.aukde.iotdoor.Adapters.RecordAdapter
import com.aukde.iotdoor.databinding.ActivityRecordBinding

class Record : BaseActivity() {

    private lateinit var binding : ActivityRecordBinding
    val mAuth : UserProvider = UserProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getRecords()
    }

    private fun getRecords(){
        val date = System.currentTimeMillis().toString()
        mAuth.getHistoryUsers(this,date)
    }

    fun successHistory(list : ArrayList<HistoryModel>){
        if (list.size > 0){
            binding.rvRecords.layoutManager = LinearLayoutManager(this)
            binding.rvRecords.setHasFixedSize(true)
            val recordAdapter = RecordAdapter(list)
            binding.rvRecords.adapter = recordAdapter
            binding.progressCircular.visibility = View.GONE
            binding.rvRecords.visibility = View.VISIBLE
        }
        else{
            binding.notFound.visibility = View.VISIBLE
            binding.rvRecords.visibility = View.GONE
            binding.progressCircular.visibility = View.GONE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    override fun onRestart() {
        super.onRestart()
        getRecords()
    }

}