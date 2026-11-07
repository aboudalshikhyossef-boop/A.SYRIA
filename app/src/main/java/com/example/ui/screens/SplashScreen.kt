package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberZen
import com.example.ui.theme.CyberCyan
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class QuranicVerse(val arabic: String, val reference: String)
private val quranicVerses = listOf(
    QuranicVerse("وَيَنْصُرَكَ اللَّهُ نَصْرًا عَزِيزًا", "الفتح [ 48:3 ]"),
    QuranicVerse("إِنَّ اللَّهَ مَعَ الصَّابِرِينَ", "البقرة [ 2:153 ]"),
    QuranicVerse("وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ", "الطلاق [ 65:3 ]"),
    QuranicVerse("فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "الشرح [ 94:5 ]"),
    QuranicVerse("وَاللَّهُ خَيْرُ الْحَافِظِينَ", "يوسف [ 12:64 ]"),
    QuranicVerse("وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ", "الضحى [ 93:5 ]"),
    QuranicVerse("إِنَّا فَتَحْنَا لَكَ فَتْحًا مُّبِينًا", "الفتح [ 48:1 ]"),
    QuranicVerse("وَكَفَىٰ بِاللَّهِ وَلِيًّا وَكَفَىٰ بِاللَّهِ نَصِيرًا", "النساء [ 4:45 ]"),
    QuranicVerse("ادْعُونِي أَسْتَجِبْ لَكُمْ", "غافر [ 40:60 ]"),
    QuranicVerse("وَمَا النَّصْرُ إِلَّا مِنْ عِندِ اللَّهِ", "آل عمران [ 3:126 ]")
)
private data class SplashParticle(val x: Float, val y: Float, val radius: Float, val speed: Float, val alpha: Float, val phase: Float)

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    // KEY FIX: rememberUpdatedState ensures the LaunchedEffect always uses the LATEST callback
    // even if the parent recomposes (because DataStore loaded and startDestination changed)
    val currentCallback by rememberUpdatedState(onNavigateToDashboard)

    val screenAlpha   = remember { Animatable(0f) }
    val shieldScale   = remember { Animatable(0.4f) }
    val logs          = remember { mutableStateListOf<String>() }
    val listState     = rememberLazyListState()
    var verseVisible  by remember { mutableStateOf(false) }
    var neuralUnlocked by remember { mutableStateOf(false) }
    val selectedVerse  = remember { quranicVerses[Random.nextInt(quranicVerses.size)] }

    val particles = remember {
        List(30) {
            SplashParticle(
                x = Random.nextFloat(), y = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 0.5f,
                speed  = Random.nextFloat() * 0.3f + 0.1f,
                alpha  = Random.nextFloat() * 0.4f + 0.05f,
                phase  = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    val systemChecks = listOf(
        "[OK] CORE_INIT // SOVEREIGN ENGINE START",
        "[OK] HW_ENCRYPT // BIOMETRIC SHIELD SYNCING",
        "[OK] SAT_GRID // PRAYER TIMES CALIBRATION",
        "[OK] VAULT_BOOT // DECRYPTING DESIGN BLUEPRINTS",
        "[OK] NEURAL_HUD // CALIBRATING SENSORS",
        "[OK] CYBER_SYNC // ESTABLISHING AI UPLINK",
        "[OK] SECURITY_CHECK // OPTIMIZING FIREWALLS",
        "[OK] DATA_STORE // LOADING USER PROFILE",
        "[OK] SYSTEM_READY // A.SYRIA V5 STABLE"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val glowAlpha   by infiniteTransition.animateFloat(initialValue=0.4f, targetValue=1.0f, animationSpec=infiniteRepeatable(tween(2000), RepeatMode.Reverse), label="glow")
    val shieldPulse by infiniteTransition.animateFloat(initialValue=1.0f, targetValue=1.06f, animationSpec=infiniteRepeatable(tween(1200), RepeatMode.Reverse), label="pulse")
    val scanLine    by infiniteTransition.animateFloat(initialValue=0f,   targetValue=1f,    animationSpec=infiniteRepeatable(tween(3000), RepeatMode.Restart), label="scan")
    val neonAlpha   by infiniteTransition.animateFloat(initialValue=0.3f, targetValue=0.9f,  animationSpec=infiniteRepeatable(tween(1500), RepeatMode.Reverse), label="neon")
    val ringRot     by infiniteTransition.animateFloat(initialValue=0f,   targetValue=360f,  animationSpec=infiniteRepeatable(tween(8000, easing=LinearEasing), RepeatMode.Restart), label="ring")
    val breathRing  by infiniteTransition.animateFloat(initialValue=0f,   targetValue=1f,    animationSpec=infiniteRepeatable(tween(2500, easing=FastOutSlowInEasing), RepeatMode.Reverse), label="breath")
    val particleTime by infiniteTransition.animateFloat(initialValue=0f,  targetValue=1000f, animationSpec=infiniteRepeatable(tween(60000, easing=LinearEasing), RepeatMode.Restart), label="pt")
    val wavePhase   by infiniteTransition.animateFloat(initialValue=0f,   targetValue=(2f*PI).toFloat(), animationSpec=infiniteRepeatable(tween(4000, easing=LinearEasing), RepeatMode.Restart), label="wave")

    LaunchedEffect(Unit) {
        shieldScale.animateTo(1f, animationSpec = spring(dampingRatio=0.6f, stiffness=200f))
        screenAlpha.animateTo(1f, animationSpec = tween(800))
        delay(200)
        verseVisible = true
        for (check in systemChecks) {
            logs.add("> $check")
            if (logs.size > 0) listState.animateScrollToItem(logs.size - 1)
            delay(300)
        }
        delay(600)
        neuralUnlocked = true
        delay(800)
        currentCallback()  // Always uses the latest callback (correct startDestination)
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF020E1A), Color(0xFF000509)), radius=1800f)).statusBarsPadding().navigationBarsPadding()) {
        // Grid background
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
            val gs = 30.dp.toPx()
            var x = 0f; while (x <= size.width) { drawLine(Color.Cyan, Offset(x,0f), Offset(x,size.height), strokeWidth=0.5f); x+=gs }
            var y = 0f; while (y <= size.height) { drawLine(Color.Cyan, Offset(0f,y), Offset(size.width,y), strokeWidth=0.5f); y+=gs }
        }
        // Particles
        Canvas(modifier = Modifier.fillMaxSize().alpha(screenAlpha.value)) {
            particles.forEach { p ->
                val px = ((p.x + sin(particleTime*p.speed*0.01f+p.phase)*0.05f)%1f)*size.width
                val py = ((p.y-(particleTime*p.speed*0.001f)%1f+1f)%1f)*size.height
                val pa = p.alpha*(0.5f+0.5f*sin(particleTime*0.05f+p.phase))
                drawCircle(CyberCyan.copy(alpha=pa.coerceIn(0f,1f)), radius=p.radius.dp.toPx(), center=Offset(px,py))
            }
        }
        // Corner brackets
        Canvas(modifier = Modifier.fillMaxSize().alpha(screenAlpha.value * 0.6f)) {
            val sw=1.5.dp.toPx(); val cs=50.dp.toPx(); val pad=16.dp.toPx()
            val fp = Path().apply {
                moveTo(pad+cs,pad); lineTo(pad,pad); lineTo(pad,pad+cs)
                moveTo(pad,size.height-pad-cs); lineTo(pad,size.height-pad); lineTo(pad+cs,size.height-pad)
                moveTo(size.width-pad-cs,size.height-pad); lineTo(size.width-pad,size.height-pad); lineTo(size.width-pad,size.height-pad-cs)
                moveTo(size.width-pad,pad+cs); lineTo(size.width-pad,pad); lineTo(size.width-pad-cs,pad)
            }
            drawPath(fp, CyberCyan, style=Stroke(sw, cap=StrokeCap.Round))
        }
        // Wave bottom
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).align(Alignment.BottomCenter).alpha(screenAlpha.value*0.35f)) {
            for (layer in 0..2) {
                val lp = wavePhase + layer * 1.2f; val la = 0.15f - layer * 0.04f
                val path = Path().apply {
                    moveTo(0f, size.height)
                    for (i in 0..size.width.toInt() step 4) { val xp=i.toFloat(); val yp=size.height*0.5f+sin(xp*0.015f+lp+layer)*(15f+layer*8f); lineTo(xp,yp) }
                    lineTo(size.width, size.height); close()
                }
                drawPath(path, Brush.verticalGradient(listOf(CyberCyan.copy(alpha=la), Color.Transparent)))
            }
        }

        Column(modifier=Modifier.fillMaxSize().padding(horizontal=24.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
            // Version badge
            Box(modifier=Modifier.alpha(screenAlpha.value).border(0.5.dp,CyberCyan.copy(alpha=0.3f),RoundedCornerShape(4.dp)).background(CyberCyan.copy(alpha=0.05f),RoundedCornerShape(4.dp)).padding(horizontal=14.dp,vertical=4.dp)) {
                Text("A.SYRIA SOVEREIGN OS v5.0.0", color=CyberCyan.copy(alpha=0.7f), fontSize=9.sp, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold, letterSpacing=2.sp)
            }
            Spacer(Modifier.height(28.dp))
            // Shield
            Box(modifier=Modifier.size(180.dp).scale(shieldScale.value*shieldPulse), contentAlignment=Alignment.Center) {
                Canvas(modifier=Modifier.fillMaxSize()) {
                    val center=Offset(size.width/2,size.height/2); val br=size.minDimension/2f+4.dp.toPx()+breathRing*6.dp.toPx(); val ba=0.08f+breathRing*0.18f
                    drawCircle(brush=Brush.radialGradient(listOf(CyberCyan.copy(alpha=ba),CyberCyan.copy(alpha=ba*0.3f),Color.Transparent),center=center,radius=br),radius=br,center=center)
                    drawCircle(color=CyberCyan.copy(alpha=0.12f*(1f-breathRing)),radius=br+4.dp.toPx(),center=center,style=Stroke(0.8.dp.toPx()))
                }
                Canvas(modifier=Modifier.size(160.dp)) {
                    val c=Offset(size.width/2,size.height/2); val or2=size.minDimension/2f-4.dp.toPx()
                    drawCircle(color=CyberCyan.copy(alpha=0.2f*glowAlpha),radius=or2,style=Stroke(1.5.dp.toPx()))
                    listOf(0f,120f,240f).forEach { sa ->
                        drawArc(color=CyberCyan.copy(alpha=0.7f*glowAlpha),startAngle=sa+ringRot,sweepAngle=60f,useCenter=false,style=Stroke(2.dp.toPx(),cap=StrokeCap.Round),size=Size(or2*2,or2*2),topLeft=Offset(c.x-or2,c.y-or2))
                    }
                    val ir=or2-14.dp.toPx()
                    listOf(30f,150f,270f).forEach { sa ->
                        drawArc(color=CyberCyan.copy(alpha=0.3f*glowAlpha),startAngle=sa-ringRot*0.5f,sweepAngle=40f,useCenter=false,style=Stroke(1.dp.toPx(),cap=StrokeCap.Round),size=Size(ir*2,ir*2),topLeft=Offset(c.x-ir,c.y-ir))
                    }
                }
                Canvas(modifier=Modifier.size(100.dp)) {
                    val sp = Path().apply {
                        val w=size.width; val h=size.height
                        moveTo(w/2f,0f); lineTo(w*0.9f,h*0.2f); lineTo(w*0.9f,h*0.55f)
                        cubicTo(w*0.9f,h*0.8f,w/2f,h,w/2f,h); cubicTo(w/2f,h,w*0.1f,h*0.8f,w*0.1f,h*0.55f); lineTo(w*0.1f,h*0.2f); close()
                    }
                    drawPath(sp, CyberCyan.copy(alpha=0.12f)); drawPath(sp, CyberCyan.copy(alpha=0.8f*glowAlpha), style=Stroke(2.dp.toPx()))
                    // Lock icon
                    val lw=size.width*0.3f; val lh=size.height*0.35f; val lx=(size.width-lw)/2f; val ly=size.height*0.42f
                    drawRect(CyberCyan.copy(alpha=0.7f*glowAlpha),topLeft=Offset(lx,ly),size=Size(lw,lh),style=Stroke(1.5.dp.toPx()))
                    drawArc(CyberCyan.copy(alpha=0.7f*glowAlpha),startAngle=180f,sweepAngle=180f,useCenter=false,style=Stroke(1.5.dp.toPx()),size=Size(lw*0.6f,lh*0.7f),topLeft=Offset(lx+lw*0.2f,ly-lh*0.45f))
                    drawCircle(CyberCyan.copy(alpha=glowAlpha),radius=2.dp.toPx(),center=Offset(size.width/2f,ly+lh/2f))
                }
                // Scan line
                Canvas(modifier=Modifier.size(160.dp).alpha(screenAlpha.value)) {
                    val slY=scanLine*size.height
                    drawLine(Brush.horizontalGradient(listOf(Color.Transparent,CyberCyan.copy(alpha=0.5f),Color.Transparent)),Offset(0f,slY),Offset(size.width,slY),strokeWidth=1.dp.toPx())
                }
            }
            Spacer(Modifier.height(32.dp))
            // Quranic verse
            AnimatedVisibility(visible=verseVisible, enter=fadeIn(tween(1000)), exit=fadeOut()) {
                Column(horizontalAlignment=Alignment.CenterHorizontally, modifier=Modifier.padding(horizontal=16.dp)) {
                    Text(selectedVerse.arabic, color=AmberZen.copy(alpha=0.9f), fontSize=18.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center, lineHeight=28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(selectedVerse.reference, color=AmberZen.copy(alpha=0.5f), fontSize=11.sp, fontFamily=FontFamily.Monospace, textAlign=TextAlign.Center)
                }
            }
            Spacer(Modifier.height(24.dp))
            // Boot logs
            Box(modifier=Modifier.fillMaxWidth().height(90.dp).border(1.dp,CyberCyan.copy(alpha=0.1f),RoundedCornerShape(8.dp)).background(CyberCyan.copy(alpha=0.02f),RoundedCornerShape(8.dp)).padding(8.dp)) {
                LazyColumn(state=listState) {
                    items(logs) { log ->
                        Text(log, color=CyberCyan.copy(alpha=0.65f), fontSize=7.sp, fontFamily=FontFamily.Monospace, modifier=Modifier.padding(vertical=0.5.dp))
                    }
                }
            }
        }

        // Bottom section
        Column(modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=24.dp), horizontalAlignment=Alignment.CenterHorizontally) {
            AnimatedVisibility(visible=neuralUnlocked, enter=fadeIn(tween(600))) {
                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.Center) {
                    Canvas(modifier=Modifier.size(12.dp)) {
                        val ms = Path().apply { val w=size.width; val h=size.height; moveTo(w/2f,0f); lineTo(w*0.85f,h*0.2f); lineTo(w*0.85f,h*0.55f); cubicTo(w*0.85f,h*0.8f,w/2f,h,w/2f,h); cubicTo(w/2f,h,w*0.15f,h*0.8f,w*0.15f,h*0.55f); lineTo(w*0.15f,h*0.2f); close() }
                        drawPath(ms, CyberCyan.copy(alpha=0.7f), style=Stroke(1.dp.toPx()))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("NEURAL INTERFACE UNLOCKED", color=CyberCyan.copy(alpha=0.5f), fontSize=8.sp, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold, letterSpacing=2.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(modifier=Modifier.alpha(screenAlpha.value), horizontalAlignment=Alignment.CenterHorizontally) {
                Text("DESIGNED BY", color=Color.White.copy(alpha=0.25f*neonAlpha), fontSize=7.sp, letterSpacing=2.sp, fontFamily=FontFamily.Monospace, fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("ABOUDA.AL.SHEKH.YOSSEF", color=CyberCyan.copy(alpha=0.5f*neonAlpha), fontSize=10.sp, fontWeight=FontWeight.Thin, letterSpacing=4.sp, fontFamily=FontFamily.Monospace,
                    style=TextStyle(shadow=androidx.compose.ui.graphics.Shadow(color=CyberCyan.copy(alpha=0.4f), blurRadius=10f)))
            }
        }
    }
}
