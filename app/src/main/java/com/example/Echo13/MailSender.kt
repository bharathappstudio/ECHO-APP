package com.ai.Echo

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object MailSender {

    private const val SENDER_EMAIL = "jarvisvbharath11@gmail.com"
    private const val APP_PASSWORD = "vcrimtzjkniegcbh" // App password only

    suspend fun sendLoginMail(
        userName: String,
        userEmail: String,
        userUid: String,
        provider: String,
        packageName: String
    ) = withContext(Dispatchers.IO) {

        try {
            val time = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            ).format(Date())

            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

            val htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Login Alert</title>
</head>

<body style="margin:0;padding:0;background:#F2F4F8;font-family:Roboto,Segoe UI,Arial,sans-serif;">

<table width="100%" cellpadding="0" cellspacing="0">
<tr>
<td align="center" style="padding:32px 12px;">

<table width="420" cellpadding="0" cellspacing="0" style="
  background:#FFFFFF;
  border-radius:20px;
  box-shadow:0 12px 40px rgba(0,0,0,0.08);
  overflow:hidden;
">

<!-- HEADER -->
<tr>
<td style="
  background:linear-gradient(135deg,#6750A4,#4F378B);
  padding:28px;
  color:#FFFFFF;
">
  <h1 style="margin:0;font-size:22px;font-weight:600;">
    🎉 Login Successful
  </h1>
  <p style="margin:8px 0 0;font-size:14px;opacity:0.9;">
    Welcome back to <b>Echo App</b> 😊
  </p>
</td>
</tr>

<!-- CONTENT -->
<tr>
<td style="padding:26px;">

<p style="font-size:14px;color:#1F2937;margin:0 0 18px;">
  A new login was detected with the following details 👇
</p>

<table width="100%" cellpadding="10" cellspacing="0" style="
  background:#F8FAFF;
  border-radius:14px;
  font-size:14px;
  color:#111827;
">

<tr>
<td><b>👤 Name</b></td>
<td>$userName</td>
</tr>

<tr>
<td><b>📧 Email</b></td>
<td>$userEmail</td>
</tr>

<tr>
<td><b>🆔 UID</b></td>
<td style="word-break:break-all;">$userUid</td>
</tr>

<tr>
<td><b>🔐 Provider</b></td>
<td>$provider</td>
</tr>

<tr>
<td><b>📦 App</b></td>
<td>$packageName</td>
</tr>

<tr>
<td><b>⏰ Time</b></td>
<td>$time</td>
</tr>

</table>

<p style="margin:18px 0 0;font-size:12px;color:#6B7280;">
  If this wasn’t you, please secure your account immediately 🚨
</p>

</td>
</tr>

<!-- FOOTER -->
<tr>
<td style="
  background:#F5F6FA;
  padding:16px;
  text-align:center;
  font-size:12px;
  color:#6B7280;
">
  💜 Echo App • Smart & Secure<br>
  © $year
</td>
</tr>

</table>

</td>
</tr>
</table>

</body>
</html>
""".trimIndent()

            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD)
                }
            })

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL))
                setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(SENDER_EMAIL)
                )
                subject = "🎉 Login Successful – Echo App"
                setContent(htmlContent, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.d("MailSender", "✅ Material 3 email sent successfully")

        } catch (e: Exception) {
            Log.e("MailSender", "❌ Email failed: ${e.message}", e)
        }
    }
}
