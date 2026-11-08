package com.example.worker

  import android.app.NotificationChannel
  import android.app.NotificationManager
  import android.content.Context
  import android.Manifest
import android.content.pm.PackageManager
  import android.os.Build
  import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
  import androidx.work.CoroutineWorker
  import androidx.work.WorkerParameters
  import com.batoulapps.adhan.CalculationMethod
  import com.batoulapps.adhan.Coordinates
  import com.batoulapps.adhan.Madhab
  import com.batoulapps.adhan.Prayer
  import com.batoulapps.adhan.PrayerTimes
  import com.batoulapps.adhan.data.DateComponents
  import com.example.data.SovereignDataStore
  import com.example.data.remote.GeminiRepository
  import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
  import java.util.Calendar
  import java.util.Date

  class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

      private val geminiRepository = GeminiRepository()

      companion object {
          const val CHANNEL_PRAYER = "prayer_alerts"
          const val CHANNEL_SECURITY = "security_alerts"
          const val CHANNEL_WISDOM = "daily_wisdom"

          private const val PREFS_THROTTLE = "sovereign_notif_throttle"
          private const val KEY_LAST_GEMINI_ATTEMPT = "last_gemini_attempt_ms"
          private const val KEY_LAST_WISDOM = "last_wisdom_ms"
          private const val TWENTY_FOUR_HOURS_MS = 24L * 60 * 60 * 1000

          private val SHORT_DUAS = mapOf(
              Prayer.FAJR to "اللّهمّ إنّي أسألك علماً نافعاً ورزقاً طيّباً وعملاً متقبّلاً",
              Prayer.SUNRISE to "اللّهمّ اجعل في قلبي نوراً وفي بصري نوراً وفي سمعي نوراً",
              Prayer.DHUHR to "ربّ اشرح لي صدري ويسّر لي أمري واحلل عقدةً من لساني",
              Prayer.ASR to "اللّهمّ إنّي أعوذ بك من علمٍ لا ينفع ومن قلبٍ لا يخشع",
              Prayer.MAGHRIB to "اللّهمّ إنّي أسألك العافية في الدنيا والآخرة",
              Prayer.ISHA to "اللّهمّ قني عذابك يوم تبعث عبادك"
          )

          private val PRAYER_MOTIVATIONS = mapOf(
              Prayer.FAJR to "من صلّى الفجر في جماعة فهو في ذمّة الله حتّى يُمسي. أقبِل على ربّك وقلبك مطمئن",
              Prayer.SUNRISE to "أشرقت الشمس بنور ربّها، فاذكر الله واشكره على نعمة يومٍ جديد",
              Prayer.DHUHR to "في وسط انشغال يومك، توقّف وقف بين يدي الله. الصلاة خير من كلّ ما يشغلك",
              Prayer.ASR to "هي الصلاة الوسطى التي أوصانا الله بالمحافظة عليها. لا تُفرّط فيها أبداً",
              Prayer.MAGHRIB to "غربت شمس يومك، فاشكر الله على ما مضى واستغفره لما فات",
              Prayer.ISHA to "اختم يومك بصلاةٍ تُنير قلبك وتُريح بالك. من صلّى العشاء في جماعة فكأنّما قام نصف الليل"
          )

          private val DAILY_WISDOMS = listOf(
              "﴿إِنَّ مَعَ الْعُسْرِ يُسْرًا﴾ — الشرح: ٦",
              "﴿وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ﴾ — الطلاق: ٣",
              "﴿فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ﴾ — البقرة: ١٥٢",
              "﴿وَقُل رَّبِّ زِدْنِي عِلْمًا﴾ — طه: ١١٤",
              "﴿رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ﴾ — البقرة: ٢٠١",
              "﴿وَلَا تَيْأَسُوا مِن رَّوْحِ اللَّهِ إِنَّهُ لَا يَيْأَسُ مِن رَّوْحِ اللَّهِ إِلَّا الْقَوْمُ الْكَافِرُونَ﴾ — يوسف: ٨٧",
              "﴿ادْعُونِي أَسْتَجِبْ لَكُمْ﴾ — غافر: ٦٠"
          )
      }

      override suspend fun doWork(): Result {
          if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
              return Result.success()
          }

          val dataStore = SovereignDataStore(applicationContext)
          val isAr = try {
              dataStore.isArabic.first()
          } catch (cancelled: CancellationException) {
              throw cancelled
          } catch (_: Exception) {
              java.util.Locale.getDefault().language == "ar"
          }
          val throttlePrefs = applicationContext.getSharedPreferences(PREFS_THROTTLE, Context.MODE_PRIVATE)
          val nowMs = System.currentTimeMillis()

          createNotificationChannels()

          // Prayer and local wisdom must never depend on Gemini credentials or availability.
          checkAndNotifyPrayer(isAr)

          // Read the encrypted Gemini key only after the independent prayer alerts have run.
          val apiKey = try {
              dataStore.geminiApiKey.first()
          } catch (cancelled: CancellationException) {
              throw cancelled
          } catch (_: Exception) {
              ""
          }

          // Optional Gemini tip. Throttle attempts, not only successes, to avoid repeating
          // paid requests every 15 minutes while the provider is unavailable.
          val lastGeminiAttempt = throttlePrefs.getLong(KEY_LAST_GEMINI_ATTEMPT, 0L)
          if (apiKey.isNotBlank() && nowMs - lastGeminiAttempt >= TWENTY_FOUR_HOURS_MS) {
              throttlePrefs.edit().putLong(KEY_LAST_GEMINI_ATTEMPT, nowMs).apply()
              try {
                  val promptLocale = if (isAr) "Arabic" else "English"
                  val prompt = "Provide one very brief (12 words max) practical cybersecurity tip in $promptLocale. Be direct. No intro."
                  val res = geminiRepository.generateText(
                      systemInstruction = "You are a concise cybersecurity educator.",
                      input = prompt,
                      apiKey = apiKey
                  )
                  if (res.isNotBlank()) {
                      showNotification(
                          title = if (isAr) "نصيحة أمنية يومية" else "Daily Security Tip",
                          message = res.trim(),
                          id = 1002,
                          channelId = CHANNEL_SECURITY
                      )
                  }
              } catch (e: Exception) {
                  if (e is CancellationException) throw e
                  // A failed request is not retried on every 15-minute worker run.
              }
          }

          // 3. Daily Quranic Wisdom — throttled to once every 24 hours
          val lastWisdom = throttlePrefs.getLong(KEY_LAST_WISDOM, 0L)
          if (nowMs - lastWisdom >= TWENTY_FOUR_HOURS_MS) {
              sendDailyWisdom(isAr)
              throttlePrefs.edit().putLong(KEY_LAST_WISDOM, nowMs).apply()
          }

          return Result.success()
      }

      private fun sendDailyWisdom(isAr: Boolean) {
          val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
          val wisdom = DAILY_WISDOMS[dayOfYear % DAILY_WISDOMS.size]
          showNotification(
              title = if (isAr) "حكمة اليوم من القرآن الكريم" else "Daily Quranic Wisdom",
              message = wisdom,
              id = 5000 + dayOfYear,
              channelId = CHANNEL_WISDOM
          )
      }

      private fun createNotificationChannels() {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

              val prayerChannel = NotificationChannel(
                  CHANNEL_PRAYER,
                  "تنبيهات الصلاة",
                  NotificationManager.IMPORTANCE_HIGH
              ).apply {
                  description = "تنبيهات مواقيت الصلاة والأذان"
                  enableVibration(true)
                  vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                  enableLights(true)
                  lightColor = android.graphics.Color.GREEN
                  val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                  setSound(soundUri, android.media.AudioAttributes.Builder()
                      .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                      .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                      .build())
              }

              val securityChannel = NotificationChannel(
                  CHANNEL_SECURITY,
                  "نصائح أمنية",
                  NotificationManager.IMPORTANCE_HIGH
              ).apply {
                  description = "نصائح أمنية عامة مدعومة بالذكاء الاصطناعي"
                  enableVibration(true)
                  vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 500)
                  enableLights(true)
                  lightColor = android.graphics.Color.RED
                  val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                  setSound(soundUri, android.media.AudioAttributes.Builder()
                      .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                      .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                      .build())
              }

              val wisdomChannel = NotificationChannel(
                  CHANNEL_WISDOM,
                  "حكمة يومية",
                  NotificationManager.IMPORTANCE_DEFAULT
              ).apply {
                  description = "حكمة يومية من القرآن الكريم"
                  enableVibration(true)
                  vibrationPattern = longArrayOf(0, 200, 100, 200)
                  enableLights(true)
                  lightColor = android.graphics.Color.YELLOW
              }

              notificationManager.createNotificationChannel(prayerChannel)
              notificationManager.createNotificationChannel(securityChannel)
              notificationManager.createNotificationChannel(wisdomChannel)
          }
      }

      private fun checkAndNotifyPrayer(isAr: Boolean) {
          val prefs = applicationContext.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
          val lat = prefs.getFloat("network_lat", 33.5138f).toDouble().takeIf { it in -90.0..90.0 } ?: 33.5138
          val lon = prefs.getFloat("network_lon", 36.2765f).toDouble().takeIf { it in -180.0..180.0 } ?: 36.2765
          val coordinates = Coordinates(lat, lon)
          val now = Date()
          val today = PrayerTimes(
              coordinates,
              DateComponents.from(now),
              CalculationMethod.UMM_AL_QURA.parameters.also { it.madhab = Madhab.SHAFI }
          )
          val tomorrowCalendar = Calendar.getInstance().apply {
              time = now
              add(Calendar.DAY_OF_YEAR, 1)
          }
          val tomorrow = PrayerTimes(
              coordinates,
              DateComponents.from(tomorrowCalendar.time),
              CalculationMethod.UMM_AL_QURA.parameters.also { it.madhab = Madhab.SHAFI }
          )

          // Sunrise is not one of the five obligatory prayers and is excluded.
          val scheduledPrayers = listOf(
              Prayer.FAJR to today.fajr,
              Prayer.DHUHR to today.dhuhr,
              Prayer.ASR to today.asr,
              Prayer.MAGHRIB to today.maghrib,
              Prayer.ISHA to today.isha,
              Prayer.FAJR to tomorrow.fajr
          )
          val arabicNames = mapOf(
              Prayer.FAJR to "الفجر",
              Prayer.DHUHR to "الظهر",
              Prayer.ASR to "العصر",
              Prayer.MAGHRIB to "المغرب",
              Prayer.ISHA to "العشاء"
          )
          val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
          val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT)

          for ((prayer, prayerDate) in scheduledPrayers) {
              val minutesUntil = kotlin.math.floor((prayerDate.time - now.time) / 60_000.0).toInt()
              val prayerDateKey = dateFormat.format(prayerDate)
              val timeText = timeFormat.format(prayerDate)
              val lastNotifiedSoon = prefs.getString("last_5min_${prayer.name}", "")
              val lastNotifiedAtTime = prefs.getString("last_exact_${prayer.name}", "")
              val name = if (isAr) arabicNames[prayer] ?: prayer.name else prayer.name

              // WorkManager is not an exact alarm; wider windows reduce missed reminders.
              if (minutesUntil in 1..30 && lastNotifiedSoon != prayerDateKey) {
                  val dua = SHORT_DUAS[prayer] ?: ""
                  showNotification(
                      title = if (isAr) "اقترب موعد صلاة $name — $timeText"
                              else "${prayer.name} is approaching — $timeText",
                      message = if (isAr)
                          "بقي نحو $minutesUntil دقيقة على صلاة $name. توضّأ واستعد.${if (dua.isNotBlank()) "\n\n$dua" else ""}"
                      else
                          "About $minutesUntil minutes until ${prayer.name}. Prepare for prayer.${if (dua.isNotBlank()) "\n\n$dua" else ""}",
                      id = 2000 + prayer.ordinal(),
                      channelId = CHANNEL_PRAYER
                  )
                  prefs.edit().putString("last_5min_${prayer.name}", prayerDateKey).apply()
              }

              if (minutesUntil in -30..0 && lastNotifiedAtTime != prayerDateKey) {
                  val motivation = PRAYER_MOTIVATIONS[prayer] ?: ""
                  showNotification(
                      title = if (isAr) "حان وقت صلاة $name"
                              else "It is time for ${prayer.name}",
                      message = if (isAr)
                          "حيّ على الصلاة، حيّ على الفلاح.${if (motivation.isNotBlank()) "\n\n$motivation" else ""}"
                      else
                          "Come to prayer, come to success.${if (motivation.isNotBlank()) "\n\n$motivation" else ""}",
                      id = 3000 + prayer.ordinal(),
                      channelId = CHANNEL_PRAYER
                  )
                  prefs.edit().putString("last_exact_${prayer.name}", prayerDateKey).apply()
              }
          }
      }

      private fun Prayer.ordinal(): Int = when (this) {
          Prayer.FAJR -> 0; Prayer.SUNRISE -> 1; Prayer.DHUHR -> 2
          Prayer.ASR -> 3; Prayer.MAGHRIB -> 4; Prayer.ISHA -> 5; else -> 99
      }

      private fun showNotification(title: String, message: String, id: Int, channelId: String = CHANNEL_PRAYER) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
              applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                  PackageManager.PERMISSION_GRANTED
          ) {
              return
          }

          val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

          val icon = when (channelId) {
              CHANNEL_PRAYER -> android.R.drawable.btn_star_big_on
              CHANNEL_SECURITY -> android.R.drawable.stat_sys_warning
              CHANNEL_WISDOM -> android.R.drawable.ic_menu_info_details
              else -> android.R.drawable.stat_sys_warning
          }

          val category = when (channelId) {
              CHANNEL_PRAYER -> NotificationCompat.CATEGORY_ALARM
              CHANNEL_SECURITY -> NotificationCompat.CATEGORY_ERROR
              CHANNEL_WISDOM -> NotificationCompat.CATEGORY_RECOMMENDATION
              else -> NotificationCompat.CATEGORY_MESSAGE
          }

          val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
          val notification = NotificationCompat.Builder(applicationContext, channelId)
              .setSmallIcon(icon)
              .setContentTitle(title)
              .setContentText(message)
              .setStyle(NotificationCompat.BigTextStyle().bigText(message))
              .setPriority(
                  if (channelId == CHANNEL_WISDOM) NotificationCompat.PRIORITY_DEFAULT
                  else NotificationCompat.PRIORITY_MAX
              )
              .setCategory(category)
              .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
              .setSound(soundUri)
              .setVibrate(longArrayOf(0, 300, 100, 300, 100, 500))
              .setAutoCancel(true)
              .build()

          runCatching { notificationManager.notify(id, notification) }
      }
  }
  