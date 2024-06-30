package com.example.ethereum

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.EthGetTransactionCount
import org.web3j.protocol.core.methods.response.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var web3j: Web3j

    private lateinit var etAddress: EditText
    private lateinit var etTransactionHash: EditText

    private lateinit var tvBalance: TextView
    private lateinit var tvNonce: TextView
    private lateinit var tvTransactionDetails: TextView

    private lateinit var btnFetchBalance: Button
    private lateinit var btnFetchNonce: Button
    private lateinit var btnFetchTransactionDetails: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        etAddress = findViewById(R.id.etAddress)
        tvBalance = findViewById(R.id.tvBalance)
        tvNonce = findViewById(R.id.tvNonce)
        btnFetchBalance = findViewById(R.id.btnFetchBalance)
        btnFetchNonce = findViewById(R.id.btnFetchNonce)
        btnFetchTransactionDetails = findViewById(R.id.btnFetchTransactionDetails)
        tvTransactionDetails = findViewById(R.id.tvTransactionDetails)
        etTransactionHash = findViewById(R.id.etTransactionHash)
        val btnScanQR: Button = findViewById(R.id.btnScanQR)


        web3j = Web3j.build(HttpService("https://mainnet.infura.io/v3/3efc5d19980240c38622f3bbce1a883d"))


        btnFetchBalance.setOnClickListener {

            val address = etAddress.text.toString()

            fetchBalance(address)
        }

        btnFetchNonce.setOnClickListener {

            val address = etAddress.text.toString()

            getNonce(address)
        }

        btnFetchTransactionDetails.setOnClickListener {

            val transactionHash = etTransactionHash.text.toString()

            fetchTransactionDetails(transactionHash)
        }



        btnScanQR.setOnClickListener {

            IntentIntegrator(this).initiateScan()
        }
    }


    private fun fetchBalance(address: String) {

        thread {
            try {

                val ethGetBalance: EthGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send()

                val wei: BigInteger = ethGetBalance.balance

                val ether: BigDecimal = Convert.fromWei(wei.toString(), Convert.Unit.ETHER)


                runOnUiThread {
                    tvBalance.text = "Balance: $ether ETH"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun getNonce(address: String) {
        thread {

            try {

                val ethGetTransactionCount: EthGetTransactionCount = web3j.ethGetTransactionCount(
                    address, DefaultBlockParameterName.PENDING).send()


                val nonce:String = ethGetTransactionCount.transactionCount.toString()


                runOnUiThread {
                    tvNonce.text = "Nonce: $nonce Times"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    private fun fetchTransactionDetails(transactionHash: String) {
        thread {
            try {
                val transaction: Transaction = web3j.ethGetTransactionByHash(transactionHash).send().transaction.orElse(null)
                val transactionDetails : String = transaction.toString()
                runOnUiThread {
                    tvTransactionDetails.text = "Transacation Details: $transactionDetails"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {

                etAddress.setText(result.contents)
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
