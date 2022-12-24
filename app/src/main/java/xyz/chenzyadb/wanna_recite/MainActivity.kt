package xyz.chenzyadb.wanna_recite

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.apache.commons.io.FileUtils
import org.json.JSONObject
import xyz.chenzyadb.wanna_recite.ui.theme.WannaReciteTheme
import java.io.File

class MainActivity : ComponentActivity() {
    var appDataPath = ""

    var wordBookName: String by mutableStateOf("未知单词本")
    var needReviewWordsNum: Int by mutableStateOf(0)
    var needReciteWordsNum: Int by mutableStateOf(0)

    var showWordBookController: Boolean by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        appDataPath = applicationContext.getFilesDir().getAbsolutePath()

        setContent {
            WannaReciteTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
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
                                    text = "WannaRecite",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(start = 15.dp),
                                    text = "The secret of success is constancy to purpose.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
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
                                    .height(382.dp),
                                painter = painterResource(id = R.drawable.main_background),
                                contentDescription = null
                            )
                        }

                        Column(
                            modifier = Modifier
                                .padding(top = paddingValues.calculateTopPadding())
                                .fillMaxSize()
                                .padding(top = 20.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            WordsReciteStateBar()
                            AnimatedVisibility(visible = showWordBookController) {
                                WordsBookController()
                            }
                            StartRecite()
                            StartReview()
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

    override fun onStart() {
        super.onStart()

        if (IsFileExist(appDataPath + "/word_book.json")) {
            val fp = File(appDataPath + "/word_book.json")
            val wordBookJson = JSONObject(FileUtils.readFileToString(fp))

            wordBookName = wordBookJson.getString("wordBookName")

            val isWordRecitedJsonArray = wordBookJson.getJSONArray("isWordRecited")
            val wordRecitedDayJsonArray = wordBookJson.getJSONArray("wordRecitedDay")

            val timeStamp = System.currentTimeMillis()
            val nowaDay = (timeStamp / 24 / 3600 / 1000).toInt()
            val needReviewDays: Array<Int> =
                Array(5) { nowaDay - 1; nowaDay - 3; nowaDay - 7; nowaDay - 14; nowaDay - 28 }

            needReciteWordsNum = wordBookJson.getInt("dailyReciteWordsTargetNum")
            needReviewWordsNum = 0
            for (idx in 0..isWordRecitedJsonArray.length() - 1) {
                if (isWordRecitedJsonArray.getBoolean(idx)) {
                    for (day in needReviewDays) {
                        if (wordRecitedDayJsonArray.getInt(idx) == day) {
                            needReviewWordsNum++
                            break
                        }
                    }
                }
            }
        } else {
            wordBookName = "尚未导入单词本"
            needReciteWordsNum = 0
            needReviewWordsNum = 0
        }
    }

    @Composable
    fun WordsReciteStateBar() {
        TextButton(
            onClick = { showWordBookController = !showWordBookController },
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                Modifier
                    .padding(start = 5.dp)
                    .height(80.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = wordBookName,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "需要复习的单词数量: ${needReviewWordsNum}个, 需要背诵的单词数量: ${needReciteWordsNum}个.",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }

    @Composable
    fun WordsBookController() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(end = 20.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { LoadWordBook() },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(30.dp)
                    .width(110.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.file_export),
                        contentDescription = "",
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp)
                            .padding(end = 5.dp)
                    )
                    Text(
                        text = "导入单词本",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            TextButton(
                onClick = { },
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(30.dp)
                    .width(110.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.delete_file),
                        contentDescription = "",
                        modifier = Modifier
                            .height(30.dp)
                            .width(30.dp)
                            .padding(end = 5.dp)
                    )

                    Text(
                        text = "内置单词本",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    @Composable
    fun StartRecite() {
        TextButton(
            onClick = { GoToReciteWords() },
            modifier = Modifier
                .padding(top = 15.dp)
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
                        .width(200.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.start_recite),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .height(30.dp)
                            .width(30.dp)
                    )
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = "完成今日背诵任务",
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
    fun StartReview() {
        TextButton(
            onClick = { },
            modifier = Modifier
                .padding(top = 5.dp)
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
                        .width(200.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.start_review),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .height(30.dp)
                            .width(30.dp)
                    )
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = "复习已学单词",
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

    fun LoadWordBook() {
        val i = Intent(this, LoadWordBookActivity::class.java)
        this.startActivity(i)
    }

    fun GoToReciteWords() {
        if (IsFileExist(appDataPath + "/word_book.json")) {
            val i = Intent(this, PrepareWordsActivity::class.java)
            this.startActivity(i)
        } else {
            Toast.makeText(this, "请先导入单词本", Toast.LENGTH_LONG).show()
        }
    }

    fun IsFileExist(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

}
