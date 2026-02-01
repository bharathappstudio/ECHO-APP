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
    private const val APP_PASSWORD = "vcrimtzjkniegcbh"

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

            val year = SimpleDateFormat(
                "yyyy",
                Locale.getDefault()
            ).format(Date())

            val htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Login Successful</title>
</head>

<body style="
  margin:0;
  padding:0;
  background:#E8F5E9;
  font-family: Roboto, 'Segoe UI', Inter, Arial, sans-serif;
">

<table width="100%" cellpadding="0" cellspacing="0">
<tr>
<td align="center" style="padding:48px 16px;">

<table width="440" cellpadding="0" cellspacing="0" style="
  background:#FFFFFF;
  border-radius:20px;
  box-shadow:0 12px 36px rgba(0,0,0,0.08);
  overflow:hidden;
">

<tr>
<td style="
  background:#20211A;
  padding:32px;
  color:#FFFFFF;
">
  <h1 style="
    margin:0;
    font-size:22px;
    font-weight:600;
    letter-spacing:0.3px;
  ">
    Login Successful
  </h1>
  <p style="
    margin:8px 0 0;
    font-size:14px;
    opacity:0.9;
  ">
    Your account was accessed successfully
  </p>
</td>
</tr>

<tr>
<td style="padding:28px;">

<p style="
  margin:0 0 20px;
  font-size:14px;
  color:#1F2937;
  line-height:1.6;
">
  A successful login new account detected.
</p>

<table width="100%" cellpadding="12" cellspacing="0" style="
  background:#F1F8F4;
  border-radius:16px;
  font-size:14px;
  color:#1F2937;
">

<tr>
<td style="font-weight:500; width:40%;">Name</td>
<td>$userName</td>
</tr>

<tr>
<td style="font-weight:500;">Email</td>
<td>$userEmail</td>
</tr>

<tr>
<td style="font-weight:500;">User ID</td>
<td style="word-break:break-all;">$userUid</td>
</tr>

<tr>
<td style="font-weight:500;">Login Provider</td>
<td>$provider</td>
</tr>

<tr>
<td style="font-weight:500;">Application</td>
<td>$packageName</td>
</tr>

<tr>
<td style="font-weight:500;">Login Time</td>
<td>$time</td>
</tr>

</table>

<p style="
  margin:24px 0 0;
  font-size:12px;
  color:#4B5563;
  line-height:1.6;
">
  If you do not recognize this login activity, please secure your account immediately.
</p>

</td>
</tr>

<tr>
<td style="
  background:#F0FDF4;
  padding:18px;
  text-align:center;
  font-size:12px;
  color:#4B5563;
">
  Echo App-UI-Studio13<br>
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

            val session = Session.getInstance(
                props,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD)
                    }
                }
            )

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL))
                setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(SENDER_EMAIL)
                )
                subject = "Login Successful"
                setContent(htmlContent, "text/html; charset=utf-8")
            }

            Transport.send(message)
            Log.d("MailSender", "Login successful mail sent")

        } catch (e: Exception) {
            Log.e("MailSender", "Mail error: ${e.message}", e)
        }
    }
}
