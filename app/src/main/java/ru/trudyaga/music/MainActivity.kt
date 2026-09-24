package ru.trudyaga.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Track(val title:String,val subtitle:String,val c1:Color,val c2:Color,val icon:String)

private val tracks=listOf(
 Track("Натяжка СИП","Трудяга · 03:42",Color(0xFF0D47A1),Color(0xFF42A5F5),"⚡"),
 Track("Опора №37","Северные линии · 04:18",Color(0xFF263238),Color(0xFF90A4AE),"37"),
 Track("Монтаж ВЛ","Трудяга · 03:56",Color(0xFF6A1B9A),Color(0xFFCE93D8),"⌁"),
 Track("Тяжёлый день","Трудяга · 05:01",Color(0xFFBF360C),Color(0xFFFFB74D),"☀"),
 Track("Северные линии","Трудяга · 04:27",Color(0xFF00695C),Color(0xFF80CBC4),"✦"),
 Track("Инструмент","Трудяга · 02:58",Color(0xFF37474F),Color(0xFFB0BEC5),"🔧")
)

@Composable fun Cover(t:Track,modifier:Modifier=Modifier){
 Canvas(modifier.background(RoundedCornerShape(18.dp)).then(modifier)){
   drawRoundRect(Brush.linearGradient(listOf(t.c1,t.c2)),cornerRadius=androidx.compose.ui.geometry.CornerRadius(34f,34f))
   drawCircle(Color.White.copy(alpha=.08f),size.minDimension*.32f,center=Offset(size.width*.78f,size.height*.22f))
   drawCircle(Color.Black.copy(alpha=.14f),size.minDimension*.5f,center=Offset(size.width*.15f,size.height*.88f))
 }
 Box(modifier=Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
   Text(t.icon,color=Color.White,fontSize=42.sp,fontWeight=FontWeight.Bold)
 }
}

@Composable fun App(){
 var tab by remember{mutableIntStateOf(0)}
 var current by remember{mutableStateOf(tracks[0])}
 var playing by remember{mutableStateOf(false)}
 val bg=Color(0xFF090A0F); val card=Color(0xFF151821); val muted=Color(0xFF969BA8)
 Surface(color=bg,modifier=Modifier.fillMaxSize()){
  Column(Modifier.fillMaxSize().padding(top=14.dp)){
   Row(Modifier.fillMaxWidth().padding(horizontal=20.dp),verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text("Трудяга Music",color=Color.White,fontSize=27.sp,fontWeight=FontWeight.Bold);Text("Музыка для рабочего дня",color=muted,fontSize=13.sp)}
    Text("•••",color=Color.White,fontSize=25.sp)
   }
   Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=20.dp,vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){
    listOf("Для тебя","Энергия","Спокойно","Избранное").forEachIndexed{i,s->AssistChip(onClick={tab=i},label={Text(s)},colors=AssistChipDefaults.assistChipColors(containerColor=if(tab==i)Color.White else card,labelColor=if(tab==i)Color.Black else Color.White))}
   }
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)){
    Text("Твои треки",color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement=Arrangement.spacedBy(14.dp)){
      tracks.take(2).forEach{t->Column(Modifier.width(165.dp).clickable{current=t}){
       Cover(t,Modifier.size(165.dp));Spacer(Modifier.height(8.dp));Text(t.title,color=Color.White,fontWeight=FontWeight.SemiBold,maxLines=1);Text(t.subtitle,color=muted,fontSize=12.sp)
      }}
    }
    Spacer(Modifier.height(22.dp));Text("Плейлисты",color=Color.White,fontSize=21.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    tracks.drop(2).forEach{t->Row(Modifier.fillMaxWidth().clickable{current=t;playing=true}.padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically){
      Cover(t,Modifier.size(62.dp));Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(t.title,color=Color.White,fontWeight=FontWeight.Medium);Text(t.subtitle,color=muted,fontSize=12.sp)}Text("⋮",color=muted,fontSize=24.sp)
    }}
    Spacer(Modifier.height(90.dp))
   }
   Surface(color=card,modifier=Modifier.fillMaxWidth().clickable{playing=!playing}){
    Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
      Cover(current,Modifier.size(54.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(current.title,color=Color.White,fontWeight=FontWeight.Bold);Text(current.subtitle,color=muted,fontSize=11.sp)}
      Text("♡",color=Color.White,fontSize=25.sp);Spacer(Modifier.width(14.dp));Text(if(playing)"Ⅱ" else "▶",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.width(5.dp))
    }
   }
   Row(Modifier.fillMaxWidth().background(bg).padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceAround){
    listOf("⌂" to "Главная","⌕" to "Поиск","♫" to "Моя музыка","♡" to "Избранное").forEachIndexed{i,p->Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.clickable{tab=i}){Text(p.first,color=if(tab==i)Color.White else muted,fontSize=22.sp);Text(p.second,color=if(tab==i)Color.White else muted,fontSize=10.sp)}}}
  }
 }
}

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()}}}
