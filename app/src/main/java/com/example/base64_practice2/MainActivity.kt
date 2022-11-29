package com.example.base64_practice2

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory
import com.amazonaws.regions.Regions
import com.example.base64_practice2.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    var sImage:String=""
    var imageUrl = arrayOf<String>()

    //
    private lateinit var Launcher: ActivityResultLauncher<Intent>
    //
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //encode押した処理
        binding.encode.setOnClickListener {
            //パーミッションの許可
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                //許可が下りない時
                //再度許可を求める
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100
                )
            } else {
                //許可が下りた時
                //関数を作成
                selectImage()

            }

        }
        //decode押した処理
        binding.decode.setOnClickListener {

            //ここで取得したurlにアクセスし、s3から画像を取得
            val getImage = ""

            val bytes : ByteArray = Base64.decode(getImage,Base64.NO_WRAP)
            val bitmap:Bitmap = BitmapFactory.decodeByteArray(bytes!!,0,bytes.size)
            binding.imageView.setImageBitmap(bitmap)

        }

        //送信
        binding.insertButton.setOnClickListener {
            lambdaConnect(RequestClass("insert","androidTest", 1.2, 12.3, sImage))
            binding.textView.setText("insert")
        }

        //取得
        binding.getButton.setOnClickListener {
            try {
                val res: String = lambdaConnect(RequestClass("select"))
                val type = object : TypeToken<List<DataClass>>() {}.type
                val result: List<DataClass> = Gson().fromJson(res, type)
                imageUrl = imageUrl.filter {false}.toTypedArray()
                for(row in result) {
                    imageUrl.plus(row.getImage())
                    Log.d("log", row.getImage())
                }
                binding.textView.setText("get")
                binding.textView.setText(result[0].getLatitude().toString())
            }catch(e: Exception) {
                binding.textView.setText("get error")
            }
        }


        //選択した画像をImageViewに貼り付け(アルバムが閉じたとき)
        //onActivityResult非推奨の対応
        Launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            val resultCode: Int = result.resultCode
            val data: Intent? = result.data
            if (resultCode == RESULT_OK) {
                try {
                    val inn: InputStream? = data?.getData().let { data?.getData()
                        ?.let { it1 -> contentResolver.openInputStream(it1) } }
                    val img: Bitmap = BitmapFactory.decodeStream(inn)
                    val stream = ByteArrayOutputStream()
                    img.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    val byte: ByteArray = stream.toByteArray()
                    sImage = Base64.encodeToString(byte, Base64.NO_WRAP)

                    binding.textView.setText("ok")
                    inn?.close()

                } catch (e: Exception) {
                    binding.textView.setText("e")
                }
            }
        }
    }


    private fun selectImage() {
        // 以前のデータをクリアする
        binding.textView.setText("")
        binding.imageView.setImageBitmap(null)
        //インテントの初期化（インテントとは、他のアクティビティやアプリケーションなどと情報のやり取りを行うための箱のようなもの）
        val intent = Intent(Intent.ACTION_VIEW)
        //タイプ
        intent.setType("image/jpeg")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        //結果
        Launcher.launch(intent)

    }

    //パーミッションの状況
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //許可が下りた時
            //関数呼び出し
            selectImage()
        } else {
            Toast.makeText(getApplicationContext(), "アルバムへのアクセスを拒否", Toast.LENGTH_SHORT).show()
        }
    }


    //AWSへ接続
    private fun lambdaConnect(request: RequestClass): String{
        val cognitoProvider = CognitoCachingCredentialsProvider(
            this.applicationContext, "us-east-2:53468dae-4554-493a-b810-44cd7e534341", Regions.US_EAST_2
        )

        val factory = LambdaInvokerFactory.builder()
            .context(this.applicationContext)
            .region(Regions.US_EAST_2)
            .credentialsProvider(cognitoProvider)
            .build()

        val myInterface = factory.build(MyInterface::class.java)

        val result: String = runBlocking {
            CoroutineScope(Dispatchers.Default).async{
                task(myInterface, request)
            }.await()
        }
        return result

    }

    //AWSへの通信
    //戻り値はjson形式の文字列
    private fun task(myInterface: MyInterface, request: RequestClass): String{
        try {
            return myInterface.ctx19team8_toEC2s3(request)
        } catch (lfe: LambdaFunctionException) {
            return lfe.toString()
        }
    }

}

