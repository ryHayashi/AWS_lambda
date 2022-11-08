package com.example.base64_practice2


import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.system.Os.close
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
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
import java.util.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    var sImage:String=""
    var getImage:String=""


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
            val bytes : ByteArray = Base64.getDecoder().decode(getImage.toByteArray())
            val bitmap:Bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.size)
            binding.imageView.setImageBitmap(bitmap)

        }

        //送信
        binding.insertButton.setOnClickListener {
            val res: List<DataClass> = lambdaConnect(RequestClass("insert", "10", "androidTest", 1.2, 12.3, sImage))
            binding.textView.setText("insert")
        }

        binding.getButton.setOnClickListener {
            val res: List<DataClass> = lambdaConnect(RequestClass("selectid", "10"))
            val type = object : TypeToken<List<DataClass>>() {}.type
            val result: List<DataClass> = Gson().fromJson(res.toString(), type)
            if(!result.isNullOrEmpty()) {
                getImage = result[0].getImage()
                getImage = getImage.replace("b'", "").replace("'", "").replace("$","/").replace("%", "=")
            }
            binding.textView.setText("get")
        }

    }
    val REQUEST_GALLERY = 0

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
        startActivityForResult(intent, REQUEST_GALLERY)

    }


    //選択した画像をImageViewに貼り付け(アルバムが閉じたとき)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            try {
                val inn: InputStream? = data?.getData().let { data?.getData()
                    ?.let { it1 -> contentResolver.openInputStream(it1) } }

                val img: Bitmap = BitmapFactory.decodeStream(inn)

              //  binding.imageView.setImageBitmap(img)

                val stream = ByteArrayOutputStream()

                img.compress(Bitmap.CompressFormat.JPEG, 100, stream)

                val byte: ByteArray = stream.toByteArray()

                sImage =Base64.getEncoder().encodeToString(byte)

                //binding.textView.setText(sImage)
                binding.textView.setText("ok")
                inn?.close()

            } catch (e: Exception) {
                binding.textView.setText("e")
            }
        }
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


    //
    private fun lambdaConnect(request: RequestClass): List<DataClass>{
        val cognitoProvider = CognitoCachingCredentialsProvider(
            this.applicationContext, "us-east-2:53468dae-4554-493a-b810-44cd7e534341", Regions.US_EAST_2
        )

        val factory = LambdaInvokerFactory.builder()
            .context(this.applicationContext)
            .region(Regions.US_EAST_2)
            .credentialsProvider(cognitoProvider)
            .build()



        val myInterface = factory.build(MyInterface::class.java)


        val result: List<DataClass> = runBlocking {
            CoroutineScope(Dispatchers.Default).async{
                task(myInterface, request)
            }.await()
        }

        return result

    }

    private fun task(myInterface: MyInterface, request: RequestClass): List<DataClass> {
        try {
            return myInterface.ctx19team8_toEC2s3(request)
        } catch (lfe: LambdaFunctionException) {
            return listOf<DataClass>()
        }
    }


    //

}

