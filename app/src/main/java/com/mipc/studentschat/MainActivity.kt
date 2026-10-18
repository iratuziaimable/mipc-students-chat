package com.mipc.studentschat

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.concurrent.TimeUnit
import import com.google.firebase.auth.PhoneAuthProvider
import import com.google.firebase.auth.PhoneAuthOptions
import import com.google.firebase.auth.PhoneAuthCredential
import import com.google.firebase.auth.FirebaseAuth
import import com.google.firebase.FirebaseException
import import android.text.InputType

class MainActivity : Activity() {

    private val green = Color.rgb(0, 137, 102)
    private val dark = Color.rgb(24, 35, 32)
    private val muted = Color.rgb(110, 124, 119)
    private val bg = Color.rgb(247, 249, 248)
    private lateinit var content: LinearLayout
    private lateinit var pageTitle: TextView
    private var currentTab = "Chats"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null


    private data class Chat(
        val name: String,
        val message: String,
        val time: String,
        val emoji: String,
        val unread: Int = 0
    )

    private val chats = listOf(
        Chat("MIPC Students", "Muraho neza banyeshuri 👋", "09:45", "🎓", 3),
        Chat("Class Representative", "Dore gahunda y'ejo.", "09:20", "👨‍🎓", 1),
        Chat("Civil Engineering", "Concrete practical iratangira saa mbili.", "08:55", "🏗️", 5),
        Chat("Study Group", "Wabonye notes za Physics?", "Ejo", "📚"),
        Chat("My Friend", "Turabonana kuri campus.", "Ejo", "🙂"),
        Chat("MIPC Announcements", "Welcome to our student community.", "Ku wa mbere", "📢")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = green
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        if (auth.currentUser != null) {
            showHome()
        } else {
            showLogin()
        }
    }

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 16): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = dark,
        bold: Boolean = false
    ): TextView = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
    }

    private fun row(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun addText(
        parent: LinearLayout,
        value: String,
        size: Float,
        color: Int = dark,
        bold: Boolean = false
    ): TextView {
        val t = text(value, size, color, bold)
        parent.addView(t)
        return t
    }


    private fun showLogin() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        val title = text("MIPC Students Chat", 27f, green, true)
        title.gravity = Gravity.CENTER
        root.addView(title)

        val subtitle = text("Injira ukoresheje nimero ya telefone", 16f, muted)
        subtitle.gravity = Gravity.CENTER
        val subtitleParams = LinearLayout.LayoutParams(-1, -2)
        subtitleParams.topMargin = dp(12)
        root.addView(subtitle, subtitleParams)

        val phone = EditText(this).apply {
            hint = "Urugero: +2507XXXXXXXX"
            textSize = 16f
            inputType = InputType.TYPE_CLASS_PHONE
            setSingleLine(true)
        }
        val phoneParams = LinearLayout.LayoutParams(-1, -2)
        phoneParams.topMargin = dp(28)
        root.addView(phone, phoneParams)

        val send = Button(this).apply {
            text = "Ohereza kode ya SMS"
            isAllCaps = false
        }
        val sendParams = LinearLayout.LayoutParams(-1, -2)
        sendParams.topMargin = dp(18)
        root.addView(send, sendParams)

        val note = text(
            "Andika nimero iriho country code, urugero +250. Firebase ishobora kugenzura app mbere yo kohereza SMS.",
            13f, muted
        )
        note.gravity = Gravity.CENTER
        val noteParams = LinearLayout.LayoutParams(-1, -2)
        noteParams.topMargin = dp(14)
        root.addView(note, noteParams)

        send.setOnClickListener {
            val number = phone.text.toString().trim()
            if (!number.matches(Regex("^\\+[1-9][0-9]{7,14}$"))) {
                phone.error = "Andika nimero yuzuye, urugero +2507XXXXXXXX"
                return@setOnClickListener
            }
            startPhoneVerification(number)
        }

        setContentView(root)
    }

    private fun startPhoneVerification(number: String) {
        Toast.makeText(this, "Turimo gutegura SMS verification...", Toast.LENGTH_SHORT).show()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@MainActivity,
                    "Verification yanze: ${e.localizedMessage ?: "ongera ugerageze"}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                resendToken = token
                showOtp()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun showOtp() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            setBackgroundColor(Color.WHITE)
        }

        val title = text("Emeza nimero yawe", 25f, green, true)
        title.gravity = Gravity.CENTER
        root.addView(title)

        val code = EditText(this).apply {
            hint = "Kode ya SMS"
            textSize = 20f
            gravity = Gravity.CENTER
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
        val codeParams = LinearLayout.LayoutParams(-1, -2)
        codeParams.topMargin = dp(24)
        root.addView(code, codeParams)

        val verify = Button(this).apply {
            text = "Emeza kode"
            isAllCaps = false
        }
        val verifyParams = LinearLayout.LayoutParams(-1, -2)
        verifyParams.topMargin = dp(16)
        root.addView(verify, verifyParams)

        verify.setOnClickListener {
            val otp = code.text.toString().trim()
            val id = verificationId
            if (otp.length < 4 || id == null) {
                code.error = "Andika kode wahawe kuri SMS"
                return@setOnClickListener
            }
            val credential = PhoneAuthProvider.getCredential(id, otp)
            signInWithPhoneCredential(credential)
        }

        setContentView(root)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Winjiye neza!", Toast.LENGTH_SHORT).show()
                    showHome()
                } else {
                    Toast.makeText(
                        this,
                        "Kwinjira byanze: ${task.exception?.localizedMessage ?: "ongera ugerageze"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(12))
            setBackgroundColor(green)
        }

        val top = row()
        val brand = text("MIPC Students Chat", 25f, Color.WHITE, true)
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        val profile = text("👤", 26f)
        profile.setPadding(dp(10), dp(5), dp(10), dp(5))
        profile.setOnClickListener { showProfile() }
        top.addView(profile)
        header.addView(top)

        val subtitle = text("Connect • Learn • Share", 13f, Color.rgb(225, 245, 237))
        val subParams = LinearLayout.LayoutParams(-2, -2)
        subParams.topMargin = dp(5)
        header.addView(subtitle, subParams)

        val search = EditText(this).apply {
            hint = "🔍  Search conversations"
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(14), 0, dp(14), 0)
            background = rounded(Color.WHITE, 24)
        }
        val searchParams = LinearLayout.LayoutParams(-1, dp(46))
        searchParams.topMargin = dp(16)
        header.addView(search, searchParams)

        root.addView(header)

        val tabs = row().apply {
            setPadding(dp(8), dp(12), dp(8), dp(12))
            setBackgroundColor(Color.WHITE)
        }
        listOf("Chats", "Status", "Calls").forEach { tab ->
            val t = text(tab, 15f, if (tab == currentTab) green else muted, tab == currentTab)
            t.gravity = Gravity.CENTER
            t.setPadding(dp(12), dp(8), dp(12), dp(8))
            if (tab == currentTab) t.background = rounded(Color.rgb(225, 245, 237), 20)
            t.setOnClickListener {
                currentTab = tab
                when (tab) {
                    "Chats" -> showHome()
                    "Status" -> showStatus()
                    "Calls" -> showCalls()
                }
            }
            tabs.addView(t, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(tabs)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), dp(8))
        }
        val scroll = ScrollView(this)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams( -1, 0, 1f))

        addText(content, "YOUR CONVERSATIONS", 11f, muted, true)
        val filter = search.text
        chats.filter {
            it.name.contains(filter, true) || it.message.contains(filter, true)
        }.forEach { chat ->
            val item = row().apply {
                setPadding(0, dp(13), 0, dp(13))
                isClickable = true
                setOnClickListener { showConversation(chat.name) }
            }

            val avatar = TextView(this).apply {
                text = chat.emoji
                textSize = 25f
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(232, 245, 239), 40)
            }
            item.addView(avatar, LinearLayout.LayoutParams(dp(52), dp(52)))

            val details = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
            }
            details.addView(text(chat.name, 16f, dark, true))
            val msg = text(chat.message, 13f, muted)
            msg.maxLines = 1
            msg.ellipsize = android.text.TextUtils.TruncateAt.END
            val msgParams = LinearLayout.LayoutParams(-1, -2)
            msgParams.topMargin = dp(5)
            details.addView(msg, msgParams)

            item.addView(details, LinearLayout.LayoutParams(0, -2, 1f))

            val right = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
            right.addView(text(chat.time, 11f, muted))
            if (chat.unread > 0) {
                val badge = text(chat.unread.toString(), 11f, Color.WHITE, true)
                badge.gravity = Gravity.CENTER
                badge.background = rounded(green, 30)
                val bp = LinearLayout.LayoutParams(dp(23), dp(23))
                bp.topMargin = dp(5)
                right.addView(badge, bp)
            }
            item.addView(right)
            content.addView(item)
            val line = View(this).apply { setBackgroundColor(Color.rgb(239, 242, 240)) }
            val lp = LinearLayout.LayoutParams(-1, dp(1))
            lp.leftMargin = dp(64)
            content.addView(line, lp)
        }

        val bottom = row().apply {
            setPadding(dp(15), dp(10), dp(15), dp(10))
            setBackgroundColor(Color.WHITE)
        }
        val navItems = listOf("💬\nChats", "🟢\nUpdates", "📞\nCalls", "⚙️\nSettings")
        navItems.forEach { item ->
            val t = text(item, 12f, if (item.startsWith("💬")) green else muted)
            t.gravity = Gravity.CENTER
            t.setPadding(0, dp(4), 0, dp(4))
            t.setOnClickListener {
                when {
                    item.startsWith("💬") -> { currentTab = "Chats"; showHome() }
                    item.startsWith("🟢") -> showStatus()
                    item.startsWith("📞") -> showCalls()
                    else -> showProfile()
                }
            }
            bottom.addView(t, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(bottom)
        setContentView(root)

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                content.removeAllViews()
                addText(content, "YOUR CONVERSATIONS", 11f, muted, true)
                chats.filter { it.name.contains(s.toString(), true) || it.message.contains(s.toString(), true) }
                    .forEach { c ->
                        val t = text("${c.emoji}   ${c.name}\n       ${c.message}", 15f)
                        t.setPadding(0, dp(14), 0, dp(14))
                        t.setOnClickListener { showConversation(c.name) }
                        content.addView(t)
                    }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun page(title: String): Pair<LinearLayout, LinearLayout> {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
            setBackgroundColor(green)
        }
        header.addView(text(title, 25f, Color.WHITE, true))
        root.addView(header)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        val scroll = ScrollView(this)
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        return Pair(root, body)
    }

    private fun addBottom(root: LinearLayout) {
        val nav = row().apply {
            setPadding(dp(10), dp(12), dp(10), dp(12))
            setBackgroundColor(Color.WHITE)
        }
        listOf("💬\nChats", "🟢\nUpdates", "📞\nCalls", "⚙️\nSettings").forEach { item ->
            val t = text(item, 12f, muted)
            t.gravity = Gravity.CENTER
            t.setOnClickListener {
                when {
                    item.startsWith("💬") -> { currentTab = "Chats"; showHome() }
                    item.startsWith("🟢") -> showStatus()
                    item.startsWith("📞") -> showCalls()
                    else -> showProfile()
                }
            }
            nav.addView(t, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(nav)
    }

    private fun showStatus() {
        val (root, body) = page("Updates")
        addText(body, "Status", 22f, dark, true)
        val info = text("Share a thought with your classmates.", 14f, muted)
        val ip = LinearLayout.LayoutParams(-1, -2)
        ip.topMargin = dp(8)
        body.addView(info, ip)

        val input = EditText(this).apply {
            hint = "What is on your mind?"
            minLines = 3
            gravity = Gravity.TOP
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = rounded(Color.WHITE, 14)
        }
        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(18)
        body.addView(input, p)

        val button = Button(this).apply {
            text = "Share status"
            setBackgroundColor(green)
            setTextColor(Color.WHITE)
            setOnClickListener {
                val value = input.text.toString().trim()
                if (value.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Banza wandike status.", Toast.LENGTH_SHORT).show()
                } else {
                    getSharedPreferences("mipc_chat", MODE_PRIVATE).edit()
                        .putString("my_status", value).apply()
                    Toast.makeText(this@MainActivity, "Status yabitswe kuri iyi telefoni.", Toast.LENGTH_LONG).show()
                    showStatus()
                }
            }
        }
        val bp = LinearLayout.LayoutParams(-1, -2)
        bp.topMargin = dp(12)
        body.addView(button, bp)

        val saved = getSharedPreferences("mipc_chat", MODE_PRIVATE).getString("my_status", "") ?: ""
        if (saved.isNotBlank()) {
            val card = text("MY STATUS\n\n$saved", 16f, dark)
            card.setPadding(dp(16), dp(18), dp(16), dp(18))
            card.background = rounded(Color.WHITE, 14)
            val cp = LinearLayout.LayoutParams(-1, -2)
            cp.topMargin = dp(22)
            body.addView(card, cp)
        }
        addBottom(root)
        setContentView(root)
    }

    private fun showCalls() {
        val (root, body) = page("Calls")
        addText(body, "Stay connected", 22f, dark, true)
        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(8)
        body.addView(text("Your recent calls will appear here.", 15f, muted), p)
        val card = text("📞   Voice calls\n\nVideo calls and real-time calling will be added after online accounts are set up.", 16f)
        card.setPadding(dp(18), dp(18), dp(18), dp(18))
        card.background = rounded(Color.WHITE)
        val cp = LinearLayout.LayoutParams(-1, -2)
        cp.topMargin = dp(20)
        body.addView(card, cp)
        addBottom(root)
        setContentView(root)
    }

    private fun showProfile() {
        val (root, body) = page("Your account")
        addText(body, "👤", 44f)
        val nameInput = EditText(this).apply {
            hint = "Your display name"
            setText(getSharedPreferences("mipc_chat", MODE_PRIVATE).getString("name", "MIPC Student"))
            textSize = 17f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE)
        }
        val p = LinearLayout.LayoutParams(-1, -2)
        p.topMargin = dp(15)
        body.addView(nameInput, p)

        val phoneInput = EditText(this).apply {
            hint = "Phone number (e.g. +250...)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setText(getSharedPreferences("mipc_chat", MODE_PRIVATE).getString("phone", ""))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE)
        }
        val pp = LinearLayout.LayoutParams(-1, -2)
        pp.topMargin = dp(12)
        body.addView(phoneInput, pp)

        val save = Button(this).apply {
            text = "Save profile"
            setOnClickListener {
                getSharedPreferences("mipc_chat", MODE_PRIVATE).edit()
                    .putString("name", nameInput.text.toString().trim())
                    .putString("phone", phoneInput.text.toString().trim())
                    .apply()
                Toast.makeText(this@MainActivity, "Amakuru yabitswe kuri iyi telefoni.", Toast.LENGTH_LONG).show()
            }
        }
        val sp = LinearLayout.LayoutParams(-1, -2)
        sp.topMargin = dp(15)
        body.addView(save, sp)

        val note = text("MIPC Students Chat\nVersion 1.1\n\nPhone-number verification and online messaging will require a secure online service.", 14f, muted)
        val np = LinearLayout.LayoutParams(-1, -2)
        np.topMargin = dp(28)
        body.addView(note, np)
        addBottom(root)
        setContentView(root)
    }

    private fun showConversation(name: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }
        val header = row().apply {
            setPadding(dp(16), dp(18), dp(16), dp(18))
            setBackgroundColor(green)
        }
        val back = text("‹", 34f, Color.WHITE, true)
        back.setOnClickListener { currentTab = "Chats"; showHome() }
        header.addView(back)
        val title = text("  $name", 20f, Color.WHITE, true)
        header.addView(title)
        root.addView(header)

        val messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        val history = getSharedPreferences("mipc_chat", MODE_PRIVATE).getString("messages_$name", "") ?: ""
        if (history.isBlank()) {
            val welcome = text("Muraho! 👋\n\nIbiganiro byawe na $name bizagaragara hano.", 16f, muted)
            welcome.setPadding(dp(12), dp(15), dp(12), dp(15))
            messages.addView(welcome)
        } else {
            history.split("\n").filter { it.isNotBlank() }.forEach {
                val bubble = text(it, 16f, dark)
                bubble.setPadding(dp(12), dp(10), dp(12), dp(10))
                bubble.background = rounded(Color.WHITE, 12)
                val bp = LinearLayout.LayoutParams(-2, -2)
                bp.bottomMargin = dp(8)
                messages.addView(bubble, bp)
            }
        }
        val scroll = ScrollView(this)
        scroll.addView(messages)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val composer = row().apply {
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setBackgroundColor(Color.WHITE)
        }
        val input = EditText(this).apply {
            hint = "Type a message..."
            maxLines = 4
            setPadding(dp(12), 0, dp(12), 0)
            background = rounded(bg, 22)
        }
        composer.addView(input, LinearLayout.LayoutParams(0, dp(48), 1f))
        val send = TextView(this).apply {
            text = "➤"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(green)
            setPadding(dp(12), 0, dp(8), 0)
            setOnClickListener {
                val message = input.text.toString().trim()
                if (message.isNotEmpty()) {
                    val old = getSharedPreferences("mipc_chat", MODE_PRIVATE)
                        .getString("messages_$name", "") ?: ""
                    getSharedPreferences("mipc_chat", MODE_PRIVATE).edit()
                        .putString("messages_$name", old + (if (old.isBlank()) "" else "\n") + message)
                        .apply()
                    showConversation(name)
                }
            }
        }
        composer.addView(send, LinearLayout.LayoutParams(dp(48), dp(48)))
        root.addView(composer)
        setContentView(root)
    }
}
