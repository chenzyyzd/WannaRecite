package xyz.chenzyadb.wanna_recite

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import java.util.ArrayList

class FinishReciteActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val appDataPath = applicationContext.getFilesDir().getAbsolutePath()

        val timeStamp = System.currentTimeMillis()
        val nowaDay = (timeStamp / 24 / 3600 / 1000).toInt()

        val todayWordsJsonFile = File(appDataPath + "/today_words.json")
        val todayWordsJson = JSONObject(FileUtils.readFileToString(todayWordsJsonFile))
        val todayWordsJsonArray = todayWordsJson.getJSONArray("word")

        val todayWordsList: MutableList<String> = ArrayList()
        for (idx in 0..todayWordsJsonArray.length() - 1) {
            todayWordsList.add(todayWordsJsonArray.getString(idx))
        }

        val wordBookFile = File(appDataPath + "/word_book.json")
        val wordBookJson = JSONObject(FileUtils.readFileToString(wordBookFile))
        val startReciteDay = wordBookJson.getInt("startReciteDay")
        val dailyReciteWordsTargetNum = wordBookJson.getInt("dailyReciteWordsTargetNum")
        val wordJsonArray = wordBookJson.getJSONArray("word")
        val isWordRecitedJsonArray = wordBookJson.getJSONArray("isWordRecited")
        val wordRecitedDayJsonArray = wordBookJson.getJSONArray("wordRecitedDay")

        for (idx in 0..wordJsonArray.length() - 1) {
            if (todayWordsList.contains(wordJsonArray.getString(idx))) {
                isWordRecitedJsonArray.put(idx, true)
                wordRecitedDayJsonArray.put(idx, nowaDay)
            }
        }

        wordBookJson.put("word", wordJsonArray)
        wordBookJson.put("isWordRecited", isWordRecitedJsonArray)
        wordBookJson.put("wordRecitedDay", wordRecitedDayJsonArray)

        try {
            WriteFile(wordBookJson.toString(4), appDataPath + "/word_book.json")

            Toast.makeText(this, "保存单词本成功", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "保存单词本失败", Toast.LENGTH_LONG).show()

            e.printStackTrace()
        }

        var needReciteWordsNum = 0
        for (idx in 0..isWordRecitedJsonArray.length() - 1) {
            if (!isWordRecitedJsonArray.getBoolean(idx)) {
                needReciteWordsNum++
            }
        }

        val passedDay = nowaDay - startReciteDay
        val remainDay = needReciteWordsNum / dailyReciteWordsTargetNum
        val delayedDay = passedDay - ((wordJsonArray.length() / dailyReciteWordsTargetNum) - remainDay)

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
                                    text = "背诵完成",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "Rome was not built in a day.",
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
                                    .height(450.dp),
                                painter = painterResource(id = R.drawable.finish_background),
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
                                text = "您已完成今日的背诵任务.",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                text = "所有背诵进度数据已经保存.",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light
                            )
                            Text(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .fillMaxWidth()
                                    .padding(start = 20.dp),
                                text = "已经坚持 ${passedDay} 天, 还需 ${remainDay} 天, 共延误 ${delayedDay} 天.",
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 2,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Light
                            )
                            FinishReciteButton()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FinishReciteButton() {
        TextButton(
            onClick = {
                val i = Intent(this, MainActivity::class.java)
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
                    text = "回到首页",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
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
