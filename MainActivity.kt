package com.xikmad.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val bg = Color(0xFF0B1020)
private val panel = Color(0xFF151D32)
private val violet = Color(0xFF8057F5)
private val cyan = Color(0xFF31C8E8)
private val white = Color(0xFFF4F7FB)
private val muted = Color(0xFF9AA8C3)

data class Msg(val id:String, val text:String, val mine:Boolean, val time:Long)
data class ThreadChat(val id:String, val title:String, val msgs:List<Msg>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { XikmadApp() }
    }
}

@Composable
private fun XikmadApp() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("xikmad", 0) }
    var chats by remember { mutableStateOf(readChats(prefs.getString("chats", null))) }
    var active by remember { mutableStateOf(chats.firstOrNull()?.id) }
    var input by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var english by remember { mutableStateOf(prefs.getBoolean("english", false)) }
    var settings by remember { mutableStateOf(false) }
    val current = chats.firstOrNull { it.id == active }

    fun save(list:List<ThreadChat>) {
        chats = list
        prefs.edit().putString("chats", writeChats(list)).apply()
    }
    fun newChat() {
        val t = ThreadChat(UUID.randomUUID().toString(), if(english) "New chat" else "Sheeko cusub", emptyList())
        save(listOf(t) + chats); active = t.id
    }
    fun send() {
        val text = input.trim()
        val c = chats.firstOrNull { it.id == active } ?: return
        if(text.isEmpty()) return
        val now = System.currentTimeMillis()
        val answer = offlineAnswer(text, english)
        val list = c.msgs + Msg(UUID.randomUUID().toString(), text, true, now) +
                Msg(UUID.randomUUID().toString(), answer, false, now + 1)
        val updated = c.copy(title = if(c.msgs.isEmpty()) text.take(30) else c.title, msgs = list)
        save(chats.map { if(it.id == c.id) updated else it })
        input = ""
    }

    MaterialTheme(colorScheme = darkColorScheme(primary=violet, secondary=cyan, background=bg, surface=panel, onSurface=white)) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(bg, Color(0xFF111A30))))) {
            Row(Modifier.fillMaxWidth().background(panel).padding(14.dp), verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(violet,cyan)), CircleShape), contentAlignment=Alignment.Center) {
                    Text("✦", color=white, fontSize=25.sp, fontWeight=FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("XIKMAD AI", color=white, fontWeight=FontWeight.Bold, fontSize=18.sp)
                    Text(if(english) "Offline • local chat" else "Offline • sheeko gudaha ah", color=muted, fontSize=11.sp)
                }
                Text("⌕", color=cyan, fontSize=26.sp, modifier=Modifier.clickable { searching=!searching }.padding(5.dp))
                Text("＋", color=white, fontSize=25.sp, modifier=Modifier.clickable { newChat() }.padding(5.dp))
                Text("⋮", color=white, fontSize=24.sp, modifier=Modifier.clickable { settings=true }.padding(start=8.dp))
            }
            if(searching) {
                OutlinedTextField(query, {query=it}, Modifier.fillMaxWidth().padding(10.dp),
                    placeholder={Text(if(english) "Search chats" else "Raadi sheekooyinka")}, singleLine=true)
            }
            if(current == null) {
                Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
                    Text("✦", color=cyan, fontSize=58.sp)
                    Text("XIKMAD AI", color=white, fontSize=27.sp, fontWeight=FontWeight.Bold)
                    Text(if(english) "Your private chat space" else "Goobtaada sheekada gaarka ah", color=muted)
                    Button(onClick={newChat()}, modifier=Modifier.padding(top=18.dp)) { Text(if(english) "Start chat" else "Bilow sheeko") }
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal=12.dp), contentPadding=PaddingValues(vertical=12.dp), verticalArrangement=Arrangement.spacedBy(9.dp)) {
                    if(current.msgs.isEmpty()) item {
                        Column(Modifier.fillMaxWidth().padding(vertical=35.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                            Text("✧", color=violet, fontSize=45.sp)
                            Text(if(english) "What would you like to ask?" else "Maxaad jeclaan lahayd inaad weydiiso?", color=white, fontSize=17.sp)
                            Text(if(english) "Chats are saved on this device." else "Sheekooyinka telefoonkan ayay ku kaydsan yihiin.", color=muted, fontSize=11.sp)
                        }
                    }
                    items(current.msgs, key={it.id}) { m ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement=if(m.mine) Arrangement.End else Arrangement.Start) {
                            Column(Modifier.widthIn(max=310.dp).background(if(m.mine) Color(0xFF284D79) else panel, RoundedCornerShape(18.dp)).padding(12.dp)) {
                                Text(m.text, color=white, fontSize=14.sp)
                                Text(timeText(m.time) + if(m.mine) "  ✓✓" else "", color=if(m.mine) Color(0xFF9DD9F5) else muted, fontSize=10.sp, modifier=Modifier.align(Alignment.End).padding(top=4.dp))
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment=Alignment.CenterVertically) {
                    OutlinedTextField(input, {input=it}, Modifier.weight(1f), placeholder={Text(if(english) "Message XIKMAD..." else "Fariin u dir XIKMAD...")}, maxLines=4, shape=RoundedCornerShape(24.dp))
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(onClick={send()}, containerColor=violet, contentColor=white, modifier=Modifier.size(48.dp)) { Text("➤") }
                }
                Text(if(english) "Local chat • no account needed" else "Chat gudaha ah • akoon looma baahna", color=muted, fontSize=10.sp, modifier=Modifier.align(Alignment.CenterHorizontally).padding(bottom=5.dp))
            }
            if(chats.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().heightIn(max=170.dp).background(Color(0xFF0E1628)).padding(horizontal=12.dp, vertical=6.dp)) {
                    Text(if(english) "RECENT CHATS" else "SHEEKOYINKII U DAMBEEYAY", color=muted, fontSize=10.sp, fontWeight=FontWeight.Bold)
                    LazyColumn {
                        items(chats.filter { it.title.contains(query, true) }.take(5), key={it.id}) { t ->
                            Row(Modifier.fillMaxWidth().clickable { active=t.id }.padding(vertical=7.dp), verticalAlignment=Alignment.CenterVertically) {
                                Box(Modifier.size(29.dp).background(Color(0xFF29365A), CircleShape), contentAlignment=Alignment.Center) { Text("✦", color=cyan) }
                                Spacer(Modifier.width(9.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t.title, color=if(t.id==active) cyan else white, fontSize=12.sp, maxLines=1, overflow=TextOverflow.Ellipsis)
                                    Text(t.msgs.lastOrNull()?.text ?: if(english) "No messages yet" else "Fariin wali ma jirto", color=muted, fontSize=10.sp, maxLines=1, overflow=TextOverflow.Ellipsis)
                                }
                                Text("×", color=muted, fontSize=20.sp, modifier=Modifier.clickable {
                                    val left=chats.filterNot { it.id==t.id }; save(left)
                                    if(active==t.id) active=left.firstOrNull()?.id
                                }.padding(horizontal=8.dp))
                            }
                        }
                    }
                }
            }
        }
        if(settings) AlertDialog(
            onDismissRequest={settings=false},
            title={Text(if(english) "Settings" else "Dejinta")},
            text={Row(verticalAlignment=Alignment.CenterVertically) {
                Text(if(english) "English language" else "Luqadda English", Modifier.weight(1f))
                Switch(english, { english=it; prefs.edit().putBoolean("english",it).apply() })
            }},
            confirmButton={TextButton(onClick={settings=false}) { Text("OK") }}
        )
    }
}

private fun offlineAnswer(s:String, en:Boolean):String {
    val q=s.lowercase(Locale.ROOT)
    return when {
        q.contains("salaan") || q.contains("hello") || q=="hi" -> if(en) "Hello! I'm XIKMAD AI." else "Salaan! Waxaan ahay XIKMAD AI."
        q.contains("magac") || q.contains("name") -> if(en) "My name is XIKMAD AI." else "Magacaygu waa XIKMAD AI."
        q.contains("mahadsan") || q.contains("thank") -> if(en) "You're welcome!" else "Adaa mudan!"
        else -> if(en)
            "I received your message. Chat and history work offline, but generative AI answers need a compatible on-device model engine that is not included yet."
        else
            "Fariintaada waan helay. Chat-ka iyo taariikhdu offline ayay shaqaynayaan; jawaabaha AI-ga dhabta ah waxay u baahan yihiin model iyo matoor ku shaqeeya telefoonka oo aan wali lagu darin."
    }
}

private fun timeText(t:Long)=SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(t))
private fun writeChats(chats:List<ThreadChat>):String {
    val root=JSONArray()
    chats.forEach { c ->
        val msgs=JSONArray()
        c.msgs.forEach { m -> msgs.put(JSONObject().put("id",m.id).put("text",m.text).put("mine",m.mine).put("time",m.time)) }
        root.put(JSONObject().put("id",c.id).put("title",c.title).put("msgs",msgs))
    }
    return root.toString()
}
private fun readChats(raw:String?):List<ThreadChat> {
    if(raw.isNullOrBlank()) return emptyList()
    return try {
        val root=JSONArray(raw)
        (0 until root.length()).map { i ->
            val c=root.getJSONObject(i); val a=c.getJSONArray("msgs")
            val msgs=(0 until a.length()).map { j ->
                val m=a.getJSONObject(j); Msg(m.getString("id"),m.getString("text"),m.getBoolean("mine"),m.getLong("time"))
            }
            ThreadChat(c.getString("id"),c.getString("title"),msgs)
        }
    } catch(_:Exception) { emptyList() }
}
