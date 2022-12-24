package xyz.chenzyadb.wanna_recite

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apache.commons.io.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import xyz.chenzyadb.wanna_recite.ui.theme.WannaReciteTheme
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CheckWordsActivity : ComponentActivity() {
    var appDataPath = ""

    val needReciteWordList: MutableList<String> = ArrayList()
    val needReciteWordMeaningList: MutableList<String> = ArrayList()
    val needReciteWordWrongTime: MutableList<Int> = ArrayList()
    val allWordList: MutableList<String> = ArrayList()
    val allWordMeaningList: MutableList<String> = ArrayList()

    var isChineseToEnglish = false
    var nowRecitedWordsNum = 0
    var targetReciteWordsNum = 0

    var questionText: String by mutableStateOf("null")
    val answerTextList = mutableStateListOf<String>("null", "null", "null", "null", "null", "null")
    var rightAnswerIdx: Int by mutableStateOf(0)

    val answerButtonColor = mutableStateListOf<Color>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        appDataPath = applicationContext.getFilesDir().getAbsolutePath()

        val wordBookFile = File(appDataPath + "/word_book.json")
        val wordBookJson = JSONObject(FileUtils.readFileToString(wordBookFile))
        val allWordListJsonArray = wordBookJson.getJSONArray("word")
        val allWordMeaningListJsonArray = wordBookJson.getJSONArray("wordMeaning")
        for (idx in 0..allWordListJsonArray.length() - 1) {
            allWordList.add(allWordListJsonArray.getString(idx))
            allWordMeaningList.add(allWordMeaningListJsonArray.getString(idx))
        }

        val todayWordsFile = File(appDataPath + "/today_words.json")
        val todayWordsJson = JSONObject(FileUtils.readFileToString(todayWordsFile))
        val needReciteWordListJsonArray = todayWordsJson.getJSONArray("word")
        val needReciteWordMeaningListJsonArray = todayWordsJson.getJSONArray("wordMeaning")
        val isWordRecitedJsonArray = todayWordsJson.getJSONArray("isWordRecited")
        for (idx in 0..needReciteWordListJsonArray.length() - 1) {
            if (!isWordRecitedJsonArray.getBoolean(idx)) {
                needReciteWordList.add(needReciteWordListJsonArray.getString(idx))
                needReciteWordMeaningList.add(needReciteWordMeaningListJsonArray.getString(idx))
                needReciteWordWrongTime.add(0)
            }
        }

        if (needReciteWordList.size == 0) {
            finish()
        }

        targetReciteWordsNum = needReciteWordList.lastIndex + 1

        nowRecitedWordsNum = 0
        isChineseToEnglish = false

        if (!isDarkMode()) {
            for (item in 0..5) {
                answerButtonColor.add(Color(0xffffffff))
            }
        } else {
            for (item in 0..5) {
                answerButtonColor.add(Color(0xff202020))
            }
        }

        GetNextQuestion()

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
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "单词检查",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "There is no royal road to learning.",
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
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                                    .height(60.dp),
                                text = questionText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )

                            for (idx in 0..5) {
                                AnswerButton(idx)
                            }
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

    @Composable
    fun AnswerButton(idx: Int) {
        TextButton(
            onClick = {
                if (idx == rightAnswerIdx) {
                    if (!isDarkMode()) {
                        answerButtonColor.set(idx, Color(0xffccffcc))
                    } else {
                        answerButtonColor.set(idx, Color(0xff008800))
                    }
                    val delayTimer = Timer()
                    delayTimer.schedule(object : TimerTask() {
                        override fun run() {
                            ResetAnswerButtonColor()
                            GetNextQuestion()
                            delayTimer.cancel()
                        }
                    }, 500)
                } else {
                    needReciteWordWrongTime[nowRecitedWordsNum - 1]++
                    if (!isDarkMode()) {
                        answerButtonColor.set(idx, Color(0xffffcccc))
                    } else {
                        answerButtonColor.set(idx, Color(0xff880000))
                    }
                    val delayTimer = Timer()
                    delayTimer.schedule(object : TimerTask() {
                        override fun run() {
                            ResetAnswerButtonColor()
                            delayTimer.cancel()
                        }
                    }, 1000)
                }
            },
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                .height(60.dp)
                .fillMaxWidth()
                .background(
                    color = answerButtonColor.get(idx),
                    shape = RoundedCornerShape(10.dp)
                ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 5.dp),
                text = answerTextList.get(idx),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }

    fun ResetAnswerButtonColor() {
        if (!isDarkMode()) {
            for (item in 0..5) {
                answerButtonColor.set(item, Color(0xffffffff))
            }
        } else {
            for (item in 0..5) {
                answerButtonColor.set(item, Color(0xff202020))
            }
        }
    }

    fun GetNextQuestion() {
        rightAnswerIdx = (0..5).random()

        if (nowRecitedWordsNum < targetReciteWordsNum) {
            if (isChineseToEnglish) {
                questionText = needReciteWordMeaningList.get(nowRecitedWordsNum)
                answerTextList.set(rightAnswerIdx, needReciteWordList.get(nowRecitedWordsNum))
                for (idx in 0..5) {
                    if (idx != rightAnswerIdx) {
                        var tempAnswer = allWordList.get((0..allWordList.lastIndex).random())
                        while (answerTextList.contains(tempAnswer)) {
                            tempAnswer = allWordList.get((0..allWordList.lastIndex).random())
                        }
                        answerTextList.set(idx, tempAnswer)
                    }
                }
                nowRecitedWordsNum++
            } else {
                questionText = needReciteWordList.get(nowRecitedWordsNum)
                answerTextList.set(rightAnswerIdx, needReciteWordMeaningList.get(nowRecitedWordsNum))
                for (idx in 0..5) {
                    if (idx != rightAnswerIdx) {
                        var tempAnswer = allWordMeaningList.get((0..allWordMeaningList.lastIndex).random())
                        while (answerTextList.contains(tempAnswer)) {
                            tempAnswer = allWordMeaningList.get((0..allWordMeaningList.lastIndex).random())
                        }
                        answerTextList.set(idx, tempAnswer)
                    }
                }
                nowRecitedWordsNum++
            }
        } else {
            if (!isChineseToEnglish) {
                isChineseToEnglish = true
                nowRecitedWordsNum = 0
                GetNextQuestion()
            } else {
                SaveReciteResult()
            }
        }
    }

    fun SaveReciteResult() {
        val todayWordJsonArray = JSONArray()
        val todayWordMeaningJsonArray = JSONArray()
        val todayWordIsRecitedJsonArray = JSONArray()

        for (idx in 0..needReciteWordList.lastIndex) {
            todayWordJsonArray.put(idx, needReciteWordList.get(idx))
            todayWordMeaningJsonArray.put(idx, needReciteWordMeaningList.get(idx))
            if (needReciteWordWrongTime.get(idx) > 0) {
                todayWordIsRecitedJsonArray.put(idx, false)
            } else {
                todayWordIsRecitedJsonArray.put(idx, true)
            }
        }

        val timeStamp = System.currentTimeMillis()
        val nowaDay = (timeStamp / 24 / 3600 / 1000).toInt()

        val todayWordsJson = JSONObject()
        todayWordsJson.put("jsonCreateDay", nowaDay)
        todayWordsJson.put("word", todayWordJsonArray)
        todayWordsJson.put("wordMeaning", todayWordMeaningJsonArray)
        todayWordsJson.put("isWordRecited", todayWordIsRecitedJsonArray)

        try {
            WriteFile(todayWordsJson.toString(4), appDataPath + "/today_words.json")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var needReview = false
        for (idx in 0..needReciteWordWrongTime.lastIndex) {
            if (needReciteWordWrongTime.get(idx) > 0) {
                needReview = true
            }
        }

        if (needReview) {
            val i = Intent(this, ReciteWordsActivity::class.java)
            this.startActivity(i)
        } else {
            val i = Intent(this, FinishReciteActivity::class.java)
            this.startActivity(i)
        }
    }

    fun isDarkMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun WriteFile(writeText: String, filePath: String) {
        val outPutStream = FileOutputStream(filePath)
        val writeFile = File(filePath)
        if (!writeFile.exists()) {
            try {
                writeFile.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            outPutStream.write(writeText.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}