package xyz.chenzyadb.wanna_recite

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apache.commons.io.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import xyz.chenzyadb.wanna_recite.ui.theme.WannaReciteTheme
import java.io.File
import java.io.FileOutputStream

class PrepareWordsActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val appDataPath = applicationContext.getFilesDir().getAbsolutePath()

        val timeStamp = System.currentTimeMillis()
        val nowaDay = (timeStamp / 24 / 3600 / 1000).toInt()

        var todayWordsJson = JSONObject()
        var jsonCreateDay = 0

        if (IsFileExist(appDataPath + "/today_words.json")) {
            try {
                val jsonFile = File(appDataPath + "/today_words.json")
                todayWordsJson = JSONObject(FileUtils.readFileToString(jsonFile))
                jsonCreateDay = todayWordsJson.getInt("jsonCreateDay")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val needReciteWord: MutableList<String> = ArrayList()
        val needReciteWordMeaning: MutableList<String> = ArrayList()

        val fp = File(appDataPath + "/word_book.json")
        val wordBookJson = JSONObject(FileUtils.readFileToString(fp))

        val wordBookName = wordBookJson.getString("wordBookName")
        val dailyReciteWordsTargetNum = wordBookJson.getInt("dailyReciteWordsTargetNum")
        val wordJsonArray = wordBookJson.getJSONArray("word")
        val wordMeaningJsonArray = wordBookJson.getJSONArray("wordMeaning")
        val isWordRecitedJsonArray = wordBookJson.getJSONArray("isWordRecited")
        val wordRecitedDayJsonArray = wordBookJson.getJSONArray("wordRecitedDay")

        if (jsonCreateDay == nowaDay) {
            val todayWordJsonArray = todayWordsJson.getJSONArray("word")
            val todayWordMeaningJsonArray = todayWordsJson.getJSONArray("wordMeaning")
            val todayWordIsRecitedJsonArray = todayWordsJson.getJSONArray("isWordRecited")

            for (idx in 0..todayWordIsRecitedJsonArray.length() - 1) {
                if (!todayWordIsRecitedJsonArray.getBoolean(idx)) {
                    needReciteWord.add(todayWordJsonArray.getString(idx))
                    needReciteWordMeaning.add(todayWordMeaningJsonArray.getString(idx))
                }
            }
        } else {
            val needReviewDays: Array<Int> =
                Array(5) { nowaDay - 1; nowaDay - 3; nowaDay - 7; nowaDay - 14; nowaDay - 28 }

            var addNewReciteWordsNum = 0
            for (idx in 0..isWordRecitedJsonArray.length() - 1) {
                if (isWordRecitedJsonArray.getBoolean(idx)) {
                    for (day in needReviewDays) {
                        if (wordRecitedDayJsonArray.getInt(idx) == day) {
                            needReciteWord.add(wordJsonArray.getString(idx))
                            needReciteWordMeaning.add(wordMeaningJsonArray.getString(idx))
                            break
                        }
                    }
                } else {
                    if (addNewReciteWordsNum < dailyReciteWordsTargetNum) {
                        needReciteWord.add(wordJsonArray.getString(idx))
                        needReciteWordMeaning.add(wordMeaningJsonArray.getString(idx))
                        addNewReciteWordsNum++
                    }
                }
            }

            val todayWordJsonArray = JSONArray()
            val todayWordMeaningJsonArray = JSONArray()
            val todayWordIsRecitedJsonArray = JSONArray()

            for (idx in 0..needReciteWord.lastIndex) {
                todayWordJsonArray.put(idx, needReciteWord.get(idx))
                todayWordMeaningJsonArray.put(idx, needReciteWordMeaning.get(idx))
                todayWordIsRecitedJsonArray.put(idx, false)
            }

            todayWordsJson = JSONObject()
            todayWordsJson.put("jsonCreateDay", nowaDay)
            todayWordsJson.put("word", todayWordJsonArray)
            todayWordsJson.put("wordMeaning", todayWordMeaningJsonArray)
            todayWordsJson.put("isWordRecited", todayWordIsRecitedJsonArray)

            try {
                WriteFile(todayWordsJson.toString(4), appDataPath + "/today_words.json")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
                                    text = "今日单词",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "One today is worth two tomorrows.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    ) {
                        val paddingValues = it

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Image(
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(388.dp),
                                painter = painterResource(id = R.drawable.recite_background),
                                contentDescription = null
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(top = paddingValues.calculateTopPadding())
                                .fillMaxSize()
                                .padding(top = 20.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                text = "《${wordBookName}》",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                text = "今日需要背诵共计${needReciteWord.lastIndex + 1}个单词, 预计花费${(needReciteWord.lastIndex + 1) * 40 / 60}分${(needReciteWord.lastIndex + 1) * 30 % 60}秒.",
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                text = "注意：背诵过程中退出程序将不会记录进度.",
                                color = MaterialTheme.colorScheme.secondary,
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light
                            )
                            if (needReciteWord.isNotEmpty()) {
                                StartReciteButton()
                            } else {
                                Text(
                                    modifier = Modifier
                                        .padding(top = 30.dp)
                                        .fillMaxWidth()
                                        .padding(start = 20.dp),
                                    text = "今日背诵已完成.",
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light
                                )
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

    @Composable
    fun StartReciteButton() {
        TextButton(
            onClick = {
                val i = Intent(this, ReciteWordsActivity::class.java)
                this.startActivity(i)
            },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 30.dp, start = 20.dp)
                .height(40.dp)
                .width(90.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开始背诵",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    fun IsFileExist(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
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
