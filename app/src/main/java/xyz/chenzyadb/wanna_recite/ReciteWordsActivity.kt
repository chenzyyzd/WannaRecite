package xyz.chenzyadb.wanna_recite

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apache.commons.io.FileUtils
import org.json.JSONObject
import xyz.chenzyadb.wanna_recite.ui.theme.WannaReciteTheme
import java.io.File
import java.util.*

class ReciteWordsActivity : ComponentActivity() {
    var appDataPath = ""

    var countDownTimer: Timer? = null
    var remainSeconds: Int by mutableStateOf(0)

    val showWordMeaning = mutableStateListOf<Boolean>()

    val word = mutableStateListOf<String>()
    val wordMeaning = mutableStateListOf<String>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        appDataPath = applicationContext.getFilesDir().getAbsolutePath()

        try {
            val jsonFile = File(appDataPath + "/today_words.json")
            val todayWordsJson = JSONObject(FileUtils.readFileToString(jsonFile))
            val wordJsonArray = todayWordsJson.getJSONArray("word")
            val wordMeaningJsonArray = todayWordsJson.getJSONArray("wordMeaning")
            val isWordRecitedJsonArray = todayWordsJson.getJSONArray("isWordRecited")

            for (idx in 0..wordJsonArray.length()) {
                if (!isWordRecitedJsonArray.getBoolean(idx)) {
                    word.add(wordJsonArray.getString(idx))
                    wordMeaning.add(wordMeaningJsonArray.getString(idx))
                    showWordMeaning.add(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (word.size == 0) {
            finish()
        }

        remainSeconds = (word.lastIndex + 1) * 10

        setContent {
            WannaReciteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            Column(
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp, end = 10.dp)
                                    .height(60.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "单词记忆",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "[${remainSeconds / 60}:${remainSeconds % 60}] Wasting time is robbing oneself.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = it.calculateTopPadding())
                                .fillMaxSize()
                                .padding(top = 20.dp, start = 10.dp, end = 10.dp, bottom = 10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            for (idx in 0..word.lastIndex) {
                                WordItem(idx)
                            }
                        }
                    }
                }
            }
        }

        countDownTimer = Timer()
        countDownTimer!!.schedule(object : TimerTask() {
            override fun run() {
                if (remainSeconds > 0) {
                    remainSeconds--
                } else {
                    GoToCheckWordsActivity()
                    countDownTimer!!.cancel()
                    countDownTimer = null
                }
            }
        }, 0, 1000)
    }

    override fun onDestroy() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }

        super.onDestroy()
    }

    @Composable
    fun WordItem(idx: Int) {
        Column(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth()
        ) {
            TextButton(
                onClick = {
                    for (listIdx in 0..showWordMeaning.lastIndex) {
                        if (listIdx != idx) {
                            showWordMeaning.set(listIdx, false)
                        } else {
                            if (showWordMeaning.get(idx)) {
                                showWordMeaning.set(idx, false)
                            } else {
                                showWordMeaning.set(idx, true)
                                val delayTimer = Timer()
                                delayTimer.schedule(object : TimerTask() {
                                    override fun run() {
                                        showWordMeaning.set(idx, false)
                                        delayTimer.cancel()
                                    }
                                }, 5000)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 5.dp),
                    text = word.get(idx),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            AnimatedVisibility(visible = showWordMeaning.get(idx)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(start = 15.dp, end = 15.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.width(200.dp),
                        text = wordMeaning.get(idx),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { SearchViaBaidu(word.get(idx)) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .height(30.dp)
                                .width(30.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.word_help),
                                contentDescription = "",
                                modifier = Modifier
                                    .height(25.dp)
                                    .width(25.dp)
                            )
                        }
                        TextButton(
                            onClick = { TextToSpeech(applicationContext, word.get(idx)) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .width(30.dp),
                        ) {
                            Image(
                                painter = painterResource(id = R.mipmap.speak),
                                contentDescription = "",
                                modifier = Modifier
                                    .height(25.dp)
                                    .width(25.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val configContext = createConfigurationContext(resources.configuration)

        return configContext.resources.apply {
            configuration.fontScale = 1.0f
            displayMetrics.scaledDensity = displayMetrics.density * configuration.fontScale
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "单词背诵过程中不得退出", Toast.LENGTH_LONG).show()
            return false
        }

        return super.onKeyUp(keyCode, event)
    }

    fun SearchViaBaidu(searchText: String) {
        try {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            val url: Uri = Uri.parse("https://www.baidu.com/s?wd=${searchText}")
            intent.data = url
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "启动系统浏览器失败", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    fun GoToCheckWordsActivity() {
        val i = Intent(this, CheckWordsActivity::class.java)
        this.startActivity(i)
    }
}
