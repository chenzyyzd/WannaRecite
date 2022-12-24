package xyz.chenzyadb.wanna_recite

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import xyz.chenzyadb.wanna_recite.ui.theme.WannaReciteTheme
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

class LoadWordBookActivity : ComponentActivity() {
    var appDataPath = ""

    val word: MutableList<String> = ArrayList()
    val wordMeaning: MutableList<String> = ArrayList()
    val isWordRecited: MutableList<Boolean> = ArrayList()
    val wordReciteDay: MutableList<Int> = ArrayList()

    var wordBookName: String by mutableStateOf("未知单词本")
    var dailyReciteWordsTargetNum: String by mutableStateOf("40")

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        appDataPath = applicationContext.getFilesDir().getAbsolutePath()

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
                                    text = "导入单词本",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "A good beginning is half done.",
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
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Image(
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(393.dp),
                                painter = painterResource(id = R.drawable.loadbook_background),
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
                            SelectWordBookFileButton()
                            WriteWordBookNameBar()
                            WriteDailyTargetWordsNumBar()
                            SaveWordBookButton()
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
    fun SelectWordBookFileButton() {
        TextButton(
            onClick = { SelectWordBookFile() },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(240.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.add_book),
                        contentDescription = null,
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp)
                    )
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = "选择要导入的单词本(.txt)",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.chevron_right),
                        contentDescription = "RightArrow",
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun WriteWordBookNameBar() {
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(50.dp)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(90.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.named_book),
                    contentDescription = null,
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = "名称:",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TextField(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                value = wordBookName,
                onValueChange = { wordBookName = it })
        }
    }

    @Composable
    fun WriteDailyTargetWordsNumBar() {
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .height(50.dp)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(190.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.daily_target),
                    contentDescription = null,
                    modifier = Modifier
                        .height(30.dp)
                        .width(30.dp)
                )
                Text(
                    modifier = Modifier.padding(start = 10.dp),
                    text = "每日学习单词目标:",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TextField(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp),
                value = dailyReciteWordsTargetNum,
                onValueChange = { dailyReciteWordsTargetNum = it }
            )
        }
    }

    @Composable
    fun SaveWordBookButton() {
        TextButton(
            onClick = { SaveWordBookToData() },
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(top = 30.dp, start = 10.dp)
                .height(40.dp)
                .width(100.dp),
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
                    text = "保存单词本",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    fun SelectWordBookFile() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "text/plain"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 42)
        } catch (e: Exception) {
            Toast.makeText(this, "调用SAF失败", Toast.LENGTH_LONG).show()

            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == 42 && resultCode == RESULT_OK) {
            val uri = resultData?.data

            try {
                val inStream = this.getContentResolver().openInputStream(uri!!)
                val reader = BufferedReader(InputStreamReader(inStream))
                var line: String?

                word.clear()
                wordMeaning.clear()
                isWordRecited.clear()
                wordReciteDay.clear()
                while (reader.readLine().also { line = it } != null) {
                    val endOfWord = line?.indexOf(" ")
                    if (endOfWord != null) {
                        word.add(line!!.substring(0, endOfWord))
                        wordMeaning.add(line!!.substring(endOfWord, line!!.length))
                        isWordRecited.add(false)
                        wordReciteDay.add(-1)
                    }
                }

                Toast.makeText(this, "导入单词本文件成功, 共计${word.size}个单词.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "导入单词本文件失败", Toast.LENGTH_LONG).show()

                e.printStackTrace()
            }
        }
    }

    fun SaveWordBookToData() {
        val wordBookJson = JSONObject()

        if (dailyReciteWordsTargetNum.length > 3) {
            Toast.makeText(this, "每日学习单词目标长度不得超过三个字符", Toast.LENGTH_LONG).show()
        } else if (wordBookName.length > 10) {
            Toast.makeText(this, "单词本名称长度不得超过十个字符", Toast.LENGTH_LONG).show()
        } else if (wordBookName.isBlank() || wordBookName.isEmpty()) {
            Toast.makeText(this, "单词本名称不得为空", Toast.LENGTH_LONG).show()
        } else if (!dailyReciteWordsTargetNum.matches("\\d+".toRegex())) {
            Toast.makeText(this, "每日学习单词目标必须为合法的数字", Toast.LENGTH_LONG).show()
        } else if (dailyReciteWordsTargetNum.toInt() < 5) {
            Toast.makeText(this, "每日学习单词目标必须不得少于5个", Toast.LENGTH_LONG).show()
        } else if (word.size < 100) {
            Toast.makeText(this, "导入的单词数量不得少于100个", Toast.LENGTH_LONG).show()
        } else if (word.size > dailyReciteWordsTargetNum.toInt()) {
            wordBookJson.put("wordBookName", wordBookName)
            wordBookJson.put("dailyReciteWordsTargetNum", dailyReciteWordsTargetNum.toInt())

            val timeStamp = System.currentTimeMillis()
            val nowaDay = (timeStamp / 24 / 3600 / 1000).toInt()
            wordBookJson.put("startReciteDay", nowaDay)

            val wordJsonArray = JSONArray()
            val wordMeaningJsonArray = JSONArray()
            val isWordRecitedJsonArray = JSONArray()
            val wordRecitedDayJsonArray = JSONArray()
            for (idx in 0..word.lastIndex) {
                wordJsonArray.put(idx, word.get(idx))
                wordMeaningJsonArray.put(idx, wordMeaning.get(idx))
                isWordRecitedJsonArray.put(idx, isWordRecited.get(idx))
                wordRecitedDayJsonArray.put(idx, wordReciteDay.get(idx))
            }
            wordBookJson.put("word", wordJsonArray)
            wordBookJson.put("wordMeaning", wordMeaningJsonArray)
            wordBookJson.put("isWordRecited", isWordRecitedJsonArray)
            wordBookJson.put("wordRecitedDay", wordRecitedDayJsonArray)

            try {
                WriteFile(wordBookJson.toString(4), appDataPath + "/word_book.json")

                Toast.makeText(this, "保存单词本成功", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "保存单词本失败", Toast.LENGTH_LONG).show()

                e.printStackTrace()
            }

            finish()
        } else {
            Toast.makeText(this, "每日学习单词目标不得大于导入单词总数", Toast.LENGTH_LONG).show()
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